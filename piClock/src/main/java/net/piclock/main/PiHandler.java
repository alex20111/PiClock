package net.piclock.main;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.wiringpi.SoftPwm;

import home.misc.Exec;
import net.piclock.button.AlarmBtnHandler;
import net.piclock.enums.Buzzer;
import net.piclock.main.Constants;
import net.piclock.enums.DayNightCycle;
import net.piclock.enums.Light;
import net.piclock.swing.component.SwingContext;

//TODO fetch Wifi list
//TODO add connect to WIFI
//TODO add disconnect to WIFI
//TODO add call to Ard
//TODO add wifi turn off and on (Not disconnect)
//TODO add buzzer handler ARD
//TODo add check internet connection
//https://computers.tutsplus.com/articles/using-a-usb-audio-device-with-a-raspberry-pi--mac-55876
//https://die-antwort.eu/techblog/2017-12-raspberry-pi-usb-audio-interface-command-line/
public class PiHandler {
	private static final Logger logger = Logger.getLogger( PiHandler.class.getName() );

	private static PiHandler piHandler;
	
	private  boolean screenOn = true;
	private  boolean wifiConnected = false; //if connected to the WIFI.
	private  boolean wifiInternetConnected = false; //if connected to the internet.	
	private  boolean isScreenAutoShutdown = false; //if screen is in auto shutdown mode.
	
	private  boolean 	wifiOn = true; //initially the wifi is always ON
	
	private  Thread wifiShutDown;
	private  Thread screenAutoShutDown;
	private  Thread checkConnection; 	
	
	private  AlarmBtnHandler  buttonHandler;	
	
	//commands to ard
	private  String command = "";
	private  String BUZZER = "buzzer";
	private  String LDR 	 = "ldr";
	private  String TIME 	 = "time";
	
	private PiHandler(){
		buttonHandler = new AlarmBtnHandler();
//		buttonHandler.listerToButton();
		System.out.println("Init");
	}
	
	public static PiHandler getInstance() {
		if (piHandler == null) {
			synchronized (PiHandler.class) {
				if(piHandler == null) {
					piHandler = new PiHandler();
				}
			}
		}
		return piHandler;
	}	

	public void turnOffScreen() throws InterruptedException{
		logger.log(Level.CONFIG,"turnOffScreen()");
		
		if (wifiShutDown != null && wifiShutDown.isAlive()){
			wifiShutDown.interrupt();
			while(wifiShutDown.isAlive()){
				wifiShutDown.join(100);
			}
			logger.log(Level.CONFIG, "turnOffScreen: Interrupted wifiShutDown");
		}
		
		//wait 3 minute before shutting down WIFI
		wifiShutDown = new Thread(new Runnable() {				
			@Override
			public void run() {
				try {
					Thread.sleep(180000);
					wifiOff();
				} catch (InterruptedException e) {}
				
			}
		});
		wifiShutDown.start();
		
		setScreenOn(false);
		setBrightness(Light.DARK);
	}
	/*withWifiOn: then turn on the wifi on request*/
	public void turnOnScreen(boolean withWifiOn) throws InterruptedException{
		logger.log(Level.CONFIG,"Turning on screen. Wifi on option: " + withWifiOn);
		
		//interrupt wifi shutdown if in process because screen turned back on.
		if (wifiShutDown != null && wifiShutDown.isAlive()){
			wifiShutDown.interrupt();
			while(wifiShutDown.isAlive()){
				wifiShutDown.join(100);
			}
			logger.log(Level.CONFIG, "turnOnScreen: Interrupted wifiShutDown");
		}
		
		//if screen is auto shutting down and there is a request by the LDR to turn it back on, kill it.
		cancelScreenAutoShutdown();
		
		if (withWifiOn){
			turnWifiOn();
		}

		setScreenOn(true);
		setBrightness(Light.VERY_BRIGHT);

	}
	public List<String> fetchWifiList(){
		logger.log(Level.CONFIG,"fetchWifiList() ");
		List<String> wifiList = new ArrayList<String>();
		wifiList.add("bob");
		wifiList.add("Long do");
		wifiList.add("Long do da");
		//TODO call process builder
		return wifiList;
		
	}
	/**When 1st connecting to a new WIFI*/
	public void connectToWifi(String wifiName, String pass) throws InterruptedException{
		logger.log(Level.CONFIG,"connectToWifi()");
		//TODO write exec
		System.out.println("trying to connect to WIFI: " + wifiName + "  With pass: " + pass);
		
		if (wifiName != null && wifiName.length() > 0 && pass != null && pass.length() > 0){			
			checkInternetConnection();
		}		
		
	}
	public Light getLDRstatus(){
		//TODO add call to ard
		DayNightCycle cycle = null;
		
		Random r = new Random();
		
		sendI2cCommand();
		
		int rand = r.nextInt(255);
		
		if (rand > 200){
			cycle = DayNightCycle.NIGHT;
		}else{
			cycle = DayNightCycle.DAY;
		}
		
		return Light.setLightLevel(rand);
	}
	/**Turn on the alarm based on the selected buzzer**/
	public void turnOnAlarm(Buzzer buzzerType){
		logger.log(Level.CONFIG,"Turning on: " + buzzerType.getName());
		System.out.println("BUZZZZZZZZZZZZZZZZZZZWE");
		if (buzzerType == Buzzer.BUZZER){
			buzzer(true);
		}else if (buzzerType == Buzzer.RADIO){
			playAlarmRadio(true);
		}else if (buzzerType == Buzzer.MP3){
			playAlarmMp3(true);
		}
	}
	public void turnOffAlarm(Buzzer buzzerType){
		logger.log(Level.CONFIG,"Turning off: " + buzzerType.getName());
		if (buzzerType == Buzzer.BUZZER){
			buzzer(false);
		}else if (buzzerType == Buzzer.RADIO){
			playAlarmRadio(false);
		}else if (buzzerType == Buzzer.MP3){
			playAlarmMp3(false);
		}
	}
	public void displayTM1637Time(String time){
		
	}
	public void turnOffTM1637Time(){
		
	}
	
