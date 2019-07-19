package net.piclock.bean;

import java.time.LocalDateTime;

public class ErrorInfo {

	
	private String errorMessage = "";
	private int errorCount = 0;
	private LocalDateTime date;
	private ErrorType type;
	
	public ErrorInfo(String errorMessage) {
		this.errorMessage = errorMessage;
		date  =  LocalDateTime.now();
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public ErrorType getType() {
		return type;
	}

	public void setType(ErrorType type) {
		this.type = type;
	}

	
	
}
