package net.piclock.main;

import java.text.DecimalFormat;

public class Constants {
	
	public static final String ALARM_BTN_HANDLER = "alarmBtnHandler";

	public static final String MAIN_VIEW 	= "main";
	public static final String WEATHER_CONFIG_VIEW = "weatherConfig";
	public static final String ALARM_VIEW 			= "alarm";
	public static final String WEATHER_FORECAST_VIEW = "forecastViewPanel";
	public static final String CONFIG_VIEW = "configViewPanel";
	public static final String WEATHER_ALERT_VIEW = "weatherAlertView";
	public static final String RADIO_STATION_VIEW = "radioStationsView";
	public static final String WEB_SERVER_VIEW = "webServerView";
	
	
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
	
	public static final String CHECK_INTERNET = "CheckingNetConnection";
	
	//volume
	public static final String RADIO_VOLUME_ICON_TRIGGER = "radioVolTrigger";
	public static final String RADIO_STREAM_ERROR  = "RadioStreamError";
	public static final String MP3_VOLUME_ICON_TRIGGER = "mp3VolTrigger";
	
	//alarm
	public static final String BUZZER_CHANGED = "AlarmTypeChanged";
	public static final String TURN_OFF_ALARM = "TurnOffAlarm";
	
	public static final String NA_ICON = "cloudNa.png";
	
	//time mask
	public static final String HOUR_MIN = "HH:mm";
	
	public static DecimalFormat numberFormat = new DecimalFormat("#.#");
	
	//DB
	public static final String DB_URL = "./PiClock";
	public static final String DB_USER = "PiClock";
	public static final String DB_PASS = "12345PiClock";
	
	
	
}