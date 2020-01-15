package net.piclock.server;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.response.Response;

import home.miniHttp.HttpBase;
import home.miniHttp.StaticPageHandler;
import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.enums.CheckWifiStatus;
import net.piclock.handlers.PiHandler;
import net.piclock.main.Constants;
import net.piclock.main.Preferences;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.FormatStackTrace;
import net.piclock.util.PreferencesHandler;

public class WifiHandler extends HttpBase implements PropertyChangeListener{
	
	private static final Logger logger = Logger.getLogger( WifiHandler.class.getName() );
	
	private final String CONNECTED = "connected";
	private final String PING = "ping";
	private final String DISCONNECT = "disconnect";
	private final String ERROR = "error";
	
	private SwingContext ct;
	private PiHandler piHandler;
	
	private String pageName = "wifiView";
	private String wifiName = "";
	private String wifiPassword = "";
	private Optional<String> btnDisconnect = Optional.empty(); 
 	
	private Optional<String> check = Optional.empty();
	
	String currWifi = "";
	String passw = "";

	private CheckWifiStatus connStatus = CheckWifiStatus.STARTING;

	public WifiHandler() {
		ct = SwingContext.getInstance();
		ct.addPropertyChangeListener(Constants.CHECK_INTERNET, this);
		piHandler = PiHandler.getInstance();
	}
	
	@Override
	public Response handleRequest() {

		String webPage = "Error";
		try{									

			String message = "";

//			System.out.println("values !!!!!!!!!!! > " + wifiName + " pa: " + wifiPassword + " check: " + check + " connstat: " + connStatus);
			if(btnDisconnect.isPresent()) {
				wifiDisconnect();
				message = generateSuccessMessage("Successfully disconnecte.\n You won't be able to reconnect since you are disconnected from the WIFI");
				
			}
			else if (check.isPresent() && check.get().equalsIgnoreCase("check")) {
				return Response.newFixedLengthResponse(checkIfConnected());
			}
			else if (wifiName != null && !wifiName.equals("Select") && wifiPassword != null && wifiPassword.length() > 0){
				//connect to wifi
				piHandler.connectToWifi(wifiName, wifiPassword);
				String conn = checkIfConnected();
				logger.log(Level.CONFIG, "---------------------------------- returning: " + conn);
				return Response.newFixedLengthResponse(conn);//message = generateSuccessMessage("Success, Connected");
			}else if (piHandler.isWifiConnected()) {
				Preferences p = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
				currWifi = p.getWifi();
				passw = p.getWifiPass();
			}

			List<File> webPageFiles = getWebPageOnDisk(pageName);

			//load wifis
			List<String> wifi = piHandler.fetchWifiList();

			Map<String, String> values = new HashMap<String, String>();
			values.put("wifilist", buildSelect(wifi)); //key in the html page is : %-valuel-%
			values.put("message1",message);
			values.put("wifiPass", passw);

			webPage =	StaticPageHandler.processPage(webPageFiles, values);

		}catch (Exception ex){
			ErrorHandler eh = (ErrorHandler)SwingContext.getInstance().getSharedObject(Constants.ERROR_HANDLER);
			eh.addError(ErrorType.WEB_SERVER, new ErrorInfo(new FormatStackTrace(ex).getFormattedException()));
			logger.log(Level.SEVERE, "error in WiFiHandler", ex);			
		}

		return Response.newFixedLengthResponse(webPage);
	}

	public String buildSelect(List<String> wifiNames){
		
		StringBuilder wifi = new StringBuilder();
		wifi.append("<option value=\"Select\">Select</option> ");
		for(String s : wifiNames){
			wifi.append("<option" + (s.equalsIgnoreCase(currWifi) ? " selected " : " ") + " value=\"" + s + "\">" + s + "</option>");
		}
		return wifi.toString();
	}

	public String getPageName() {
		return pageName;
	}
	public void setPageName(String pageName) {
		this.pageName = pageName;
	}
	@Override
	public void handleParameters(Map<String, List<String>> params) {
		wifiName = "";
		wifiPassword = "";
		check = Optional.empty();
		btnDisconnect = Optional.empty();

		if (params.get("wifiName") != null && params.get("wifiName").size() > 0){
			wifiName = params.get("wifiName").get(0);
		}
		if (params.get("wifiPassword") != null && params.get("wifiPassword").size() > 0){
			wifiPassword = params.get("wifiPassword").get(0);
		}
		if (params.get("check") != null && params.get("check").size() > 0){
			check = Optional.ofNullable(params.get("check").get(0));
		}
		if (params.get("btnDisconnect") != null && params.get("btnDisconnect").size() > 0){
			btnDisconnect = Optional.ofNullable(params.get("btnDisconnect").get(0));
		}
	}

	private String checkIfConnected() {
		if (connStatus == CheckWifiStatus.STARTING) {
			return PING;
		}else if(connStatus.isConnected()) {
			return CONNECTED;
		}else if(connStatus == CheckWifiStatus.END_DISCONNECT) {
			return DISCONNECT;
		}else {
			return ERROR;
		}
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		connStatus = (CheckWifiStatus)evt.getNewValue();
//		System.out.println("values ------->>>>>> " + connStatus);
	}
	
	private void wifiDisconnect() {
		//start a new thread to be able to send a message before disconnecting.
		new Thread(() ->  {
			try {
				Thread.sleep(3000);
				piHandler.disconnectWifi();
				Preferences p = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
				p.setWifiPass("");
				PreferencesHandler.save(p);
				logger.log(Level.CONFIG, "WIFI disconnected");
			}catch(Exception ex) {
				logger.log(Level.SEVERE, "Could not disconnect", ex);
			}
		}).start();
	}
}