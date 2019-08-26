package net.piclock.main;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.exec.ExecuteException;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;

import home.fileutils.FileUtils;
import home.misc.Exec;
import net.piclock.arduino.ArduinoSerialCmd;
import net.piclock.arduino.ButtonChangeListener;
import net.piclock.arduino.ListenerNotFoundException;
import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.button.MonitorButtonHandler;
import net.piclock.enums.Buzzer;
import net.piclock.enums.CheckWifiStatus;
import net.piclock.main.Constants;
import net.piclock.enums.Light;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.FormatStackTrace;
import net.piclock.util.Mp3Streaming;
import net.piclock.util.RadioStreaming;

public class PiHandler {
	
	public static boolean localTest = false;
	
	private static final Logger logger = Logger.getLogger( PiHandler.class.getName() );

	private static PiHandler piHandler;
	
	private ArduinoSerialCmd cmd;
	private SwingContext context;
	
	private  boolean screenOn = true;
	private  boolean wifiConnected = false; //if connected to the WIFI.
	private  boolean wifiInternetConnected = false; //if connected to the internet.	
	
	private  boolean 	wifiOn = true; //initially the wifi is always ON
	
	private  Thread wifiShutDown;
	private  Thread screenAutoShutDown;
	private  Thread checkConnection; 		
	
	//commands to ard
	private  String BUZZER = "buzzer";
	private  String LDR 	 = "ldr";
	private  String TIME 	 = "time";
	private  String TIME_OFF = "timeOff";
	
	private ButtonChangeListener monitorBtnHandler;
	
	private RadioStreaming streaming;
	private Mp3Streaming mp3Stream;
	
	private PiHandler() {
		Gpio.wiringPiSetup();
		SoftPwm.softPwmCreate(24, 70, 100);
		
		monitorBtnHandler = new MonitorButtonHandler();
		try {
			cmd = ArduinoSerialCmd.getInstance();
			cmd.addButtonListener(monitorBtnHandler);
			context = SwingContext.getInstance();
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Error in PiHandler", ex);
		} 
	}
	
	public static PiHandler getInstance() {
		if (piHandler == null) {
			synchronized (PiHandler.class) {
				if(piHandler == null) {
					logger.log(Level.INFO, "PiHandler initialized");
					piHandler = new PiHandler();
				}
			}
		}
		return piHandler;
	}	

