package net.piclock.thread;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Trigger {

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
			System.out.println("Reset date");
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
