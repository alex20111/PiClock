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
		
//		Exec exe = new Exec();
//		exe.addCommand("./native/2591");
//		
//		exe.timeout(5000);
//		exe.run();
//		
//		String ret = exe.getOutput();
//		
//		System.out.println("Info: " + ret);
		
		System.out.println("Getting light");
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




























//import com.pi4j.io.i2c.I2CBus;
//import com.pi4j.io.i2c.I2CDevice;
//import com.pi4j.io.i2c.I2CFactory;
//import java.io.IOException;
//import java.text.DecimalFormat;
//import java.text.NumberFormat;
//
//public class TSL2561
//{
//	public static void main(String args[]) throws Exception
//	{
//		// Create I2C bus
//		I2CBus Bus = I2CFactory.getInstance(I2CBus.BUS_1);
//		// Get I2C device, TSL2561 I2C address is 0x39(57)
//		I2CDevice device = Bus.getDevice(0x39);
//
//		// Select control register
//		// Power ON mode
//		device.write(0x00 | 0x80, (byte)0x03);
//		// Select timing register
//		// Nominal integration time = 402ms
//		device.write(0x01 | 0x80, (byte)0x02);
//		Thread.sleep(500);
//
//		// Read 4 bytes of data
//		// ch0 lsb, ch0 msb, ch1 lsb, ch1 msb
//		byte[] data=new byte[4];
//		device.read(0x0C | 0x80, data, 0, 4);
//
//		// Convert the data
//		double ch0 = ((data[1] & 0xFF)* 256 + (data[0] & 0xFF));
//		double ch1 = ((data[3] & 0xFF)* 256 + (data[2] & 0xFF));
//
//		// Output data to screen
//		System.out.printf("Full Spectrum(IR + Visible) : %.2f lux %n", ch0);
//		System.out.printf("Infrared Value : %.2f lux %n", ch1);
//		System.out.printf("Visible Value : %.2f lux %n", (ch0 - ch1));
//	}
//}
