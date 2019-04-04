package temp;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
//https://www.raspberrypi.org/forums/viewtopic.php?t=150038
public class CommToArd {

	private static CommToArd comm = null;
	
	private I2CBus i2c;
	private I2CDevice device;
	
	private String prevCommand = "";

	public CommToArd() throws UnsupportedBusNumberException, IOException {
//	       for (int number = I2CBus.BUS_0; number <= I2CBus.BUS_17; ++number) {
//	            try {
//	                @SuppressWarnings("unused")
//					I2CBus bus = I2CFactory.getInstance(number);
//	                System.out.println("Supported I2C bus " + number + " found");
//	            } catch (IOException exception) {
//	            	System.out.println("I/O error on I2C bus " + number + " occurred");
//	            } catch (UnsupportedBusNumberException exception) {
//	            	System.out.println("Unsupported I2C bus " + number + " required");
//	            }
//	}
		
		
		 i2c = I2CFactory.getInstance(I2CBus.BUS_3);
		device = i2c.getDevice(0x08);
	}
	
	public static void main(String[] args) throws  IOException, UnsupportedBusNumberException, InterruptedException {

		
		System.out.println("Starting. ctrl-c to stop");

		final CommToArd c = CommToArd.getInstance();

		 Runtime.getRuntime().addShutdownHook(new Thread() 
		    { 
		      public void run() 
		      { 
		        System.out.println("Shutdown Hook is running !");
		        try {
					c.i2c.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		      } 
		    }); 
		  
		while(true) {
			System.out.println("Get ldr in loop22");
			for(int i = 0 ; i < 20 ; i ++) {
				System.out.println("LDR: " + c.readLDR());
				Thread.sleep(100);
			}
//			System.out.println("Get button A");
//			for(int i = 0 ; i < 10 ; i ++) {
//				System.out.println("btn: " + c.readButtonA());
//				Thread.sleep(200);
//			}
			System.out.println("display time ON");
			String time = new SimpleDateFormat("HH:mm").format(new Date());
			c.writeTime(time);
			Thread.sleep(2000);
			System.out.println("buzzer on");
			c.buzzer(true);
			Thread.sleep(10000);
			System.out.println("buzzer off");
			c.buzzer(false);
			Thread.sleep(2000);
			System.out.println("LDR 2nd: " + c.readLDR());
			Thread.sleep(2000);
			System.out.println("reading button input");
			for(int i = 0; i < 30 ; i++) {
				System.out.println("Button: " + c.readButtonA());
				Thread.sleep(100);
			}
			System.out.println("Turning time off");
			c.timeOff();
			Thread.sleep(2000);
			

		}

	}
	
	
	/** oN arduino   https://github.com/avishorp/TM1637
	 *  OR https://github.com/bremme/arduino-tm1637/blob/master/examples/ExtClock/ExtClock.ino
	 *  https://www.instructables.com/id/TM1637-7-Segment-Display-Making-It-Work/**/ 
	
	
	public static CommToArd getInstance() throws UnsupportedBusNumberException, IOException {
		if (comm == null) {
			comm = new CommToArd();
		}
		return comm;
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
