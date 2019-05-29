package net.piclock.button;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import net.piclock.arduino.ArduinoCmd;
import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ButtonState;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.SwingContext;
import net.piclock.thread.Alarm;

public class AlarmBtnHandler  implements ButtonChangeListener{

	private static final Logger logger = Logger.getLogger( AlarmBtnHandler.class.getName() );
	private boolean active = false;
	
	private  Thread alarmAutoOff; 
	
	@Override
	public void stateChanged(ButtonState state) {

		boolean alarmTriggered = Alarm.isAlarmTriggered();
		
		logger.log(Level.CONFIG, "Button State: " + state + " alarmTriggered: " + alarmTriggered);
		try {
			if (alarmTriggered && state == ButtonState.HIGH) {		
				autoAlarmShutOff(false);
				shutDownAlarm();	
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in button handler" , e);
			
		}
	}

	public void autoAlarmShutOff( boolean startThread) {
		logger.log(Level.CONFIG, "autoAlarmShutOff. Start: " + startThread );

		if (alarmAutoOff != null && alarmAutoOff.isAlive()) {
			alarmAutoOff.interrupt();
			logger.log(Level.CONFIG, "autoAlarmShutOff not null and interrupted");
		}

		if (startThread) {
			alarmAutoOff = new Thread(new Runnable() {

				@Override
				public void run() {
					logger.log(Level.CONFIG, "autoAlarmShutOff: Auto Off start in run");
					try {
						Thread.sleep(60000);
						
						shutDownAlarm();
					}catch(InterruptedException | IOException | UnsupportedBusNumberException e) {
						Thread.currentThread().interrupt();
					}
					logger.log(Level.CONFIG, "autoAlarmShutOff: end run method");
				}		
			});

			alarmAutoOff.start();
		}
	}
	
	private void shutDownAlarm() throws InterruptedException, UnsupportedBusNumberException, IOException {
		logger.log(Level.CONFIG, "Turning off alarm");
		
		Message msg = new Message("off - Btn Handler");
		SwingContext.getInstance().sendMessage(msg);
		
		deactivateListener();		
		
		ArduinoCmd cm = ArduinoCmd.getInstance();
		cm.stopBtnMonitor();
	}
	
	@Override
	public boolean isActive() {
		
		return active;
	}

	@Override
	public void setListenerActive() {
		this.active = true;
		
	}

	@Override
	public void deactivateListener() {
		active = false;
	}
}