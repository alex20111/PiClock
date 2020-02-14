package net.piclock.handlers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.ExecuteException;


import home.misc.Exec;
import net.piclock.enums.Light;

public class PiScreenHandler {

	private String ldrFile = "home/pi/native/TSL2561/tslReading.rdr";
	private Path  ldrFilePath = null;
	
	public void setScreenBrightness(Light light) {


		RandomAccessFile f;
		try {
			f = new RandomAccessFile(new File("/sys/class/backlight/rpi_backlight/brightness"), "rw");

			f.seek(0); // to the beginning
			f.write(String.valueOf(light.getPiTchScreenBrightness()).getBytes());
			f.close();
		} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public int getVisibleLight() throws ExecuteException, IOException {		

		new Thread(new Runnable(){

		@Override
		public void run() {
			Exec exe = new Exec();
			exe.addCommand("./native/2591").addCommand(ldrFile).addCommand("1").addCommand("0");
			exe.run();		
		}			
		}).start();
		
	//read from file
		if (ldrFilePath == null){
			ldrFilePath = Paths.get(fileLoc);
		}		
		
		List<String> r = Files.readAllLines(f);
		
		String split[] = r.get(0).split(",");	
				
		System.out.println("Getting light - Visible : " + split[2] );
		return 176;
	}

	public void time(boolean ON) {
		System.out.println("Setting time");
	}
	

	public static void main(String args[]) throws InterruptedException,  IOException {

		System.out.println("Starting");
		PiScreenHandler handler = new PiScreenHandler();

		boolean testScreenBrightness = false;
		boolean testLuxSensor 		 = true;


		if (testScreenBrightness) {
			System.out.println("Test brightness settings");
			Thread.sleep(1000);
			Light l = Light.DARK;
			System.out.println("Turning Off screen");
			handler.setScreenBrightness(l);
			Thread.sleep(2000);
			l = Light.VERY_BRIGHT;
			System.out.println("Turning screen back on");
			handler.setScreenBrightness(l);

			System.out.println("test all levels");
			for (int i = 0; i < Light.values().length ; i++) {
				Light lvl = Light.values()[i];
				System.out.println("Level: " + lvl);
				handler.setScreenBrightness(lvl);
				Thread.sleep(1000);
			}
			l = Light.VERY_BRIGHT;
			System.out.println("Turning screen back on");
			handler.setScreenBrightness(l);
			System.out.println("End of screen brightness test");

		}
		
		if (testLuxSensor) {
			System.out.println("Testing lux sensor");
//			handler.I2cScanner(I2CBus.BUS_1);
						
			Thread.sleep(1000);
			
//			handler.testTSL2561(true);
			for(int y = 0 ; y < 10; y ++) {
				handler.getVisibleLight();
				Thread.sleep(2000);
			}
			
			
		}		
		





	}
}
