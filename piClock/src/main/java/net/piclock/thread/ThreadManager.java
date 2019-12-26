package net.piclock.thread;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;

import net.piclock.main.Constants;
import net.piclock.main.Preferences;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.SwingContext;

 
//add code to main code.. 
public class ThreadManager {

	private static final Logger logger = Logger.getLogger( ThreadManager.class.getName() );
	private ScheduledExecutorService scheduler;

	private ScheduledFuture<?> alarmThread ;
	private ScheduledFuture<WeatherWorker> weatherThread;
	private ScheduledFuture<TempSensorWorker> sensorThread;
	
	private static ThreadManager threadManager;
	
	public int wthErrorCount = 0;
	
	private ThreadManager(){		
		scheduler = Executors.newScheduledThreadPool(10);
		
	}
	public static ThreadManager getInstance(){
		if (threadManager == null){
			synchronized (ThreadManager.class) {
				if (threadManager == null){
					threadManager = new ThreadManager();
				}
			}
		}
		return threadManager;
	}

	public void startAlarm(){
		//calculate initial delay
		
//		LocalDateTime currentDate = LocalDateTime.now();
//		LocalDateTime alarmTime = LocalDateTime.now();
//		
//		int hours = Integer.parseInt(alarm.getHour());
//		int minutes = Integer.parseInt(alarm.getMinutes());
//		
//		if ( hours > alarmTime.getHour()  ||
//				hours == alarmTime.getHour() && minutes > alarmTime.getMinute()){
//			//time after current time
//			alarmTime = LocalDate.now().atTime(hours, minutes, 0, 0);
//		}else{
//			//time before current time, set date for next day.
//			alarmTime = LocalDate.now().atTime(hours, minutes, 0, 0).plusDays(1);
//		}
//
//		//remove 1 min to allow system to perform something before triggering the alarm		
//		long initDelay = ChronoUnit.MILLIS.between(currentDate, alarmTime) - 60000;
//		
//		logger.log(Level.CONFIG, "Alarm Delay starting: " + initDelay + " " + new Date(new Date().getTime() + initDelay));
		AlarmMonitorThread amt = new AlarmMonitorThread();
		LocalDateTime now = LocalDateTime.now();
		int millis = now.get(ChronoField.MILLI_OF_SECOND);
		
		long delay = (1000-millis);
		
		logger.log(Level.CONFIG, "startAlarm. Delay: " + delay);
		
		alarmThread = scheduler.scheduleAtFixedRate(amt, delay, 1000, TimeUnit.MILLISECONDS);
	}
	public void stopAlarm(){
		logger.log(Level.CONFIG, "stopAlarm");
		if (alarmThread != null && !alarmThread.isDone()){

			logger.log(Level.CONFIG,"startStopTimer::alarmThread Done? " + alarmThread.isDone());			

			if (Alarm.isAlarmTriggered()){
				SwingContext.getInstance().sendMessage(Constants.TURN_OFF_ALARM, new Message("Thread Manager turning off alarm"));
			}
			alarmThread.cancel(true);	
			//wait for timer to stop
			while(!alarmThread.isDone()){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {}
			}		

			logger.log(Level.CONFIG, "alarmThread end loop Done? " + alarmThread.isDone());
		}	
	}
	@SuppressWarnings("unchecked")
	public void startWeatherThread(int initDelay, Preferences pref) {
		logger.config("startWeatherThread. Pool size: " );
		
//		//verify if too much errors from the weather worker
//		ErrorHandler eh =(ErrorHandler) SwingContext.getInstance().getSharedObject(Constants.ERROR_HANDLER);
//		
//		boolean start = true;  //TODO
//		
//		if (eh.getErrorMap().get(net.piclock.bean.ErrorType.WEATHER) != null && eh.getErrorMap().get(net.piclock.bean.ErrorType.WEATHER).getErrorCount() > 30 ) {
//			start = false;
//			logger.log(Level.WARNING, "Too much error for the weather action, do not restart weather thread");
//		}
		
//		if (start) {
		weatherThread = (ScheduledFuture<WeatherWorker>) scheduler.scheduleAtFixedRate(new WeatherWorker(), initDelay, pref.getWeatherRefresh(), TimeUnit.MINUTES);
//		}
		
	}
	public void stopWeatherThread() {
		logger.log(Level.CONFIG, "Stop Weather thread");
		if (weatherThread != null && !weatherThread.isDone()){

			weatherThread.cancel(true);	
			//wait for timer to stop
			while(!weatherThread.isDone()){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {}
			}		

		}	
		logger.log(Level.CONFIG, "Weather thread stopped. ");
	}
	public void startLdr() {
		logger.log(Level.CONFIG, "startLdr");
		scheduler.scheduleWithFixedDelay(new LDRStatusWorker(), 1, 5, TimeUnit.SECONDS);
	}
	
	public void startClock (JLabel clockLabel, JLabel weekDateLable, long delay) {
		logger.log(Level.CONFIG, "startClock");
		scheduler.scheduleAtFixedRate(new Clock(clockLabel, weekDateLable), delay, 2000, TimeUnit.MILLISECONDS);
	}
	@SuppressWarnings("unchecked")
	public void startSensorThread() {
		logger.log(Level.CONFIG, "startSensorThread");
		sensorThread = (ScheduledFuture<TempSensorWorker>) scheduler.scheduleAtFixedRate(new TempSensorWorker(), 0, 5, TimeUnit.MINUTES);
	}
	public void stopSensorThread() {
		logger.config("stopSensorThread");
		if (sensorThread != null){
			
			if (!sensorThread.isDone()){
			
				sensorThread.cancel(true); //cancel worker if running
				
				logger.config("sensorThread Thread done: " +  sensorThread.isDone());
				//wait until cancelled
				while(!sensorThread.isDone()){
					try {
						Thread.sleep(40);
					} catch (InterruptedException e1) {}
				}
			}
		}	
		logger.config("sensorThread Thread finished");
		
	}
}