package net.piclock.theme;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.enums.BackgroundEnum;
import net.piclock.enums.DayNightCycle;
import net.piclock.enums.IconEnum;
import net.piclock.enums.LabelEnums;
import net.piclock.main.Constants;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.FormatStackTrace;
import net.piclock.util.LoadThemesFromXml;
import net.piclock.view.WeatherForecastView;

public class ThemeHandler {
	private static final Logger logger = Logger.getLogger( ThemeHandler.class.getName() );
	
	private Map<ThemeEnum, List<BackgroundTheme>> themesMap;
	
	private ThemeEnum currentTheme;
	private List<BackgroundEnum> currentThemeBackgrounds; //list of all availaible background for the theme
	private BackgroundTheme currBackground;

	private SwingContext ct = SwingContext.getInstance();
	
	//registered labels
	private Map<LabelEnums, Object> registeredLabels = new HashMap<LabelEnums, Object>();
	private Map<IconEnum, Object> registeredIcon = new HashMap<IconEnum, Object>();
	
	private boolean init = true;

	public ThemeHandler() throws Exception{
		//init Themes
		themesMap = LoadThemesFromXml.loadThemeFromXml();
	}
	
	public void refreshCurrentBackground() {
		logger.log(Level.CONFIG, "Refreshing current background: " + currBackground.getName());//todo since it's same object ., it won't refresh

		ct.sendMessage(Constants.THEMES_BACKGROUND_IMG_UPDATE, new Message(currBackground.fullPathBackImg(currBackground.getCycle())));
//		ct.putSharedObject(Constants.THEMES_BACKGROUND_IMG_UPDATE, currBackground.fullPathBackImg(currBackground.getCycle()));

	}

	public void loadTheme(ThemeEnum theme){

		currentTheme = theme;
				
		List<BackgroundTheme> back = themesMap.get(currentTheme);
		currentThemeBackgrounds = new ArrayList<BackgroundEnum>();
		for(BackgroundTheme bt : back){
			if (bt.getName() == BackgroundEnum.SUNNY){
				currBackground = bt;				
			}
			currentThemeBackgrounds.add(bt.getName());
		}		
	}

