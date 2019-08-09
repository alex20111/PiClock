package net.piclock.arduino;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;



public class ArduinoCmdBackup {
	private static final Logger logger = Logger.getLogger( ArduinoCmdBackup.class.getName() );
	
	private static ArduinoCmdBackup arduinoCmd;
	private I2CBus i2c;
	private I2CDevice device;
	
	private Commands prevCmd = Commands.NONE;	
	
	private List<ButtonChangeListener> btnListener = new ArrayList<ButtonChangeListener>();
//	private ButtonMonitor btnMon;
	
	public static ArduinoCmdBackup getInstance() throws UnsupportedBusNumberException, IOException {
		if (arduinoCmd == null) {
			synchronized (ArduinoCmdBackup.class) {
				if(arduinoCmd == null) {
					arduinoCmd = new ArduinoCmdBackup();
				}
			}
		}
		return arduinoCmd;
	}
	
	private ArduinoCmdBackup() throws UnsupportedBusNumberException, IOException {
		logger.log(Level.CONFIG,"Initi arduino");
		 i2c = I2CFactory.getInstance(I2CBus.BUS_3);
		device = i2c.getDevice(0x08);
	}
	public int readLDR() throws IOException, InterruptedException  {
		return sendCommand(Commands.LDR);
	}
	public int readButtonA() throws IOException, InterruptedException {
		return sendCommand(Commands.BTN_A);
	}
	public void writeTime(String time) throws IOException, InterruptedException {
		logger.log(Level.CONFIG, "write Time: " + time);
		byte[] commBuffer = new byte[3];
		
		int hours = Integer.parseInt(time.substring(0, 2));
		int minutes = Integer.parseInt(time.substring(3, time.length()));		
		System.out.println("H: " + hours + " min " + minutes);
		commBuffer[0] = (byte)Commands.DSP_TIME.getCommand(); //for time request.. 'b' will be for buzzer requestt
		commBuffer[1] = (byte)hours;
		commBuffer[2] = (byte)minutes;
		sendCommand(Commands.DSP_TIME,commBuffer);
	}
	public void timeOff() throws IOException, InterruptedException {
		logger.log(Level.CONFIG, "time Off");
		byte[] commBuffer = new byte[1];

		commBuffer[0] = (byte)Commands.TIME_OFF.getCommand(); //for time request.. 'b' will be for buzzer request
		sendCommand(Commands.TIME_OFF,commBuffer);
	}
	public void buzzer(boolean on) throws IOException, InterruptedException {
		byte[] commBuffer = new byte[3];

		commBuffer[0] = (byte)Commands.BUZZER.getCommand();
		if (on) {
			//commBuffer[0] = 'b'; // 'b' will be for buzzer request. 01 = on = 02 = off
			commBuffer[1] = (byte)0;
			commBuffer[2] = (byte)1;
		}else {
			//commBuffer[0] = 'b'; // 'b' will be for buzzer request. 01 = on = 02 = off
			commBuffer[1] = (byte)0;
			commBuffer[2] = (byte)2;			
		}

		sendCommand(Commands.BUZZER,commBuffer);
	}
	public void turnSpeakerOn() throws InterruptedException, IOException {
		logger.log(Level.CONFIG, "turnSpeakerOn");
		byte[] commBuffer = new byte[2];

		commBuffer[0] = (byte)Commands.SPEAKER.getCommand();
		commBuffer[1] = (byte)1;
		sendCommand(Commands.SPEAKER,commBuffer);
	}
	public void turnSpeakerOff() throws InterruptedException, IOException {
		logger.log(Level.CONFIG, "turnSpeakerOff");
		byte[] commBuffer = new byte[2];

		commBuffer[0] = (byte)Commands.SPEAKER.getCommand();
		commBuffer[1] = (byte)2;
		sendCommand(Commands.SPEAKER,commBuffer);
	}
	/**
	 * Monitor button. you must register a listener event to listen to the button events.
	 * example: addButtonlistenet(new ButtonChangeListener(){
	 * 		public void stateChanged( ButtonState event ){
	 * 				System.out.println("event: " + event);
	 * 		}
	 * });
	 * @throws ListenerNotFoundException
	 * @throws UnsupportedBusNumberException
	 * @throws IOException
	 */
	public void startBtnMonitoring() throws ListenerNotFoundException, UnsupportedBusNumberException, IOException{
		logger.log(Level.CONFIG, "startBtnMonitoring" );
		if (btnListener.size() == 0){
			throw new ListenerNotFoundException("Listener not found, add listener");
		}		

//		if (btnMon != null && btnMon.isRunning()) {
//			logger.log(Level.CONFIG, "Btn monitor already started");
//		}else {
//			btnMon = new ButtonMonitor(btnListener, 200);
//			btnMon.start();
//		}
	}

	public void addButtonListener(ButtonChangeListener bsl){
		logger.log(Level.CONFIG,"Add button listener");
		btnListener.add(bsl);
	}
	public void clearButtonListeners() {
		logger.log(Level.CONFIG, "ClearButtonListener");
		btnListener.clear();
	}
	
	public void stopBtnMonitor() throws InterruptedException {
		logger.log(Level.CONFIG, "stopBtnMonitor");
//		if (btnMon != null) {
//			if (anyActiveBtnListener()) {
//				logger.log(Level.CONFIG, "There is still active listeners");
//			}else {
////				btnMon.stop();
//			}
//		}
	}
	
	public boolean isBtnMonitorRunning() {
//		return btnMon != null && btnMon.isRunning();
		return true;
	}

	private synchronized int sendCommand(Commands command) throws InterruptedException, IOException{
		return sendCommand(command,  null);
	}
	private synchronized int sendCommand(Commands command, byte[] byteVal) throws InterruptedException, IOException{
		
		if (command.isReturnVal()  ){ //set the command 1st to expect a retuen value
			if (command != prevCmd){
				prevCmd = command;
				device.write((byte)command.getCommand());
				Thread.sleep(50);
			}
			return device.read();
		}else if (command.isWriteOnly()){
			prevCmd = command;
			device.write(byteVal);
		}		
		return -1;
	}
	private boolean anyActiveBtnListener() {
		if(this.btnListener.size() > 0) {
			for(ButtonChangeListener l : btnListener) {
//				if(l.isActive()) {
					return true;
//				}
			}
		}
			return false;
		
	}
	enum Commands{
		LDR('l'), DSP_TIME('t'), TIME_OFF('o'), BTN_A('a'), BUZZER('b'), SPEAKER('m'), NONE('-');
		
		private char cmdName;
		
		private Commands(char cmdName){
			this.cmdName = cmdName;
		}
		public char getCommand(){
			return cmdName;
		}
		
		public boolean isReturnVal(){
			return this == LDR || this == BTN_A;
		}
		public boolean isWriteOnly(){
			return this == DSP_TIME || this == TIME_OFF || this == BUZZER || this == SPEAKER;
		}		
	}
}
