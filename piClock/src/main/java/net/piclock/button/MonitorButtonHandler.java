package net.piclock.button;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ButtonState;
import net.piclock.enums.Light;
import net.piclock.main.PiHandler;

public class MonitorButtonHandler implements ButtonChangeListener {

	private static final Logger logger = Logger.getLogger( MonitorButtonHandler.class.getName() );
	
	private PiHandler piHandler;
		
	@Override
	public void stateChanged(ButtonState state) {
		piHandler = PiHandler.getInstance();

		logger.log(Level.CONFIG, "Screen on : " + piHandler.isScreenOn() + "  button state: " + state);

		try {
			if (!piHandler.isScreenOn() && state == ButtonState.HIGH) {
				try {

				piHandler.setBrightness(Light.VERY_DIM);
				piHandler.turnWifiOn();
				
					piHandler.autoShutDownScreen();
				} catch (InterruptedException e) {
					logger.log(Level.CONFIG, "Interrupted in button monitor");
				}
			}
		}catch(IOException ex) {
			logger.log(Level.SEVERE, "Problem with monitorbutton", ex);
		} 

	}

}
