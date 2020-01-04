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
import java.io.IOException;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.metal.MetalToggleButtonUI;

import com.pi4j.io.gpio.exception.UnsupportedBoardType;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import net.piclock.arduino.ArduinoSerialCmd;
import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.button.AlarmBtnHandler;
import net.piclock.db.entity.AlarmEntity;
import net.piclock.db.sql.AlarmSql;
import net.piclock.enums.AlarmRepeat;
import net.piclock.enums.Buzzer;
import net.piclock.enums.LabelEnums;
import net.piclock.main.Constants;
import net.piclock.main.Preferences;
import net.piclock.swing.component.AlarmDayMouseSelector;
import net.piclock.swing.component.BuzzerSelection;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;
import net.piclock.thread.ThreadManager;
import net.piclock.util.FormatStackTrace;

public class AlarmView extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = -8991056994673610266L;

	private static final Logger logger = Logger.getLogger( AlarmView.class.getName() );

	private JToggleButton tglbtnOnOff;
	private int hours = 0;
	private int minutes = 0;
	private JLabel lblHours;
	private JLabel lblMinutes;

	private SwingContext ct = SwingContext.getInstance();

	Thread timeCounter;
	private boolean keepRunning = true;
	private int btnDelay = 300;

	private BuzzerOptionDialog wakeUpAlarmOptions;
	private JButton btnBuzzer;

	private AlarmSql sql;

	private  boolean alarmInfoChanged = false;
	private BuzzerSelection buzzerSelection;

	//days of the week label
	private AlarmDayMouseSelector sunday;
	private AlarmDayMouseSelector monday;
	private AlarmDayMouseSelector tuesday;
	private AlarmDayMouseSelector wednesday;
	private AlarmDayMouseSelector thursday;
	private AlarmDayMouseSelector friday;
	private AlarmDayMouseSelector saturday;

	private ThreadManager tm;

	private AlarmEntity alarmEnt;//alarm for the current screen
	
	private JLabel lblAlarmIcon = null;

	/**
	 * Create the panel.
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws UnsupportedBusNumberException 
	 * @throws InterruptedException 
	 * @throws UnsupportedBoardType 
	 */
	public AlarmView(final JPanel cardsPanel, final Preferences prefs, final JLabel lblAlarm) throws ClassNotFoundException, SQLException, IOException, UnsupportedBusNumberException, UnsupportedBoardType, InterruptedException {		
		logger.config("Starting alarmView");
		
		lblAlarmIcon = lblAlarm;

		tm = ThreadManager.getInstance();
		tm.startAlarm();

		sql = new AlarmSql();
		sql.CreateAlarmTable();

		alarmEnt = sql.loadAlarmByOrderNbr(1);

		//add listener for button
		AlarmBtnHandler btnHandler = new AlarmBtnHandler();
		ct.putSharedObject(Constants.ALARM_BTN_HANDLER, btnHandler);
		ArduinoSerialCmd cm = ArduinoSerialCmd.getInstance();
		cm.addButtonListener(btnHandler);

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

		//active alarm found, start it.
		if (alarmEnt != null) {
			hours = Integer.parseInt(alarmEnt.getHour());
			minutes = Integer.parseInt(alarmEnt.getMinutes());
			Message msg = new Message(alarmEnt);

//			ct.sendMessage(Constants.UPDATE_ALARMS, msg);
		}else {
			//new alarm
			alarmEnt = new AlarmEntity();
			alarmEnt.setAlarmOrder(1);
		}

		JButton btnHoursPlus = new JButton("+");
		btnHoursPlus.setFont(new Font("tahoma", Font.BOLD, 20));
		btnHoursPlus.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {

				keepRunning = true;
				alarmInfoChanged = true;

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
		btnHoursPlus.setBounds(240, 85, 55, 40);
		add(btnHoursPlus);

		JButton btnMinPlus = new JButton("+");
		btnMinPlus.setFont(new Font("Tahoma", Font.BOLD, 20));
		btnMinPlus.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
				keepRunning = true;
				alarmInfoChanged = true;


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

		btnMinPlus.setBounds(458, 85, 55, 40);
		add(btnMinPlus);

		lblHours = new JLabel(String.valueOf(hours));
		lblHours.setFont(new Font("Tahoma", Font.BOLD, 80));
		lblHours.setHorizontalAlignment(SwingConstants.CENTER);
		lblHours.setBounds(193, 121, 130, 145);
		add(lblHours);
		theme.registerLabelTextColor(lblHours, LabelEnums.ALARM_HOUR);


		lblMinutes = new JLabel(String.valueOf(minutes));
		lblMinutes.setHorizontalAlignment(SwingConstants.CENTER);
		lblMinutes.setFont(new Font("Tahoma", Font.BOLD, 80));
		lblMinutes.setBounds(429, 121, 130, 145);
		add(lblMinutes);
		theme.registerLabelTextColor(lblMinutes, LabelEnums.ALARM_MIN);

		JButton btnHourMinus = new JButton("-");
		btnHourMinus.setFont(new Font("Tahoma", Font.BOLD, 20));
		btnHourMinus.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
				keepRunning = true;
				alarmInfoChanged = true;

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


		btnHourMinus.setBounds(240, 326, 55, 40);
		add(btnHourMinus);

		//Minutes button minus 
		JButton btnMinMinus = new JButton("-");
		btnMinMinus.setFont(new Font("tahoma", Font.BOLD, 20));
		btnMinMinus.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
				keepRunning = true;
				alarmInfoChanged = true;

				timeCounter = new Thread(new Runnable(){

					@Override
					public void run() {
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

		btnMinMinus.setBounds(458, 326, 55, 40);
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
				AbstractButton abstractButton =  	(AbstractButton)e.getSource();

				// return true or false according 
				// to the selection or deselection  of the button 
				boolean selected = abstractButton.getModel().isSelected();				

				alarmInfoChanged = true;
				if (!selected){
					tglbtnOnOff.setText("Alarm OFF");  //TODO TGL [roblem !!!!!!!!!!!!!
					tglbtnOnOff.setBackground(Color.RED);

				}else{
					tglbtnOnOff.setText("Alarm ON");				

				}			
			}
		});
		add(tglbtnOnOff);

		if (alarmEnt != null && alarmEnt.isActive()){
			tglbtnOnOff.doClick();
		}

		BasicArrowButton back = new BasicArrowButton(BasicArrowButton.WEST);
		back.setFont(new Font("Tahoma", Font.PLAIN, 15));
		back.setSize(50, 40);
		back.setLocation(46, 406);

		back.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				try{
					saving();
					
					//check if any alarm are active.
					if (sql.isAnyAlarmActive()) {
						lblAlarmIcon.setVisible(true);
					}else {
						lblAlarmIcon.setVisible(false);
					}
					CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
					cardLayout.show(cardsPanel, Constants.MAIN_VIEW);

				}catch(Exception ex){
					JOptionPane.showMessageDialog(AlarmView.this, "Error in saving, see logs.", "Error Saving", JOptionPane.ERROR_MESSAGE);
					ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
					eh.addError(ErrorType.ALARM, new ErrorInfo(new FormatStackTrace(ex).getFormattedException()));
					logger.log(Level.SEVERE, "Error in Alarm", ex);
				}
			}
		});


		add(back);

		JLabel lblNewLabel = new JLabel("Hours");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 40));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(200, 270, 120, 35);
		add(lblNewLabel);
		theme.registerLabelTextColor(lblNewLabel, LabelEnums.ALARM_HOUR_TXT);

		JLabel lblMinutes_1 = new JLabel("Minutes");
		lblMinutes_1.setFont(new Font("Tahoma", Font.PLAIN, 40));
		lblMinutes_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblMinutes_1.setBounds(420, 270, 160, 35);
		add(lblMinutes_1);
		theme.registerLabelTextColor(lblMinutes_1, LabelEnums.ALARM_MIN_TXT);

		wakeUpAlarmOptions = new BuzzerOptionDialog();
		btnBuzzer = new JButton(Buzzer.BUZZER.name());
		btnBuzzer.setFont(new Font("Tahoma", Font.PLAIN, 25));
		btnBuzzer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {

					wakeUpAlarmOptions.setBuzzerType(alarmEnt);

					wakeUpAlarmOptions.setVisible(true);

				}catch(Exception ex) {
					ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
					eh.addError(ErrorType.ALARM, new ErrorInfo(new FormatStackTrace(ex).getFormattedException()));
					logger.log(Level.SEVERE, "Error while choosing the buzzer option", ex);
				}
			}
		});
		if (alarmEnt != null && alarmEnt.getAlarmSound() != null &&  alarmEnt.getAlarmSound().trim().length() > 0){
			btnBuzzer.setText(alarmEnt.getAlarmSound());
		}

		buzzerSelection = new BuzzerSelection(alarmEnt);

		btnBuzzer.setBounds(615, 235, 140, 40);
		add(btnBuzzer);

		dayDaysToSelect(alarmEnt, theme);

		//add option into session
		ct.putSharedObject(Constants.BUZZER_OPTION_PANEL, wakeUpAlarmOptions);

		alarmSelectButtons();
	}

	public void setAlarmNotToggled() {
		this.alarmInfoChanged = false;
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {


		buzzerSelection = (BuzzerSelection)evt.getNewValue();

		logger.log(Level.CONFIG, "PropChange: " + buzzerSelection);

		btnBuzzer.setText(buzzerSelection.getBuzzer().name());

		alarmInfoChanged = true;

	}
	private void dayDaysToSelect(AlarmEntity alarm, ThemeHandler theme) {	

		List<AlarmRepeat> ar = new ArrayList<AlarmRepeat>();
		if (alarm != null) {
			ar = alarm.getAlarmRepeat();
		}

		JLabel lblDaySunday = new JLabel("S");
		sunday = new AlarmDayMouseSelector(lblDaySunday, ar.contains(AlarmRepeat.SUNDAY) ? true : false);
		lblDaySunday.setFont(new Font("Tahoma", Font.BOLD, 16));
		//		lblDaySunday.setBorder(new RoundedBorder(Color.BLACK, 40));
		lblDaySunday.addMouseListener(sunday);
		lblDaySunday.setHorizontalAlignment(SwingConstants.CENTER);
		lblDaySunday.setBounds(55,85,41,38);
		theme.registerLabelTextColor(lblDaySunday, LabelEnums.ALARM_SUNDAY);

		JLabel lblDayMonday = new JLabel("M");
		//		lblDayMonday.setBorder(new RoundedBorder(Color.BLACK, 40));
		lblDayMonday.setFont(new Font("Tahoma", Font.BOLD, 16));
		monday = new AlarmDayMouseSelector(lblDayMonday, ar.contains(AlarmRepeat.MONDAY) ? true : false);
		lblDayMonday.addMouseListener(monday);
		lblDayMonday.setHorizontalAlignment(SwingConstants.CENTER);
		lblDayMonday.setBounds(55,125,41,38);
		theme.registerLabelTextColor(lblDayMonday, LabelEnums.ALARM_MON);

		JLabel lblDayTue = new JLabel("T");
		//		lblDayTue.setBorder(new RoundedBorder(Color.BLACK, 40));
		lblDayTue.setFont(new Font("Tahoma", Font.BOLD, 16));
		tuesday = new AlarmDayMouseSelector(lblDayTue, ar.contains(AlarmRepeat.TUESDAY) ? true : false);
		lblDayTue.addMouseListener(tuesday);
		lblDayTue.setHorizontalAlignment(SwingConstants.CENTER);
		lblDayTue.setBounds(55,165,41,38);
		theme.registerLabelTextColor(lblDayTue, LabelEnums.ALARM_TU);

		JLabel lblDayWed = new JLabel("W");
		//		lblDayWed.setBorder(new RoundedBorder(Color.BLACK, 40));
		lblDayWed.setFont(new Font("Tahoma", Font.BOLD, 16));
		wednesday = new AlarmDayMouseSelector(lblDayWed, ar.contains(AlarmRepeat.WEDNESDAY) ? true : false);
		lblDayWed.addMouseListener(wednesday);
		lblDayWed.setHorizontalAlignment(SwingConstants.CENTER);
		lblDayWed.setBounds(55,205,41,38);
		theme.registerLabelTextColor(lblDayWed, LabelEnums.ALARM_WED);

		JLabel lblDayThu = new JLabel("T");
		lblDayThu.setFont(new Font("Tahoma", Font.BOLD, 16));
		//		lblDayThu.setBorder(new RoundedBorder(Color.BLACK, 40));
		thursday = new AlarmDayMouseSelector(lblDayThu, ar.contains(AlarmRepeat.THURSDAY) ? true : false);
		lblDayThu.addMouseListener(thursday);
		lblDayThu.setHorizontalAlignment(SwingConstants.CENTER);
		lblDayThu.setBounds(55,245,41,38);
		theme.registerLabelTextColor(lblDayThu, LabelEnums.ALARM_TH);

		JLabel lblDayFriday = new JLabel("F");
		lblDayFriday.setFont(new Font("Tahoma", Font.BOLD, 16));
		//		lblDayFriday.setBorder(new RoundedBorder(Color.BLACK, 40));
		friday = new AlarmDayMouseSelector(lblDayFriday, ar.contains(AlarmRepeat.FRIDAY) ? true : false);
		lblDayFriday.addMouseListener(friday);
		lblDayFriday.setHorizontalAlignment(SwingConstants.CENTER);
		lblDayFriday.setBounds(55,285,41,38);
		theme.registerLabelTextColor(lblDayFriday, LabelEnums.ALARM_FRI);

		JLabel lblDaySat = new JLabel("S");
		lblDaySat.setFont(new Font("Tahoma", Font.BOLD, 16));
		//		lblDaySat.setBorder(new RoundedBorder(Color.BLACK, 40));
		saturday = new AlarmDayMouseSelector(lblDaySat, ar.contains(AlarmRepeat.SATURDAY) ? true : false);
		lblDaySat.addMouseListener(saturday);
		lblDaySat.setHorizontalAlignment(SwingConstants.CENTER);
		lblDaySat.setBounds(55,325,41,38);
		theme.registerLabelTextColor(lblDaySat, LabelEnums.ALARM_SAT);

		add(lblDaySunday);
		add(lblDayMonday);
		add(lblDayTue);
		add(lblDayWed);
		add(lblDayThu);
		add(lblDayFriday);
		add(lblDaySat);
	}	
	private void alarmSelectButtons() {
		final JButton btnOne = new JButton("1");
		final JButton btnTwo = new JButton("2");
		final JButton btnThree = new JButton("3");
		final JButton btnFour = new JButton("4");
		final JButton btnFive = new JButton("5");

		setSelected(btnOne);
		
		btnOne.setFont(new Font("Tahoma", Font.BOLD, 20));
		btnOne.setBounds(235, 406, 50, 40);
		btnOne.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (alarmEnt != null && alarmInfoChanged) {
						saving();
					}
					
					setSelected(btnOne);
					returnBtnDefault(btnTwo, btnThree, btnFour, btnFive);
					alarmEnt = sql.loadAlarmByOrderNbr(1);
					if (alarmEnt == null) {
						alarmEnt = new AlarmEntity();
						alarmEnt.setAlarmOrder(1);
					}
					populateAlarmFields();
				} catch (ClassNotFoundException | SQLException e1) {
					logger.log(Level.SEVERE, "Error in alarm button", e1);
				} 

			}
		});
		add(btnOne);

		btnTwo.setFont(new Font("Tahoma", Font.BOLD, 20));
		btnTwo.setBounds(295, 406, 50, 40);
		btnTwo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					//verify if we need to save the previous alarm
					
					if (alarmEnt != null && alarmInfoChanged) {
						saving();
					}					
					
					setSelected(btnTwo);
					returnBtnDefault(btnOne, btnThree, btnFour, btnFive);
					alarmEnt = sql.loadAlarmByOrderNbr(2);
					if (alarmEnt == null) {
						alarmEnt = new AlarmEntity();
						alarmEnt.setAlarmOrder(2);
					}
					populateAlarmFields();
				} catch (ClassNotFoundException | SQLException e1) {
					logger.log(Level.SEVERE, "Error in alarm button", e1);
				} 

			}
		});
		add(btnTwo);


		btnThree.setFont(new Font("Tahoma", Font.BOLD, 20));
		btnThree.setBounds(353, 406, 50, 40);
		btnThree.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (alarmEnt != null && alarmInfoChanged) {
						saving();
					}
					
					setSelected(btnThree);
					returnBtnDefault(btnTwo, btnOne, btnFour, btnFive);
					alarmEnt = sql.loadAlarmByOrderNbr(3);
					if (alarmEnt == null) {
						alarmEnt = new AlarmEntity();
						alarmEnt.setAlarmOrder(3);
					}
					populateAlarmFields();
				} catch (ClassNotFoundException | SQLException e1) {
					logger.log(Level.SEVERE, "Error in alarm button", e1);
				} 

			}
		});
		add(btnThree);

		btnFour.setFont(new Font("Tahoma", Font.BOLD, 20));
		btnFour.setBounds(413, 406, 50, 40);
		btnFour.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (alarmEnt != null && alarmInfoChanged) {
						saving();
					}
					
					setSelected(btnFour);
					returnBtnDefault(btnTwo, btnThree, btnOne, btnFive);
					alarmEnt = sql.loadAlarmByOrderNbr(4);
					if (alarmEnt == null) {
						alarmEnt = new AlarmEntity();
						alarmEnt.setAlarmOrder(4);
					}
					populateAlarmFields();
				} catch (ClassNotFoundException | SQLException e1) {
					logger.log(Level.SEVERE, "Error in alarm button", e1);
				} 

			}
		});
		add(btnFour);


		btnFive.setFont(new Font("Tahoma", Font.BOLD, 20));
		btnFive.setBounds(473, 406, 50, 40);
		btnFive.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (alarmEnt != null && alarmInfoChanged) {
						saving();
					}
					
					setSelected(btnFive);
					returnBtnDefault(btnTwo, btnThree, btnFour, btnOne);
					alarmEnt = sql.loadAlarmByOrderNbr(5);
					if (alarmEnt == null) {
						alarmEnt = new AlarmEntity();
						alarmEnt.setAlarmOrder(5);
					}
					populateAlarmFields();
				} catch (ClassNotFoundException | SQLException e1) {
					logger.log(Level.SEVERE, "Error in alarm button", e1);
				} 

			}
		});
		add(btnFive);
	}
	private void populateAlarmFields() {
		hours = Integer.parseInt(alarmEnt.getHour());
		lblHours.setText(alarmEnt.getHour());
		minutes = Integer.parseInt(alarmEnt.getMinutes());
		lblMinutes.setText(alarmEnt.getMinutes());
		logger.log(Level.CONFIG, "Toggle button status: " + tglbtnOnOff.isSelected());

		if (alarmEnt != null) {
			if (alarmEnt.isActive() && !tglbtnOnOff.isSelected()){
				tglbtnOnOff.doClick();
			}else if (!alarmEnt.isActive() && tglbtnOnOff.isSelected()) {
				tglbtnOnOff.doClick();
			}

			if (alarmEnt.getAlarmSound() != null &&  alarmEnt.getAlarmSound().trim().length() > 0){
				btnBuzzer.setText(alarmEnt.getAlarmSound());
			}

		}else if (alarmEnt == null) {
			if (tglbtnOnOff.isSelected()) {
				tglbtnOnOff.doClick();
			}
			btnBuzzer.setText(Buzzer.BUZZER.name());
		}

		buzzerSelection = new BuzzerSelection(alarmEnt);

		//de-select all alarm repeat
		sunday.deSelect();
		monday.deSelect();
		tuesday.deSelect();
		wednesday.deSelect();
		thursday.deSelect();
		friday.deSelect();
		saturday.deSelect();

		if (alarmEnt.getAlarmRepeat() != null && alarmEnt.getAlarmRepeat().size() > 0) {
			for(AlarmRepeat ap : alarmEnt.getAlarmRepeat()) {

				if (ap.isEqual(DayOfWeek.SUNDAY)) {
					sunday.select();
				}else if (ap.isEqual(DayOfWeek.MONDAY)) {
					monday.select();
				}else if (ap.isEqual(DayOfWeek.THURSDAY)) {
					thursday.select();
				}else if (ap.isEqual(DayOfWeek.TUESDAY)) {
					tuesday.select();
				}else if (ap.isEqual(DayOfWeek.WEDNESDAY)) {
					wednesday.select();
				}else if (ap.isEqual(DayOfWeek.FRIDAY)) {
					friday.select();
				}else if (ap.isEqual(DayOfWeek.SATURDAY)) {
					saturday.select();
				}

			}
		}

		//		dayDaysToSelect(alarmEnt, theme);
	}
	private void setSelected(JButton btn) {
		btn.setForeground(new Color(255, 255, 255));
		btn.setBackground(new Color(0, 153, 255));
		btn.setBorder(new LineBorder(new Color(51, 51, 255), 2, true));
	}
	private void returnBtnDefault(JButton... btnList) {
		for (JButton btn : btnList) {
			btn.setForeground(null);
			btn.setBackground(null);
			btn.setBorder(null);
		}
	}
	
	private void saving() throws ClassNotFoundException, SQLException {
		logger.log(Level.CONFIG,"Alarm alarmInfoChanged: " + alarmInfoChanged);

		if (alarmInfoChanged) {

			boolean add = false;
			if (alarmEnt.getId() == -1) {
				add = true;
			}

			alarmEnt.setHour(String.valueOf(hours));
			alarmEnt.setMinutes(String.valueOf(minutes));
			alarmEnt.setAlarmSound(btnBuzzer.getText());

			if (Buzzer.valueOf(btnBuzzer.getText()) == Buzzer.RADIO) {
				alarmEnt.setRadioId(buzzerSelection.getRadioId());
			}else {
				alarmEnt.setRadioId(-1);
			}
			if (Buzzer.valueOf(btnBuzzer.getText()) == Buzzer.MP3) {
				alarmEnt.setMp3Id(buzzerSelection.getMp3Id());
			}else {
				alarmEnt.setMp3Id(-1);
			}

			if (buzzerSelection.getSelVolume() > 0) {
				alarmEnt.setVolume(buzzerSelection.getSelVolume());
			}

			List<AlarmRepeat> rp = new ArrayList<AlarmRepeat>();

			if (sunday.isSelected()) {
				rp.add(AlarmRepeat.SUNDAY);
			}
			if (monday.isSelected()) {
				rp.add(AlarmRepeat.MONDAY);
			}
			if (tuesday.isSelected()) {
				rp.add(AlarmRepeat.TUESDAY);
			}
			if (wednesday.isSelected()) {
				rp.add(AlarmRepeat.WEDNESDAY);
			}
			if (thursday.isSelected()) {
				rp.add(AlarmRepeat.THURSDAY);
			}
			if (friday.isSelected()) {
				rp.add(AlarmRepeat.FRIDAY);
			}
			if (saturday.isSelected()) {
				rp.add(AlarmRepeat.SATURDAY);
			}

			alarmEnt.setAlarmRepeat(rp);
			alarmEnt.setAlarmShutdown(buzzerSelection.getShutdownMin());

			if (tglbtnOnOff.isSelected()){
//				lblAlarmIcon.setVisible(true);
				alarmEnt.setActive(true);

			}else {
				alarmEnt.setActive(false);
//				lblAlarmIcon.setVisible(false);
			}

			if (add) {
				int id = sql.add(alarmEnt);
				alarmEnt = sql.loadAlarmById(id);
			}else {
				sql.update(alarmEnt);
			}

			Message msg = new Message(alarmEnt);

			ct.sendMessage(Constants.UPDATE_ALARMS, msg);

			logger.log(Level.INFO, "Alarm " + (add ? "Added: " : "Updated:" )  + alarmEnt);
			
			alarmInfoChanged = false;

		}
	}
}
