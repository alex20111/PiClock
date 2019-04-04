package net.piclock.arduino;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class ArduinoCmd {
	
	private static ArduinoCmd arduinoCmd;
	private I2CBus i2c;
	private I2CDevice device;
	
	private String prevCommand = "";

	public static ArduinoCmd getInstance() throws UnsupportedBusNumberException, IOException {
		if (arduinoCmd == null) {
			synchronized (ArduinoCmd.class) {
				if(arduinoCmd == null) {
					arduinoCmd = new ArduinoCmd();
				}
			}
		}
		return arduinoCmd;
	}
	
	private ArduinoCmd() throws UnsupportedBusNumberException, IOException {
		System.out.println("init arduino cmd");
		 i2c = I2CFactory.getInstance(I2CBus.BUS_3);
		device = i2c.getDevice(0x08);
	}
	public int readLDR() throws IOException, InterruptedException  {
		if (!"l".equals(prevCommand)) {
			prevCommand = "l";
			getDevice().write((byte)'l');
			Thread.sleep(100);
		}
		int ldrVal = 0;
		try {
		  ldrVal =	getDevice().read();
		} catch (IOException e) {
			Thread.sleep(1000);
			System.out.println("retry");
			ldrVal =	getDevice().read();
		}
		
		
		return ldrVal;
	}
	public int readButtonA() throws IOException, InterruptedException {
		if (!"a".equals(prevCommand)) {
			prevCommand = "a";
			getDevice().write((byte)'a');
			Thread.sleep(50);
		}		
		return getDevice().read();
	}
	public void writeTime(String time) throws IOException {
		byte[] commBuffer = new byte[3];
		
		int hours = Integer.parseInt(time.substring(0, 2));
		int minutes = Integer.parseInt(time.substring(3, time.length()));		
		System.out.println("H: " + hours + " min " + minutes);
		commBuffer[0] = 't'; //for time request.. 'b' will be for buzzer request
		commBuffer[1] = (byte)hours;
		commBuffer[2] = (byte)minutes;
		getDevice().write(commBuffer);
	}
	public void timeOff() throws IOException {
		byte[] commBuffer = new byte[1];

		commBuffer[0] = 'o'; //for time request.. 'b' will be for buzzer request
		getDevice().write(commBuffer);
	}
	public void buzzer(boolean on) throws IOException {
		byte[] commBuffer = new byte[3];

		if (on) {
			commBuffer[0] = 'b'; // 'b' will be for buzzer request. 01 = on = 02 = off
			commBuffer[1] = (byte)0;
			commBuffer[2] = (byte)1;
		}else {
			commBuffer[0] = 'b'; // 'b' will be for buzzer request. 01 = on = 02 = off
			commBuffer[1] = (byte)0;
			commBuffer[2] = (byte)2;			
		}

		getDevice().write(commBuffer);
	}
	
	private synchronized I2CDevice getDevice() {
		return device;
	}
}
