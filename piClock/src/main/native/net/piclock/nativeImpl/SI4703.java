package net.piclock.nativeImpl;

public class SI4703 {
	
	static{
		 System.loadLibrary("piclocknativeCPP");
	}
	
	public SI4703(int reset, int sdio){
		init(reset, sdio);
	}

	public static void main(String[] args) throws InterruptedException {
		
		SI4703 si = new SI4703(18,0);
		float frequency = 106.9f;
		
		System.out.println("Starting radio");
		Thread.sleep(1000);
		System.out.println("Powering ON:");
		int stat = si.powerOn();
		if (stat == 0){
			
			System.out.println("Setting frequency 106.9");
			si.setFrequency(frequency);
			Thread.sleep(1000);
			System.out.println("Current frequency from getFrequency: " + si.getFrequency());
			Thread.sleep(1000);
			System.out.println("trying Volume UP");
			for(int i = 0; i < 6 ; i++){
				System.out.println("Volume level: " + i);
				si.setVolume(i);
				Thread.sleep(1000);
			}
			System.out.println("trying Volume Down");
			for(int y = 5; y >= 0 ; y--){
				System.out.println("Volume level: " + y);
				si.setVolume(y);
				Thread.sleep(1000);
			}
			Thread.sleep(2000);
			si.setVolume(2);
			System.out.println("Min frequency: " + si.minFrequency());
			System.out.println("Max frequency: " + si.maxFrequency());
			System.out.println("seek UP");
			System.out.println(" to: " + si.seek(SeekDirection.UP));
			Thread.sleep(2000);
			System.out.println("seek Down");
			System.out.println(" to: " + si.seek(SeekDirection.DOWN));
			Thread.sleep(2000);
			System.out.println("Get RDS.");
			System.out.println(si.getRDS());
			System.out.println("end powering down");
			si.powerOff();
			Thread.sleep(2000);
			System.out.println("Powering up again");
			si.powerOn();
			si.setFrequency(89.9f);
			Thread.sleep(1000);
			System.out.println("Current frequency from getFrequency: " + si.getFrequency());
			Thread.sleep(3000);
			System.out.println("Powering down");
			si.powerOff();
			Thread.sleep(2000);
			
			
			
			
			
		}else{
			System.out.println("Problem powering up radio. Status : " + stat);
		}		

	}	
	/**
	 * Seek for the next station.
	 * @param sd - Seek direction
	 * @return  station number or 0 if not found.
	 */
	public float seek(SeekDirection sd){
		int conv = 0;
		if (sd == SeekDirection.UP){
			conv = 0;
		}else if (sd == SeekDirection.DOWN){
			conv = 1;
		}
		
		return seek(conv);
	}
	
	public native int powerOn();
	public native void setVolume(int volume);
	public native void powerOff();
	public native void setFrequency(float frequency);
	public native float getFrequency();
	public native float minFrequency();
	public native float maxFrequency();
	public native String getRDS();
	public native int getSignalStrength();
	private native float seek(int seekDirection);
	private native void init(int resetPin, int sdioPin);
		
	enum SeekDirection{
		UP, DOWN;
	}
}