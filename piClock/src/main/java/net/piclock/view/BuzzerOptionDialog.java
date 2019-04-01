package net.piclock.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalToggleButtonUI;

import net.miginfocom.swing.MigLayout;
import net.piclock.enums.Buzzer;
import net.piclock.main.Constants;
import net.piclock.main.Preferences;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.PreferencesHandler;

public class BuzzerOptionDialog extends JDialog {
	
	private static final Logger logger = Logger.getLogger( BuzzerOptionDialog.class.getName() );

	private static final long serialVersionUID = -4331544749034377215L;
//	private static final String BUZZER = "Buzzer";
//	private static final String MP3 = "MP3";
//	private static final String RADIO = "Radio";
	
	private final JPanel contentPanel = new JPanel();
	private SwingContext ct = SwingContext.getInstance();
	
	private JToggleButton tglbtnBuzzer;
	private JToggleButton btnRadio;
	private JToggleButton btnMp3;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			BuzzerOptionDialog dialog = new BuzzerOptionDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public BuzzerOptionDialog() {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setUndecorated(true);

		setLocationRelativeTo(null);
		
		setSize( 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[grow][][grow]", "[][][][][][]"));
		{
			JLabel lblWakeUpAlarm = new JLabel("Wake Up Alarm Options");
			lblWakeUpAlarm.setFont(new Font("Tahoma", Font.BOLD, 16));
			contentPanel.add(lblWakeUpAlarm, "cell 0 0 3 1,alignx center");
		}
		
		
		Preferences pref = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
		
		 ButtonGroup buttonGroup = new ButtonGroup();
		
			tglbtnBuzzer = new JToggleButton("Buzzer");
			tglbtnBuzzer.setUI(new MetalToggleButtonUI() {
				@Override
				protected Color getSelectColor() {
					return Color.GREEN;
				}
			});
			if (Buzzer.valueOf(pref.getAlarmType()) == Buzzer.BUZZER){
				tglbtnBuzzer.doClick();
			}
			
			contentPanel.add(tglbtnBuzzer, "cell 1 2");
		
		
			btnRadio = new JToggleButton("Radio");
			btnRadio.setUI(new MetalToggleButtonUI() {
				@Override
				protected Color getSelectColor() {
					return Color.GREEN;
				}
			});
			if (Buzzer.valueOf(pref.getAlarmType()) == Buzzer.RADIO){
				btnRadio.doClick();
			}
			contentPanel.add(btnRadio, "cell 1 3,growx");
			
			btnRadio.setEnabled(false);//TODO re-enable when function is working		
		
			btnMp3 = new JToggleButton("Mp3");
			btnMp3.setUI(new MetalToggleButtonUI() {
				@Override
				protected Color getSelectColor() {
					return Color.GREEN;
				}
			});
			if (Buzzer.valueOf(pref.getAlarmType()) == Buzzer.MP3){
				btnMp3.doClick();
			}
			
			btnMp3.setEnabled(false); //TODO re-enable when function is working.
			contentPanel.add(btnMp3, "cell 1 4,growx");
			
			buttonGroup.add(tglbtnBuzzer);
			buttonGroup.add(btnRadio);
			buttonGroup.add(btnMp3);		
		
			JLabel lblSelection = new JLabel("FM channel 106.9 selected");
			lblSelection.setFont(new Font("Tahoma", Font.PLAIN, 13));
			contentPanel.add(lblSelection, "cell 0 5 3 1,alignx left");
		
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try{
							boolean close = false;
							Preferences pref = (Preferences)ct.getSharedObject(Constants.PREFERENCES);


							if (tglbtnBuzzer.isSelected()){
								pref.setAlarmType(Buzzer.BUZZER.name());
								close = true;
							}else if(btnRadio.isSelected()){
								pref.setAlarmType(Buzzer.RADIO.name());
								close = true;
							}else if(btnMp3.isSelected()){
								pref.setAlarmType(Buzzer.MP3.name());
								close = true;
							}
							PreferencesHandler.save(pref);

							if (close){
								ct.putSharedObject(Constants.BUZZER_CHANGED, pref.getAlarmType());
								setVisible(false);
							}
						}catch (Exception ex){
							JOptionPane.showMessageDialog(BuzzerOptionDialog.this, "Error in saving, see logs.", "Error Saving", JOptionPane.ERROR_MESSAGE);
							logger.log(Level.SEVERE, "Error saving", ex);
						}
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
						
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}