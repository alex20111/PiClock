package net.piclock.button;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ButtonState;
import net.piclock.arduino.ListenerNotFoundException;
import net.piclock.enums.LDRCycle;
import net.piclock.enums.ScreenType;
import net.piclock.handlers.PiHandler;
import net.piclock.main.Constants;
import net.piclock.main.HardwareConfig;
import net.piclock.swing.component.SwingContext;

public class MonitorButtonHandler implements ButtonChangeListener {

	private static final Logger logger = Logger.getLogger( MonitorButtonHandler.class.getName() );

	private PiHandler piHandler;

	@Override
	public void stateChanged(ButtonState state) {
		piHandler = PiHandler.getInstance();

		logger.log(Level.CONFIG, "Screen on : " + piHandler.isScreenOn() + " WIFI on? " + piHandler.isWifiOn() + "  Button state: " + state);

		if(state == ButtonState.HIGH) {

			SwingContext sc = SwingContext.getInstance();
			
			LDRCycle ldr = (LDRCycle)sc.getSharedObject(Constants.LDR_VALUE);
			if (!piHandler.isScreenOn()  ) {
				try {

					HardwareConfig hw = (HardwareConfig)sc.getSharedObject(Constants.HARDWARE);
					
					ScreenType type = hw.getScreenType();
					
					piHandler.turnOnScreen(true, type.getMinBacklight());  
					piHandler.autoShutDownScreen(30000);
				} catch (InterruptedException | IOException e) {
					logger.log(Level.CONFIG, "in button monitor");
				}
			}else if (ldr == LDRCycle.DARK && piHandler.isScreenOn()) {
				try {
					logger.log(Level.CONFIG, "Button turning off screen. LDR status: " + ldr);
					piHandler.turnOffScreen();
				} catch (InterruptedException | IOException | ListenerNotFoundException e) {
					logger.log(Level.CONFIG, "in button monitor");
				}
			}

		}


	}

}
