package net.piclock.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.exec.ExecuteException;

import home.misc.Exec;
import net.piclock.main.Constants;
import net.piclock.main.Preferences;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.PreferencesHandler;

public class Volume extends JDialog {
	
	private static final Logger logger = Logger.getLogger( Volume.class.getName() );

	//Add volume call on radio or mp3.
	private static final long serialVersionUID = 1L;
	private final JPanel volumePanel = new JPanel();
	private SwingContext ct;
	private Preferences prefs;
	
	private JSlider slider;

	/**
	 * Create the dialog.
	 */
	public Volume() {
		
		ct = SwingContext.getInstance();
		prefs = (Preferences) ct.getSharedObject(Constants.PREFERENCES);
		
		setModalityType(ModalityType.APPLICATION_MODAL); 
		setUndecorated(true); 
		setLocationRelativeTo(null);
		
		setSize(80,400);
		volumePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		volumePanel.setLayout(new BorderLayout(0, 0));

		getContentPane().add(volumePanel);
		
		slider = new JSlider(JSlider.VERTICAL, 0, 100, 20);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setMinorTickSpacing(1);
        slider.setMajorTickSpacing(10);

		volumePanel.add(slider);
		
		JLabel lblVolume = new JLabel("Vol");
		lblVolume.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblVolume.setHorizontalAlignment(SwingConstants.CENTER);
		volumePanel.add(lblVolume, BorderLayout.NORTH);


		JPanel buttonPane = new JPanel();
		
		volumePanel.add(buttonPane, BorderLayout.SOUTH);

		JButton okButton = new JButton("OK");
		okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));
		
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);		
		
		buttonPane.add(Box.createRigidArea(new Dimension(0, 5)));		
		
		JButton btnMute = new JButton("Mute");
		btnMute.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPane.add(btnMute);
		
		slider.setValue(prefs.getLastVolumeLevel());

		slider.addMouseListener(new MouseAdapter(){
			
			@Override
			public void mouseReleased(MouseEvent e) {			
				try {
					manipulateVolume(slider.getValue());
				} catch (IOException e1) {
					logger.log(Level.CONFIG, "Error " , e1);
				}
			}
		});		

	}
	private void manipulateVolume(int volume) throws ExecuteException, IOException{
		logger.log(Level.CONFIG, "manipulate volume: " + volume);
		Exec exec = new Exec();
		exec.addCommand("amixer").addCommand("-c").addCommand("1").addCommand("set")
		.addCommand("Speaker").addCommand(String.valueOf(volume) + "%").timeout(10000);
		
		int ext = exec.run();
		
		if (ext > 0 ){
			logger.log(Level.INFO, "Problem with volume. Ext: " + ext + "  output: " + exec.getOutput());
		}else {
			prefs.setLastVolumeLevel(slider.getValue());
			PreferencesHandler.save(prefs);
		}
		
//		amixer -c 1 set Speaker 49%
		
	}

}
