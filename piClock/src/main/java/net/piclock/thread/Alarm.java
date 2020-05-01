package net.piclock.thread;

import java.io.IOException;
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
import net.piclock.db.entity.Mp3Entity;
import net.piclock.db.entity.RadioEntity;
import net.piclock.db.sql.Mp3Sql;
import net.piclock.db.sql.RadioSql;
import net.piclock.enums.AlarmRepeat;
import net.piclock.enums.Buzzer;
import net.piclock.enums.ScreenType;
import net.piclock.handlers.PiHandler;
import net.piclock.main.Constants;
import net.piclock.main.HardwareConfig;
import net.piclock.main.Preferences;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.MessageListener;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.FormatStackTrace;

public class Alarm implements Runnable, MessageListener{

	private static final Logger logger = Logger.getLogger( Alarm.class.getName() );

	private AlarmEntity alarm;	
	private static boolean alarmTriggered = false;
	private SwingContext ct;
	private static PiHandler handler = PiHandler.getInstance();	

	private static boolean buzzerDefaultUsed = false; //if a problem occured , the default buzzer will be used 

	private  Thread alarmAutoOff; 

	public Alarm(AlarmEntity alarmEnt){
		
		alarm = alarmEnt;
		ct = SwingContext.getInstance();
		buzzerDefaultUsed = false;
		logger.log(Level.INFO, "Alarm class created for:  " + alarm);
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

		//remove active alarm
		ct.sendMessage(Constants.REMOVE_TRIGGER, new Message("remove"));
		
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
			}else {
				handler.turnOffAlarm(buzzer);
			}
			autoAlarmShutOff(false, 0);//turn off autoshutdown when alarm is shut down

			ct.removeMessageListener(Constants.TURN_OFF_ALARM, this);
			ct.removeMessageListener(Constants.RADIO_STREAM_ERROR, this);
			ct.removeMessageListener(Constants.MP3_STREAM_ERROR, this);
			resetVar();

		} catch (IOException | InterruptedException e) {
			ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
			eh.addError(ErrorType.ALARM, new ErrorInfo(new FormatStackTrace(e).getFormattedException()));
			logger.log(Level.SEVERE, "error turning off the alarm", e);
		}
	}	
	private void triggerAlarm(){	
		Date start = new Date();

		ct.addMessageChangeListener(Constants.TURN_OFF_ALARM , this);
		ct.addMessageChangeListener(Constants.RADIO_STREAM_ERROR , this);
		ct.addMessageChangeListener(Constants.MP3_STREAM_ERROR , this);

		try {
			Preferences pref = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
			HardwareConfig hw = (HardwareConfig)SwingContext.getInstance().getSharedObject(Constants.HARDWARE);
			
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

						Thread.sleep(200);

						count ++;
					}
				}else{
					int triggerForecast = new Random().nextInt(999999);
					ct.putSharedObject(Constants.FETCH_FORECAST, triggerForecast);
				}
			}

			String mp3Filename = "";
			int radioChannel = -1;

			if (buzzer == Buzzer.RADIO && alarm.getRadioId() > -1 ) {
				if (handler.isWifiInternetConnected()) {
					RadioEntity rad = new RadioSql().loadRadioById(alarm.getRadioId());
					if (rad != null) {
						radioChannel = rad.radioNameToChannel();
					}else {
						buzzer = Buzzer.BUZZER;
						buzzerDefaultUsed = true;
					}
				}else {
					buzzer = Buzzer.BUZZER;
					buzzerDefaultUsed = true;
				}
			}else if (buzzer == Buzzer.MP3 && alarm.getMp3Id() > -1) {
				Mp3Entity mp3 = new Mp3Sql().loadMp3ById(alarm.getMp3Id());
				if (mp3 != null) {
					mp3Filename = mp3.getMp3FileName();
				}else {
					buzzer = Buzzer.BUZZER;
					buzzerDefaultUsed = true;
				}
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
					ScreenType type = hw.getScreenType();
					handler.turnOnScreen(false, type.getLowestBrightness());
					handler.autoShutDownScreen(45000);
				}	
				
				
				if (alarm.getVolume() == -1) {
					alarm.setVolume(15);
				}

				handler.turnOnAlarm(buzzer, mp3Filename, radioChannel, alarm.getVolume());

			}catch (InterruptedException ie) {					
				logger.log(Level.CONFIG, "Current ALARM thread interrupted");
				turnOffAlarmSound();
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
			logger.log(Level.CONFIG, "Message: " + message.getFirstMessage());
			turnOffAlarmSound();
		}else if (Constants.RADIO_STREAM_ERROR.equals(message.getPropertyName()) && alarmTriggered) {
			logger.log(Level.INFO, "Radio stream error in wake up alarm, using default. Message: " + message.getFirstMessage() + "  Date registered: " + message.getDateTime());
			buzzerDefaultUsed = true;
			try {
				handler.turnOnAlarm(Buzzer.BUZZER, "", -1, -1);
				handler.playRadio(false, "", -1);//force turn off radio
			} catch (Exception e) {
				ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
				eh.addError(ErrorType.ALARM, new ErrorInfo(new FormatStackTrace(e).getFormattedException()));
				logger.log(Level.SEVERE, "Error in Radio stream error message", e);
			}
		}else if (Constants.MP3_STREAM_ERROR.equals(message.getPropertyName()) && alarmTriggered) {
			logger.log(Level.INFO, "MP3 stream error in wake up alarm, using default. Message: " + message.getFirstMessage() + "  Date registered: " + message.getDateTime());
			buzzerDefaultUsed = true;
			try {
				handler.turnOnAlarm(Buzzer.BUZZER, "", -1, -1);
				handler.playMp3(false, "", -1);//force turn off radio
			} catch (Exception e) {
				ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
				eh.addError(ErrorType.ALARM, new ErrorInfo(new FormatStackTrace(e).getFormattedException()));
				logger.log(Level.SEVERE, "Error in MP3 stream error message", e);
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