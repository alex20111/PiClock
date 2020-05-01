package net.piclock.enums;

public enum ScreenType {
	PI_TOUCH_SCREEN(13,177, 1), HYPERPIXEL40(3,180,15);
	
	private int minBacklight = 0;
	private int maxBacklight = 0;//value from the LDR when the screen should turn black
	private int lowestBrightness = 0; //the lowest level of the ldr value for brightness in a room
	
	private ScreenType(int min, int max, int low) {
		this.minBacklight = min;
		this.maxBacklight = max;
		this.lowestBrightness = low;
	}
	
	public int getMinBacklight() {
		return this.minBacklight;
	}
	
	public int getMaxBacklight() {
		return this.maxBacklight;
	}
	
	public int getLowestBrightness() {
		return this.lowestBrightness;
	}
}
