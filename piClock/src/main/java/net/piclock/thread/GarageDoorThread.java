package net.piclock.thread;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;

import home.websocket.WebSocketClientEndPoint;
import net.piclock.enums.IconEnum;
import net.piclock.handlers.PiHandler;
import net.piclock.main.Constants;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;

/*
 * Class that monitor web socket to update the garage door icon
 */
public class GarageDoorThread implements Runnable{

	private static final Logger logger = Logger.getLogger( GarageDoorThread.class.getName() );

	private String host = "def";
	private JLabel lblGaragedoor;

	public GarageDoorThread(JLabel lblGaragedoor) throws URISyntaxException, UnknownHostException {

		host = InetAddress.getLocalHost().getHostName();

		logger.log(Level.CONFIG, "---------> Host name: " + host);

		this.lblGaragedoor = lblGaragedoor;
	}

	@Override
	public void run() {
		try {
			logger.log(Level.CONFIG, "---------> Starting garage door: " + host);
			//register user
			final WebSocketClientEndPoint clientEndPoint  = new WebSocketClientEndPoint(new URI("ws://192.168.1.110:8081/events/"), 7200000);
			clientEndPoint.sendMessage("{'operation': 1, 'userName':'"+host+"' }");
			PiHandler handler = PiHandler.getInstance();

			// add listener
			clientEndPoint.addMessageHandler(new WebSocketClientEndPoint.MessageHandler() {
				public void handleMessage(String message) {
					logger.log(Level.CONFIG, message);
					if (message.contains("garage")) {
						String trimmedMessage = message.trim();
						String value = trimmedMessage.substring(trimmedMessage.indexOf(":"), trimmedMessage.length() - 1).trim();
						logger.log(Level.CONFIG, "GARAGE DOOOR STATUS: " + value);
						//					System.out.println("Mess: " + value);

						try {
							ThemeHandler tm = (ThemeHandler)SwingContext.getInstance().getSharedObject(Constants.THEMES_HANDLER);
							if ("1".equalsIgnoreCase(value)) {
								lblGaragedoor.setIcon(tm.getIcon(IconEnum.GARAGE_OPEN));
								tm.registerIconColor(lblGaragedoor, IconEnum.GARAGE_OPEN);
							}else {
								lblGaragedoor.setIcon(tm.getIcon(IconEnum.GARAGE_CLOSED));
								tm.registerIconColor(lblGaragedoor, IconEnum.GARAGE_CLOSED);
							}

						}catch(IOException e) {
							logger.log(Level.SEVERE, "Error swithching icon",e);
						}
					}
				}
			});


			while (!Thread.currentThread().isInterrupted()) {
				try {
					if (handler.isWifiConnected()) {
						//send heartBeep
						clientEndPoint.sendMessage("{'operation': 20 }");
					}
					Thread.sleep(3600000); //3600000
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}


		}catch(Exception ex) {
			logger.log(Level.SEVERE, "Error in WS", ex);
		}

	}
}
