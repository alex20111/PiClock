package net.piclock.weather;

import java.util.Date;

import com.google.gson.annotations.Expose;

public class Temperature {

	public static String TBL_NAME = "temperature";
	public static String ID 		= "id";
	public static String TEMP 		= "temp_c";
	public static String REC_DATE 	= "recorded_date";
	public static String REC_NAME 	= "recorder_name";
	public static String BATT_LVL 	= "battery_level";
	public static String HUMIDITY 	= "humidity";	
	
	private int 	id 				= 0;
	@Expose
	private Float 	tempC 			= null;
	@Expose
	private Date	recordedDate	= null;
	@Expose
	private String  recorderName	= "";
	@Expose
	private String  batteryLevel	= null;	
	@Expose
	private String  humidity		= "";	
	
	public Temperature(){}
	
		
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Float getTempC() {
		return tempC;
	}
	public void setTempC(Float tempC) {
		this.tempC = tempC;
	}
	public Date getRecordedDate() {
		return recordedDate;
	}
	public void setRecordedDate(Date recordedDate) {
		this.recordedDate = recordedDate;
	}
	public String getRecorderName() {
		return recorderName;
	}
	public void setRecorderName(String recorderName) {
		this.recorderName = recorderName;
	}
	public String getBatteryLevel() {
		return batteryLevel;
	}
	public void setBatteryLevel(String batteryLevel) {
		this.batteryLevel = batteryLevel;
	}
	
	public String getHumidity() {
		return humidity;
	}

	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}

	

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Temperature [id=");
		builder.append(id);
		builder.append(", tempC=");
		builder.append(tempC);
		builder.append(", Humidity=");
		builder.append(humidity);
		builder.append(", recordedDate=");
		builder.append(recordedDate);
		builder.append(", recorderName=");
		builder.append(recorderName);
		builder.append(", batteryLevel=");
		builder.append(batteryLevel);
		builder.append("]");
		return builder.toString();
	}

}
