package net.piclock.thread;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.bean.LightLevel;
import net.piclock.enums.LDRCycle;
import net.piclock.handlers.PiHandler;
import net.piclock.main.Constants;
import net.piclock.main.Preferences;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.FormatStackTrace;

public class LDRStatusWorker implements Runnable{

	private static final Logger logger = Logger.getLogger( LDRStatusWorker.class.getName() );

	private SwingContext ct = SwingContext.getInstance();
	private LDRCycle lastCycleStatus = LDRCycle.NOT_DEFINED;
	private LDRCycle cycle = LDRCycle.NOT_DEFINED;

	private LightLevel lastLightStatus = new LightLevel(0, 13);

	PiHandler handler;

	public LDRStatusWorker() throws IllegalStateException, IOException, InterruptedException {
		handler = PiHandler.getInstance();

		LightLevel currLight = handler.getLDRstatus();

		cycle = (currLight.isDark() ? LDRCycle.DARK : LDRCycle.LIGHT); 
	}

	@Override
	public void run() {
		try{			
			//call it every 10 seconds
			Preferences p = (Preferences)ct.getSharedObject(Constants.PREFERENCES);

			LightLevel lightStatus = handler.getLDRstatus();

			logger.log(Level.CONFIG, "LDR cycle: " + cycle + " lastCycleStatus: " + lastCycleStatus + 
					" lightStatus: "+ lightStatus + " lastLightStatus: " + lastLightStatus +" AutoOffScreen Option: " + p.isAutoOffScreen());

			if (!lastLightStatus.status().equals(lightStatus.status()) ) {

				//adjust LCD based on the LDR.					
				lastLightStatus.setStatus(lightStatus.status());
				if (lightStatus.isDark()) {
					handler.setBrightness(lightStatus.getScreenDimMode()); //we don't want to turn off screen here
					cycle = LDRCycle.DARK;
				}else {
					handler.setBrightness(lightStatus.getLdrValue());
					cycle = LDRCycle.LIGHT;
				}
			}

			if (cycle == LDRCycle.DARK ){

				if (cycle != lastCycleStatus) {
					lastCycleStatus = LDRCycle.valueOf(cycle.name());
				}


				//turn off screeen if screen is on.
				if(p.isAutoOffScreen() && handler.isScreenOn() && !handler.isAutoShutdownInProgress()){
					handler.turnOffScreen();
					handler.displayTM1637Time(new SimpleDateFormat(Constants.HOUR_MIN).format(new Date()));
				}else if (!p.isAutoOffScreen() && !handler.isScreenOn()){
					handler.turnOnScreen(false, lightStatus.getScreenDimMode()); //basd on the light surrounding.. it will be at the loest
					handler.turnOffTM1637Time();
				}

				//if we need to shutdown wifi and it is night 
				if (p.isWifiOff() && handler.isWifiOn() && !handler.isWifiAutoShutdownInProgress()) {
					logger.log(Level.CONFIG, "Fire wifi auto shudown from LDR Worker");
					handler.autoWifiShutDown(true);
				}else if (!p.isWifiOff() && !handler.isWifiOn()){
					logger.log(Level.CONFIG, "Turn on wifi because it was off at night");
					handler.turnWifiOn();					
				}				


			}else if (cycle == LDRCycle.LIGHT ){

				if (cycle != lastCycleStatus) {
					lastCycleStatus = LDRCycle.valueOf(cycle.name());
				}

				if (!handler.isScreenOn()) {
					handler.turnOnScreen(false, lightStatus.getLdrValue());
					handler.turnOffTM1637Time();
				}

				if (!handler.isWifiOn() || handler.isWifiAutoShutdownInProgress()) {//does not matter if the option to turn off wifi is ON, when day and wifi dowon, turn it on. Since we only turn off wifi at night
					handler.turnWifiOn();
				}

				handler.setBrightness(lightStatus.getLdrValue());
			}

		}catch(Throwable tr){
			ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
			eh.addError(ErrorType.LDR, new ErrorInfo(new FormatStackTrace(tr).getFormattedException()));
			logger.log(Level.SEVERE,"Error in ldr",tr);
		}
	}

}
