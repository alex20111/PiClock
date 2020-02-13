package net.piclock.handlers;

import java.io.IOException;
import java.util.logging.Logger;

import com.pi4j.io.gpio.exception.UnsupportedBoardType;
import com.pi4j.wiringpi.SoftPwm;

import net.piclock.arduino.ArduinoSerialCmd;
import net.piclock.arduino.ButtonChangeListener;
import net.piclock.enums.Light;
import net.piclock.enums.ScreenType;
import net.piclock.main.Constants;
import net.piclock.swing.component.SwingContext;
import net.piclock.main.Preferences;

public class DeviceHandler {
	
	private static final Logger logger = Logger.getLogger( DeviceHandler.class.getName() );

	private ArduinoSerialCmd ard;
	private PiScreenHandler piScreen;
	private SwingContext context;
	private ScreenType device;
	
	
	public DeviceHandler() throws UnsupportedBoardType, IOException, InterruptedException {
		context = SwingContext.getInstance();
		
		device = ScreenType.valueOf(((Preferences)context.getSharedObject(Constants.PREFERENCES)).getScreenType());
		
		if (deviceHyperPixel40()) {
			ard = new ArduinoSerialCmd();
		}else if (device == ScreenType.PI_TOUCH_SCREEN) {
			piScreen = new PiScreenHandler();
		}
		
		logger.config("Screen selected: " + device +".  ");
		
	}
	
	
	public void addButtonListener(ButtonChangeListener btn) {
		
		if (deviceHyperPixel40() ) {
			ard.addButtonListener(btn);
		}else if(devicePiScreen()) {
			//GPIO listener
		}
		
	}
	public void writeTime(String time) throws IllegalStateException, IOException {
		
		if (deviceHyperPixel40()) {
			ard.writeTime(time);
		}else if (devicePiScreen()) {
			//TODO , maybe in loop, but must find a way to turn off program
		}
	}
	
	public int readLdr() throws IllegalStateException, IOException, InterruptedException {
		int ldrValue =  -1; 
		
		if (deviceHyperPixel40()) {
			ldrValue =  ard.readLdr();
		}else if (devicePiScreen()) {
			ldrValue = piScreen.getVisibleLight();
		}
		
		return ldrValue;
	}
	
	public void turnOffTimeScreen() throws IllegalStateException, IOException {
		if (deviceHyperPixel40()) {
			ard.timeOff();
		}else if(devicePiScreen()) {
			piScreen.time(true);
		}
	}
	
	public void buzzerOn() throws IllegalStateException, IOException {
		if (deviceHyperPixel40()) {
			ard.buzzer(true);
		}
	}
	
	public void buzzerOff() throws IllegalStateException, IOException {
		if (deviceHyperPixel40()) {
			ard.buzzer(false);
		}
	}
	
	public void turnOnRadio() throws IllegalStateException, IOException {
		if (deviceHyperPixel40()) {
			ard.turnOnRadio();
		}
	}

	public void turnOffRadio() throws IllegalStateException, IOException {
		if (deviceHyperPixel40()) {
			ard.turnOffRadio();
		}
	}
	
	public void selectRadioChannel(int channel) throws IllegalStateException, IOException {
		if (deviceHyperPixel40()) {
			ard.radioSelectChannel(channel);
		}
	}
	
	public void turnSpeakerOn() throws IllegalStateException, IOException {
		if (deviceHyperPixel40()) {
			ard.turnSpeakerOn();
		}
	}
	public void turnSpeakerOff() throws IllegalStateException, IOException {
		if (deviceHyperPixel40()) {
			ard.turnSpeakerOff();
		}
	}
	
	public void setScreenBrightness(Light light) {
		if (deviceHyperPixel40()) {
			SoftPwm.softPwmWrite(24, light.getPwmLevel());
		}else if (devicePiScreen()) {
			piScreen.setScreenBrightness(light);
		}
	}
	
	
	private boolean deviceHyperPixel40() {
		return device == ScreenType.HYPERPIXEL40;
	}
	
	private boolean devicePiScreen() {
		return device == ScreenType.PI_TOUCH_SCREEN;
	}
	
	
}
