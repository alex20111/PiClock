package net.piclock.server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.nanohttpd.protocols.http.content.CookieHandler;
import org.nanohttpd.protocols.http.response.Response;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import home.fileutils.UnZip;
import home.miniHttp.CookieValue;
import home.miniHttp.HttpBase;
import home.miniHttp.StaticPageHandler;
import home.miniHttp.Table;
import net.piclock.db.entity.Mp3Entity;
import net.piclock.db.sql.Mp3Sql;
import net.piclock.main.Constants;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.SwingContext;


public class Mp3Handler extends HttpBase{

	private static List<Mp3GenreFilter> filters;
	
	private Mp3Sql sql;
	
	List<String> errors = new ArrayList<String>();
	
	//temp and MP3 folder
	private String mp3Folder = "/home/pi/piClock/mp3";
	private String tempDir = "/home/pi/piClock/temp";		
	
	private String viewPageName = "mp3Page";
	private String addPageName = "addMp3Page";
	
	private Optional<String> mode; //add or view or delete or deleteall
	private Optional<Integer> mp3Catg; //add or view
	
	private Optional<String> mp3File; //add or view
	private Optional<String> btnSendFile; //add or view
	private Optional<String> delMp3; //add or view	
	
	private String message = "";	
	private SwingContext ct;
	
	public Mp3Handler() {
		sql = new Mp3Sql();
		filters = new ArrayList<>();
		ct = SwingContext.getInstance();
	}
	
