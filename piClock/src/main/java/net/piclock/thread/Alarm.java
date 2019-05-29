package net.piclock.thread;

import java.io.IOException;
import java.sql.SQLException;
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
import net.piclock.db.sql.AlarmSql;
import net.piclock.db.sql.RadioSql;
import net.piclock.enums.AlarmRepeat;
import net.piclock.enums.Buzzer;
import net.piclock.enums.Light;
import net.piclock.main.Constants;
import net.piclock.main.PiHandler;
import net.piclock.main.Preferences;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.MessageListener;
import net.piclock.swing.component.SwingContext;

public class Alarm implements Runnable, MessageListener{
	
	private static final Logger logger = Logger.getLogger( Alarm.class.getName() );

	private static AlarmEntity alarm;	
	private static boolean alarmTriggered = false;
	private SwingContext ct;
	private static PiHandler handler = PiHandler.getInstance();	
	
	private static boolean buzzerDefaultUsed = false; //if a problem occured , the default buzzer will be used 
	
	public Alarm(AlarmEntity alarmEnt){
		logger.log(Level.INFO, "Alarm class created for:  " + alarm);
		alarm = alarmEnt;
		ct = SwingContext.getInstance();
		
		ct.addMessageChangeListener(Constants.TURN_OFF_ALARM , this);
		
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
//		ct.removeMessageListener(Constants.TURN_OFF_ALARM, this);
		logger.log(Level.INFO, "Alarm class exited");
		
	}
	
	public static boolean isAlarmTriggered(){
		return alarmTriggered;
	}
	public void turnOffAlarmSound(){
		logger.log(Level.CONFIG, "Turning off alarm");
		alarmTriggered = false;
		try {
			Buzzer buzzer = Buzzer.valueOf(alarm.getAlarmSound());
			if (buzzerDefaultUsed) {
				buzzer = Buzzer.BUZZER;
				buzzerDefaultUsed = false;
				alarm.setAlarmSound(Buzzer.BUZZER.name());
				new AlarmSql().update(alarm);
			}
			handler.turnOffAlarm(buzzer);
		} catch (IOException | InterruptedException | SQLException | ClassNotFoundException e) {
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

			String track = "";
			
			if (buzzer == Buzzer.RADIO && alarm.getRadioId() > -1) { //TODO test
				RadioEntity rad = new RadioSql().loadRadioById(alarm.getRadioId());
				if (rad != null) {
					track = rad.getRadioLink();
				}else {
					buzzer = Buzzer.BUZZER;
					buzzerDefaultUsed = true;
				}
			}else if (buzzer == Buzzer.MP3 && alarm.getMp3Id() > -1) {
				//TODO load track
			}else {
				buzzer = Buzzer.BUZZER;  //load default value if errors in others.
			}
			
			Date end = new Date();
			long timeRemaining = 60000 - (end.getTime() - start.getTime());
			//pause for approx 1 min before triggering the alarm.
			try {
				Thread.sleep(timeRemaining);
				alarmTriggered = true;
				
				btnH.autoAlarmShutOff(true); //start alarm auto shutdown

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
//	@Override
//	public synchronized void propertyChange(PropertyChangeEvent evt) {
//		if (evt.getPropertyName().equals(Constants.TURN_OFF_ALARM)) {
//			turnOffAlarmSound();
//		}
//		
//	}
	@Override
	public synchronized void message(Message message) {
		logger.log(Level.CONFIG, "Recieved message: " + message.getPropertyName());
		if (Constants.TURN_OFF_ALARM.equals(message.getPropertyName())) {
			turnOffAlarmSound();
		}

	}
}