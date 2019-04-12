package net.piclock.thread;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import net.piclock.db.entity.AlarmEntity;
import net.piclock.enums.AlarmRepeat;

public class Alarm implements Runnable{

	private AlarmEntity alarm;	
	private static boolean alarmTriggered = false;
	
	public Alarm(AlarmEntity alarm){
		this.alarm = alarm;
	}
	@Override
	public void run() {
		
		System.out.println("Alerm Triggered");
		List<AlarmRepeat> repeats = alarm.getAlarmRepeat();
		
		DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
		
		for(AlarmRepeat r : repeats){
			if (r.isEqual(dayOfWeek)){
				System.out.println("same day");
				triggerAlarm(1);
				break;
			}
		}		
	}
	
	public static boolean isAlarmTriggered(){
		return alarmTriggered;
	}
	public static void turnOffAlarmSound(){
		alarmTriggered = false;
		//TODO add PiHandler turn off alarm
	}	
	private void triggerAlarm(int a){
		System.out.println("Alarm triggered: "  + a);
		alarmTriggered = true;
	}
}