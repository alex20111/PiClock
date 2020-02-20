package net.piclock.handlers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.exec.ExecuteException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ButtonState;
import net.piclock.enums.Light;
import net.piclock.nativeImpl.SI4703;
import net.piclock.nativeImpl.TSL2591;
import net.piclock.nativeImpl.Tm1637;

public class PiScreenHandler {
	
	private static final Logger logger = Logger.getLogger( PiScreenHandler.class.getName() );

	private TSL2591 tsl2591;
	private Tm1637 tm1637;
	private SI4703 si4703;

	private List<ButtonChangeListener> btnListeners = new ArrayList<ButtonChangeListener>();	

	public PiScreenHandler(){

		init();
	}


	public void setScreenBrightness(int level) {


		RandomAccessFile f;
		try {
			f = new RandomAccessFile(new File("/sys/class/backlight/rpi_backlight/brightness"), "rw");

			f.seek(0); // to the beginning
			f.write(String.valueOf(level).getBytes());
			f.close();
		} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

/**
 * get lux an map it from 5 to 255
 * @return
 * @throws ExecuteException
 * @throws IOException
 */
	public int getVisibleLight() throws ExecuteException, IOException {		
//TODO add calibration? 
		int lux = (int)tsl2591.getLux();
		
		long resultLux = map(lux, 0, 1000, 3, 255);
		
		logger.config("LUX value: " + lux);
		
		return (int)resultLux;
	}

	public void writeTime(String time) {
		int hours = Integer.parseInt(time.substring(0, 2));
	        int minutes = Integer.parseInt(time.substring(3, time.length()));
		//0 = 12hrs format, 1=24 hours format
		tm1637.displayTime(hours, minutes, 1);
		
	}
	public void clockOn() {
		tm1637.setBrightness(3);
	}
	public void clockOff() {
		tm1637.displayPoint(false);
		tm1637.clearDisplay();	
	}
	public void radioOn(){
		int stat = si4703.powerOn();
		//TODO display error if 
	}
	public void radioOff(){
		si4703.powerOff();
	}
	public void setFmStation(float fmStation){
		si4703.setFrequency(fmStation);
	}
	public void addButtonListeners(ButtonChangeListener btn){
		btnListeners.add(btn);
	}
	private void init(){
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
		
		//init the LUX sensor
		tsl2591 = new TSL2591();
		tsl2591.init(0x29);
		
		//init TM1637
		tm1637 = new Tm1637(4,5); //pins
		
		//radio
		si4703 = new SI4703(18,0);
	}

	private synchronized void fireBtnChangeEvent(ButtonState buttonState) {

		Iterator<ButtonChangeListener> listeners = btnListeners.iterator();
		while( listeners.hasNext() ) {
			ButtonChangeListener bl =  (ButtonChangeListener) listeners.next();
			bl.stateChanged( buttonState );
		}
	}
	private long map(long x, long in_min, long in_max, long out_min, long out_max) {
		  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}
//	public static void main(String args[]) throws InterruptedException,  IOException {
//
//		System.out.println("Starting");
//		PiScreenHandler handler = new PiScreenHandler();
//
//		boolean testScreenBrightness = false;
//		boolean testLuxSensor 		 = true;
//
//
//		if (testScreenBrightness) {
//			System.out.println("Test brightness settings");
//			Thread.sleep(1000);
//			Light l = Light.DARK;
//			System.out.println("Turning Off screen");
//			handler.setScreenBrightness(l);
//			Thread.sleep(2000);
//			l = Light.VERY_BRIGHT;
//			System.out.println("Turning screen back on");
//			handler.setScreenBrightness(l);
//
//			System.out.println("test all levels");
//			for (int i = 0; i < Light.values().length ; i++) {
//				Light lvl = Light.values()[i];
//				System.out.println("Level: " + lvl);
//				handler.setScreenBrightness(lvl);
//				Thread.sleep(1000);
//			}
//			l = Light.VERY_BRIGHT;
//			System.out.println("Turning screen back on");
//			handler.setScreenBrightness(l);
//			System.out.println("End of screen brightness test");
//
//		}
//
//		if (testLuxSensor) {
//			System.out.println("Testing lux sensor");
//			//			handler.I2cScanner(I2CBus.BUS_1);
//
//			Thread.sleep(1000);
//
//			//			handler.testTSL2561(true);
//			for(int y = 0 ; y < 10; y ++) {
//				handler.getVisibleLight();
//				Thread.sleep(2000);
//			}
//
//
//		}		
//
//
//
//
//
//
//	}
}
