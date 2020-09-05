package net.piclock.thread;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.enums.DayNightCycle;
import net.piclock.handlers.PiHandler;
import net.piclock.main.Constants;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeEnum;
import net.piclock.theme.ThemeHandler;
import net.piclock.util.FormatStackTrace;

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

	private int minutesDiff = 20;


	public Clock(JLabel clockLabel, JLabel weekDateLable){	

		this.clockLabel = clockLabel;
		this.weekDateLable = weekDateLable;

		zid = ZoneId.systemDefault();
		Location loc = new Location("45.41117","-75.69812");
		calc = new SunriseSunsetCalculator(loc, TimeZone.getDefault());
		Calendar now = Calendar.getInstance();
		sunrise = convert(calc.getOfficialSunriseCalendarForDate(now)).minusMinutes(minutesDiff);
		sunset = convert(calc.getOfficialSunsetCalendarForDate(now)).plusMinutes(minutesDiff);

	}


	@Override
	public void run() {
		try{
			LocalDateTime dt = LocalDateTime.now();

			String time = "";

			if (dt.isAfter(sunrise) && dt.isBefore(sunset) && !currCycle.isDay()){ 
				logger.config("Day triggered. Sunrise: " + sunrise);
				currCycle = DayNightCycle.DAY;			
				ct.putSharedObject(Constants.DAY_NIGHT_CYCLE, currCycle);				
				ThemeHandler themes = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
				themes.fireDayCycle();


			}else if (  (dt.isAfter(sunset) || dt.isBefore(sunrise) ) && !currCycle.isNight()  ){
				logger.config("Night triggered. Sunset: " + sunset);
				currCycle = DayNightCycle.NIGHT;
				ct.putSharedObject(Constants.DAY_NIGHT_CYCLE, currCycle);
				ThemeHandler themes = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
				themes.fireNightCycle();
			}		

			if ( (prevDate == null || dt.truncatedTo(ChronoUnit.MINUTES).isAfter(prevDate.truncatedTo(ChronoUnit.MINUTES)))){
				timeChanged = true;
				prevDate = dt;
				Calendar now = Calendar.getInstance();
				sunrise = convert(calc.getOfficialSunriseCalendarForDate(now)).minusMinutes(minutesDiff);
				sunset = convert(calc.getOfficialSunsetCalendarForDate(now)).plusMinutes(minutesDiff);			
			}

			if(timeChanged){			
				time = dt.format(shortDt);
				clockLabel.setText(time);
				weekDateLable.setText(dt.format(longDt));
				prevDate = dt;
			}

			if ( !handler.isScreenOn() && ( timeChanged || firstTimeIn) ){	
				if (time == null || time.trim().length() == 0) {
					time = dt.format(shortDt);
				}
				handler.displayTM1637Time(time);
				firstTimeIn = false;
			}else if (handler.isScreenOn() && !firstTimeIn){
				firstTimeIn = true; //reset value
			}


			if (timeChanged){ //reset time changed
				timeChanged = false;
			}				

			//switch theme depending on date
			switchTheme(dt);

		}catch(Throwable r){
			ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
			eh.addError(ErrorType.GENERAL, new ErrorInfo(new FormatStackTrace(r).getFormattedException()));
			logger.log(Level.SEVERE,"Error in clock",r);
		}
	}

	private void switchTheme(LocalDateTime dt) {

		ThemeHandler themes = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);

		//find if a theme's date is valid for the current date.
		LocalDate now = dt.toLocalDate();
		ThemeEnum detectedTheme = null;
		for(ThemeEnum te : ThemeEnum.values()) {		

			if ( te.getThemeStart() != null && te.getThemeEnd() != null) {
				LocalDate start = LocalDate.of(now.getYear(), te.getThemeStart().getMonthValue(), te.getThemeStart().getDayOfMonth());
				LocalDate end = LocalDate.of(now.getYear(), te.getThemeEnd().getMonth(), te.getThemeEnd().getDayOfMonth());

				if(	( start.isBefore(now) && end.isAfter(now) ) ||
						start.isEqual(now) ||  end.isEqual(now)  	) {
					detectedTheme = te;
					break;
				}
			}

		}

		if (detectedTheme != null && themes.getCurrentTheme() != detectedTheme) {
			logger.log(Level.CONFIG, "New theme detechted for the current date. Theme: " + detectedTheme);
			themes.loadTheme(detectedTheme);

		}else if (themes.getCurrentTheme() != ThemeEnum.defaultTheme && detectedTheme == null){
			//default theme
			logger.log(Level.CONFIG, "No theme found for the date, using default Theme");
			themes.loadTheme(ThemeEnum.defaultTheme);
		}


	}
	private LocalDateTime convert(Calendar cal){
		return cal.getTime().toInstant().atZone(zid).toLocalDateTime();
	}


}
