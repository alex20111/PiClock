package net.piclock.handlers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;


import home.misc.Exec;
import net.piclock.nativeImpl.SI4703;

public class PiScreenHandler {

	private static final Logger logger = Logger.getLogger( PiScreenHandler.class.getName() );

	private SI4703 si4703;

	private boolean screenOn = true;
	private boolean clockOn = false;

	private Thread clockThread;
	
	public PiScreenHandler(){

		init();
	}


	public void setScreenBrightness(int level) {

		try {
			String brightness = "/sys/class/backlight/rpi_backlight/brightness";
			String onOff      = "/sys/class/backlight/rpi_backlight/bl_power";
			if (level > 0 ){
				if(!screenOn){
					writeToScreenFile(0, onOff);
					screenOn = true;
				}
				writeToScreenFile(level, brightness);
			}else if (screenOn && level == 0){

				writeToScreenFile(1, onOff);
				screenOn = false;
			}
		}catch (Exception e) {
			logger.log(Level.SEVERE,"error ", e);
		}
	}


	public void clockOn() {
		logger.log(Level.CONFIG, "clockOn() -> " + clockOn);

		if (clockThread == null || !clockThread.isAlive()) {
			clockThread = new Thread(new Runnable() {

				@Override
				public void run() {
					logger.log(Level.CONFIG, "Starting clock thread");
					int ret = 99;
					

						try {
							Exec exec = new Exec();

							exec.addCommand("sudo");
							exec.addCommand("./scripts/clock.sh");
							clockOn = true;
							ret = exec.run();

						}catch(Exception ex) {
							logger.log(Level.INFO, "Exception in clockThread", ex);
							clockOn = false;
						}

				
					logger.log(Level.CONFIG, "Clock thread finished. " + ret);
				}
			});
			
			clockThread.start();
		}else {
			clockOn = true;
		}
	}
	public void clockOff() {
		logger.config("Clock off(). is Clock thread alive: " + (clockThread != null ? clockThread.isAlive() : "False"));
		
		
		if (clockThread != null && clockThread.isAlive()) {
			
			try {
				//1st kill the process
				Exec exec = new Exec();

				exec.addCommand("sudo");
				exec.addCommand("./scripts/killClock.sh");
				clockOn = true;
				int ret = exec.run();

				clockOn = false;
				logger.config("END CLOCK OFF. -> Ret: " + ret + " out: " + exec.getOutput());
			}catch(Exception ex) {
				logger.log(Level.SEVERE, "Exception clock off", ex);
			}
		}
	}
	public void radioOn(){
		int stat = si4703.powerOn();
		si4703.setVolume(1);
	}
	public void radioOff(){
		si4703.powerOff();
	}
	public void setFmStation(float fmStation){
		si4703.setFrequency(fmStation);
	}

	private void init(){

		//radio
		si4703 = new SI4703(18,0);
	}

	private void writeToScreenFile(int level, String filePath) throws IOException{
		RandomAccessFile f;

		f = new RandomAccessFile(new File(filePath), "rw");
		f.seek(0); // to the beginning
		f.write(String.valueOf(level).getBytes());
		f.close();

	}

	public boolean isClockOn() {
		return clockOn;
	}
}
