package net.piclock.weather;

public class Temperature {
	

	private  String tempSun = "-99";
	private  String tmpSunUpdDt = "21:45am";
	private  String tempShade = "-99";
	private  String tmpShadeUpdDt = "10:45am";
	private  String tempPool = "-99";
	private  String tmpPoolUpdDt = "11:45am";
	
	
	public String getTempSun() {
		return tempSun;
	}
	public void setTempSun(String tempSun) {
		this.tempSun = tempSun;
	}
	public String getTempShade() {
		return tempShade;
	}
	public void setTempShade(String tempShade) {
		this.tempShade = tempShade;
	}
	public String getTempPool() {
		return tempPool;
	}
	public void setTempPool(String tempPool) {
		this.tempPool = tempPool;
	}

	public String getTmpSunUpdDt() {
		return tmpSunUpdDt;
	}
	public void setTmpSunUpdDt(String tmpSunUpdDt) {
		this.tmpSunUpdDt = tmpSunUpdDt;
	}
	public String getTmpShadeUpdDt() {
		return tmpShadeUpdDt;
	}
	public void setTmpShadeUpdDt(String tmpShadeUpdDt) {
		this.tmpShadeUpdDt = tmpShadeUpdDt;
	}
	public String getTmpPoolUpdDt() {
		return tmpPoolUpdDt;
	}
	public void setTmpPoolUpdDt(String tmpPoolUpdDt) {
		this.tmpPoolUpdDt = tmpPoolUpdDt;
	}
	@Override
	public String toString() {
		return "Temperature [tempSun=" + tempSun + ", tmpSunUpdDt=" + tmpSunUpdDt + ", tempShade=" + tempShade
				+ ", tmpShadeUpdDt=" + tmpShadeUpdDt + ", tempPool=" + tempPool + ", tmpPoolUpdDt=" + tmpPoolUpdDt
				+ "]";
	}


}
