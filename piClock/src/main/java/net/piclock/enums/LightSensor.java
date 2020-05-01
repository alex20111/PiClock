package net.piclock.enums;

public enum LightSensor {
	
	LDR_ARDUINO, LDR_PI, TSL2591_ARDUINO, TSL2591_PI;

	
	public boolean isSensorOnArduino() {
		return this == LDR_ARDUINO || this == TSL2591_ARDUINO;
	}
	
	public boolean isSensorOnPi() {
		return this == LDR_PI || this == TSL2591_PI;
	}
}
