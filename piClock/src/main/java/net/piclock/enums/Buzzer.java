package net.piclock.enums;

public enum Buzzer {
	BUZZER("Buzzer"), MP3("Mp3"), RADIO("Radio");
	
	private String name;
	
	private Buzzer(String nm){
		this.name = nm;
	}
	
	public String getName(){
		return name;
	}	
}