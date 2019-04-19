package net.piclock.button;


import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.arduino.ArduinoCmd;
import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ButtonState;
import net.piclock.thread.Alarm;

public class AlarmBtnHandler  implements ButtonChangeListener{

	private static final Logger logger = Logger.getLogger( AlarmBtnHandler.class.getName() );
	private boolean active = false;
	
	@Override
	public void stateChanged(ButtonState state) {

		boolean alarmTriggered = Alarm.isAlarmTriggered();
		
		logger.log(Level.CONFIG, "Button State: " + state + " alarmTriggered: " + alarmTriggered);
		try {
			if (alarmTriggered && state == ButtonState.HIGH) {
				System.out.println("Turning off alarm");
				Alarm.turnOffAlarmSound();
				deactivateListener();
				
				
				ArduinoCmd cm = ArduinoCmd.getInstance();
				cm.stopBtnMonitor();
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in button handler" , e);
			
		}


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