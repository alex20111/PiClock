package net.piclock.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalToggleButtonUI;

import net.miginfocom.swing.MigLayout;
import net.piclock.db.entity.AlarmEntity;
import net.piclock.db.entity.RadioEntity;
import net.piclock.db.sql.AlarmSql;
import net.piclock.db.sql.RadioSql;
import net.piclock.enums.Buzzer;
import net.piclock.main.Constants;
import net.piclock.main.Preferences;
import net.piclock.swing.component.BuzzerSelection;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.PreferencesHandler;

public class BuzzerOptionDialog extends JDialog {

	private static final Logger logger = Logger.getLogger( BuzzerOptionDialog.class.getName() );

	private static final long serialVersionUID = -4331544749034377215L;

	private final JPanel contentPanel = new JPanel();
	private SwingContext ct = SwingContext.getInstance();

	private JToggleButton tglbtnBuzzer;
	private JToggleButton btnRadio;
	private JToggleButton btnMp3;

	private JComboBox<RadioEntity> radioCmb;
	private int radioSelectedId = -1;
	
	private boolean mp3ScrInit = true;//TODO
	private boolean radioScrInit = true; //used to prevent trigger of the combobox when opening the dialog

	/**
	 * Create the dialog.
	 */
	public BuzzerOptionDialog(AlarmEntity alarmEnt) {

		if (alarmEnt != null && alarmEnt.getRadioId() > 0) {
			radioSelectedId = alarmEnt.getRadioId();
		}
		//TODO if mp3
		
		radioCmb = new JComboBox<>();
		radioCmb.setVisible(false);

		setModalityType(ModalityType.APPLICATION_MODAL);
		setUndecorated(true);

		setLocationRelativeTo(null);

		setSize( 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[grow][][grow]", "[][][][][][]"));

		JLabel lblWakeUpAlarm = new JLabel("Wake Up Alarm Options");
		lblWakeUpAlarm.setFont(new Font("Tahoma", Font.BOLD, 16));
		contentPanel.add(lblWakeUpAlarm, "cell 0 0 3 1,alignx center");

		Preferences pref = (Preferences)ct.getSharedObject(Constants.PREFERENCES);

		ButtonGroup buttonGroup = new ButtonGroup();

		tglbtnBuzzer = new JToggleButton("Buzzer");
		tglbtnBuzzer.setUI(new MetalToggleButtonUI() {
			@Override
			protected Color getSelectColor() {
				return Color.GREEN;
			}
		});

		tglbtnBuzzer.addActionListener(e -> {radioSelectedId = -1; });

		contentPanel.add(tglbtnBuzzer, "cell 1 2");		

		btnRadio = new JToggleButton("Radio");
		btnRadio.setUI(new MetalToggleButtonUI() {
			@Override
			protected Color getSelectColor() {
				return Color.GREEN;
			}
		});

		contentPanel.add(btnRadio, "cell 1 3,growx");

		btnRadio.setEnabled(true);
		btnRadio.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					AbstractButton button = (AbstractButton)e.getSource();
					boolean selected = button.getModel().isSelected();

					logger.log(Level.CONFIG, "Btn radio clicked: Selected - " +selected);
					

					if (selected ) { //if the button is selected and radios exist
						if ( loadRadioList()) {
							radioCmb.setVisible(true);
							if(radioCmb.getSelectedIndex() > -1) {
								radioSelectedId = ((RadioEntity)radioCmb.getSelectedItem()).getId();
							}
						}else {
							radioSelectedId = -1;
							tglbtnBuzzer.doClick();
							JOptionPane.showMessageDialog(BuzzerOptionDialog.this, "No radio station avalaible", "No radio", JOptionPane.INFORMATION_MESSAGE);
						}
					}else {
						radioCmb.setVisible(false);
						radioSelectedId = -1;
					}
				}catch(Exception ex) {
					logger.log(Level.SEVERE, "Error loading", ex);
				}
			}
		});

		radioCmb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (radioCmb != null && radioCmb.getSelectedItem() != null && !radioScrInit) {
				radioSelectedId = ((RadioEntity)radioCmb.getSelectedItem()).getId();
				}
			}
		});
		btnMp3 = new JToggleButton("Mp3");
		btnMp3.setUI(new MetalToggleButtonUI() {
			@Override
			protected Color getSelectColor() {
				return Color.GREEN;
			}
		});

		btnMp3.setEnabled(false); //TODO re-enable when function is working.
		contentPanel.add(btnMp3, "cell 1 4,growx");

		buttonGroup.add(tglbtnBuzzer);
		buttonGroup.add(btnRadio);
		buttonGroup.add(btnMp3);		

		contentPanel.add(radioCmb, "cell 0 5 3 1,alignx center");

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
							BuzzerSelection bs;
							
							if (tglbtnBuzzer.isSelected()){
								bs = new BuzzerSelection(Buzzer.BUZZER);
								ct.putSharedObject(Constants.BUZZER_CHANGED, bs);
								close = true;
							}else if(btnRadio.isSelected()){
								if (radioCmb != null && radioCmb.getSelectedItem() != null) {
									bs = new BuzzerSelection(Buzzer.RADIO,((RadioEntity)radioCmb.getSelectedItem()).getId() );
									ct.putSharedObject(Constants.BUZZER_CHANGED, bs);
								}
								close = true;
							}else if(btnMp3.isSelected()){
								bs = new BuzzerSelection(Buzzer.MP3, -1);//TODO
								ct.putSharedObject(Constants.BUZZER_CHANGED, bs);
								close = true;
							}
							PreferencesHandler.save(pref);

							if (close){
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
	public void setBuzzerType() throws ClassNotFoundException, SQLException {

		radioScrInit = true;
		mp3ScrInit = true;
		radioCmb.setVisible(false);

		AlarmEntity alarm = new AlarmSql().loadActiveAlarm();
		Buzzer buzzer = Buzzer.BUZZER;
		
//		logger.log(Level.CONFIG, "setBuzzerType: " + alarm);

		if (alarm != null) {
			buzzer = Buzzer.valueOf(alarm.getAlarmSound());
			if (buzzer == Buzzer.BUZZER){
				tglbtnBuzzer.doClick();
			}
			if (buzzer == Buzzer.RADIO){
				radioSelectedId = alarm.getRadioId();
				btnRadio.doClick();
			}
			if (buzzer == Buzzer.MP3){
				btnMp3.doClick();
			}
		}
		else {
			tglbtnBuzzer.doClick();
		}
		radioScrInit = false;
		mp3ScrInit = false;
	}
	private boolean loadRadioList() throws ClassNotFoundException, SQLException {
		RadioSql sql = new RadioSql();
		List<RadioEntity> radioList = sql.loadAllRadios();

		if (radioList.size() > 0) {
			RadioEntity toSel = null;
			radioCmb.removeAllItems();
			for(RadioEntity r : radioList) {
				
				radioCmb.addItem(r);
				if (r.getId() == radioSelectedId) {
					logger.log(Level.CONFIG, "radioSelectedId:  --------------->  " + radioSelectedId);
					toSel = r;
				}
			}
			
			if (toSel != null) {
				System.out.println("Radio -!-!-!-!-! : " + toSel);
				radioCmb.setSelectedItem(toSel);
			}
			
			radioCmb.setVisible(true);
			btnRadio.setEnabled(true);
			
			return true;
		}else {
			radioCmb.setVisible(false);
			btnRadio.setEnabled(false);

		}

		return false;
	}
	public int getRadioSelectedId() {
		return radioSelectedId;
	}
	public void setRadioSelectedId(int radioSelectedId) {
		this.radioSelectedId = radioSelectedId;
	}
}