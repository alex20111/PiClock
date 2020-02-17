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
import net.piclock.nativeImpl.tsl2591.TSL2591;

public class PiScreenHandler {
	
	private static final Logger logger = Logger.getLogger( PiScreenHandler.class.getName() );

//	private String ldrFile = "/home/pi/native/TSL2561/tslReading.rdr";
//	private Path  ldrFilePath = null;
	private TSL2591 tsl2591;

	private List<ButtonChangeListener> btnListeners = new ArrayList<ButtonChangeListener>();	

	public PiScreenHandler(){

		init();
	}


	public void setScreenBrightness(Light light) {


		RandomAccessFile f;
		try {
			f = new RandomAccessFile(new File("/sys/class/backlight/rpi_backlight/brightness"), "rw");

			f.seek(0); // to the beginning
			f.write(String.valueOf(light.getPiTchScreenBrightness()).getBytes());
			f.close();
		} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public int getVisibleLight() throws ExecuteException, IOException {		

//		new Thread(new Runnable(){
//
//			@Override
//			public void run() {
//				Exec exe = new Exec();
//				exe.addCommand("./2591").addCommand(ldrFile).addCommand("2").addCommand("2");
//				try {
//					exe.run();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}		
//			}			
//		}).start();
//
//		//read from file
//		if (ldrFilePath == null){
//			ldrFilePath = Paths.get(ldrFile);
//		}		
//
//		List<String> r = Files.readAllLines(ldrFilePath);
//
//		String split[] = r.get(0).split(",");	
//
//		System.out.println("Getting light - Visible : " + split[2] );
		
		logger.config("Visible: " + tsl2591.getVisible());
		
		return 176;
	}

	public void time(boolean ON) {
		System.out.println("Setting time");
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
	}

	private synchronized void fireBtnChangeEvent(ButtonState buttonState) {

		Iterator<ButtonChangeListener> listeners = btnListeners.iterator();
		while( listeners.hasNext() ) {
			ButtonChangeListener bl =  (ButtonChangeListener) listeners.next();
			bl.stateChanged( buttonState );
		}
	}
	public static void main(String args[]) throws InterruptedException,  IOException {

		System.out.println("Starting");
		PiScreenHandler handler = new PiScreenHandler();

		boolean testScreenBrightness = false;
		boolean testLuxSensor 		 = true;


		if (testScreenBrightness) {
			System.out.println("Test brightness settings");
			Thread.sleep(1000);
			Light l = Light.DARK;
			System.out.println("Turning Off screen");
			handler.setScreenBrightness(l);
			Thread.sleep(2000);
			l = Light.VERY_BRIGHT;
			System.out.println("Turning screen back on");
			handler.setScreenBrightness(l);

			System.out.println("test all levels");
			for (int i = 0; i < Light.values().length ; i++) {
				Light lvl = Light.values()[i];
				System.out.println("Level: " + lvl);
				handler.setScreenBrightness(lvl);
				Thread.sleep(1000);
			}
			l = Light.VERY_BRIGHT;
			System.out.println("Turning screen back on");
			handler.setScreenBrightness(l);
			System.out.println("End of screen brightness test");

		}

		if (testLuxSensor) {
			System.out.println("Testing lux sensor");
			//			handler.I2cScanner(I2CBus.BUS_1);

			Thread.sleep(1000);

			//			handler.testTSL2561(true);
			for(int y = 0 ; y < 10; y ++) {
				handler.getVisibleLight();
				Thread.sleep(2000);
			}


		}		






	}
}
