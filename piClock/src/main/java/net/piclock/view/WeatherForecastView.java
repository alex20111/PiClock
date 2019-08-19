package net.piclock.view;


import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;

import net.miginfocom.swing.MigLayout;
import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.enums.LabelEnums;
import net.piclock.main.Constants;
import net.piclock.main.Preferences;
import net.piclock.swing.component.Scroll;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;
import net.piclock.thread.ScreenAutoClose;
import net.piclock.util.FormatStackTrace;
import net.piclock.util.ImageUtils;
import net.piclock.weather.DarkSkyUtil;
import net.weather.bean.Message;
import net.weather.bean.WeatherForecastModel;
import net.weather.bean.WeatherGenericModel;
import net.weather.enums.Host;
import net.weather.utils.MessageHandl;

	
public class WeatherForecastView extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger( WeatherForecastView.class.getName() );
	
	private JPanel parent = new JPanel();
	private Color currentColor = Color.BLACK;
	
	private String lableForecastSize = "420px";
	
	private JLabel lblAlert;
	private SwingContext ct = SwingContext.getInstance();

	int indx = 0;
	boolean notFirst = false;

	public WeatherForecastView() {
		
		ct.addPropertyChangeListener(Constants.FORECAST_RESULT, this);
		ct.addPropertyChangeListener(Constants.FORECAST_DISPLAY_ERROR, this);

		parent.setAutoscrolls(true);

		setLayout(new BorderLayout());

		parent.setLayout(new MigLayout("", "[40px,center][70px][grow]"));//"");
		
		ThemeHandler theme = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
		
		theme.registerLabelTextColor(this, LabelEnums.WEATHER_FORECAST_VIEW);

		JScrollPane scrollPane = new JScrollPane(parent);
		
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);		
		
		add(scrollPane);
		
		Scroll scroll = new Scroll(parent);

		parent.addMouseListener(scroll);
		parent.addMouseMotionListener(scroll);
		
		//alert
		JPanel alertPanel = new JPanel();
		alertPanel.setOpaque(false);
		parent.add(alertPanel, BorderLayout.NORTH);
		alertPanel.setLayout(new MigLayout("", "[89px][grow][89px]", "[23px]"));
		
		JButton backBtn = new JButton("<");
		backBtn.setFont(new Font("Tahoma", Font.BOLD, 18));
		alertPanel.add(backBtn, "cell 0 0,alignx left,aligny top");
		
		backBtn.addActionListener(l -> {						
				try {
					ScreenAutoClose.stop();
				} catch (InterruptedException e1) {
					ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
					eh.addError(ErrorType.WEATHER, new ErrorInfo(new FormatStackTrace(e1).getFormattedException()));
					logger.log(Level.SEVERE,"Error while trying to stop auto close screen", e1);
				}
				
				JPanel cardsPanel = (JPanel) ct.getSharedObject(Constants.CARD_PANEL);
				
				CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
				cardLayout.show(cardsPanel, Constants.MAIN_VIEW);				
		});		
		
		lblAlert = new JLabel("!! Weather Alert !!");
		lblAlert.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblAlert.setForeground(Color.RED);
		alertPanel.add(lblAlert, "cell 1 0,alignx center");
		lblAlert.setVisible(false);
		
		lblAlert.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				
				JPanel cardsPanel = (JPanel) ct.getSharedObject(Constants.CARD_PANEL);
				try {					
					ScreenAutoClose.stop();
					ScreenAutoClose.start(cardsPanel, 45, TimeUnit.SECONDS);
				} catch (InterruptedException e1) {
					ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
					eh.addError(ErrorType.WEATHER, new ErrorInfo(new FormatStackTrace(e1).getFormattedException()));
					logger.log(Level.SEVERE,"Error while trying to stop auto close screen", e1);
				}				
				
				WeatherAlertView.goingBackPage = Constants.WEATHER_FORECAST_VIEW;
				
				CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
				cardLayout.show(cardsPanel, Constants.WEATHER_ALERT_VIEW);
			}		
		});

		setOpaque(false);
		parent.setOpaque(false);		
	}
	
	private void addFPanel(String day, String f , ImageIcon ic){
		
		JLabel daysLbl = new JLabel();
		JLabel icLbl = new JLabel();
		JLabel fLbl = new JLabel();	
		
		JSeparator separator;	
		
		if(notFirst){
			//add seperator
			 separator = new JSeparator();
			 separator.setForeground(currentColor);
			 parent.add(separator, "cell 0 "+indx+" 3 1, grow" );
			 indx ++;			
		}else{
			notFirst = true;
		}		

		daysLbl = new JLabel();
		daysLbl.setForeground(currentColor);
		daysLbl.setText("<html><div style='width: 35px;word-wrap: break-word;'>"+day+"</html>");
		daysLbl.setFont(new Font("Tahoma", Font.BOLD, 20));
		daysLbl.setOpaque(false);

		icLbl = new JLabel(ic);
		
		fLbl = new JLabel();
		fLbl.setForeground(currentColor);
		fLbl.setFont(new Font("Tahoma", Font.BOLD, 20));
		fLbl.setText("<html><div style='width: "+ lableForecastSize + ";word-wrap: break-word;'>"+f+"</html>");
		fLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
  
		parent.add(daysLbl, "cell 0 "+indx+",alignx left,aligny center" );
		parent.add(icLbl , "cell 1 "+indx+",alignx center");
		parent.add(fLbl, "cell 2 "+indx+",grow");		
		
		parent.revalidate();
		parent.repaint();
		indx++;
	}
	
	public void displayLoading(){
		clear();
		addFPanel("", "LOADING Forecast...", ImageUtils.getInstance().getWeatherLoader()); 
	}
	
	private void clear(){
		Component[] componentList = parent.getComponents();
		//Loop through the components
		for(Component c : componentList){

		    //Find the components you want to remove
		    if(c instanceof JLabel){
		        //Remove it
		        parent.remove(c);
		    }else if(c instanceof JSeparator){
		        //Remove it
		        parent.remove(c);
		    }
		}
		//IMPORTANT
		parent.revalidate();
		parent.repaint();
		indx = 0;
		notFirst = false;
	}
	
	public void colorComponent(Color color){
		logger.log(Level.CONFIG, "Color the weather forecast text: COLOR: " + color);
		
		currentColor = color;
		
		Component[] componentList = parent.getComponents();
		//Loop through the components
		for(Component c : componentList){

		    //Find the components you want to remove
		    if(c instanceof JLabel){		      
		        ((JLabel)c).setForeground(color);
		    }else if(c instanceof JSeparator){		       
		    	((JSeparator)c).setForeground(color);
		    }
		}
		//IMPORTANT
		parent.revalidate();
		parent.repaint();

	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		logger.config("event received: " + evt.getPropertyName());

		if(evt.getPropertyName().equals(Constants.FORECAST_RESULT)){
			//get Host
			Preferences pref = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
			Host host = Host.valueOf(pref.getWeatherProvider());
			clear();
			WeatherGenericModel wgm = (WeatherGenericModel)evt.getNewValue();
			
			if (wgm.getWForecastModel() != null && wgm.getWForecastModel().size() > 0){
				ImageUtils img = ImageUtils.getInstance();
				for(int i = 0 ; i < wgm.getWForecastModel().size() ; i++){
					WeatherForecastModel wfm = wgm.getWForecastModel().get(i);
					try {
					
						if (host == Host.envCanada) {
							addFPanel(wfm.getDayOfWeek(),  wfm.getForecast(), img.getImage("weather" + File.separatorChar + wfm.getIconName()));
						}else {
							
							addFPanel(wfm.getDayOfWeek(),  wfm.getForecast(), img.getImage("weather" + File.separatorChar + DarkSkyUtil.getIconFileName(wfm.getIconName())));
						}
						
					} catch (IOException e) {
						addFPanel( wfm.getForecast(), wfm.getDayOfWeek(), img.getWeatherNA());
						ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
						eh.addError(ErrorType.WEATHER, new ErrorInfo(new FormatStackTrace(e).getFormattedException()));
						logger.log(Level.SEVERE, "WeatherIcon", e);
					}
				}
			}
			
			if (wgm.getWeatherAlert() != null){
				lblAlert.setVisible(true);
			}else {
				lblAlert.setVisible(false);
			}
		}else if(evt.getPropertyName().equals(Constants.FORECAST_DISPLAY_ERROR)){
			WeatherGenericModel wgm = (WeatherGenericModel)evt.getNewValue();
			
			MessageHandl msg =  wgm.getMessages();
			
			Message m = msg.getAllMessages().get( msg.getAllMessages().size() - 1);			
			
			clear();
		addFPanel("", m.getTitle(), ImageUtils.getInstance().getWeatherNA());

		}
	}
}