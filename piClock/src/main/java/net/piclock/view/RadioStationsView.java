package net.piclock.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.exec.ExecuteException;

import net.miginfocom.swing.MigLayout;
import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.bean.VolumeConfig;
import net.piclock.db.entity.RadioEntity;
import net.piclock.db.sql.RadioSql;
import net.piclock.enums.CheckWifiStatus;
import net.piclock.enums.IconEnum;
import net.piclock.enums.LabelEnums;
import net.piclock.handlers.PiHandler;
import net.piclock.main.Constants;
import net.piclock.main.Preferences;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.MessageListener;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;
import net.piclock.util.FormatStackTrace;
import net.piclock.util.ImageUtils;
import net.piclock.util.VolumeIndicator;

public class RadioStationsView extends JPanel implements PropertyChangeListener, MessageListener {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger( RadioStationsView.class.getName() );
	private JComboBox<RadioEntity> radioStations;
	private JButton btnReload;
	private JButton btnPlay;
	private JButton btnStop;
	private JButton btnVolume;
	private JLabel lblNowPlaying;
	private String nowPlayingText = "Now playing: ";

	private SwingContext ct;	
	private JLabel lblRadioIcon;

	private PiHandler handler;
	
	private RadioSql sql ;
	
	Thread playingThread;
	
	Process process;
	
