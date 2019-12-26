package net.piclock.swing.component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Message {
	
	private String propertyName = "";

	private List messageList;
	private LocalDateTime dateTime;

	public  Message(List  messageList) {
		this.messageList = messageList;
		dateTime = LocalDateTime.now();
	}
	public  Message(Object... messageList ) {
		this.messageList = Arrays.asList(messageList);
		dateTime = LocalDateTime.now();
	}	
	public  Message(Object messageObject) {
		this.messageList = new ArrayList();
		this.messageList.add(messageObject);
		dateTime = LocalDateTime.now();
	}	
	public  Message() {
		dateTime = LocalDateTime.now();
	}
	public void addStringToMessageList(String string){
		if (messageList == null){
			messageList = new ArrayList<>();
		}
		messageList.add(string);
	}
	
	public void addIntToMessageList(int nbr){
		if (messageList == null){
			messageList = new ArrayList<>();
		}
		messageList.add(nbr);
	}
	
	public Object getMessagePerIndex(int indx){
		return messageList.get(indx);
	}
	
	public int getMessageSize(){
		return messageList.size();
	}
	
	public Object getFirstMessage() {
		return messageList.get(0);
	}
	
	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public List getMessageList() {
		return messageList;
	}
	public void setMessageList(List messageList) {
		this.messageList = messageList;
	}
	public LocalDateTime getDateTime() {
		return dateTime;
	}
	
	@Override
	public String toString() {
		return "Message [propertyName=" + propertyName + ", messageList=" + messageList + "]";
	}
	

//	private String propertyName = "";
//	private Object message;
//	private LocalDateTime dateTime;
//	
//	public  Message(Object msg) {
//		this.message = msg;
//		dateTime = LocalDateTime.now();
//	}
//
//	public Object getMessage() {
//		return message;
//	}
//
//	public void setMessage(Object message) {
//		this.message = message;
//	}
//
//	public String getPropertyName() {
//		return propertyName;
//	}
//
//	public void setPropertyName(String propertyName) {
//		this.propertyName = propertyName;
//	}
//
//	public LocalDateTime getDateTime() {
//		return dateTime;
//	}
	
}
