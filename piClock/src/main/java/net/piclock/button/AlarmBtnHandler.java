package net.piclock.button;


import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ButtonState;
import net.piclock.thread.Alarm;

public class AlarmBtnHandler  implements ButtonChangeListener{

	private static final Logger logger = Logger.getLogger( AlarmBtnHandler.class.getName() );
	
	@Override
	public void stateChanged(ButtonState state) {

		System.out.println("Button state: " + state);
		try {
			if (Alarm.isAlarmTriggered() && state == ButtonState.HIGH) {
				System.out.println("Turning off alarm");
				Alarm.turnOffAlarmSound();
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in button handler" , e);
			
		}


	}
}