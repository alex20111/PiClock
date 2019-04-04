package net.piclock.view;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import net.piclock.enums.LabelEnums;
import net.piclock.main.Constants;
import net.piclock.main.PiHandler;
import net.piclock.main.Preferences;
import net.piclock.swing.component.KeyBoard;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;
import net.piclock.util.ImageUtils;
import net.piclock.util.PreferencesHandler;
import java.awt.Dimension;

public class ConfigView extends JPanel implements PropertyChangeListener {	
	private static final Logger logger = Logger.getLogger( ConfigView.class.getName() );

	private static final long serialVersionUID = 4789259468261096925L;
	private JTextField txtWifiPass;
	private JButton btnRefreshWifi;
	private JButton btnConnect;
	private JButton btnSave;
	private JButton btnCancel;
	private JComboBox<String> wifiNames;
	private JCheckBox chckbxTurnOffScr;
	
	private KeyBoard keyBoard;
	
	private SwingContext ct = SwingContext.getInstance();
	//onchange old values
	private String oldPassValue = "";
	private String oldWifiValue = "";
	
	private ImageUtils img;
	
	/**
	 * Create the panel.
	 */
	public ConfigView() {
		
		img = ImageUtils.getInstance();
		
		ct.addPropertyChangeListener(Constants.CHECK_INTERNET, this);		
		
		Preferences initPref = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
		ThemeHandler theme = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
		
		setForeground(Color.RED);
		setLayout(new MigLayout("", "[][grow][grow][][grow][]", "[][grow 30][center][center][][][][grow][]"));
		setOpaque(false);
		
		JLabel lblConfig = new JLabel("Config");
		lblConfig.setFont(new Font("Tahoma", Font.BOLD, 35));
		add(lblConfig, "cell 1 0 4 1,alignx center");
		theme.registerLabelTextColor(lblConfig, LabelEnums.CFG_TITLE);
		
		System.out.println("Inti: " + initPref.getWeatherRefresh());
		
		wifiNames = new JComboBox<String>();
		 
		 wifiNames.setPreferredSize(new Dimension(28, 35));
		 wifiNames.addItem("Disable Wifi");
		
		if (initPref.getWifi() != null && initPref.getWifi().length() > 0){
			oldWifiValue = initPref.getWifi();
			wifiNames.addItem(initPref.getWifi());
			wifiNames.setSelectedIndex(1);
		}
		
		btnRefreshWifi = new JButton("Refresh");
		btnRefreshWifi.setFont(new Font("Tahoma", Font.PLAIN, 15));
		btnRefreshWifi.setPreferredSize(new Dimension(71, 35));
		btnRefreshWifi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.log(Level.CONFIG, "refresh Wifi");

				PiHandler handler = PiHandler.getInstance();

				wifiNames.removeAllItems();
				wifiNames.addItem("Disable Wifi");
				btnRefreshWifi.setIcon(img.getButtonLoader());
				btnRefreshWifi.setText("");

				List<String> wifiList;
				try {
					wifiList = handler.fetchWifiList();
					if (!wifiList.isEmpty()){
						for(String s : wifiList){
							wifiNames.addItem(s);
						}
					}

				} catch (Exception e1) {
					logger.log(Level.SEVERE, "Error fetching SSIDs" , e);
				}
				btnRefreshWifi.setIcon(null);
				btnRefreshWifi.setText("Refresh");	

			}
		});
		

		 
		 wifiNames.addActionListener(new ActionListener() {
		 	
		 	@Override
		 	public void actionPerformed(ActionEvent e) {

		 		if (wifiNames.getSelectedIndex() == 0){
		 			txtWifiPass.setText("");					
		 		}				
		 	}
		 });
		 
		 JLabel lblWifi = new JLabel("Wifi:");
		 lblWifi.setFont(new Font("Tahoma", Font.PLAIN, 20));
		 add(lblWifi, "cell 1 2,alignx trailing");
		 theme.registerLabelTextColor(lblWifi, LabelEnums.CFG_WIFI_NAME);
		 
		 add(wifiNames, "cell 2 2,growx");
		add(btnRefreshWifi, "cell 3 2");
		oldPassValue = initPref.getWifiPass();
		
		btnConnect = new JButton("Connect");
		btnConnect.setPreferredSize(new Dimension(73, 35));
		btnConnect.setFont(new Font("Tahoma", Font.PLAIN, 15));
		btnConnect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					if (txtWifiPass.getText().length() == 0){
						JOptionPane.showMessageDialog(ConfigView.this, "Please enter password", "No Password", JOptionPane.ERROR_MESSAGE);
					}else if(wifiNames.getSelectedIndex() == 0){ 
						JOptionPane.showMessageDialog(ConfigView.this, "Please select WIFI", "WIFI", JOptionPane.ERROR_MESSAGE);
					}else{
						btnSave.setEnabled(false);
						btnCancel.setEnabled(false);
						txtWifiPass.setEditable(false);
						btnConnect.setEnabled(false);
						btnConnect.setIcon(img.getButtonLoader());
						btnConnect.setText("");
						PiHandler handler = PiHandler.getInstance();
						handler.connectToWifi((String)wifiNames.getSelectedItem(),txtWifiPass.getText() );
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(ConfigView.this, "Serious error", "Error", JOptionPane.ERROR_MESSAGE);
					logger.log(Level.SEVERE, "Error in Config", e1);
					setConnectOrigValue();
				}
			}
		});
		
		txtWifiPass = new JTextField();
		txtWifiPass.setPreferredSize(new Dimension(6, 35));
		txtWifiPass.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				oldPassValue = txtWifiPass.getText();
				if (txtWifiPass.getText().trim().length() > 0){					
					keyBoard.setText(txtWifiPass.getText().trim());
				}

				keyBoard.setVisible(true);
			
				txtWifiPass.setText(keyBoard.getText());				
			}
		});				
		
		JLabel lblPass = new JLabel("Pass:");
		lblPass.setFont(new Font("Tahoma", Font.PLAIN, 20));
		add(lblPass, "cell 1 3,alignx trailing");
		theme.registerLabelTextColor(lblPass, LabelEnums.CFG_WIFI_PASS);
		
		txtWifiPass.setText(initPref.getWifiPass());
		add(txtWifiPass, "cell 2 3,growx");
		add(btnConnect, "cell 3 3");
		
		chckbxTurnOffScr = new JCheckBox("Turn off screen/use LED display when dark");
		chckbxTurnOffScr.setFont(new Font("Tahoma", Font.PLAIN, 15));
		chckbxTurnOffScr.setOpaque(false);
		chckbxTurnOffScr.setSelected(initPref.isAutoOffScreen());
		add(chckbxTurnOffScr, "cell 2 4 3 1");

		JPanel panel = new JPanel();
		panel.setOpaque(false);
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		add(panel, "cell 0 8 3 1,grow");
		
		btnSave = new JButton("Save");
		btnSave.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnSave.setPreferredSize(new Dimension(80, 35));
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					boolean canSave = true;			

					Preferences p = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
					
					PiHandler handler = PiHandler.getInstance();
					
					if (wifiNames.getSelectedIndex() > 0 && txtWifiPass.getText().trim().length() == 0){
						JOptionPane.showMessageDialog(ConfigView.this, "Please enter a password for wifi" , "Pass missing", JOptionPane.INFORMATION_MESSAGE);
						canSave = false;
					}else if (wifiNames.getSelectedIndex() > 0 && txtWifiPass.getText().trim().length() > 0 && !handler.isWifiConnected() ){ 
						//this means that he has enter the wifi use and pass and he has not tested or cannot connect sonce wrong pass.
						JOptionPane.showMessageDialog(ConfigView.this, "Please test connection before saving.\nClick cancel if other problems." , "Cannot save", JOptionPane.INFORMATION_MESSAGE);
						canSave = false;
					
					}else if(wifiNames.getSelectedIndex() > 0 && handler.isWifiConnected()  &&
							 ( !txtWifiPass.getText().equals(oldPassValue)  || !oldWifiValue.equals((String)wifiNames.getSelectedItem()) )
							){
						JOptionPane.showMessageDialog(ConfigView.this, "Wifi info changed, Please test connection before saving." , "Cannot save", JOptionPane.INFORMATION_MESSAGE);
						canSave = false;
					}
					
					if (canSave){
								
						if (wifiNames.getSelectedIndex() == 0 && handler.isWifiConnected()){
							
							handler.disconnectWifi();
							txtWifiPass.setText("");
						}

						p.setWifi(wifiNames.getSelectedIndex() > 0 ? (String)wifiNames.getSelectedItem() : "");
						p.setWifiPass(txtWifiPass.getText().trim().length() == 0? "" :txtWifiPass.getText() );
						p.setAutoOffScreen(chckbxTurnOffScr.isSelected());

						PreferencesHandler.save(p);
						setVisible(false);
					}
				}catch(Exception ex){
					JOptionPane.showMessageDialog(ConfigView.this, "Error in saving, see logs.", "Error Saving", JOptionPane.ERROR_MESSAGE);
					logger.log(Level.SEVERE, "Error saving", ex);
				}
			}
		});
		panel.add(btnSave);
		
		btnCancel = new JButton("Cancel");
		btnCancel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnCancel.setPreferredSize(new Dimension(90, 35));
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (!PiHandler.getInstance().isWifiConnected()){
					//empty pass if connection not success.
					txtWifiPass.setText("");
				}				
				JPanel cardsPanel = (JPanel)ct.getSharedObject(Constants.CARD_PANEL);				

				CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
				cardLayout.show(cardsPanel, Constants.MAIN_VIEW);
			}
		});
		panel.add(btnCancel);

		keyBoard = new KeyBoard();
		keyBoard.setLocationRelativeTo(null);
	}
	private void setConnectOrigValue(){
		btnSave.setEnabled(true);
		btnCancel.setEnabled(true);
		txtWifiPass.setEditable(true);
		btnConnect.setEnabled(true);
		btnConnect.setIcon(null);
		btnConnect.setText("Connect");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (isVisible()){
			String initValue = (String)evt.getNewValue();
			String value = initValue.substring(4,initValue.length() );

			if ("success".equals(value)){
				JOptionPane.showMessageDialog(ConfigView.this, "Connection Successful", "Success", JOptionPane.INFORMATION_MESSAGE);
				oldPassValue = txtWifiPass.getText();
				oldWifiValue = (String)wifiNames.getSelectedItem();
				setConnectOrigValue();

			}else if (!"starting".equals(initValue) && !"disconnect".equals(value)){
				JOptionPane.showMessageDialog(ConfigView.this, "Cannot Connect, wrong password or other problems.", "No Connection", JOptionPane.ERROR_MESSAGE);
				setConnectOrigValue();
			}
		}
	}
}