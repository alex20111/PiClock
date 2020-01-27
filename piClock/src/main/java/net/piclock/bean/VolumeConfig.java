package net.piclock.bean;

public class VolumeConfig {

	private int volumeLevel 	= -1;
	private int radioId 		= -1;
	private int mp3Id 			= -1;
	private boolean fromAlarm 	= false;
	private String msgPropertyName = "";
	
	public VolumeConfig(){}
	
	public VolumeConfig(int volLevel){
		this.volumeLevel = volLevel;
	}

	
	public int getVolumeLevel() {
		return volumeLevel;
	}
	public void setVolumeLevel(int volumeLevel) {
		this.volumeLevel = volumeLevel;
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
	public boolean isFromAlarm() {
		return fromAlarm;
	}
	public void setFromAlarm(boolean fromAlarm) {
		this.fromAlarm = fromAlarm;
	}
	
	public boolean hasId(){
		return radioId > 0 || mp3Id > 0;
	}

	public String getMsgPropertyName() {
		return msgPropertyName;
	}

	public void setMsgPropertyName(String msgPropertyName) {
		this.msgPropertyName = msgPropertyName;
	}
	
}