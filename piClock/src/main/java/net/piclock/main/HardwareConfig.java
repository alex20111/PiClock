package net.piclock.main;

import net.piclock.enums.ClockType;
import net.piclock.enums.HardwareType;
import net.piclock.enums.LightSensor;
import net.piclock.enums.ScreenType;

//class containing the different hardware for the piClock
public class HardwareConfig {
	
	private ScreenType screenType = null;
	private LightSensor lightSensor = null;
	private ClockType  clockType = null;
	private HardwareType hardwareType = null;
	
	public HardwareConfig (Preferences pref) {
		
		screenType = ScreenType.valueOf(pref.getScreenType());
		lightSensor = LightSensor.valueOf(pref.getLightSensor());
		hardwareType = HardwareType.valueOf(pref.getHardwareType()); //hardware type contains (Gpis events or Arduino events)
		clockType  = ClockType.valueOf(pref.getClockType());
		
		
	}
	//you should have device handler that call the arduino or pi screen or si4703 or loght sensor.. 

	
	public ScreenType getScreenType() {
		return this.screenType;
	}
	
	public LightSensor getLightSensor() {
		return this.lightSensor;
	}
	
	public HardwareType getHardwareType() {
		return this.hardwareType;
	}
	
	public boolean isPIi2cRequired() {
		return lightSensor == LightSensor.BH1750FVI_PI || clockType == ClockType.HT16K33_PI;
	}


	public ClockType getClockType() {
		return clockType;
	}


	@Override
	public String toString() {
		return "HardwareConfig [screenType=" + screenType + ", lightSensor=" + lightSensor + ", clockType=" + clockType
				+ ", hardwareType=" + hardwareType + "]";
	}
}
