package net.piclock.nativeImpl;

import java.time.LocalDateTime;

public class Tm1637 {

	static{
		 System.loadLibrary("piclocknative");
	}
	
	public Tm1637(int clk, int dio){
		init(4,5);
	}
	
	
	
	public static void main(String[] args) throws InterruptedException {
		
		Tm1637 tm = new Tm1637(4,5);
		
		System.out.println("Displat time");
		LocalDateTime d = LocalDateTime.now();
		tm.displayTime(d.getHour(), d.getMinute(), 1);
		Thread.sleep(3000);
		System.out.println("Clear display");
		tm.clearDisplay();
		Thread.sleep(1000);
		System.out.println("set brightness from 0 to 7");
		for(int i = 0 ; i < 7; i++){
			System.out.println("Brightness level: " + i);
			tm.setBrightness(i);
			Thread.sleep(1500);
		}
		System.out.println("clearing dots");
		tm.displayPoint(false);
		Thread.sleep(2000);
		System.out.println("Adding points again");
		tm.displayPoint(true);
		Thread.sleep(2000);
		System.out.println("random numbers - displayNumberPos");
		tm.displayNumberPos(9, 1);
		Thread.sleep(1000);
		tm.displayNumberPos(6, 4);
		Thread.sleep(1000);
		tm.displayNumberPos(2, 2);
		System.out.println("Finished!!!! -- Clearing out");
		Thread.sleep(2000);
		tm.clearDisplay();		
	}
	private native int init(int clk, int dio);
	public native void displayTime(int hour, int min, int format); //format 0 = 12hrs/ 1 = 24 hrs. Always needs to be passed as 24hours.
	public native void clearDisplay();
	public native void displayNumberPos(int nbr, int position);//display a number to a position. ex: 5 pos 3 (out of 4) on the display.
	public native void displayPoint(boolean pointOn);
	public native void setBrightness(int brightness);
	
}
