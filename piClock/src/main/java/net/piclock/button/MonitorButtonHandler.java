package net.piclock.button;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ButtonState;
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

					
					piHandler.turnOnScreen(true, 255);
					piHandler.autoShutDownScreen(20000);
				} catch (InterruptedException | IOException e) {
					logger.log(Level.CONFIG, "in button monitor");
				}
			}

		}


	}

}
