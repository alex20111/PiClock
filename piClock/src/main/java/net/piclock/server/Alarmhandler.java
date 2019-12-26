package net.piclock.server;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nanohttpd.protocols.http.response.Response;

import home.miniHttp.HttpBase;
import home.miniHttp.HttpHandler;
import home.miniHttp.StaticPageHandler;
import net.piclock.db.entity.AlarmEntity;
import net.piclock.db.sql.AlarmSql;
import net.piclock.enums.AlarmRepeat;
import net.piclock.enums.Buzzer;

public class Alarmhandler extends HttpBase implements HttpHandler{
	private  String ALARM_PAGE = "alarmView";
	
	private final String TIME24HOURS_PATTERN =  "([01]?[0-9]|2[0-3]):[0-5][0-9]";
	private final String SAVED_VIEWING_ALARM = "CurrentViewingAlarm";  //alex


	public AlarmEntity alarm = new AlarmEntity();	
	
	//parameters 
	private Boolean active = null;
	private String timeInput = "";
	private boolean repeatSun = false;
	private boolean repeatMon = false;
	private boolean repeatTue = false;
	private boolean repeatWed = false;
	private boolean repeatThu = false;
	private boolean repeatFri = false;
	private boolean repeatSat = false;
	
	//alarm btns
	private String btn1Alarm = ""; 
	private String btn2Alarm = ""; 
	private String btn3Alarm = ""; 
	private String btn4Alarm = ""; 
	private String btn5Alarm = ""; 
	
	private int buzzerType = -1;
	private int volume = -1;
	private String musicSelected = "";
	
	private int fetchBuzzerList = -1;
	
	private String btnSave = "";

	//message
	private String message = "";
	
	//script
	private String customJs = "";

