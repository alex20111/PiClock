package net.piclock.bean;

import java.util.HashMap;
import java.util.Map;

public class ErrorHandler {

	private Map<ErrorType, Error> errorMap;
	
	public ErrorHandler() {
		errorMap = new HashMap<>();
	}

	public Map<ErrorType, Error> getErrorMap() {
		return errorMap;
	}

	public void setErrorMap(Map<ErrorType, Error> errorMap) {
		this.errorMap = errorMap;
	}
}