	public void loadRainBackdrop(){		
		logger.log(Level.INFO, "loadRainBackdrop(). Current: " + currBackground.getName());
		
		boolean backGroundIncluded = isBackgroundIncludedInTheme(BackgroundEnum.RAIN);
		
		//load only when it's not the same
		if (currBackground.getName() != BackgroundEnum.RAIN && backGroundIncluded){
			
			//save in session
			ct.putSharedObject(Constants.CURRENT_BACKGROUND, BackgroundEnum.RAIN );

			List<BackgroundTheme> back = themesMap.get(currentTheme);		

			for(BackgroundTheme bt : back){
				if (bt.getName() == BackgroundEnum.RAIN){				
					DayNightCycle cycle = (DayNightCycle)ct.getSharedObject(Constants.DAY_NIGHT_CYCLE);
					if (cycle == null){
						cycle = DayNightCycle.DAY;
					}
					bt.setCycle(cycle);
					ct.sendMessage(Constants.THEMES_BACKGROUND_IMG_UPDATE, new Message(bt.fullPathBackImg(cycle)));
//					ct.putSharedObject(Constants.THEMES_BACKGROUND_IMG_UPDATE, bt.fullPathBackImg(cycle));
					currBackground = bt;
					refreshTheme();
					break;
				}
			}
		}else if (!backGroundIncluded) {
			logger.log(Level.INFO, "loadRainBackdrop(). Loading sunny since Rain is not included into the theme");
			loadSunnyBackdrop();
		}
	}
	public void loadSunnyBackdrop(){
		logger.log(Level.INFO, "loadSunnyBackdrop(). Current: " + currBackground.getName());

		//load only when it's not the same
		if (currBackground.getName() != BackgroundEnum.SUNNY || init){
			
			//save in session
			ct.putSharedObject(Constants.CURRENT_BACKGROUND, BackgroundEnum.SUNNY );
			
			init = false;
			List<BackgroundTheme> back = themesMap.get(currentTheme);		

			for(BackgroundTheme bt : back){
				if (bt.getName() == BackgroundEnum.SUNNY){	

					DayNightCycle cycle = (DayNightCycle)ct.getSharedObject(Constants.DAY_NIGHT_CYCLE);
					if (cycle == null){
						cycle = DayNightCycle.DAY;
					}
					bt.setCycle(cycle);
//					ct.putSharedObject(Constants.THEMES_BACKGROUND_IMG_UPDATE, bt.fullPathBackImg(cycle));
					ct.sendMessage(Constants.THEMES_BACKGROUND_IMG_UPDATE, new Message(bt.fullPathBackImg(cycle)));
					currBackground = bt;				

					refreshTheme();
					break;
				}
			}
		}
	}
	public void loadThunderBackdrop(){
		logger.log(Level.INFO, "loadThunderBackdrop(). Current: " + currBackground.getName());
		
		boolean backGroundIncluded = isBackgroundIncludedInTheme(BackgroundEnum.THUNDER);
		
		//load only when it's not the same
		if (currBackground.getName() != BackgroundEnum.THUNDER && backGroundIncluded){
			
			//save in session
			ct.putSharedObject(Constants.CURRENT_BACKGROUND, BackgroundEnum.THUNDER );
			
			List<BackgroundTheme> back = themesMap.get(currentTheme);		

			for(BackgroundTheme bt : back){
				if (bt.getName() == BackgroundEnum.THUNDER){				
					DayNightCycle cycle = (DayNightCycle)ct.getSharedObject(Constants.DAY_NIGHT_CYCLE);
					if (cycle == null){
						cycle = DayNightCycle.DAY;
					}
					bt.setCycle(cycle);
//					ct.putSharedObject(Constants.THEMES_BACKGROUND_IMG_UPDATE, bt.fullPathBackImg(cycle));
					ct.sendMessage(Constants.THEMES_BACKGROUND_IMG_UPDATE, new Message(bt.fullPathBackImg(cycle)));
					currBackground = bt;
					refreshTheme();
					break;
				}
			}
		}else if (!backGroundIncluded) {
			logger.log(Level.INFO, "loadThunderBackdrop(). Loading sunny since Thunder is not included into the theme");
			loadSunnyBackdrop();
		}
	}
	public void loadSnowBackdrop(){
		logger.log(Level.INFO, "loadSnowBackdrop(). Current: " + currBackground.getName());

		boolean backGroundIncluded = isBackgroundIncludedInTheme(BackgroundEnum.SNOW);
		
		//load only when it's not the same
		if (currBackground.getName() != BackgroundEnum.SNOW && backGroundIncluded){
			
			//save in session
			ct.putSharedObject(Constants.CURRENT_BACKGROUND, BackgroundEnum.SNOW );
			
			List<BackgroundTheme> back = themesMap.get(currentTheme);		

			for(BackgroundTheme bt : back){
				if (bt.getName() == BackgroundEnum.SNOW){				
					DayNightCycle cycle = (DayNightCycle)ct.getSharedObject(Constants.DAY_NIGHT_CYCLE);
					if (cycle == null){
						cycle = DayNightCycle.DAY;
					}
					bt.setCycle(cycle);
//					ct.putSharedObject(Constants.THEMES_BACKGROUND_IMG_UPDATE, bt.fullPathBackImg(cycle));
					ct.sendMessage(Constants.THEMES_BACKGROUND_IMG_UPDATE, new Message(bt.fullPathBackImg(cycle)));
					currBackground = bt;
					refreshTheme();
					break;
				}
			}
		}else if (!backGroundIncluded) {
			logger.log(Level.INFO, "loadSnowBackdrop(). Loading sunny since Snow is not included into the theme");
			loadSunnyBackdrop();
		}
	}
	public void loadCloudyBackdrop(){
		logger.log(Level.INFO, "loadCloudyBackdrop(). Current: " + currBackground.getName());

		boolean backGroundIncluded = isBackgroundIncludedInTheme(BackgroundEnum.CLOUDY);
		
		//load only when it's not the same
		if (currBackground.getName() != BackgroundEnum.CLOUDY && backGroundIncluded){
			
			//save in session
			ct.putSharedObject(Constants.CURRENT_BACKGROUND, BackgroundEnum.CLOUDY );
			
			List<BackgroundTheme> back = themesMap.get(currentTheme);		

			for(BackgroundTheme bt : back){
				if (bt.getName() == BackgroundEnum.CLOUDY){				
					DayNightCycle cycle = (DayNightCycle)ct.getSharedObject(Constants.DAY_NIGHT_CYCLE);
					if (cycle == null){
						cycle = DayNightCycle.DAY;
					}
					bt.setCycle(cycle);
//					ct.putSharedObject(Constants.THEMES_BACKGROUND_IMG_UPDATE, bt.fullPathBackImg(cycle));
					ct.sendMessage(Constants.THEMES_BACKGROUND_IMG_UPDATE, new Message(bt.fullPathBackImg(cycle)));
					currBackground = bt;
					refreshTheme();
					break;
				}
			}
		}else if (!backGroundIncluded) {
			logger.log(Level.INFO, "loadCloudyBackdrop(). Loading sunny since Cloudy is not included into the theme");
			loadSunnyBackdrop();
		}
	}
	public void loadFogBackdrop(){
		logger.log(Level.INFO, "loadFogBackdrop(). Current: " + currBackground.getName());

		boolean backGroundIncluded = isBackgroundIncludedInTheme(BackgroundEnum.FOG);
		
		//load only when it's not the same
		if (currBackground.getName() != BackgroundEnum.FOG && backGroundIncluded){
			
			//save in session
			ct.putSharedObject(Constants.CURRENT_BACKGROUND, BackgroundEnum.FOG );
			
			List<BackgroundTheme> back = themesMap.get(currentTheme);		

			for(BackgroundTheme bt : back){
				if (bt.getName() == BackgroundEnum.FOG){				
					DayNightCycle cycle = (DayNightCycle)ct.getSharedObject(Constants.DAY_NIGHT_CYCLE);
					if (cycle == null){
						cycle = DayNightCycle.DAY;
					}
					bt.setCycle(cycle);
//					ct.putSharedObject(Constants.THEMES_BACKGROUND_IMG_UPDATE, bt.fullPathBackImg(cycle));
					ct.sendMessage(Constants.THEMES_BACKGROUND_IMG_UPDATE, new Message(bt.fullPathBackImg(cycle)));
					currBackground = bt;
					refreshTheme();
					break;
				}
			}
		}else if (!backGroundIncluded) {
			logger.log(Level.INFO, "loadFogBackdrop(). Loading sunny since FOG is not included into the theme");
			loadSunnyBackdrop();
		}
	}
	public void fireNightCycle(){	
		logger.log(Level.CONFIG,"fireNightCycle");

		currBackground.setCycle(DayNightCycle.NIGHT);

		refreshTheme();
//		ct.putSharedObject(Constants.THEMES_BACKGROUND_IMG_UPDATE,  currBackground.fullPathBackImg(DayNightCycle.NIGHT));		
		ct.sendMessage(Constants.THEMES_BACKGROUND_IMG_UPDATE, new Message(currBackground.fullPathBackImg(DayNightCycle.NIGHT)));
	}
	
