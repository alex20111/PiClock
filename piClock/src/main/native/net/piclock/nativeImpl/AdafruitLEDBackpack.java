package net.piclock.nativeImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * <p>
 * This GPIO provider implements the PCF8574 I2C GPIO expansion board as native
 * Pi4J GPIO pins. More information about the board can be found here: *
 * http://www.ti.com/lit/ds/symlink/pcf8574.pdf
 * </p>
 * 
 * <p>
 * The PCF8574 is connected via I2C connection to the Raspberry Pi and provides
 * 8 GPIO pins that can be used for either digital input or digital output pins.
 * </p>
 * 
 * @author Robert Savage
 * 
 */
public class AdafruitLEDBackpack implements I2CDevice {
	
	public static final String DESCRIPTION = "AdafruitLEDBackpack GPIO Provider";

	static HashMap<String, Byte> translation = new HashMap<String, Byte>();

	CycleThread ct = null;
	private static boolean translationInitialized = false;

	// TODO abtract out Pi4J
//	private com.pi4j.io.i2c.I2CBus i2cbus;

	private com.pi4j.io.i2c.I2CDevice device;

	// ---------------------- refactored RasPi --------------------------------

	String lastValue = null;
	private boolean doubleDotsOn = false;

	public static void initTranslation() {

		if (translationInitialized) {
			return;
		}
		translation.put("", (byte) 0);
		translation.put(" ", (byte) 0);

		translation.put(":", (byte) 3);
		// translation.put("of", (byte)0)

		translation.put("a", (byte) 119);
		translation.put("b", (byte) 124);
		translation.put("c", (byte) 57);
		translation.put("d", (byte) 94);
		translation.put("e", (byte) 121);
		translation.put("f", (byte) 113);
		translation.put("g", (byte) 111);
		translation.put("h", (byte) 118);
		translation.put("i", (byte) 48);
		translation.put("J", (byte) 30);
		translation.put("k", (byte) 118);
		translation.put("l", (byte) 56);
		translation.put("m", (byte) 21);
		translation.put("n", (byte) 84);
		translation.put("o", (byte) 63);
		translation.put("p", (byte) 115);
		translation.put("q", (byte) 103);
		translation.put("r", (byte) 80);
		translation.put("s", (byte) 109);
		translation.put("t", (byte) 120);
		translation.put("u", (byte) 62);
		translation.put("v", (byte) 98);
		translation.put("x", (byte) 118);
		translation.put("y", (byte) 110);
		translation.put("z", (byte) 91);

		translation.put("-", (byte) 64);
		// translation.put("dot", (byte)???);

		translation.put("0", (byte) 63);
		translation.put("1", (byte) 6);
		translation.put("2", (byte) 91);
		translation.put("3", (byte) 79);
		translation.put("4", (byte) 102);
		translation.put("5", (byte) 109);
		translation.put("6", (byte) 125);
		translation.put("7", (byte) 7);
		translation.put("8", (byte) 127);
		translation.put("9", (byte) 111);

		translationInitialized = true;
	}

	public static byte translate(char c) {
		byte b = 0;
		String s = String.valueOf(c).toLowerCase();
		if (translation.containsKey(s)) {
			b = translation.get(s);
		}
		return b;
	}

	public 	AdafruitLEDBackpack(I2CBus  bus, int address) throws IOException, UnsupportedBusNumberException {

		// create I2C communications bus instance
//		i2cbus = I2CFactory.getInstance(bus);

		// create I2C device instance
		device = bus.getDevice(address);

		init7SegmentDisplay();
		initTranslation();

	}

	public void blinkOff(String value) {
		BlinkThread b = new BlinkThread();
		b.value = value;
		b.leaveOn = false;
		b.start();
	}

	public void blinkOn(String value) {
		BlinkThread b = new BlinkThread();
		b.value = value;
		b.start();
	}

	public void cycle(String msg) {
		if (ct != null) {
			cycleStop();
		}
		ct = new CycleThread(msg, 300);
		ct.start();
	}

	public void cycle(String msg, int delay) {
		if (ct != null) {
			cycleStop();
		}
		ct = new CycleThread(msg, delay);
		ct.start();
	}

	public void cycleStop() {
		if (ct != null) {
			ct.isRunning = false;
		}
	}
	public void displayDoubleDots(boolean on) {


		if (translationInitialized) {
			initTranslation();
		}
		// d1 d2 : d3 d4
		byte[] display = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		String data = lastValue;

		if (!doubleDotsOn && on) {
			display[4] = translate(':');
			doubleDotsOn = true;

		}else if(doubleDotsOn && !on) {
			display[4] = (byte)0;
			doubleDotsOn = false;
		}

		if (data == null || data == "") {
			writeDisplay(display);
			return;
		}else {
			if (data.length() < 4 && data.length() > 0) {
				data = String.format("%4s", data);
			}

			display[0] = translate(data.charAt(0));
			display[2] = translate(data.charAt(1));

			display[6] = translate(data.charAt(2));
			display[8] = translate(data.charAt(3));
			writeDisplay(display);
		}

	}

