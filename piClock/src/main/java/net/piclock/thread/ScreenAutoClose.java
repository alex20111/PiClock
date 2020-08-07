package net.piclock.thread;

import java.awt.CardLayout;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import net.piclock.main.Constants;

public class ScreenAutoClose implements Runnable {

	private static Thread autoCloseThread;
	private JPanel cardPanel;
	private TimeUnit timeUnit;
	private long delay = 0l;
	
	private ScreenAutoClose( JPanel cardPanel , int delay, TimeUnit timeUnit){
		this.cardPanel = cardPanel;
		this.delay = delay;
		this.timeUnit = timeUnit;
	}
	
	@Override
	public void run() {

		try {
			timeUnit.sleep(delay);
			CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
			cardLayout.show(cardPanel, Constants.MAIN_VIEW);
		
		} catch (InterruptedException e) {
		}
	}
	
	public static synchronized void start(JPanel panel , int delayTime, TimeUnit timeUnit){
		
			
		autoCloseThread = new Thread(new ScreenAutoClose( panel ,  delayTime,  timeUnit));
		
		autoCloseThread.start();
		
	}
	public static synchronized void stop() throws InterruptedException{
		
		if (autoCloseThread != null && autoCloseThread.isAlive()){
			autoCloseThread.interrupt();
			while(autoCloseThread.isAlive()){
				autoCloseThread.join(100);
				
			}		
		}		
	}
}
