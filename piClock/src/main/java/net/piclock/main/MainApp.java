package net.piclock.main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import net.piclock.enums.IconEnum;
import net.piclock.enums.LabelEnums;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeEnum;
import net.piclock.theme.ThemeHandler;
import net.piclock.thread.Clock;
import net.piclock.thread.EnvCanWorker;
import net.piclock.thread.LDRStatusWorker;
import net.piclock.thread.ScreenAutoClose;
import net.piclock.thread.TempSensorWorker;
import net.piclock.util.ImageUtils;
import net.piclock.util.LogConfig;
import net.piclock.util.PreferencesHandler;
import net.piclock.view.AlarmView;
import net.piclock.view.ConfigView;
import net.piclock.view.RadioStationsView;
import net.piclock.view.WeatherAlertView;
import net.piclock.view.WeatherConfigView;
import net.piclock.view.WeatherForecastView;
import net.piclock.weather.WeatherBean;
import net.weather.bean.Message;
import net.weather.bean.WeatherCurrentModel;
import net.weather.bean.WeatherGenericModel;
import net.weather.enums.Host;
import net.weather.utils.MessageHandl;

public class MainApp extends JFrame implements PropertyChangeListener {
	private static final Logger logger = Logger.getLogger( MainApp.class.getName() );
	private static final long serialVersionUID = 1L;	

	public Preferences prefs;	
	private SwingContext ct = SwingContext.getInstance();	
	private JPanel cardsPanel;
	private ThemeHandler themes;
	
	private Image dimg;
	private JLabel clockLabel;
	private JLabel weekDateLable;
	private JLabel lblCurrentweather;
	private JLabel lblWeatherAlert;
	private JLabel lblWeatherIcon;
	private JLabel lblTempSun;	
	
	private JLabel lblRadioIcon;
	private JLabel lblMp3Icon;
	private JLabel lblWebserverIcon;
	private  JPopupMenu options;
	private JButton btnOptionsIcon;
	private JLabel lblAlarmIcon;
	private JLabel lblTempShade;
	private JLabel lblWiFiIcon;
	
	private ImageIcon weatherNaIcon ;
	
	//panels
	private WeatherForecastView forecastView;
	private WeatherConfigView  weatherConfig;
	private WeatherAlertView weatherAlertView;
	private RadioStationsView radioStationsView;
	
	//thread executor
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
	private ScheduledFuture<EnvCanWorker> envCanThread;
	private ScheduledFuture<TempSensorWorker> sensorThread;		

	//worker LDR
	private LDRStatusWorker ldrWorker;
	
	//wifi blinking
	private boolean blinking = true;
	private Timer blinkingWifiTimer;

	private boolean sensorActive = true; //for the external temperature sensors.. only if available.
	
