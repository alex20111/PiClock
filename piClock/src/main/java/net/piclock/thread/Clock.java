package net.piclock.thread;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;

import net.piclock.main.Constants;
import net.piclock.main.PiHandler;

public class Clock implements Runnable {

	private JLabel clockLabel;
	private JLabel weekDateLable;
	private SimpleDateFormat sdfDate = new SimpleDateFormat("EEE, MMM d");
	private SimpleDateFormat sdfTime = new SimpleDateFormat(Constants.HOUR_MIN);
	
	public Clock(JLabel clockLabel, JLabel weekDateLable){
		this.clockLabel = clockLabel;
		this.weekDateLable = weekDateLable;
	}
	@Override
	public void run() {
		Date dt = new Date();
		String time = sdfTime.format(dt);
		clockLabel.setText(time);
		weekDateLable.setText(sdfDate.format(dt));
		
		if (!PiHandler.screenOn){
			PiHandler.displayTM1637Time(time);
		}
		
	}

}
