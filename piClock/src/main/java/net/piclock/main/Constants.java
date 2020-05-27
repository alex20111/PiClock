package net.piclock.main;

import java.text.DecimalFormat;

public class Constants {
	
	public static final String SCREEN_ENV = "Screen_environment";

	public static final String MAIN_VIEW 			= "main";
	public static final String WEATHER_CONFIG_VIEW = "weatherConfig";
	public static final String ALARM_VIEW 			= "alarm";
	public static final String WEATHER_FORECAST_VIEW = "forecastViewPanel";
	public static final String CONFIG_VIEW = "configViewPanel";
	public static final String WEATHER_ALERT_VIEW = "weatherAlertView";
	public static final String RADIO_STATION_VIEW = "radioStationsView";
	public static final String WEB_SERVER_VIEW = "webServerView";
	public static final String ERROR_VIEW = "errorView";
	public static final String MP3_VIEW = "mp3_view";
		
	
	public static final String MUSIC_TOGGELED = "Music toggeled";
	public static final String CARD_PANEL = "card_panel";
	public static final String PREFERENCES = "PREFERENCES";
	
	public static final String FORECAST_RESULT = "forecastResult";
	public static final String SENSOR_INFO = "TempSensorInfo";
	
	public static final String FETCH_FORECAST = "fetchForecast";
	public static final String WEATHER_LST_UPD = "WeatherLastUpdatedDate";	
	
	public static final String FORECAST_DISPLAY_LOAD = "DisplayForecastLoading";
	public static final String FORECAST_DISPLAY_ERROR = "DisplayError";
	public static final String FORECAST_CITY = "forecastCity";
	
	
	public static final String THEMES_HANDLER = "themesHandler";
	public static final String THEMES_BACKGROUND_IMG_UPDATE = "themesBackgroundImgChange";
	public static final String DAY_NIGHT_CYCLE = "dayNightCycle";	
	public static final String CURRENT_BACKGROUND = "currBack";
	
	public static final String CHECK_INTERNET = "CheckingNetConnection";
	
	//volume
	public static final String RADIO_VOLUME_ICON_TRIGGER = "radioVolTrigger";
	public static final String RADIO_STREAM_ERROR  = "RadioStreamError";
	public static final String MP3_VOLUME_ICON_TRIGGER = "mp3VolTrigger";
	
	//alarm
	public static final String BUZZER_CHANGED = "AlarmTypeChanged";
	public static final String TURN_OFF_ALARM = "TurnOffAlarm";
	public static final String UPDATE_ALARMS = "UPDATE_ALARMS";
	public static final String REMOVE_TRIGGER = "REMOVE_TRIGGER";
	public static final String ALA_SAY_WEEK_UPD = "day of the week updated";	
	
	public static final String NA_ICON = "cloudNa.png";
	
	//time mask
	public static final String HOUR_MIN = "HH:mm";
	
	public static DecimalFormat numberFormat = new DecimalFormat("#.#");
	
	public static String ERROR_HANDLER = "errorHandler";
	public static String ERROR_BROADCAST = "errorBroadcast";
	
	//DB
	public static final String DB_URL = "./PiClock;DB_CLOSE_DELAY=120";
	public static final String DB_USER = "PiClock";
	public static final String DB_PASS = "12345PiClock";
	
	//mp3
	public static final String RELOAD_FROM_WEB = "reloadFromWeb";
	public static final String B_VISIBLE_FRM_BUZZ_SEL = "selectMakeVisibleFromBuzzerSelect";
	public static final String VOLUME_SENT_FOR_CONFIG_MP3 = "VOLUME_SENT_FOR_CONFIG_mp3";
	public static final String VOLUME_SENT_FOR_CONFIG_RADIO = "VOLUME_SENT_FOR_CONFIG_RADIO";
	public static final String MP3_PLAYER_ERROR = "MP3_PLAYER_ERROR";
	public static final String MP3_FOLDER ="/home/pi/piClock/mp3/";
	
	
	public static final String VOLUME_DISPLAY = "display_volume_icon";
	public static final String MP3_INFO = "mp3Info";
	public static final String BUZZ_OPT_MSG = "buzzerOptionMessage";
	public static final String BUZZER_OPTION_PANEL = "buzzerOptionPanel";
	public static final String MP3_STREAM_ERROR = "mp3StreamError";
	public static final String MP3_PLAY_NEXT = "PlayingNextMp3";
	
	//hardware
	public static final String HARDWARE = "Hardware";
	
	//ldr
	public static final String LDR_VALUE = "ldr_value"; //value from the LDR.. if detected night or day
		
}