package net.piclock.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.piclock.main.Constants;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.SwingContext;

public class RadioStreaming {
	
	private static final Logger logger = Logger.getLogger( RadioStreaming.class.getName() );
	
	private String httpLink = "";
	private Process process;
	private BufferedWriter out;
	
	private StringBuilder output;
	private boolean processFinished = false;
	private SwingContext ct;
	
	private boolean radioPlaying = false;
	
	public RadioStreaming(String link) {
		processFinished = false;
		radioPlaying = false;
		httpLink = link;
		ct = SwingContext.getInstance();
	}
	
	public void play() throws IOException {
		logger.log(Level.CONFIG, "PLAY - Http link: " + httpLink);
		final ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("omxplayer", httpLink,"--vol","-1000", "-o","alsa:hw:1,0");
		process = processBuilder.start();
		
		radioPlaying = true;
		out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));		
		
		new Thread(() -> {
			logger.log(Level.CONFIG, "Starting process builder output thread");
			output = new StringBuilder();
			String line;
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			try{				
				
				while((line = br.readLine()) != null) {
					output.append(line);					
					if (line != null && line.contains("have a nice day")) {
						logger.log(Level.CONFIG, "Throwing error message: " + line);
						ct.sendMessage(Constants.RADIO_STREAM_ERROR, new Message("Radio Stream not found. Message: " + line));
						radioPlaying = false;
						break;
					}
				}
			} catch (IOException e) {
				radioPlaying = false;
				logger.log(Level.INFO, "error in output - can be normal", e);
			}finally {
				try {
					br.close();

				}catch (IOException i) {
					logger.log(Level.SEVERE, "Don't know what happend", i);
				}
			}
			processFinished = true;
			logger.log(Level.CONFIG, "ENDING process builder output thread");
		}).start();
	}

	public void stop()  {
		logger.log(Level.CONFIG, "STOP");
		int count = 0;
		radioPlaying = false;
		try {
			out.close();
		} catch (IOException e) {
			logger.log(Level.INFO, "Already closed", e);
		}
		process.destroyForcibly();
		
		while(!processFinished) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				break;
			}
			if (count > 20) {
				break;
			}
			count++;
		}
		logger.log(Level.CONFIG, "RADIO STOP BREAK, COUNT:"+ count);
	}
	
	public void writeCommand(String command)  {
		logger.log(Level.CONFIG, "Write COmmand: " + command);
		try {
			out.write(command);
			out.flush();
		} catch (IOException e) {
			logger.log(Level.INFO, "error writing, closing process", e);
			process.destroyForcibly();
			
		}
	}
	
	public String getOutput() {
		return (output != null ? output.toString() : "");
	}

	public boolean isRadioPlaying() {
		return radioPlaying;
	}
}