	private final SimpleDateFormat sdfTime = new SimpleDateFormat(Constants.HOUR_MIN);
	private SimpleDateFormat parseToDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
	/**
	 * Launch the application.
	 */
	public static void main(String... args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LogConfig logs = new LogConfig();
//					OFF SEVERE	WARNING	INFO CONFIG(is debug),  FINE FINER FINEST ALL  -- height 480 - width 800
					logs.configLogs("mainAppLog", Level.CONFIG, true, false);  //TODO turn on file log					
					
					MainApp frame = new MainApp();
//					frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Error starting", e);
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws Exception 
	 */
	public MainApp() throws Exception {
		logger.info("Start Program");	
		
//		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//		int screenHeight = screenSize.height;
//		int screenWidth = screenSize.width;
//		System.out.println("SCREEN SIZEEEEEEEEEEEEEEE: " + screenHeight + " --  Width: " + screenWidth);
		
		
		ImageIO.setUseCache(true);
		
		weatherNaIcon = ImageUtils.getInstance().getWeatherNA();
		
		setLocationRelativeTo(null);
		
		ct.addPropertyChangeListener(Constants.FORECAST_RESULT, this);	
		ct.addPropertyChangeListener(Constants.FETCH_FORECAST, this);
		ct.addPropertyChangeListener(Constants.FORECAST_DISPLAY_LOAD, this);	
		ct.addPropertyChangeListener(Constants.FORECAST_DISPLAY_ERROR, this);
		ct.addPropertyChangeListener(Constants.THEMES_BACKGROUND_IMG_UPDATE, this);
		ct.addPropertyChangeListener(Constants.CHECK_INTERNET, this);
		ct.addPropertyChangeListener(Constants.SENSOR_INFO, this);
		
		
		setBackground(Color.BLUE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize( 800, 480);	
		
		//preferences
		prefs = PreferencesHandler.read();	
		ct.putSharedObject(Constants.PREFERENCES, prefs);
		
		themes = new ThemeHandler();
		ct.putSharedObject(Constants.THEMES_HANDLER, themes);
		themes.loadTheme(ThemeEnum.defaultTheme);		
				
		//set background 
		cardsPanel = new JPanel(){
		 	private static final long serialVersionUID = 1L;

			@Override
		    public void paintComponent(Graphics g){
		      super.paintComponent(g);
		      g.drawImage(dimg, 0, 0, this);
		    }
		};
						
		setContentPane(cardsPanel);
		
		weatherConfig = new WeatherConfigView();
		forecastView = new WeatherForecastView(cardsPanel);
		weatherAlertView = new WeatherAlertView();	
		radioStationsView = new RadioStationsView();
		
		cardsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		setUndecorated(true);
		
		//TODO  remove to activate transparent cursor
//		// Transparent 16 x 16 pixel cursor image.
//		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
//		// Create a new blank cursor.
//		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
//		    cursorImg, new Point(0, 0), "blank cursor");
//		// Set the blank cursor to the JFrame.
//		cardsPanel.setCursor(blankCursor);			
	
		cardsPanel.setLayout(new CardLayout(0, 0));
		
		JPanel mainPanel = new JPanel();
		cardsPanel.add(mainPanel, Constants.MAIN_VIEW);
		mainPanel.setLayout(new BorderLayout(0, 0));
		mainPanel.setOpaque(false);
		
		JPanel timePanel = new JPanel();
		mainPanel.add(timePanel, BorderLayout.CENTER);
		timePanel.setLayout(new MigLayout("", "[][grow 80][][grow 90,right]", "[][grow 40][center][][grow 50][]"));
		timePanel.setOpaque(false);
		
		clockLabel = new JLabel("00:00");
		clockLabel.setFont(new Font("Courier New", Font.BOLD, 80));
		themes.registerLabelTextColor(clockLabel, LabelEnums.CLOCK);
		timePanel.add(clockLabel, "cell 2 2,alignx center,aligny center");
		
		weekDateLable = new JLabel("Fri, dec 21");
		themes.registerLabelTextColor(weekDateLable, LabelEnums.DAY_OF_WEEK);
		weekDateLable.setFont(new Font("Tahoma", Font.BOLD, 20));
		timePanel.add(weekDateLable, "cell 2 3,alignx center,aligny top");
		lblWebserverIcon = new JLabel();
		lblWebserverIcon.setVisible(false);
		timePanel.add(lblWebserverIcon, "cell 0 5");
		lblWebserverIcon.setBorder(new EmptyBorder(10,10,0,0));
		themes.registerIconColor(lblWebserverIcon, IconEnum.WEB_SERVER);
		
		btnOptionsIcon = new JButton("");
		btnOptionsIcon.setFocusPainted(false);
		timePanel.add(btnOptionsIcon, "cell 3 5,alignx right,aligny bottom");
		btnOptionsIcon.setOpaque(false);
		btnOptionsIcon.setContentAreaFilled(false);
		btnOptionsIcon.setBorderPainted(false);
		themes.registerIconColor(btnOptionsIcon, IconEnum.BUTTON_COG);		
		
		//Show options!
		btnOptionsIcon.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
			
				options.show(btnOptionsIcon,-140,-160);
			}
		});				
		
		JPanel weatherPanel = new JPanel();
		mainPanel.add(weatherPanel, BorderLayout.NORTH);
		weatherPanel.setLayout(new MigLayout("", "[grow 15][100px,left][][80px][80px][grow 20]", "[]"));
		weatherPanel.setOpaque(false);
		
