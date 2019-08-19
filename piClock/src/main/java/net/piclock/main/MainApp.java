package net.piclock.main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
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
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorType;
import net.piclock.bean.VolumeConfig;
import net.piclock.db.entity.AlarmEntity;
import net.piclock.db.sql.AlarmSql;
import net.piclock.enums.CheckWifiStatus;
import net.piclock.enums.IconEnum;
import net.piclock.enums.LabelEnums;
import net.piclock.swing.component.MessageListener;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeEnum;
import net.piclock.theme.ThemeHandler;
import net.piclock.thread.ScreenAutoClose;
import net.piclock.thread.ThreadManager;
import net.piclock.util.FormatStackTrace;
import net.piclock.util.ImageUtils;
import net.piclock.util.LogConfig;
import net.piclock.util.PreferencesHandler;
import net.piclock.util.VolumeIndicator;
import net.piclock.view.AlarmView;
import net.piclock.view.ConfigView;
import net.piclock.view.ErrorView;
import net.piclock.view.Mp3View;
import net.piclock.view.RadioStationsView;
import net.piclock.view.VolumeNew;
import net.piclock.view.WeatherAlertView;
import net.piclock.view.WeatherConfigView;
import net.piclock.view.WeatherForecastView;
import net.piclock.view.WebServerView;
import net.piclock.weather.DarkSkyUtil;
import net.piclock.weather.Temperature;
import net.piclock.weather.WeatherBean;
import net.weather.bean.Message;
import net.weather.bean.WeatherCurrentModel;
import net.weather.bean.WeatherGenericModel;
import net.weather.enums.Host;
import net.weather.utils.MessageHandl;
import java.awt.FlowLayout;

public class MainApp extends JFrame implements PropertyChangeListener, MessageListener {
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
	private AlarmView av;
	private WebServerView webServerView;
	private Mp3View mp3View;
	
	//wifi blinking
	private boolean blinking = true;
	private Timer blinkingWifiTimer;

	private boolean sensorActive = true; //for the external temperature sensors.. only if available.
	
	private final SimpleDateFormat sdfTime = new SimpleDateFormat(Constants.HOUR_MIN);
	private SimpleDateFormat parseToDateEnv = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
	private SimpleDateFormat parseToDateDsky = new SimpleDateFormat("yyyy-mm-dd HH:mm.ss");
	
	private PiHandler handler;
	private ThreadManager tm ;
	private JPanel btnPanel;
	private JButton btnVolume;
	private JLabel lblWarningIcon;
	