	public void turnOffScreen() throws InterruptedException, ExecuteException, IOException, ListenerNotFoundException, UnsupportedBusNumberException{
		logger.log(Level.INFO,"turnOffScreen()");

//		autoWifiShutDown(true);

		setScreenOn(false);
		setBrightness(Light.DARK);

		logger.log(Level.CONFIG,"end turnOffScreen(). ");


	}
	/*withWifiOn: then turn on the wifi on request*/
	public void turnOnScreen(boolean withWifiOn, Light brightness) throws InterruptedException, ExecuteException, IOException{
		logger.log(Level.INFO,"Turning on screen. Wifi on option: " + withWifiOn);

//		autoWifiShutDown(false);

		//if screen is auto shutting down and there is a request by the LDR to turn it back on, kill it.
		cancelScreenAutoShutdown();

		if (withWifiOn){
			turnWifiOn();
		}

		setScreenOn(true);
		setBrightness(brightness);

	}
	public List<String> fetchWifiList() throws Exception{
		logger.log(Level.CONFIG,"fetchWifiList() ");
		List<String> wifiList = new ArrayList<String>();
		Exec e = new Exec();
		e.addCommand("sudo").addCommand("./scripts/scanssid.sh").addCommand("/home/pi/piClock/scripts/essid.txt");

		e.timeout(10000);

		int ext = e.run();

		wifiList = FileUtils.readFileToArray("/home/pi/piClock/scripts/essid.txt");

		logger.log(Level.INFO, "fetchWifiList ext return: " + ext +  "  out: " + e.getOutput());

		return wifiList;

	}
	/**When 1st connecting to a new WIFI
	 * @throws IOException */
	public void connectToWifi(String wifiName, String pass) throws InterruptedException, IOException{
		logger.log(Level.INFO,"connectToWifi()");
		
		if (wifiName != null && wifiName.length() > 0 && pass != null && pass.length() > 0){
			//1st remove any network if exist
			disconnectWifi();
			
			Path wpaFile = Paths.get("/etc/wpa_supplicant/wpa_supplicant.conf");
			
			String newNetwork = "\n\nnetwork={\n\tssid=\"" + wifiName.trim() +"\"\n\tpsk=\""+pass.trim()+"\"\n\tkey_mgmt=WPA-PSK\n} ";

			String newContents = new String(Files.readAllBytes(wpaFile));

			String newString = newContents.trim().concat(newNetwork);
			
			logger.log(Level.CONFIG, "Wpa conf content: " + newContents + "  Appended (newString): " + newString + " WPA exist:" + Files.exists(wpaFile));
			byte[] strToBytes = newString.getBytes();
			Files.write(wpaFile, strToBytes);
			
			refreshWifi();
			
			checkInternetConnection(false);
		}				
	}
	public Light getLDRstatus(){

		int ldrVal = sendI2cCommand(LDR,null);		
		return Light.setLightLevel(ldrVal);
	}
	/**Turn on the alarm based on the selected buzzer
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws ExecuteException 
	 * @throws InterruptedException **/
	public void turnOnAlarm(Buzzer alarm, String text, int volume) throws   ExecuteException, IOException, InterruptedException{
		logger.log(Level.CONFIG,"Turning on: " + alarm.getName());		
		
		if (alarm == Buzzer.BUZZER){
			buzzer(true);
		}else if (alarm == Buzzer.RADIO){
			playRadio(true, text, volume);
		}else if (alarm == Buzzer.MP3){
			playMp3(true, text, volume);
		}
	}
	public void turnOffAlarm(Buzzer buzzerType) throws ExecuteException, IOException, InterruptedException{
		logger.log(Level.CONFIG,"Turning off: " + buzzerType.getName());
			
		if (buzzerType == Buzzer.BUZZER){
			buzzer(false);
		}else if (buzzerType == Buzzer.RADIO){
			playRadio(false, "", -1);
		}else if (buzzerType == Buzzer.MP3){
			playMp3(false, "", -1);
		}
	}
	public void displayTM1637Time(String time){
		
		sendI2cCommand(TIME,time);
	}
	public void turnOffTM1637Time(){
		sendI2cCommand(TIME_OFF, null);
	}
	
