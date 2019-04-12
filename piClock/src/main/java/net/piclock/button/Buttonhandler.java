package net.piclock.button;

import java.io.IOException;

import org.apache.commons.exec.ExecuteException;

import net.piclock.enums.Buzzer;
import net.piclock.main.Constants;
import net.piclock.main.PiHandler;
import net.piclock.main.Preferences;
import net.piclock.swing.component.SwingContext;
import net.piclock.view.AlarmView;

public class Buttonhandler {

	private SwingContext ct = SwingContext.getInstance();
	//TODO test with pi4j
	public void listerToButton() throws InterruptedException, ExecuteException, IOException{
		
		PiHandler handler = PiHandler.getInstance();
		
		//check status of all
		if (AlarmView.alarmOn){
			AlarmView.alarmOn = false; // turn off alarm
			//get the buzzer type
			Preferences pref = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
//			Buzzer buzzer = Buzzer.valueOf(pref.getAlarmType());
//			handler.turnOffAlarm(buzzer);
			
		}else if (!handler.isScreenOn()){
			//turn screen on
			handler.turnOnScreen(true);
			handler.autoShutDownScreen();
		}
		
		
		
	}
}