	/**
	 * Create the panel.
	 * @return 
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public  RadioStationsView(JLabel radioIcon) throws IOException, ClassNotFoundException, SQLException {
		sql = new RadioSql();
		
		sql.CreateRadioTable();
		
		handler = PiHandler.getInstance();
		
		ct = SwingContext.getInstance();
		
		ThemeHandler t = (ThemeHandler) ct.getSharedObject(Constants.THEMES_HANDLER);

		ct.addPropertyChangeListener(Constants.CHECK_INTERNET, this);
		ct.addMessageChangeListener(Constants.TURN_OFF_ALARM , this);
		ct.addMessageChangeListener(Constants.RADIO_STREAM_ERROR , this);
		ct.addMessageChangeListener(Constants.MUSIC_TOGGELED , this);
		ct.addPropertyChangeListener(Constants.RADIO_VOLUME_ICON_TRIGGER , this);
		
		
		this.lblRadioIcon = radioIcon;
		setLayout(new BorderLayout(0, 0));

		ct = SwingContext.getInstance();
		setOpaque(false);


		JPanel titlePanel = new JPanel();
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.setLayout(new MigLayout("", "[][grow 70][][grow]", "[]"));
		titlePanel.setOpaque(false);
		JButton back = new JButton("<"); 
		back.setPreferredSize(new Dimension(41, 30));
		titlePanel.add(back, "cell 0 0");		

		back.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				logger.log(Level.CONFIG,"Back");
				JPanel cardsPanel = (JPanel)ct.getSharedObject(Constants.CARD_PANEL);

				CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
				cardLayout.show(cardsPanel, Constants.MAIN_VIEW);	

			}
		});

		JLabel lblRadio = new JLabel("Radio");
		lblRadio.setFont(new Font("Tahoma", Font.BOLD, 35));
		titlePanel.add(lblRadio, "cell 2 0");
		t.registerLabelTextColor(lblRadio, LabelEnums.RADIO_TITLE);

		JPanel bodyPanel = new JPanel();
		bodyPanel.setOpaque(false);
		add(bodyPanel, BorderLayout.CENTER);
		bodyPanel.setLayout(new MigLayout("", "[][grow 50][][][grow][][][grow 50]", "[][][][100px][][][grow][]"));

		JLabel lblSelectRadio = new JLabel("Select Radio:");
		lblSelectRadio.setFont(new Font("Tahoma", Font.PLAIN, 16));
		bodyPanel.add(lblSelectRadio, "cell 2 1,alignx trailing");

		radioStations = new JComboBox<RadioEntity>();
		radioStations.setPreferredSize(new Dimension(28, 35));
		bodyPanel.add(radioStations, "cell 3 1 3 1,growx");

		btnReload = new JButton("Reload");
		btnReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnReload.setText("Loading");
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						try {
							lblNowPlaying.setVisible(false);
							loadAllRadioStations();

						} catch (ClassNotFoundException | SQLException | IOException e1) {
							ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
							eh.addError(ErrorType.RADIO, new ErrorInfo(new FormatStackTrace(e1).getFormattedException()));
							logger.log(Level.SEVERE, "Error in loading" , e1);
						}
						
						btnReload.setText("Reload");
					}
				});			
			}
		});
		btnReload.setPreferredSize(new Dimension(65, 30));
		btnReload.setFont(new Font("Tahoma", Font.PLAIN, 16));
		bodyPanel.add(btnReload, "cell 6 1");		
		
		lblNowPlaying = new JLabel("");
		lblNowPlaying.setFont(new Font("Tahoma", Font.PLAIN, 16));
		bodyPanel.add(lblNowPlaying, "cell 3 2 4 1");
		
		t.registerLabelTextColor(lblNowPlaying, LabelEnums.RADIO_NOW_PLAYING);

		JPanel btnPanel = new JPanel();
		btnPanel.setOpaque(false);
		FlowLayout flowLayout = (FlowLayout) btnPanel.getLayout();
		flowLayout.setHgap(15);
		bodyPanel.add(btnPanel, "cell 0 4 8 1,grow");

		ImageUtils ut = ImageUtils.getInstance();

		btnPlay = new JButton();
		btnPlay.setIcon(ut.getImage("play.png"));
		btnPanel.add(btnPlay);
		btnPlay.setEnabled(false);

		btnPlay.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(radioStations.getSelectedIndex() != -1) {					
					try {

						lblRadioIcon.setVisible(true);
						lblNowPlaying.setText("");
						lblNowPlaying.setVisible(true);

						RadioEntity re = (RadioEntity)radioStations.getSelectedItem();
						logger.log(Level.INFO, "Playing : " + re.getRadioName() + " TRACK: " + re.getTrackNbr());
						
						Preferences prefs  = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
						
						handler.playRadio(true, re.getRadioLink(), prefs.getLastVolumeLevel());

						btnStop.setEnabled(true);						

						fireVolumeIconChange(true);
						
						lblNowPlaying.setText(nowPlayingText + re.getRadioName());
						
						btnVolume.setVisible(true);						

					}catch(Exception ex) {
						lblRadioIcon.setVisible(false);
						btnStop.setEnabled(false);
						fireVolumeIconChange(false);
						ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
						eh.addError(ErrorType.RADIO, new ErrorInfo(new FormatStackTrace(ex).getFormattedException()));
						logger.log(Level.SEVERE, "Error communicating with arduino", ex);
					}
				}
			}
		});

		btnStop = new JButton();
		btnStop.setIcon(ut.getImage("stop.png"));
		btnStop.setEnabled(false);
		btnPanel.add(btnStop);

		btnStop.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					

					lblRadioIcon.setVisible(false);					
					lblNowPlaying.setVisible(false);
					
					btnStop.setEnabled(false);

					fireVolumeIconChange(false);
					
					handler.playRadio(false, "", -1);

				}catch(Exception ex) {
					ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
					eh.addError(ErrorType.RADIO, new ErrorInfo(new FormatStackTrace(ex).getFormattedException()));
					logger.log(Level.SEVERE, "Error trying to play stream", ex);
				}
			}
		});

		JPanel panel = new JPanel();
		panel.setOpaque(false);
		bodyPanel.add(panel, "cell 6 7");
		
		 btnVolume = new JButton("");
		btnVolume.setFocusPainted(false);
		btnVolume.setOpaque(false);
		btnVolume.setContentAreaFilled(false);
		btnVolume.setBorderPainted(false);
		btnVolume.setVisible(false);
		btnVolume.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
							
				Preferences prefs  = (Preferences) ct.getSharedObject(Constants.PREFERENCES);
				VolumeConfig config = new VolumeConfig(prefs.getLastVolumeLevel());
				VolumeNew vol = new VolumeNew(config);
//				Volume vol = new Volume(btnVolume, IconEnum.VOLUME_ICON_RADIO, IconEnum.VOLUME_MUTED_RADIO);
				vol.setVisible(true);
				
		
			}
		});
		
		t.registerIconColor(btnVolume, IconEnum.VOLUME_ICON_RADIO);
		panel.add(btnVolume);
		
		setOpaque(false);
		loadAllRadioStations();
		
	}
	private void loadAllRadioStations() throws ClassNotFoundException, SQLException, ExecuteException, IOException {
		logger.log(Level.CONFIG,"loadAllRadioStations");
		

		radioStations.removeAllItems();

		List<RadioEntity> radios = sql.loadAllRadios();
		for(RadioEntity radio : radios){
			radioStations.addItem(radio);
		}

	}
	
	private void fireVolumeIconChange(boolean displayOn) {
		VolumeIndicator vi = (VolumeIndicator)ct.getSharedObject(Constants.RADIO_VOLUME_ICON_TRIGGER);			
		
		if (vi == null) {
			vi =  new VolumeIndicator();
			vi.setRadioPlaying(displayOn);
			ct.putSharedObject(Constants.RADIO_VOLUME_ICON_TRIGGER, vi);
		}else {
			
			VolumeIndicator viNew = new VolumeIndicator();
			viNew.setMp3Playing(vi.isMp3Playing());
			viNew.setRadioPlaying(displayOn);
			ct.putSharedObject(Constants.RADIO_VOLUME_ICON_TRIGGER, viNew);
			
		}
	}
	
	private void autoChange(boolean wifiProblem) {
		logger.log(Level.CONFIG, "Property change RADIO VIEWWWWW !!!!!!!!!!");
		if (wifiProblem) {
			btnPlay.setEnabled(false);
		}
		btnStop.setEnabled(false);
		VolumeIndicator vi = (VolumeIndicator) ct.getSharedObject(Constants.RADIO_VOLUME_ICON_TRIGGER);
		if(vi != null && vi.isRadioPlaying()) {
			try {
				handler.playRadio(false, "" , -1);
			} catch (Exception e) {
				logger.log(Level.CONFIG, "Error in property change", e);
			}
			fireVolumeIconChange(false);
			lblRadioIcon.setVisible(false);	
			lblNowPlaying.setVisible(false);
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		logger.log(Level.CONFIG, "RADio VIew property change, " + evt.getPropertyName());
		if(evt.getPropertyName().equals(Constants.CHECK_INTERNET)){
			CheckWifiStatus status = (CheckWifiStatus)evt.getNewValue();
			
			if (status == CheckWifiStatus.SUCCESS){
				btnPlay.setEnabled(true);
			}else if (!status.isConnected()) {

				autoChange(true);
			}
		}else if(evt.getPropertyName().equals(Constants.RADIO_VOLUME_ICON_TRIGGER) ){
			VolumeIndicator vi = (VolumeIndicator)evt.getNewValue();
			logger.log(Level.CONFIG, "Volume icon indicator: " + vi.displayVolumeIcon());			
			btnVolume.setVisible(vi.displayVolumeIcon());
		}
	}
	@Override
	public void message(Message message) {
		logger.log(Level.CONFIG, "Recieved message RADIO VIEW!!: " + message.getPropertyName());
		if (Constants.TURN_OFF_ALARM.equals(message.getPropertyName())) {
			autoChange(false);
		}else if(Constants.RADIO_STREAM_ERROR.equals(message.getPropertyName())) {
			if (isVisible()) {
				JOptionPane.showMessageDialog(this, "Cannot play Radio stream, verify stream", "Error in stream", JOptionPane.ERROR_MESSAGE);
			}
			autoChange(false);
		}else if (message.getPropertyName().equals(Constants.MUSIC_TOGGELED)) {
			logger.log(Level.CONFIG, "Music toggeled: " + message);
			String msg = (String)message.getFirstMessage();
			if (msg.equals("radiooff")) {
				logger.log(Level.CONFIG, "Music toggeled ---- OOOOFFFFRRRR: " + message);
				btnStop.setEnabled(false);
				lblRadioIcon.setVisible(false);	
				lblNowPlaying.setVisible(false);
			}
		}		
	}
}