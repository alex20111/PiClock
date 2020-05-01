package net.piclock.handlers;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.exec.ExecuteException;

import net.piclock.enums.LightSensor;
import net.piclock.enums.ScreenType;
import net.piclock.nativeImpl.TSL2591;

public class LightSensorHandler {

	private static final Logger logger = Logger.getLogger( LightSensorHandler.class.getName() );


	private TSL2591 tsl2591;
	private ScreenType screenType;
	private LightSensor lightSensor;

	public LightSensorHandler(LightSensor sensor, ScreenType screenType) {
		if (sensor == LightSensor.TSL2591_PI) {
			//init the LUX sensor
			tsl2591 = new TSL2591();
			tsl2591.init(0x29);

		}

		lightSensor = sensor;
		this.screenType = screenType;
	}


	public int getVisibleLight() {
		if (lightSensor == LightSensor.TSL2591_PI) {
			return getTsl2591VisibleLight();
		}

		return -1;
	}
	/**
	 * get lux an map it from 5 to 255
	 * @return
	 * @throws ExecuteException
	 * @throws IOException
	 */
	private int getTsl2591VisibleLight()  {		

		int level = 0;
		int luxHighestValue = 200;
		float luxFloat = tsl2591.getLux();
		int lux = (int) luxFloat;

		//		if (lux > 200){//TODO  re-assessed max value once a week
		//			lux = luxHighestValue;
		//		}

		//lux of 3 is light in the room
		//15 to 177 is the backlight control values. 15 being the lowest and 177 highest brightness.
		if (lux >  ScreenType.PI_TOUCH_SCREEN.getLowestBrightness()){
			//the LDR will return 
			long resultLux = map(lux, 0, luxHighestValue, screenType.getMinBacklight(), screenType.getMaxBacklight()) ;

			if (resultLux == screenType.getMaxBacklight()){
				resultLux = 255;
			}

			level = (int) resultLux;
		}

		logger.config("LUX Float value: " + luxFloat + " LUX int: " + lux + " level: " + level + " Highest Lux: " + luxHighestValue);

		return level;
	}

	private long map(long x, long in_min, long in_max, long out_min, long out_max) {
		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}





}
