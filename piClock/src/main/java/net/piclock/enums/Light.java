package net.piclock.enums;

public enum Light {
	DARK(255,243, 0,1),
	DIM(236,200, 8, 10),
	LIGHT(192,126, 15 , 130),
	BRIGHT(118,77, 50, 210),
	VERY_BRIGHT(69,0, 100, 255),
	GREY_ZONE(0,0, 100, 255),
	VERY_DIM(0,0,3, 5); //very dim is only used when to turn the monitor back on with button.
	
	private int ldrHighLevel = -1;
	private int ldrLowLevel = -1;
	
	private int pwmLevel = -1;
	private int piTchScreenBrightness = -1;
	
	/**
	 * 
	 * @param highLevel - high level for brightness detection
	 * @param lowLevel - low level for brightness detection
	 * @param pwmLevel - PWM level for Hyperpixel brightness
	 * @param piTchScreenBrightness - backlight brightness for pi touch screen.
	 */
	private Light (int highLevel, int lowLevel, int pwmLevel, int piTchScreenBrightness){
		ldrHighLevel = highLevel;
		ldrLowLevel = lowLevel;
		this.pwmLevel = pwmLevel;
		this.piTchScreenBrightness = piTchScreenBrightness;
	}
	
	public int getLdrHighLevel(){
		return ldrHighLevel;
	}
	public int getLdrLowLevel(){
		return ldrLowLevel;
	}
	public int getPwmLevel(){
		return pwmLevel;
	}
	public int getPiTchScreenBrightness() {
		return piTchScreenBrightness;
	}
	public boolean isDayLight() {
		return this == Light.BRIGHT || this == Light.VERY_BRIGHT || this == Light.DIM || this == Light.LIGHT;
	}

	public static Light setLightLevel(int ldrLevel){
		if (ldrLevel >= Light.DARK.ldrLowLevel && ldrLevel <= Light.DARK.ldrHighLevel){
			return Light.DARK;
		}else if (ldrLevel >= Light.DIM.ldrLowLevel && ldrLevel <= Light.DIM.ldrHighLevel){
			return Light.DIM;
		}else if (ldrLevel >= Light.LIGHT.ldrLowLevel && ldrLevel <= Light.LIGHT.ldrHighLevel){
			return Light.LIGHT;
		}else if (ldrLevel >= Light.BRIGHT.ldrLowLevel && ldrLevel <= Light.BRIGHT.ldrHighLevel){
			return Light.BRIGHT;
		}else if (ldrLevel >= Light.VERY_BRIGHT.ldrLowLevel && ldrLevel <= Light.VERY_BRIGHT.ldrHighLevel){
			return Light.VERY_BRIGHT;
		}
		return Light.GREY_ZONE;
	}
}
