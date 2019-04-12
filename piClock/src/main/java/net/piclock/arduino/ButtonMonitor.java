package net.piclock.arduino;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class ButtonMonitor implements Runnable{
	private List<ButtonChangeListener> btnListeners = new ArrayList<ButtonChangeListener>();
	 private ButtonState buttonState;
	 
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
		try {
			while (true) {


				int state = ard.readButtonA();
				if (state != prevBtnStatus){
					prevBtnStatus = state;
					buttonState = (state > 0 ? ButtonState.HIGH : ButtonState.LOW);
					fireBtnChangeEvent();	
				}

				
				Thread.sleep(delayInMillis);
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}
	private synchronized void fireBtnChangeEvent() {

       Iterator<ButtonChangeListener> listeners = btnListeners.iterator();
       while( listeners.hasNext() ) {
           ( (ButtonChangeListener) listeners.next() ).stateChanged( buttonState );
       }
   }	
	
	public void start(){
		monitor = new Thread(this);
		monitor.start();
	}
	public void stop() throws InterruptedException{//add interrupt logic
		if (monitor != null && monitor.isAlive()){
			monitor.interrupt(); 
			while(monitor.isAlive()){ 
				monitor.join(100); 
			} 
		}
	}
}
