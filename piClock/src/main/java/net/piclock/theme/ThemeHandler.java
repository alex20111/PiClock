package net.piclock.theme;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import net.piclock.enums.BackgroundEnum;
import net.piclock.enums.DayNightCycle;
import net.piclock.enums.IconEnum;
import net.piclock.enums.LabelEnums;
import net.piclock.main.Constants;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.LoadThemesFromXml;

public class ThemeHandler {
	private static final Logger logger = Logger.getLogger( ThemeHandler.class.getName() );
	
	private Map<ThemeEnum, List<BackgroundTheme>> themesMap;
	private ThemeEnum currentTheme;
	private BackgroundTheme currBackground;

	private SwingContext ct = SwingContext.getInstance();
	
	//registered labels
	private Map<LabelEnums, JLabel> registeredLabels = new HashMap<LabelEnums, JLabel>();
	private Map<IconEnum, Object> registeredIcon = new HashMap<IconEnum, Object>();
	
	private boolean init = true;

	public ThemeHandler() throws Exception{
		//init Themes
		themesMap = LoadThemesFromXml.loadThemeFromXml();
//		logger.log(Level.CONFIG, "theme map: " + themesMap);
	}

	public void loadTheme(ThemeEnum theme){

		currentTheme = theme;
		
		List<BackgroundTheme> back = themesMap.get(currentTheme);
		for(BackgroundTheme bt : back){
			if (bt.getName() == BackgroundEnum.SUNNY){
				currBackground = bt;
				break;
			}
		}		
	}

	public void loadRainBackdrop(){		
		logger.log(Level.CONFIG, "loadRainBackdrop(). Current: " + currBackground.getName());
		
		//load only when it's not the same
		if (currBackground.getName() != BackgroundEnum.RAIN){

			List<BackgroundTheme> back = themesMap.get(currentTheme);		

			for(BackgroundTheme bt : back){
				if (bt.getName() == BackgroundEnum.RAIN){				
					DayNightCycle cycle = (DayNightCycle)ct.getSharedObject(Constants.DAY_NIGHT_CYCLE);
					if (cycle == null){
						cycle = DayNightCycle.DAY;
					}
					bt.setCycle(cycle);
					ct.putSharedObject(Constants.THEMES_BACKGROUND_IMG_UPDATE, bt.fullPathBackImg());
					currBackground = bt;
					refreshTheme();
					break;
				}
			}
		}
	}
	public void loadSunnyBackdrop(){
		logger.log(Level.CONFIG, "loadSunnyBackdrop(). Current: " + currBackground.getName());

		//load only when it's not the same
		if (currBackground.getName() != BackgroundEnum.SUNNY || init){
			init = false;
			List<BackgroundTheme> back = themesMap.get(currentTheme);		

			for(BackgroundTheme bt : back){
				if (bt.getName() == BackgroundEnum.SUNNY){	

					DayNightCycle cycle = (DayNightCycle)ct.getSharedObject(Constants.DAY_NIGHT_CYCLE);
					if (cycle == null){
						cycle = DayNightCycle.DAY;
					}
					bt.setCycle(cycle);
					ct.putSharedObject(Constants.THEMES_BACKGROUND_IMG_UPDATE, bt.fullPathBackImg());
					currBackground = bt;				

					refreshTheme();
					break;
				}
			}
		}
	}
	public void loadThunderBackdrop(){
		logger.log(Level.CONFIG, "loadThunderBackdrop(). Current: " + currBackground.getName());
		
		//load only when it's not the same
		if (currBackground.getName() != BackgroundEnum.THUNDER){
			List<BackgroundTheme> back = themesMap.get(currentTheme);		

			for(BackgroundTheme bt : back){
				if (bt.getName() == BackgroundEnum.THUNDER){				
					DayNightCycle cycle = (DayNightCycle)ct.getSharedObject(Constants.DAY_NIGHT_CYCLE);
					if (cycle == null){
						cycle = DayNightCycle.DAY;
					}
					bt.setCycle(cycle);
					ct.putSharedObject(Constants.THEMES_BACKGROUND_IMG_UPDATE, bt.fullPathBackImg());
					currBackground = bt;
					refreshTheme();
					break;
				}
			}
		}
	}
	public void loadSnowBackdrop(){
		logger.log(Level.CONFIG, "loadSnowBackdrop(). Current: " + currBackground.getName());

		//load only when it's not the same
		if (currBackground.getName() != BackgroundEnum.SNOW){
			List<BackgroundTheme> back = themesMap.get(currentTheme);		

			for(BackgroundTheme bt : back){
				if (bt.getName() == BackgroundEnum.SNOW){				
					DayNightCycle cycle = (DayNightCycle)ct.getSharedObject(Constants.DAY_NIGHT_CYCLE);
					if (cycle == null){
						cycle = DayNightCycle.DAY;
					}
					bt.setCycle(cycle);
					ct.putSharedObject(Constants.THEMES_BACKGROUND_IMG_UPDATE, bt.fullPathBackImg());
					currBackground = bt;
					refreshTheme();
					break;
				}
			}
		}
	}
	public void fireNightCycle(){		

		currBackground.setCycle(DayNightCycle.NIGHT);

		refreshTheme();
		ct.putSharedObject(Constants.THEMES_BACKGROUND_IMG_UPDATE,  currBackground.fullPathBackImg());		
	}
	
