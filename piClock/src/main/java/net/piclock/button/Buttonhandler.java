package net.piclock.button;

import net.piclock.enums.Buzzer;
import net.piclock.main.Constants;
import net.piclock.main.PiHandler;
import net.piclock.main.Preferences;
import net.piclock.swing.component.SwingContext;
import net.piclock.view.AlarmView;

public class Buttonhandler {

	private SwingContext ct = SwingContext.getInstance();
	//TODO test with pi4j
	public void listerToButton() throws InterruptedException{
		
		//check status of all
		if (AlarmView.alarmOn){
			AlarmView.alarmOn = false; // turn off alarm
			//get the buzzer type
			Preferences pref = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
			Buzzer buzzer = Buzzer.valueOf(pref.getAlarmType());
			PiHandler.turnOffAlarm(buzzer);
			
		}else if (!PiHandler.screenOn){
			//turn screen on
			PiHandler.turnOnScreen(true);
			PiHandler.autoShutDownScreen();
		}
		
		
		
	}
}