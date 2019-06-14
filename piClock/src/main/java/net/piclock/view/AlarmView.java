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
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.metal.MetalToggleButtonUI;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import net.piclock.arduino.ArduinoCmd;
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
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;
import net.piclock.thread.ThreadManager;

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
	
	private  boolean alarmToggled = false;
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
	
	
	/**
	 * Create the panel.
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws UnsupportedBusNumberException 
	 */
	public AlarmView(final JPanel cardsPanel, final Preferences prefs, final JLabel lblAlarm) throws ClassNotFoundException, SQLException, IOException, UnsupportedBusNumberException {		
		logger.config("Starting alarmView");
		
		tm = ThreadManager.getInstance();
		
		sql = new AlarmSql();
		sql.CreateAlarmTable();
		
		AlarmEntity alarmEnt = sql.loadActiveAlarm();
		
		//add listener for button
		AlarmBtnHandler btnHandler = new AlarmBtnHandler();
		ct.putSharedObject(Constants.ALARM_BTN_HANDLER, btnHandler);
		ArduinoCmd cm = ArduinoCmd.getInstance();
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

		if (alarmEnt != null) {
			hours = Integer.parseInt(alarmEnt.getHour());
			minutes = Integer.parseInt(alarmEnt.getMinutes());
			buzzerSelection = new BuzzerSelection(alarmEnt);
			
			tm.startAlarm(alarmEnt);
			
		}else {
			//if no alarm .. look if theere is any and loaf the 1st one
			List<AlarmEntity> ala = sql.loadAllAlarms();
			if (ala.size() > 0) {
				hours = Integer.parseInt(ala.get(0).getHour());
				minutes = Integer.parseInt(ala.get(0).getMinutes());
				//also set the days 
				alarmEnt = ala.get(0);
				buzzerSelection = new BuzzerSelection(alarmEnt);
				
				if (ala.get(0).isActive()) {
					tm.startAlarm(alarmEnt);
				}
			}
		}
				
		JButton btnHoursPlus = new JButton("+");
		btnHoursPlus.setFont(new Font("tahoma", Font.BOLD, 20));
		btnHoursPlus.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
					
				keepRunning = true;
				alarmToggled = true;
				
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
				alarmToggled = true;
				
			
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
				alarmToggled = true;
				
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
				alarmToggled = true;
			
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
				AbstractButton abstractButton =  	(AbstractButton)e.getSource();
				
				// return true or false according 
				// to the selection or deselection  of the button 
				boolean selected = abstractButton.getModel().isSelected();				
				
				alarmToggled = true;
				if (!selected){
					tglbtnOnOff.setText("Alarm OFF");
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
		back.setSize(45, 37);
		back.setLocation(10, 415);

		back.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				try{

					logger.log(Level.CONFIG,"Alarm toggeled: " + alarmToggled);

					if (alarmToggled) {
						

						tm.stopAlarm();

						List<AlarmEntity> aes = sql.loadAllAlarms();

//						if (tglbtnOnOff.isSelected()){

							AlarmEntity ae = null;

							boolean add = false;
							if (aes.size() > 0) {//update
								ae = aes.get(0);

							}else { //add
								ae = new AlarmEntity();
								add = true;
							}

							ae.setHour(String.valueOf(hours));
							ae.setMinutes(String.valueOf(minutes));
							ae.setAlarmSound(btnBuzzer.getText());

							if (Buzzer.valueOf(btnBuzzer.getText()) == Buzzer.RADIO) {
								ae.setRadioId(buzzerSelection.getRadioId());
							}else {
								ae.setRadioId(-1);
							}
							//TODO mp3
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

							ae.setAlarmRepeat(rp);
							if (tglbtnOnOff.isSelected()){
								lblAlarm.setVisible(true);
								ae.setActive(true);
								tm.startAlarm(ae);
							}else {
								ae.setActive(false);
								lblAlarm.setVisible(false);
							}

							if (add) {
								sql.add(ae);
							}else {
								sql.update(ae);
							}

							
							logger.log(Level.INFO, "Alarm SAVED : " + ae);
						
					}
					CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
					cardLayout.show(cardsPanel, "main");

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
		
		wakeUpAlarmOptions = new BuzzerOptionDialog(alarmEnt);
		btnBuzzer = new JButton(Buzzer.BUZZER.name());
		btnBuzzer.setFont(new Font("Tahoma", Font.PLAIN, 25));
		btnBuzzer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {
					wakeUpAlarmOptions.setBuzzerType();

					wakeUpAlarmOptions.setVisible(true);
					
				}catch(Exception ex) {
					logger.log(Level.SEVERE, "Error while choosing the buzzer option", ex);
				}
			}
		});
		if (alarmEnt != null && alarmEnt.getAlarmSound() != null &&  alarmEnt.getAlarmSound().trim().length() > 0){
			btnBuzzer.setText(alarmEnt.getAlarmSound());
		}
		
		btnBuzzer.setBounds(615, 235, 140, 40);
		add(btnBuzzer);
	
		dayDaysToSelect(alarmEnt, theme);
	}
	
	public void setAlarmNotToggled() {
		this.alarmToggled = false;
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		
		buzzerSelection = (BuzzerSelection)evt.getNewValue();
		
		logger.log(Level.CONFIG, "PropChange: " + buzzerSelection);
		
		btnBuzzer.setText(buzzerSelection.getBuzzer().name());
		alarmToggled = true;
		
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
		lblDaySunday.setBounds(55,109,36,33);
		theme.registerLabelTextColor(lblDaySunday, LabelEnums.ALARM_SUNDAY);
		
		JLabel lblDayMonday = new JLabel("M");
//		lblDayMonday.setBorder(new RoundedBorder(Color.BLACK, 40));
		lblDayMonday.setFont(new Font("Tahoma", Font.BOLD, 16));
		monday = new AlarmDayMouseSelector(lblDayMonday, ar.contains(AlarmRepeat.MONDAY) ? true : false);
		lblDayMonday.addMouseListener(monday);
		lblDayMonday.setHorizontalAlignment(SwingConstants.CENTER);
		lblDayMonday.setBounds(55,143,36,33);
		theme.registerLabelTextColor(lblDayMonday, LabelEnums.ALARM_MON);
		
		JLabel lblDayTue = new JLabel("T");
//		lblDayTue.setBorder(new RoundedBorder(Color.BLACK, 40));
		lblDayTue.setFont(new Font("Tahoma", Font.BOLD, 16));
		tuesday = new AlarmDayMouseSelector(lblDayTue, ar.contains(AlarmRepeat.TUESDAY) ? true : false);
		lblDayTue.addMouseListener(tuesday);
		lblDayTue.setHorizontalAlignment(SwingConstants.CENTER);
		lblDayTue.setBounds(55,177,36,33);
		theme.registerLabelTextColor(lblDayTue, LabelEnums.ALARM_TU);
		
		JLabel lblDayWed = new JLabel("W");
//		lblDayWed.setBorder(new RoundedBorder(Color.BLACK, 40));
		lblDayWed.setFont(new Font("Tahoma", Font.BOLD, 16));
		wednesday = new AlarmDayMouseSelector(lblDayWed, ar.contains(AlarmRepeat.WEDNESDAY) ? true : false);
		lblDayWed.addMouseListener(wednesday);
		lblDayWed.setHorizontalAlignment(SwingConstants.CENTER);
		lblDayWed.setBounds(55,211,36,33);
		theme.registerLabelTextColor(lblDayWed, LabelEnums.ALARM_WED);
		
		JLabel lblDayThu = new JLabel("T");
		lblDayThu.setFont(new Font("Tahoma", Font.BOLD, 16));
//		lblDayThu.setBorder(new RoundedBorder(Color.BLACK, 40));
		thursday = new AlarmDayMouseSelector(lblDayThu, ar.contains(AlarmRepeat.THURSDAY) ? true : false);
		lblDayThu.addMouseListener(thursday);
		lblDayThu.setHorizontalAlignment(SwingConstants.CENTER);
		lblDayThu.setBounds(55,245,36,33);
		theme.registerLabelTextColor(lblDayThu, LabelEnums.ALARM_TH);
		
		JLabel lblDayFriday = new JLabel("F");
		lblDayFriday.setFont(new Font("Tahoma", Font.BOLD, 16));
//		lblDayFriday.setBorder(new RoundedBorder(Color.BLACK, 40));
		friday = new AlarmDayMouseSelector(lblDayFriday, ar.contains(AlarmRepeat.FRIDAY) ? true : false);
		lblDayFriday.addMouseListener(friday);
		lblDayFriday.setHorizontalAlignment(SwingConstants.CENTER);
		lblDayFriday.setBounds(55,279,36,33);
		theme.registerLabelTextColor(lblDayFriday, LabelEnums.ALARM_FRI);
		
		JLabel lblDaySat = new JLabel("S");
		lblDaySat.setFont(new Font("Tahoma", Font.BOLD, 16));
//		lblDaySat.setBorder(new RoundedBorder(Color.BLACK, 40));
		saturday = new AlarmDayMouseSelector(lblDaySat, ar.contains(AlarmRepeat.SATURDAY) ? true : false);
		lblDaySat.addMouseListener(saturday);
		lblDaySat.setHorizontalAlignment(SwingConstants.CENTER);
		lblDaySat.setBounds(55,313,36,33);
		theme.registerLabelTextColor(lblDaySat, LabelEnums.ALARM_SAT);
		
		add(lblDaySunday);
		add(lblDayMonday);
		add(lblDayTue);
		add(lblDayWed);
		add(lblDayThu);
		add(lblDayFriday);
		add(lblDaySat);
		
	}

}
