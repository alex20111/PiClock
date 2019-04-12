package net.piclock.enums;

import java.time.DayOfWeek;

public enum AlarmRepeat {
	MONDAY(DayOfWeek.MONDAY), 
	TUESDAY(DayOfWeek.TUESDAY), 
	WEDNESDAY(DayOfWeek.WEDNESDAY), 
	THURSDAY(DayOfWeek.THURSDAY), 
	FRIDAY(DayOfWeek.FRIDAY),
	SATURDAY(DayOfWeek.SATURDAY),
	SUNDAY(DayOfWeek.SUNDAY),
	NONE(null);
	
	private DayOfWeek dayOfWeek;
	
	private AlarmRepeat(DayOfWeek day){
		dayOfWeek = day;
	}
	
	public DayOfWeek getDayOfWeek(){
		return dayOfWeek;		
	}
	
	public boolean isEqual(DayOfWeek dayOfWeek ){
		if (this.getDayOfWeek() != null && this.getDayOfWeek() == dayOfWeek){
			return true;
		}
		return false;
	}
}
