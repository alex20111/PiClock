package net.piclock.thread;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.syndication.io.ParsingFeedException;

import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.handlers.PiHandler;
import net.piclock.main.Constants;
import net.piclock.main.Preferences;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;
import net.piclock.util.FormatStackTrace;
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

			logger.info("pref.getWeatherCity(): " + pref.getWeatherCity() + ". Code: " + pref.getStationCode() + "  - Wifi connected: "+ handler.isWifiConnected() + 
					". Wifi Internet connected: " + handler.isWifiInternetConnected());

			if (handler.isWifiConnected()){

				int retry = 0;

				Host host = Host.valueOf(pref.getWeatherProvider());

				while (retry < 3) {
					boolean inError = false;
					retry++;
					try {

						if (host == Host.envCanada) {
							wgm =  WeatherAction.getEnvironmentCanadaRSSWeather(pref.getStationCode(), WeatherLang.english, false, true);
						}else if (host == Host.DARKSKY) {
							wgm = WeatherAction.getDarkSkyForecast(Double.valueOf(city.getLat()).longValue(),Double.valueOf( city.getLon()).longValue(), null, DarkSkyUnits.SI,  WeatherLang.english);
						}

						ct.putSharedObject(Constants.WEATHER_LST_UPD, new Date());//to prevent too many refresh.

						ct.putSharedObject(Constants.FORECAST_RESULT, wgm);

						//Environment canada

						WeatherCurrentModel wcm = wgm.getWeatherCurrentModel();
						if (host == Host.envCanada) {
							logger.log(Level.CONFIG, "Current Weather NAME: " + wcm.getSummary());
							ThemeHandler theme = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
							if (summaryContains(wcm.getSummary(), "rain")){						
								theme.loadRainBackdrop();
							}else if (summaryContains(wcm.getSummary(),"thunder")){						
								theme.loadThunderBackdrop();
							}else if (summaryContains(wcm.getSummary(),"snow")){						
								theme.loadSnowBackdrop();
							}else if (summaryContains(wcm.getSummary(),"cloud")) {					
								theme.loadCloudyBackdrop();
							}else if (summaryContains(wcm.getSummary(),"fog","mist")){					
								theme.loadFogBackdrop();
							}else {//default sunny						
								theme.loadSunnyBackdrop();
							}
						}else if (host == Host.DARKSKY) {
							logger.log(Level.CONFIG, "Dark Sky Current Weather : " + wcm.getSummary());
							ThemeHandler theme = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
							if (summaryContains(wcm.getSummary(), "rain", "drizzle")){						
								theme.loadRainBackdrop();
							}else if (summaryContains(wcm.getSummary(),"thunder")){						
								theme.loadThunderBackdrop();
							}else if (summaryContains(wcm.getSummary(),"snow")){						
								theme.loadSnowBackdrop();
							}else if (summaryContains(wcm.getSummary(),"cloud")) {					
								theme.loadCloudyBackdrop();
							}else if (summaryContains(wcm.getSummary(),"fog","mist")){					
								theme.loadFogBackdrop();
							}else {//default sunny						
								theme.loadSunnyBackdrop();
							}
						}
						
						retry = 99;

					}catch(UnknownHostException uhx) {
						Thread.sleep(10000);
						logger.log(Level.INFO, "Unknown host exception. Retry # " + retry);
						inError = true;
						
					} catch(ParsingFeedException pfe) {
						logger.log(Level.INFO, "ParsingFeedException. Retry" + retry); //TODO write it to file.
						Thread.sleep(10000);
						inError = true;
					}
					catch(SocketException s) {
						logger.log(Level.INFO, "SocketException. Retry" + retry); //TODO write it to file.
						Thread.sleep(10000);
						inError = true;
					}
					
					if (retry == 3 && inError) {
						//tried 3 times and failed.
						wgm.addMessage("Error", "Max tries", Message.INFO);
						ct.putSharedObject(Constants.FORECAST_DISPLAY_ERROR, wgm);
						logger.log(Level.CONFIG, "Weather max tries");
					}
				}

			}else{
				wgm.addMessage("No Wifi", "Not connected to WIFI", Message.INFO);
				ct.putSharedObject(Constants.FORECAST_DISPLAY_ERROR, wgm);
			}


		}catch (Throwable tr){
			ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
			eh.addError(ErrorType.WEATHER, new ErrorInfo(new FormatStackTrace(tr).getFormattedException()));
			logger.log(Level.SEVERE, "Error in WeatherWorker", tr);
			wgm.addMessage("Error", "In trowable is a severe error", Message.ERROR);
			ct.putSharedObject(Constants.FORECAST_DISPLAY_ERROR, wgm);
		}		
	}

	private boolean summaryContains(String summary, String... weather) {

		if (weather != null && weather.length > 0 && summary != null && summary.length() > 0) {
			for(String wth: weather) {
				if (summary.toLowerCase().contains(wth)) {
					return true;
				}
			}
		}

		return false;
	}
}
