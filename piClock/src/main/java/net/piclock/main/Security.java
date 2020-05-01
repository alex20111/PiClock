package net.piclock.main;


import javax.swing.JOptionPane;

import net.piclock.swing.component.KeyBoard;
import net.piclock.swing.component.SwingContext;

public class Security {
	
	
	public boolean isSettingsPassProtected() {
		Preferences pref = (Preferences)SwingContext.getInstance().getSharedObject(Constants.PREFERENCES);
		
		return pref.isSettingPassProtected();
	}


	
	public boolean validateSettingsAccess() {
		boolean canAccess = true;
		Preferences prefs = (Preferences)SwingContext.getInstance().getSharedObject(Constants.PREFERENCES);

		if (prefs != null ) {
			
			if (prefs.isSettingPassProtected()) {
				
				KeyBoard keyboard = new KeyBoard(false);  
				keyboard.setVisible(true);
				String password = keyboard.getText().trim();

				if (prefs.getSettingsPassword() != null && prefs.getSettingsPassword().equals(password)) {
					canAccess = true;
				}else {
					JOptionPane.showMessageDialog( null, "Access denied", "Denied", JOptionPane.ERROR_MESSAGE);							
					canAccess = false;
				}
			}
		}

		return canAccess;
	}
}
