package net.piclock.enums;

public enum ScreenType {
	PI_TOUCH_SCREEN(10,5), HYPERPIXEL40(3,0);
	
	private int lowestBacklight = 0;
	private int ldrDarkValue   = 0;//value from the LDR when the screen should turn black
	
	private ScreenType(int backLight, int darkLevel) {
		this.lowestBacklight = backLight;
		this.ldrDarkValue = darkLevel;
	}
	
	public int getLowestBacklight() {
		return this.lowestBacklight;
	}
	
	public int getLdrDarkValue() {
		return this.ldrDarkValue;
	}
}