		lblCurrentweather = new JLabel("<html><div style='width: 80px;word-wrap: break-word;text-align: center'>Not available</html>");
		lblCurrentweather.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblCurrentweather.setForeground(Color.WHITE);
		themes.registerLabelTextColor(lblCurrentweather, LabelEnums.CURRENT_WEATHER);
		weatherPanel.add(lblCurrentweather, "cell 1 0,alignx right");
		
		lblWeatherIcon = new JLabel("--:--",weatherNaIcon,  SwingConstants.CENTER);
		lblWeatherIcon.setVerticalTextPosition(JLabel.BOTTOM);
		lblWeatherIcon.setHorizontalTextPosition(JLabel.CENTER);
		themes.registerLabelTextColor(lblWeatherIcon, LabelEnums.WTH_LST_UPD_TIME);
		
		lblWeatherIcon.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				
				logger.log(Level.CONFIG, "Fetching weather from the weather Icon. Is weather activated? " + prefs.isWeatherActivated());
				
				if (prefs.isWeatherActivated()){

					CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
					cardLayout.show(cardsPanel, Constants.WEATHER_FORECAST_VIEW);	

					//test if it's been more than 3 min since the weather refreshed
					Date lastUpdated = (Date) ct.getSharedObject(Constants.WEATHER_LST_UPD);
					if (lastUpdated == null || (new Date().getTime() - lastUpdated.getTime()) > 180000){
						forecastView.displayLoading();

						fetchForecast(0);
					}
					ScreenAutoClose.start(cardsPanel, 45, TimeUnit.SECONDS);
				}
			}
		});
		
		weatherPanel.add(lblWeatherIcon, "cell 2 0,growx,aligny center");		
		
		JPanel alertIconsPanel = new JPanel();
		alertIconsPanel.setPreferredSize(new Dimension(40, 10));
		alertIconsPanel.setMinimumSize(new Dimension(30, 10));
		mainPanel.add(alertIconsPanel, BorderLayout.WEST);
		alertIconsPanel.setOpaque(false);
		alertIconsPanel.setLayout(new BoxLayout(alertIconsPanel, BoxLayout.Y_AXIS));
		
		lblAlarmIcon = new JLabel();
		lblAlarmIcon.setBorder(new EmptyBorder(5,10,0,0));//top,left,bottom,right
		lblAlarmIcon.setVisible(false);
		themes.registerIconColor(lblAlarmIcon, IconEnum.ALARM_ICON);
		alertIconsPanel.add(lblAlarmIcon);
		
		lblWeatherAlert = new JLabel(ImageUtils.getInstance().getWeatherAlertIcon());
		lblWeatherAlert.setVisible(false);
		lblWeatherAlert.setBorder(new EmptyBorder(10,10,0,0));//top,left,bottom,right
		alertIconsPanel.add(lblWeatherAlert);
		
		lblWeatherAlert.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				System.out.println("Clicked on the alert");
				WeatherAlertView.goingBackPage = Constants.MAIN_VIEW;
				
				CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
				cardLayout.show(cardsPanel, Constants.WEATHER_ALERT_VIEW);	
				
				ScreenAutoClose.start(cardsPanel, 45, TimeUnit.SECONDS);
			}
		});
		
		lblRadioIcon = new JLabel();
		lblRadioIcon.setVisible(false);
		lblRadioIcon.setBorder(new EmptyBorder(10,10,0,0));//top,left,bottom,right
		themes.registerIconColor(lblRadioIcon, IconEnum.RADIO_ICON);
		alertIconsPanel.add(lblRadioIcon);
		
		lblMp3Icon = new JLabel();
		lblMp3Icon.setBorder(new EmptyBorder(10,10,0,0));//top,left,bottom,right
		lblMp3Icon.setVisible(false);
		themes.registerIconColor(lblMp3Icon, IconEnum.MP3_ICON);
		alertIconsPanel.add(lblMp3Icon);
		
		lblWiFiIcon = new JLabel();
		lblWiFiIcon.setBorder(new EmptyBorder(10,10,0,0));//top,left,bottom,right
		lblWiFiIcon.setVisible(false);
		themes.registerIconColor(lblWiFiIcon, IconEnum.WIFI_ON_ICON);
		alertIconsPanel.add(lblWiFiIcon);			
		
		clock();
		
		lblTempSun = new JLabel("--c");
		
		lblTempSun.setFont(new Font("Tahoma", Font.BOLD, 20));
		themes.registerIconColor(lblTempSun, IconEnum.TEMP_SUN);
		themes.registerLabelTextColor(lblTempSun, LabelEnums.TEMP_SUN);
		weatherPanel.add(lblTempSun, "cell 3 0,alignx right,aligny center");
		
		lblTempShade = new JLabel("--c");
		lblTempShade.setFont(new Font("Tahoma", Font.BOLD, 20));
		themes.registerIconColor(lblTempShade, IconEnum.TEMP_SHADE);
		themes.registerLabelTextColor(lblTempShade, LabelEnums.TEMP_SHADE);
		weatherPanel.add(lblTempShade, "cell 4 0,alignx center");
		
		AlarmView av = new AlarmView(cardsPanel, prefs , lblAlarmIcon);
		
		cardsPanel.add(av, Constants.ALARM_VIEW);
		cardsPanel.add(weatherConfig, Constants.WEATHER_CONFIG_VIEW);	
		cardsPanel.add(forecastView, Constants.WEATHER_FORECAST_VIEW);
		cardsPanel.add(weatherAlertView, Constants.WEATHER_ALERT_VIEW);
		cardsPanel.add(radioStationsView, Constants.RADIO_STATION_VIEW);
		
		
		//settings
		ConfigView configView = new ConfigView();
		cardsPanel.add(configView, Constants.CONFIG_VIEW);
		
		//options menu popups
		optionsPopup();
		
		ct.putSharedObject(Constants.CARD_PANEL, cardsPanel);		

		if (prefs.isAlarmOn()){
			lblAlarmIcon.setVisible(true);
		}			
		
		themes.loadSunnyBackdrop(); //start the theme	
		
		PiHandler.init();
		
		//start the LDR
		ldrWorker = new LDRStatusWorker();
		scheduler.scheduleWithFixedDelay(ldrWorker, 1, 10, TimeUnit.SECONDS);	
		
		//check if we have wifi credentials.
		if (prefs.isWifiCredentialProvided()){
			PiHandler.checkInternetConnection();		
		}
			
	}
	/**change the background image **/
	public void changeBackImage(File backImage){
		try {
			BufferedImage img = ImageIO.read(backImage);
			dimg = img.getScaledInstance(480, 320, Image.SCALE_SMOOTH);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		cardsPanel.repaint();
	}
	
	private void clock(){		
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("EEE, MMM d");
		Date dt = new Date();
		clockLabel.setText(sdfTime.format(dt));
		weekDateLable.setText(sdfDate.format(dt));
		
		Calendar dtf = Calendar.getInstance();
		dtf.setTime(dt);
		dtf.add(Calendar.MINUTE, 1);
		dtf.set(Calendar.SECOND, 00);
		dtf.set(Calendar.MILLISECOND, 00);
		
		long delay = dtf.getTimeInMillis() - dt.getTime();
		
		scheduler.scheduleAtFixedRate(new Clock(clockLabel, weekDateLable), delay, 60000, TimeUnit.MILLISECONDS);
		
		
		
//		final SimpleDateFormat sdfDate = new SimpleDateFormat("EEE, MMM d");
//		
//		ActionListener updateClockAction = new ActionListener() {
//			@Override
//			  public void actionPerformed(ActionEvent e) {
//				Date dt = new Date();
//					clockLabel.setText(sdfTime.format(dt));
//					weekDateLable.setText(sdfDate.format(dt));
//					
//					
//			    }			
//			};
//		
//		Timer t = new Timer(1000,updateClockAction );
//		
//		t.start();	

	}
	/**
	 * option menu
	 */
	private void optionsPopup(){
		options = new JPopupMenu("Menu");

		JMenuItem alarm = new JMenuItem("Alarm");
		alarm.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					keepAliveIfScreenShutdown();
					CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
					cardLayout.show(cardsPanel, Constants.ALARM_VIEW);
				}catch (Exception ex){
					logger.log(Level.SEVERE, "Error in Option alarm", ex);
				}
			}
		});
		alarm.setFont(new Font("Tahoma", Font.BOLD, 20));
		
		JMenuItem forecast = new JMenuItem("Weather Config");
		forecast.setFont(new Font("Tahoma", Font.BOLD, 20));
		forecast.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					keepAliveIfScreenShutdown();
					CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
					cardLayout.show(cardsPanel, Constants.WEATHER_CONFIG_VIEW);
				}catch (Exception ex){
					logger.log(Level.SEVERE, "Error in Option weather config", ex);
				}
			}
		});
		
		JMenuItem radio = new JMenuItem("Radio");
		radio.setFont(new Font("Tahoma", Font.BOLD, 20));
		radio.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					keepAliveIfScreenShutdown();
					CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
					cardLayout.show(cardsPanel, Constants.RADIO_STATION_VIEW);

				}catch (Exception ex){
					logger.log(Level.SEVERE, "Error in Option radio", ex);
				}
			}
		});
		
		JMenuItem mp3Player = new JMenuItem("Mp3");
		mp3Player.setFont(new Font("Tahoma", Font.BOLD, 20));
		
