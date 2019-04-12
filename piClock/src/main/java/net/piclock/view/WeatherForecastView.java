package net.piclock.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;

import net.miginfocom.swing.MigLayout;
import net.piclock.main.Constants;
import net.piclock.swing.component.DragScrollListener;
import net.piclock.swing.component.SwingContext;
import net.piclock.thread.ScreenAutoClose;
import net.piclock.util.ImageUtils;
import net.weather.bean.Message;
import net.weather.bean.WeatherForecastModel;
import net.weather.bean.WeatherGenericModel;
import net.weather.utils.MessageHandl;
import java.awt.Dimension;

public class WeatherForecastView extends JPanel implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger( WeatherForecastView.class.getName() );
	
	private JLabel lblWeatheralert;
	private int totalOcc = 14;
	private JLabel daysLbl[] = new JLabel[totalOcc];
	private JLabel iconsLbl[] = new JLabel[totalOcc];
	private JLabel forecastLbl[] = new JLabel[totalOcc];
	private JSeparator separator[] = new JSeparator[totalOcc];
	
	private JButton back;
	//rows
//	private String rowSpaces = "200px";
	private String lableForecastSize = "420px";
	
	private SwingContext ct = SwingContext.getInstance(); 	
	
	/**
	 * Create the frame.
	 */
	public WeatherForecastView(final JPanel cardsPanel) {
		
		ct.addPropertyChangeListener(Constants.FORECAST_RESULT, this);
		ct.addPropertyChangeListener(Constants.FORECAST_DISPLAY_ERROR, this);		
		
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		scrollPane.setViewportView(panel);
		scrollPane.getViewport().setOpaque(false);
		panel.setOpaque(false);
		
//		StringBuilder layoutSize = new StringBuilder();
//		for(int s = 0 ; s < totalOcc ; s++){
//			layoutSize.append("["+rowSpaces+"][]");
//		}
		
		MigLayout layout = new MigLayout("hidemode 3", "[40px,center][70px][grow]", "[15px,center]");
		
		panel.setLayout(layout);
		
		DragScrollListener dl = new DragScrollListener(panel);
		back =  new JButton("<");	
		back.setAlignmentX(Component.CENTER_ALIGNMENT);
		back.setFont(new Font("Tahoma", Font.BOLD, 18));
		back.setPreferredSize(new Dimension(20, 25));
		
//		back.setSize(40, 25);
		back.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					ScreenAutoClose.stop();
				} catch (InterruptedException e1) {
					logger.log(Level.SEVERE,"Error while trying to stop auto close screen", e1);
				}
				
				CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
				cardLayout.show(cardsPanel, Constants.MAIN_VIEW);		
				
			}
		});
		panel.add(back, "hidemode 3,cell 0 0,alignx left,aligny top");
		
		lblWeatheralert = new JLabel("   !! Weather Alert !!");
		lblWeatheralert.setHorizontalAlignment(SwingConstants.LEFT);
		lblWeatheralert.setForeground(Color.RED);
		lblWeatheralert.setFont(new Font("Tahoma", Font.BOLD, 18));
		panel.add(lblWeatheralert, "cell 2 0");
		
		lblWeatheralert.setVisible(false);
		
		lblWeatheralert.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				
				try {
					ScreenAutoClose.stop();
					ScreenAutoClose.start(cardsPanel, 45, TimeUnit.SECONDS);
				} catch (InterruptedException e1) {
					logger.log(Level.SEVERE,"Error while trying to stop auto close screen", e1);
				}				
				
				WeatherAlertView.goingBackPage = Constants.WEATHER_FORECAST_VIEW;
				
				CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
				cardLayout.show(cardsPanel, Constants.WEATHER_ALERT_VIEW);
			}		
		});
	
		boolean addForecastRow= true;
		int index = -1;
		for(int i = 1;i< (totalOcc * 2) + 1 ;i++){		
			
			if (addForecastRow){
				
				index ++;
				
				daysLbl[index]= new JLabel();
				daysLbl[index].setFont(new Font("Tahoma", Font.BOLD, 20));
				
				iconsLbl[index] = new JLabel();						
				forecastLbl[index] = new JLabel();
				forecastLbl[index].setAlignmentX(Component.CENTER_ALIGNMENT);
				forecastLbl[index].setFont(new Font("Tahoma", Font.BOLD, 20));
				
				panel.add(daysLbl[index], "hidemode 3,cell 0 "+i+",alignx left,aligny center");
				panel.add(iconsLbl[index], "hidemode 3,cell 1  "+i+",alignx center");
				panel.add(forecastLbl[index], "hidemode 3,cell 2  "+i+",grow");
				
				forecastLbl[index].addMouseListener(dl);
				forecastLbl[index].addMouseMotionListener(dl);
				
				daysLbl[index].setVisible(false);
				iconsLbl[index].setVisible(false);
				forecastLbl[index].setVisible(false);	
				
				addForecastRow = false;
			}else{
				//add seperator
				separator[index] = new JSeparator();
				panel.add(separator[index], "cell 0 "+i+" 3 1,grow");
				separator[index].setVisible(false);
				 addForecastRow = true;
			}
		}				 
		panel.addMouseListener(dl);
		panel.addMouseMotionListener(dl);
		
		setOpaque(false);		
	}
	
	public void updateForecast(int pos, String forecast, ImageIcon icon, String dayOfWeek){
		daysLbl[pos].setText("<html><div style='width: 35px;word-wrap: break-word;'>"+dayOfWeek+"</html>");
		daysLbl[pos].setVisible(true);
		forecastLbl[pos].setText("<html><div style='width: "+ lableForecastSize + ";word-wrap: break-word;'>"+forecast+"</html>");
		forecastLbl[pos].setVisible(true);
		iconsLbl[pos].setIcon(icon);
		iconsLbl[pos].setVisible(true);
		separator[pos].setVisible(true);		
	}	
	
	public void displayLoading(){
		hideLabels();
		daysLbl[0].setText("");
		daysLbl[0].setVisible(true);
		forecastLbl[0].setText("LOADIND Forecast..");
		forecastLbl[0].setVisible(true);
		iconsLbl[0].setIcon(ImageUtils.getInstance().getWeatherLoader());
		iconsLbl[0].setVisible(true);
	}
	
	private void hideLabels(){
		for(int i = 1;i< totalOcc ;i++){	
			daysLbl[i].setVisible(false);
			forecastLbl[i].setVisible(false);
			daysLbl[i].setVisible(false);
			separator[i].setVisible(false);
			iconsLbl[i].setVisible(false);
		}
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		logger.config("event reciecved: " + evt.getPropertyName());

		if(evt.getPropertyName().equals(Constants.FORECAST_RESULT)){

			WeatherGenericModel wgm = (WeatherGenericModel)evt.getNewValue();
			
			if (wgm.getWForecastModel() != null && wgm.getWForecastModel().size() > 0){
				ImageUtils img = ImageUtils.getInstance();
				for(int i = 0 ; i < wgm.getWForecastModel().size() ; i++){
					WeatherForecastModel wfm = wgm.getWForecastModel().get(i);
					try {
						System.out.println("wfm.getIconName() "+ wfm.getIconName());
						updateForecast(i, wfm.getForecast(), img.getImage("weather" + File.separatorChar + wfm.getIconName()), wfm.getDayOfWeek());
					} catch (IOException e) {
						updateForecast(i, wfm.getForecast(),img.getWeatherNA(), wfm.getDayOfWeek());
						logger.log(Level.SEVERE, "WeatherIcon", e);
					}
				}
			}
			
			if (wgm.getWeatherAlert() != null){
				lblWeatheralert.setVisible(true);
			}			
		}else if(evt.getPropertyName().equals(Constants.FORECAST_DISPLAY_ERROR)){
			WeatherGenericModel wgm = (WeatherGenericModel)evt.getNewValue();
			
			MessageHandl msg =  wgm.getMessages();
			
			Message m = msg.getAllMessages().get( msg.getAllMessages().size() - 1);			
			
			hideLabels();
			daysLbl[0].setText("");
			daysLbl[0].setVisible(true);
			forecastLbl[0].setText(m.getTitle());
			forecastLbl[0].setVisible(true);
			iconsLbl[0].setIcon(ImageUtils.getInstance().getWeatherNA());
			iconsLbl[0].setVisible(true);
		}
	}	
}
