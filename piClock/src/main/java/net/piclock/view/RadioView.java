package net.piclock.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.bean.VolumeConfig;
import net.piclock.db.entity.RadioEntity;
import net.piclock.db.sql.RadioSql;
import net.piclock.enums.IconEnum;
import net.piclock.enums.LabelEnums;
import net.piclock.handlers.PiHandler;
import net.piclock.main.Constants;
import net.piclock.main.Preferences;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.MessageListener;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;
import net.piclock.thread.ScreenAutoClose;
import net.piclock.util.FormatStackTrace;
import net.piclock.util.VolumeIndicator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.awt.event.ActionEvent;

public class RadioView extends JPanel implements MessageListener{

	private static final Logger logger = Logger.getLogger( RadioView.class.getName() );
	
	private static final long serialVersionUID = 1L;
	private static final int HIGH_END_FRQ = 1081;
	private static final int LOW_END_FRQ = 880;

	private PiHandler handler;

	private JButton btnPlay;
	private JList<RadioEntity> stationList;

	private int channel = 1069;
	private JLabel labelRadioFrq;
	private DecimalFormat df = new DecimalFormat("#.0"); 
	
	private JLabel lblRadioIcon;
	private Preferences prefs;
	private JButton btnVolume ;
	private JComboBox<Integer> timeToSleep;
	
	private Thread radioShutDown = null;

	/**
	 * Create the panel.
	 */
	public RadioView(JLabel radioIcon) {

		this.lblRadioIcon = radioIcon;
		handler = PiHandler.getInstance();
		setOpaque(false);
				
		
		ThemeHandler t = (ThemeHandler) SwingContext.getInstance().getSharedObject(Constants.THEMES_HANDLER);
		
		prefs = (Preferences)SwingContext.getInstance().getSharedObject(Constants.PREFERENCES);

		SwingContext.getInstance().addMessageChangeListener(Constants.MUSIC_TOGGELED, this);
		setLayout(new BorderLayout(0, 0));
		JPanel titlePanel = new JPanel();
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.setOpaque(false);

		JLabel lblRadio = new JLabel("Radio");
		lblRadio.setFont(new Font("Tahoma", Font.BOLD, 35));
		titlePanel.add(lblRadio);
		t.registerLabelTextColor(lblRadio, LabelEnums.RADIO_TITLE);

		JPanel mainPanel = new JPanel();
		add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new MigLayout("", "[150px][grow]", "[][grow][][][40px]"));
		mainPanel.setOpaque(false);

		DefaultListModel<RadioEntity> m = new DefaultListModel<>();

		JLabel lblStations = new JLabel("Stations");
		lblStations.setFont(new Font("Tahoma", Font.BOLD, 13));
		t.registerLabelTextColor(lblStations, LabelEnums.RADIO_STATIONS);
		mainPanel.add(lblStations, "cell 0 0");

		stationList = new JList<>(m);
		mainPanel.add(stationList, "cell 0 1,grow");

		JPanel nowPlayingPanel = new JPanel();
		mainPanel.add(nowPlayingPanel, "cell 1 1 1 4,grow");
		nowPlayingPanel.setLayout(new BorderLayout(0, 0));
		nowPlayingPanel.setOpaque(false);

//		JPanel presetPanel = new JPanel();
//		nowPlayingPanel.add(presetPanel, BorderLayout.SOUTH);
//		presetPanel.setOpaque(false);

		JPanel nowPlayingTitlepanel = new JPanel();
		nowPlayingPanel.add(nowPlayingTitlepanel, BorderLayout.NORTH);
		nowPlayingTitlepanel.setLayout(new MigLayout("", "[grow][center][][grow]", "[][][][50px][40px]"));
		nowPlayingTitlepanel.setOpaque(false);