	@Override
	public Response handleRequest() {
	
		sql = new Mp3Sql();
		
		String webPage = "Error";
		try{
			message = "";

			List<File> webPageFiles = new ArrayList<File>();
			Map<String, String> values = new HashMap<String, String>();

			if (mode.isPresent() && mode.get().equals("add")){
//				System.out.println("Mode add. btnSendFile " + btnSendFile);	

				if (btnSendFile.isPresent() && !btnSendFile.get().isEmpty()){
					if (mp3File.isPresent() && !mp3File.get().isEmpty()){
						handleFile();
						
						if (!errors.isEmpty()){
							
							StringBuilder errorMsg = new StringBuilder();
							errorMsg.append("Some errors occured: <br/>");
							for(String s : errors){
								errorMsg.append(s + "<br/>");
							}
							if (message.length() > 0){
								message = message +  generateErrorMessage(errorMsg.toString());
							}else{
								message = generateErrorMessage(errorMsg.toString());
							}
						}
						
					}else{
						message = generateErrorMessage("Please enter a ZIP or MP3 file");
					}
				}
				values.put("message1",message);
				webPageFiles = getWebPageOnDisk(addPageName);
			}else{
//				System.out.println("Mode view");
				if (mode.isPresent() && mode.get().equals("delete")){
//					System.out.println("Delete : " + delMp3);
					deleteMp3();
					
				}else if(mode.isPresent() && mode.get().equals("deleteall"))	{
//					System.out.println("deleta all: " + mode.get() + " catg: " + mp3Catg.get());
					deleteAllMp3();
				}
				
				webPageFiles = getWebPageOnDisk(viewPageName );	
				values.put("message1",message);
				values.put("table",buildTable());
				values.put("mp3catSel", buildSelect());
				values.put("customScript", generateCustomScript());
				values.put("deleteAllBtn", buildDeleteAllBtn());
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
 * @throws SQLException 
 * @throws ClassNotFoundException 
 */
	public String buildSelect() throws ClassNotFoundException, SQLException{
		
		int currCat = mp3Catg.get();	
		
		filters = sql.getAllAvailableGenre();
		
		StringBuilder catgSelect = new StringBuilder();
		Mp3GenreFilter f  = new Mp3GenreFilter(99, -1, Mp3GenreFilter.ALL);
		
		catgSelect.append("<option" + (100 == currCat ? " selected " : " ") + " value=\"100\">Select</option>");
		catgSelect.append("<option" + (f.getId() == currCat ? " selected " : " ") + " value=\"" + f.getId() + "\">" + f.getDisplayName() + "</option>");
		int row2 = catgSelect.length();
		for(Mp3GenreFilter s : filters){
			String select = "<option" + (s.getId() == currCat ? " selected " : " ") + " value=\"" + s.getId() + "\">" + s.getDisplayName() + "</option>";
			if (Mp3GenreFilter.OTHER.equals(s.getDisplayName())){
				catgSelect.insert(row2, select);
			}else{
				catgSelect.append(select);
			}			
		}		
		return catgSelect.toString();
	}

	private String buildTable() throws Exception{		

		List<Mp3Entity> mp3s  = new ArrayList<>();

		CookieValue cookie = getCookieValue();

		if (mp3Catg.isPresent()){
			Mp3GenreFilter filter = filters.stream().filter(c -> c.getId() == mp3Catg.get().intValue())
					.findAny().
					orElse(new Mp3GenreFilter(99, -1, Mp3GenreFilter.ALL));
			mp3s = sql.loadAllMp3ByGenre(filter);

			if (cookie.hasCookie()){
				cookie.setMp3GenreValue(String.valueOf(mp3Catg.get()));

			}else{
				cookie = new CookieValue();
				cookie.setMp3GenreValue(String.valueOf(mp3Catg.get()));
				cookie.setFiller("Anything");
			}			 
			setCookieValue(cookie);
		}else if (cookie != null && cookie.hasCookie()){
			mp3Catg = Optional.of(Integer.parseInt(cookie.getMp3GenreValue()));
			Mp3GenreFilter filter = filters.stream().filter(c -> c.getId() == mp3Catg.get().intValue())
					.findAny().
					orElse(new Mp3GenreFilter(99, -1, Mp3GenreFilter.ALL));
			mp3s = sql.loadAllMp3ByGenre(filter);
		}else{
			mp3Catg = Optional.of(100);
		}

		Table table = new Table(4);
		table.addHeader("Name");
		table.addHeader("Artist");
		table.addHeader("Category");
		table.addHeader("Delete");

		table.addTableClass("table");
		if (mp3s.size() > 0){
			for(Mp3Entity mp3 : mp3s){
				table.addTableBody(mp3.getMp3Name());
				table.addTableBody(mp3.getArtist());
				table.addTableBody( (mp3.getMp3GenreDesc() == null || mp3.getMp3GenreDesc().trim().isEmpty()? "Other" : mp3.getMp3GenreDesc()) );
				table.addTableBody("<a href=\"/mp3?mode=delete&delMp3="+mp3.getId() + "&mp3Catg=" + mp3Catg.get() +  " \" > Delete </a>");
			}
		}else{
			table.addTableBody("No Mp3 ");
		}

		table.addTheaderClass("thead-dark");

		return table.build();
	}
	private String buildDeleteAllBtn(){
		String returns = "";
		if (mp3Catg.isPresent() && mp3Catg.get().intValue() != 100){			
			returns = "<a href=\"/mp3?mode=deleteall&mp3Catg=" + mp3Catg.get() + "\" class=\"btn btn-danger btn-xs\">Delete All</a>";
		}
		return returns;
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
			mp3Catg = Optional.ofNullable(Integer.parseInt(params.get("mp3Catg").get(0)));
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

				List<Path> mp3FileList = new ArrayList<>();
				for(File f : mp3Files){
					Path mvFile = f.toPath(); //convert to path
					Path fm =  new File(mp3Folder).toPath().resolve(mvFile.getFileName());
					Files.move(mvFile,fm, StandardCopyOption.REPLACE_EXISTING);
					mp3FileList.add(fm);
				}

				//delete everything in folder.
				for(File file: tempDirF.listFiles())
				{ 		
					Files.deleteIfExists(file.toPath());
				}
				
				addToDB(mp3FileList);
				
				
				ct.sendMessage(Constants.RELOAD_FROM_WEB, new Message("Reload"));
				
				message = generateSuccessMessage("File successfully uploaded");
				
			}else if (mp3File.get().indexOf(".mp3") > 0){	
				System.out.println("MP#: IN");
				saveFiles(mp3Folder);
				String fileName = getFileName();
				
				System.out.println("MP#: FILE name: " + fileName);
				
				addToDB(new File(mp3Folder + File.separatorChar +  fileName));
				
				ct.sendMessage(Constants.RELOAD_FROM_WEB, new Message("Reload"));

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
		String fileName = mp3File.get();
		
		if (fileName.contains(osSepChar)) {
			fileName = mp3File.get().substring(mp3File.get().lastIndexOf(osSepChar), mp3File.get().length());
		}
		
		return fileName;	
	}
	private void addToDB(File mp3File) throws ClassNotFoundException, SQLException, UnsupportedTagException, InvalidDataException, IOException{
		
		List<Path> files =  new ArrayList<>();
		files.add(mp3File.toPath());
		
		addToDB(files);
	}
	private void addToDB(List<Path> mp3Files) throws ClassNotFoundException, SQLException{
		for(Path p : mp3Files){
			File f = p.toFile();

			if (f.exists() && f.isFile() && f.getName().endsWith("mp3")){

				Mp3Entity ent = new Mp3Entity();

				try{
					Mp3File mp3 = new Mp3File(f);

					long duration = mp3.getLengthInSeconds();

					if (duration > 0){
						ent.setMp3Length((int)duration);
					}

					if (mp3.hasId3v2Tag()){
						ID3v2  id = mp3.getId3v2Tag();
						ent.setMp3Name(id.getTitle() != null && id.getTitle().length() > 0 ? id.getTitle() : getNameFromFile(f.getName()));
						ent.setMp3Genre(id.getGenre());						
						ent.setMp3GenreDesc(id.getGenreDescription());
						ent.setArtist(id.getArtist());
						ent.setMp3FileName(f.getName());
					}else if(mp3.hasId3v1Tag()){
						ID3v1  id = mp3.getId3v1Tag();
						ent.setMp3Name(id.getTitle() != null && id.getTitle().length() > 0 ? id.getTitle() : getNameFromFile(f.getName()));
						ent.setMp3Genre(id.getGenre());
						ent.setMp3GenreDesc(id.getGenreDescription());
						ent.setArtist(id.getArtist());
						ent.setMp3FileName(f.getName());
					}else{
						ent.setMp3Name(getNameFromFile(f.getName()));
						ent.setMp3FileName(f.getName());
					}

					sql.add(ent);
				}catch(Exception e){
					errors.add("Error while adding file: " + f.getName() + " file was not added");
					e.printStackTrace();
				}

			}

		}
	}	
	public static String getNameFromFile(String fileName){
		String name = fileName.substring(0,fileName.lastIndexOf("."));
		
		return name;
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
	
	private void deleteMp3() throws ClassNotFoundException, SQLException, IOException{
		//1st load filename for the mp3
		int mp3Id = Integer.parseInt(delMp3.get());
		 Mp3Entity fileName = sql.loadMp3FileNameById(mp3Id);
		Path f =  Paths.get(mp3Folder + fileName.getMp3FileName());
		
		Files.deleteIfExists(f);
		
		sql.delete(mp3Id);
		
		message = generateSuccessMessage("Deleted mp3: " + fileName.getMp3Name());
		ct.sendMessage(Constants.RELOAD_FROM_WEB, new Message("Reload"));
	}
	private void deleteAllMp3() throws ClassNotFoundException, SQLException{
		List<String> successDelete = new ArrayList<>();
		List<String> errorsDelete = new ArrayList<>();		
		
		//1st load all mp3 from that category
		Optional<Mp3GenreFilter> filter = filters.stream().filter(c -> c.getId() == mp3Catg.get().intValue())
				.findAny();
		
		if (!filter.isPresent() && mp3Catg.get().intValue() == 99){
			filter = Optional.of(new Mp3GenreFilter(99, -1, Mp3GenreFilter.ALL));
		}
		List<Mp3Entity> mp3List = sql.loadAllMp3ByGenre(filter.get());

		//then Delete the files from the disk, also create a list of ids to delete from DB
		List<Integer> mp3Ids = new ArrayList<>();
		for(Mp3Entity delMp3 : mp3List){
			mp3Ids.add(delMp3.getId());
			try {
				Path f =  Paths.get(mp3Folder + delMp3.getMp3FileName());			

				 Files.deleteIfExists(f);
				
				sql.deleteMultiple(mp3Ids);
				
				successDelete.add(delMp3.getMp3Name());
//				System.out.println("fileDeleted: " + fileDeleted);
			} catch (IOException | SQLException e) {
				errorsDelete.add("Error deleting mp3: " + delMp3.getMp3Name());
				e.printStackTrace();
			}
		}		

		if (!successDelete.isEmpty()){
			StringBuilder msg = new StringBuilder();
			msg.append("Deleted: <br/>");
			for(String s: successDelete){
				msg.append(s + " <br/>");
			}
			if (!errorsDelete.isEmpty()){
				msg.append("<br/>The following errors occured:");
				for(String s: errorsDelete){
					msg.append(s + " <br/>");
				}
			}
		
			message = generateSuccessMessage(msg.toString() );
			ct.sendMessage(Constants.RELOAD_FROM_WEB, new Message("Reload"));
		}
	}
	
	//FIXME to add to httpBase
	public CookieValue getCookieValue(){
		String cookieName = "cookiename";
		
		CookieValue cookieValue= new CookieValue();
		CookieHandler ch = getHttpSession().getCookies();
		
		for(String cname : ch){
			if (cookieName.equals(cname)){
				cookieValue.fromString(ch.read(cookieName));
				break;
			}
		}
		
		return cookieValue;
	
	}
	public void setCookieValue(CookieValue cookie){
		String cookieName = "cookiename";
		CookieHandler ch = getHttpSession().getCookies();
		ch.set(cookieName, cookie.toString(), 365 * 20);
	}
}
