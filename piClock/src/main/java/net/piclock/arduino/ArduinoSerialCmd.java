package net.piclock.arduino;


import com.pi4j.io.gpio.exception.UnsupportedBoardType;
import com.pi4j.io.serial.*;

import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.main.Constants;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.FormatStackTrace;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This example code demonstrates how to perform serial communications using the Raspberry Pi.
 *
 * @author Robert Savage
 */
public class ArduinoSerialCmd {
	
	private static final Logger logger = Logger.getLogger( ArduinoSerialCmd.class.getName() );

	private static ArduinoSerialCmd arduinoSerialCmd;
	private Serial serial;
	private SerialConfig config;
	
	private CmdTranslator translator;
	
	private List<ButtonChangeListener> btnListeners = new ArrayList<ButtonChangeListener>();
	
	private final BlockingQueue<Integer> ldrQueue =  new ArrayBlockingQueue<>(1);
	
	
	private ArduinoSerialCmd() throws UnsupportedBoardType, IOException, InterruptedException {
		serial = SerialFactory.createInstance();
		config = new SerialConfig();
		config.device("/dev/ttyUSB0")
        .baud(Baud._9600)
        .dataBits(DataBits._8)
        .parity(Parity.NONE)
        .stopBits(StopBits._1)
        .flowControl(FlowControl.NONE);
		
		translator = new CmdTranslator();
		
		startListener();
		
		open();
	}

	
	public void open() throws IOException {
		serial.open(config);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {}
	}
	
	public static ArduinoSerialCmd getInstance() throws UnsupportedBoardType, IOException, InterruptedException {
		
		if (arduinoSerialCmd == null) {
			synchronized (ArduinoSerialCmd.class) {
				if(arduinoSerialCmd == null) {
					arduinoSerialCmd = new ArduinoSerialCmd();
				}
			}
		}
		return arduinoSerialCmd;
	}
	
	public int readLdr() throws IllegalStateException, IOException, InterruptedException {
		sendCommand(translator.generateLDRCmd());
		int ldrVal = -1;
			try{
				ldrVal = ldrQueue.poll(4000, TimeUnit.MILLISECONDS); 
			}catch(Exception ex) {
				logger.log(Level.INFO, "LDR retrieve value timeout or null: ", ex);
			}
		return ldrVal;

		
	}
	public void writeTime(String time) throws IllegalStateException, IOException {
		String modTime = time.substring(0, 2) + time.substring(3, time.length());
		logger.log(Level.CONFIG, "Sending time: " + modTime);		
		sendCommand(translator.generateTimeCmd(modTime));
		
	}
	public void buzzer(boolean on) throws IllegalStateException, IOException {
		sendCommand(translator.generateBuzzerCmd(on));
	}
	public void turnSpeakerOn() throws IllegalStateException, IOException {
		sendCommand(translator.generateMosfetCmd(true));
		
	}
	public void turnSpeakerOff() throws IllegalStateException, IOException {
		sendCommand(translator.generateMosfetCmd(false));
	}

	public void timeOff() throws IllegalStateException, IOException {
		sendCommand(translator.generateTimeOffCmd());
	}
	public synchronized void  sendCommand(String command) throws IllegalStateException, IOException {		
		logger.log(Level.CONFIG, "Sending command: " + command);
		serial.write(command);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {}
	}
	public void addButtonListener(ButtonChangeListener btn) {
		btnListeners.add(btn);
	}
	public void removeButtonListener(ButtonChangeListener btn) {
		btnListeners.remove(btn);
	}
	
	private void startListener() {
		serial.addListener(new SerialDataEventListener() {
          @Override
          public void dataReceived(SerialDataEvent event) {

        	  try {
            	
//            	  System.out.println("Data retrieve form arduino: " + event.getAsciiString());
            	  
            	 Command cmd =  translator.translateReceivedCmd(event.getAsciiString());
            	  
            	  if (translator.isCommandComplete()) {
            		  if (cmd == Command.LDR) {
            			  ldrQueue.put(translator.getLdrValue());
            		  }else if (cmd == Command.BTN) {
            			  ButtonState state;
            			  if (translator.getButtonValue() == 1) {
            				  state = ButtonState.HIGH;
            			  }else {
            				  state = ButtonState.LOW;
            			  }
            			  fireBtnChangeEvent(state);
            		  }else if (cmd == Command.READY) {
            			  logger.log(Level.INFO, "!!! ARDUINO ready !!!");
            		  }
            	  }

              } catch (IOException  | InterruptedException e ) {
  				ErrorHandler eh = (ErrorHandler)SwingContext.getInstance().getSharedObject(Constants.ERROR_HANDLER);
  				eh.addError(ErrorType.ARDUINO, new ErrorInfo(new FormatStackTrace(e).getFormattedException()));
                  logger.log(Level.SEVERE, "Error in arduino listener", e);
              } 
			
          }
      });
	}
	
