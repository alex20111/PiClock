package net.piclock.arduino;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;


public class ArduinoCmd {
	
	private static ArduinoCmd arduinoCmd;
	private I2CBus i2c;
	private I2CDevice device;
	
	private Commands prevCmd = Commands.NONE;	
	private Thread monitorBtnThrd;	
	
	public static ArduinoCmd getInstance() throws UnsupportedBusNumberException, IOException {
		if (arduinoCmd == null) {
			synchronized (ArduinoCmd.class) {
				if(arduinoCmd == null) {
					arduinoCmd = new ArduinoCmd();
				}
			}
		}
		return arduinoCmd;
	}
	
	private ArduinoCmd() throws UnsupportedBusNumberException, IOException {
		System.out.println("init arduino cmd");
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
		byte[] commBuffer = new byte[3];
		
		int hours = Integer.parseInt(time.substring(0, 2));
		int minutes = Integer.parseInt(time.substring(3, time.length()));		
		System.out.println("H: " + hours + " min " + minutes);
		commBuffer[0] = (byte)Commands.DSP_TIME.getCommand(); //for time request.. 'b' will be for buzzer requestt
		commBuffer[1] = (byte)hours;
		commBuffer[2] = (byte)minutes;
		sendCommand(Commands.DSP_TIME,null,commBuffer);
	}
	public void timeOff() throws IOException, InterruptedException {
		byte[] commBuffer = new byte[1];

		commBuffer[0] = (byte)Commands.TIME_OFF.getCommand(); //for time request.. 'b' will be for buzzer request
		sendCommand(Commands.TIME_OFF,null,commBuffer);
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

		sendCommand(Commands.BUZZER,null,commBuffer);
	}
	public void monitorBtn(){		
		monitorBtnThrd = new Thread(new Runnable(){
			private int prevBtnStatus = 0;
			@Override
			public void run() {
				try {
					while(true){
						int btnStatus = readButtonA();
						if (btnStatus != prevBtnStatus){
							prevBtnStatus = btnStatus;
							System.out.println("Status changed");
						}
						Thread.sleep(200);
					}
				} catch (InterruptedException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}			 
		 });
		monitorBtnThrd.start();
	}
	public void stopBtnMon() throws InterruptedException{
		if (monitorBtnThrd != null && monitorBtnThrd.isAlive()){
			monitorBtnThrd.interrupt(); 
			while(monitorBtnThrd.isAlive()){ 
				monitorBtnThrd.join(100); 
			} 
		}
	}
//	private synchronized I2CDevice getDevice() {
//		return device;
//	}
	private synchronized int sendCommand(Commands command) throws InterruptedException, IOException{
		return sendCommand(command, null, null);
	}
	private synchronized int sendCommand(Commands command, String value, byte[] byteVal) throws InterruptedException, IOException{
		
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
	enum Commands{
		LDR('l'), DSP_TIME('t'), TIME_OFF('o'), BTN_A('a'), BUZZER('b'), NONE('-');
		
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
			return this == DSP_TIME || this == TIME_OFF || this == BUZZER;
		}		
	}
}
