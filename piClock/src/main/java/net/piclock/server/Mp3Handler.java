package net.piclock.server;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.nanohttpd.protocols.http.response.Response;

import home.fileutils.UnZip;
import home.miniHttp.HttpBase;
import home.miniHttp.StaticPageHandler;
import home.miniHttp.Table;
import net.piclock.db.entity.Mp3Entity;
import net.piclock.db.sql.Mp3Sql;

public class Mp3Handler extends HttpBase{

	private final String SELECT = "Select";
	private final String ALL 	= "All";
	private final String ROCK 	= "Rock";
	private final String POP 	= "Pop";
	private final String OTHER 	= "Other";
	
	private Mp3Sql sql;
	
	//temp and MP3 folder
	private String mp3Folder = "/home/pi/piClock/mp3";
	private String tempDir = "/home/pi/piClock/temp";	
	
	private String viewPageName = "mp3Page";
	private String addPageName = "addMp3Page";
	
	private Optional<String> mode; //add or view
	private Optional<String> mp3Catg; //add or view
	
	private Optional<String> mp3File; //add or view
	private Optional<String> btnSendFile; //add or view
	private Optional<String> delMp3; //add or view	
	
	private String message = "";	
	
	@Override
	public Response handleRequest() {
		sql = new Mp3Sql();
		String webPage = "Error";
		try{
			message = "";

			List<File> webPageFiles = new ArrayList<File>();
			Map<String, String> values = new HashMap<String, String>();

			if (mode.isPresent() && mode.get().equals("add")){
				System.out.println("Mode add. btnSendFile " + btnSendFile);	

				if (btnSendFile.isPresent() && !btnSendFile.get().isEmpty()){
					if (mp3File.isPresent() && !mp3File.get().isEmpty()){
						handleFile();
					}else{
						message = generateErrorMessage("Please enter a ZIP or MP3 file");
					}
				}
				values.put("message1",message);
				webPageFiles = getWebPageOnDisk(addPageName);
			}else{
				System.out.println("Mode view");
				if (mode.isPresent() && mode.get().equals("delete")){
					System.out.println("Delete : " + delMp3);
					sql.delete(Integer.parseInt(delMp3.get()));
					
				}
				webPageFiles = getWebPageOnDisk(viewPageName );				
				values.put("table",buildTable());
				values.put("mp3catSel", buildSelect()); //key in the html page is : %-valuel-%
				values.put("customScript", generateCustomScript());
			}

			webPage =	StaticPageHandler.processPage(webPageFiles, values);

		}catch (Exception ex){
			ex.printStackTrace();
		}

		return Response.newFixedLengthResponse(webPage);
	}
/**
 * Build a select statement for the categories.
 * @param category
 * @return
 */
	public String buildSelect(){
		
		String currCat = mp3Catg.get();		
		
		List<String> mp3Cat = new ArrayList<String>();
		mp3Cat.add(SELECT);
		mp3Cat.add(ALL);
		mp3Cat.add(ROCK);
		mp3Cat.add(POP);
		mp3Cat.add(OTHER);
		
		StringBuilder catgSelect = new StringBuilder();
		for(String s : mp3Cat){
			catgSelect.append("<option" + (s.equalsIgnoreCase(currCat) ? " selected " : " ") + " value=\"" + s + "\">" + s + "</option>");
		}
		return catgSelect.toString();
	}

