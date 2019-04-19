package net.piclock.enums;

public enum Light {
	DARK(255,234, 0), DIM(226,194, 5), LIGHT(186,126, 15), BRIGHT(118,60, 50), VERY_BRIGHT(52,0, 100), GREY_ZONE(0,0, 100), VERY_DIM(0,0,3); //very dim is only used when to turn the monitor back on with button.
	
	private int ldrHighLevel = -1;
	private int ldrLowLevel = -1;
	
	private int pwmLevel = -1;
	
	private Light (int highLevel, int lowLevel, int pwmLevel){
		ldrHighLevel = highLevel;
		ldrLowLevel = lowLevel;
		this.pwmLevel = pwmLevel;
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