	//turn off the screen automatically
	public void autoShutDownScreen() throws InterruptedException{
		logger.log(Level.CONFIG, "autoShutDownScreen.");		
	
		cancelScreenAutoShutdown();	
		
		screenAutoShutDown = new Thread(new Runnable() {				
			@Override
			public void run() {
				isScreenAutoShutdown = true;
				try {
					Thread.sleep(60000);
					turnOffScreen();
				} catch (InterruptedException e) {}
				
			}
		});
		screenAutoShutDown.start();
	}
	public boolean isAutoShutdownInProgress() {
		return (screenAutoShutDown != null && screenAutoShutDown.isAlive() ? true : false);
	}
	/**check if connected to the internet
	 * @throws InterruptedException **/
	public void checkInternetConnection() throws InterruptedException{
	
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
			@Override
			public void run() {
				SwingContext context = SwingContext.getInstance();	
				try {
						
					context.putSharedObject(Constants.CHECK_INTERNET, "starting");					
					boolean hasInternet = false;
					boolean hasIp = false;
					
					while(keepRunning){
						
						if (!hasIp){							
							hasIp = checkIfIpPresent();
						}
						
						if (count == 15 && !hasIp && !hasInternet){
							context.putSharedObject(Constants.CHECK_INTERNET, "end_timeout");
							keepRunning = false;
						}else if (count == 10 && hasIp && !hasInternet){
							setWifiConnected(true);							
							wifiInternetConnected = false;
							context.putSharedObject(Constants.CHECK_INTERNET, "end_no_inet");
							keepRunning = false;
						}
						else if (hasIp && hasInternet){
							setWifiConnected(true);							
							wifiInternetConnected = true;
							context.putSharedObject(Constants.CHECK_INTERNET, "end_success");
							keepRunning = false;
						}else if (hasIp){
							//check for internet
							hasInternet = checkIfCanAccessInternet();							
						}
						
						count ++;
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {					
					context.putSharedObject(Constants.CHECK_INTERNET, "end_interrupted");
					keepRunning = false;
				} catch (SocketException e) {
					context.putSharedObject(Constants.CHECK_INTERNET, "end_timeout");
					keepRunning = false;
					logger.log(Level.SEVERE, "Error connecting to wifi", e);
				}
				
			}
		});
		checkConnection.start();
	}
	public void disconnectWifi(){
		logger.log(Level.CONFIG, "disconnectWifi()");
		SwingContext context = SwingContext.getInstance();
		context.putSharedObject(Constants.CHECK_INTERNET, "end_disconnect");
		setWifiConnected(false);
		wifiInternetConnected = false;
		//TODO add disconnect
	}
	/** turn wifi card ON. This does not mean that we are connected to it, it just turn it ON**/
	public void turnWifiOn() throws InterruptedException{
		logger.log(Level.CONFIG, "wifiOn(). wifiOn : " + wifiOn);
		if (!wifiOn){
			wifiOn = true;	
//			Exec exec = new Exec();
//			exec.addCommand("ifconfig").addCommand("wlan0").addCommand("up").timeout(10, TimeUnit.SECONDS);
////			exec.run(); //TODO enable.
			SwingContext context = SwingContext.getInstance();
			Preferences p = (Preferences)context.getSharedObject(Constants.PREFERENCES);
			
			if (p.getWifi() != null && p.getWifi().length() > 0 
					&& p.getWifiPass() != null && p.getWifiPass().length() > 0){				
				checkInternetConnection(); //chekc if internet connection is still working
			}
			
			
		}
	}
	//verify if the monitor is actually ON of OFF	
	public boolean isMonitorOn() {
		//TODO
		return true;
	}
	public void setBrightness(Light light) {
		logger.log(Level.CONFIG, "setBrightness : " + light);
		System.out.println("Setting brightness");
	}
	private void cancelScreenAutoShutdown() throws InterruptedException{
		if (screenAutoShutDown != null && screenAutoShutDown.isAlive()){
			screenAutoShutDown.interrupt();
			while(screenAutoShutDown.isAlive()){
				screenAutoShutDown.join(100);
			}
			screenAutoShutDown = null;
			isScreenAutoShutdown = false;
			logger.log(Level.CONFIG, "Interrupted screenAutoShutDown");
		}
	}
	private synchronized void sendI2cCommand(){
		
		if (command.equals(TIME)){
			//TODO create class ard controller
		}		
		
	}
	/**Turn the buzzer on / off **/
	private void buzzer(boolean on){
		logger.log(Level.CONFIG, "buzzer() : " + on);
		
		if (on){
			//turn buzzer On
		}else{
			//turn off
		}
	}
	/**play random mp3 on / off **/
	private void playAlarmMp3(boolean on){
		logger.log(Level.CONFIG, "playAlarmMp3() : " + on);
		if (on){
			//play random MP3
		}else{
			//off
		}			
	}
	/**Play the radio on the user selected frequency **/
	private void playAlarmRadio(boolean on){
		logger.log(Level.CONFIG, "playAlarmRadio() : " + on);
		if (on){
			//play radio
		}else{
			//off
		}
			
	}		
	private void wifiOff(){
		logger.log(Level.CONFIG, "wifiOff() : wifiOn : " + wifiOn);
		if (wifiOn){
			System.out.println("Turn off wifi - 2");
			//TODO call process builder
			wifiOn = false;	
			setWifiConnected(false);
			wifiInternetConnected = false;
			SwingContext context = SwingContext.getInstance();
			context.putSharedObject(Constants.CHECK_INTERNET, "end_wifiOff");
			
			Exec exec = new Exec();
			exec.addCommand("ifconfig").addCommand("wlan0").addCommand("down").timeout(10); //TODO add time unit
//			exec.run(); //TODO enable
		}
	}
	/**
	 * Check if the computer has an ip address.
	 * @return
	 * @throws SocketException
	 */
	private boolean checkIfIpPresent() throws SocketException{
		logger.log(Level.CONFIG, "checkIfIpPresent() ");
		Enumeration<NetworkInterface> eni = NetworkInterface.getNetworkInterfaces();
      while(eni.hasMoreElements()) {        	
          Enumeration<InetAddress> eia = eni.nextElement().getInetAddresses();
          
          while(eia.hasMoreElements()) {
              InetAddress ia = eia.nextElement();
              if (!ia.isAnyLocalAddress() && !ia.isLoopbackAddress() && !ia.isSiteLocalAddress()) {
                  if (!ia.getHostName().equals(ia.getHostAddress()))
                  	logger.log(Level.CONFIG, "IP present? HostName: " + ia.getHostName() + ". HostAddress: " + ia.getHostAddress() );
                  	return true;
              }
          }
      }
      return false;
	}
	/**
	 * check if can access the internet.. only do it when an Ip address is present
	 */
	private boolean checkIfCanAccessInternet(){
		logger.log(Level.CONFIG, "checkIfCanAccessInternet() ");
//		https://unix.stackexchange.com/questions/190513/shell-scripting-proper-way-to-check-for-internet-connectivity
		//TODO add it to a script
//			if ping -q -c 1 -W 1 google.com >/dev/null; then
//			  echo "The network is up"
//			else
//			  echo "The network is down"
//			fi
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
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
	public  boolean isScreenAutoShutdown() {
		return isScreenAutoShutdown;
	}
	public  void setScreenAutoShutdown(boolean isScreenAutoShutdown) {
		this.isScreenAutoShutdown = isScreenAutoShutdown;
	}
}
