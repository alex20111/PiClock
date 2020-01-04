package net.piclock.thread;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Trigger {
	
	private static final Logger logger = Logger.getLogger( Trigger.class.getName() );

	private LocalDateTime dateTime;
	private boolean active = false;
	
	private List<Integer> alarmIds = new ArrayList<>();
	
	public Trigger(LocalDateTime t ){
		dateTime = t;
	}	
	
	public LocalDateTime getDateTime() {
		return dateTime;
	}
	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public List<Integer> getAlarmIds() {
		return alarmIds;
	}
	public void setAlarmIds(List<Integer> alarmIds) {
		this.alarmIds = alarmIds;
	}	
	public void updateTrigger(LocalDateTime date){
		if (dateTime != null && !dateTime.toLocalDate().isEqual(date.toLocalDate())){
			//reset
			dateTime = date;
			alarmIds.clear();
			active = false;
			logger.log(Level.CONFIG, "resetting trigger for alarms");
		}
	}	
	public boolean containsAlarmId(Integer alarmId){
		return alarmIds.contains(alarmId);
	}
	public void setAlarmTriggered(Integer alarmId){
		alarmIds.add(alarmId);
		this.active = true;
	}
}
