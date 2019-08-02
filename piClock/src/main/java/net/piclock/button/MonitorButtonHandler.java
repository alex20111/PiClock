package net.piclock.button;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ButtonState;
import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.enums.Light;
import net.piclock.main.Constants;
import net.piclock.main.PiHandler;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.FormatStackTrace;

public class MonitorButtonHandler implements ButtonChangeListener {

	private static final Logger logger = Logger.getLogger( MonitorButtonHandler.class.getName() );
	
	private PiHandler piHandler;
		
	@Override
	public void stateChanged(ButtonState state) {
		piHandler = PiHandler.getInstance();

		logger.log(Level.CONFIG, "Screen on : " + piHandler.isScreenOn() + " WIFI on? " + piHandler.isWifiOn() + "  Button state: " + state);

		try {
			
			if(state == ButtonState.HIGH) {
				
				if (!piHandler.isScreenOn()  ) {
					try {

						piHandler.setBrightness(Light.VERY_DIM);
						piHandler.autoShutDownScreen(20000);
					} catch (InterruptedException e) {
						logger.log(Level.CONFIG, "Interrupted in button monitor");
					}
				}
				
				if(!piHandler.isWifiOn()) {
					try {
						piHandler.turnWifiOn();
						piHandler.autoWifiShutDown(true);
					} catch (InterruptedException e) {
						logger.log(Level.CONFIG, "Interrupted in button monitor WIFI");
					}
				}
			}
		}catch(IOException ex) {
			ErrorHandler eh = (ErrorHandler)SwingContext.getInstance().getSharedObject(Constants.ERROR_HANDLER);
				eh.addError(ErrorType.GENERAL, new ErrorInfo(new FormatStackTrace(ex).getFormattedException()));
			logger.log(Level.SEVERE, "Problem with monitorbutton", ex);
		} 

	}

}
