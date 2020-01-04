package net.piclock.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;

import net.piclock.bean.VolumeConfig;
import net.piclock.db.entity.Mp3Entity;
import net.piclock.db.entity.RadioEntity;
import net.piclock.db.sql.Mp3Sql;
import net.piclock.db.sql.RadioSql;
import net.piclock.main.Constants;
import net.piclock.main.PiHandler;
import net.piclock.main.Preferences;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.PopupSlider;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.PreferencesHandler;
import javax.swing.BoxLayout;


public class VolumeNew extends JDialog {

	private static final Logger logger = Logger.getLogger( VolumeNew.class.getName() );

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private SwingContext ct;

	private Preferences prefs;
	private PiHandler handler;
	private PopupSlider s;

	private JButton muteButton; 
	private JButton sampleVolBtn;

	private Thread sampleVolThrd;

	private int mutedValue = -1;


	/**
	 * Create the dialog.
	 */
	public VolumeNew(VolumeConfig config) {

		ct = SwingContext.getInstance();
		prefs = (Preferences) ct.getSharedObject(Constants.PREFERENCES);

		handler = PiHandler.getInstance();

		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setUndecorated(true);
		
		setModalityType(ModalityType.APPLICATION_MODAL);
//		setSize(100, 430);
		setBounds(400, 0, 110, 430);
//		setLocation(400, 0);
//		setLocationRelativeTo(null);		
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setPreferredSize(new Dimension(57, 23));
				okButton.addActionListener(l -> {

					if (config.isFromAlarm()){
						System.out.println("Volume Class - send vol for ala: " + s.getSlider().getValue());

						if (sampleVolThrd != null && sampleVolThrd.isAlive()){
							sampleVolThrd.interrupt();
						}
						ct.sendMessage(Constants.VOLUME_SENT_FOR_CONFIG, new Message(s.getSlider().getValue()));
					}else{
						try {

							prefs.setLastVolumeLevel(s.getSlider().getValue());
							PreferencesHandler.save(prefs);
							ct.sendMessage(Constants.VOLUME_SENT_FOR_CONFIG, new Message(s.getSlider().getValue()));
						}catch (IOException e) {
							logger.log(Level.SEVERE, "Error while saving volume in preferences", e);
						}
					}

					this.setVisible(false);
				});
				buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{

				muteButton = new JButton("mute");
				buttonPane.add(muteButton);
				muteButton.addActionListener(l ->{

					try {
						if (muteButton.getText().equalsIgnoreCase("mute")){
							muteButton.setText("UnMute");
							mutedValue = s.getSlider().getValue();
							handler.adjustVolume(0);
						}else{
							muteButton.setText("Mute");
							handler.adjustVolume(mutedValue);
							mutedValue = -1;
						}
					}catch (IOException e) {
						logger.log(Level.SEVERE, "Error in mute" ,e);
					}
				});				
			}			
			sampleVolBtn = new JButton("Test");
			sampleVolBtn.addActionListener(l ->{

				//sample for 5 seconds
				if (sampleVolThrd != null && sampleVolThrd.isAlive()){
					sampleVolThrd.interrupt();
				}

				sampleVolThrd = new Thread(new Runnable(){

					@Override
					public void run() {
						try {
							System.out.println("sampleVolThrd, started");
							if (config.getMp3Id() > 0){
								Mp3Entity m;

								m = new  Mp3Sql().loadMp3ById(config.getMp3Id());

								handler.playMp3(true,m.getMp3FileName(), s.getSlider().getValue());

								try {
									Thread.sleep(6000);
								} catch (InterruptedException e) {
									handler.playMp3(false, "", -1);
									Thread.currentThread().interrupt();
								}

								handler.playMp3(false, "", -1);
							}else if (config.getRadioId() > 0){
								RadioSql sql = new RadioSql();
								RadioEntity r  = sql.loadRadioById(config.getRadioId());
								handler.radioSetChannel(r.radioNameToChannel(), s.getSlider().getValue());
								
								try {
									Thread.sleep(6000);
								} catch (InterruptedException e) {
									handler.radioOff(false);
									Thread.currentThread().interrupt();
								}
								handler.radioOff(false);
							}
							System.out.println("sampleVolThrd, stopped");
						} catch (Exception ex){
							logger.log(Level.SEVERE, "Error in sampling music", ex);
						}
					}
				});
				sampleVolThrd.start();

			});
			buttonPane.add(sampleVolBtn);
		}
		s = new PopupSlider(JSlider.VERTICAL, 0, 100, config.getVolumeLevel());

		s.getSlider().setMinorTickSpacing(5);
		s.getSlider().setMajorTickSpacing(25);
		s.setPaintTicks(true);
		s.setPaintLabels(true);
		s.setThumbDimension(new Dimension(29, 38));


		getContentPane().add(s, BorderLayout.CENTER);

		if (config.isFromAlarm()){
			muteButton.setVisible(false);
			if (config.hasId()){
				sampleVolBtn.setVisible(true);
			}
		}else{
			muteButton.setVisible(true);
			sampleVolBtn.setVisible(false);
		}


		s.getSlider().addMouseListener(new MouseAdapter(){

			@Override
			public void mouseReleased(MouseEvent e) {			
				try {

					handler.adjustVolume(s.getSlider().getValue());
					//					adjustVolume(s.getValue());
				} catch (IOException e1) {
					logger.log(Level.CONFIG, "Error " , e1);
				}
			}
		});		




	} 
	//	private void adjustVolume(int volume) throws IOException{
	//		logger.log(Level.CONFIG, "manipulate volume: " + volume);
	//		Exec exec = new Exec();
	//		exec.addCommand("amixer").addCommand("-c").addCommand("1").addCommand("set")
	//		.addCommand("Speaker").addCommand(String.valueOf(volume) + "%").timeout(10000);
	//		
	//		int ext = exec.run();
	//		
	//		if (ext > 0 ){
	//			logger.log(Level.INFO, "Problem with volume. Ext: " + ext + "  output: " + exec.getOutput());
	//		}
	//		
	////		if (prefs.getLastVolumeLevel() == 0 && s.getValue() > 0) {
	////			logger.log(Level.CONFIG, "Volume unmuted");
	////			ThemeHandler t = (ThemeHandler) ct.getSharedObject(Constants.THEMES_HANDLER);
	////			volumeButton.setIcon(t.getIcon(IconEnum.VOLUME_ICON));
	////			t.registerIconColor(volumeButton, IconEnum.VOLUME_ICON);
	////		}
	//		
	//	}

}
