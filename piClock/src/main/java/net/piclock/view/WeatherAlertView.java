package net.piclock.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import net.piclock.enums.LabelEnums;
import net.piclock.main.Constants;
import net.piclock.swing.component.DragScrollListener;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;
import net.piclock.thread.ScreenAutoClose;
import net.weather.bean.WeatherAlert;
import net.weather.bean.WeatherGenericModel;
import java.awt.Dimension;

public class WeatherAlertView extends JPanel implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	
	public static String goingBackPage = "";
	private static final Logger logger = Logger.getLogger( WeatherAlertView.class.getName() );
	private SwingContext ct = SwingContext.getInstance();
	
	private JLabel lblWeatherAlert;

	/**
	 * Create the panel.
	 */
	public WeatherAlertView() {
		setOpaque(false);
		setLayout(new BorderLayout(0, 0));
		
		ThemeHandler theme = (ThemeHandler)ct.getSharedObject(Constants.THEMES_HANDLER);
		
		ct.addPropertyChangeListener(Constants.FORECAST_RESULT, this);
		ct.addPropertyChangeListener(Constants.FORECAST_DISPLAY_ERROR, this);	
		
		goingBackPage = Constants.MAIN_VIEW;
		
		JPanel waHeader = new JPanel();
		waHeader.setOpaque(false);
		add(waHeader, BorderLayout.NORTH);
		waHeader.setLayout(new MigLayout("", "[grow][grow][grow]", "[center]"));
		
		JButton back = new JButton("<"); 
		back.setSize(new Dimension(20, 25));
		back.setFont(new Font("Tahoma", Font.BOLD, 18));
		back.setPreferredSize(new Dimension(20, 25));
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JPanel cardsPanel = (JPanel)ct.getSharedObject(Constants.CARD_PANEL);
				try {
					ScreenAutoClose.stop();
				} catch (InterruptedException e1) {
					logger.log(Level.SEVERE,"Error while trying to stop auto close screen", e1);
				}
				
				if (!goingBackPage.equals(Constants.MAIN_VIEW)){
					ScreenAutoClose.start(cardsPanel, 45, TimeUnit.SECONDS);
				}						
				
				CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
				cardLayout.show(cardsPanel, goingBackPage);				
				
			}
		});

		waHeader.add(back, "cell 0 0,alignx left");
		
		JLabel lblTitle = new JLabel("Weather Alert");
		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 35));
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		waHeader.add(lblTitle, "cell 1 0,alignx center");
		
		theme.registerLabelTextColor(lblTitle, LabelEnums.WC_ALERT_TITLE);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		JPanel contentPanel = new JPanel();
		DragScrollListener dl = new DragScrollListener(contentPanel);
		contentPanel.setOpaque(false);
		add(scrollPane, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[grow]", "[grow]"));
		
		scrollPane.setViewportView(contentPanel);
		scrollPane.getViewport().setOpaque(false);
		
		
		lblWeatherAlert = new JLabel("");
		lblWeatherAlert.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblWeatherAlert.setVerticalAlignment(SwingConstants.TOP);
		lblWeatherAlert.setHorizontalAlignment(SwingConstants.LEFT);
		contentPanel.add(lblWeatherAlert, "cell 0 0,aligny top");
		
		theme.registerLabelTextColor(lblWeatherAlert, LabelEnums.WC_ALERT_TEXT);
		
		lblWeatherAlert.addMouseListener(dl);
		lblWeatherAlert.addMouseMotionListener(dl);
		
		contentPanel.addMouseListener(dl);
		contentPanel.addMouseMotionListener(dl);
		

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		logger.config("WeatherAlertView::event: " + evt.getPropertyName());

		if(evt.getPropertyName().equals(Constants.FORECAST_RESULT)){
			
			WeatherGenericModel wgm = (WeatherGenericModel)evt.getNewValue();
			
			if (wgm.getWeatherAlert() != null){
				WeatherAlert wa = wgm.getWeatherAlert();
	//display weather alert
				lblWeatherAlert.setText("<html> <div style='width: 450px;word-wrap: break-word;' >" + wa.getMessage() + "</div></html>");
			}
			
		}else if(evt.getPropertyName().equals(Constants.FORECAST_DISPLAY_ERROR)){
			
		}
		
	}

}