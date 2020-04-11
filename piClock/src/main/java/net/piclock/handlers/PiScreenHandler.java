package net.piclock.handlers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.exec.ExecuteException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import home.misc.Exec;
import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ButtonState;
import net.piclock.enums.ScreenType;
import net.piclock.nativeImpl.SI4703;
import net.piclock.nativeImpl.TSL2591;
import net.piclock.nativeImpl.Tm1637;

public class PiScreenHandler {

	private static final Logger logger = Logger.getLogger( PiScreenHandler.class.getName() );

	private TSL2591 tsl2591;
	private Tm1637 tm1637;
	private SI4703 si4703;

	private int luxHighestValue = 200; //highest value for LUX..
	private boolean screenOn = true;
	private boolean clockOn = false;

	private List<ButtonChangeListener> btnListeners = new ArrayList<ButtonChangeListener>();	

	private Thread clockThread;
	
	public PiScreenHandler(){

		init();
	}


	public void setScreenBrightness(int level) {
//		logger.log(Level.CONFIG, "In setScreenBrightness. Level: " + level + " ScreenOn: " + screenOn);

		try {
			String brightness = "/sys/class/backlight/rpi_backlight/brightness";
			String onOff      = "/sys/class/backlight/rpi_backlight/bl_power";
			if (level > 0 ){
				if(!screenOn){
					writeToScreenFile(0, onOff);
					screenOn = true;
				}
				writeToScreenFile(level, brightness);
			}else if (screenOn && level == 0){

				writeToScreenFile(1, onOff);
				screenOn = false;
			}
		}catch (Exception e) {
			logger.log(Level.SEVERE,"error ", e);
		}
	}

	/**
	 * get lux an map it from 5 to 255
	 * @return
	 * @throws ExecuteException
	 * @throws IOException
	 */
	public int getVisibleLight() throws ExecuteException, IOException {		

		int level = 0;
		float luxFloat = tsl2591.getLux();
		int lux = (int) luxFloat;
		
//		if (lux > luxHighestValue){//TODO  re-assessed max value once a week
//			luxHighestValue = lux;
//		}

		//lux of 3 is light in the room
		//15 to 177 is the backlight control values. 15 being the lowest and 177 highest brightness.
		if (lux >  ScreenType.PI_TOUCH_SCREEN.getLowestBrightness()){
			//the LDR will return 
			long resultLux = map(lux, 0, luxHighestValue, ScreenType.PI_TOUCH_SCREEN.getMinBacklight(), ScreenType.PI_TOUCH_SCREEN.getMaxBacklight());

			if (resultLux == ScreenType.PI_TOUCH_SCREEN.getMaxBacklight()){
				resultLux = 255;
			}
			
			level = (int) resultLux;
		}
		
		logger.config("LUX Float value: " + luxFloat + " LUX int: " + lux + " level: " + level + " Highest Lux: " + luxHighestValue);
		
		return level;
	}

	public void writeTime(String time) {
		logger.log(Level.CONFIG, "Sending time: " + time);
		int hours = Integer.parseInt(time.substring(0, 2));
		int minutes = Integer.parseInt(time.substring(3, time.length()));
		tm1637.initProgram();
		//0 = 12hrs format, 1=24 hours format
		tm1637.displayTime(hours, minutes, 1);

	}
	public void clockOn() {
		logger.log(Level.CONFIG, "clockOn() -> " + clockOn);

		if (clockThread == null || !clockThread.isAlive()) {
			clockThread = new Thread(new Runnable() {

				@Override
				public void run() {
					logger.log(Level.CONFIG, "Starting clock thread");
					int ret = 99;
					

						try {
							Exec exec = new Exec();

							exec.addCommand("sudo");
							exec.addCommand("./scripts/clock.sh");
							clockOn = true;
							ret = exec.run();

						}catch(Exception ex) {
							logger.log(Level.INFO, "Exception in clockThread", ex);
							clockOn = false;
						}

				
					logger.log(Level.CONFIG, "Clock thread finished. " + ret);
				}
			});
			
			clockThread.start();
		}else {
			clockOn = true;
		}
		//		tm1637.initProgram();
		//		tm1637.setBrightness(3);
	}
	public void clockOff() {
		logger.config("Clock off(). is Clock thread alive: " + (clockThread != null ? clockThread.isAlive() : "False"));
		
		
		if (clockThread != null && clockThread.isAlive()) {
			
			try {
				//1st kill the process
				Exec exec = new Exec();

				exec.addCommand("sudo");
				exec.addCommand("./scripts/killClock.sh");
				clockOn = true;
				int ret = exec.run();

//				clockThread.interrupt();
				clockOn = false;
				logger.config("END CLOCK OFF. -> " + exec.getOutput());
			}catch(Exception ex) {
				logger.log(Level.SEVERE, "Exception clock off", ex);
			}
		}
//		tm1637.initProgram();
//		tm1637.displayPoint(false);
//		tm1637.clearDisplay();	
	}
	public void radioOn(){
		int stat = si4703.powerOn();
		si4703.setVolume(4);
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
	private void writeToScreenFile(int level, String filePath) throws IOException{
		RandomAccessFile f;

		f = new RandomAccessFile(new File(filePath), "rw");
		f.seek(0); // to the beginning
		f.write(String.valueOf(level).getBytes());
		f.close();

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


	public boolean isClockOn() {
		return clockOn;
	}
}
