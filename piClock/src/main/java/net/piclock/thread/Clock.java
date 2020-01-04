package net.piclock.thread;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.swing.JLabel;

import net.piclock.main.Constants;
import net.piclock.main.PiHandler;

public class Clock implements Runnable {

	private JLabel clockLabel;
	private JLabel weekDateLable;
	
	private DateTimeFormatter longDt = DateTimeFormatter.ofPattern("EEE, MMM d");
	private DateTimeFormatter shortDt = DateTimeFormatter.ofPattern(Constants.HOUR_MIN);
	
	
	private PiHandler handler = PiHandler.getInstance();
	private LocalDateTime prevDate = null;
	
	public Clock(JLabel clockLabel, JLabel weekDateLable){
		this.clockLabel = clockLabel;
		this.weekDateLable = weekDateLable;
	}
	@Override
	public void run() {
		LocalDateTime dt = LocalDateTime.now();
		String time = dt.format(shortDt); //sdfTime.format(dt);
		clockLabel.setText(time);
		weekDateLable.setText(dt.format(longDt));//sdfDate.format(dt));
		
		if (!handler.isScreenOn() && (prevDate == null || dt.truncatedTo(ChronoUnit.MINUTES).isAfter(prevDate.truncatedTo(ChronoUnit.MINUTES)))){
			handler.displayTM1637Time(time);
			prevDate = dt;
		}
		
	}

}
