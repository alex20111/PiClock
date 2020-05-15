package net.piclock.enums;

public enum ScreenType {
	PI_TOUCH_SCREEN(13,177), HYPERPIXEL40(3,180);
	
	private int minBacklight = 0;
	private int maxBacklight = 0;//value from the LDR when the screen should turn 
	
	
	private ScreenType(int min, int max) {
		this.minBacklight = min;
		this.maxBacklight = max;
//		this.lowestBrightness = low;
	}
	
	public int getMinBacklight() {
		return this.minBacklight;
	}
	
	public int getMaxBacklight() {
		return this.maxBacklight;
	}
	
//	public int getLowestBrightness() {
//		return this.lowestBrightness;
//	}
}