	public void fireDayCycle(){
		
		currBackground.setCycle(DayNightCycle.DAY);
		
		refreshTheme();
		ct.putSharedObject(Constants.THEMES_BACKGROUND_IMG_UPDATE, currBackground.fullPathBackImg());
		
	}
	/** Register a label and it's associated enum from XML**/
	public void registerLabelTextColor(JLabel label, LabelEnums lblEnum){
		registeredLabels.put(lblEnum, label);	
		
	}
	public void registerIconColor(Object iconObject, IconEnum lblIconEnum){
		
		//check if dupplicate label icon by checking the enum icon key.
		for(Map.Entry<IconEnum, Object> lblIcon : registeredIcon.entrySet()){
			if (lblIcon.getKey().getIconKey().equals(lblIconEnum.getIconKey())){
				getRegisteredIcon().remove(lblIcon.getKey());
				break;
			}
		}
		
		getRegisteredIcon().put(lblIconEnum, iconObject);			
				
	}
	public ImageIcon getIcon(IconEnum icon) throws IOException{		
		return currBackground.getLabelIconMap().get(icon).getImage(currBackground.getCycle());
	}
	private void refreshTheme(){
		logger.log(Level.CONFIG, "refreshTheme()");
		SwingUtilities.invokeLater(new Runnable(){

			@Override
			public void run() {		
				String errorFile = "";
				try{
					
					//do labels color 1st
					Map<LabelEnums, LabelTheme> lblMap = currBackground.getLabels();

					for(Map.Entry<LabelEnums, JLabel> label : registeredLabels.entrySet()){
						LabelTheme lblTheme = lblMap.get(label.getKey());
						//COLOR
						Color color = currBackground.getCycle() == DayNightCycle.DAY ? lblTheme.getTextDayColor() :
							lblTheme.getTextNightColor();
						label.getValue().setForeground(color);										
					}

					//do label icons 
					Map<IconEnum, IconTheme> lblIconMap = currBackground.getLabelIconMap();
					for(Map.Entry<IconEnum, Object> lblIcon : getRegisteredIcon().entrySet()){
					
						IconTheme iconTheme = lblIconMap.get(lblIcon.getKey());
						//Label
						if (lblIcon.getValue() instanceof JLabel){
							JLabel label =  (JLabel)lblIcon.getValue();
							label.setIcon(iconTheme.getImage(currBackground.getCycle()));
						}else if(lblIcon.getValue() instanceof JButton){
							JButton btn = (JButton)lblIcon.getValue();
							btn.setIcon(iconTheme.getImage(currBackground.getCycle()));
						}
						errorFile = lblIcon.getKey().name();
					}
				}catch(Exception ex){
					logger.log(Level.SEVERE, "Error in refreshTheme. File: " + errorFile  , ex);
				}
				
			}
		});

	}

	public synchronized Map<IconEnum, Object> getRegisteredIcon() {
		return registeredIcon;
	}

	public synchronized void setRegisteredIcon(Map<IconEnum, Object> registeredIcon) {
		this.registeredIcon = registeredIcon;
	}

}