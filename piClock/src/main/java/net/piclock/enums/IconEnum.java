package net.piclock.enums;

public enum IconEnum {

	ALARM_ICON("alarm"),RADIO_ICON("radio"),MP3_ICON("mp3"),TEMP_SHADE("temp_shade"),
	TEMP_SUN("temp_sun"), WIFI_OFF_ICON("wifi"),WIFI_ON_ICON("wifi"),WIFI_ON_NO_INET("wifi"),
	BUTTON_COG("cog"),WEB_SERVER("web"), VOLUME_ICON("vol"), VOLUME_MUTED("vol"), VOLUME_ICON_RADIO("volradio"), VOLUME_MUTED_RADIO("volradio");
	
	
	private String iconKey;
	
	private IconEnum(String key){
		iconKey = key;
	}
	
	public String getIconKey(){
		return iconKey;
	}
	
	
}