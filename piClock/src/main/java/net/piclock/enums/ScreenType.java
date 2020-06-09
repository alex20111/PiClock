package net.piclock.enums;

public enum ScreenType {
	PI_TOUCH_SCREEN(13,177, 13), HYPERPIXEL40(3,180, 0);
	
	private int minBacklight = 0;
	private int maxBacklight = 0;//value from the LDR when the screen should turn 
	private int brightnessAdj = 0; //to adjust the brightness of the screen if the lux value is not enough	
	
	private ScreenType(int min, int max, int adj) {
		this.minBacklight = min;
		this.maxBacklight = max;
		this.brightnessAdj = adj;
	}
	
	public int getMinBacklight() {
		return this.minBacklight;
	}
	
	public int getMaxBacklight() {
		return this.maxBacklight;
	}
	
	public int getBrightnessAdj() {
		return this.brightnessAdj;
	}
	
}
