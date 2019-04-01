package net.piclock.view;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.text.NumberFormatter;

import net.miginfocom.swing.MigLayout;
import net.piclock.enums.LabelEnums;
import net.piclock.main.Constants;
import net.piclock.main.Preferences;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;
import net.piclock.util.ImageUtils;
import net.piclock.util.PreferencesHandler;
import net.piclock.weather.City;
import net.piclock.weather.CityNameSort;
import net.weather.action.WeatherAction;
import net.weather.enums.Host;

public class WeatherConfigView extends JPanel {
	
	private static final Logger logger = Logger.getLogger( WeatherConfigView.class.getName() );

	private static final long serialVersionUID = 1L;
	private JCheckBox chckbxActivate;
	private JComboBox<Host> cmbProvider;
//	private JCheckBox chckbxNoUpdate;
	private JComboBox<City> cmbCity;
	private JSpinner refreshInMinutes;

	private  SwingContext ct = SwingContext.getInstance();
	private Preferences prefs;
	
	private JLabel lblCountry;
	private JComboBox cmbCountry;
	
	private int whtChanged = 0; //pos number is active, neg number is not active.
	private JLabel lblCountryLoading;
	private JLabel lblCityLoading;
	private JLabel lblError;
	
	/**
	 * Create the panel.
	 */
	public WeatherConfigView() {
		prefs = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
		
		ThemeHandler theme = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
		
		setSize(480, 320);
		setOpaque(false);
		setLayout(new MigLayout("hidemode 3", "[][85.00][][][grow 80][grow 80]", "[][30px][][][][][][][][][grow][]"));
		
		JLabel lblWeatherConfiguration = new JLabel("Weather Configuration");
		lblWeatherConfiguration.setFont(new Font("Tahoma", Font.PLAIN, 20));
		add(lblWeatherConfiguration, "cell 1 0 4 1,alignx center");
		theme.registerLabelTextColor(lblWeatherConfiguration, LabelEnums.WC_TITILE);
		
		JLabel lblActivate = new JLabel("Activate");
		lblActivate.setFont(new Font("Tahoma", Font.PLAIN, 16));
		add(lblActivate, "cell 0 2");
		theme.registerLabelTextColor(lblActivate, LabelEnums.WC_ACTIVATE);
		
		chckbxActivate = new JCheckBox("");
		chckbxActivate.setOpaque(false);
		chckbxActivate.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				
				enableDisable();
			}
		});	
		
		add(chckbxActivate, "cell 1 2");
		
		JLabel lblProvider = new JLabel("Provider:");
		lblProvider.setFont(new Font("Tahoma", Font.PLAIN, 16));
		add(lblProvider, "cell 0 3,alignx trailing");
		theme.registerLabelTextColor(lblProvider, LabelEnums.WC_PROVIDER);
		
		cmbProvider = new JComboBox<Host>(Host.values());
		cmbProvider.setSelectedIndex(-1); 		
	
		cmbProvider.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
			public Component getListCellRendererComponent(JList<?> list,
		            Object value,
		            int index,
		            boolean isSelected,
		            boolean cellHasFocus) {
				
				
				if (index == -1 && cmbProvider.getSelectedIndex() == -1){
					value = "Select";
				}else{
					value = ((Host) value).getName();
				}
		        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		    }
		});
	
		cmbProvider.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				
				lblError.setVisible(false);
			
				lblCityLoading.setVisible(true);
					
				
				SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
					@Override
					protected Boolean doInBackground() throws Exception {

						try{
							loadProviderCity();

						} catch (IOException e1) {
							lblError.setVisible(true);
							lblError.setText("An Error occured, Rety");
							logger.log(Level.SEVERE, "Error",e1 );
							lblCityLoading.setVisible(false);
						}
						return true;
					}   
					protected void done() {
						lblCityLoading.setVisible(false);
					}
				};
				worker.execute();
			}			
		});
		add(cmbProvider, "cell 1 3 3 1,growx");
		
		 lblCountry = new JLabel("Country");
		lblCountry.setVisible(false);
		lblCountry.setFont(new Font("Tahoma", Font.PLAIN, 16));
		add(lblCountry, "hidemode 3,cell 0 4,alignx trailing");
