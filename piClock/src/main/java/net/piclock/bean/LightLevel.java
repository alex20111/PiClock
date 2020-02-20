package net.piclock.bean;

import net.piclock.enums.ScreenType;

public class LightLevel {
	
	public  static final String DARK = "Dark";
	public  static final String LIGHT = "Light";
	
	private ScreenType screenType;
	
	private int ldrValue = 0;
	private int screenDimMode = 0;
	private boolean isDark = false;
	private boolean isLight = false;
	
	
	public LightLevel(int ldr, ScreenType type) {
		this.ldrValue = ldr;
		
		if (ldrValue <=  type.getLdrDarkValue()) {
			isDark = true;
		}else {
			isLight = true;
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
		return isDark;
	}

	public void setDark(boolean isDark) {
		this.isDark = isDark;
	}

	public boolean isLight() {
		return isLight;
	}

	public void setLight(boolean isLight) {
		this.isLight = isLight;
	}
	
	public String status() {
		if (isDark) {
			return DARK;
		}else {
			return LIGHT;
		}
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
				+ ", isDark=" + isDark + ", isLight=" + isLight + "]";
	}
	

}
