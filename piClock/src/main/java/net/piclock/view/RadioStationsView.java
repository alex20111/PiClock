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
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.exec.ExecuteException;

import home.misc.Exec;
import net.miginfocom.swing.MigLayout;
import net.piclock.db.entity.RadioEntity;
import net.piclock.db.sql.RadioSql;
import net.piclock.enums.CheckWifiStatus;
import net.piclock.main.Constants;
import net.piclock.main.PiHandler;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.ImageUtils;
import net.piclock.util.VolumeIndicator;

public class RadioStationsView extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger( RadioStationsView.class.getName() );
	private JComboBox<RadioEntity> radioStations;
	private JButton btnReload;
	private JButton btnPlay;
	private JButton btnStop;

	private SwingContext ct;	
	private JLabel lblRadioIcon;

	private PiHandler handler;
	
	/**
	 * Create the panel.
	 * @return 
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public  RadioStationsView(JLabel radioIcon) throws IOException, ClassNotFoundException, SQLException {
		
		handler = PiHandler.getInstance();
		
		ct = SwingContext.getInstance();

		ct.addPropertyChangeListener(Constants.CHECK_INTERNET, this);
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

		JPanel bodyPanel = new JPanel();
		bodyPanel.setOpaque(false);
		add(bodyPanel, BorderLayout.CENTER);
		bodyPanel.setLayout(new MigLayout("", "[][grow 50][][][grow][][][grow 50]", "[][][][100px][][]"));

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

							loadAllRadioStations();

						} catch (ClassNotFoundException | SQLException | IOException e1) {		
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
						


						RadioEntity re = (RadioEntity)radioStations.getSelectedItem();
						logger.log(Level.CONFIG, "Playing : " + re.getRadioName() + " TRACK: " + re.getTrackNbr());

//						Exec exec = new Exec();
//						exec.addCommand("mpc").addCommand("play").addCommand(String.valueOf(re.getTrackNbr())).timeout(10000);

						try {
							handler.playRadio(true, re.getTrackNbr());
//							exec.run();

							btnStop.setEnabled(true);						
							
							fireVolumeIconChange(true);
							
														
						} catch (IOException e1) {
							logger.log(Level.SEVERE, "Error executing music", e1);
						}	
					}catch(Exception ex) {
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
					ct.putSharedObject(Constants.RADIO_VOLUME_ICON_TRIGGER, false);

					try {
						handler.playRadio(false, -1);
						btnStop.setEnabled(false);
						
						fireVolumeIconChange(false);
					} catch (IOException e1) {
						logger.log(Level.SEVERE, "Error stopping music", e1);
					}	
				}catch(Exception ex) {
					logger.log(Level.SEVERE, "Error communicating with arduino", ex);
				}
			}
		});

		setOpaque(false);
		loadAllRadioStations();

	}
	private void loadAllRadioStations() throws ClassNotFoundException, SQLException, ExecuteException, IOException {
		logger.log(Level.CONFIG,"loadAllRadioStations");
		RadioSql sql = new RadioSql();

		radioStations.removeAllItems();

		List<RadioEntity> radios = sql.loadAllRadios();
		for(RadioEntity radio : radios){
			radioStations.addItem(radio);
		}

		//match radio station to mpc play list
		Exec exec = new Exec();
		exec.addCommand("mpc").addCommand("playlist").timeout(10000);

		int ex = exec.run();

		if(ex == 0) {
			String out = exec.getOutput();
			if (out.length() > 0) {
				String outSplit[] = out.split("\n");

				for(int i = 0 ; i <  outSplit.length ; i ++) {

					String play = outSplit[i];
					for(RadioEntity r : radios) {
						if (play.trim().equals(r.getRadioLink())) {
							r.setTrackNbr(i+1);

							sql.update(r);;
							break;
						}
					}
				}
			}

		}else {
			logger.log(Level.SEVERE, "Error greater than 0");
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
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(Constants.CHECK_INTERNET)){
			CheckWifiStatus status = (CheckWifiStatus)evt.getNewValue();
			
			if (status == CheckWifiStatus.SUCCESS){
				btnPlay.setEnabled(true);
			}else if (status == CheckWifiStatus.END_WIFI_OFF) {
				btnPlay.setEnabled(false);
				btnStop.setEnabled(false);			
			}
		}
	}

}