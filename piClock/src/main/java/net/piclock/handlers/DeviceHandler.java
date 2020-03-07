package net.piclock.handlers;

import java.io.IOException;
import java.util.logging.Logger;

import com.pi4j.io.gpio.exception.UnsupportedBoardType;
import com.pi4j.wiringpi.SoftPwm;

import net.piclock.arduino.ArduinoSerialCmd;
import net.piclock.arduino.ButtonChangeListener;
import net.piclock.bean.LightLevel;
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
			piScreen.addButtonListeners(btn);
		}
		
	}
	public void writeTime(String time) throws IllegalStateException, IOException {
		
		if (deviceHyperPixel40()) {
			ard.writeTime(time);
		}else if (devicePiScreen()) {
			piScreen.writeTime(time);
		}
	}
	
	public LightLevel readLdr() throws IllegalStateException, IOException, InterruptedException {

		LightLevel level = null;
		if (deviceHyperPixel40()) {
			level = new LightLevel(ard.readLdr(), ScreenType.HYPERPIXEL40.getMinBacklight());
		}else if (devicePiScreen()) {
					
			level =  new LightLevel(piScreen.getVisibleLight(), ScreenType.PI_TOUCH_SCREEN.getMinBacklight());
		}
		
		return level;
	}
	
	public void turnOffTimeScreen() throws IllegalStateException, IOException {
		if (deviceHyperPixel40()) {
			ard.timeOff();
		}else if(devicePiScreen()) {
			piScreen.clockOff();
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
		}else if(devicePiScreen()) {
			piScreen.radioOn();
		}
	}

	public void turnOffRadio() throws IllegalStateException, IOException {
		if (deviceHyperPixel40()) {
			ard.turnOffRadio();
		}else if(devicePiScreen()) {
			piScreen.radioOff();
		}
	}
	
	public void selectRadioChannel(int channel) throws IllegalStateException, IOException {
		
		if (deviceHyperPixel40()) {
			ard.radioSelectChannel(channel);
		}else if (devicePiScreen()) {
			float ch =(float)channel / 10;
			piScreen.setFmStation(ch);
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
	
	public void setScreenBrightness(int level) {
		if (deviceHyperPixel40()) {
			SoftPwm.softPwmWrite(24, level);
		}else if (devicePiScreen()) {
			piScreen.setScreenBrightness(level);
		}
	}
	
	
	private boolean deviceHyperPixel40() {
		return device == ScreenType.HYPERPIXEL40;
	}
	
	private boolean devicePiScreen() {
		return device == ScreenType.PI_TOUCH_SCREEN;
	}
	
	
}
