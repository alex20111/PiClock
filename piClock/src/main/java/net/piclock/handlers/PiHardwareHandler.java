package net.piclock.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ButtonState;

public class PiHardwareHandler {

	
	private List<ButtonChangeListener> btnListeners = new ArrayList<ButtonChangeListener>();	


	
	public PiHardwareHandler() {
		
		final GpioController gpio = GpioFactory.getInstance();
		// provision gpio pin #02 as an input pin with its internal pull down resistor enabled
		final GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);

		myButton.setDebounce(100);

		// create and register gpio pin listener
		myButton.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				ButtonState state = event.getState().isHigh() ? ButtonState.HIGH : ButtonState.LOW;
				fireBtnChangeEvent(state);
			}
		});
	}
	
	public void addButtonListeners(ButtonChangeListener btn){
		btnListeners.add(btn);
	}
	
	
	private synchronized void fireBtnChangeEvent(ButtonState buttonState) {

		Iterator<ButtonChangeListener> listeners = btnListeners.iterator();
		while( listeners.hasNext() ) {
			ButtonChangeListener bl =  (ButtonChangeListener) listeners.next();
			bl.stateChanged( buttonState );
		}
	}
	
}
