package net.piclock.bean;

public class LightLevel {
	
	public  static final String DARK = "Dark";
	public  static final String LIGHT = "Light";
	
	private String status = LIGHT;
	
	private int ldrValue = 0;
	private int screenDimMode = 0;	
	
	public LightLevel(int ldr, int dimMode) {
		this.ldrValue = ldr;
		
		if (ldrValue ==  0) {
			status = DARK;
		}else {
			status = LIGHT;
		}
		
		this.screenDimMode =dimMode;
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
	
	public void setStatus(String status) {
		this.status = status;
	}
	

	public int getScreenDimMode() {
		return screenDimMode;
	}

	public void setScreenDimMode(int screenDimMode) {
		this.screenDimMode = screenDimMode;
	}
	@Override
	public String toString() {
		return "LightLevel [ldrValue=" + ldrValue + ", status=" + status + ", Dim mode: " + screenDimMode + " ]";
	}
	

}
