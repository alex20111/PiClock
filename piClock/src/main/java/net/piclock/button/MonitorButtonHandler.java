package net.piclock.button;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ButtonState;
import net.piclock.enums.Light;
import net.piclock.main.PiHandler;

public class MonitorButtonHandler implements ButtonChangeListener {

	private static final Logger logger = Logger.getLogger( MonitorButtonHandler.class.getName() );
	
	private boolean active = false;
	private PiHandler piHandler;
	
	
	@Override
	public void stateChanged(ButtonState state) {
		piHandler = PiHandler.getInstance();
		
		logger.log(Level.CONFIG, "Screen on : " + piHandler.isScreenOn() + "  button state: " + state);
		
		if (!piHandler.isScreenOn() && state == ButtonState.HIGH) {
			piHandler.setBrightness(Light.VERY_DIM);
			try {
				piHandler.autoShutDownScreen();
			} catch (InterruptedException e) {
			}
		}
		
		
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void setListenerActive() {
		this.active = true;
		
	}

	@Override
	public void deactivateListener() {
		this.active = false;
		
	}

}
