package net.piclock.thread;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.arduino.ArduinoCmd;
import net.piclock.button.AlarmBtnHandler;
import net.piclock.db.entity.AlarmEntity;
import net.piclock.enums.AlarmRepeat;
import net.piclock.enums.Buzzer;
import net.piclock.main.Constants;
import net.piclock.main.PiHandler;
import net.piclock.main.Preferences;
import net.piclock.swing.component.SwingContext;

public class Alarm implements Runnable{
	
	private static final Logger logger = Logger.getLogger( Alarm.class.getName() );

	private static AlarmEntity alarm;	
	private static boolean alarmTriggered = false;
	private SwingContext ct;
	private static PiHandler handler = PiHandler.getInstance();	
	
	public Alarm(AlarmEntity alarmEnt){
		alarm = alarmEnt;
		ct = SwingContext.getInstance();
		
	}
	@Override
	public void run() {
		
		System.out.println("Alerm Triggered");
		List<AlarmRepeat> repeats = alarm.getAlarmRepeat();
		
		DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
		
		for(AlarmRepeat r : repeats){
			if (r.isEqual(dayOfWeek)){
				triggerAlarm();
				break;
			}
		}		
	}
	
	public static boolean isAlarmTriggered(){
		return alarmTriggered;
	}
	public static void turnOffAlarmSound(){
		logger.log(Level.CONFIG, "Turning off alarm");
		alarmTriggered = false;
		handler.turnOffAlarm(Buzzer.valueOf(alarm.getAlarmSound()));
	}	
	private void triggerAlarm(){
		
		Date start = new Date();
		alarmTriggered = true;
		
		try {
			Preferences pref = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
			Buzzer buzzer = Buzzer.valueOf(alarm.getAlarmSound());			
			
			if (pref.isWeatherActivated() ){
				
				if (!handler.isWifiConnected() && pref.isWifiCredentialProvided()){
					handler.turnWifiOn();
				}else{
					int triggerForecast = new Random().nextInt(999999);
					ct.putSharedObject(Constants.FETCH_FORECAST, triggerForecast);
				}
			}
			
			//start button
			ArduinoCmd cm = ArduinoCmd.getInstance();
			cm.startBtnMonitoring();
			
			
			Date end = new Date();
			long timeRemaining = 60000 - (end.getTime() - start.getTime());
			
			
			//pause for 1 min before triggering the alarm.
			System.out.println("Time rem: " + timeRemaining);
			Thread.sleep(timeRemaining);// sleep 1 min then turn buzzer on.
			if (!handler.isScreenOn()){						
				handler.turnOnScreen(false);
				handler.autoShutDownScreen();
			}
			handler.turnOnAlarm(buzzer);
//			cm.stopBtnMonitor();
//			cm.clearButtonListeners();
			
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error in setting off timer" , e);
			//TODO set someting letting the user know that there is been a log generated!!! 						
		}				
	}
}