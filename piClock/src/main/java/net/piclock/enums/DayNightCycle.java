package net.piclock.enums;

public enum DayNightCycle {
	DAY, NIGHT, NONE,NOT_DEFINED;
	
	
	public boolean isDay() {
		return this == DayNightCycle.DAY;
	}
	
	public boolean isNight() {
		return this == DayNightCycle.NIGHT;
	}
}
