package net.piclock.weather;

import java.util.Date;

public class WeatherBean {

	private Temperature tempSun;
	private Temperature tempShade;
	
	public Temperature getTempSun() {
		return tempSun;
	}
	public void setTempSun(Temperature tempSun) {
		this.tempSun = tempSun;
	}
	public Temperature getTempShade() {
		return tempShade;
	}
	public void setTempShade(Temperature tempShade) {
		this.tempShade = tempShade;
	}
	
}