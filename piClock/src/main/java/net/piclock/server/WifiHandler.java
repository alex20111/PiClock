package net.piclock.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nanohttpd.protocols.http.response.Response;

import home.miniHttp.HttpBase;
import home.miniHttp.StaticPageHandler;

public class WifiHandler extends HttpBase{

	private String pageName = "wifiView";
	private String wifiName = "";
	private String wifiPassword = "";

	@Override
	public Response handleRequest() {
		String webPage = "Error";
		try{
			String message = "";
			//create main page with values
			if (wifiName != null && !wifiName.equals("Select") && wifiPassword != null && wifiPassword.length() > 0){
				message = generateSuccessMessage("Success, Connected");
			}

			List<File> webPageFiles = getWebPageOnDisk(pageName);

			//load wifis
			List<String> wifi = new ArrayList<String>();
			wifi.add("Select");
			wifi.add("DolMat");
			wifi.add("ppout");
			wifi.add("bob");

			Map<String, String> values = new HashMap<String, String>();
			values.put("wifilist", buildSelect(wifi)); //key in the html page is : %-valuel-%
			values.put("message1",message);

			webPage =	StaticPageHandler.processPage(webPageFiles, values);

		}catch (Exception ex){
			ex.printStackTrace();
		}

		return Response.newFixedLengthResponse(webPage);
	}

	public String buildSelect(List<String> wifiNames){

		String currWifi = "bob";

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

		if (params.get("wifiName") != null && params.get("wifiName").size() > 0){
			wifiName = params.get("wifiName").get(0);
		}
		if (params.get("wifiPassword") != null && params.get("wifiPassword").size() > 0){
			wifiPassword = params.get("wifiPassword").get(0);
		}
	}
}