package net.piclock.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import net.piclock.main.Preferences;

public class PreferencesHandler {

	//default config
	private final static String WIFI_SSID 		= "WIFI_SSID";
	private final static String WIFI_PASS 		= "WIFI_PASS";
	private final static String AUTO_SCREEN_OFF = "AUTO_SCREEN_OFF";
	
	//alarm
	private final static String ALARM_ON 		= "ALARM_ON";
	private final static String ALARM_HOUR 	= "ALARM_HOUR";
	private final static String ALARM_MINUTE 	= "ALARM_MINUTE";
	private final static String ALARM_BUZZER 	= "ALARM_BUZZER_TYPE";
	
	//weather
	private final static String WEATHER_ACTIVATED 	= "WEATHER_ACTIVATED";
	private final static String WEATHER_PROVIDER 	= "WEATHER_PROVIDER";
	private final static String WEATHER_CITY 		= "WEATHER_CITY";
	private final static String WEATHER_REFRESH 	= "WEATHER_REFRESH";
	private final static String WEATHER_STATION_CODE 	= "WEATHER_STATION_CODE";
	
	//RADIO
	private final static String RADIO_STATION 		= "RADIO_STATION";
	private final static String RADIO_SLEEP 		= "RADIO_SLEEP";
	private final static String RADIO_SLEEP_TIME	= "RADIO_SLEEP_TIME";
	
	private final static String prefFileName = "user_prf.cfg";
	
	
	public static void save(Preferences prefs) throws IOException{
		Properties prop = new Properties();
		OutputStream output = null;

		try {
			output = new FileOutputStream(prefFileName);

			//default config
			prop.setProperty(AUTO_SCREEN_OFF, String.valueOf(prefs.isAutoOffScreen()));
			prop.setProperty(WIFI_SSID,prefs.getWifi());
			prop.setProperty(WIFI_PASS,prefs.getWifiPass());
			
			// set the properties value
			prop.setProperty(ALARM_ON, String.valueOf(prefs.isAlarmOn()));
			prop.setProperty(ALARM_HOUR, String.valueOf(prefs.getAlarmHour()));
			prop.setProperty(ALARM_MINUTE, String.valueOf(prefs.getAlarmMinutes()));
			prop.setProperty(ALARM_BUZZER, prefs.getAlarmType());

			// set the properties value
			prop.setProperty(WEATHER_ACTIVATED, String.valueOf(prefs.isWeatherActivated()));
			prop.setProperty(WEATHER_PROVIDER, prefs.getWeatherProvider());
			prop.setProperty(WEATHER_CITY, prefs.getWeatherCity());
			prop.setProperty(WEATHER_REFRESH, String.valueOf(prefs.getWeatherRefresh()));
			prop.setProperty(WEATHER_STATION_CODE, prefs.getStationCode());

//			private String weatherCountry = "";
//						private String weatherLang = "en";

			//RADIO
			prop.setProperty(RADIO_STATION,prefs.getRadioStation());
			prop.setProperty(RADIO_SLEEP, String.valueOf(prefs.isRadioSleep()));
			prop.setProperty(RADIO_SLEEP_TIME, String.valueOf(prefs.getSleepInMin()));
			

			// save properties to project root folder
			prop.store(output, "User preferences for world best clock");
		
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static Preferences read() throws IOException{
		
		Preferences userPrefs = null;
		
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream(prefFileName);

			// load a properties file
			prop.load(input);
			userPrefs = new Preferences();
			//default config AUTO_SCREEN_CYCLE
			userPrefs.setAutoOffScreen(Boolean.valueOf(prop.getProperty(AUTO_SCREEN_OFF, "true")));
			userPrefs.setWifi(prop.getProperty(WIFI_SSID, ""));
			userPrefs.setWifiPass(prop.getProperty(WIFI_PASS, ""));			
			
			// ALARM
			userPrefs.setAlarmOn(Boolean.valueOf(prop.getProperty(ALARM_ON,"false")));
			userPrefs.setAlarmHour(Integer.parseInt(prop.getProperty(ALARM_HOUR, "0")));
			userPrefs.setAlarmMinutes(Integer.parseInt(prop.getProperty(ALARM_MINUTE, "0")));
			userPrefs.setAlarmType(prop.getProperty(ALARM_BUZZER, "BUZZER"));
			
			//WEATHER
			userPrefs.setWeatherActivated(Boolean.valueOf(prop.getProperty(WEATHER_ACTIVATED, "false")));
			userPrefs.setWeatherProvider(prop.getProperty(WEATHER_PROVIDER, ""));
			userPrefs.setWeatherCity(prop.getProperty(WEATHER_CITY, ""));
			userPrefs.setWeatherRefresh(Integer.parseInt(prop.getProperty(WEATHER_REFRESH, "0")));
			userPrefs.setStationCode(prop.getProperty(WEATHER_STATION_CODE, "on-118"));	
						
			//RADIO
			userPrefs.setRadioSleep(Boolean.valueOf(prop.getProperty(RADIO_SLEEP, "false")));
			userPrefs.setRadioStation(prop.getProperty(RADIO_STATION, ""));
			userPrefs.setSleepInMin(Integer.parseInt(prop.getProperty(RADIO_SLEEP_TIME, "0")));	
			

		}catch(FileNotFoundException f){
			userPrefs = new Preferences();
		
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
		return userPrefs;
	}	
}
