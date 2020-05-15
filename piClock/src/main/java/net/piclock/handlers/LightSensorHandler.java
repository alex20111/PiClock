package net.piclock.handlers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.exec.ExecuteException;

import com.pi4j.io.i2c.I2CBus;

import net.piclock.enums.LightSensor;
import net.piclock.enums.ScreenType;
import net.piclock.nativeImpl.BH1750FVI;
import net.piclock.nativeImpl.TSL2591;

public class LightSensorHandler {

	private static final Logger logger = Logger.getLogger( LightSensorHandler.class.getName() );


	
	private TSL2591 tsl2591;
	private BH1750FVI bh1750;
	private ScreenType screenType;
	private LightSensor lightSensor;

	public LightSensorHandler(LightSensor sensor, ScreenType screenType, I2CBus i2cBus) {
		if (sensor == LightSensor.TSL2591_PI) {
			//init the LUX sensor
			tsl2591 = new TSL2591();
			tsl2591.init(0x29);

		}else if (sensor == LightSensor.BH1750FVI_PI) {
			bh1750 = new BH1750FVI(i2cBus, BH1750FVI.I2C_ADDRESS_23);
		}

		lightSensor = sensor;
		this.screenType = screenType;
	}


	public int getVisibleLight() {

		int level = 0;
		int luxHighestValue = 200;
		float luxFloat = 0.0f;


		if (lightSensor == LightSensor.TSL2591_PI) {
			luxFloat =  getTsl2591VisibleLight();
		}else if (lightSensor == LightSensor.BH1750FVI_PI) {
			luxFloat = getBH1750Lux();
		}


		int lux = (int) luxFloat;

		if (lux > lightSensor.getLuxMaxValue()){//TODO  re-assessed max value once a week
			lux = screenType.getMaxBacklight();
		}

		//lux of 3 is light in the room
		//15 to 177 is the backlight control values. 15 being the lowest and 177 highest brightness.
		if (lux >  lightSensor.getDarkThreshold()){
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
	
	/**
	 * get lux an map it from 5 to 255
	 * @return
	 * @throws ExecuteException
	 * @throws IOException
	 */
	private float getTsl2591VisibleLight()  {		
		return tsl2591.getLux();
	}

	private float getBH1750Lux() {

		float value = -1;
		try {
			value = bh1750.getOptical();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error getting BH1750 lux. " , e);
		}	

		return value;
	}
	private long map(long x, long in_min, long in_max, long out_min, long out_max) {
		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}





}