	@Override 
	public Response handleRequest() {
		
		try{			
			loadAlarm(); 
			
			String webPage = "Page not found";//default message
			//empty message:
			message = "";
			
			if (active != null){ //toggle switch
							
				String status = "success";
				//update db
				if (alarm.getId() > 0){
					alarm.setActive(active);
					getSession().set(SAVED_VIEWING_ALARM, alarm);
				}else{
					status = "notSaved";
				}
				return Response.newFixedLengthResponse(status);
				
			}else if (fetchBuzzerList > -1){
				return Response.newFixedLengthResponse(buildMusicSelectionListSelect(fetchBuzzerList));
			}else if (!btnSave.isEmpty()){
				//validate time
				if (save()){
					message = generateSuccessMessage("Save successful");
				}else{
					message = generateErrorMessage(message);
				}
			}

			//create main page with values
			List<File> webPageFiles = getWebPageOnDisk(ALARM_PAGE);

			if (webPageFiles != null && webPageFiles.size() > 0){
				Map<String, String> values = new HashMap<String, String>();

				try {
					String showFunction = hideShowFunction();
					
					values.put("alarm", (alarm.getHour().isEmpty() ? "--" : alarm.getHour()) + ":" + (alarm.getMinutes().isEmpty() ? "--" : alarm.getMinutes()));
					values.put("alarmFormMessage", message);
					values.put("repeatDisplay", buildRepeatDisplay());
					values.put("alarmActive", buildIfActive());
					values.put("checkboxes", buildRepeat());
					values.put("timeInput", buildTimeInput());
					values.put("buzzOption", buildBuzzerTypeSelect());
					values.put("volumeDisplay", buildVolumeSelect());
					values.put("buzzerMusicSel", buildMusicSelectionListSelect(Buzzer.valueOf(alarm.getAlarmSound()).ordinal()));					
					values.put("hideShowMusic", showFunction);
					values.put("hideShowVolume", showFunction);
					values.put("buzzerdisplay", buildBuzzerDisplay());
					values.put("customScript", customJs);
					webPage =	StaticPageHandler.processPage(webPageFiles, values);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				webPage = "Web page not found";
			}	
			return Response.newFixedLengthResponse(webPage);
		}catch (Exception ex){
			ex.printStackTrace();
			return Response.newFixedLengthResponse("Unexpected error");
		}
	}
	@Override 
	public void handleParameters(Map<String, List<String>> params) { 

		fetchBuzzerList = -1;
		active = null;
		repeatSun = false;
		repeatMon = false;
		repeatTue = false;
		repeatWed = false;
		repeatThu = false;
		repeatFri = false;
		repeatSat = false; 
		btnSave = "";
		timeInput = "";
		volume = -1;
		buzzerType = -1;
		musicSelected = "";
		btn1Alarm = ""; //alex
		btn2Alarm = "";//alex
		btn3Alarm = "";//alex
		btn4Alarm = "";//alex
		btn5Alarm = "";//alex
		
		if (params.get("active") != null && params.get("active").size() > 0){ 
			active = Boolean.valueOf(params.get("active").get(0)); 			 			
		}	 
		if (params.get("repeatSun") != null && params.get("repeatSun").size() > 0){ 
			repeatSun = ( params.get("repeatSun").get(0).equals("on") ? true : false); 
		} 
		if (params.get("repeatMon") != null && params.get("repeatMon").size() > 0){ 
			repeatMon = ( params.get("repeatMon").get(0).equals("on") ? true : false);
		} 
		if (params.get("repeatTue") != null && params.get("repeatTue").size() > 0){ 
			repeatTue = ( params.get("repeatTue").get(0).equals("on") ? true : false);
		} 
		if (params.get("repeatWed") != null && params.get("repeatWed").size() > 0){ 
			repeatWed = ( params.get("repeatWed").get(0).equals("on") ? true : false);
		}
		if (params.get("repeatThu") != null && params.get("repeatThu").size() > 0){ 
			repeatThu = ( params.get("repeatThu").get(0).equals("on") ? true : false);
		} 
		if (params.get("repeatFri") != null && params.get("repeatFri").size() > 0){ 
			repeatFri = ( params.get("repeatFri").get(0).equals("on") ? true : false);
		} 
		if (params.get("repeatSat") != null && params.get("repeatSat").size() > 0){ 
			repeatSat = ( params.get("repeatSat").get(0).equals("on") ? true : false);
		} 
		if (params.get("btnSave") != null && params.get("btnSave").size() > 0){ 
			btnSave =  params.get("btnSave").get(0);
		} 
		if (params.get("timeInput") != null && params.get("timeInput").size() > 0){ 
			timeInput =  params.get("timeInput").get(0);
		} 
		if (params.get("volume") != null && params.get("volume").size() > 0){ 
			volume =  Integer.parseInt(params.get("volume").get(0));
		}
		if (params.get("fetchBuzzerList") != null && params.get("fetchBuzzerList").size() > 0){ 
			fetchBuzzerList =  Integer.parseInt(params.get("fetchBuzzerList").get(0));
		} 
		if (params.get("buzzerType") != null && params.get("buzzerType").size() > 0){ 
			buzzerType =  Integer.parseInt(params.get("buzzerType").get(0));
		} 
		if (params.get("musicSelected") != null && params.get("musicSelected").size() > 0){ 
			musicSelected =  params.get("musicSelected").get(0);
		}
		if (params.get("btn1Alarm") != null && params.get("btn1Alarm").size() > 0){   //alex
			btn1Alarm =  params.get("btn1Alarm").get(0);
		} 
		if (params.get("btn2Alarm") != null && params.get("btn2Alarm").size() > 0){   //alex
			btn2Alarm =  params.get("btn2Alarm").get(0);
		} 
		if (params.get("btn3Alarm") != null && params.get("btn3Alarm").size() > 0){   //alex
			btn3Alarm =  params.get("btn3Alarm").get(0);
		} 
		if (params.get("btn4Alarm") != null && params.get("btn4Alarm").size() > 0){   //alex
			btn4Alarm =  params.get("btn4Alarm").get(0);
		} 
		if (params.get("btn5Alarm") != null && params.get("btn5Alarm").size() > 0){   //alex
			btn5Alarm =  params.get("btn5Alarm").get(0);
		} 
		
	} 
	private String buildBuzzerDisplay(){
		StringBuilder sb = new StringBuilder();
		

		sb.append("<div class='mb-1'> <b>Buzzer:</b> " + (alarm != null ? alarm.getAlarmSound() : "N/A") + "</div>");
		Buzzer b = (alarm != null ? Buzzer.valueOf(alarm.getAlarmSound()): Buzzer.BUZZER);
		if (b == Buzzer.MP3){
			sb.append("<div class='mb-1'> <b>Song:</b> music here</div>"); //TODO fetch mp3 name
			sb.append("<div class='mb-2'> <b>Volume:</b> " +alarm.getVolume()+ "</div>"); //TODO fetch mp3 name
		}else if(b == Buzzer.RADIO){
			sb.append("<div class='mb-1'> <b>Radio:</b> 106.9</div>");  //TODO fetch radio name
			sb.append("<div class='mb-2'> <b>Volume:</b> " +alarm.getVolume()+ "</div>"); //TODO fetch mp3 name
		}
		
		return sb.toString();
	}
	private String buildIfActive(){
		StringBuilder sb = new StringBuilder();

		sb.append("<label class='switch'>");
		sb.append("<input type='checkbox' " + (alarm.isActive()? " checked" : "" ) + " id='activeCheckId'>");
		sb.append("<span class='slider round'></span>");
		sb.append("</label>");

		return sb.toString();
	}
	private String buildRepeat(){
		StringBuilder sb = new StringBuilder();		

		for(AlarmRepeat r : AlarmRepeat.values()){
			if(r != AlarmRepeat.NONE){

				String dayShort = r.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.CANADA) ;
				sb.append("<label class='form-check-label mr-2'>");
				sb.append("<input type='checkbox' name='repeat" + dayShort + "' " + (alarm.getAlarmRepeat().contains(r) ? "checked": "") + "> " + dayShort );
				sb.append("</label>");
			}
		}		

		return sb.toString();
	}
	private String buildRepeatDisplay(){
		StringBuilder sb = new StringBuilder();		
		sb.append("<div class='mb-3' >");

		for(AlarmRepeat r : AlarmRepeat.values()){
			if(r != AlarmRepeat.NONE){	
				String badge = "";
				if (alarm.getAlarmRepeat().contains(r)){
					badge = "badge badge-pill badge-success";
				}else{
					badge = "text-muted";
				}
				
				sb.append("<span class='" + badge + " mr-2' />"
						+ " <strong>" +r.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.CANADA)+ "</strong> </span>");

			}
		}
		sb.append("</div>");		

