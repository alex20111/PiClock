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

import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
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
import net.piclock.swing.component.MessageListener;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;
import net.piclock.thread.ThreadManager;
import net.piclock.util.FormatStackTrace;

public class AlarmView extends JPanel implements PropertyChangeListener, MessageListener {

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
	private boolean btnNotAutoClicked = true;
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
	private Color active = Color.GREEN;
	private Color inActive = Color.RED;
	
	private JButton btnSelected;

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
				
				if (btnNotAutoClicked){
					alarmInfoChanged = true;
				}
				if (!selected){
					tglbtnOnOff.setText("Alarm OFF");  
					tglbtnOnOff.setBackground(Color.RED);
					if (btnSelected != null) {		
						btnSelected.setName("inactive");
					}

				}else{
					tglbtnOnOff.setText("Alarm ON");
					if (btnSelected != null) {				
						btnSelected.setName("active");
					}
				}			
			}
		});
		add(tglbtnOnOff);

		if (alarmEnt != null && alarmEnt.isActive()){
			btnNotAutoClicked = false;
			tglbtnOnOff.doClick();
			btnNotAutoClicked= true;
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

		ct.addMessageChangeListener(Constants.ALA_SAY_WEEK_UPD, this);
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
		if (buzzerSelection.getBuzzer() != Buzzer.BUZZER) {
		//just do some temp updates until the real save:
			alarmEnt.setVolume(buzzerSelection.getSelVolume());
			alarmEnt.setMp3Id(buzzerSelection.getMp3Id());
			alarmEnt.setRadioId(buzzerSelection.getRadioId());
			alarmEnt.setAlarmShutdown(buzzerSelection.getShutdownMin());
			alarmEnt.setAlarmSound(btnBuzzer.getText());
		}
	}
	private void dayDaysToSelect(AlarmEntity alarm, ThemeHandler theme) {	

		List<AlarmRepeat> ar = new ArrayList<AlarmRepeat>();
		if (alarm != null) {
			ar = alarm.getAlarmRepeat();
		}

		JLabel lblDaySunday = new JLabel("S");
		sunday = new AlarmDayMouseSelector(lblDaySunday, ar.contains(AlarmRepeat.SUNDAY) ? true : false);
		lblDaySunday.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblDaySunday.addMouseListener(sunday);
		lblDaySunday.setHorizontalAlignment(SwingConstants.CENTER);
		lblDaySunday.setBounds(55,85,41,38);
		theme.registerLabelTextColor(lblDaySunday, LabelEnums.ALARM_SUNDAY);

		JLabel lblDayMonday = new JLabel("M");
		lblDayMonday.setFont(new Font("Tahoma", Font.BOLD, 16));
		monday = new AlarmDayMouseSelector(lblDayMonday, ar.contains(AlarmRepeat.MONDAY) ? true : false);
		lblDayMonday.addMouseListener(monday);
		lblDayMonday.setHorizontalAlignment(SwingConstants.CENTER);
		lblDayMonday.setBounds(55,125,41,38);
		theme.registerLabelTextColor(lblDayMonday, LabelEnums.ALARM_MON);

		JLabel lblDayTue = new JLabel("T");
		lblDayTue.setFont(new Font("Tahoma", Font.BOLD, 16));
		tuesday = new AlarmDayMouseSelector(lblDayTue, ar.contains(AlarmRepeat.TUESDAY) ? true : false);
		lblDayTue.addMouseListener(tuesday);
		lblDayTue.setHorizontalAlignment(SwingConstants.CENTER);
		lblDayTue.setBounds(55,165,41,38);
		theme.registerLabelTextColor(lblDayTue, LabelEnums.ALARM_TU);

		JLabel lblDayWed = new JLabel("W");
		lblDayWed.setFont(new Font("Tahoma", Font.BOLD, 16));
		wednesday = new AlarmDayMouseSelector(lblDayWed, ar.contains(AlarmRepeat.WEDNESDAY) ? true : false);
		lblDayWed.addMouseListener(wednesday);
		lblDayWed.setHorizontalAlignment(SwingConstants.CENTER);
		lblDayWed.setBounds(55,205,41,38);
		theme.registerLabelTextColor(lblDayWed, LabelEnums.ALARM_WED);

		JLabel lblDayThu = new JLabel("T");
		lblDayThu.setFont(new Font("Tahoma", Font.BOLD, 16));
		thursday = new AlarmDayMouseSelector(lblDayThu, ar.contains(AlarmRepeat.THURSDAY) ? true : false);
		lblDayThu.addMouseListener(thursday);
		lblDayThu.setHorizontalAlignment(SwingConstants.CENTER);
		lblDayThu.setBounds(55,245,41,38);
		theme.registerLabelTextColor(lblDayThu, LabelEnums.ALARM_TH);

		JLabel lblDayFriday = new JLabel("F");
		lblDayFriday.setFont(new Font("Tahoma", Font.BOLD, 16));
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
	private void alarmSelectButtons() throws ClassNotFoundException, SQLException {
		
		JButton btnAlarms[] = new JButton[7];		
		btnAlarms[0] = new JButton("1");
		btnAlarms[1] = new JButton("2");
		btnAlarms[2] = new JButton("3");
		btnAlarms[3] = new JButton("4");
		btnAlarms[4] = new JButton("5");
		btnAlarms[5] = new JButton("6");
		btnAlarms[6] = new JButton("7");
		
		btnAlarms[0].setFont(new Font("Tahoma", Font.BOLD, 20));
		btnAlarms[0].setBounds(175, 406, 50, 40);
		btnAlarms[0].addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					btnSelected = null;
					if (alarmEnt != null && alarmInfoChanged) {
						saving();
					}
					alarmEnt = sql.loadAlarmByOrderNbr(1);
					if (alarmEnt == null) {
						alarmEnt = new AlarmEntity();
						alarmEnt.setAlarmOrder(1);
					}				

					setSelected(btnAlarms[0]);
					returnBtnDefault(btnAlarms, btnAlarms[0]);
					populateAlarmFields();
					
					btnSelected = btnAlarms[0];
				} catch (ClassNotFoundException | SQLException e1) {
					logger.log(Level.SEVERE, "Error in alarm button", e1);
				} 
			}
		});
		add(btnAlarms[0]);

		btnAlarms[1].setFont(new Font("Tahoma", Font.BOLD, 20));
		btnAlarms[1].setBounds(235, 406, 50, 40);
		btnAlarms[1].addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					btnSelected = null;
					//verify if we need to save the previous alarm
					if (alarmEnt != null && alarmInfoChanged) {
						saving();
					}		

					alarmEnt = sql.loadAlarmByOrderNbr(2);
					if (alarmEnt == null) {
						alarmEnt = new AlarmEntity();
						alarmEnt.setAlarmOrder(2);
					}					

					setSelected(btnAlarms[1]);
					returnBtnDefault(btnAlarms, btnAlarms[1]);
					populateAlarmFields();
					
					btnSelected = btnAlarms[1];
				} catch (ClassNotFoundException | SQLException e1) {
					logger.log(Level.SEVERE, "Error in alarm button", e1);
				} 
			}
		});
		add(btnAlarms[1]);

		btnAlarms[2].setFont(new Font("Tahoma", Font.BOLD, 20));
		btnAlarms[2].setBounds(295, 406, 50, 40);
		btnAlarms[2].addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					btnSelected = null;
					if (alarmEnt != null && alarmInfoChanged) {
						saving();
					}

					setSelected(btnAlarms[2]);
					returnBtnDefault(btnAlarms, btnAlarms[2]);
					alarmEnt = sql.loadAlarmByOrderNbr(3);
					if (alarmEnt == null) {
						alarmEnt = new AlarmEntity();
						alarmEnt.setAlarmOrder(3);
					}
					populateAlarmFields();
					
					btnSelected = btnAlarms[2];
					
				} catch (ClassNotFoundException | SQLException e1) {
					logger.log(Level.SEVERE, "Error in alarm button", e1);
				} 
			}
		});
		add(btnAlarms[2]);

		btnAlarms[3].setFont(new Font("Tahoma", Font.BOLD, 20));
		btnAlarms[3].setBounds(355, 406, 50, 40);
		btnAlarms[3].addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					btnSelected = null;
					if (alarmEnt != null && alarmInfoChanged) {
						saving();
					}

					setSelected(btnAlarms[3]);
					returnBtnDefault(btnAlarms, btnAlarms[3]);
					alarmEnt = sql.loadAlarmByOrderNbr(4);
					if (alarmEnt == null) {
						alarmEnt = new AlarmEntity();
						alarmEnt.setAlarmOrder(4);
					}
					populateAlarmFields();
					
					btnSelected = btnAlarms[3];
				} catch (ClassNotFoundException | SQLException e1) {
					logger.log(Level.SEVERE, "Error in alarm button", e1);
				} 
			}
		});
		add(btnAlarms[3]);

		btnAlarms[4].setFont(new Font("Tahoma", Font.BOLD, 20));
		btnAlarms[4].setBounds(415, 406, 50, 40);
		btnAlarms[4].addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					btnSelected = null;
					if (alarmEnt != null && alarmInfoChanged) {
						saving();
					}

					setSelected(btnAlarms[4]);
					returnBtnDefault(btnAlarms, btnAlarms[4]);
					alarmEnt = sql.loadAlarmByOrderNbr(5);
					if (alarmEnt == null) {
						alarmEnt = new AlarmEntity();
						alarmEnt.setAlarmOrder(5);
					}
					populateAlarmFields();
					
					btnSelected = btnAlarms[4];
				} catch (ClassNotFoundException | SQLException e1) {
					logger.log(Level.SEVERE, "Error in alarm button", e1);
				} 
			}
		});
		add(btnAlarms[4]);
		
		btnAlarms[5].setFont(new Font("Tahoma", Font.BOLD, 20));
		btnAlarms[5].setBounds(475, 406, 50, 40);
		btnAlarms[5].addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					btnSelected = null;
					if (alarmEnt != null && alarmInfoChanged) {
						saving();
					}

					setSelected(btnAlarms[5]);
					returnBtnDefault(btnAlarms, btnAlarms[5]);
					alarmEnt = sql.loadAlarmByOrderNbr(6);
					if (alarmEnt == null) {
						alarmEnt = new AlarmEntity();
						alarmEnt.setAlarmOrder(6);
					}
					populateAlarmFields();
					
					btnSelected = btnAlarms[5];
				} catch (ClassNotFoundException | SQLException e1) {
					logger.log(Level.SEVERE, "Error in alarm button", e1);
				} 
			}
		});
		add(btnAlarms[5]);
		
		btnAlarms[6].setFont(new Font("Tahoma", Font.BOLD, 20));
		btnAlarms[6].setBounds(535, 406, 50, 40);
		btnAlarms[6].addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					btnSelected = null;
					if (alarmEnt != null && alarmInfoChanged) {
						saving();
					}
					setSelected(btnAlarms[6]);
					returnBtnDefault(btnAlarms, btnAlarms[6]);
					alarmEnt = sql.loadAlarmByOrderNbr(7);
					if (alarmEnt == null) {
						alarmEnt = new AlarmEntity();
						alarmEnt.setAlarmOrder(7);
					}
					populateAlarmFields();
					
					btnSelected = btnAlarms[6];
				} catch (ClassNotFoundException | SQLException e1) {
					logger.log(Level.SEVERE, "Error in alarm button", e1);
				} 
			}
		});
		add(btnAlarms[6]);		
		
		//color paint buttons 
		List<AlarmEntity> al = sql.loadAllActiveAlarm();
		for(int i = 0 ; i < btnAlarms.length ; i ++) {				
			try {
				AlarmEntity ae = al.get(i);
				if (ae.isActive()) {
					setBtnActiveInactiveName(btnAlarms[i], true);
					btnAlarms[i].setBackground(active);
				}else {
					setBtnActiveInactiveName(btnAlarms[i], false);	
					btnAlarms[i].setBackground(inActive);
				}		
			}catch (IndexOutOfBoundsException ie) {
				setBtnActiveInactiveName(btnAlarms[i], false);
				btnAlarms[i].setBackground(inActive);
			}
		}		
		setSelected(btnAlarms[0]);
	}
	private void populateAlarmFields() {
		hours = Integer.parseInt(alarmEnt.getHour());
		lblHours.setText(alarmEnt.getHour());
		minutes = Integer.parseInt(alarmEnt.getMinutes());
		lblMinutes.setText(alarmEnt.getMinutes());
		logger.log(Level.CONFIG, "Toggle button status: " + tglbtnOnOff.isSelected());

		if (alarmEnt != null) {
			if (alarmEnt.isActive() && !tglbtnOnOff.isSelected()){
				btnNotAutoClicked = false;
				tglbtnOnOff.doClick();
				btnNotAutoClicked = true;
			}else if (!alarmEnt.isActive() && tglbtnOnOff.isSelected()) {
				btnNotAutoClicked = false;
				tglbtnOnOff.doClick();
				btnNotAutoClicked = true;
			}

			if (alarmEnt.getAlarmSound() != null &&  alarmEnt.getAlarmSound().trim().length() > 0){
				btnBuzzer.setText(alarmEnt.getAlarmSound());
			}

		}else if (alarmEnt == null) {
			if (tglbtnOnOff.isSelected()) {
				btnNotAutoClicked = false;
				tglbtnOnOff.doClick();
				btnNotAutoClicked = true;
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
	}
	private void setSelected(JButton btn) {
		btn.setForeground(new Color(255, 255, 255));
		btn.setBackground(new Color(0, 153, 255));
		btn.setBorder(new LineBorder(new Color(51, 51, 255), 2, true));
	}
	private void returnBtnDefault(JButton btnAlarms[], JButton btnToSkip) { //exclude
		for (int i = 0 ; i < btnAlarms.length ; i++){
			if (!btnAlarms[i].equals(btnToSkip)){
				btn.setForeground(null);
				if ("active".equals(btn.getName())) {
					btn.setBackground(Color.GREEN);
				}else {
					btn.setBackground(Color.RED);
				}
				btn.setBorder(null);
			}
		}
	}
	private void setBtnActiveInactiveName(JButton btn , boolean active) {
		if (active) {
			btn.setName("active");
		}else {
			btn.setName("inactive");
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
				alarmEnt.setActive(true);
			}else {
				alarmEnt.setActive(false);
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
	@Override
	public void message(Message message) {
		if (Constants.ALA_SAY_WEEK_UPD.contentEquals(message.getPropertyName())) {
			alarmInfoChanged = true;
		}
	}
}
