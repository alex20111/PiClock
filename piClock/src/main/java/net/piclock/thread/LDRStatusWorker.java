package net.piclock.thread;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.enums.DayNightCycle;
import net.piclock.main.Constants;
import net.piclock.main.PiHandler;
import net.piclock.main.Preferences;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;

public class LDRStatusWorker implements Runnable{

	private static final Logger logger = Logger.getLogger( LDRStatusWorker.class.getName() );
	
	private SwingContext ct = SwingContext.getInstance();
	private DayNightCycle lastStatus = DayNightCycle.NONE;
	
	@Override
	public void run() {
		try{

			PiHandler handler = PiHandler.getInstance();
			//call it every 10 seconds
			Preferences p = (Preferences)ct.getSharedObject(Constants.PREFERENCES);

			DayNightCycle cycle = handler.getLDRstatus();

			if (cycle == DayNightCycle.NIGHT && cycle != lastStatus){
				//turn off screeen if screen is on.
				if(p.isAutoOffScreen()){
					handler.turnOffScreen();
					handler.displayTM1637Time(new SimpleDateFormat(Constants.HOUR_MIN).format(new Date()));
				}
				ct.putSharedObject(Constants.DAY_NIGHT_CYCLE, DayNightCycle.NIGHT);
				ThemeHandler themes = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
				themes.fireNightCycle();

			}else if (cycle == DayNightCycle.DAY && cycle != lastStatus){
				if(p.isAutoOffScreen()){
					handler.turnOnScreen(true);
					handler.turnOffTM1637Time();
				}
				ct.putSharedObject(Constants.DAY_NIGHT_CYCLE, DayNightCycle.DAY);
				ThemeHandler themes = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
				themes.fireDayCycle();

			}
			logger.log(Level.CONFIG, "LDR Init: " + cycle + " LastStatus: " + lastStatus + " AutoOffScreen Option: " + p.isAutoOffScreen());
			
			lastStatus = DayNightCycle.valueOf(cycle.name());				
			
			//check is screen is on OR off, if it's off and it's supposed to be off , then start the auto shutdown.
			if (!handler.isScreenOn() && handler.isMonitorOn() && !handler.isAutoShutdownInProgress()) {
				logger.log(Level.CONFIG,"auto shutting down screen from LDR worker");
				//auto shutdown.
				handler.autoShutDownScreen();
			}
			
		}catch(Throwable tr){
			logger.log(Level.SEVERE,"Error in ldr",tr);
		}
	}
}