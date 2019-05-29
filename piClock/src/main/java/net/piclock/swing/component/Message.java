package net.piclock.swing.component;

public class Message {

	private String propertyName = "";
	private Object message;
	
	public  Message(Object msg) {
		this.message = msg;
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
	
}