	private ErrorHandler eh;
	/**
	 * Launch the application.
	 */
	public static void main(String... args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LogConfig logs = new LogConfig();
//					OFF SEVERE	WARNING	INFO CONFIG(is debug),  FINE FINER FINEST ALL  -- height 480 - width 800
					logs.configLogs("mainAppLog", Level.CONFIG, true, true);  					
					
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
		
		ErrorView ev = new ErrorView();
		
		tm = ThreadManager.getInstance();
		 eh = new ErrorHandler();
		
		ct.putSharedObject(Constants.ERROR_HANDLER, eh);
		
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
		ct.addPropertyChangeListener(Constants.RADIO_VOLUME_ICON_TRIGGER, this);
		ct.addPropertyChangeListener(Constants.MP3_VOLUME_ICON_TRIGGER, this);
		
		ct.addMessageChangeListener(Constants.ERROR_BROADCAST, this);
				
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
		lblRadioIcon = new JLabel();		
		
		weatherConfig = new WeatherConfigView();
		forecastView = new WeatherForecastView();
		weatherAlertView = new WeatherAlertView();	
		radioStationsView = new RadioStationsView(lblRadioIcon);
		mp3View				   = new Mp3View();
		
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
		timePanel.setLayout(new MigLayout("", "[][grow 68][][grow 70,right]", "[][grow 40][center][][grow 50][]"));
		timePanel.setOpaque(false);
		
		clockLabel = new JLabel("00:00");
		clockLabel.setFont(new Font("Courier New", Font.BOLD, 120));
		themes.registerLabelTextColor(clockLabel, LabelEnums.CLOCK);
		timePanel.add(clockLabel, "cell 2 2,alignx center,aligny center");
		
		weekDateLable = new JLabel("Fri, dec 21");
		themes.registerLabelTextColor(weekDateLable, LabelEnums.DAY_OF_WEEK);
		weekDateLable.setFont(new Font("Tahoma", Font.BOLD, 40));
		timePanel.add(weekDateLable, "cell 2 3,alignx center,aligny top");
		lblWebserverIcon = new JLabel();
		lblWebserverIcon.setVisible(false);
//		timePanel.add(lblWebserverIcon, "cell 0 5");
		lblWebserverIcon.setBorder(new EmptyBorder(10,10,0,0));
		lblWebserverIcon.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try{
					keepAliveIfScreenShutdown();//keep the screen alive if the screen is temporary turned on.
					CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
					cardLayout.show(cardsPanel, Constants.WEB_SERVER_VIEW);
				}catch (Exception ex){
					String fmtEx = new FormatStackTrace(ex).getFormattedException();
					eh.addError(ErrorType.GENERAL, new ErrorInfo(fmtEx));
					logger.log(Level.SEVERE, "Error in mouse clicked", ex);
				}
			}
		});		
		themes.registerIconColor(lblWebserverIcon, IconEnum.WEB_SERVER);
		
		btnPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) btnPanel.getLayout();
		flowLayout.setHgap(15);
		btnPanel.setOpaque(false);
		flowLayout.setAlignment(FlowLayout.RIGHT);
		timePanel.add(btnPanel, "cell 3 5,grow");
		
		JPanel leftIcons = new JPanel();
		leftIcons.setOpaque(false);
		timePanel.add(leftIcons, "cell 0 5 2 1,alignx left,growy");
		
		leftIcons.add(lblWebserverIcon);  
		
		btnVolume = new JButton("");
		btnVolume.setFocusPainted(false);
		btnVolume.setOpaque(false);
		btnVolume.setContentAreaFilled(false);
		btnVolume.setBorderPainted(false);
		btnVolume.setVisible(false);
		btnVolume.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.log(Level.CONFIG, "Loading volume : ---> " + prefs.getLastVolumeLevel());
				VolumeConfig config = new VolumeConfig(prefs.getLastVolumeLevel());
				VolumeNew vol = new VolumeNew(config);
