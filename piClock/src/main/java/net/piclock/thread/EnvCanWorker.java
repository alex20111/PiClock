package net.piclock.thread;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.main.Constants;
import net.piclock.main.PiHandler;
import net.piclock.main.Preferences;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;
import net.weather.action.WeatherAction;
import net.weather.bean.Message;
import net.weather.bean.WeatherCurrentModel;
import net.weather.bean.WeatherGenericModel;
import net.weather.enums.EnvCanLang;

public class EnvCanWorker implements Runnable {

	private static final Logger logger = Logger.getLogger( EnvCanWorker.class.getName() );
	
	private SwingContext ct = SwingContext.getInstance();
	private static int load = 0;
	
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
//
//				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.omega.dce-eir.net", 8080));	
//				Utilities.proxy = proxy;
//				Utilities.createAuthForProxy("axb161", "109256PO)");
//				
				wgm =  WeatherAction.getEnvironmentCanadaRSSWeather(pref.getStationCode(), EnvCanLang.english, false, true);
//				
//				wgm = TestAction.getLocal();
				
//				try{
//					Thread.sleep(2000);
//				}catch (InterruptedException ie){
//					ie.printStackTrace();
//				}

				ct.putSharedObject(Constants.WEATHER_LST_UPD, new Date());//to prevent too many refresh.

				
				ct.putSharedObject(Constants.FORECAST_RESULT, wgm);
				
				WeatherCurrentModel wcm = wgm.getWeatherCurrentModel();
//				wcm.setWeather("Mainly rain");
				
				if (wcm.getWeather().toLowerCase().contains("rain")){
					ThemeHandler theme = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
					theme.loadRainBackdrop();
				}else if (wcm.getWeather().toLowerCase().contains("sunny")){
					ThemeHandler theme = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
					theme.loadSunnyBackdrop();
				}else if (wcm.getWeather().toLowerCase().contains("thunder")){
					ThemeHandler theme = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
					theme.loadThunderBackdrop();
				}else if (wcm.getWeather().toLowerCase().contains("snow")){
					ThemeHandler theme = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
					theme.loadSnowBackdrop();
				}
					
				
			}else{
				wgm.addMessage("No Wifi", "Not connected to WIFI", Message.INFO);
				ct.putSharedObject(Constants.FORECAST_DISPLAY_ERROR, wgm);
			}


		}catch (Throwable tr){
			logger.log(Level.SEVERE, "Error in EnvCanWorker", tr);
			wgm.addMessage("Severe Error", "In trowable is a severe error", Message.ERROR);
			ct.putSharedObject(Constants.FORECAST_DISPLAY_ERROR, wgm);
		}		
	}
}
