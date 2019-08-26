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
	private ErrorHandler eh;
	
	private CmdTranslator translator;
	
	private List<ButtonChangeListener> btnListeners = new ArrayList<ButtonChangeListener>();
	
	private final BlockingQueue<Integer> ldrQueue =  new ArrayBlockingQueue<>(1);
	private final BlockingQueue<List<String>> scanQueue =  new ArrayBlockingQueue<>(1);
	
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
		
		 eh = new ErrorHandler();
			
		eh = (ErrorHandler) SwingContext.getInstance().getSharedObject(Constants.ERROR_HANDLER);	
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
				String fmtEx = new FormatStackTrace(ex).getFormattedException();
				eh.addError(ErrorType.ARDUINO, new ErrorInfo(fmtEx));
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
	public List<String> scanForFmChanels() throws IllegalStateException, IOException, InterruptedException {
		System.out.println("Scanning");
		if (translator.getRadioScanStatus() == RadioScan.STARTED) {
			throw new IllegalStateException("Scan is still running, cannot initiate an other scan");
		}
		sendCommand(translator.generateScanFM());
		
		List<String> data = new ArrayList<>();
		try {
			data = scanQueue.poll(30, TimeUnit.SECONDS);
		}catch(Exception ex) {
			String fmtEx = new FormatStackTrace(ex).getFormattedException();
			eh.addError(ErrorType.ARDUINO, new ErrorInfo(fmtEx));
			translator.setScan(RadioScan.NONE);
			logger.log(Level.INFO, "scan station retrieve value timeout or null: ", ex);
		}
		
		return data;
		
	}
	/**
	 * Turn radio off
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public void radioOff() throws IllegalStateException, IOException {
		setRadioChannel(-1, false);
	}
	/**
	 * select a radio channel
	 * @param channel
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public void radioSelectChannel(int channel) throws IllegalStateException, IOException {
		setRadioChannel(channel, true);
	}
	private  void setRadioChannel(int channel, boolean on ) throws IllegalStateException, IOException {
		sendCommand(translator.generateSelectChannelCmd(String.valueOf(channel), on));
	}
	public synchronized void  sendCommand(String command) throws IllegalStateException, IOException {		
		logger.log(Level.CONFIG, "Sending command: " + command);
		System.out.println("Sending command: " + command);
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
	
	private synchronized void startListener() {
		serial.addListener(new SerialDataEventListener() {
          @Override
          public void dataReceived(SerialDataEvent event) {

        	  try {
            	
            	  System.out.println("Data retrieve form arduino: " + event.getAsciiString());
            	  
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
            		  }else if (cmd == Command.SCAN_RADIO && translator.getRadioScanStatus() ==  RadioScan.FINISHED) {
            			  
            			  translator.setScan(RadioScan.NONE);
//            			System.out.println("Station list: " + translator.getStations());
            			scanQueue.put(translator.getStations());
            		  }
            	  }

              } catch (IOException  | InterruptedException e ) {
            	  String fmtEx = new FormatStackTrace(e).getFormattedException();
  				eh.addError(ErrorType.ARDUINO, new ErrorInfo(fmtEx));
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
}
