package net.piclock.handlers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import home.misc.Exec;
import net.piclock.enums.ClockType;
import net.piclock.nativeImpl.AdafruitLEDBackpack;

public class ClockHandler {
	private static final Logger logger = Logger.getLogger( ClockHandler.class.getName() );


	private boolean clockOn = false;
	private Thread clockThread;


	private ClockType clock;

	private AdafruitLEDBackpack ht16k33Led ;

	public ClockHandler(I2CBus bus, ClockType type) {
		
		this.clock = type;

		if (type == ClockType.HT16K33_PI) {
			try {
				ht16k33Led = new AdafruitLEDBackpack(bus, 0x70);
			} catch (IOException e) {
				logger.log(Level.SEVERE,  "error in ht16k33 clock", e);
			} catch (UnsupportedBusNumberException e) {
				logger.log(Level.SEVERE,  "error in ht16k33 clock", e);
			}
		}
	}


	public void writeTime(String time) {
		if (clock == ClockType.TM1637_PI && !clockOn) {
			writeTM1637time();
		}else if (clock == ClockType.HT16K33_PI) {
			writeHT16kTime(time);
		}
	}
	public void turnClockOff() throws IOException {
		if (clock == ClockType.TM1637_PI) {
			tm1637off();
		}else if (clock == ClockType.HT16K33_PI) {
			turnHt16k33Off();
		}

	}

	private void writeHT16kTime(String time) {
		
		logger.log(Level.CONFIG, "writeHT16kTime()  clock On? -> " + clockOn + "  time: " + time);

		String modTime = time.substring(0, 2) + time.substring(3, time.length());

		if (!clockOn) {
			ht16k33Led.displayDoubleDots(true);
			ht16k33Led.setBrightness(1);
			clockOn = true;
		}

		ht16k33Led.display(modTime);


	}
	private void writeTM1637time() {
		logger.log(Level.CONFIG, "writeTM1637time() -> " + clockOn);

		if (clockThread == null || !clockThread.isAlive()) {
			clockThread = new Thread(new Runnable() {

				@Override
				public void run() {
					logger.log(Level.CONFIG, "Starting clock thread");
					int ret = 99;


					try {
						Exec exec = new Exec();

						exec.addCommand("sudo");
						exec.addCommand("./scripts/clock.sh");
						clockOn = true;
						ret = exec.run();

					}catch(Exception ex) {
						logger.log(Level.INFO, "Exception in clockThread", ex);
						clockOn = false;
					}


					logger.log(Level.CONFIG, "Clock thread finished. " + ret);
				}
			});

			clockThread.start();
		}else {
			clockOn = true;
		}

	}
	private void tm1637off() {
		logger.config("tm1637off. is Clock thread alive: " + (clockThread != null ? clockThread.isAlive() : "False"));


		if (clockThread != null && clockThread.isAlive()) {

			try {
				//1st kill the process
				Exec exec = new Exec();

				exec.addCommand("sudo");
				exec.addCommand("./scripts/killClock.sh");
				clockOn = true;
				int ret = exec.run();

				clockOn = false;
				logger.config("END CLOCK OFF. -> Ret: " + ret + " out: " + exec.getOutput());
			}catch(Exception ex) {
				logger.log(Level.SEVERE, "Exception clock off", ex);
			}
		}
	}

	private void turnHt16k33Off() throws IOException {
		logger.config("turnHt16k33Off. is Clock On? : " + clockOn);
		ht16k33Led.displayClear();
		clockOn = false;
	}



}
