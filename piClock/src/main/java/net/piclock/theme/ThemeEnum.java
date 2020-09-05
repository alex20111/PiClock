package net.piclock.theme;

import java.time.LocalDate;

public enum ThemeEnum {

	defaultTheme (null, null),
	winterTheme (null, null), 
	HALLOWEEN_THEME("0000-08-22", "0000-08-22"), 
	CHRISTMAS_THEME("0000-12-14", "0000-12-31");
	
	
	private LocalDate themeStart;
	private LocalDate themeEnd;
	
	private ThemeEnum(String start, String end) {
		this.themeStart = (start != null ? LocalDate.parse(start): null);
		this.themeEnd = (end != null ? LocalDate.parse(end): null) ;
	}
	
	public LocalDate getThemeStart() {
		return this.themeStart;
	}
	
	public LocalDate getThemeEnd() {
		return this.themeEnd;
	}
	
//	public boolean isThemeActive(LocalDate dt) {
//		
//	}
}
