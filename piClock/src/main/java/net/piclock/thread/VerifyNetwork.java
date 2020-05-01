package net.piclock.thread;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import home.misc.Exec;
import net.piclock.enums.CheckWifiStatus;
import net.piclock.handlers.PiHandler;
import net.piclock.main.Constants;
import net.piclock.swing.component.SwingContext;

public class VerifyNetwork implements Runnable {

	private static final Logger logger = Logger.getLogger( VerifyNetwork.class.getName() );

	private PiHandler handler;

	@Override
	public void run() {
		logger.log(Level.INFO,"!!!!!!Checking IP!!!!");

		try {

			handler = PiHandler.getInstance();

			if (handler.isWifiOn()) {


				int retries = 0;

//				Exec exec = new Exec();

				//				exec.addCommand("sudo").addCommand("/home/pi/piClock/scripts/getIp.sh").timeout(1000);
				//				exec.run();

				//				exec.addCommand("sudo").addCommand("/home/pi/piClock/scripts/getIp.sh").timeout(1000);
				//				exec.run();
				//				String ip = exec.getOutput();
				//				
				//				logger.log(Level.CONFIG, "IP: " +ip);

				logger.log(Level.CONFIG, lookForIp());

				boolean connectingToWifi = ((CheckWifiStatus)SwingContext.getInstance().getSharedObject(Constants.CHECK_INTERNET)).isConnecting();
				while(!handler.checkIfIpPresent() && retries < 3 && !connectingToWifi) {				
					retries ++;
					//					Exec exec = new Exec();
					//
					//					exec.addCommand("sudo").addCommand("/home/pi/piClock/scripts/getIp.sh").timeout(1000);
					//					exec.run();
					//					String ip = exec.getOutput();

					logger.log(Level.INFO, "Wifi is on but not connected. Trying to connect again !!. Current ip: " + handler.getIpAddress());



					handler.rebootWifi();

					Thread.sleep(20000);



				}	

				if (retries == 3) {
					logger.log(Level.INFO, "Wifi number of reconnect excedeed. retry in next loop");
				}

			}

		} catch (Exception e) {
			logger.log(Level.INFO, "Exception in VerifyNetwork.", e);
		}


		//


	}

	private String lookForIp() {
		StringBuilder sb = new StringBuilder();
		try {
			
			
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

			while (interfaces.hasMoreElements()) {

				NetworkInterface networkInterface = interfaces.nextElement();

				sb.append("getDisplayName:[" + networkInterface.getDisplayName()  + "]  LoopBack:[" +networkInterface.isLoopback() +"]" );
				
//				System.out.println("Network Interface Name : [" + networkInterface.getDisplayName() + "]");
//				System.out.println("Network Interface loop : [" + networkInterface.isLoopback() + "]");

//				if (!networkInterface.isLoopback() &&  networkInterface.isUp()) {
				
				sb.append(" Is Up? " + networkInterface.isUp() );
				
//					System.out.println("Is It connected? : [" + networkInterface.isUp() + "]");

					for (InterfaceAddress i : networkInterface.getInterfaceAddresses()){

						sb.append("  Host Name : "+i.getAddress().getCanonicalHostName());
						sb.append("Host Address : "+i.getAddress().getHostAddress());

					}
//				}
//				System.out.println("----------------------");

			}
		}catch(SocketException se) {
			sb.append("error: " + se.getMessage());
			se.printStackTrace();
		}
		
		return sb.toString();
	}
	
	public static void main(String args[]) {
		VerifyNetwork k = new VerifyNetwork();
		k.lookForIp();
		
	}

}