	public String display(String data) {
		System.out.println("Display string: " + data);
		lastValue = data;
		if (translationInitialized) {
			initTranslation();
		}
		// d1 d2 : d3 d4
		byte[] display = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		if (doubleDotsOn) {
			display[4] = translate(':');
		}

		if (data == null || data == "") {
			writeDisplay(display);
			return data;
		}

		if (data.length() < 4) {
			data = String.format("%4s", data);
		}

		display[0] = translate(data.charAt(0));
		display[2] = translate(data.charAt(1));
		display[6] = translate(data.charAt(2));
		display[8] = translate(data.charAt(3));

		writeDisplay(display);

		return data;
	}

	public void displayClear() throws IOException {
		lastValue = "";
		device.write(0x00, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 0, 16);
		device.write(0x00, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 0, 16);
		device.write(0x00, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 0, 16);
		device.write(0x00, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 0, 16);


	}

	public int displayDigit(int i) {

		String data = String.format("%d", i);
		display(data);

		return i;
	}

	public boolean init7SegmentDisplay() {
		try {
			if (device == null) {
				System.out.println("Device is null");
				return false;
			}

			device.write(0x21, (byte) 0x00);
			device.write(0x81, (byte) 0x00);
			device.write(0xEF, (byte) 0x00);
			device.write(0x00, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 0, 16);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
	public void setBrightness(int brightness) {
		if(brightness < 0) brightness = 0;
		else if (brightness > 15) brightness = 15;
		try {
			device.write((byte) (0xE0 | brightness));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public int read() throws IOException {
		return device.read();
	}

	@Override
	public int read(byte[] buffer, int offset, int size) throws IOException {
		return device.read(buffer, offset, size);
	}

	// New in pi4j 1.1
	@Override
	public int read(byte[] writeBuffer, int writeOffset, int writeSize, byte[] readBuffer, int readOffset, int readSize) throws IOException {
		return device.read(writeBuffer, writeOffset, writeSize, readBuffer, readOffset, readSize);
	}
	//

	@Override
	public int read(int address) throws IOException {
		return device.read(address);
	}

	@Override
	public int read(int address, byte[] buffer, int offset, int size) throws IOException {
		return device.read(address, buffer, offset, size);
	}

	@Override
	public void write(byte b) throws IOException {
		device.write(b);
	}

	// New in pi4j 1.1
	@Override
	public void write(int address, byte[] b) throws IOException {
		device.write(address, b);
	}
	//

	@Override
	public void write(byte[] buffer, int offset, int size) throws IOException {
		device.write(buffer, offset, size);
	}

	@Override
	public void write(int address, byte b) throws IOException {
		device.write(address, b);
	}

	// New in pi4j 1.1
	@Override
	public void write(byte[] b) throws IOException {
		device.write(b);
	}
	//

	@Override
	public void write(int address, byte[] buffer, int offset, int size) throws IOException {
		device.write(address, buffer, offset, size);
	}

	public byte[] writeDisplay(byte[] data) {
		if (device == null) {
			System.out.println("Device is null 2");
			return data;
		}

		try {
			device.write(0x00, data, 0, 16);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return data;
	}

	@Override
	public int getAddress() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void ioctl(long arg0, int arg1) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void ioctl(long arg0, ByteBuffer arg1, IntBuffer arg2) throws IOException {
		// TODO Auto-generated method stub

	}
	public class BlinkThread extends Thread {
		public int number = 5;
		public int delay = 100;
		public String value = "";
		public boolean leaveOn = true;

		@Override
		public void run() {
			try {
				int count = 0;
				while (count < 5) {
					display(value);
					Thread.sleep(delay);        
					display("   ");
					Thread.sleep(delay);
					++count;
				}

				if (leaveOn) {
					display(value);
				}
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public class CycleThread extends Thread {
		public boolean isRunning = false;
		int delay = 300;
		String msg;

		public CycleThread(String msg, int delay) {
			this.msg = "    " + msg + "    ";
			this.delay = delay;
		}

		@Override
		public void run() {
			isRunning = true;
			try {
				while (isRunning) {
					// start with scroll on page
					for (int i = 0; i < msg.length() - 3; ++i) {
						display(msg.substring(i, i + 4));
						sleep(delay);
					}
				}
			} catch (InterruptedException e) {
				isRunning = false;
			}
		}
	}

}
