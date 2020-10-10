package net.piclock.thread;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;

import home.common.data.Service;
import home.websocket.WebSocketClientEndPoint;
import home.websocket.WebSocketException;
import net.piclock.enums.CheckWifiStatus;
import net.piclock.enums.IconEnum;
import net.piclock.handlers.PiHandler;
import net.piclock.main.Constants;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;

/*
 * Class that monitor web socket to update the garage door icon
 */
public class GarageDoorThread implements Runnable, PropertyChangeListener{

	private static final Logger logger = Logger.getLogger( GarageDoorThread.class.getName() );

	private String host = "def";
	private JLabel lblGaragedoor;
	private  WebSocketClientEndPoint clientEndPoint;
	private boolean iconVisible = false;

	public GarageDoorThread(JLabel lblGaragedoor) throws URISyntaxException, UnknownHostException {

		host = InetAddress.getLocalHost().getHostName();

		logger.log(Level.CONFIG, "---------> Host name: " + host);

		this.lblGaragedoor = lblGaragedoor;

		//		SwingContext.getInstance().getect(Constants.CHECK_INTERNET, CheckWifiStatus.END_WIFI_OFF);
		SwingContext.getInstance().addPropertyChangeListener(Constants.CHECK_INTERNET, this);
	}

	@Override
	public void run() {
		try {
			logger.log(Level.CONFIG, "---------> Starting garage door: " + host);
			//register user
			clientEndPoint = new WebSocketClientEndPoint(new URI("ws://192.168.1.110:8081/events/"), 64800000);  //18 hours

			PiHandler handler = PiHandler.getInstance();

			// add listener
			clientEndPoint.addMessageHandler(new WebSocketClientEndPoint.MessageHandler() {
				public void handleMessage(String message) {
					logger.log(Level.CONFIG, message);
					if (message.contains("garage")) {
						String trimmedMessage = message.trim();
						String value = trimmedMessage.substring(trimmedMessage.indexOf(":") + 1, trimmedMessage.length() - 1).trim();
						logger.log(Level.CONFIG, "GARAGE DOOOR STATUS: " + value + "  Icon visible: " + iconVisible);

						try {
							int garageValue = Integer.parseInt(value);

							//check is we need to set the label visible.
							if (garageValue != -1) {
								logger.log(Level.CONFIG, "Setting visible garage door icon" );
								lblGaragedoor.setVisible(true);
								iconVisible = true;
							}

							ThemeHandler tm = (ThemeHandler)SwingContext.getInstance().getSharedObject(Constants.THEMES_HANDLER);
							if (garageValue == 0) {
								logger.log(Level.CONFIG, "GARAGE open");
								lblGaragedoor.setIcon(tm.getIcon(IconEnum.GARAGE_OPEN));
								tm.registerIconColor(lblGaragedoor, IconEnum.GARAGE_OPEN);

							}else if (garageValue == 1) {
								logger.log(Level.CONFIG, "GARAGE Closed");
								lblGaragedoor.setIcon(tm.getIcon(IconEnum.GARAGE_CLOSED));
								tm.registerIconColor(lblGaragedoor, IconEnum.GARAGE_CLOSED);
							}else if (garageValue == -1) {
								//hide icon
								logger.log(Level.CONFIG, "Hiding garage door icon" );
								lblGaragedoor.setVisible(false);
								iconVisible = false;
							}

						}catch(IOException e) {
							logger.log(Level.SEVERE, "Error swithching icon",e);
						}
					}
				}
			});

//			connectToWs();

			while (!Thread.currentThread().isInterrupted()) {
				try {
					boolean wifiOn = handler.isWifiConnected();
					logger.log(Level.CONFIG, "Sending heart Beat. Wifi on?  " +  wifiOn);
					if (wifiOn && clientEndPoint.isConnectionAlive()) {
						//send heartBeep
						clientEndPoint.sendMessage("{'operation': 20 }"); //TODO getting null pointer exception... in Utilities, verify if not null beofre sending..  Other // re connect if disconnected by server
					}else if (wifiOn && ! clientEndPoint.isConnectionAlive()) {
						//reconnect
						logger.log(Level.CONFIG, "Reconnecting to websocket endpoint. ");
						connectToWs();
					}
					Thread.sleep(3600000); //3600000  == 1 hour
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				} 
			}


		}catch(Exception ex) {
			logger.log(Level.SEVERE, "Error in WS", ex);
		}

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		logger.log(Level.CONFIG, "Event : " + evt.getPropertyName() + " Value: " + evt.getNewValue());
		if (Constants.CHECK_INTERNET.equals(evt.getPropertyName())){
			//check if wifi is turning off? 

			CheckWifiStatus status = (CheckWifiStatus) evt.getNewValue();

			try {
				if (status.isOff()) {
					clientEndPoint.closeConnection();
				}else if (status.isConnected()){
					connectToWs();
				}
			}catch (Exception e) {
				logger.log(Level.SEVERE, "Error in websocket: " , e);
			}

		}

	}

	private void connectToWs() throws WebSocketException {
		//connect
		clientEndPoint.connect();

		//identify to web socket
		clientEndPoint.sendMessage("{'operation': 1, 'userName':'"+host+"', 'service':'" + Service.GARAGE_NOTIFICATION + "' }");

		//get last garage status
		clientEndPoint.sendMessage("{'operation': 3 }");
		
		logger.log(Level.CONFIG, "Connection ended");

	}
}
