package net.piclock.main;

import net.piclock.enums.HardwareType;
import net.piclock.enums.LightSensor;
import net.piclock.enums.ScreenType;

public class Preferences {

	//general settings
	private String screenType = ScreenType.HYPERPIXEL40.name();
	private String hardwareType = HardwareType.ARDUINO.name();
	private String lightSensor  = LightSensor.LDR_ARDUINO.name();
	
	private String wifi = "";
	private String wifiPass = "";
	private boolean autoOffScreen = true; //turn on off screen
	private boolean wifiOff		  = true;
	
	//weather preferences
	private boolean weatherActivated = false;
	private String weatherProvider = "";
	private String weatherCountry = "";
	private String weatherCity = "";
	private int weatherRefresh = 60;
	private String weatherLang = "en";
	private String stationCode = "";//can be anything. For env can its the airport code (key) to fetch the weather..
		
	//RADIO
	private String radioStation = "";
	private int sleepInMin = 0;
	
	//volume
	private int lastVolumeLevel = 20;
	
	//Limited access for settings
	private boolean isSettingPassProtected = false;
	private String settingsPassword = "";
	
	

	public String getWeatherProvider() {
		return weatherProvider;
	}
	public void setWeatherProvider(String weatherProvider) {
		this.weatherProvider = weatherProvider;
	}
	public String getWeatherCity() {
		return weatherCity;
	}
	public void setWeatherCity(String weatherCity) {
		this.weatherCity = weatherCity;
	}
	public int getWeatherRefresh() {
		return weatherRefresh;
	}
	public void setWeatherRefresh(int weatherRefresh) {
		this.weatherRefresh = weatherRefresh;
	}
	public boolean isWeatherActivated() {
		return weatherActivated;
	}
	public void setWeatherActivated(boolean weatherActivated) {
		this.weatherActivated = weatherActivated;
	}
	public String getWeatherLang() {
		return weatherLang;
	}
	public void setWeatherLang(String weatherLang) {
		this.weatherLang = weatherLang;
	}
	public String getWeatherCountry() {
		return weatherCountry;
	}
	public void setWeatherCountry(String weatherCountry) {
		this.weatherCountry = weatherCountry;
	}
	public String getStationCode() {
		return stationCode;
	}
	public void setStationCode(String stationCode) {
		this.stationCode = stationCode;
	}
	public String getWifi() {
		return wifi;
	}
	public void setWifi(String wifi) {
		this.wifi = wifi;
	}
	public String getWifiPass() {
		return wifiPass;
	}
	public void setWifiPass(String wifiPass) {
		this.wifiPass = wifiPass;
	}
	public boolean isAutoOffScreen() {
		return autoOffScreen;
	}
	public void setAutoOffScreen(boolean autoOffScreen) {
		this.autoOffScreen = autoOffScreen;
	}
	
	public boolean isWifiCredentialProvided(){
		boolean yes = false;
		
		if (this.wifi != null && this.wifi.trim().length() > 0){
			if (this.wifiPass != null && this.wifiPass.length() > 0){
				yes = true;
			}			
		}
				
		return yes;
	}
	public String getRadioStation() {
		return radioStation;
	}
	public void setRadioStation(String radioStation) {
		this.radioStation = radioStation;
	}
	public int getSleepInMin() {
		return sleepInMin;
	}
	public void setSleepInMin(int sleepInMin) {
		this.sleepInMin = sleepInMin;
	}
	public int getLastVolumeLevel() {
		return lastVolumeLevel;
	}
	public void setLastVolumeLevel(int lastVolumeLevel) {
		this.lastVolumeLevel = lastVolumeLevel;
	}
	public boolean isWifiOff() {
		return wifiOff;
	}
	public void setWifiOff(boolean wifiOff) {
		this.wifiOff = wifiOff;
	}
	public String getScreenType() {
		return screenType;
	}
	public void setScreenType(String screenType) {
		this.screenType = screenType;
	}
		public String getSettingsPassword() {
		return settingsPassword;
	}
	public void setSettingsPassword(String settingsPassword) {
		this.settingsPassword = settingsPassword;
	}
	public boolean isSettingPassProtected() {
		return isSettingPassProtected;
	}
	public void setSettingPassProtected(boolean isSettingPassProtected) {
		this.isSettingPassProtected = isSettingPassProtected;
	}
	public String getHardwareType() {
		return hardwareType;
	}
	public void setHardwareType(String hardwareType) {
		this.hardwareType = hardwareType;
	}
	public String getLightSensor() {
		return lightSensor;
	}
	public void setLightSensor(String lightSensor) {
		this.lightSensor = lightSensor;
	}
	@Override
	public String toString() {
		return "Preferences [ weatherActivated=" + weatherActivated + ", weatherProvider=" + weatherProvider + ", weatherCity="
				+ weatherCity + ", weatherRefresh=" + weatherRefresh + "]";
	}
	
	
}
