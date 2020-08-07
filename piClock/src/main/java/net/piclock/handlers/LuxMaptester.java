package net.piclock.handlers;

import net.piclock.enums.LightSensor;
import net.piclock.enums.ScreenType;


public class LuxMaptester {

	public static void main(String[] args) {
		
		float lux = 0.8333333f;
		
		if (lux >= 0.80) {
			System.out.println("Out is higher");
		}
		
		
		LightSensor lightSensor = LightSensor.BH1750FVI_PI;
		ScreenType screenType = ScreenType.PI_TOUCH_SCREEN;
		
	for(int i = 1 ; i < 255 ; i++) {
	System.out.println("i: " + i + " Level: " + map(i, 1, lightSensor.getLuxMaxValue(), screenType.getMinBacklight(), screenType.getMaxBacklight())) ;
	}
	}

	/**
	 * value: the number to map.
		fromLow: the lower bound of the value’s current range.
		fromHigh: the upper bound of the value’s current range.
		toLow: the lower bound of the value’s target range.
		toHigh: the upper bound of the value’s target range.
	 * @param x
	 * @param in_min
	 * @param in_max
	 * @param out_min
	 * @param out_max
	 * @return
	 */
	private static long map(long x, long in_min, long in_max, long out_min, long out_max) {
		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}

}