//		theme.registerLabelTextColor(lblProvider, LabelEnums.WC_PROVIDER);
		
		cmbCountry = new JComboBox();
		cmbCountry.setVisible(false);
		add(cmbCountry, "hidemode 3,cell 1 4 3 1,growx");
		
		lblCountryLoading = new JLabel("");
		lblCountryLoading.setVisible(false);
		add(lblCountryLoading, "cell 4 4,alignx left");
		
		JLabel lblCity = new JLabel("City:");
		lblCity.setFont(new Font("Tahoma", Font.PLAIN, 16));	
		theme.registerLabelTextColor(lblCity, LabelEnums.WC_CITY);
		
		add(lblCity, "cell 0 5,alignx trailing");
		
		cmbCity = new JComboBox<City>();
		
		//resize scrollbar
		Object comp = cmbCity.getUI().getAccessibleChild(cmbCity, 0);
		
		if (comp instanceof JPopupMenu) {
	        JPopupMenu popup = (JPopupMenu) comp;
	        JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
	        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(30, 30));
	    }
		
		add(cmbCity, "cell 1 5 3 1,growx");
		
		lblCityLoading = new JLabel(""); 
		lblCityLoading.setIcon(ImageUtils.getInstance().getButtonLoader());
		lblCityLoading.setOpaque(false);
		lblCityLoading.setVisible(false);
		add(lblCityLoading, "cell 4 5,alignx left");
		
		
		JLabel lblRefreshIntervals = new JLabel("Refresh:");
		lblRefreshIntervals.setFont(new Font("Tahoma", Font.PLAIN, 16));
		add(lblRefreshIntervals, "cell 0 6,alignx trailing");
		theme.registerLabelTextColor(lblRefreshIntervals, LabelEnums.WC_REFRESH);
		
		 refreshInMinutes = new JSpinner();
		 refreshInMinutes.setValue(60);
		 refreshInMinutes.setModel(new SpinnerNumberModel(60,1,1440,1));
		add(refreshInMinutes, "cell 1 6,growx");
		
		
		JFormattedTextField txt = ((JSpinner.NumberEditor) refreshInMinutes.getEditor()).getTextField();
		((NumberFormatter) txt.getFormatter()).setAllowsInvalid(false);
		
		
		JLabel lblMinutes = new JLabel("Minutes");
		lblMinutes.setFont(new Font("Tahoma", Font.PLAIN, 16));
		add(lblMinutes, "cell 2 6");
		theme.registerLabelTextColor(lblMinutes, LabelEnums.WC_MIN);
		