		return sb.toString();
	}

	private String buildTimeInput(){
		StringBuilder sb = new StringBuilder();		

		String time = "";
		if (!timeInput.isEmpty()){
			time = timeInput;
		}else{

			time = (alarm.getHour().isEmpty() ? "--" : alarm.getHour()) + ":" + (alarm.getMinutes().isEmpty() ? "--" : alarm.getMinutes());
		}

		sb.append("<input type='text' size='1' class='form-control' id='timeInput' name='timeInput' placeholder='HH:MM'"
				+ " value='" + time + "' required> " );

		return sb.toString();
	}
	
	private boolean timeValid(){

		Pattern pattern = Pattern.compile(TIME24HOURS_PATTERN);

		Matcher matcher = pattern.matcher(timeInput);

		return matcher.matches();

	}
	
	private boolean save(){
		message = "";
		
		StringBuilder errorBuilder = new StringBuilder();

		boolean canSave = true;
		
		//validate time
		if (!timeValid()){
			canSave = false;
			errorBuilder.append("Wrong time format: " + timeInput + " must be HH:MM<br/>");
		}else{		
			alarm.setHour(timeInput.substring(0, timeInput.indexOf(":") ));
			alarm.setMinutes(timeInput.substring(timeInput.indexOf(":")+1, timeInput.length()));
		}
		
		List<AlarmRepeat> repeats = new ArrayList<>();
		if (repeatSun){
			repeats.add(AlarmRepeat.SUNDAY);
		}
		if (repeatMon){
			repeats.add(AlarmRepeat.MONDAY);
		}
		if (repeatTue){
			repeats.add(AlarmRepeat.TUESDAY);
		}
		if (repeatWed){
			repeats.add(AlarmRepeat.WEDNESDAY);
		}
		if (repeatThu){
			repeats.add(AlarmRepeat.THURSDAY);
		}
		if (repeatFri){
			repeats.add(AlarmRepeat.FRIDAY);
		}
		if (repeatSat){
			repeats.add(AlarmRepeat.SATURDAY);
		}
		
		if (repeats.size() == 0){
			canSave = false;
			errorBuilder.append("No days of the week selected<br/>");
		}else{
			alarm.setAlarmRepeat(repeats);
		}
		
		Buzzer b = Buzzer.values()[buzzerType];		
		
		alarm.setAlarmSound(b.name());	
		if (b != Buzzer.BUZZER){	
			
			
			if (b == Buzzer.MP3 ){
				if (musicSelected == null || musicSelected.trim().length() ==0){
					errorBuilder.append("Cannot save, no song selected.<br/>");
					canSave = false;
				}else{
					alarm.setMp3Id(Integer.parseInt(musicSelected));
					alarm.setVolume(volume);
				}
			}else if (b == Buzzer.RADIO ){
				if (musicSelected == null || musicSelected.trim().length() ==0){
					errorBuilder.append("Cannot save, no radio station selected.<br/>");
					canSave = false;
				}else{
					alarm.setRadioId(Integer.parseInt(musicSelected));
					alarm.setVolume(volume);
				}
			}
			if (volume < 1){
				errorBuilder.append("Cannot save, please select volume greathen than 0.<br/>");
				canSave = false;
			}
			
		}else{
			alarm.setRadioId(-1);
			alarm.setMp3Id(-1);
			alarm.setVolume(-1);
		}
		
		if (canSave){
			getSession().set(SAVED_VIEWING_ALARM, alarm);
		}else{
			message = "Error while saving: <br/>" + errorBuilder.toString();
		}
		return canSave;
	}

	private String buildVolumeSelect(){
		StringBuilder volList = new StringBuilder();		
		
		for(int i = 0 ; i < 101 ; i++){
			volList.append("<option" +   ( i == alarm.getVolume() ? " selected " : " ") + " value=\"" + i + "\">" + i + "</option>");
		}
		return volList.toString();
	}
	
	private String buildBuzzerTypeSelect(){
		StringBuilder type = new StringBuilder();
		Buzzer alaBz = Buzzer.valueOf(alarm.getAlarmSound());
		
		for(int i = 0 ; i < Buzzer.values().length ; i++){
			Buzzer b = Buzzer.values()[i];
			type.append("<option" +   ( i == alaBz.ordinal() ? " selected " : " ") + " value=\"" + i + "\">" + b.getName() + "</option>");
		}
		return type.toString();
	}
	
	private String buildMusicSelectionListSelect(int buzzerOrdinal){
		//TODO load from radio or mp3 when needed.
		
		StringBuilder typeList = new StringBuilder();
		
		Buzzer alaBz = Buzzer.values()[buzzerOrdinal];
		
		if (alaBz == Buzzer.RADIO){
//			type.append("<option" +   ( i == alaBz.ordinal() ? " selected " : " ") + " value=\"" + i + "\">" + b.getName() + "</option>");
			typeList.append("<option value=\"2\">106.9</option>");
			typeList.append("<option value=\"1\">106.8</option>");
			typeList.append("<option value=\"22\">106.7</option>");
		}else if (alaBz == Buzzer.MP3){
			typeList.append("<option value=\"2\">sia</option>");
			typeList.append("<option value=\"1\">adda</option>");
			typeList.append("<option value=\"22\">affaa</option>");
		}
		
		return typeList.toString();
	}
	
	private String hideShowFunction(){
		Buzzer alaBz = Buzzer.valueOf(alarm.getAlarmSound());
		return alaBz != Buzzer.BUZZER ? "" : "display:none";
	}

	
	enum AlarmStatus{
		ACTIVE, DE_ACTIVATED, NOT_REQUESTED;
		
		public boolean isPresent(){
			return this == AlarmStatus.ACTIVE || this == AlarmStatus.DE_ACTIVATED;
		}
	}
	
	private void loadAlarm() throws ClassNotFoundException, SQLException{ //alex		
		
		if (btn1Alarm.length() > 0){
		
			//get the alarm with the order 1
			AlarmEntity ae = new AlarmSql().loadAlarmByOrderNbr(1);
			
			if (ae != null){				
				alarm = ae;
			}else{
				alarm = new AlarmEntity();
			}
			getSession().set(SAVED_VIEWING_ALARM, alarm);
			timeInput = "";
			customJs = customScript(1);
			
		}else if (btn2Alarm.length() > 0){
		
			AlarmEntity ae = new AlarmSql().loadAlarmByOrderNbr(2);
			
			if (ae != null){
				alarm = ae;
			}else{
				alarm = new AlarmEntity();
			}
			timeInput = "";
			getSession().set(SAVED_VIEWING_ALARM, alarm);
			customJs = customScript(2);
		}else if (btn3Alarm.length() > 0){
		
			AlarmEntity ae = new AlarmSql().loadAlarmByOrderNbr(3);
			
			if (ae != null){
				alarm = ae;
			}else{
				alarm = new AlarmEntity();
			}
			timeInput = "";
			getSession().set(SAVED_VIEWING_ALARM, alarm);
			customJs = customScript(3);
		}else if (btn4Alarm.length() > 0){
		
			AlarmEntity ae = new AlarmSql().loadAlarmByOrderNbr(4);
			
			if (ae != null){
				alarm = ae;
			}else{
				alarm = new AlarmEntity();
			}
			timeInput = "";
			getSession().set(SAVED_VIEWING_ALARM, alarm);
			customJs = customScript(4);
		}else if (btn5Alarm.length() > 0){
		
			AlarmEntity ae = new AlarmSql().loadAlarmByOrderNbr(5);
			
			if (ae != null){
				alarm = ae;
			}else{
				alarm = new AlarmEntity();
			}
			timeInput = "";
			getSession().set(SAVED_VIEWING_ALARM, alarm);
			customJs = customScript(5);
		}else{			
			
			AlarmEntity alarmSession = (AlarmEntity)getSession().get(SAVED_VIEWING_ALARM);
			
			if (alarmSession != null){
				//1st load alarm
				alarm = alarmSession; 
		
			}else{
				List<AlarmEntity> alarmEnt = new AlarmSql().loadAllAlarms();
				if(alarmEnt.size() > 0) {

					alarm = alarmEnt.get(0);
				}else {
					alarm = new AlarmEntity();
				}				

				getSession().set(SAVED_VIEWING_ALARM, alarm);

			}			
			
		}		
	}
	private String customScript(int btnNbr){
		StringBuilder sb = new StringBuilder();
		
		sb.append("<script>  $(document).ready(function(){ var btnNbr = "+btnNbr+"; "	);
		sb.append("$(\"#btn\" + btnNbr).removeClass(\"btn-primary\");");
		sb.append("$(\"#btn\" + btnNbr).addClass(\"btn-warning\");");
		sb.append(" }); </script>");
			
		return sb.toString();	
		
	}
}
