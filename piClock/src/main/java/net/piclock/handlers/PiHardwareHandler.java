package net.piclock.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ButtonState;

public class PiHardwareHandler {
	
	private static final Logger logger = Logger.getLogger( PiHardwareHandler.class.getName() );

	
	private List<ButtonChangeListener> btnListeners = new ArrayList<ButtonChangeListener>();	

	private GpioPinDigitalOutput speakerSwitch;
	
	public PiHardwareHandler() {
		
		final GpioController gpio = GpioFactory.getInstance();
		// provision gpio pin #02 as an input pin with its internal pull down resistor enabled
		final GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);
		speakerSwitch = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, PinState.LOW);

	
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
	
	public void speakerOn() {
//		GpioFactory.getInstance().provisionDigitalOutputPin(RaspiPin.GPIO_25);
		
		logger.config("Speaker ON Before. SpeakerState state:  " + speakerSwitch.getState());
//		if (speakerSwitch.isLow()) {
			speakerSwitch.high();
//		}
			logger.config("Speaker ON After. SpeakerState state:  " + speakerSwitch.getState());
	}
	public void speakerOff() {
		logger.config("Speaker OFF Before. SpeakerState state? " + speakerSwitch.getState() );
//		if (speakerSwitch.isHigh()) {
			speakerSwitch.low();
//		}
			logger.config("Speaker OFF After. SpeakerState state? " + speakerSwitch.getState() );
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
