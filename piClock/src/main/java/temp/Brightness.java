package temp;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;

public class Brightness {

	
	public static void main(String args[]) throws InterruptedException {
		
		System.out.println("Starting");
		Gpio.wiringPiSetup();
        SoftPwm.softPwmCreate(24, 0, 100);
		
//        0 - 3 Backlight off
//        5 - 30 Various degrees of brightness
//        91 - 1023 Full brightness

		System.out.println("Set PWM to 20");
        // optionally set the PWM range (100 is default range)
		 SoftPwm.softPwmWrite(24, 20);
		
		Thread.sleep(2000);
		for (int i = 0; i < 90 ; i++) {
			System.out.println("brightness : " + i);
        // optionally set the PWM range (100 is default range)
		 SoftPwm.softPwmWrite(24, i);
		 Thread.sleep(500);
		 }
		
		Thread.sleep(2000);
		System.out.println("Set PWM to 90");
        // optionally set the PWM range (100 is default range)
		 SoftPwm.softPwmWrite(24, 90);
		
		Thread.sleep(2000);
		System.out.println("Set PWM to 300");
        // optionally set the PWM range (100 is default range)
		 SoftPwm.softPwmWrite(24, 300);
		
		Thread.sleep(2000);
		
		System.out.println("Set PWM to 1023");
        // optionally set the PWM range (100 is default range)
		 SoftPwm.softPwmWrite(24, 1023);
		
		Thread.sleep(2000);
		

	}
	
	
}
