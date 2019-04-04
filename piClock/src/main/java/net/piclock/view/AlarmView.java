package net.piclock.view;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.metal.MetalToggleButtonUI;

import net.piclock.enums.Buzzer;
import net.piclock.enums.LabelEnums;
import net.piclock.main.Constants;
import net.piclock.main.PiHandler;
import net.piclock.main.Preferences;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;
import net.piclock.util.PreferencesHandler;

public class AlarmView extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = -8991056994673610266L;

	private static final Logger logger = Logger.getLogger( AlarmView.class.getName() );
	
	public static boolean alarmOn = false;
	
	private JToggleButton tglbtnOnOff;
	private int hours = 0;
	private int minutes = 0;
	private JLabel lblHours;
	private JLabel lblMinutes;
	
	private ScheduledExecutorService alarmScheduler = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> alarmThread ;
	private SwingContext ct = SwingContext.getInstance();
	
	Thread timeCounter;
	private boolean keepRunning = true;
	private int btnDelay = 300;
	
	private BuzzerOptionDialog wakeUpAlarmOptions;
	private JButton btnBuzzer;
	

	private boolean prefChanged = false;
	
	
	/**
	 * Create the panel.
	 */
	public AlarmView(final JPanel cardsPanel, final Preferences prefs, final JLabel lblAlarm) {		
		logger.config("Starting alarmView");
		setLayout(null);
		setOpaque(false);
		
		ThemeHandler theme = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
		
		ct.addPropertyChangeListener(Constants.BUZZER_CHANGED, this);	

		JLabel lblAlarmTitle = new JLabel("Alarm");
		lblAlarmTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblAlarmTitle.setFont(new Font("Tahoma", Font.BOLD, 35));
		lblAlarmTitle.setBounds(304, 11, 194, 46);
		add(lblAlarmTitle);
		theme.registerLabelTextColor(lblAlarmTitle, LabelEnums.ALARM_TITLE);

		hours = prefs.getAlarmHour();
		minutes = prefs.getAlarmMinutes();	
		
		JButton btnHoursPlus = new JButton("+");
		btnHoursPlus.setFont(new Font("tahoma", Font.BOLD, 20));
		btnHoursPlus.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
					
				keepRunning = true;
				prefChanged = true;
				timeCounter = new Thread(new Runnable(){

					@Override
					public void run() {
						while(keepRunning){
							hours ++;
							if (hours == 24){
								hours = 0;
							}				
							lblHours.setText(String.valueOf(hours));							
							try {
								Thread.sleep(btnDelay);
							} catch (InterruptedException e) {}
						}	
					}					
				});
				timeCounter.start();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				keepRunning = false;
				timeCounter.interrupt();
			}
		});
		btnHoursPlus.setBounds(240, 95, 55, 40);
		add(btnHoursPlus);

		JButton btnMinPlus = new JButton("+");
		btnMinPlus.setFont(new Font("Tahoma", Font.BOLD, 20));
		btnMinPlus.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
				keepRunning = true;
				prefChanged = true;
				timeCounter = new Thread(new Runnable(){

					@Override
					public void run() {
						while(keepRunning){
							minutes ++;
							if (minutes == 60){
								minutes = 0;
							}	
							String min = (minutes < 10 ? "0"+minutes : String.valueOf(minutes));

							lblMinutes.setText(min);
							
							try {
								Thread.sleep(btnDelay);
							} catch (InterruptedException e) {}
						}
					}					
				});
				timeCounter.start();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				keepRunning = false;
				timeCounter.interrupt();
			}
		});

		btnMinPlus.setBounds(458, 95, 55, 40);
		add(btnMinPlus);

		lblHours = new JLabel(String.valueOf(hours));
		lblHours.setFont(new Font("Tahoma", Font.BOLD, 80));
		lblHours.setHorizontalAlignment(SwingConstants.CENTER);
		lblHours.setBounds(193, 131, 130, 145);
		add(lblHours);
		theme.registerLabelTextColor(lblHours, LabelEnums.ALARM_HOUR);


		lblMinutes = new JLabel(String.valueOf(minutes));
		lblMinutes.setHorizontalAlignment(SwingConstants.CENTER);
		lblMinutes.setFont(new Font("Tahoma", Font.BOLD, 80));
		lblMinutes.setBounds(429, 131, 130, 145);
		add(lblMinutes);
		theme.registerLabelTextColor(lblMinutes, LabelEnums.ALARM_MIN);

		JButton btnHourMinus = new JButton("-");
		btnHourMinus.setFont(new Font("Tahoma", Font.BOLD, 20));
		btnHourMinus.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
				keepRunning = true;
				prefChanged = true;
				timeCounter = new Thread(new Runnable(){

					@Override
					public void run() {
						while(keepRunning){
							hours --;
							if (hours == -1){
								hours = 23;
							}				
							lblHours.setText(String.valueOf(hours));
							
							try {
								Thread.sleep(btnDelay);
							} catch (InterruptedException e) {}
						}	
					}					
				});
				timeCounter.start();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				keepRunning = false;
				timeCounter.interrupt();
			}
		});
		

		btnHourMinus.setBounds(240, 336, 55, 40);
		add(btnHourMinus);

		//Minutes button minus 
		JButton btnMinMinus = new JButton("-");
		btnMinMinus.setFont(new Font("tahoma", Font.BOLD, 20));
		btnMinMinus.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
				keepRunning = true;
				prefChanged = true;
				timeCounter = new Thread(new Runnable(){

					@Override
					public void run() {
						System.out.println("Thread running");
						while(keepRunning){
							minutes --;
							if (minutes == -1){
								minutes = 59;
							}	
							String min = (minutes < 10 ? "0"+minutes : String.valueOf(minutes));

							lblMinutes.setText(min);
							try {
								Thread.sleep(btnDelay);
							} catch (InterruptedException e) {}
						}	
					}					
				});
				timeCounter.start();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				keepRunning = false;
				timeCounter.interrupt();
			}
		});
			
		btnMinMinus.setBounds(458, 335, 55, 40);
		add(btnMinMinus);

		tglbtnOnOff = new JToggleButton("Alarm OFF");
		tglbtnOnOff.setFont(new Font("Tahoma", Font.PLAIN, 18));
		tglbtnOnOff.setUI(new MetalToggleButtonUI() {
			@Override
			protected Color getSelectColor() {

				return Color.GREEN;
			}
		});
		tglbtnOnOff.setBackground(Color.RED);
		tglbtnOnOff.setBounds(615, 160, 140, 40);
		tglbtnOnOff.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractButton abstractButton =  
						(AbstractButton)e.getSource(); 

				// return true or false according 
				// to the selection or deselection  of the button 
				boolean selected = abstractButton.getModel().isSelected(); 
				if (!selected){
					tglbtnOnOff.setText("Alarm OFF");
					tglbtnOnOff.setBackground(Color.RED);
				}else{
					tglbtnOnOff.setText("Alarm ON");
				}

				prefChanged = true;

			}
		});
		add(tglbtnOnOff);

		if (prefs.isAlarmOn()){
			tglbtnOnOff.doClick();
			prefChanged = false;
		}

		BasicArrowButton back = new BasicArrowButton(BasicArrowButton.WEST);
		back.setFont(new Font("Tahoma", Font.PLAIN, 15));
		back.setSize(45, 37);
		back.setLocation(10, 415);

		back.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				try{
					if (tglbtnOnOff.isSelected() && ( prefs.getAlarmType() == null ||  prefs.getAlarmType().trim().length() == 0 )){
						JOptionPane.showMessageDialog(AlarmView.this, "Please select a buzzer" , "No Buzzer" , JOptionPane.INFORMATION_MESSAGE);
					}else{


						if (prefChanged){
							//save in preferences
							prefs.setAlarmOn(tglbtnOnOff.isSelected());
							prefs.setAlarmHour(hours);
							prefs.setAlarmMinutes(minutes);
							PreferencesHandler.save(prefs);
							prefChanged = false;

							//start or stop timer
							System.out.println("Pref changed");
							startStopTimer();
						}

						if (tglbtnOnOff.isSelected()){
							lblAlarm.setVisible(true);
						}else{
							lblAlarm.setVisible(false);
						}			
						System.out.println("Buzzer: " + prefs.getAlarmType());

						CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
						cardLayout.show(cardsPanel, "main");
					}
				}catch(Exception ex){
					JOptionPane.showMessageDialog(AlarmView.this, "Error in saving, see logs.", "Error Saving", JOptionPane.ERROR_MESSAGE);
					logger.log(Level.SEVERE, "Error in Alarm", ex);
				}
			}
		});


		add(back);

		JLabel lblNewLabel = new JLabel("Hours");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 40));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(200, 280, 120, 35);
		add(lblNewLabel);
		theme.registerLabelTextColor(lblNewLabel, LabelEnums.ALARM_HOUR_TXT);

		JLabel lblMinutes_1 = new JLabel("Minutes");
		lblMinutes_1.setFont(new Font("Tahoma", Font.PLAIN, 40));
		lblMinutes_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblMinutes_1.setBounds(420, 280, 160, 35);
		add(lblMinutes_1);
		theme.registerLabelTextColor(lblMinutes_1, LabelEnums.ALARM_HOUR_TXT);
		
		wakeUpAlarmOptions = new BuzzerOptionDialog();
		btnBuzzer = new JButton("N/A");
		btnBuzzer.setFont(new Font("Tahoma", Font.PLAIN, 25));
		btnBuzzer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				wakeUpAlarmOptions.setVisible(true);
			}
		});
		if (prefs.getAlarmType() != null &&  prefs.getAlarmType().trim().length() > 0){
			btnBuzzer.setText(prefs.getAlarmType());
		}
		
		btnBuzzer.setBounds(615, 235, 140, 40);
		add(btnBuzzer);
		
	
		startStopTimer();
		
	}
	/**
	 * Toggle on off has changed, update timer
	 */
	private void startStopTimer(){
		logger.config("startStopTimer()");
		//verify if timer is running, if yes , kill it
		if (alarmThread != null && !alarmThread.isDone()){

			logger.config("startStopTimer::alarmThread Done? " + alarmThread.isDone());
			
			alarmThread.cancel(true);	
			//wait for timer to stop
			while(!alarmThread.isDone()){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {}
			}
			logger.config("alarmThread end loop Done? " + alarmThread.isDone());
		}		
		
		//if alarm is on, start it
		if (tglbtnOnOff.isSelected()){
						
			logger.config("tglbtnOnOff selected, Starting alarm thread");
			
			//calculate delay
			LocalDateTime currentDate = LocalDateTime.now();
			LocalDateTime alarmTime = LocalDateTime.now();
			
			if ( hours > alarmTime.getHour()  ||
					hours == alarmTime.getHour() && minutes > alarmTime.getMinute()){
				//time after current time
				alarmTime = LocalDate.now().atTime(hours, minutes, 0, 0);
			}else{
				//time before current time, set date for next day.
				alarmTime = LocalDate.now().atTime(hours, minutes, 0, 0).plusDays(1);
			}

			long initDelay = ChronoUnit.MILLIS.between(currentDate, alarmTime);
			
			alarmThread  = alarmScheduler.scheduleAtFixedRate(new Runnable(){

				@Override
				public void run() {
					PiHandler handler = PiHandler.getInstance();
					System.out.println(new Date());
					alarmOn = true; // alarm is active and buzzing
					try {
						Preferences pref = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
						Buzzer buzzer = Buzzer.valueOf(pref.getAlarmType());
						
						handler.turnOnAlarm(buzzer);
						
						if (!handler.isScreenOn()){						
							handler.turnOnScreen(false);
							handler.autoShutDownScreen();
						}
						
						if (pref.isWeatherActivated() ){
							
							if (!handler.isWifiConnected() && pref.isWifiCredentialProvided()){
								handler.turnWifiOn();
							}else{
								int triggerForecast = new Random().nextInt(999999);
								ct.putSharedObject(Constants.FETCH_FORECAST, triggerForecast);
							}
						}
						
					} catch (Exception e) {
						logger.log(Level.SEVERE,"Error in setting off timer" , e);
						//TODO set someting letting the user know that there is been a log generated!!! 						
					}					
				}				
			}, initDelay, 86400000, TimeUnit.MILLISECONDS);			
			
			logger.config("Setting alarm at: " + alarmTime );
			
		}		
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		btnBuzzer.setText( Buzzer.valueOf((String)evt.getNewValue()).getName());
		
	}
}
