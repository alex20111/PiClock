package net.piclock.handlers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;


import net.piclock.nativeImpl.SI4703;

public class PiScreenHandler {

	private static final Logger logger = Logger.getLogger( PiScreenHandler.class.getName() );

	private SI4703 si4703;

	private boolean screenOn = true;
	
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
//		logger.log(Level.CONFIG, "Brightness level: " + level + " File accessed: " +filePath);
		RandomAccessFile f;

		f = new RandomAccessFile(new File(filePath), "rw");
		f.seek(0); // to the beginning
		f.write(String.valueOf(level).getBytes());
		f.close();

	}

}
