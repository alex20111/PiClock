package net.piclock.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.piclock.main.Constants;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.SwingContext;

public class ErrorHandler {

	private Map<ErrorType, List<ErrorInfo>> errorMap;
	private int maxErrorPerType = 10; //number of maximum errors before resetting.
	
	public ErrorHandler() {
		errorMap = new HashMap<>();
	}
	
	
	public void addError(ErrorType type, ErrorInfo errorMessage) {
		List<ErrorInfo> errors = errorMap.get(type);
		
		if (errors == null) {
			errors = new ArrayList<>();
		}
		
		errorMessage.setType(type);
		
		errors.add(errorMessage);
		
		errorMap.put(type, errors);
		
		broadcastOn();
	}
	
	//it will read the errors, send them to the log and then remove them
	public String readErrors() {
		StringBuilder sb = new StringBuilder();
		if (errorMap != null) {
			errorMap.entrySet().stream()
			  .forEach(e -> {
				  sb.append("Error type: " + e.getKey()  + " \n");			       
			       e.getValue().stream().forEach(er -> {
			    	   sb.append("Error: " + er.getErrorMessage());
			       });
			  }
			  );
		}
		return sb.toString();
	}
	
	private void broadcastOn() {
		SwingContext sc = SwingContext.getInstance();
		
		sc.sendMessage(Constants.ERROR_HANDLER, new Message(true));
	}
	
	public void broadcastOff() {
		SwingContext sc = SwingContext.getInstance();
		
		sc.sendMessage(Constants.ERROR_HANDLER, new Message(false));
	}
	public List<ErrorInfo> getErrorAsList(){
		List<ErrorInfo> ers = new ArrayList<>();
		
		errorMap.entrySet().stream()
		  .forEach(e -> {   
		       e.getValue().stream().forEach(er -> {
		    	  ers.add(er);
		       });
		  }
		  );
		
		return ers;
		
	}
	/**
	 * This will clear the errors and dump them into a file for future reference
	 */
	public void clearErrors(){
		//TODO write it to file
		errorMap.clear();
	}
	public Map<ErrorType, List<ErrorInfo>> getErrorMap() {
		return errorMap;
	}

	public void setErrorMap(Map<ErrorType, List<ErrorInfo>> errorMap) {
		this.errorMap = errorMap;
	}
}
