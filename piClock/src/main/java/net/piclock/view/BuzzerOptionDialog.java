package net.piclock.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.bean.VolumeConfig;
import net.piclock.db.entity.AlarmEntity;
import net.piclock.db.entity.RadioEntity;
import net.piclock.db.sql.Mp3Sql;
import net.piclock.db.sql.RadioSql;
import net.piclock.enums.Buzzer;
import net.piclock.main.Constants;
import net.piclock.main.Preferences;
import net.piclock.swing.component.BuzzerSelection;
import net.piclock.swing.component.KeyBoard;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.MessageListener;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.FormatStackTrace;
import net.piclock.util.PreferencesHandler;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class BuzzerOptionDialog extends JDialog implements MessageListener {

	private static final Logger logger = Logger.getLogger( BuzzerOptionDialog.class.getName() );

	private static final long serialVersionUID = -4331544749034377215L;

	private final JPanel contentPanel = new JPanel();
	private SwingContext ct = SwingContext.getInstance();

	private JToggleButton tglbtnBuzzer;
	private JToggleButton btnRadio;
	private JToggleButton btnMp3;

	private JComboBox<RadioEntity> radioCmb;
	private int radioSelectedId = -1;
	private int mp3SelectedId = -1;
	
	private int lastVolume = -1;
	private JButton btnVolume;
	
	private boolean mp3ScrInit = true;
	private JTextField txtShutdown;
	private JLabel lblMp3TrkName;
	
	private boolean noMp3Selected = false;
	//

	/**
	 * Create the dialog.
	 */
	public BuzzerOptionDialog() {	
		
		radioCmb = new JComboBox<>();
		radioCmb.setFont(new Font("Tahoma", Font.PLAIN, 16));
		radioCmb.setVisible(false);

		setModalityType(ModalityType.APPLICATION_MODAL);
		setUndecorated(true);		
		
		setSize( 450, 300);
		
		setLocationRelativeTo(null);
		
		ct.addMessageChangeListener(Constants.MP3_INFO, this);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[grow][][grow]", "[][][][][][][][][grow][]"));

		JLabel lblWakeUpAlarm = new JLabel("Wake Up Alarm Options");
		lblWakeUpAlarm.setFont(new Font("Tahoma", Font.BOLD, 18));
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
							btnVolume.setVisible(true);
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
					ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
					eh.addError(ErrorType.ALARM, new ErrorInfo(new FormatStackTrace(ex).getFormattedException()));
					logger.log(Level.SEVERE, "Error loading", ex);
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

		btnMp3.addActionListener(l -> {
			if (!mp3ScrInit) { //only handle it when the user click on the button and not when initializing it.
			handleMp3Button();
			}
		});
		contentPanel.add(btnMp3, "cell 1 4,growx");

		buttonGroup.add(tglbtnBuzzer);
		buttonGroup.add(btnRadio);
		buttonGroup.add(btnMp3);		

		contentPanel.add(radioCmb, "cell 0 5 3 1,alignx center");
		
		 lblMp3TrkName = new JLabel("");
		lblMp3TrkName.setHorizontalAlignment(SwingConstants.CENTER);
		contentPanel.add(lblMp3TrkName, "cell 0 6 3 1,alignx center");
		
		JPanel panel = new JPanel();
		contentPanel.add(panel, "cell 0 7 3 1,grow");
		
		JLabel lblShutdownIn = new JLabel("Shutdown in: ");
		lblShutdownIn.setFont(new Font("Tahoma", Font.BOLD, 16));
		panel.add(lblShutdownIn);

		
		txtShutdown = new JTextField();

		txtShutdown.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtShutdown.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				KeyBoard key = new KeyBoard(true);
				if (txtShutdown.getText().trim().length() > 0){					
					key.setText(txtShutdown.getText().trim());
				}

				key.setVisible(true);
			
				if (Integer.parseInt(key.getText()) > 60) {
					JOptionPane.showMessageDialog(BuzzerOptionDialog.this, "Cannot be more than 60 min" , "Time too long", JOptionPane.WARNING_MESSAGE);
				}else {
					txtShutdown.setText(key.getText());	
				}
			}
		});	
		
		panel.add(txtShutdown);
		txtShutdown.setColumns(3);
		
		JLabel lblMinutes = new JLabel("minutes");
		lblMinutes.setFont(new Font("Tahoma", Font.BOLD, 16));
		panel.add(lblMinutes);
		
		 btnVolume = new JButton("Vol");
		contentPanel.add(btnVolume, "cell 2 9,alignx right");
		btnVolume.setVisible(false);
		btnVolume.addActionListener(l -> {
			
			if (radioCmb != null && radioCmb.getSelectedItem() != null) {

				VolumeConfig config = new VolumeConfig(lastVolume);
				config.setRadioId(((RadioEntity)radioCmb.getSelectedItem()).getId());
				config.setFromAlarm(true);
				VolumeNew vol = new VolumeNew(config); 

				vol.setVisible(true);
			}
		});

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

							int shtDownMin = Integer.parseInt(txtShutdown.getText());

							if (tglbtnBuzzer.isSelected()){
								bs = new BuzzerSelection(Buzzer.BUZZER, shtDownMin);
								ct.putSharedObject(Constants.BUZZER_CHANGED, bs);
								close = true;
							}else if(btnRadio.isSelected()){
								//TODO add last volume for radio
								if (radioCmb != null && radioCmb.getSelectedItem() != null) {
									bs = new BuzzerSelection(Buzzer.RADIO,((RadioEntity)radioCmb.getSelectedItem()).getId(), shtDownMin );
									bs.setSelVolume(lastVolume);
									ct.putSharedObject(Constants.BUZZER_CHANGED, bs);
								}
								close = true;
							}else if(btnMp3.isSelected()){
								if (noMp3Selected){
									JOptionPane.showMessageDialog(BuzzerOptionDialog.this, "No Mp3 selected,\nplease select a Mp3 or another option",
											"No Selection", JOptionPane.WARNING_MESSAGE);
								}else {
									bs = new BuzzerSelection(Buzzer.MP3, mp3SelectedId, shtDownMin);
									bs.setSelVolume(lastVolume);
									ct.putSharedObject(Constants.BUZZER_CHANGED, bs);
									close = true;
								}
							}
							PreferencesHandler.save(pref);

							if (close){
								setVisible(false);
							}
						}catch (Exception ex){
							JOptionPane.showMessageDialog(BuzzerOptionDialog.this, "Error in saving, see logs.", "Error Saving", JOptionPane.ERROR_MESSAGE);
							ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
							eh.addError(ErrorType.ALARM, new ErrorInfo(new FormatStackTrace(ex).getFormattedException()));
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
	public void setBuzzerType(AlarmEntity alarmEnt) throws ClassNotFoundException, SQLException {
		
		//reset all variable needed 
		mp3ScrInit = true;
		noMp3Selected = false;
		radioCmb.setVisible(false);
		btnVolume.setVisible(false);
		lblMp3TrkName.setText("");

		//set the button based on the alarm sound type
		Buzzer buzzer = Buzzer.BUZZER;
		
		if (alarmEnt != null ) {

			txtShutdown.setText(String.valueOf(alarmEnt.getAlarmShutdown()));//alarm shutdown time

			if (alarmEnt.getRadioId() > 0) {
				radioSelectedId = alarmEnt.getRadioId();
			}
			if (alarmEnt.getMp3Id() > 0) {
				mp3SelectedId = alarmEnt.getMp3Id();
			}
			
			lastVolume = alarmEnt.getVolume();
			
			buzzer = Buzzer.valueOf(alarmEnt.getAlarmSound());
			if (buzzer == Buzzer.BUZZER){
				tglbtnBuzzer.doClick();
			}
			if (buzzer == Buzzer.RADIO){
				radioSelectedId = alarmEnt.getRadioId();
				btnRadio.doClick();
			}
			if (buzzer == Buzzer.MP3){
				lblMp3TrkName.setText(new Mp3Sql().loadMp3ById(alarmEnt.getMp3Id()).getMp3Name());
				btnMp3.doClick();
			}

		} else {
			txtShutdown.setText("5");
			tglbtnBuzzer.doClick();
		}

			
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
				radioCmb.setSelectedItem(toSel);
			}
			
			radioCmb.setVisible(true);
			btnRadio.setEnabled(true);
			btnVolume.setVisible(true);
			
			return true;
		}else {
			radioCmb.setVisible(false);
			btnRadio.setEnabled(false);
			btnVolume.setVisible(false);

		}

		return false;
	}
	public int getRadioSelectedId() {
		return radioSelectedId;
	}
	public void setRadioSelectedId(int radioSelectedId) {
		this.radioSelectedId = radioSelectedId;
	}
	
	private void handleMp3Button() {
		setVisible(false);
		
		if (lastVolume == -1) {
			logger.log(Level.CONFIG, "Last volume at -1, adjusting to 25");
			lastVolume = 20;
		}

		JPanel contentPane = (JPanel)ct.getSharedObject(Constants.CARD_PANEL);
		//send message to other panel for selecting the row in the table with associated id.			
		ct.sendMessage(Constants.B_VISIBLE_FRM_BUZZ_SEL, new Message(mp3SelectedId, lastVolume));
		CardLayout cardLayout = (CardLayout) contentPane.getLayout(); 
		cardLayout.show(contentPane, Constants.MP3_VIEW);	
	}
	@Override
	public void message(Message message) {
		if(message.getPropertyName().equals(Constants.MP3_INFO)){

			logger.log(Level.CONFIG,"Message in BuzzerOption", message);
			int success = (int)message.getMessagePerIndex(0);
			if (success == 0){
				noMp3Selected = false;			

				mp3SelectedId = (Integer)message.getMessageList().get(1);//comes from alm entity
				lastVolume = (Integer)message.getMessageList().get(2);
				lblMp3TrkName.setText((String)message.getMessagePerIndex(3));
			}else {
				noMp3Selected = true;
			}
		}

	}
	
}