	private String buildTable() throws Exception{
		
		if (!mp3Catg.isPresent()){
			mp3Catg = Optional.of(ROCK);
		}
		
		List<Mp3Entity> mp3s = sql.loadAllMp3ByCatg(mp3Catg.get());
		
		Table table = new Table(3);
		table.addHeader("Name");
		table.addHeader("Category");
		table.addHeader("Delete");
		
		table.addTableClass("table");
		if (mp3s.size() > 0){
			for(Mp3Entity mp3 : mp3s){
				table.addTableBody(mp3.getMp3Name());
				table.addTableBody(mp3.getMp3Catg());
				table.addTableBody("<a href=\"/mp3?mode=delete&delMp3="+mp3.getId() + "&mp3Catg=" + mp3Catg.get() +  " \" > del </a>");
			}
		}else{
			table.addTableBody("No Mp3 ");
		}
		
		table.addTheaderClass("thead-dark");

		return table.build();
	}
	@Override
	public void handleParameters(Map<String, List<String>> params) {
		mode = Optional.empty();
		mp3Catg = Optional.empty();
		mp3File = Optional.empty();
		btnSendFile = Optional.empty();
		delMp3 = Optional.empty();
		
		
		if (params.get("mode") != null && params.get("mode").size() > 0){
			mode = Optional.ofNullable(params.get("mode").get(0));
		}	
		if (params.get("mp3Catg") != null && params.get("mp3Catg").size() > 0){
			mp3Catg = Optional.ofNullable(params.get("mp3Catg").get(0));
		}
		if (params.get("mp3File") != null && params.get("mp3File").size() > 0){
			mp3File = Optional.ofNullable(params.get("mp3File").get(0));
		}
		if (params.get("btnSendFile") != null && params.get("btnSendFile").size() > 0){
			btnSendFile = Optional.ofNullable(params.get("btnSendFile").get(0));
		}
		if (params.get("delMp3") != null && params.get("delMp3").size() > 0){
			delMp3 = Optional.ofNullable(params.get("delMp3").get(0));
		}
	}
	private void handleFile(){
		try {
			if (mp3File.get().indexOf(".zip") > 0) {
				
				String fileName = getFileName();

				saveFiles(tempDir);

				//uncompress files
				UnZip unzip = new UnZip();
				unzip.addUnzipDestFolder(tempDir);
				unzip.addUnzipFile(tempDir + fileName);
				unzip.unZipIt();

				File tempDirF = new File(tempDir);

				File[] mp3Files = tempDirF.listFiles(new FilenameFilter() {

					public boolean accept(File dir, String name) {
						return name.endsWith(".mp3");
					}
				});

				for(File f : mp3Files){
					Path mvFile = f.toPath();
					Files.move(mvFile, new File(mp3Folder).toPath().resolve(mvFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
				}

				//delete everything in folder.
				for(File file: tempDirF.listFiles())
				{ 		
					Files.deleteIfExists(file.toPath());
				}
				
				addToDB(mp3Files);
				
				message =generateSuccessMessage("Upload success");
				
			}else if (mp3File.get().indexOf(".mp3") > 0){				
				saveFiles(mp3Folder);
				String fileName = getFileName();
				addToDB(fileName);
				message =generateSuccessMessage("Upload success");

			}else{				
				message = generateErrorMessage("Invalid file format. It needs to be a ZIP or MP3 file");
			}


		} catch (Exception e) {
			e.printStackTrace();
			message = generateErrorMessage("error");
		}
	}
	private String getFileName(){
		String osSepChar = (mp3File.get().contains("\\") ? "\\" : "/" );
		return mp3File.get().substring(mp3File.get().lastIndexOf(osSepChar), mp3File.get().length());	
	}
	private void addToDB(String mp3File) throws ClassNotFoundException, SQLException{	
		Mp3Entity mp = new Mp3Entity();
		mp.setMp3Name(mp3File);
		mp.setMp3Catg(OTHER);
		sql.add(mp);

	}
	private void addToDB(File[] mp3Files) throws ClassNotFoundException, SQLException{
		for(File f : mp3Files){
		Mp3Entity mp = new Mp3Entity();
			mp.setMp3Name(f.getName());
			mp.setMp3Catg(OTHER);
			sql.add(mp);
		}
	}	
	private String generateCustomScript(){
		StringBuilder sb = new StringBuilder();
		sb.append("<script> ");
		sb.append("$('#mp3CatgSelect').on('change', function(){ ");
		sb.append("$('form').submit(); ");
		sb.append("}); ");
		sb.append("	</script>");
		return sb.toString();
	}
}
