package net.piclock.thread;

import java.io.IOException;
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
import net.piclock.db.entity.RadioEntity;
import net.piclock.db.sql.RadioSql;
import net.piclock.enums.AlarmRepeat;
import net.piclock.enums.Buzzer;
import net.piclock.enums.Light;
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
	
	private static boolean buzzerDefaultUsed = false; //if a problem occured , the default buzzer will be used 
	
	public Alarm(AlarmEntity alarmEnt){
		alarm = alarmEnt;
		ct = SwingContext.getInstance();
		
	}
	@Override
	public void run() {
		logger.log(Level.INFO, "Alarm triggered for: " + alarm);
		
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
		try {
			Buzzer buzzer = Buzzer.valueOf(alarm.getAlarmSound());
			if (buzzerDefaultUsed) {
				buzzer = Buzzer.BUZZER;
				buzzerDefaultUsed = false;
			}
			handler.turnOffAlarm(buzzer);
		} catch (IOException | InterruptedException e) {
			logger.log(Level.SEVERE, "error turning off the alarm", e);
		}
	}	
	private void triggerAlarm(){	
		Date start = new Date();

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
			AlarmBtnHandler btnH = (AlarmBtnHandler)ct.getSharedObject(Constants.ALARM_BTN_HANDLER);
			btnH.setListenerActive();
			ArduinoCmd cm = ArduinoCmd.getInstance();
			cm.startBtnMonitoring();			

			Date end = new Date();
			long timeRemaining = 60000 - (end.getTime() - start.getTime());

			int track = -1;
			
			if (buzzer == Buzzer.RADIO && alarm.getRadioId() > -1) { //TODO test
				RadioEntity rad = new RadioSql().loadRadioById(alarm.getRadioId());
				if (rad != null) {
					track = rad.getTrackNbr();
				}else {
					buzzer = Buzzer.BUZZER;
					buzzerDefaultUsed = true;
				}
			}else if (buzzer == Buzzer.MP3 && alarm.getMp3Id() > -1) {
				//TODO load track
			}else {
				buzzer = Buzzer.BUZZER;  //load default value if errors in others.
			}
			
			
			//pause for 1 min before triggering the alarm.
			try {
				Thread.sleep(timeRemaining);// sleep 1 min then turn buzzer on.
				alarmTriggered = true;
				
				btnH.autoAlarmShutOff(true);

				if (!handler.isScreenOn()){						
					handler.turnOnScreen(false, Light.VERY_BRIGHT);
					handler.autoShutDownScreen();
				}				
			
				handler.turnOnAlarm(buzzer, track);

			}catch (InterruptedException ie) {					
				logger.log(Level.CONFIG, "Current thread interrupted");
				btnH.deactivateListener();
				cm.stopBtnMonitor();
				Thread.currentThread().interrupt();
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error in setting off timer" , e); 	

		}				
	}
}