package net.piclock.weather;

import java.util.Date;
import java.util.Optional;

public class WeatherBean {

	private Temperature tempSun;
	private Temperature tempShade;
	
	public Optional<Temperature> getTempSun() {
		return Optional.ofNullable(tempSun);
	}
	public void setTempSun(Temperature tempSun) {
		this.tempSun = tempSun;
	}
	public Optional<Temperature> getTempShade() {
		return Optional.ofNullable(tempShade);
	}
	public void setTempShade(Temperature tempShade) {
		this.tempShade = tempShade;
	}
	
}