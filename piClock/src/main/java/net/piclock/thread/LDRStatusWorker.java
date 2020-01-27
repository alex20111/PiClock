package net.piclock.thread;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.enums.LDRCycle;
import net.piclock.enums.Light;
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

	private Light lastLightStatus = Light.VERY_BRIGHT;

	private Map<Light, Integer> cntMap = new HashMap<Light, Integer>();
	PiHandler handler;

	public LDRStatusWorker() {
		cntMap.put(Light.DARK, 4);
		handler = PiHandler.getInstance();
		Light currLight = handler.getLDRstatus();

		cycle = (currLight == Light.DARK ? LDRCycle.DARK : LDRCycle.LIGHT); 
	}

	@Override
	public void run() {
		try{			
			//call it every 10 seconds
			Preferences p = (Preferences)ct.getSharedObject(Constants.PREFERENCES);

			Light lightStatus = handler.getLDRstatus();

			logger.log(Level.CONFIG, "LDR cycle: " + cycle + " lastCycleStatus: " + lastCycleStatus + 
					" lightStatus: "+ lightStatus + " lastLightStatus: " + lastLightStatus +" AutoOffScreen Option: " + p.isAutoOffScreen());

			if (lastLightStatus != lightStatus && lightStatus != Light.GREY_ZONE) {

				Integer cMap = cntMap.get(lightStatus);
				int cnt = (cMap == null ? 0 : cMap.intValue());

				if (cnt  >= getCnt(lightStatus)) {
					cntMap.clear();
					//adjust LCD based on the LDR.					
					lastLightStatus = lightStatus;
					if (lightStatus == Light.DARK) {
						handler.setBrightness(Light.DIM);
						cycle = LDRCycle.DARK;
					}else {
						handler.setBrightness(lightStatus);
						cycle = LDRCycle.LIGHT;
					}
				}else {
					cnt++;
					cntMap.put(lightStatus, cnt);

					if(cntMap.size() > 1) {
						cntMap.clear();
						cntMap.put(lightStatus, cnt);
					}
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
					handler.turnOnScreen(false, Light.DIM);
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
					handler.turnOnScreen(false, lightStatus);
					handler.turnOffTM1637Time();
				}

				if (!handler.isWifiOn()) {//does not matter if the option to turn off wifi is ON, when day and wifi dowon, turn it on. Since we only turn off wifi at night
					handler.turnWifiOn();
				}
			}



		}catch(Throwable tr){
			ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
			eh.addError(ErrorType.LDR, new ErrorInfo(new FormatStackTrace(tr).getFormattedException()));
			logger.log(Level.SEVERE,"Error in ldr",tr);
		}
	}

	private int getCnt(Light current) {
		if (lastLightStatus == Light.DARK && current.isDayLight() || 
				lastLightStatus.isDayLight() && !current.isDayLight()) {
			return 4;
		}else {
			return 1;
		}
	}

}