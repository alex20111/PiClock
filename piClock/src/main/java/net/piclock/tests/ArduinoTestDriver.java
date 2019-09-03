package net.piclock.tests;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

import org.apache.commons.exec.ExecuteException;

import home.misc.Exec;
import net.piclock.arduino.ArduinoSerialCmd;

public class ArduinoTestDriver {

	public static void main(String args[]) throws InterruptedException, IOException {
    	System.out.println("Start");
        	
    	ArduinoSerialCmd s = ArduinoSerialCmd.getInstance();
    	
//    	s.open();
    	
    	Thread.sleep(2000);
    	System.out.println("Starting tests");
    	
//    	for(int i = 0 ; i < 9 ; i++) {
//    	System.out.println("Reading Ldr: " + s.readLdr());
//    	Thread.sleep(500);
//    	}
//    	
//    	System.out.println("Write time");
//    	s.writeTime("11:33");
//    	Thread.sleep(1000);
//    	System.out.println("Timr off");
//    	s.timeOff();
//    	Thread.sleep(1000);
//    	System.out.println("buzzer ON");
//    	s.buzzer(true);
//    	Thread.sleep(1000);
//    	System.out.println("buzzer off");
//    	s.buzzer(false);
//    	Thread.sleep(1000);
    	System.out.println("Mosfet on");
    	s.turnSpeakerOn();
//    	Thread.sleep(1000);
//    	System.out.println("Mosfet off");
//    	s.turnSpeakerOff();
    	Thread.sleep(1000);
    	System.out.println("Radio scan - this is blocking");
    	List<String> data = s.scanForFmChanels();
    	System.out.println("Data recieved: " + data);
    	Thread.sleep(1000);
//    	for(int i = 0 ; i < 10; i ++) {
    	System.out.println("Setting channel to 106.9");
    	s.radioSelectChannel(1069);
    	reRoutToSpeaker();
    	Thread.sleep(5000);
    	
//    	System.out.println("Setting channel to 89.9");
//    	s.radioSelectChannel(8990);
//    	Thread.sleep(5000);
//    	}
//    	System.out.println("Radio off");
//    	s.radioOff();
    	
    	
    	
    	
    	System.out.println("to finished press ctrl-c");
//    	while(true) {
//    		System.out.println("Reading Ldr: " + s.readLdr() + "   --- date: " + LocalTime.now());
//    		Thread.sleep(500);
//    	}
//    	
    	
    }
	
	private static void reRoutToSpeaker() throws ExecuteException, IOException {
		System.out.println("Start");
		Exec exec = new Exec();
		
		exec.addCommand("sudo");
		exec.addCommand("./piClock/scripts/play.sh");
		exec.timeout(5000);
		int ret = exec.run();
		
		System.out.println("END: " + ret);
		
		
//		arecord --buffer-time=1 - | aplay --buffer-time=1 -
		
	}

}
