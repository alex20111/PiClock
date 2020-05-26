package net.piclock.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import net.piclock.enums.ClockType;
import net.piclock.enums.HardwareType;
import net.piclock.enums.LightSensor;
import net.piclock.enums.ScreenType;
import net.piclock.main.Preferences;

public class PreferencesHandler {

	//default config
	private final static String SCREEN_TYPE     = "SCREEN_TYPE";
	private final static String LIGHT_SENSOR     = "LIGHT_SENSOR";
	private final static String HWD_TYPE     = "HARDWARE_TYPE";
	private final static String CLOCK_TYPE  = "CLOCK_TYPE";
	
	private final static String WIFI_SSID 		= "WIFI_SSID";
	private final static String WIFI_PASS 		= "WIFI_PASS";
	private final static String AUTO_SCREEN_OFF = "AUTO_SCREEN_OFF";
	private final static String WIFI_OFF = "WIFI_OFF";
	
	//weather
	private final static String WEATHER_ACTIVATED 	= "WEATHER_ACTIVATED";
	private final static String WEATHER_PROVIDER 	= "WEATHER_PROVIDER";
	private final static String WEATHER_CITY 		= "WEATHER_CITY";
	private final static String WEATHER_REFRESH 	= "WEATHER_REFRESH";
	private final static String WEATHER_STATION_CODE 	= "WEATHER_STATION_CODE";
	
	//RADIO
	private final static String RADIO_STATION 		= "RADIO_STATION";
	private final static String RADIO_SLEEP_TIME	= "RADIO_SLEEP_TIME";
	
	//VOLUME
	private final static String LAST_VOLUME	= "VOLUME";
	
	//SETTINGS PASSWORD
	private final static String SETTINGS_PASS_PROTECTED	= "SETTINGS_PASS_PROTECTED";
	private final static String SETTINGS_PASS	= "SETTINGS_PASS";
	
	private final static String prefFileName = "user_prf.cfg";
	
	
	public static void save(Preferences prefs) throws IOException{
		Properties prop = new Properties();
		OutputStream output = null;

		try {
			output = new FileOutputStream(prefFileName);

			//default config
			prop.setProperty(SCREEN_TYPE, String.valueOf(prefs.getScreenType()));
			prop.setProperty(LIGHT_SENSOR, prefs.getLightSensor());
			prop.setProperty(HWD_TYPE, prefs.getHardwareType());
			prop.setProperty(CLOCK_TYPE, prefs.getClockType());
			
			prop.setProperty(AUTO_SCREEN_OFF, String.valueOf(prefs.isAutoOffScreen()));
			prop.setProperty(WIFI_OFF, String.valueOf(prefs.isWifiOff()));
			prop.setProperty(WIFI_SSID,prefs.getWifi());
			prop.setProperty(WIFI_PASS,prefs.getWifiPass());
			
			// set the properties value
			prop.setProperty(WEATHER_ACTIVATED, String.valueOf(prefs.isWeatherActivated()));
			prop.setProperty(WEATHER_PROVIDER, prefs.getWeatherProvider());
			prop.setProperty(WEATHER_CITY, prefs.getWeatherCity());
			prop.setProperty(WEATHER_REFRESH, String.valueOf(prefs.getWeatherRefresh()));
			prop.setProperty(WEATHER_STATION_CODE, prefs.getStationCode());

			//RADIO
			prop.setProperty(RADIO_STATION,prefs.getRadioStation());
//			prop.setProperty(RADIO_SLEEP, String.valueOf(prefs.isRadioSleep()));
			prop.setProperty(RADIO_SLEEP_TIME, String.valueOf(prefs.getSleepInMin()));
			
			prop.setProperty(LAST_VOLUME, String.valueOf(prefs.getLastVolumeLevel()));
			
			//settings
			prop.setProperty(SETTINGS_PASS_PROTECTED, String.valueOf(prefs.isSettingPassProtected()));
			prop.setProperty(SETTINGS_PASS, prefs.getSettingsPassword());
			// save properties to project root folder
			StringBuilder sb = new StringBuilder("User preferences for world best clock");
			sb.append("\nHARDWARE_TYPE=OPTIONS\n\tOPTIONS: ARDUINO, PI ");
			sb.append("\nSCREEN_TYPE=OPTIONS\n\tOPTIONS: PI_TOUCH_SCREEN, HYPERPIXEL40 ");
			sb.append("\nLIGHT_SENSOR=OPTIONS\n\tOPTIONS: LDR_ARDUINO, LDR_PI, TSL2591_ARDUINO, TSL2591_PI ");		
			sb.append("\nCLOCK_TYPE=OPTIONS\n\tOPTIONS: TM1637_ARDUINO, TM1637_PI, HT16K33_ARDUINO, HT16K33_PI ");
			prop.store(output, sb.toString());
		
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
			userPrefs.setScreenType(prop.getProperty(SCREEN_TYPE, ScreenType.HYPERPIXEL40.name()));
			userPrefs.setLightSensor(prop.getProperty(LIGHT_SENSOR, LightSensor.TSL2591_PI.name()));
			userPrefs.setHardwareType(prop.getProperty(HWD_TYPE, HardwareType.PI.name()));
			userPrefs.setClockType(prop.getProperty(CLOCK_TYPE, ClockType.TM1637_PI.name()));
			userPrefs.setAutoOffScreen(Boolean.valueOf(prop.getProperty(AUTO_SCREEN_OFF, "true")));
			userPrefs.setWifiOff(Boolean.valueOf(prop.getProperty(WIFI_OFF, "true")));
			userPrefs.setWifi(prop.getProperty(WIFI_SSID, ""));
			userPrefs.setWifiPass(prop.getProperty(WIFI_PASS, ""));			
			
			
			//WEATHER
			userPrefs.setWeatherActivated(Boolean.valueOf(prop.getProperty(WEATHER_ACTIVATED, "false")));
			userPrefs.setWeatherProvider(prop.getProperty(WEATHER_PROVIDER, ""));
			userPrefs.setWeatherCity(prop.getProperty(WEATHER_CITY, ""));
			userPrefs.setWeatherRefresh(Integer.parseInt(prop.getProperty(WEATHER_REFRESH, "0")));
			userPrefs.setStationCode(prop.getProperty(WEATHER_STATION_CODE, "on-118"));	
						
			//RADIO
			userPrefs.setRadioStation(prop.getProperty(RADIO_STATION, ""));
			userPrefs.setSleepInMin(Integer.parseInt(prop.getProperty(RADIO_SLEEP_TIME, "0")));	
			
			//volume
			userPrefs.setLastVolumeLevel(Integer.parseInt(prop.getProperty(LAST_VOLUME, "20")));
			
			//settings
			userPrefs.setSettingPassProtected(Boolean.valueOf(prop.getProperty(SETTINGS_PASS_PROTECTED, "false")));
			userPrefs.setSettingsPassword(prop.getProperty(SETTINGS_PASS, "20"));

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
