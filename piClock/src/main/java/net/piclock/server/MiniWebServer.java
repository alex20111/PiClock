package net.piclock.server;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import home.miniHttp.WebServer;
import net.piclock.db.sql.Mp3Sql;
import net.piclock.db.sql.RadioSql;
import net.piclock.main.Security;

/**
 * Content layout file. contentlayout.xml
 * <base>
	<page name="dashboard">
		<page_part name="dashboard.html"/>
		<page_part name="footer.html"/>		
	</page>
	<page name="alarmView">
		<page_part name="dashboard.html"/>
		<page_part name="footer.html"/>		
	</page>
  </base>
 *
 */
public class MiniWebServer  {

	//external html pages _
	private static String EXT_WEB_FOLDER = "webpages";//contains all the html pages

	private String serverRootDir = "/home/pi/piClock/webapp";
	private WebServer server;

	private static MiniWebServer miniWebServer;	

	public static MiniWebServer getInstance() { 
		if (miniWebServer == null) { 
			synchronized (MiniWebServer.class) { 
				if(miniWebServer == null) {  
					miniWebServer = new MiniWebServer(); 
				} 
			} 
		} 
		return miniWebServer; 
	} 

	public static void main(String[] args) throws IOException, URISyntaxException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ParserConfigurationException, SAXException, ClassNotFoundException, SQLException {

		MiniWebServer s = MiniWebServer.getInstance();
		s.startServer();
	}	
	public void createTables() throws ClassNotFoundException, SQLException, IOException{
		new RadioSql().CreateRadioTable();
		new Mp3Sql().CreateMp3Table();
	}

	public void startServer() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ParserConfigurationException, SAXException, IOException, URISyntaxException, ClassNotFoundException, SQLException{
		if (server == null || !server.isAlive()){

			Security s = new Security();

			boolean canAccessSettings = s.isSettingsPassProtected();

			//file location on disk
			File rootDir = new File(serverRootDir);		

			//handlers
			RadioPage rp = new RadioPage();

			WifiHandler wifi = null;
			if (canAccessSettings) {
				wifi = new WifiHandler();
			}
			Mp3Handler mp3 = new Mp3Handler();
			Alarmhandler ah = new Alarmhandler();
			BackgroundHandler bh = new BackgroundHandler();

			server = new WebServer(80,rootDir);
			server.addHandler("/", new MainPage());
//			server.addHandler("/radio", rp);
			if (canAccessSettings) {
				server.addHandler("/wifi", wifi);
			}
			server.addHandler("/mp3", mp3);
//			server.addHandler("/alarmView", ah);
			server.addHandler("/background", bh);
			server.addFileFolder("css");
			server.addFileFolder("js"); 
			server.addExternalHtmlFolder(EXT_WEB_FOLDER); //if the html pages are loaded externally. This define the root for html pages

			server.enableSessionManagement();

			server.startServer();

			createTables();

		}else{
			throw new IOException("Server already running");
		}
	}	
	public void stop(){
		if (server != null){
			server.stop();
		}
	}
}