//		JMenuItem webServer = new JMenuItem("Web Server");
//		webServer.setFont(new Font("Tahoma", Font.BOLD, 20));
		
		JMenuItem settings = new JMenuItem("Settings");
		settings.setFont(new Font("Tahoma", Font.BOLD, 20));
		settings.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					keepAliveIfScreenShutdown();//keep the screen alive if the screen is temporary turned on.
					CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
					cardLayout.show(cardsPanel, Constants.CONFIG_VIEW);
				}catch (Exception ex){
					logger.log(Level.SEVERE, "Error in Option settings", ex);
				}
			}
		});		
		options.add(alarm);
		options.add(radio);
		options.add(mp3Player);
		options.add(forecast);
//		options.add(webServer);
		options.add(settings);		
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		logger.config("--propertyChange--: " + evt.getPropertyName());
		if (evt.getPropertyName().equals(Constants.SENSOR_INFO)) {
			WeatherBean wb = (WeatherBean)evt.getNewValue();			
			
			addCurrentTemp(Constants.numberFormat.format(wb.getTempSun().getTempC()),
					Constants.numberFormat.format(wb.getTempShade().getTempC()));
		}
		else if (evt.getPropertyName().equals(Constants.FORECAST_RESULT)){
			try {
				logger.config("Auto Forecast result event ");
				
				WeatherGenericModel wgm = (WeatherGenericModel)evt.getNewValue();
				
				WeatherCurrentModel wcm = wgm.getWeatherCurrentModel();
				ImageIcon icon = ImageUtils.getInstance().getImage("weather" + File.separatorChar + wcm.getIconName());
				
				if (sensorActive){
					WeatherBean wb = (WeatherBean) ct.getSharedObject(Constants.SENSOR_INFO);
					
					if (wb == null){
						wb = new WeatherBean();
					}
					Date dt = parseToDate.parse(wcm.getObservationTime()); 
					
					addCurrentWeather(wcm.getWeather(),icon,dt );
				}else{
					
					Date dt = parseToDate.parse(wcm.getObservationTime());					
					addCurrentWeather(wcm.getWeather(),icon , dt);
					addCurrentTemp(String.valueOf(wcm.getCurrectTempC()), "--");
				}				

				if (wgm.getWeatherAlert() != null){ 
					logger.config("Alert recieved: ");
					lblWeatherAlert.setVisible(true);
				}
			} catch (IOException | ParseException e) {
				addCurrentWeather("Error!!", weatherNaIcon , null);
				addCurrentTemp("--", "--");
				logger.log(Level.SEVERE, "error", e);
			}

		}else if (evt.getPropertyName().equals(Constants.FETCH_FORECAST)){
			logger.config("FETCHING forecast on request.");
			int active = (int)evt.getNewValue(); //positive number weather is active
			if (active > 0){
				fetchForecast(0);
			}else{
				addCurrentWeather("Not Available",weatherNaIcon, null);
				addCurrentTemp("--", "--");
				//kill worker if running.
				killWeatherWorker();
				lblWeatherAlert.setVisible(false);

			}
		}else if (evt.getPropertyName().equals(Constants.FORECAST_DISPLAY_LOAD)){
			logger.config("Display forecast load animation (" + Constants.FORECAST_DISPLAY_LOAD + ")");
			displayCurrentForecastLoading();

		}else if(evt.getPropertyName().equals(Constants.FORECAST_DISPLAY_ERROR)){
			logger.config("Error in forecast ("+Constants.FORECAST_DISPLAY_ERROR+")");
			WeatherGenericModel wgm = (WeatherGenericModel)evt.getNewValue();			
			
			MessageHandl msg =  wgm.getMessages();
			
			Message m = msg.getAllMessages().get( msg.getAllMessages().size() - 1);
			
			addCurrentWeather(m.getTitle() + " " + new SimpleDateFormat(" EEE HH:mm:ss").format(m.getRecDate()),
					weatherNaIcon , null);
			addCurrentTemp("--", "--");
			lblWeatherAlert.setVisible(false);
			killWeatherWorker();
		}else  if(evt.getPropertyName().equals(Constants.THEMES_BACKGROUND_IMG_UPDATE)){

			changeBackImage(new File((String)evt.getNewValue()));
			
		}else if(evt.getPropertyName().equals(Constants.CHECK_INTERNET)){
			try{
				String initValue = ((String)evt.getNewValue());
				String value = initValue.substring(4,initValue.length() );
				logger.config("CHECK INTERNET : " + evt.getNewValue() + " - Value: " + value);

				final ImageIcon wifiOn = themes.getIcon(IconEnum.WIFI_ON_ICON);
				final ImageIcon wifiOFF = themes.getIcon(IconEnum.WIFI_OFF_ICON);
				if ("starting".equals(initValue)){

					lblWiFiIcon.setVisible(true);
					
					ActionListener blink = new ActionListener(){					
						public void actionPerformed(ActionEvent e) {

							if (blinking){
								lblWiFiIcon.setIcon(null);
								blinking = false;
							}else {
								lblWiFiIcon.setIcon(wifiOn);
								blinking = true;
							}
						}
					};
					blinkingWifiTimer = new Timer( 1000 , blink);
					blinkingWifiTimer.setInitialDelay(0);
					blinkingWifiTimer.start();

				}else if ("success".equals(value)){
					blinkingWifiTimer.stop();
					lblWiFiIcon.setIcon(wifiOn);
					themes.registerIconColor(lblWiFiIcon, IconEnum.WIFI_ON_ICON);
					callProgThatReqWiFi();

				}else if ("timeout".equals(value) || "interrupted".equals(value) || "disconnect".equals(value)){
					lblWiFiIcon.setVisible(true);
					lblWiFiIcon.setIcon(wifiOFF);
					themes.registerIconColor(lblWiFiIcon, IconEnum.WIFI_OFF_ICON);
					
					if (blinkingWifiTimer != null){
						blinkingWifiTimer.stop();
					}
				}else if ("no_inet".equals(value)){
					lblWiFiIcon.setVisible(true);
					lblWiFiIcon.setIcon(themes.getIcon(IconEnum.WIFI_ON_NO_INET));
					themes.registerIconColor(lblWiFiIcon, IconEnum.WIFI_ON_NO_INET);
					
					if (blinkingWifiTimer != null){
						blinkingWifiTimer.stop();
					}
					
				}else if ("wifiOff".equals(value)){
					lblWiFiIcon.setVisible(false);
				}
			}catch (Exception ex){
				logger.log(Level.SEVERE, "Error in blinking wifi", ex);
			}
		}
	}		
 
	private void addCurrentWeather(String status, Object icon,  Date refreshDate){
		lblCurrentweather.setText("<html><div style='width: 80px;word-wrap: break-word;text-align: center'>" + status + "</html>");
		if (refreshDate != null){
			lblWeatherIcon.setText(sdfTime.format(refreshDate));
		}else{
			lblWeatherIcon.setText("");
		}
			
		if (icon != null){
			if (icon instanceof String){
				lblWeatherIcon.setIcon(new ImageIcon((String)icon));
			}else{
				lblWeatherIcon.setIcon((ImageIcon)icon);
			}
		}else{
			lblWeatherIcon.setIcon(weatherNaIcon);
		}			
	}
	private void addCurrentTemp(String tempSun, String tempShade) {
		//TODO handle nulls
		lblTempSun.setText(tempSun+"c");
		lblTempShade.setText(tempShade+"c");
	}
	private void displayCurrentForecastLoading(){
		lblCurrentweather.setText("<html><div style='width: 80px;word-wrap: break-word;text-align: center'> LOADING...</html>");
		lblTempSun.setText("--c");
		lblTempShade.setText("--c");
		lblWeatherIcon.setIcon(ImageUtils.getInstance().getWeatherLoader());
		lblWeatherIcon.setText("");
	}
	
	@SuppressWarnings("unchecked")
	private void fetchForecast(int initDelay){		
		logger.config("Fetching forecast (fetchForecast())");
		
		prefs = (Preferences)ct.getSharedObject(Constants.PREFERENCES);		
		
		killWeatherWorker();

		Host provider = Host.valueOf(prefs.getWeatherProvider());
		if (provider == Host.envCanada){
			envCanThread = (ScheduledFuture<EnvCanWorker>) scheduler.scheduleAtFixedRate(new EnvCanWorker(), initDelay, prefs.getWeatherRefresh(), TimeUnit.MINUTES);
		}else if (provider == Host.weatherUnderground){
			//TODO fetch wther undr
		}
	}	
	private void killWeatherWorker(){
		logger.config("killing Weather thread (killWeatherWorker())");
		if (envCanThread != null){
			
			if (!envCanThread.isDone()){
			
				envCanThread.cancel(true); //cancel worker if running
				
				logger.config("Thread done: " +  envCanThread.isDone());
				//wait until cancelled
				while(!envCanThread.isDone()){
					try {
						Thread.sleep(40);
					} catch (InterruptedException e1) {}
				}
			}
		}//TODO Add weather undr		
		
	}
	/**now we know that the wifi just connected, then call programs that require wifi **/ 
	private void callProgThatReqWiFi(){
		Preferences pref = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
	
		if (pref.isWeatherActivated() ){
			//call 
			if (sensorActive){
				fetchSensorInfo();
			}
			fetchForecast(0);
		}
	}
	/** method to use to keep the screen ON if the screen is off and the user is doing something 
	 * @throws InterruptedException **/ 
	private void keepAliveIfScreenShutdown() throws InterruptedException{
		//if screen is in auto shutdown mode , them restart the countdown when user do something.
		logger.log(Level.CONFIG, "keepAliveIfScreenShutdown. PiHandler.isScreenAutoShutdown: " + PiHandler.isScreenAutoShutdown);
		if (PiHandler.isScreenAutoShutdown){			
			PiHandler.autoShutDownScreen();
		}
	}
	@SuppressWarnings("unchecked")
	private void fetchSensorInfo(){ //every 5 min
		logger.config("killing fetchSensorInfo");
		if (sensorThread != null){
			
			if (!sensorThread.isDone()){
			
				sensorThread.cancel(true); //cancel worker if running
				
				logger.config("sensorThread Thread done: " +  sensorThread.isDone());
				//wait until cancelled
				while(!sensorThread.isDone()){
					try {
						Thread.sleep(40);
					} catch (InterruptedException e1) {}
				}
			}
		}		
		sensorThread = (ScheduledFuture<TempSensorWorker>) scheduler.scheduleAtFixedRate(new TempSensorWorker(), 0, 5, TimeUnit.MINUTES);
	}
}