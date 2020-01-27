package net.piclock.enums;

public enum LDRCycle {

	LIGHT, DARK, NOT_DEFINED;
	
	
	
	public boolean isLight() {
		return this == LDRCycle.LIGHT;
	}
	
	public boolean isDark() {
		return this == LDRCycle.DARK;
	}
}
