package net.piclock.db.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.piclock.enums.AlarmRepeat;

public class AlarmEntity {
	
	public static final String TBL_NM 		= "ALARM";
	public static final String ID 			= "id";
	public static final String TIME_HOUR	= "hour";
	public static final String TIME_MIN		= "minute";
	public static final String ALARM_SOUND	= "alarm_sound";
	public static final String ACTIVE 		= "active";
	public static final String REPEAT 		= "alarm_repeat";
	public static final String RADIO_ID 	= "alarm_radio_id";
	public static final String MP3_ID 		= "alarm_mp3_id";
	public static final String VOLUME		= "alarm_volume";
	public static final String ALARM_SHUTDOWN = "alarm_sh_timer"; //in minutes
	
	private int id 			= -1;
	private String hour 	= "";
	private String minutes 	= "";
	private String alarmSound   = "BUZZER";
	private boolean active 	= false;
	private int radioId 	= -1;
	private int mp3Id 		= -1;
	private int volume 		= -1;
	private int alarmShutdown = -1;
	private List<AlarmRepeat> alarmRepeat = new ArrayList<AlarmRepeat>();  // rang  -> all week , only week end, only mondays.
	
	public AlarmEntity(){}
	
	public AlarmEntity(ResultSet rs) throws SQLException{		
		this.id = rs.getInt(ID);
		this.hour = rs.getString(TIME_HOUR);
		this.minutes = rs.getString(TIME_MIN);
		this.alarmSound  = rs.getString(ALARM_SOUND);
		this.active = rs.getBoolean(ACTIVE);
		this.radioId = rs.getInt(RADIO_ID);
		this.mp3Id = rs.getInt(MP3_ID);
		
		String repeat = rs.getString(REPEAT);
		if (repeat != null && repeat.length() > 0){
			
			for(String s: repeat.split(",")){
				this.alarmRepeat.add(AlarmRepeat.valueOf(s));
			}
		}
		
		this.volume = rs.getInt(VOLUME);
		this.alarmShutdown = rs.getInt(ALARM_SHUTDOWN);
	}
	
	public static String checkIfTableExist() { 
		return "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME ='"+TBL_NM+"'"; 
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public String getRepeatString(){ //to save on DB
		String commaDelString = "";
		boolean first = true;
		for(AlarmRepeat s : alarmRepeat){
			if (first){
				commaDelString += s.name();
				first= false;
			}else{
				commaDelString +=  "," + s.name() ;
			}
		}
		return commaDelString;
	}

	public String getAlarmSound() {
		return alarmSound;
	}
	public void setAlarmSound(String alarmSound) {
		this.alarmSound = alarmSound;
	}

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getMinutes() {
		return minutes;
	}

	public void setMinutes(String minutes) {
		this.minutes = minutes;
	}

	public List<AlarmRepeat> getAlarmRepeat() {
		return alarmRepeat;
	}

	public void setAlarmRepeat(List<AlarmRepeat> alarmRepeat) {
		this.alarmRepeat = alarmRepeat;
	}

	public int getRadioId() {
		return radioId;
	}

	public void setRadioId(int radioId) {
		this.radioId = radioId;
	}

	public int getMp3Id() {
		return mp3Id;
	}

	public void setMp3Id(int mp3Id) {
		this.mp3Id = mp3Id;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public int getAlarmShutdown() {
		return alarmShutdown;
	}

	public void setAlarmShutdown(int alarmShutdown) {
		this.alarmShutdown = alarmShutdown;
	}

	@Override
	public String toString() {
		return "AlarmEntity [id=" + id + ", hour=" + hour + ", minutes=" + minutes + ", alarmSound=" + alarmSound
				+ ", active=" + active + ", radioId=" + radioId + ", mp3Id=" + mp3Id + ", volume=" + volume
				+ ", alarmShutdown=" + alarmShutdown + ", alarmRepeat=" + alarmRepeat + "]";
	}



	
}