package net.piclock.main;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class deciding what screen environment we are running.
 * this support HyperPixel and Raspberry pi 7 inch
 * @author ADMIN
 *
 */
public class ScreenEnv {
	
	private static final Logger logger = Logger.getLogger( ScreenEnv.class.getName() );

	private static ScreenEnv screenEnv = null;
	
//	private ScreenType  screenType;
//	
//	
//	public static ScreenEnv getInstance() {
//		if (screenEnv == null) {
//			synchronized (ScreenEnv.class) {
//				if(screenEnv == null) {
//					logger.log(Level.INFO, "PiHandler initialized");
//					screenEnv = new ScreenEnv();
//				}
//			}
//		}
//		return screenEnv;
//	}
//	
//	
//	public void setScreenType(ScreenType type) {
//		this.screenType = type;
//	}
//	
//	
//	public boolean isHyperPixel() {
//		return screenType == ScreenType.HYPERPIXEL40;
//	}
//	
//	public boolean isPiScreen() {
//		return screenType == ScreenType.PI_TOUCH_SCREEN;
//	}
	

}