//		chckbxNoUpdate = new JCheckBox("No update when screen is off");
//		chckbxNoUpdate.setFont(new Font("Tahoma", Font.PLAIN, 16));
//		chckbxNoUpdate.setOpaque(false);
//		add(chckbxNoUpdate, "cell 1 7 4 1,alignx left");
		
		JButton btnBack = new JButton("<");
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try{

					boolean canExit = true;
					boolean fireForecastChange = false;

					prefs = (Preferences)ct.getSharedObject(Constants.PREFERENCES);

					if (verifyIfFieldHasChanged(prefs)){

						whtChanged = new Random().nextInt(999999);

						if (!chckbxActivate.isSelected()){
							whtChanged *= -1;
						}
						fireForecastChange = true;					
					}

					prefs.setWeatherActivated(chckbxActivate.isSelected());				

					if(chckbxActivate.isSelected()){
						System.out.println("sel: " + cmbProvider.getSelectedIndex() + " " + chckbxActivate.isSelected() );				

						if (cmbProvider.getSelectedIndex() < 0){
							JOptionPane.showMessageDialog(WeatherConfigView.this, "Please select a provider or de-activate the weather.", "Missing Weather Provide", JOptionPane.INFORMATION_MESSAGE);
							canExit = false;
						}else {

							prefs.setWeatherProvider(((Host)cmbProvider.getSelectedItem()).name());						
							prefs.setWeatherCity(((City)cmbCity.getSelectedItem()).getNameEn());
							prefs.setWeatherRefresh((Integer)refreshInMinutes.getValue());					
							prefs.setStationCode(((City)cmbCity.getSelectedItem()).getKey() );
						}
					}
					if (canExit){

						PreferencesHandler.save(prefs);

						JPanel cardsPanel = (JPanel)ct.getSharedObject(Constants.CARD_PANEL);				

						CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
						cardLayout.show(cardsPanel, Constants.MAIN_VIEW);

						if(fireForecastChange){
							System.out.println("Fire forecast change!!");
							ct.putSharedObject(Constants.FETCH_FORECAST, whtChanged);
						}					
					}
				}catch (Exception ex){
					JOptionPane.showMessageDialog(WeatherConfigView.this, "Error in saving, see logs.", "Error Saving", JOptionPane.ERROR_MESSAGE);
					logger.log(Level.SEVERE, "Error saving", ex);
				}
			}
		});
		
		lblError = new JLabel("");
		lblError.setForeground(Color.RED);
		lblError.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblError.setVisible(false);
		add(lblError, "cell 1 8 4 1");
		add(btnBack, "cell 0 11");
		
		enableDisable();
		
		//check preferences
		if (prefs.isWeatherActivated()){
			chckbxActivate.setSelected(true);
		}
		try {
			loadPreferences();
		} catch (Exception e1) {
			logger.log(Level.SEVERE, "error", e1);
		}		
		
	}
	private void loadPreferences() throws Exception{
		//TODO load country checkbox if needed
		
		if (prefs.getWeatherProvider() != null && prefs.getWeatherProvider().trim().length() > 0){

			Host provider = Host.valueOf(prefs.getWeatherProvider().trim());

			if (provider == Host.envCanada){
				System.out.println("canada");
				lblCountry.setVisible(false);
				cmbCountry.setVisible(false);
				//set provider
				cmbProvider.setSelectedItem(Host.envCanada);
				//load city						
				loadProviderCity();			
				
				if (prefs.getWeatherCity() != null && prefs.getWeatherCity().trim().length() > 0){
					ComboBoxModel<City> model = cmbCity.getModel();
					int size = model.getSize();
					for(int i=0;i<size;i++) {
						City element = model.getElementAt(i);

						if (element.getNameEn().equalsIgnoreCase(prefs.getWeatherCity())){
							cmbCity.setSelectedIndex(i);
							break;
						}
					}
				}
			}else if(provider == Host.weatherUnderground){
				cmbProvider.setSelectedIndex(1);
				lblCountry.setVisible(true);
				cmbCountry.setVisible(true);
				
				//	TODO load country
				if (prefs.getWeatherCountry() != null && prefs.getWeatherCountry().trim().length() > 0){
					
				}
				if (prefs.getWeatherCity() != null && prefs.getWeatherCity().trim().length() > 0){
					
				}				
			}			
			if (prefs.getWeatherRefresh() > 0){
				refreshInMinutes.setValue(prefs.getWeatherRefresh());
			}
		}else {
			//only load provider if prefs does not exist
			
		}
		
	}	
	private void loadProviderCity() throws Exception{

		cmbCity.removeAllItems();
		if ((Host)cmbProvider.getSelectedItem() == Host.envCanada){

			List<City> cities = new ArrayList<City>();


			for(net.weather.bean.City city : WeatherAction.loadAllEnvCanCities(true)){
				City localCity = new City(city);
				cities.add(localCity);

			}
			Collections.sort(cities, new CityNameSort());
			//add cities to box
			for(City c : cities){
				cmbCity.addItem(c);
			}

		}
	}
	private void enableDisable(){
		if (chckbxActivate.isSelected()){
			cmbProvider.setEnabled(true);
//			chckbxNoUpdate.setEnabled(true);
			cmbCity.setEnabled(true);
			refreshInMinutes.setEnabled(true);

		}else{
			cmbProvider.setEnabled(false);
//			chckbxNoUpdate.setEnabled(false);
			cmbCity.setEnabled(false);
			refreshInMinutes.setEnabled(false);
		}
	}
	private boolean verifyIfFieldHasChanged(Preferences prefs){
		boolean changed = false;
		
		System.out.println(prefs.isWeatherActivated() + " chk: " + chckbxActivate.isSelected());
		
		if (prefs.isWeatherActivated() != chckbxActivate.isSelected()){
			changed = true;
		}
		if (chckbxActivate.isSelected()){ //only check when weather is active.
			System.out.println("Active");
			if (prefs.getWeatherCity() != null && cmbCity.getSelectedIndex() != -1 &&
					((City)cmbCity.getSelectedItem()).getNameEn().length() > 0 &&
					!prefs.getWeatherCity().equalsIgnoreCase(((City)cmbCity.getSelectedItem()).getNameEn())){
				changed = true;
			}
			if (prefs.getWeatherRefresh() != (int)refreshInMinutes.getValue()){
				changed = true;
			}
		}
		logger.config("return val of verifyIfFieldHasChanged: " + changed );
		return changed;
	}
}