package net.piclock.swing.component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SwingContext {

	private static final Logger logger = Logger.getLogger( SwingContext.class.getName() );

	/** Singleton instance of context */
	private static SwingContext swingContext = new SwingContext();
	/** PropertyChangeSupport */
	private PropertyChangeSupport propertyChangeSupport = 	new PropertyChangeSupport(this);

	private List<MessageListener> msgListeners = new ArrayList<>();
	private Map<String,Set<MessageListener>> msgListenersMap = new WeakHashMap<>();

	@SuppressWarnings("rawtypes")
	private Map shareableDataMap = null;

	public SwingContext(){
		shareableDataMap = new WeakHashMap();
	}

	public static SwingContext getInstance() {
		return swingContext;
	}

	/** returns replaced object. null if no previous value */
	public Object putSharedObject(String key, Object value)  {
		Object oldValue = getSharedObject(key);

		Object removedObject = shareableDataMap.put(key, value);

		firePropertyChange(key, oldValue, value, this);
		return removedObject;
	}

	public Object getSharedObject(String key) {
		return shareableDataMap.get(key);
	}

	private void firePropertyChange(String propertyName, Object oldValue, Object newValue, Object source) {
		if (propertyName == null)
			throw new IllegalArgumentException("No property name specified.");

		PropertyChangeEvent e = new PropertyChangeEvent(source, propertyName, oldValue, newValue);
		propertyChangeSupport.firePropertyChange(e);


	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.addPropertyChangeListener(l);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, l);
	}


	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.removePropertyChangeListener(l);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
		propertyChangeSupport.removePropertyChangeListener(propertyName, l);
	}

	public void addMessageChangeListener(String propertyName, MessageListener l) {	

		try {
			Set<MessageListener> msg = msgListenersMap.get(propertyName);

			if (msg != null) {
				msg.add(l);
			}else {
				msg = new HashSet<>();
				msg.add(l);
			}
			logger.log(Level.CONFIG,"SWING CONTEXT -  PROPERTY: " + propertyName +  " size: " + msg.size() + " l: " + l  );
			msgListenersMap.put(propertyName,msg);

			logger.log(Level.CONFIG,"END - SWING CONTEXT -  PROPERTY: " + propertyName +  " size: " + msg.size() + " l: " + l  );
		}catch (Exception ex) {
			logger.log(Level.SEVERE, "Error " , ex);
		}
	}
	public void addMessageChangeListener(MessageListener l) {
		msgListeners.add(l);
	}

	public void removeMessageListener(String propertyName , MessageListener l) {
	
		try {

			logger.log(Level.CONFIG, "remove property: " + propertyName + "   l: " + l);
			Set<MessageListener> msg = msgListenersMap.get(propertyName);
			if (msg != null) {
				msg.remove(l);
			}
		}catch (Exception ex) {
			logger.log(Level.SEVERE, "Error " , ex);
		}

	}
	public void removeMessageListener(MessageListener l) {		
		msgListeners.remove(l);	
	}
	public void sendMessage(Message message){
		for(MessageListener m : msgListeners) {			
			m.message(message);
		}
	}
	public void sendMessage(String propertyName, Message message){
	
		try {
			Set<MessageListener> m = msgListenersMap.get(propertyName);		


			if (m != null) {
				logger.log(Level.CONFIG, "Message: " + propertyName + "  Message size: " + m.size() );

				for(MessageListener mfor : m) {
					message.setPropertyName(propertyName);
					mfor.message(message);
				}
			}else {
				logger.log(Level.CONFIG,"Message not found");
			}
		}catch (Exception ex) {
			logger.log(Level.SEVERE, "Error " , ex);
		}
	}


}