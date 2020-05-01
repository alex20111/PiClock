package net.piclock.handlers;

import java.io.IOException;
import java.util.logging.Logger;

import com.pi4j.io.gpio.exception.UnsupportedBoardType;
import com.pi4j.wiringpi.SoftPwm;

import net.piclock.arduino.ArduinoSerialCmd;
import net.piclock.arduino.ButtonChangeListener;
import net.piclock.bean.LightLevel;
import net.piclock.enums.HardwareType;
import net.piclock.enums.ScreenType;
import net.piclock.main.Constants;
import net.piclock.main.HardwareConfig;
import net.piclock.swing.component.SwingContext;

public class DeviceHandler {
	
	private static final Logger logger = Logger.getLogger( DeviceHandler.class.getName() );

	//handlers
	private ArduinoSerialCmd ard;
	private PiScreenHandler piScreen;  //If official pi screen installed
	private PiHardwareHandler piHwHandler; // if need to interface with the Pi Hardware(GPIOs)
	private LightSensorHandler lightHandler; // type of light sensor
	
	//context
	private SwingContext context;
	private HardwareConfig hwc;
	
	
	
	public DeviceHandler() throws UnsupportedBoardType, IOException, InterruptedException {
		context = SwingContext.getInstance();
		
		hwc = (HardwareConfig)context.getSharedObject(Constants.HARDWARE);
		
				
		if (arduinoHw()) {
			ard = new ArduinoSerialCmd();			
		}else if (piHw()) {
			piHwHandler = new PiHardwareHandler();
		}
		
		if (screenPiScreen()) {
			piScreen = new PiScreenHandler();
		}
			
		if (hwc.getLightSensor().isSensorOnPi()) {
			lightHandler = new LightSensorHandler(hwc.getLightSensor(), hwc.getScreenType());
		}
		logger.config("Hardware Info: \n\tHardware: " + hwc.getHardwareType() + "\n\tScreen selected: " + hwc.getScreenType() +".\n\tLight Sensor:  " + hwc.getLightSensor());
		
	}
	
	/**
	 * Button wired on Arduino or Pi gpio
	 * @param btn
	 */
	public void addButtonListener(ButtonChangeListener btn) {
		
		if (arduinoHw() ) {
			ard.addButtonListener(btn);
		}else if(piHw()) {
			piHwHandler.addButtonListeners(btn);
		}
		
	}
	/**
	 * Arduino controlled or Pi controlled.
	 * @param time
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	
	public void writeTime(String time) throws IllegalStateException, IOException {
		
		if (arduinoHw()) {
			ard.writeTime(time);
		}
		
		else if (piHw() && !piScreen.isClockOn()) {
			
			piScreen.clockOn();
		}
	}
	
	public LightLevel readLdr() throws IllegalStateException, IOException, InterruptedException {

		LightLevel level = null;
		if (hwc.getLightSensor().isSensorOnArduino()) {
			level = new LightLevel(ard.readLdr(), hwc.getScreenType().getMinBacklight());
		}else if (hwc.getLightSensor().isSensorOnPi()) {
					
			level =  new LightLevel(lightHandler.getVisibleLight(), hwc.getScreenType().getMinBacklight());
		}
		
		return level;
	}
	
	public void turnOffTimeScreen() throws IllegalStateException, IOException {
		if (arduinoHw()) {
			ard.timeOff();
		}else if(piHw()) {
			piScreen.clockOff();
		}
	}
	
	public void buzzerOn() throws IllegalStateException, IOException {
		if (arduinoHw()) {
			ard.buzzer(true);
		}
	}
	
	public void buzzerOff() throws IllegalStateException, IOException {
		if (arduinoHw()) {
			ard.buzzer(false);
		}
	}
	
	public void turnOnRadio() throws IllegalStateException, IOException {
		if (arduinoHw()) {
			ard.turnOnRadio();
		}else if(piHw()) {
			piScreen.radioOn();
		}
	}

	public void turnOffRadio() throws IllegalStateException, IOException {
		if (arduinoHw()) {
			ard.turnOffRadio();
		}else if(piHw()) {
			piScreen.radioOff();
		}
	}
	
	public void selectRadioChannel(int channel) throws IllegalStateException, IOException {
		
		if (arduinoHw()) {
			ard.radioSelectChannel(channel);
		}else if (piHw()) {
			float ch =(float)channel / 10;
			piScreen.setFmStation(ch);
		}
	}
	
	public void turnSpeakerOn() throws IllegalStateException, IOException {
		if (arduinoHw()) {
			ard.turnSpeakerOn();
		}
	}
	public void turnSpeakerOff() throws IllegalStateException, IOException {
		if (arduinoHw()) {
			ard.turnSpeakerOff();
		}
	}
	
	public void setScreenBrightness(int level) {
		if (screenHyperPixel40()) {
			SoftPwm.softPwmWrite(24, level);
		}else if (screenPiScreen()) {
			piScreen.setScreenBrightness(level);
		}
	}
	
	
	private boolean screenHyperPixel40() {		
		return hwc.getScreenType() == ScreenType.HYPERPIXEL40;
	}
	
	private boolean screenPiScreen() {
		return hwc.getScreenType() == ScreenType.PI_TOUCH_SCREEN;
	}
	
	private boolean arduinoHw() {
		return hwc.getHardwareType() == HardwareType.ARDUINO;
	}
	
	private boolean piHw() {
		return hwc.getHardwareType() == HardwareType.PI;
	}
	

	
}
