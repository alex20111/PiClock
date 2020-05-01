package net.piclock.button;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ButtonState;
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

			if (!piHandler.isScreenOn()  ) {
				try {

					HardwareConfig hw = (HardwareConfig)SwingContext.getInstance().getSharedObject(Constants.HARDWARE);
					
					ScreenType type = hw.getScreenType();
					
					piHandler.turnOnScreen(true, type.getMinBacklight());  
					piHandler.autoShutDownScreen(20000);
				} catch (InterruptedException | IOException e) {
					logger.log(Level.CONFIG, "in button monitor");
				}
			}

		}


	}

}
