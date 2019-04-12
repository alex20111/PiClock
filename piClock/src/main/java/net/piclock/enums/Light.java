package net.piclock.enums;

public enum Light {
	DARK(255,240, 0), DIM(239,200, 20), LIGHT(199,150, 70), BRIGHT(149,70, 80), VERY_BRIGHT(69,0, 255), NONE(0,0, 255);
	
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
		return Light.NONE;
	}
}
