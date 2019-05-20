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
import net.piclock.enums.CheckWifiStatus;
import net.piclock.main.Constants;
import net.piclock.main.PiHandler;
import net.piclock.main.Preferences;
import net.piclock.swing.component.SwingContext;

public class WifiHandler extends HttpBase implements PropertyChangeListener{
	
	private static final Logger logger = Logger.getLogger( WifiHandler.class.getName() );
	
	private final String CONNECTED = "connected";
	private final String PING = "ping";
	private final String ERROR = "error";
	
	private SwingContext ct;
	private PiHandler piHandler;
	
	private String pageName = "wifiView";
	private String wifiName = "";
	private String wifiPassword = "";
	
	private Optional<String> check = Optional.empty();
	
	String currWifi = "";

	private CheckWifiStatus connStatus;

	public WifiHandler() {
		ct = SwingContext.getInstance();
		ct.addPropertyChangeListener(Constants.CHECK_INTERNET, this);
		piHandler = PiHandler.getInstance();
	}
	
	@Override
	public Response handleRequest() {
		
		String webPage = "Error";
		try{
			if (piHandler.isWifiConnected()) {
				Preferences p = (Preferences)ct.getSharedObject(Constants.PREFERENCES);
				currWifi = p.getWifi();
			}						
			
			String message = "";
			
			if (check.isPresent() && check.get().equalsIgnoreCase("check")) {
				return Response.newFixedLengthResponse(checkIfConnected());
			}
			else if (wifiName != null && !wifiName.equals("Select") && wifiPassword != null && wifiPassword.length() > 0){
				//connect to wifi
				piHandler.connectToWifi(wifiName, wifiPassword);
				return Response.newFixedLengthResponse(checkIfConnected());//message = generateSuccessMessage("Success, Connected");
			}

			List<File> webPageFiles = getWebPageOnDisk(pageName);

			//load wifis
			List<String> wifi = piHandler.fetchWifiList();
			
			Map<String, String> values = new HashMap<String, String>();
			values.put("wifilist", buildSelect(wifi)); //key in the html page is : %-valuel-%
			values.put("message1",message);

			webPage =	StaticPageHandler.processPage(webPageFiles, values);

		}catch (Exception ex){
			logger.log(Level.SEVERE, "error in WiFiHandler", ex);
			
		}

		return Response.newFixedLengthResponse(webPage);
	}

	public String buildSelect(List<String> wifiNames){

		
		StringBuilder wifi = new StringBuilder();
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

		if (params.get("wifiName") != null && params.get("wifiName").size() > 0){
			wifiName = params.get("wifiName").get(0);
		}
		if (params.get("wifiPassword") != null && params.get("wifiPassword").size() > 0){
			wifiPassword = params.get("wifiPassword").get(0);
		}
		if (params.get("check") != null && params.get("check").size() > 0){
			check = Optional.ofNullable(params.get("check").get(0));
		}
	}

	private String checkIfConnected() {
		if (connStatus == CheckWifiStatus.STARTING) {
			return PING;
		}else if(connStatus.isConnected()) {
			return CONNECTED;
		}else {
			return ERROR;
		}
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		connStatus = (CheckWifiStatus)evt.getSource();
		
	}
}