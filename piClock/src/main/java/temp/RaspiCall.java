package temp;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.exec.ExecuteException;

import home.fileutils.FileUtils;
import home.misc.Exec;

public class RaspiCall {

	//to add a new network
	// -1 sudo wpa_cli disconnect    OKKK script disconnectWifi.sh
	// 0 sudp wpa_cli remove_network 0   OKKK script disconnectWifi.sh  
	// 0 sudo wpa_cli save_config    //to remove from wpa_supplicant.conf   OKKK script disconnectWifi.sh
	// 1 sudo wpa_cli add_network 0
	// 2 sudo wpa_cli set_network 0 ssid '"slimy-n"'
	// 3 sudo wpa_cli set_network 0 psk '"12345"'
	// 4 sudo wpa_cli enable_network 0
	// 5 sudo wpa_cli save_config
	public static void main(String[] args) throws Exception {
		System.out.println("Starting");
		//bring wireless card up    good
		//bring wireless card down  good
		//connect to new network
		//disconnect from network
		//re-connect to network
		//scan ssid     good

		//wpa_cli set_network 0 ssid '"slimy-n"'  --works
		//		checkIfConnected();

		//		bringNetworkCardDown();
		//		Thread.sleep(10000);
		//		bringNetworkCardUp();

		//		addNetwork();
		scanSSId();
		Thread.sleep(2000);
		System.out.println("Ip exist: " + verifyLocalIpExist());
		System.out.println("Turn off monitor");
		turnMonitorOff();
		Thread.sleep(1000);
		checkMonitorStatus();
		Thread.sleep(1000);
		turnMonitorOn();
		checkMonitorStatus();
		Thread.sleep(3000);
		System.out.println("Starting network tests");
		removeWifiNetwork();
		refreshWifi();
		Thread.sleep(15000);
		addWifiNetwork("slimy-n", "1A384Xkl81#");
		refreshWifi();
		while(!verifyLocalIpExist()) {
			Thread.sleep(1000);
			System.out.println("NoIp yet");
		}
		System.out.println("Ye found ip");
		Thread.sleep(1000);
		System.out.println("Now turning of wifi");
		bringNetworkCardDown();
		Thread.sleep(2000);
		System.out.println("Briging card up");
		bringNetworkCardUp();
		while(!verifyLocalIpExist()) {
			Thread.sleep(1000);
			System.out.println("NoIp yet");
		}
		System.out.println("Ye found ip AGAIN");
		System.out.println("Check if internet connection");
		checkIfConnected();
		
		

	}

	private static void checkIfConnected() {

		try { 
			URL url = new URL("https://www.google.ca/"); 
			URLConnection connection = url.openConnection(); 
			connection.connect(); 

			System.out.println("Connection Successful"); 
		} 
		catch (Exception e) { 
			System.out.println("Internet Not Connected"); 
		} 
	}

	public static void bringNetworkCardDown() throws ExecuteException, IOException {
		Exec e = new Exec();
		e.addCommand("sudo").addCommand("ifconfig").addCommand("wlan0").addCommand("down");

		e.timeout(10000);

		int ext = e.run();

		System.out.println("bringNetworkCardDown: output: " + e.getOutput() + "exit value: " + ext);

	}

	public static void bringNetworkCardUp() throws ExecuteException, IOException {
		Exec e = new Exec();
		e.addCommand("sudo").addCommand("ifconfig").addCommand("wlan0").addCommand("up");

		e.timeout(10000);

		int ext = e.run();

		System.out.println("bringNetworkCardup: output: " + e.getOutput() + "exit value: " + ext);

	}


	//sudo iw dev wlan0 scan | grep SSID
	public static void scanSSId() throws Exception {
		//sudo iwlist wlan0 scan | grep -i essid | awk -F'"' '{ print $2 }' >> essid.txt
		Exec e = new Exec();
		e.addCommand("sudo").addCommand("./testScripts/scanssid.sh").addCommand("/home/pi/testScripts/essid.txt");


		e.timeout(10000);

		int ext = e.run();

		System.out.println("scanSSId: output: " + e.getOutput() + "exit value: " + ext);

		if (ext == 0) {
			List<String> essid = FileUtils.readFileToArray("/home/pi/testScripts/essid.txt");

			System.out.println("SSID: " + essid);
		}

	}
	public static void turnMonitorOff() throws ExecuteException, IOException {
		//DISPLAY=:0.0 xset dpms force off
		Exec e = new Exec();
		e.addCommand("sudo").addCommand("DISPLAY=:0.0").addCommand("xset").addCommand("dpms").addCommand("force").addCommand("off");

		e.timeout(10000);

		int ext = e.run();
	}
	public static void turnMonitorOn() throws ExecuteException, IOException {
		//DISPLAY=:0.0 xset dpms force off
		Exec e = new Exec();
		e.addCommand("sudo").addCommand("DISPLAY=:0.0").addCommand("xset").addCommand("dpms").addCommand("force").addCommand("on");

		e.timeout(10000);

		int ext = e.run();
	}
	private static void removeWifiNetwork() throws IOException {
		System.out.println("remove");

		Path wpaFile = Paths.get("/etc/wpa_supplicant/wpa_supplicant.conf");
		String contents = new String(Files.readAllBytes(wpaFile));

		String noWifi = contents.substring(0,contents.indexOf("network"));

		//		System.out.println(noWifi);


		byte[] strToBytes = noWifi.getBytes();

		Files.write(wpaFile, strToBytes);

	}

	private static void addWifiNetwork( String ssid, String pass) throws IOException {
		String newNetwork = "\n\nnetwork={\n\tssid=\"" + ssid +"\"\n\tpsk=\""+pass+"\"\n\tkey_mgmt=WPA-PSK\n} ";

		Path wpaFile = Paths.get("/etc/wpa_supplicant/wpa_supplicant.conf");
		String contents = new String(Files.readAllBytes(wpaFile));

		String newString = contents.concat(newNetwork);

		System.out.println("add");
		byte[] strToBytes = newString.getBytes();
		Files.write(wpaFile, strToBytes);



	}
	private static void refreshWifi() throws ExecuteException, IOException {
		//wpa_cli -i wlan0 reconfigure
		Exec e = new Exec();
		e.addCommand("sudo").addCommand("wpa_cli").addCommand("-i").addCommand("wlan0").addCommand("reconfigure");

		e.timeout(10000);

		int ext = e.run();

		System.out.println("Refresh exit code: " + ext);


	}
	private static void checkMonitorStatus() throws ExecuteException, IOException {

		Exec exec = new Exec();
		exec.addCommand("sudo").addCommand("xset").addCommand("q");

		exec.run();

		String output = exec.getOutput().toLowerCase();

		if (output.contains("monitor is on")){
			System.out.println("Monitor is ON");
		}else if (output.contains("monitor is off")){
			System.out.println("Monitor is off");
		}else {
			System.out.println("Wrong output");
		}
	}

	private static boolean verifyLocalIpExist() {


		try {
			Exec exec = new Exec();

			exec.addCommand("hostname").addCommand("--all-ip-addresses").timeout(5000);

			exec.run();

			if (exec.getOutput().contains("192.168")){
				return true;
			}
		}catch (Exception ex) {
			ex.printStackTrace();

		}

		return false;
	}

	enum Monitor{
		ON,OFF;
	}
}
