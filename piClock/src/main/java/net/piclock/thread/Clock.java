package net.piclock.thread;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class Clock implements Runnable {
	
	private static final Logger logger = Logger.getLogger( Clock.class.getName() );

	private boolean timeChanged = false;
	private boolean firstTimeIn = true;
	private DayNightCycle currCycle = DayNightCycle.NOT_DEFINED;

	private JLabel clockLabel;
	private JLabel weekDateLable;
	
	private DateTimeFormatter longDt = DateTimeFormatter.ofPattern("EEE, MMM d");
	private DateTimeFormatter shortDt = DateTimeFormatter.ofPattern("HH:mm");

	private PiHandler handler = PiHandler.getInstance();
	private SwingContext ct = SwingContext.getInstance();

	private LocalDateTime prevDate = null;
	
	private ZoneId zid = null;
	private SunriseSunsetCalculator calc;
	private LocalDateTime sunrise;
	private LocalDateTime sunset;	

	public Clock(JLabel clockLabel, JLabel weekDateLable){	

		this.clockLabel = clockLabel;
		this.weekDateLable = weekDateLable;
		
		zid = ZoneId.systemDefault();
		Location loc = new Location("45.41117","-75.69812");
		calc = new SunriseSunsetCalculator(loc, TimeZone.getDefault());
		Calendar now = Calendar.getInstance();
		sunrise = convert(calc.getOfficialSunriseCalendarForDate(now));
		sunset = convert(calc.getOfficialSunsetCalendarForDate(now));

	}
	//IN LDRWorker, change DayNightCycle by LDRCycle , ENUMS OF : LIGHT, DARK, NOT_DEFINED. 
	//	Remove the theme handler and the fire night/day cycle.
	//  the dayNightCycle enum will be controled by the sunrise and sunset in CLOCK.
	// get the Maven library for sunrise sunset
	
	@Override
	public void run() {
		try{
			LocalDateTime dt = LocalDateTime.now();

			String time = "";
			
			if (dt.isAfter(sunrise) && dt.isBefore(sunset) && !currCycle.isDay()){ 
				logger.config("Day triggered. Sunrise: " + sunrise);
				currCycle = DayNightCycle.DAY;			
				ct.putSharedObject(Constants.DAY_NIGHT_CYCLE, currCycle);
			}else if (  (dt.isAfter(sunset) || dt.isBefore(sunrise) ) && !currCycle.isNight()  ){
				Logger.config("Night triggered. Sunset: " + sunset);
				currCycle = DayNightCycle.NIGHT;
				ct.putSharedObject(Constants.DAY_NIGHT_CYCLE, currCycle);
			}		

			if ( (prevDate == null || dt.truncatedTo(ChronoUnit.MINUTES).isAfter(prevDate.truncatedTo(ChronoUnit.MINUTES)))){
				timeChanged = true;
				prevDate = dt;
				Calendar now = Calendar.getInstance();
				sunrise = convert(calc.getOfficialSunriseCalendarForDate(now));
				sunset = convert(calc.getOfficialSunsetCalendarForDate(now));			
			}

			if(timeChanged){			
				time = dt.format(shortDt);
				clockLabel.setText(time);
				weekDateLable.setText(dt.format(longDt));
				prevDate = dt;
			}

			if ( !handler.isScreenOn() && ( timeChanged || firstTimeIn) ){	
				handler.displayTM1637Time(time);
				firstTimeIn = false;
			}else if (handler.isScreenOn() && !firstTimeIn){
				System.out.println("Screen On , Reset value");
				firstTimeIn = true; //reset value
			}


			if (timeChanged){ //reset time changed
				timeChanged = false;
			}	
		}catch(Throwable r){
			r.printStackTrace();
		}
	}


	private LocalDateTime convert(Calendar cal){
		return cal.getTime().toInstant().atZone(zid).toLocalDateTime();
	}


}