//				Volume vol = new Volume(btnVolume, IconEnum.VOLUME_ICON, IconEnum.VOLUME_MUTED);
				vol.setVisible(true);
				
			}
		});
		btnPanel.add(btnVolume);
		themes.registerIconColor(btnVolume, IconEnum.VOLUME_ICON);
		
		btnOptionsIcon = new JButton("");
		btnPanel.add(btnOptionsIcon);
		btnOptionsIcon.setFocusPainted(false);
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
		weatherPanel.setLayout(new MigLayout("", "[grow 14][220px,left][][50px][80px][80px][grow 14]", "[]"));
		weatherPanel.setOpaque(false);
		
		lblCurrentweather = new JLabel("<html><div style='width: 100px;word-wrap: break-word;text-align: center'>Not available</html>");
		lblCurrentweather.setFont(new Font("Tahoma", Font.BOLD, 30));
		lblCurrentweather.setForeground(Color.WHITE);
		themes.registerLabelTextColor(lblCurrentweather, LabelEnums.CURRENT_WEATHER);
		weatherPanel.add(lblCurrentweather, "cell 1 0,alignx center");
		
		lblWeatherIcon = new JLabel("--:--",weatherNaIcon,  SwingConstants.CENTER);
		lblWeatherIcon.setFont(new Font("Tahoma", Font.PLAIN, 18));
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
		alertIconsPanel.setPreferredSize(new Dimension(61, 10));
		alertIconsPanel.setMinimumSize(new Dimension(35, 10));
		mainPanel.add(alertIconsPanel, BorderLayout.WEST);
		alertIconsPanel.setOpaque(false);
		alertIconsPanel.setLayout(new BoxLayout(alertIconsPanel, BoxLayout.Y_AXIS));
		
		lblAlarmIcon = new JLabel();
		lblAlarmIcon.setBorder(new EmptyBorder(5,10,0,0));//top,left,bottom,right
		lblAlarmIcon.setVisible(false);
		themes.registerIconColor(lblAlarmIcon, IconEnum.ALARM_ICON);
		alertIconsPanel.add(lblAlarmIcon);
		lblAlarmIcon.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				try{
					keepAliveIfScreenShutdown();
					av.setAlarmNotToggled();
					CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
					cardLayout.show(cardsPanel, Constants.ALARM_VIEW);
				}catch (Exception ex){
					String fmtEx = new FormatStackTrace(ex).getFormattedException();
					eh.addError(ErrorType.ALARM, new ErrorInfo(fmtEx));
					logger.log(Level.SEVERE, "Error in lblAlarmIcon", ex);
				}
			}
		});
		
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
				
		lblRadioIcon.setVisible(false);
		lblRadioIcon.setBorder(new EmptyBorder(10,10,0,0));//top,left,bottom,right
		themes.registerIconColor(lblRadioIcon, IconEnum.RADIO_ICON);
		lblRadioIcon.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try{
					keepAliveIfScreenShutdown();
					CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
					cardLayout.show(cardsPanel, Constants.RADIO_STATION_VIEW);

				}catch (Exception ex){
					String fmtEx = new FormatStackTrace(ex).getFormattedException();
					eh.addError(ErrorType.RADIO, new ErrorInfo(fmtEx));
					logger.log(Level.SEVERE, "Error in mouse listener radio", ex);
				}
			}
		});
		
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
		weatherPanel.add(lblTempSun, "cell 4 0,alignx right,aligny center");
		
		lblTempShade = new JLabel("--c");
		lblTempShade.setFont(new Font("Tahoma", Font.BOLD, 20));
		themes.registerIconColor(lblTempShade, IconEnum.TEMP_SHADE);
		themes.registerLabelTextColor(lblTempShade, LabelEnums.TEMP_SHADE);
		weatherPanel.add(lblTempShade, "cell 5 0,alignx right");
		
		av = new AlarmView(cardsPanel, prefs , lblAlarmIcon);
		webServerView = new WebServerView(lblWebserverIcon);	
		
		cardsPanel.add(ev, Constants.ERROR_VIEW);
		cardsPanel.add(mp3View, Constants.MP3_VIEW);
		lblWarningIcon = new JLabel(ImageUtils.getInstance().getWarningIcon());
		lblWarningIcon.setVisible(false);
		lblWarningIcon.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try{
					ev.populateScreen();
					keepAliveIfScreenShutdown();
					CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
					cardLayout.show(cardsPanel, Constants.ERROR_VIEW);

				}catch (Exception ex){
					String fmtEx = new FormatStackTrace(ex).getFormattedException();
					eh.addError(ErrorType.GENERAL, new ErrorInfo(fmtEx));
					logger.log(Level.SEVERE, "Error in mouse listener warning icon", ex);
				}
			}
		});
		leftIcons.add(lblWarningIcon);
		
		cardsPanel.add(av, Constants.ALARM_VIEW);
		cardsPanel.add(weatherConfig, Constants.WEATHER_CONFIG_VIEW);	
		cardsPanel.add(forecastView, Constants.WEATHER_FORECAST_VIEW);
		cardsPanel.add(weatherAlertView, Constants.WEATHER_ALERT_VIEW);
		cardsPanel.add(radioStationsView, Constants.RADIO_STATION_VIEW);
		cardsPanel.add(webServerView, Constants.WEB_SERVER_VIEW);
		
		//settings
		ConfigView configView = new ConfigView();
		cardsPanel.add(configView, Constants.CONFIG_VIEW);
		
		//options menu popups
		optionsPopup();
		
		ct.putSharedObject(Constants.CARD_PANEL, cardsPanel);		

		AlarmEntity ae = new AlarmSql().loadActiveAlarm();
		if (ae != null && ae.isActive()){
			lblAlarmIcon.setVisible(true);
		}			
		
		themes.loadSunnyBackdrop(); //start the theme	
		
		handler = PiHandler.getInstance();
		
		//start the LDR
		tm.startLdr();
			
		//check if we have wifi credentials.
		if (prefs.isWifiCredentialProvided()){
			handler.checkInternetConnection(true);		
		}
		
		//ADD shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() 
	    { 
	      public void run() 
	      { 
	        handler.shutdown(); 
	      } 
	    }); 
					
	}
	/**change the background image **/
	public void changeBackImage(File backImage){
		try {
			BufferedImage img = ImageIO.read(backImage);
			dimg = img.getScaledInstance(800, 480, Image.SCALE_SMOOTH);
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
		
		tm.startClock(clockLabel, weekDateLable, delay);
		
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
					av.setAlarmNotToggled();
					CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
					cardLayout.show(cardsPanel, Constants.ALARM_VIEW);
				}catch (Exception ex){
					String fmtEx = new FormatStackTrace(ex).getFormattedException();
					eh.addError(ErrorType.ALARM, new ErrorInfo(fmtEx));
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
					String fmtEx = new FormatStackTrace(ex).getFormattedException();
					eh.addError(ErrorType.WEATHER, new ErrorInfo(fmtEx));
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
					String fmtEx = new FormatStackTrace(ex).getFormattedException();
					eh.addError(ErrorType.RADIO, new ErrorInfo(fmtEx));
					logger.log(Level.SEVERE, "Error in Option radio", ex);
				}
			}
		});
		
		JMenuItem mp3Player = new JMenuItem("Mp3");
		mp3Player.setFont(new Font("Tahoma", Font.BOLD, 20));

		mp3Player.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					keepAliveIfScreenShutdown();//keep the screen alive if the screen is temporary turned on.
					logger.log(Level.CONFIG, "Opening MP3 with volume: " + prefs.getLastVolumeLevel());
					ct.sendMessage(Constants.VOLUME_SENT_FOR_CONFIG,new net.piclock.swing.component.Message(prefs.getLastVolumeLevel()));
					CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
					cardLayout.show(cardsPanel, Constants.MP3_VIEW);
				}catch (Exception ex){
					String fmtEx = new FormatStackTrace(ex).getFormattedException();
					eh.addError(ErrorType.GENERAL, new ErrorInfo(fmtEx));
					logger.log(Level.SEVERE, "Error in Option settings", ex);
				}
			}
		});		
		
		
		JMenuItem webServer = new JMenuItem("Web Server");
		webServer.setFont(new Font("Tahoma", Font.BOLD, 20));
		webServer.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					keepAliveIfScreenShutdown();//keep the screen alive if the screen is temporary turned on.
					CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
					cardLayout.show(cardsPanel, Constants.WEB_SERVER_VIEW);
				}catch (Exception ex){
					String fmtEx = new FormatStackTrace(ex).getFormattedException();
					eh.addError(ErrorType.GENERAL, new ErrorInfo(fmtEx));
					logger.log(Level.SEVERE, "Error in Option settings", ex);
				}
			}
		});		
		
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
					String fmtEx = new FormatStackTrace(ex).getFormattedException();
					eh.addError(ErrorType.GENERAL, new ErrorInfo(fmtEx));
					logger.log(Level.SEVERE, "Error in Option settings", ex);
				}
			}
		});		
		options.add(alarm);
		options.add(radio);
		options.add(mp3Player);
		options.add(forecast);
		options.add(webServer);
		options.add(settings);		
	}

	@Override
	public synchronized void propertyChange(PropertyChangeEvent evt) {
		logger.info("--propertyChange--: " + evt.getPropertyName());
		if (evt.getPropertyName().equals(Constants.SENSOR_INFO)) {
			WeatherBean wb = (WeatherBean)evt.getNewValue();	
			
			Temperature sun = wb.getTempSun().orElse(new Temperature(new Float(-999)));
			Temperature shade = wb.getTempSun().orElse(new Temperature(new Float(-999)));
			
			addCurrentTemp(Constants.numberFormat.format(sun.getTempC()),
					Constants.numberFormat.format(shade.getTempC()));
			
		}
		else if (evt.getPropertyName().equals(Constants.FORECAST_RESULT)){
			try {
				logger.info("Auto Forecast result event ");
				
				WeatherGenericModel wgm = (WeatherGenericModel)evt.getNewValue();
				
				WeatherCurrentModel wcm = wgm.getWeatherCurrentModel();
				
				Host host = Host.valueOf(prefs.getWeatherProvider());
				
				ImageIcon icon = null;
				if (host == Host.envCanada) {
					icon = ImageUtils.getInstance().getImage("weather" + File.separatorChar + wcm.getIconName(), 48, 48);	
				}else {
					icon = ImageUtils.getInstance().getImage("weather" + File.separatorChar + DarkSkyUtil.getIconFileName(wcm.getIconName()), 48, 48);	
				}			
				
				Date dt = null;
				try {
					dt = parseToDateEnv.parse(wcm.getObservationTime()); //env cadada format	
				}catch (ParseException px) {
					dt = parseToDateDsky.parse(wcm.getObservationTime()); //darksky format
				}
				
				if (sensorActive){		

					addCurrentWeather(wcm.getSummary().trim(),icon,dt );
					WeatherBean wb = (WeatherBean)ct.getSharedObject(Constants.SENSOR_INFO);
					if (wb != null) { 
						Temperature sun = wb.getTempSun().orElse(new Temperature(new Float(-999)));
						Temperature shade = wb.getTempSun().orElse(new Temperature(new Float(-999)));
						
						addCurrentTemp(Constants.numberFormat.format(sun.getTempC()),
								Constants.numberFormat.format(shade.getTempC()));
						
						
					}
				}else{			
												
					addCurrentWeather(wcm.getSummary().trim(),icon , dt);
					addCurrentTemp(String.valueOf(wcm.getCurrTemp()), "--");
				}				

				if (wgm.getWeatherAlert() != null){ 
					logger.config("Alert recieved: ");
					lblWeatherAlert.setVisible(true);
				}
			} catch (IOException | ParseException e) {
				addCurrentWeather("Error!!", weatherNaIcon , null);
				addCurrentTemp("--", "--");
				String fmtEx = new FormatStackTrace(e).getFormattedException();
				eh.addError(ErrorType.WEATHER, new ErrorInfo(fmtEx));
				
				logger.log(Level.SEVERE, "error", e);
			}

		}else if (evt.getPropertyName().equals(Constants.FETCH_FORECAST)){
			logger.info("FETCHING forecast on request.");
			int active = (int)evt.getNewValue(); //positive number weather is active
			if (active > 0){
				fetchForecast(0);
			}else{
				addCurrentWeather("Not Available",weatherNaIcon, null);
				addCurrentTemp("--", "--");
				//kill worker if running.
				tm.stopWeatherThread();
				lblWeatherAlert.setVisible(false);

			}
		}else if (evt.getPropertyName().equals(Constants.FORECAST_DISPLAY_LOAD)){
			logger.info("Display forecast load animation (" + Constants.FORECAST_DISPLAY_LOAD + ")");
			displayCurrentForecastLoading();

		}else if(evt.getPropertyName().equals(Constants.FORECAST_DISPLAY_ERROR)){
			logger.info("Error in forecast ("+Constants.FORECAST_DISPLAY_ERROR+")");
			WeatherGenericModel wgm = (WeatherGenericModel)evt.getNewValue();			
			
			MessageHandl msg =  wgm.getMessages();
			
			Message m = msg.getAllMessages().get( msg.getAllMessages().size() - 1);
			
			addCurrentWeather(m.getTitle() + " " + new SimpleDateFormat(" EEE HH:mm:ss").format(m.getRecDate()),
					weatherNaIcon , null);
			addCurrentTemp("--", "--");
			lblWeatherAlert.setVisible(false);
//			tm.stopWeatherThread(); TODO add function to not restart it if too much errors.
		}else  if(evt.getPropertyName().equals(Constants.THEMES_BACKGROUND_IMG_UPDATE)){
			String image = (String)evt.getNewValue();
			logger.info("Image background update. Image: " + image);
			changeBackImage(new File(image));
			
		}else if(evt.getPropertyName().equals(Constants.CHECK_INTERNET)){
			try{
				CheckWifiStatus status = (CheckWifiStatus) evt.getNewValue();
				
				logger.info("CHECK INTERNET : " + evt.getNewValue() + " - Value: " + status);

				final ImageIcon wifiOn = themes.getIcon(IconEnum.WIFI_ON_ICON);
				final ImageIcon wifiOFF = themes.getIcon(IconEnum.WIFI_OFF_ICON);
				if (status == CheckWifiStatus.STARTING){

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

				}else if (status == CheckWifiStatus.SUCCESS){
					blinkingWifiTimer.stop();
					lblWiFiIcon.setIcon(wifiOn);
					themes.registerIconColor(lblWiFiIcon, IconEnum.WIFI_ON_ICON);
					callProgThatReqWiFi();

				}else if (status == CheckWifiStatus.END_TIMEOUT || status == CheckWifiStatus.END_INTERRUPTED || status == CheckWifiStatus.END_DISCONNECT
						|| status == CheckWifiStatus.END_WIFI_OFF){
					lblWiFiIcon.setVisible(true);
					lblWiFiIcon.setIcon(wifiOFF);
					themes.registerIconColor(lblWiFiIcon, IconEnum.WIFI_OFF_ICON);
					
					if (blinkingWifiTimer != null){
						blinkingWifiTimer.stop();
					}
					//when wifi problems, radio off
					
					
				}else if (status == CheckWifiStatus.END_NO_INET ){
					lblWiFiIcon.setVisible(true);
					lblWiFiIcon.setIcon(themes.getIcon(IconEnum.WIFI_ON_NO_INET));
					themes.registerIconColor(lblWiFiIcon, IconEnum.WIFI_ON_NO_INET);
					
					if (blinkingWifiTimer != null){
						blinkingWifiTimer.stop();
					}
					
				}
//				else if (status == CheckWifiStatus.END_WIFI_OFF){
//					lblWiFiIcon.setVisible(false);
//				}
			}catch (Exception ex){
				String fmtEx = new FormatStackTrace(ex).getFormattedException();
				eh.addError(ErrorType.WIFI, new ErrorInfo(fmtEx));
				logger.log(Level.SEVERE, "Error in blinking wifi", ex);
			}
		}else if(evt.getPropertyName().equals(Constants.RADIO_VOLUME_ICON_TRIGGER) ||
				evt.getPropertyName().equals(Constants.MP3_VOLUME_ICON_TRIGGER)){
			logger.log(Level.CONFIG, "Volume icon indicator");
			VolumeIndicator vi = (VolumeIndicator)evt.getNewValue();
			btnVolume.setVisible(vi.displayVolumeIcon());
		}
	}		
 
	private void addCurrentWeather(String status, Object icon,  Date refreshDate){
		lblCurrentweather.setText("<html><div style='width: 100px;word-wrap: break-word;text-align: center'>" + status + "</html>");
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
	
	private void fetchForecast(int initDelay){		
		logger.config("Fetching forecast (fetchForecast())");

		prefs = (Preferences)ct.getSharedObject(Constants.PREFERENCES);		

		tm.stopWeatherThread();
		
		tm.startWeatherThread(initDelay, prefs);

	}	

	/**now we know that the wifi just connected, then call programs that require wifi **/ 
	private void callProgThatReqWiFi(){
		Preferences pref = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
	
		if (pref.isWeatherActivated() ){
			//call
			fetchForecast(0);
		}
		
		if (sensorActive){
			fetchSensorInfo();
		}
	}
	/** method to use to keep the screen ON if the screen is off and the user is doing something 
	 * @throws InterruptedException **/ 
	private void keepAliveIfScreenShutdown() throws InterruptedException{
		//if screen is in auto shutdown mode , them restart the countdown when user do something.
		logger.log(Level.CONFIG, "keepAliveIfScreenShutdown. PiHandler.isAutoShutdownInProgress: " + handler.isAutoShutdownInProgress());
		if (handler.isAutoShutdownInProgress()){			
			handler.autoShutDownScreen(20000);
			
		}
		if (handler.isWifiAutoShutdownInProgress()) {
			handler.autoWifiShutDown(true);
		}
	}
	private void fetchSensorInfo(){ //every 5 min
		
		tm.stopSensorThread();
		
		tm.startSensorThread();
	}

	@Override
	public void message(net.piclock.swing.component.Message message) {
		logger.log(Level.INFO,"Message property: " +  message.getPropertyName() + " vakue: " + message);
		if (message.getPropertyName().equals(Constants.ERROR_BROADCAST)) {
			boolean displayIcon = (boolean) message.getFirstMessage();
			if (displayIcon) {
				lblWarningIcon.setVisible(true);
			}else {
				lblWarningIcon.setVisible(false);
			}
		}
		
	}
}