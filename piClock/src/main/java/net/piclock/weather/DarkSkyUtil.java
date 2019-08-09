package net.piclock.weather;

import java.util.HashMap;
import java.util.Map;

import net.piclock.util.ImageUtils;

public class DarkSkyUtil {
	private static final String CLEAR_DAY = "clear-day";
	private static final String CLEAR_NIGHT = "clear-night";
	private static final String RAIN = "rain";
	private static final String	SNOW = "snow";
	private static final String SLEET = "sleet";
	private static final String WIND = "wind";
	private static final String FOG = "fog";
	private static final String CLOUDY = "cloudy";
	private static final String PARTLY_CLOUDY_DAY = "partly-cloudy-day";
	private static final String PARTLY_CLOUDY_NIGHT = "partly-cloudy-night";
	
	private static Map<String, String> icons;
	
	static {
		icons = new HashMap<>();
		icons.put(CLEAR_DAY, "clear-day.png");
		icons.put(CLEAR_NIGHT, "clear-night.png");
		icons.put(RAIN,"rain.png");
		icons.put(SNOW, "snow.png");
		icons.put(SLEET,"sleet.png");
		icons.put(WIND,"wind.png");
		icons.put(FOG,"fog.png");
		icons.put(CLOUDY,"cloudy.png");
		icons.put(PARTLY_CLOUDY_DAY,"partly-cloudy-day.png");
		icons.put(PARTLY_CLOUDY_NIGHT,"partly-cloudy-night.png");
	}
	
	public DarkSkyUtil() {

	}
	
	public static String getIconFileName(String icon) {
		String fileName = "";
		if (icons.containsKey(icon)){
			fileName = icons.get(icon);
		}else {
			fileName = ImageUtils.WEATHER_NA_ICON;
		}
		return fileName;
	}
}


