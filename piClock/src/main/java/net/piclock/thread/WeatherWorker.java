package net.piclock.thread;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.main.Constants;
import net.piclock.main.PiHandler;
import net.piclock.main.Preferences;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;
import net.piclock.weather.City;
import net.weather.action.WeatherAction;
import net.weather.bean.Message;
import net.weather.bean.WeatherCurrentModel;
import net.weather.bean.WeatherGenericModel;
import net.weather.enums.DarkSkyUnits;
import net.weather.enums.Host;
import net.weather.enums.WeatherLang;;

public class WeatherWorker implements Runnable {

	private static final Logger logger = Logger.getLogger( WeatherWorker.class.getName() );
	
	private SwingContext ct = SwingContext.getInstance();
	private static int load = 0;
	private City city;
	
	public WeatherWorker() {
		city = (City) ct.getSharedObject(Constants.FORECAST_CITY);
	}
	
	@Override
	public void run() {

		WeatherGenericModel wgm = new WeatherGenericModel();

		try{

			//fire change to display loading
			ct.putSharedObject(Constants.FORECAST_DISPLAY_LOAD, load++);

			Preferences pref = (Preferences)ct.getSharedObject(Constants.PREFERENCES);

			PiHandler handler = PiHandler.getInstance();

			logger.config("pref.getWeatherCity(): " + pref.getWeatherCity() + ". Code: " + pref.getStationCode() + "  - Wifi connected: "+ handler.isWifiConnected() + 
					". Wifi Internet connected: " + handler.isWifiInternetConnected());

			if (handler.isWifiConnected()){

				Host host = Host.valueOf(pref.getWeatherProvider());

				if (host == Host.envCanada) {
					wgm =  WeatherAction.getEnvironmentCanadaRSSWeather(pref.getStationCode(), WeatherLang.english, false, true);
				}else if (host == Host.DARKSKY) {
					wgm = WeatherAction.getDarkSkyForecast(Long.valueOf(city.getLat()).longValue(),Long.valueOf( city.getLon()).longValue(), null, DarkSkyUnits.SI,  WeatherLang.english);
				}

				ct.putSharedObject(Constants.WEATHER_LST_UPD, new Date());//to prevent too many refresh.

				ct.putSharedObject(Constants.FORECAST_RESULT, wgm);

				WeatherCurrentModel wcm = wgm.getWeatherCurrentModel();
				if (host == Host.envCanada) {
					if (wcm.getSummary().toLowerCase().contains("rain")){
						ThemeHandler theme = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
						theme.loadRainBackdrop();
					}else if (wcm.getSummary().toLowerCase().contains("thunder")){
						ThemeHandler theme = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
						theme.loadThunderBackdrop();
					}else if (wcm.getSummary().toLowerCase().contains("snow")){
						ThemeHandler theme = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
						theme.loadSnowBackdrop();
					}else {//default sunny
						ThemeHandler theme = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
						theme.loadSunnyBackdrop();
					}
				}else if (host == Host.DARKSKY) {
//TODO add darksy theme changes
				}


			}else{
				wgm.addMessage("No Wifi", "Not connected to WIFI", Message.INFO);
				ct.putSharedObject(Constants.FORECAST_DISPLAY_ERROR, wgm);
			}


		}catch (Throwable tr){
			logger.log(Level.SEVERE, "Error in WeatherWorker", tr);
			wgm.addMessage("Severe Error", "In trowable is a severe error", Message.ERROR);
			ct.putSharedObject(Constants.FORECAST_DISPLAY_ERROR, wgm);
		}		
	}
}
