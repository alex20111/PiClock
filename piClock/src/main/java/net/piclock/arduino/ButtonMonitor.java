package net.piclock.arduino;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class ButtonMonitor implements Runnable{

	private static final Logger logger = Logger.getLogger( ButtonMonitor.class.getName() );

	private List<ButtonChangeListener> btnListeners = new ArrayList<ButtonChangeListener>();
	private ButtonState buttonState;

	private final AtomicBoolean running = new AtomicBoolean(false);

	private Thread monitor;
	private ArduinoCmd ard;
	private long delayInMillis;

	public ButtonMonitor(List<ButtonChangeListener> listeners, long delayInMillis) throws UnsupportedBusNumberException, IOException{
		this.btnListeners = listeners;
		ard = ArduinoCmd.getInstance();
		this.delayInMillis =  delayInMillis;
	}	 
	@Override
	public void run() {
		int prevBtnStatus = 0;
		running.set(true);
		try {
			while (running.get()) {

				try {

					int state = ard.readButtonA();
					if (state != prevBtnStatus){
						prevBtnStatus = state;
						buttonState = (state > 0 ? ButtonState.HIGH : ButtonState.LOW);
						fireBtnChangeEvent();	
					}

					Thread.sleep(delayInMillis);
					forceStop(); //verify if we need to force stop the thread

				} catch (InterruptedException e) {
					logger.log(Level.CONFIG, "Btn monitor Interrupted");
					running.set(false);
					Thread.currentThread().interrupt();
				}
			}
		}catch ( IOException e) {
			logger.log(Level.CONFIG, "Error in Button Monitor.", e);
			running.set(false);

		}
		logger.log(Level.CONFIG, "end btn monitor run");
	}
	private synchronized void fireBtnChangeEvent() {

		Iterator<ButtonChangeListener> listeners = btnListeners.iterator();
		while( listeners.hasNext() ) {
			ButtonChangeListener bl =  (ButtonChangeListener) listeners.next();
			if (bl.isActive()) {
				bl.stateChanged( buttonState );
			}
		}
	}	

	public void start(){
		running.set(true);
		monitor = new Thread(this);
		monitor.start();
	}
	public void stop(){
		logger.log(Level.CONFIG, "Stopping btn monitor. Active: " + monitor.isAlive());

		if (monitor != null ){
			running.set(false);
			monitor.interrupt();
			logger.log(Level.CONFIG, "Btn monitor stopped");
		}
		logger.log(Level.CONFIG, "End of stop");
	}
	public boolean isRunning() {
		if(monitor != null && monitor.isAlive()) {
			return true;
		}
		return false;
	}

	private void forceStop() {
		boolean noActiveListeners = true;
		Iterator<ButtonChangeListener> listeners = btnListeners.iterator();
		while(listeners.hasNext()) {
			ButtonChangeListener bl =  (ButtonChangeListener) listeners.next();
			if (bl.isActive()) {
				noActiveListeners = false;
				break;
			}

		}

		if (noActiveListeners) {
			logger.log(Level.CONFIG, "Forced stopped BtnMonitor");
			stop();
		}

	}
}
