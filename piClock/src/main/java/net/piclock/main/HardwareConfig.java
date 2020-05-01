package net.piclock.main;

import net.piclock.enums.HardwareType;
import net.piclock.enums.LightSensor;
import net.piclock.enums.ScreenType;

//class containing the different hardware for the piClock
public class HardwareConfig {
	
	private ScreenType screenType = null;
	private LightSensor lightSensor = null;
	private HardwareType hardwareType = null;
	
	public HardwareConfig (Preferences pref) {
		
		screenType = ScreenType.valueOf(pref.getScreenType());
		lightSensor = LightSensor.valueOf(pref.getLightSensor());
		hardwareType = HardwareType.valueOf(pref.getHardwareType()); //hardware type contains (Gpis events or Arduino events)
		
		
		
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
}
