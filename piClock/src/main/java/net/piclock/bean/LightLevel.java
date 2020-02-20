package net.piclock.bean;

import net.piclock.enums.ScreenType;
public class LightLevel {
	
	public  static final String DARK = "Dark";
	public  static final String LIGHT = "Light";
	
	private ScreenType screenType;
	private String status = LIGHT;
	
	private int ldrValue = 0;
	private int screenDimMode = 0;	
	
	public LightLevel(int ldr, ScreenType type) {
		this.ldrValue = ldr;
		
		if (ldrValue <=  type.getLdrDarkValue()) {
			status = DARK;
		}else {
			status = LIGHT;
		}
		
		this.screenType = type;
		this.screenDimMode =type.getLowestBacklight();
	}


	public ScreenType getScreenType() {
		return screenType;
	}

	public void setScreenType(ScreenType screenType) {
		this.screenType = screenType;
	}

	public int getLdrValue() {
		return ldrValue;
	}

	public void setLdrValue(int ldrValue) {
		this.ldrValue = ldrValue;
	}

	public boolean isDark() {
		return status.equals(DARK);
	}

	public boolean isLight() {
		return status.equals(LIGHT);
	}
	
	public String status() {
		return status;
	}

	public int getScreenDimMode() {
		return screenDimMode;
	}

	public void setScreenDimMode(int screenDimMode) {
		this.screenDimMode = screenDimMode;
	}
	@Override
	public String toString() {
		return "LightLevel [screenType=" + screenType + ", ldrValue=" + ldrValue + ", screenDimMode=" + screenDimMode
				+ ", status=" + status + "]";
	}
	

}