	//turn off the screen automatically
	public void autoShutDownScreen(final int shutDownMillis) throws InterruptedException{
		logger.log(Level.CONFIG, "autoShutDownScreen.");		
	
		cancelScreenAutoShutdown();	
		
		screenAutoShutDown = new Thread(new Runnable() {				
			@Override
			public void run() {
				try {
					Thread.sleep(shutDownMillis);
					try {
						logger.log(Level.INFO, "autoShutDownScreen invoked, turning off screen");
						turnOffScreen();
					} catch (Exception e) {
						ErrorHandler eh = (ErrorHandler)context.getSharedObject(Constants.ERROR_HANDLER);
		  				eh.addError(ErrorType.PI, new ErrorInfo(new FormatStackTrace(e).getFormattedException()));
						logger.log(Level.SEVERE ,  "Cannot automatically shutdown monitor", e);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}	
				
				logger.log(Level.CONFIG, "autoShutDownScreen RUN ethode finished");
			}
		});
		screenAutoShutDown.start();
	}
	public boolean isAutoShutdownInProgress() {
		return (screenAutoShutDown != null && screenAutoShutDown.isAlive() ? true : false);
	}
	public boolean isWifiAutoShutdownInProgress() {
		return (wifiShutDown != null && wifiShutDown.isAlive() ? true : false);
	}
	public boolean isWifiOn() {
		return wifiOn;
	}

	/**check if connected to the internet
	 * @throws InterruptedException **/
	public void checkInternetConnection(boolean retry) throws InterruptedException{
	
		if (checkConnection != null && checkConnection.isAlive()){
			checkConnection.interrupt();
			while(checkConnection.isAlive()){
				checkConnection.join(100);
			}
			logger.log(Level.CONFIG, "check Connection: Interrupted checkInternetConnection");
		}			
	
		checkConnection = new Thread(new Runnable() {
			boolean keepRunning = true;
			int count = 0;
			int maxCnt = 15;
			@Override
			public void run() {
					
				try {
						
					context.putSharedObject(Constants.CHECK_INTERNET, CheckWifiStatus.STARTING);					
					boolean hasInternet = false;
					boolean hasIp = false;
					boolean retryOnce = retry;
					
					while(keepRunning){
						
						if (!hasIp){							
							hasIp = checkIfIpPresent();
						}
						
						if (count == maxCnt && !hasIp && !hasInternet){
							//1st check if we do a retry
							if (retryOnce) {
								logger.log(Level.CONFIG, "Retrying to connect to WIFI!");
								retryOnce = false;
								refreshWifi();
								count = 0;
								
							}else {
								context.putSharedObject(Constants.CHECK_INTERNET, CheckWifiStatus.END_TIMEOUT);
								keepRunning = false;
							}
						}else if (count == maxCnt && hasIp && !hasInternet){
							setWifiConnected(true);							
							wifiInternetConnected = false;
							context.putSharedObject(Constants.CHECK_INTERNET, CheckWifiStatus.END_NO_INET);
							keepRunning = false;
						}
						else if (hasIp && hasInternet){
							setWifiConnected(true);							
							wifiInternetConnected = true;
							context.putSharedObject(Constants.CHECK_INTERNET, CheckWifiStatus.SUCCESS);
							keepRunning = false;
						}else if (hasIp){
							//check for internet
							hasInternet = checkIfCanAccessInternet();							
						}
						
						count ++;
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {	
					Thread.currentThread().interrupt();
					context.putSharedObject(Constants.CHECK_INTERNET, CheckWifiStatus.END_INTERRUPTED);
					keepRunning = false;
				}  catch ( IOException e) {
					context.putSharedObject(Constants.CHECK_INTERNET, CheckWifiStatus.END_TIMEOUT);
					keepRunning = false;
					ErrorHandler eh = (ErrorHandler)context.getSharedObject(Constants.ERROR_HANDLER);
	  				eh.addError(ErrorType.PI, new ErrorInfo(new FormatStackTrace(e).getFormattedException()));
					logger.log(Level.SEVERE, "Error connecting to wifi", e);
				} 
			}
		});
		checkConnection.start();
	}
	public void disconnectWifi() throws InterruptedException, IOException{
		logger.log(Level.CONFIG, "disconnectWifi()");
		
		Path wpaFile = Paths.get("/etc/wpa_supplicant/wpa_supplicant.conf");
		String contents = new String(Files.readAllBytes(wpaFile));
		
		if (contents.contains("network")) {
			String noWifi = contents.substring(0,contents.indexOf("network"));

			byte[] strToBytes = noWifi.getBytes();

			Files.write(wpaFile, strToBytes);			
		}
		
		refreshWifi();
		Thread.sleep(2000);		
		

		context.putSharedObject(Constants.CHECK_INTERNET, CheckWifiStatus.END_DISCONNECT);
		setWifiConnected(false);
		setWifiInternetConnected(false);
	
	}
	/** turn wifi card ON. This does not mean that we are connected to it, it just turn it ON
	 * @throws IOException 
	 * @throws ExecuteException **/
	public void turnWifiOn() throws InterruptedException, ExecuteException, IOException{
		logger.log(Level.CONFIG, "wifiOn(). wifiOn : " + wifiOn);
		
		cancelWifiAutoShutdown();
		
		if (!wifiOn){
			wifiOn = true;	
			Exec e = new Exec();
			e.addCommand("sudo").addCommand("ifconfig").addCommand("wlan0").addCommand("up");

			e.timeout(10000);

			int ext = e.run();

			Preferences p = (Preferences)context.getSharedObject(Constants.PREFERENCES);

			if (p.getWifi() != null && p.getWifi().length() > 0 
					&& p.getWifiPass() != null && p.getWifiPass().length() > 0){				
				checkInternetConnection(true); //chekc if internet connection is still working
			}

			logger.log(Level.CONFIG, "turning wifi up. Exit : " + ext + ".  output: " + e.getOutput() );
		}
	
	}
	//verify if the monitor is actually ON of OFF	
	public boolean isMonitorOn() throws ExecuteException, IOException {
		
		Exec exec = new Exec();
		exec.addCommand("sudo").addCommand("xset").addCommand("q");

		exec.run();

		String output = exec.getOutput().toLowerCase();

		if (output.contains("monitor is on")){
			return true;
		}else if (output.contains("monitor is off")){
			return false;
		}else {
			return false;
		}
	}
	public void adjustVolume(int volume) throws ExecuteException, IOException {
		logger.log(Level.CONFIG, "manipulate volume: " + volume);
		Exec exec = new Exec();
		exec.addCommand("amixer").addCommand("-c").addCommand("1").addCommand("set")
		.addCommand("Speaker").addCommand(String.valueOf(volume) + "%").timeout(10000);
		
		int ext = exec.run();
		
		if (ext > 0 ){
			logger.log(Level.INFO, "Problem with volume. Ext: " + ext + "  output: " + exec.getOutput());
		}
	}
	public void setBrightness(Light light) {
		logger.log(Level.CONFIG, "setBrightness : " + light + "   pwn: " + light.getPwmLevel());
		SoftPwm.softPwmWrite(24, light.getPwmLevel());
	}
	public void shutdown() {
		setBrightness(Light.VERY_BRIGHT);
		GpioFactory.getInstance().shutdown();
	}
	public  boolean isScreenOn() {
		return screenOn;
	}
	public  void setScreenOn(boolean screenOn) {
		this.screenOn = screenOn;
	}
	public  boolean isWifiConnected() {
		return wifiConnected;
	}
	public  synchronized void setWifiConnected(boolean wifiConnected) {
		this.wifiConnected = wifiConnected;
	}
	public  boolean isWifiInternetConnected() {
		return wifiInternetConnected;
	}
	public  void setWifiInternetConnected(boolean wifiInternetConnected) {
		this.wifiInternetConnected = wifiInternetConnected;
	}

	public String getIpAddress() throws ExecuteException, IOException {
		Exec exec = new Exec();

		exec.addCommand("hostname").addCommand("--all-ip-addresses").timeout(5000);

		int ext = exec.run();

		logger.log(Level.INFO, "getIpAddress(), error in return exec:  " + ext +". Output: " + exec.getOutput());
		return exec.getOutput();
	}	
	private void cancelScreenAutoShutdown() throws InterruptedException{
			
		boolean isRunning = screenAutoShutDown != null && screenAutoShutDown.isAlive();
		
		logger.log(Level.CONFIG, "cancelScreenAutoShutdown. Is running? " + isRunning);
		
		if (isRunning){
			screenAutoShutDown.interrupt();
			
			screenAutoShutDown = null;
			logger.log(Level.CONFIG, "Interrupted screenAutoShutDown");
		}
	}
	private synchronized int sendI2cCommand(String command , String value){
		int retCd = -1;

		try {

			if (command.equals(TIME)){
				cmd.writeTime(value);
			}else if(command.equals(LDR)) {
				retCd = cmd.readLdr();
			}else if(command.equals(TIME_OFF)) {
				cmd.timeOff();
			}else if(command.equals(BUZZER)) {
				if ( value.equals("true")) {
					cmd.buzzer(true);
				}else {
					cmd.buzzer(false);
				}
			}

		} catch (IOException | InterruptedException  e) {
			ErrorHandler eh = (ErrorHandler)context.getSharedObject(Constants.ERROR_HANDLER);
				eh.addError(ErrorType.PI, new ErrorInfo(new FormatStackTrace(e).getFormattedException()));
			logger.log(Level.SEVERE, "Error contacting arduino", e);
		} 
		return retCd;

	}
	/**Turn the buzzer on / off **/
	private void buzzer(boolean on){
		logger.log(Level.CONFIG, "buzzer() : " + on);
		
		if (on){
			sendI2cCommand(BUZZER, "true");
		}else{
			sendI2cCommand(BUZZER, "false");
		}
	}
	/**play random mp3 on / off 
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws IllegalStateException **/
	public void playMp3(boolean on, String mp3File, int volume) throws InterruptedException, IllegalStateException, IOException{
		logger.log(Level.CONFIG, "playMp3() : " + on + " track: " + mp3File);
		if (on) {
			toggleMusicSystem(false, true);
			if (mp3Stream != null) {
				logger.log(Level.CONFIG, "Already streaming, closing stream");
				mp3Stream.writeCommand("q");
				Thread.sleep(100);
				mp3Stream.stop();
			}
			adjustVolume(volume);
			mp3Stream = new Mp3Streaming(mp3File);
			mp3Stream.play();
			
			cmd.turnSpeakerOn();
		}else {
			if (mp3Stream != null) {
				mp3Stream.writeCommand("q");
				Thread.sleep(100);
				mp3Stream.stop();
			}
			cmd.turnSpeakerOff();
			mp3Stream = null;
		}

		logger.log(Level.CONFIG, "playMp3() , end method. " );
			
	}
	/**Play the radio on the user selected frequency 
	 * @throws IOException 
	 * @throws ExecuteException 
	 * @throws InterruptedException **/
	public void playRadio(boolean on, String link, int volume) throws ExecuteException, IOException, InterruptedException{
		logger.log(Level.CONFIG, "playAlarmRadio() : " + on);
		
		if (on) {
			toggleMusicSystem(true, false);
			
			if (streaming != null) {
				logger.log(Level.CONFIG, "Already streaming, closing stream");
				streaming.writeCommand("q");
				Thread.sleep(100);
				streaming.stop();
			}
			
			adjustVolume(volume);
			streaming = new RadioStreaming(link);
			streaming.play();
			
			cmd.turnSpeakerOn();
		}else {
			if (streaming != null) {
				streaming.writeCommand("q");
				Thread.sleep(100);
				streaming.stop();
			}
			cmd.turnSpeakerOff();
			streaming = null;
		}

		logger.log(Level.CONFIG, "playAlarmRadio() , end method. " );

	}		
	private void turnWifiOff() throws InterruptedException{
		logger.log(Level.INFO, "wifiOff() : wifiOn : " + wifiOn);
						
		if (wifiOn){

			Exec e = new Exec();
			e.addCommand("sudo").addCommand("ifconfig").addCommand("wlan0").addCommand("down");
			e.timeout(10000);

			try {
				int ext = e.run();
				
				wifiOn = false;	
				setWifiConnected(false);
				wifiInternetConnected = false;

				context.putSharedObject(Constants.CHECK_INTERNET, CheckWifiStatus.END_WIFI_OFF);

				logger.log(Level.INFO, "Exit code : " + ext + " OUtput: " + e.getOutput());
			} catch (IOException e1) {
				ErrorHandler eh = (ErrorHandler)context.getSharedObject(Constants.ERROR_HANDLER);
  				eh.addError(ErrorType.PI, new ErrorInfo(new FormatStackTrace(e1).getFormattedException()));
				logger.log(Level.SEVERE, "wifiOff() : Error shutting wifi : " , e1);
			}
		}
		
//		autoWifiShutDown(false);//TODO
	}
	/**
	 * Check if the computer has an ip address.
	 * @return
	 */
	private boolean checkIfIpPresent() throws ExecuteException, IOException {
		logger.log(Level.CONFIG, "checkIfIpPresent() ");
		
			if (getIpAddress().contains("192.168")){
				return true;
			}
		
		return false;
	}
	/**
	 * check if can access the internet.. only do it when an Ip address is present
	 */
	private boolean checkIfCanAccessInternet(){
		logger.log(Level.CONFIG, "checkIfCanAccessInternet() ");

		try { 
			URL url = new URL("https://www.google.ca/"); 
			URLConnection connection = url.openConnection(); 
			connection.connect(); 

			return true;
		} 
		catch (Exception e) { 
			return false;
		} 
	}
	private void refreshWifi() throws ExecuteException, IOException {
		logger.log(Level.CONFIG, "refreshWifi() ");
		//wpa_cli -i wlan0 reconfigure
		Exec e = new Exec();
		e.addCommand("sudo").addCommand("wpa_cli").addCommand("-i").addCommand("wlan0").addCommand("reconfigure");

		e.timeout(10000);

		int ext = e.run();

		logger.log(Level.CONFIG, "refreshWifi(), ext: " + ext + "  output: " + e.getOutput());
	}
	
	
	public void autoWifiShutDown(boolean startAutoShutdown) throws InterruptedException {
		logger.log(Level.CONFIG, "autoWifiShutDown");
		
		cancelWifiAutoShutdown();

		if (startAutoShutdown) {
			//wait 3 minute before shutting down WIFI
			wifiShutDown = new Thread(new Runnable() {
				int delay = 180000;
				@Override
				public void run() {
					try {
						if (streaming!= null && streaming.isRadioPlaying()) {
							logger.log(Level.INFO, "Radio is still playing , waiting until turn off or alarm button shutdown pressed");
							while(streaming!= null && streaming.isRadioPlaying()) {
								//check if radio is playing.. if playing , do not turn off the wifi.. or put a timer for 3 hours.
								Thread.sleep(10000);									
							}
							Thread.sleep(10000);
							turnWifiOff();
						}else {
							logger.log(Level.INFO, "no radio playing, waiting 3 min");
							Thread.sleep(delay);
							turnWifiOff();
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			});
			wifiShutDown.start();
		}
	}
	public void cancelWifiAutoShutdown() {
		boolean isRunning = wifiShutDown != null && wifiShutDown.isAlive();
		
		logger.log(Level.CONFIG, "cancelWifiAutoShutdown. Running? " + isRunning);
		if (isRunning){
			wifiShutDown.interrupt();
			logger.log(Level.CONFIG, "autoWifiShutDown: Interrupted wifiShutDown");
		}
	}
	/**
	 * If the radio is requested, tur off mp3 and if mp3 is requested , turn off radio
	 * @param radioRequested
	 * @param mp3Requested
	 * @throws InterruptedException
	 */
	private void toggleMusicSystem(boolean radioRequested, boolean mp3Requested) throws InterruptedException {
		logger.log(Level.CONFIG, " Toggeling music system. Radio: " + radioRequested + "  MP3: " +mp3Requested);
		
		
		//if radio requested, turn off MP3.
		if (radioRequested) {
			if (mp3Stream != null) {
				
				mp3Stream.writeCommand("q");
				Thread.sleep(100);
				mp3Stream.stop();
				context.sendMessage(Constants.MUSIC_TOGGELED, new Message("mp3off"));
			}
		}else if (mp3Requested) {
		//if MP3 requested, turn off Radio
			if (streaming != null) {
				logger.log(Level.CONFIG, "closing radio");
				streaming.writeCommand("q");
				Thread.sleep(100);
				streaming.stop();
				//send message that the radio was running and then turned off
				context.sendMessage(Constants.MUSIC_TOGGELED, new Message("radiooff"));
			}
		}
		
	}
}