	private synchronized void fireBtnChangeEvent(ButtonState buttonState) {

		Iterator<ButtonChangeListener> listeners = btnListeners.iterator();
		while( listeners.hasNext() ) {
			ButtonChangeListener bl =  (ButtonChangeListener) listeners.next();			
				bl.stateChanged( buttonState );
			
		}
	}
	
	
    /**
     * This example program supports the following optional command arguments/options:
     *   "--device (device-path)"                   [DEFAULT: /dev/ttyAMA0]
     *   "--baud (baud-rate)"                       [DEFAULT: 38400]
     *   "--data-bits (5|6|7|8)"                    [DEFAULT: 8]
     *   "--parity (none|odd|even)"                 [DEFAULT: none]
     *   "--stop-bits (1|2)"                        [DEFAULT: 1]
     *   "--flow-control (none|hardware|software)"  [DEFAULT: none]
     *
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String args[]) throws InterruptedException, IOException {
    	System.out.println("Start");
    	
    	ArduinoSerialCmd s = ArduinoSerialCmd.getInstance();
    	
    	s.open();
    	
    	for(int i = 0 ; i < 9 ; i++) {
    	System.out.println("Reading Ldr: " + s.readLdr());
    	Thread.sleep(1000);
    	}
    	
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
//    	System.out.println("Mosfet on");
//    	s.turnSpeakerOn();
//    	Thread.sleep(1000);
//    	System.out.println("Mosfet off");
//    	s.turnSpeakerOff();
//    	Thread.sleep(1000);
    	
    	System.out.println("to finished press ctrl-c");
    	while(true) {
    		System.out.println("Reading Ldr: " + s.readLdr() + "   --- date: " + LocalTime.now());
    		Thread.sleep(500);
    	}
    	
    	
    }
//
//        // !! ATTENTION !!
//        // By default, the serial port is configured as a console port
//        // for interacting with the Linux OS shell.  If you want to use
//        // the serial port in a software program, you must disable the
//        // OS from using this port.
//        //
//        // Please see this blog article for instructions on how to disable
//        // the OS console for this port:
//        // https://www.cube-controls.com/2015/11/02/disable-serial-port-terminal-output-on-raspbian/
//
//        // create Pi4J console wrapper/helper
//        // (This is a utility class to abstract some of the boilerplate code)
//        final Console console = new Console();
//
//        // print program title/header
//        console.title("<-- The Pi4J Project -->", "Serial Communication Example");
//
//        // allow for user to exit program using CTRL-C
//        console.promptForExit();
//
//        // create an instance of the serial communications class
////        final Serial serial = SerialFactory.createInstance();
//
//        // create and register the serial data listener
//        serial.addListener(new SerialDataEventListener() {
//            @Override
//            public void dataReceived(SerialDataEvent event) {
//
//                // NOTE! - It is extremely important to read the data received from the
//                // serial port.  If it does not get read from the receive buffer, the
//                // buffer will continue to grow and consume memory.
//
//                // print out the data received to the console
//                try {
//                    console.println("[HEX DATA]   " + event.getHexByteString());
//                    console.println("[ASCII DATA] " + event.getAsciiString());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        try {
//            // create serial config object
//            SerialConfig config = new SerialConfig();
//
//            // set default serial settings (device, baud rate, flow control, etc)
//            //
//            // by default, use the DEFAULT com port on the Raspberry Pi (exposed on GPIO header)
//            // NOTE: this utility method will determine the default serial port for the
//            //       detected platform and board/model.  For all Raspberry Pi models
//            //       except the 3B, it will return "/dev/ttyAMA0".  For Raspberry Pi
//            //       model 3B may return "/dev/ttyS0" or "/dev/ttyAMA0" depending on
//            //       environment configuration.
//            config.device(SerialPort.getDefaultPort())
//                  .baud(Baud._38400)
//                  .dataBits(DataBits._8)
//                  .parity(Parity.NONE)
//                  .stopBits(StopBits._1)
//                  .flowControl(FlowControl.NONE);
//
//            // parse optional command argument options to override the default serial settings.
//            if(args.length > 0){
//                config = CommandArgumentParser.getSerialConfig(config, args);
//            }
//
//            // display connection details
//            console.box(" Connecting to: " + config.toString(),
//                    " We are sending ASCII data on the serial port every 1 second.",
//                    " Data received on serial port will be displayed below.");
//
//
//            // open the default serial device/port with the configuration settings
//            serial.open(config);
//
//            // continuous loop to keep the program running until the user terminates the program
//            while(console.isRunning()) {
//                try {
//                    // write a formatted string to the serial transmit buffer
//                    serial.write("CURRENT TIME: " + new Date().toString());
//
//                    // write a individual bytes to the serial transmit buffer
//                    serial.write((byte) 13);
//                    serial.write((byte) 10);
//
//                    // write a simple string to the serial transmit buffer
//                    serial.write("Second Line");
//
//                    // write a individual characters to the serial transmit buffer
//                    serial.write('\r');
//                    serial.write('\n');
//
//                    // write a string terminating with CR+LF to the serial transmit buffer
//                    serial.writeln("Third Line");
//                }
//                catch(IllegalStateException ex){
//                    ex.printStackTrace();
//                }
//
//                // wait 1 second before continuing
//                Thread.sleep(1000);
//            }
//
//        }
//        catch(IOException ex) {
//            console.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
//            return;
//        }
//    }
}

// END SNIPPET: serial-snippet

