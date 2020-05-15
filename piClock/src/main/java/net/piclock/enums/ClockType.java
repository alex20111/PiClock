package net.piclock.enums;

public enum ClockType {

	TM1637_ARDUINO, TM1637_PI, HT16K33_ARDUINO, HT16K33_PI;
	
	
	
	public boolean isSensorOnArduino() {
		return this == TM1637_ARDUINO || this == HT16K33_ARDUINO;
	}
	
	public boolean isSensorOnPi() {
		return this == TM1637_PI || this == HT16K33_PI;
	}
	
	public boolean requireI2CBus() {
		return this == ClockType.HT16K33_PI;
	}
}