		JLabel lblNowPlaying = new JLabel("Now Playing"); //TODO add in themes
		lblNowPlaying.setFont(new Font("Tahoma", Font.PLAIN, 18));
		nowPlayingTitlepanel.add(lblNowPlaying, "cell 0 0 4 1,alignx center");

		labelRadioFrq = new JLabel("106.9");
		labelRadioFrq.setFont(new Font("Tahoma", Font.BOLD, 70));
		nowPlayingTitlepanel.add(labelRadioFrq, "cell 0 2 4 1,alignx center");
		
		t.registerLabelTextColor(labelRadioFrq, LabelEnums.RADIO_NOW_PLAYING);

		JButton scanDown = new JButton("<");
		scanDown.setPreferredSize(new Dimension(41, 30));
		scanDown.setFont(new Font("Tahoma", Font.BOLD, 15));
		scanDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					channel -= 1;
					
					logger.log(Level.CONFIG, "CHANNEL DOWN: " + channel);

					if (channel < LOW_END_FRQ ) {
						channel = HIGH_END_FRQ;
					}

					if (handler.isRadioOn()) {
						handler.radioSetChannel(channel, prefs.getLastVolumeLevel());
					}
					labelRadioFrq.setText(df.format(channel/10.0));
				} catch (Exception e1) {
					logger.log(Level.SEVERE, "Error", e1);
				}
			}
		});


		nowPlayingTitlepanel.add(scanDown, "cell 0 4,alignx right");

		btnPlay = new JButton("Play");
		btnPlay.setPreferredSize(new Dimension(53, 30));
		btnPlay.setFont(new Font("Tahoma", Font.BOLD, 15));
		btnPlay.addActionListener(l -> {
			handlePlayStopButton();
		});
		nowPlayingTitlepanel.add(btnPlay, "cell 1 4");

		JButton scanUp = new JButton(">");
		scanUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					channel += 1;
					
					if (channel > HIGH_END_FRQ) {
						channel = LOW_END_FRQ;
					}
					if (handler.isRadioOn()) {
						handler.radioSetChannel(channel, prefs.getLastVolumeLevel());
					}
					labelRadioFrq.setText(df.format(channel/10.0));

				}catch(Exception ex) {
					logger.log(Level.SEVERE, "Error", ex);
				}
			}
		});
		scanUp.setPreferredSize(new Dimension(41, 30));
		scanUp.setFont(new Font("Tahoma", Font.BOLD, 15));
		nowPlayingTitlepanel.add(scanUp, "cell 2 4");

		JButton btnAddStation = new JButton("+");
		btnAddStation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				DefaultListModel<RadioEntity> listModel = (DefaultListModel<RadioEntity>)stationList.getModel();

				boolean doesNotExist = true;
				//verify if exist first
				for(int i = 0; i< listModel.getSize();i++){
					RadioEntity listChannel  = (RadioEntity)listModel.getElementAt(i);
			
					if (StringToChannel(listChannel.getRadioName()) == channel) {
						JOptionPane.showMessageDialog(RadioView.this, "Station already exist" ,"Exist" , JOptionPane.INFORMATION_MESSAGE);
						doesNotExist=false;
						break;
					}
				}

				if (doesNotExist) {
					
					String formattedChannel = df.format(channel/10.0);
					RadioEntity radio = new RadioEntity();
					radio.setRadioName(formattedChannel);
					
					listModel.addElement(radio);				
					
					RadioSql sql = new RadioSql();
					try {
						sql.add(radio);
					} catch (ClassNotFoundException | SQLException e1) {
						logger.log(Level.CONFIG, "Error in sql", e1);
					}
				}

			}
		});
		btnAddStation.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnAddStation.setForeground(Color.BLACK);
		mainPanel.add(btnAddStation, "flowx,cell 0 2,alignx center");

		JButton btnBack = new JButton("<");
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ScreenAutoClose.stop();
				} catch (InterruptedException e1) {
					ErrorHandler eh = (ErrorHandler)SwingContext.getInstance().getSharedObject(Constants.ERROR_HANDLER);
	  				eh.addError(ErrorType.RADIO, new ErrorInfo(new FormatStackTrace(e1).getFormattedException()));
					logger.log(Level.INFO ,  "Cannot stop scren auto close", e);
				}
				JPanel contentPane = (JPanel)SwingContext.getInstance().getSharedObject(Constants.CARD_PANEL);
				CardLayout cardLayout = (CardLayout) contentPane.getLayout(); 
				cardLayout.show(contentPane, Constants.MAIN_VIEW);				
			}
		});
		btnBack.setPreferredSize(new Dimension(41, 30));
		btnBack.setFont(new Font("Tahoma", Font.BOLD, 13));
		mainPanel.add(btnBack, "cell 0 4,aligny top");

		JButton btnDelStation = new JButton("X");
		btnDelStation.addActionListener(new ActionListener() {
			@SuppressWarnings("rawtypes")
			public void actionPerformed(ActionEvent e) {
				try {

					int idx = stationList.getSelectedIndex();

					if (idx > -1) {
						@SuppressWarnings("unchecked")
						DefaultListModel<RadioEntity> listModel = (DefaultListModel)stationList.getModel();
						RadioEntity toDel = listModel.get(idx);

						RadioSql sql = new RadioSql();
						sql.delete(toDel.getId());

						listModel.remove(idx);
					}
				}catch(Exception ex) {
					logger.log(Level.SEVERE, "error in deleting radio station", ex);
				}

			}
		});
		btnDelStation.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnDelStation.setForeground(Color.RED);
		mainPanel.add(btnDelStation, "cell 0 2");
		
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
				config.setMsgPropertyName("");
				VolumeNew vol = new VolumeNew(config);
				vol.setVisible(true);
				
			}
		});
		mainPanel.add(btnVolume, "cell 1 3,alignx center");
		t.registerIconColor(btnVolume, IconEnum.VOLUME_ICON_RADIO);

		
		JPanel shutDownPanel = new JPanel();
		shutDownPanel.setOpaque(false);
		mainPanel.add(shutDownPanel, "flowx,cell 1 0,growx");
		shutDownPanel.setLayout(new MigLayout("", "[grow][][][]", "[]"));
		
		JLabel sleep = new JLabel("Auto Off:");
		t.registerLabelTextColor(sleep, LabelEnums.RADIO_SLEEP);
		shutDownPanel.add(sleep, "cell 1 0");
		sleep.setFont(new Font("Tahoma", Font.BOLD, 18));
		timeToSleep = new JComboBox<>();
		timeToSleep.setFont(new Font("Tahoma", Font.PLAIN, 20));
		shutDownPanel.add(timeToSleep, "cell 2 0");
		timeToSleep.addItem(0);
		timeToSleep.addItem(1);
		timeToSleep.addItem(5);		
		timeToSleep.addItem(10);
		timeToSleep.addItem(15);
		timeToSleep.addItem(20);
		timeToSleep.addItem(25);
		timeToSleep.addItem(30);
		timeToSleep.addItem(35);
		timeToSleep.addItem(40);
		timeToSleep.addItem(45);
		timeToSleep.addItem(50);
		timeToSleep.addItem(55);
		timeToSleep.addItem(60);
		timeToSleep.addItem(70);
		timeToSleep.addItem(80);
		
		timeToSleep.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED && handler.isRadioOn()) {
					handleRadioShutDown();				
				}
			}
		});
		
		stationList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				try {
					RadioEntity item = stationList.getSelectedValue();

					if (item != null) {
						channel = StringToChannel(item.getRadioName());

						if (handler.isRadioOn()) {

							handler.radioSetChannel(channel, prefs.getLastVolumeLevel());

						}
						
						labelRadioFrq.setText(df.format(channel/10.0));
					}

				} catch (Exception e1) {
					logger.log(Level.CONFIG, "Error" , e1);
				}

			}
		});
		
		loadListFromDb();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void loadListFromDb() {
		
		try {
			
			
			RadioSql sql = new RadioSql();
			sql.CreateRadioTable();
			List<RadioEntity> radios = sql.loadAllRadios();
			DefaultListModel<RadioEntity> listModel = (DefaultListModel)stationList.getModel();
			
			//populate list
			listModel.removeAllElements();
			for(RadioEntity station : radios) {
				listModel.addElement(station);
			}

		} catch (IllegalStateException |  ClassNotFoundException | SQLException | IOException e) {
			logger.log(Level.CONFIG, "Error in loadList", e);
		}
		
		
	}
	private int StringToChannel(String strChannel) {
		float channelFloat = Float.parseFloat(strChannel);

		return (int) (channelFloat * 10);
	}
	private void handlePlayStopButton() {
		try {

			if (!handler.isRadioOn()) {

				lblRadioIcon.setVisible(true);
				handler.radioSetChannel(channel,prefs.getLastVolumeLevel());
				btnPlay.setText("Stop");
				btnVolume.setVisible(true);
				fireVolumeIconChange(true);
				handleRadioShutDown();
			}else {
				handler.radioOff(false);

				lblRadioIcon.setVisible(false);
				btnPlay.setText("Play");
				btnVolume.setVisible(false);
				fireVolumeIconChange(false);
				shutDownThread();
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error", e);
		}
	}

	
	private void fireVolumeIconChange(boolean displayOn) {
		VolumeIndicator vi = (VolumeIndicator)SwingContext.getInstance().getSharedObject(Constants.RADIO_VOLUME_ICON_TRIGGER);			
		
		if (vi == null) {
			vi =  new VolumeIndicator();
			vi.setRadioPlaying(displayOn);
			SwingContext.getInstance().putSharedObject(Constants.RADIO_VOLUME_ICON_TRIGGER, vi);
		}else {
			
			VolumeIndicator viNew = new VolumeIndicator();
			viNew.setMp3Playing(vi.isMp3Playing());
			viNew.setRadioPlaying(displayOn);
			SwingContext.getInstance().putSharedObject(Constants.RADIO_VOLUME_ICON_TRIGGER, viNew);
			
		}
	}
	
	private void handleRadioShutDown() {

		shutDownThread();

		int minutes = (Integer)timeToSleep.getSelectedItem();

		if (minutes > 0) {

			radioShutDown = new Thread(new Runnable() {

				@Override
				public void run() {
					logger.log(Level.CONFIG, "radio shut down: Auto Off start in run. Shutdown time: " + minutes);
					try {
						Thread.sleep(minutes * 60 * 1000);
						if (handler.isRadioOn()) {
							btnPlay.doClick();
						}
						logger.log(Level.INFO, "autoAlarmShutOff: Turning off alarm automatically.");

					}catch(InterruptedException i) {
						Thread.currentThread().interrupt();
					}
					logger.log(Level.CONFIG, "radio shut down: end run method");
				}		
			});
			radioShutDown.start();
		}

		

	}
	private void shutDownThread() {
		if (radioShutDown != null && radioShutDown.isAlive()) {
			radioShutDown.interrupt();
			logger.log(Level.CONFIG, "handleRadioShutDown not null and interrupted");
		}
	}

	@Override
	public void message(Message message) {
		if (message.getPropertyName().equals(Constants.MUSIC_TOGGELED)) {
			logger.log(Level.CONFIG, "Music toggeled: " + message);
			String msg = (String)message.getFirstMessage();
			if (msg.equals("radiooff")) {
				logger.log(Level.CONFIG, "Music toggeled ---- OOOOFFFFRRRR: " + message);
				btnPlay.setText("Play");
				lblRadioIcon.setVisible(false);	
				btnVolume.setVisible(false);
			}
		}
	}

}
