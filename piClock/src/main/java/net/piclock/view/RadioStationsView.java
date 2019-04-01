package net.piclock.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.plaf.basic.BasicArrowButton;

import net.miginfocom.swing.MigLayout;
import net.piclock.main.Constants;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.ImageUtils;

public class RadioStationsView extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger( RadioStationsView.class.getName() );
	
	private JSpinner sleepInMinutes;

	private SwingContext ct = SwingContext.getInstance();
	/**
	 * Create the panel.
	 * @throws IOException 
	 */
	public RadioStationsView() throws IOException {
		setLayout(new BorderLayout(0, 0));
		
		JPanel titlePanel = new JPanel();
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.setLayout(new MigLayout("", "[][grow 70][][grow]", "[]"));
		
		BasicArrowButton back = new BasicArrowButton(BasicArrowButton.WEST); 
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
		lblRadio.setFont(new Font("Tahoma", Font.BOLD, 20));
		titlePanel.add(lblRadio, "cell 2 0");
		
		JPanel bodyPanel = new JPanel();
		add(bodyPanel, BorderLayout.CENTER);
		bodyPanel.setLayout(new MigLayout("", "[][grow][][][grow]", "[][][][][][]"));
		
		JLabel lblSelectRadio = new JLabel("Select Radio:");
		lblSelectRadio.setFont(new Font("Tahoma", Font.PLAIN, 16));
		bodyPanel.add(lblSelectRadio, "cell 0 0,alignx trailing");
		
		JComboBox comboBox = new JComboBox();
		bodyPanel.add(comboBox, "cell 1 0 3 1,growx");
		
		JCheckBox chckbxRadioToSleep = new JCheckBox("Radio to sleep");
		chckbxRadioToSleep.setFont(new Font("Tahoma", Font.PLAIN, 16));
		bodyPanel.add(chckbxRadioToSleep, "cell 0 1 2 1,alignx left");
		
		JLabel lblSleep = new JLabel("Sleep :");
		lblSleep.setFont(new Font("Tahoma", Font.PLAIN, 16));
		bodyPanel.add(lblSleep, "cell 0 2,alignx trailing");
		
		sleepInMinutes = new JSpinner();
		bodyPanel.add(sleepInMinutes, "cell 1 2,alignx left");
		sleepInMinutes.setModel(new SpinnerNumberModel(1,1,1440,1));
		
		JPanel btnPanel = new JPanel();
		bodyPanel.add(btnPanel, "cell 0 4 5 1,grow");
		
		ImageUtils ut = ImageUtils.getInstance();
		
		JButton btnPlay = new JButton();
		btnPlay.setIcon(ut.getImage("play.png"));
		btnPanel.add(btnPlay);
		
		JButton btnStop = new JButton();
		btnStop.setIcon(ut.getImage("stop.png"));
		btnPanel.add(btnStop);
		
		setOpaque(false);
		

		

	}

}