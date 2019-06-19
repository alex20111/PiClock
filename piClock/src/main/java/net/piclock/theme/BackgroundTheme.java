package net.piclock.theme;

import java.io.File;
import java.util.Map;

import net.piclock.enums.BackgroundEnum;
import net.piclock.enums.DayNightCycle;
import net.piclock.enums.IconEnum;
import net.piclock.enums.LabelEnums;

public class BackgroundTheme {
	
	private BackgroundEnum  name;
	private String backgroundImage = "";
	private String backgroundImageNight = "";
	private DayNightCycle cycle = DayNightCycle.DAY;
	private Map<LabelEnums, LabelTheme> labels;
	private Map<IconEnum, IconTheme> labelIconMap;

	private String imageFolder = "";
	
	public BackgroundTheme(){}
	public BackgroundTheme(BackgroundEnum name, String imageDay, String imageNight, String folder){
		this.name = name;
		this.backgroundImage = imageDay;
		this.backgroundImageNight = imageNight;
		this.imageFolder = folder;
	}
	
	public BackgroundEnum getName() {
		return name;
	}
	public void setName(BackgroundEnum name) {
		this.name = name;
	}
	public Map<LabelEnums, LabelTheme> getLabels() {
		return labels;
	}
	public void setLabels(Map<LabelEnums, LabelTheme> labels) {
		this.labels = labels;
	}

	public Map<IconEnum, IconTheme> getLabelIconMap() {
		return labelIconMap;
	}
	public void setLabelIconMap(Map<IconEnum, IconTheme> labelIconMap) {
		this.labelIconMap = labelIconMap;
	}
	public String getBackgroundImage() {
		return backgroundImage;
	}
	public void setBackgroundImage(String backgroundImage) {
		this.backgroundImage = backgroundImage;
	}
	public String getBackgroundImageNight() {
		return backgroundImageNight;
	}
	public void setBackgroundImageNight(String backgroundImageNight) {
		this.backgroundImageNight = backgroundImageNight;
	}
	public DayNightCycle getCycle() {
		return cycle;
	}
	public void setCycle(DayNightCycle cycle) {
		this.cycle = cycle;
	}
	public String getImageFolder() {
		return imageFolder;
	}
	public void setImageFolder(String imageFolder) {
		this.imageFolder = imageFolder;
	}

	/**Return the full path for the background image. **/
	public String fullPathBackImg(DayNightCycle cycle){
		return imageFolder + File.separatorChar + (cycle == DayNightCycle.DAY ? backgroundImage : backgroundImageNight);
	}
	@Override
	public String toString() {
		return "BackgroundTheme [name=" + name + ", backgroundImage=" + backgroundImage + ", cycle=" + cycle
				+ ", labels=" + labels +  ", iCONS: " + labelIconMap + ", imageFolder=" + imageFolder + "]";
	}

}