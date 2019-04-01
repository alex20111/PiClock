package net.piclock.swing.component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

public class SwingContext {

	/** Singleton instance of context */
	private static SwingContext swingContext = new SwingContext();
	/** PropertyChangeSupport */
	private PropertyChangeSupport propertyChangeSupport = 	new PropertyChangeSupport(this);

	@SuppressWarnings("rawtypes")
	private Map shareableDataMap = null;

	public SwingContext(){
		shareableDataMap = new HashMap();
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
}