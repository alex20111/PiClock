package net.piclock.enums;

public enum LightSensor {
	
	LDR_ARDUINO(15,255), LDR_PI(15, 255), TSL2591_ARDUINO(1, 200), TSL2591_PI(1, 200), BH1750FVI_PI(1, 110), BH1750FVI_ARDUINO(1, 110);

	private int darkTreshold = 0; //value used to turn screen on..
	private int luxMaxValue = 0; //the max value a sensor will gather for the room overall light during the day..
								//it's basically it's max value that it will get with the amount of light in the room
	
	private  LightSensor(int darknessTreshold, int luxMaxValue) {
		this.darkTreshold = darknessTreshold;
		this.luxMaxValue = luxMaxValue;
	}
	
	public int getDarkThreshold() {
		return darkTreshold;
	}
	
	public int getLuxMaxValue() {
		return luxMaxValue;
	}
	
	public boolean isSensorOnArduino() {
		return this == LDR_ARDUINO || this == TSL2591_ARDUINO || this == BH1750FVI_ARDUINO;
	}
	
	public boolean isSensorOnPi() {
		return this == LDR_PI || this == TSL2591_PI || this == BH1750FVI_PI;
	}
	
	public boolean requireI2CBus() {
		return this == LightSensor.BH1750FVI_PI;
	}
}
