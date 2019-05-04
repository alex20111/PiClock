package net.piclock.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nanohttpd.protocols.http.response.Response;

import home.miniHttp.HttpBase;
import home.miniHttp.HttpHandler;
import home.miniHttp.StaticPageHandler;

public class MainPage extends HttpBase implements HttpHandler{
	private  String MAIN_PAGE = "dashboard";
	
	@Override 
	public Response handleRequest() {
	
		String webPage = "Page not found";//default message

		//create main page with values
		List<File> webPageFiles = getWebPageOnDisk(MAIN_PAGE);
		
		if (webPageFiles != null && webPageFiles.size() > 0){
			Map<String, String> values = new HashMap<String, String>();
			
			try {
				webPage =	StaticPageHandler.processPage(webPageFiles, values);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			webPage = "Web page not found";
		}	
		
		System.out.println("------------end---------------");
		return Response.newFixedLengthResponse(webPage);
	}
	@Override
	public void handleParameters(Map<String, List<String>> params) {	
		
	}
}
