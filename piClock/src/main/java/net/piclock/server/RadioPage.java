package net.piclock.server;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.NanoHTTPD.ResponseException;
import org.nanohttpd.protocols.http.response.Response;

import home.miniHttp.HttpBase;
import home.miniHttp.StaticPageHandler;
import home.miniHttp.Table;
import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.db.entity.RadioEntity;
import net.piclock.db.sql.RadioSql;
import net.piclock.main.Constants;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.FormatStackTrace;

public class RadioPage extends HttpBase {
	private static final Logger logger = Logger.getLogger( RadioPage.class.getName() );
	
	private String pageName = "radioView"; //name from xml to build the page
	private RadioSql sql = new RadioSql();
	
	private Optional<String> deleteRadio = Optional.empty();
	private Optional<String> radioName = Optional.empty();
	private Optional<String> radioLink = Optional.empty();
	
	private Optional<String> selrad    		  = Optional.empty();
	private Optional<String> selradChecked    = Optional.empty();
	private int lastSelectedValue = 0;
	
	
	private String errorMessages = "";

	@Override 
	public Response handleRequest() {

		errorMessages = "";
		
		String webPage = "Page not found";//default message
		try {

			System.out.println("selrad: " + selrad + " (Boolean)selradChecked.get(): " + selradChecked);			
			
			if(selrad.isPresent()){ //user click on the checkbox and we have a value of empty or number

				RadioEntity r = sql.loadRadioById(Integer.parseInt(selrad.get()));
				if (selradChecked.isPresent() && Boolean.valueOf(selradChecked.get()).booleanValue()){
					System.out.println("checked");
					r.setLastSelection(true);
				}else{
					r.setLastSelection(false);
				}
				sql.update(r);
				System.out.println("radio updated");
				return Response.newFixedLengthResponse("Success");
				
			}else if (radioName.isPresent() && radioLink.isPresent() || deleteRadio.isPresent() ){

				if (deleteRadio.isPresent()){				
					sql.delete(Integer.parseInt(deleteRadio.get()));

				}else if (validLink()){

					//validate params				
					RadioEntity rad = new RadioEntity();
					rad.setRadioName(radioName.get());
					rad.setRadioLink(radioLink.get());
					sql.add(rad);
					
//					//add it to mpc also, but verify if it exist 1st
//					if(!linkExist(radioLink.get())) {
//						Exec exec = new Exec();
//						exec.addCommand("mpc").addCommand("add").addCommand(radioLink.get()).timeout(10000);
//
//						 exec.run();
//					}
					

				}else{
					errorMessages = generateErrorMessage("Please add a valid link that starts with http:// or https:// ");
				}
			}

			//1st see if we have any tables
			List<RadioEntity> re = sql.loadAllRadios();

			//create main page with values
			List<File> webPageFiles = getWebPageOnDisk(pageName);

			Map<String, String> values = new HashMap<String, String>();
			values.put("table", buildRadioTable(re)); //key in the html page is : %-valuel-%
			values.put("radioFormError", errorMessages);
			values.put("lastid", String.valueOf(lastSelectedValue));
//			values.put("customScript", generateCustomJsScript());

			webPage =	StaticPageHandler.processPage(webPageFiles, values);

		} catch (IOException | ResponseException | ClassNotFoundException | SQLException e) {
			webPage = "ERROR";
			ErrorHandler eh = (ErrorHandler)SwingContext.getInstance().getSharedObject(Constants.ERROR_HANDLER);
			eh.addError(ErrorType.WEB_SERVER, new ErrorInfo(new FormatStackTrace(e).getFormattedException()));
			logger.log(Level.SEVERE, "Error in RadioPage", e);
		}
		return Response.newFixedLengthResponse(webPage);
	}

	public String getPageName(){
		return this.pageName;
	}
	private String buildRadioTable(List<RadioEntity> re) throws IOException, ResponseException{

		try{
			Table t = new Table(4);

			t.addHeader("Radio name");
			t.addHeader("Radio link");
			t.addHeader("Selected Station");
			t.addHeader("Delete");

			t.addTableClass("table");

			if (re.size() > 0){
				
				boolean anyPreSelected = false;
				for(RadioEntity r : re){
					if (r.isLastSelection()){
						anyPreSelected = true;
						break;
					}
				}				
				
				for(RadioEntity r : re){
					t.addTableBody(r.getRadioName());
					t.addTableBody(r.getRadioLink());	
					
					if (r.isLastSelection()){
						lastSelectedValue = r.getId();
						t.addTableBody("<input type=\"checkbox\" name=\"selrad\" value=\""+r.getId() +"\" checked class=\"selChkbox\" > ");
					}else{
						t.addTableBody("<input type=\"checkbox\" name=\"selrad\" value=\""+r.getId() +"\"  " + ( anyPreSelected ?  "disabled=\"disabled\"" : "" ) + "  class=\"selChkbox\" > ");
					}
										
					t.addTableBody("<a href=\"/radio?deleteRadio="+r.getId() + "\" > del </a>");
				}
			}else{
				t.addTableBody("No Radio stations");
			}				
			t.addTheaderClass("thead-dark");

			return t.build();

		}catch(Exception ex){
			ErrorHandler eh = (ErrorHandler)SwingContext.getInstance().getSharedObject(Constants.ERROR_HANDLER);
			eh.addError(ErrorType.WEB_SERVER, new ErrorInfo(new FormatStackTrace(ex).getFormattedException()));
			logger.log(Level.SEVERE, "Error while creating table" , ex);
		}
		return "Error";
	}

	@Override
	public void handleParameters(Map<String, List<String>> params) {
		System.out.println(params);		
		
		deleteRadio = Optional.empty();
		radioName = Optional.empty();
		radioLink = Optional.empty();
		selrad    = Optional.empty();
		selradChecked  = Optional.empty();
		
		if (params.get("deleteRadio") != null && params.get("deleteRadio").size() > 0){
			deleteRadio =  Optional.ofNullable(params.get("deleteRadio").get(0));
		}
		if (params.get("radioName") != null && params.get("radioName").size() > 0){
			radioName = Optional.ofNullable(params.get("radioName").get(0));
		}
		if (params.get("radioLink") != null && params.get("radioLink").size() > 0){
			radioLink =  Optional.ofNullable(params.get("radioLink").get(0));
		}
		if (params.get("selrad") != null && params.get("selrad").size() > 0){
			selrad =  Optional.ofNullable(params.get("selrad").get(0));
		}
		if (params.get("selradChecked") != null && params.get("selradChecked").size() > 0){
			selradChecked =  Optional.ofNullable(params.get("selradChecked").get(0));
		}	
	}
	private boolean validLink(){
		if (radioLink.isPresent() &&
				(radioLink.get().startsWith("http://") || radioLink.get().startsWith("https://")) ){
			return true;
		}
		return false;
	}


}