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
import net.piclock.enums.DayNightCycle;
import net.piclock.enums.Light;
import net.piclock.main.Constants;
import net.piclock.main.PiHandler;
import net.piclock.main.Preferences;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;
import net.piclock.util.FormatStackTrace;

public class LDRStatusWorker implements Runnable{

	private static final Logger logger = Logger.getLogger( LDRStatusWorker.class.getName() );

	private SwingContext ct = SwingContext.getInstance();
	private DayNightCycle lastCycleStatus = DayNightCycle.NONE;
	private DayNightCycle cycle = DayNightCycle.NONE;

	private Light lastLightStatus = Light.VERY_BRIGHT;

	private Map<Light, Integer> cntMap = new HashMap<Light, Integer>();
	PiHandler handler;

	public LDRStatusWorker() {
		cntMap.put(Light.DARK, 4);
		handler = PiHandler.getInstance();
		Light currLight = handler.getLDRstatus();

		cycle = (currLight == Light.DARK ? DayNightCycle.NIGHT : DayNightCycle.DAY); 
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
						cycle = DayNightCycle.NIGHT;
					}else {
						handler.setBrightness(lightStatus);
						cycle = DayNightCycle.DAY;
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

			if (cycle == DayNightCycle.NIGHT ){

				if (cycle != lastCycleStatus) {
					//turn off screeen if screen is on.
					if(p.isAutoOffScreen()){
						handler.turnOffScreen();
						handler.displayTM1637Time(new SimpleDateFormat(Constants.HOUR_MIN).format(new Date()));
					}
					ct.putSharedObject(Constants.DAY_NIGHT_CYCLE, DayNightCycle.NIGHT);
					ThemeHandler themes = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
					themes.fireNightCycle();
					lastCycleStatus = DayNightCycle.valueOf(cycle.name());
				}

				//if we need to shutdown wifi and it is night 
				if (p.isWifiOff() && handler.isWifiOn() && !handler.isWifiAutoShutdownInProgress()) {
					logger.log(Level.CONFIG, "Fire wifi auto shudown from LDR Worker");
					handler.autoWifiShutDown(true);
				}else if (!p.isWifiOff() && !handler.isWifiOn()){
					logger.log(Level.CONFIG, "Turn on wifi because it was off at night");
					handler.turnWifiOn();					
				}				


			}else if (cycle == DayNightCycle.DAY ){

				if (cycle != lastCycleStatus) {

					handler.turnOnScreen(true, lightStatus);
					handler.turnOffTM1637Time();


					ct.putSharedObject(Constants.DAY_NIGHT_CYCLE, DayNightCycle.DAY);
					ThemeHandler themes = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
					themes.fireDayCycle();
					lastCycleStatus = DayNightCycle.valueOf(cycle.name());
				}

				if (!handler.isWifiOn()) {//does not matter if the option to turn off wifi is ON, when day and wifi dowon, turn it on.
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