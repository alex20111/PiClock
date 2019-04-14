package net.piclock.arduino;

public class ListenerNotFoundException extends Exception{
	
	private static final long serialVersionUID = 1L;
	
	public ListenerNotFoundException(String message){
		super(message);
	}
}