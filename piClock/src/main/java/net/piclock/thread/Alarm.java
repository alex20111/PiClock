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

import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
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
import net.piclock.util.FormatStackTrace;

public class Alarm implements Runnable, MessageListener{
	
	private static final Logger logger = Logger.getLogger( Alarm.class.getName() );

	private static AlarmEntity alarm;	
	private static boolean alarmTriggered = false;
	private SwingContext ct;
	private static PiHandler handler = PiHandler.getInstance();	
	
	private static boolean buzzerDefaultUsed = false; //if a problem occured , the default buzzer will be used 
	
	private  Thread alarmAutoOff; 
	
	public Alarm(AlarmEntity alarmEnt){
		logger.log(Level.INFO, "Alarm class created for:  " + alarm);
		alarm = alarmEnt;
		ct = SwingContext.getInstance();
		buzzerDefaultUsed = false;
	
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

		logger.log(Level.INFO, "Alarm class exited");
		
	}
	
	public static boolean isAlarmTriggered(){
		return alarmTriggered;
	}
	public void turnOffAlarmSound(){
		logger.log(Level.CONFIG, "Turning off alarm");
		
		try {

			Buzzer buzzer = Buzzer.valueOf(alarm.getAlarmSound());
			if (buzzerDefaultUsed) {
				buzzer = Buzzer.BUZZER;
				handler.turnOffAlarm(buzzer);				
				alarm.setAlarmSound(Buzzer.BUZZER.name());
				new AlarmSql().update(alarm);
			}else {
				handler.turnOffAlarm(buzzer);
			}
			autoAlarmShutOff(false, 0);//turn off autoshutdown when alarm is shut down
			
			ct.removeMessageListener(Constants.TURN_OFF_ALARM, this);
			ct.removeMessageListener(Constants.RADIO_STREAM_ERROR, this);			
			resetVar();
			
		} catch (IOException | InterruptedException | SQLException | ClassNotFoundException e) {
			ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
				eh.addError(ErrorType.ALARM, new ErrorInfo(new FormatStackTrace(e).getFormattedException()));
			logger.log(Level.SEVERE, "error turning off the alarm", e);
		}
	}	
	private void triggerAlarm(){	
		Date start = new Date();
		
		ct.addMessageChangeListener(Constants.TURN_OFF_ALARM , this);
		ct.addMessageChangeListener(Constants.RADIO_STREAM_ERROR , this);

//		AlarmBtnHandler btnH = null;
//		ArduinoSerialCmd cm = null;
		
		try {
			Preferences pref = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
			Buzzer buzzer = Buzzer.valueOf(alarm.getAlarmSound());			

			if (pref.isWeatherActivated() ){

				if (!handler.isWifiConnected() && pref.isWifiCredentialProvided()){
					handler.turnWifiOn();
					boolean waiting = true;
					int count = 0;
					//wait unti wifi connect if no wifi connected
					while(waiting) {

						if (handler.isWifiConnected()) {
							logger.log(Level.CONFIG, "Wifi aquired in Alarm");
							break;
						}else if(count > 100) {
							waiting = false;
							logger.log(Level.INFO, "Could not connect to the internet in ALARM");
							break;
						}
						try {
							Thread.sleep(200);
						}catch(InterruptedException i) {
							logger.log(Level.INFO, "wifi aquisition in alarm interrupted");
							break;}
						count ++;
					}
					
					
				}else{
					int triggerForecast = new Random().nextInt(999999);
					ct.putSharedObject(Constants.FETCH_FORECAST, triggerForecast);
				}
			}
			
			//start button
//			btnH = (AlarmBtnHandler)ct.getSharedObject(Constants.ALARM_BTN_HANDLER);
//			btnH.setListenerActive();
//			cm = ArduinoCmd.getInstance();
//			cm.startBtnMonitoring();			

			String track = "";
			
			if (buzzer == Buzzer.RADIO && alarm.getRadioId() > -1 ) {
				if (handler.isWifiInternetConnected()) {
					RadioEntity rad = new RadioSql().loadRadioById(alarm.getRadioId());
					if (rad != null) {
						track = rad.getRadioLink();
					}else {
						buzzer = Buzzer.BUZZER;
						buzzerDefaultUsed = true;
					}
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
				
				autoAlarmShutOff(true, alarm.getAlarmShutdown()); //start alarm auto shutdown

				if (!handler.isScreenOn()){						
					handler.turnOnScreen(false, Light.LIGHT);
					handler.autoShutDownScreen();
				}				
			
				handler.turnOnAlarm(buzzer, track);

			}catch (InterruptedException ie) {					
				logger.log(Level.CONFIG, "Current thread interrupted");
				Thread.currentThread().interrupt();
				resetVar();
			}
		} catch (Exception e) {
			ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
			eh.addError(ErrorType.ALARM, new ErrorInfo(new FormatStackTrace(e).getFormattedException()));
			logger.log(Level.SEVERE,"Error in setting off timer" , e);
			resetVar();
		}				
	}

	@Override
	public synchronized void message(Message message) {
		logger.log(Level.CONFIG, "Recieved message: " + message.getPropertyName());
		if (Constants.TURN_OFF_ALARM.equals(message.getPropertyName())) {
			logger.log(Level.CONFIG, "Message: " + message.getMessage());
			turnOffAlarmSound();
		}else if (Constants.RADIO_STREAM_ERROR.equals(message.getPropertyName()) && alarmTriggered) {
			logger.log(Level.INFO, "Radio stream error in wake up alarm, using default. Message: " + message.getMessage() + "  Date registered: " + message.getDateTime());
			buzzerDefaultUsed = true;
			try {
				handler.turnOnAlarm(Buzzer.BUZZER, "");
				handler.playRadio(false, "");//force turn off radio
			} catch (Exception e) {
				ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
				eh.addError(ErrorType.ALARM, new ErrorInfo(new FormatStackTrace(e).getFormattedException()));
				logger.log(Level.SEVERE, "Error in Radio stream error message", e);
			}
		}
	}
	/**
	 * reset needed variables for the alarm
	 */
	private void resetVar() {
		buzzerDefaultUsed = false;
		alarmTriggered = false;
	}
	
	private  void autoAlarmShutOff( boolean startThread, int shutdownTime) {
		logger.log(Level.CONFIG, "autoAlarmShutOff. Start: " + startThread );

		if (alarmAutoOff != null && alarmAutoOff.isAlive()) {
			alarmAutoOff.interrupt();
			logger.log(Level.CONFIG, "autoAlarmShutOff not null and interrupted");
		}

		if (startThread) {
			alarmAutoOff = new Thread(new Runnable() {

				@Override
				public void run() {
					logger.log(Level.CONFIG, "autoAlarmShutOff: Auto Off start in run. Shutdown time: " + shutdownTime);
					try {
						Thread.sleep(shutdownTime * 60000);
						logger.log(Level.INFO, "autoAlarmShutOff: Turning off alarm automatically.");
						Message msg = new Message("off -From autoshutdown");
						ct.sendMessage(Constants.TURN_OFF_ALARM, msg);; //always use the message to turn off the alarm so all registered parties will know about it.
					}catch(InterruptedException i) {
						Thread.currentThread().interrupt();
					}
					logger.log(Level.CONFIG, "autoAlarmShutOff: end run method");
				}		
			});

			alarmAutoOff.start();
		}
	}

}