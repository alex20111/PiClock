package net.piclock.arduino;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CmdTranslator {

	private static final Logger logger = Logger.getLogger( CmdTranslator.class.getName() );
	
	private final String START_CHAR = "<";
	private final String STOP_CHAR = ">";
	private final int ON = 8; //on command
	private final int OFF = 9; //off command
	
	private int ldr 		= -1;
	private int btnStatus 	= -1;	
	
	//temp storage
	private StringBuilder tempStorage = new StringBuilder();
	
	private boolean commandComplete = false; //used to verify if we received the full validated command
	private boolean receiverReady = false;
	
	public CmdTranslator(){}
		
	public synchronized Command translateReceivedCmd(String receivedCmd) throws IllegalArgumentException{		
		
		commandComplete = false;
		
		Command cmd = Command.NONE;
		logger.log(Level.CONFIG, "receivedCmd: " + receivedCmd + ". TempStorage: " + (tempStorage != null? tempStorage.toString(): "Empty" ) );
		
		if (receivedCmd.startsWith(START_CHAR) && receivedCmd.endsWith(STOP_CHAR)){
			if (tempStorage != null && tempStorage.length() > 0){ //reset if we have anything in the temp
				logger.log(Level.CONFIG,"resetting temp storage: " + tempStorage);
				tempStorage = new StringBuilder();
			}
			cmd = populateFromCommand(receivedCmd);			
		}else if(receivedCmd.startsWith(START_CHAR) && !receivedCmd.endsWith(STOP_CHAR)){
			//this means that we received the start but not the end tag. Store it into the temp variable
			logger.log(Level.CONFIG,"receivedCmd partial serial info.. ");
			
			if (tempStorage != null && tempStorage.length() > 0){
				throw new IllegalArgumentException("Problem, temp storage not null. It should be empty. receivedCmd: " + receivedCmd + " - trempStorage: " + tempStorage);
			}
			tempStorage.append(receivedCmd);
		}else if (!receivedCmd.startsWith(START_CHAR) && receivedCmd.endsWith(STOP_CHAR)){
			logger.log(Level.CONFIG,"receivedCmd end of partial serial info.. ");
			tempStorage.append(receivedCmd);
			cmd = populateFromCommand(tempStorage.toString());	
			tempStorage = new StringBuilder();
		}else{			
			throw new IllegalArgumentException("Problem processing received command. receivedCmd: " + receivedCmd);
		}
		
		return cmd;
	}
	public boolean isCommandComplete(){
		return commandComplete;
	}
	
	public int getLdrValue(){
		return ldr;
	}
	
	public int getButtonValue(){
		return btnStatus;
	}
	
	/// Generate commands	
	public String generateLDRCmd(){
		StringBuilder cmd = new StringBuilder();
		cmd.append(START_CHAR);
		cmd.append(Command.LDR.getCmd());
//		cmd.append((turnOn ? ON : OFF ));//maybe
		cmd.append(STOP_CHAR);		
		
		return cmd.toString();		
	}
	
	public String generateTimeCmd(String time){
		StringBuilder cmd = new StringBuilder();
		cmd.append(START_CHAR);
		cmd.append(Command.TIME.getCmd());
		cmd.append(time);
		cmd.append(STOP_CHAR);
		return cmd.toString();
	}
	public String generateTimeOffCmd(){
		StringBuilder cmd = new StringBuilder();
		cmd.append(START_CHAR);
		cmd.append(Command.TIME_OFF.getCmd());
		cmd.append(OFF);
		cmd.append(STOP_CHAR);
		
		return cmd.toString();
	}
	public String generateTimeBrightnessCmd(int level){
		StringBuilder cmd = new StringBuilder();
		cmd.append(START_CHAR);
		cmd.append(Command.TIME_BRIGHTNESS.getCmd());
		cmd.append(level);
		cmd.append(STOP_CHAR);
		
		return cmd.toString();
	}
	
	public String generateMosfetCmd(boolean on){
		StringBuilder cmd = new StringBuilder();
		cmd.append(START_CHAR);
		cmd.append(Command.MOSFET.getCmd());
		cmd.append( (on ? ON : OFF) );
		cmd.append(STOP_CHAR);
		
		return cmd.toString();
	}
	
	public String generateBuzzerCmd(boolean on){
		StringBuilder cmd = new StringBuilder();
		cmd.append(START_CHAR);
		cmd.append(Command.BUZZER.getCmd());
		cmd.append( (on ? ON : OFF) );
		cmd.append(STOP_CHAR);
		
		return cmd.toString();
	}
	private Command populateFromCommand(String receivedCmd){
		
		commandComplete = true;
		//rest var
		ldr 		= -1;
		btnStatus 	= -1;
		
		Command cmd = Command.value(String.valueOf(receivedCmd.charAt(1)));
		
		if (cmd == Command.LDR){
			ldr = Integer.parseInt(receivedCmd.substring(2, receivedCmd.indexOf(">") ));

		}else if (cmd == Command.BTN){
			btnStatus = Integer.parseInt(receivedCmd.substring(2, receivedCmd.indexOf(">") ));

		}else if(cmd == Command.NONE){
			//verify if it's the ready command
			String ready = receivedCmd.substring(2, receivedCmd.indexOf(">"));
			if (("ready").equals(ready)){
				receiverReady = true;
				cmd = Command.READY;
			}
			
		}
		return cmd;
	}

	public boolean isReceiverReady() {
		return receiverReady;
	}
		
	
	
	
}