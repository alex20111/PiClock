package net.piclock.theme;

import java.awt.Color;

import net.piclock.enums.LabelEnums;

public class LabelTheme {

	private LabelEnums name;
	private Color textDayColor;
	private Color textNightColor;
	
	public LabelTheme(){}
	public LabelTheme(LabelEnums name, Color day, Color night){
		this.name = name;
		this.textDayColor = day;
		this.textNightColor = night;			
	}
	
	public LabelEnums getName() {
		return name;
	}
	public void setName(LabelEnums name) {
		this.name = name;
	}
	public Color getTextDayColor() {
		return textDayColor;
	}
	public void setTextDayColor(Color textDayColor) {
		this.textDayColor = textDayColor;
	}
	public Color getTextNightColor() {
		return textNightColor;
	}
	public void setTextNightColor(Color textNightColor) {
		this.textNightColor = textNightColor;
	}	
	@Override
	public String toString() {
		return "LabelTheme [name=" + name + ", textDayColor=" + textDayColor + ", textNightColor=" + textNightColor
				+ "]";
	}	
}
