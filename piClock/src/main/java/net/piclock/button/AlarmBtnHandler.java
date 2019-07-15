package net.piclock.button;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

//import net.piclock.arduino.ArduinoCmd;
import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ButtonState;
import net.piclock.main.Constants;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.SwingContext;
import net.piclock.thread.Alarm;

public class AlarmBtnHandler  implements ButtonChangeListener{

	private static final Logger logger = Logger.getLogger( AlarmBtnHandler.class.getName() );

	@Override
	public void stateChanged(ButtonState state) {

		boolean alarmTriggered = Alarm.isAlarmTriggered();
		
		logger.log(Level.CONFIG, "Button State: " + state + " alarmTriggered: " + alarmTriggered);
		try {
			if (alarmTriggered && state == ButtonState.HIGH) {		
				shutDownAlarm();	
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in button handler" , e);
			
		}
	}	
	
	private void shutDownAlarm() throws InterruptedException, UnsupportedBusNumberException, IOException {
		logger.log(Level.CONFIG, "Turning off alarm");
		
		Message msg = new Message("off - Btn Handler");
		SwingContext.getInstance().sendMessage(Constants.TURN_OFF_ALARM, msg);
		
	}


}