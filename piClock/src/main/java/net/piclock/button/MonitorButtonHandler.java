package net.piclock.button;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ButtonState;
import net.piclock.enums.Light;
import net.piclock.handlers.PiHandler;

public class MonitorButtonHandler implements ButtonChangeListener {

	private static final Logger logger = Logger.getLogger( MonitorButtonHandler.class.getName() );

	private PiHandler piHandler;

	@Override
	public void stateChanged(ButtonState state) {
		piHandler = PiHandler.getInstance();

		logger.log(Level.CONFIG, "Screen on : " + piHandler.isScreenOn() + " WIFI on? " + piHandler.isWifiOn() + "  Button state: " + state);

		//		try {

		if(state == ButtonState.HIGH) {

			if (!piHandler.isScreenOn()  ) {
				try {

					piHandler.turnOnScreen(true, Light.VERY_DIM);
					//						piHandler.setBrightness(Light.VERY_DIM);
					piHandler.autoShutDownScreen(20000);
					//						piHandler.turnWifiOn();
				} catch (InterruptedException | IOException e) {
					logger.log(Level.CONFIG, "in button monitor");
				}
			}

		}


	}

}
