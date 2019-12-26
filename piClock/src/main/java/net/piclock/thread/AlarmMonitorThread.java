package net.piclock.thread;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.db.entity.AlarmEntity;
import net.piclock.enums.AlarmRepeat;
import net.piclock.main.Constants;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.MessageListener;
import net.piclock.swing.component.SwingContext;

public class AlarmMonitorThread implements Runnable, MessageListener{
	private static final Logger logger = Logger.getLogger( AlarmMonitorThread.class.getName() );

	private boolean updateList = false;
	private List<AlarmEntity> alarms = Collections.synchronizedList(new ArrayList<AlarmEntity>());

	private Trigger alarmTrig = null;	
	private Thread currAlarmThread = null;	

	public AlarmMonitorThread(){
		logger.info("--> Starting alarm monitor thread");
		//register event
		SwingContext.getInstance().addMessageChangeListener( Constants.UPDATE_ALARMS, this);
		SwingContext.getInstance().addMessageChangeListener( Constants.REMOVE_TRIGGER, this);
		alarmTrig = new Trigger(LocalDateTime.now());
	}

	@Override
	public void run() {
		try{
			if (!updateList){

				//we need to check if the date and time is 1 minute before the alarm sounds off and if it should sound off.
				LocalDateTime 	date = LocalDateTime.now().plusMinutes(1);					
//				date = LocalDateTime.now();

				DayOfWeek dayOfWeek = date.getDayOfWeek();

//				logger.config("Running: " + date + " alarmThreadAlive? " + (currAlarmThread != null && currAlarmThread.isAlive() ? "YES" : "NO"));

				alarmTrig.updateTrigger(date);

				synchronized (alarms) {  

					Iterator<AlarmEntity> iter = alarms.iterator();
					while(iter.hasNext() ){

						AlarmEntity a = iter.next();

						if (date.getHour() == Integer.parseInt(a.getHour()) && 
								date.getMinute() == Integer.parseInt(a.getMinutes()) &&
								!isAlarmTriggered( a, date ) && a.isActive() ){

							for(AlarmRepeat ar : a.getAlarmRepeat()){

								if (ar.isEqual(dayOfWeek)){

//									System.out.println("generate: " + a.getId());
									logger.info("Alarm triggered: " + a);
									alarmTrig.setAlarmTriggered(a.getId());

									Alarm a1 = new Alarm(a);					
									currAlarmThread = new Thread(a1);
									currAlarmThread.start();
								}
							}
						}
					}
				}
			}
		}catch(Throwable tr){
			logger.log(Level.SEVERE, "error in alarm monitor" , tr);
		}
	}

	@Override
	public synchronized void message(Message message) {
		updateList = true;

		try{
			if (message.getPropertyName().equals(Constants.UPDATE_ALARMS)){				
//				alarms.clear();
				AlarmEntity a = (AlarmEntity)message.getFirstMessage();
//				List<Object> h = (List<Object>)message.getMessageList().get(0);
				logger.info("Updating alarm list: " + a);
				
				int idx = -1;
				for(int i = 0 ; i < alarms.size() ; i++) {
					AlarmEntity ae = alarms.get(i);
					if (ae.getId() == a.getId()) {
						idx = i;
						break;
					}
				}
				if (idx > -1) {
					alarms.remove(idx);
					alarms.add(a);
				}else {
					alarms.add(a);
				}
				
//				for(Object o : h){
//					alarms.add((AlarmEntity)o);
//				}		
				logger.info("---> New Alarm List: " + alarms);
				alarmTrig = new Trigger(LocalDateTime.now());//clear the triggers
			}
			else if (message.getPropertyName().equals(Constants.REMOVE_TRIGGER)){
				System.out.println("Mesage remove trigger");
				alarmTrig.setActive(false);

				if (currAlarmThread != null && currAlarmThread.isAlive()){
					currAlarmThread.interrupt();
				}
			}
		}catch(Throwable tr){
			logger.log(Level.SEVERE, "Error in message",tr );
		}
		updateList = false;
	}
	private boolean isAlarmTriggered(AlarmEntity a, LocalDateTime date ){

		if (alarmTrig.containsAlarmId(a.getId())){
			return true;
		}else if(alarmTrig.isActive()){
			return true;
		}
		return false;
	}
}
