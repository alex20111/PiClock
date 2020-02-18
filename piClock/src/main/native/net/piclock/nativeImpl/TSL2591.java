package net.piclock.nativeImpl;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class TSL2591 {
	
	static {
        System.loadLibrary("piclocknative");
        }	

	public native void init(int address);
	public native int getFullSpectrum();
	public native int getInfrared();
	public native int getVisible();
	public native int getLux();
	public native void setGain(int gainLevel);
	public native void setIntegrationTime(int intgTime);
	
		
	public static void main(String[] args) throws InterruptedException, IOException {

		TSL2591 t = new TSL2591();
		t.init(0x29);

		 FileWriter myWriter = new FileWriter("/home/pi/java/lixreading.txt");
		
		while(true) {
			StringBuilder sb = new StringBuilder();
			sb.append("Full: " + t.getFullSpectrum() + " - ");
			sb.append("IR : " + t.getInfrared()+ " - ");
			sb.append("LUX: " + t.getLux()+ " - ");
			sb.append("Visible: " + t.getVisible()+ " - " + "\n\n");
			
			myWriter.write(sb.toString() + "  " + LocalDateTime.now());
			myWriter.flush();
			Thread.sleep(2000);
		}

		
		
	
	}
	
	enum Gain{
		TSL2591_GAIN_LOW (0x00),    // low gain (1x)
		TSL2591_GAIN_MED(0x10),    // medium gain (25x)
		TSL2591_GAIN_HIGH(0x20),    // medium gain (428x)
		TSL2591_GAIN_MAX (0x30);
		
		private int gain = 0x10;
		
		private Gain(int g) {
			gain = g;
		}
		
		public int getGainValue() {
			return gain;
		}
	}

}
