package net.piclock.thread;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;

import net.piclock.db.entity.AlarmEntity;

 
//add code to main code.. 
public class ThreadManager {

	private static final Logger logger = Logger.getLogger( ThreadManager.class.getName() );
	private ScheduledExecutorService scheduler;

	private ScheduledFuture<?> alarmThread ;
	
	private static ThreadManager threadManager;
	private ThreadManager(){
		scheduler = Executors.newScheduledThreadPool(4);
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

	public void startAlarm(AlarmEntity alarm){
		//calculate initial delay
		logger.log(Level.CONFIG, "startAlarm: " + alarm);
		LocalDateTime currentDate = LocalDateTime.now();
		LocalDateTime alarmTime = LocalDateTime.now();
		
		int hours = Integer.parseInt(alarm.getHour());
		int minutes = Integer.parseInt(alarm.getMinutes());
		
		if ( hours > alarmTime.getHour()  ||
				hours == alarmTime.getHour() && minutes > alarmTime.getMinute()){
			//time after current time
			alarmTime = LocalDate.now().atTime(hours, minutes, 0, 0);
		}else{
			//time before current time, set date for next day.
			alarmTime = LocalDate.now().atTime(hours, minutes, 0, 0).plusDays(1);
		}

		//remove 1 min to allow system to perform something before triggering the alarm		
		long initDelay = ChronoUnit.MILLIS.between(currentDate, alarmTime) - 60000;
		
		System.out.println("Delay starting: " + initDelay + " " + new Date(new Date().getTime() + initDelay));
		
		scheduler.scheduleAtFixedRate(new Alarm(alarm), initDelay, 86400000, TimeUnit.MILLISECONDS);
	}
	public void stopAlarm(){
		logger.log(Level.CONFIG, "stopAlarm");
		if (alarmThread != null && !alarmThread.isDone()){

			System.out.println("startStopTimer::alarmThread Done? " + alarmThread.isDone());			

			if (Alarm.isAlarmTriggered()){
				//TODO , shut down alarm sound if running
			}
			alarmThread.cancel(true);	
			//wait for timer to stop
			while(!alarmThread.isDone()){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {}
			}		

			System.out.println("alarmThread end loop Done? " + alarmThread.isDone());
		}	
	}
	public void startLdr() {
		 
		scheduler.scheduleWithFixedDelay(new LDRStatusWorker(), 1, 5, TimeUnit.SECONDS);
	}
	
	public void startClock (JLabel clockLabel, JLabel weekDateLable, long delay) {
		scheduler.scheduleAtFixedRate(new Clock(clockLabel, weekDateLable), delay, 60000, TimeUnit.MILLISECONDS);
	}
}