	public void fireDayCycle(){
		logger.log(Level.CONFIG,"fireDayCycle");
		currBackground.setCycle(DayNightCycle.DAY);
		
		refreshTheme();
//		ct.putSharedObject(Constants.THEMES_BACKGROUND_IMG_UPDATE, currBackground.fullPathBackImg(DayNightCycle.DAY));
		ct.sendMessage(Constants.THEMES_BACKGROUND_IMG_UPDATE, new Message(currBackground.fullPathBackImg(DayNightCycle.DAY)));
		
	}
	/** Register a label and it's associated enum from XML**/
	public void registerLabelTextColor(Object label, LabelEnums lblEnum){
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

					for(Map.Entry<LabelEnums, Object> label : registeredLabels.entrySet()){
						LabelTheme lblTheme = lblMap.get(label.getKey());
						//COLOR
						Color color = currBackground.getCycle() == DayNightCycle.DAY ? lblTheme.getTextDayColor() :
							lblTheme.getTextNightColor();
						
						Object o = label.getValue();
						if (o instanceof JLabel) {
							((JLabel)o).setForeground(color);
						}else if (o instanceof WeatherForecastView && label.getKey() == LabelEnums.WEATHER_FORECAST_VIEW) {
							((WeatherForecastView)o).colorComponent(color);
						}else if (o instanceof JCheckBox) {
							((JCheckBox)o).setForeground(color);
						}
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
					ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
	  				eh.addError(ErrorType.GENERAL, new ErrorInfo(new FormatStackTrace(ex).getFormattedException()));
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
	
	//when loading a background, verify if it is included in the themes, if not , default to the sunny theme.
	private boolean isBackgroundIncludedInTheme(BackgroundEnum backEnum) {
		
		if (currentThemeBackgrounds.contains(backEnum)) {
			return true;
		}
		
		
		return false;
	}
	

}