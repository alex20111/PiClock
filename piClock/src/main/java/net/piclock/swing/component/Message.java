package net.piclock.swing.component;

import java.time.LocalDateTime;

public class Message {

	private String propertyName = "";
	private Object message;
	private LocalDateTime dateTime;
	
	public  Message(Object msg) {
		this.message = msg;
		dateTime = LocalDateTime.now();
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}
	
}
