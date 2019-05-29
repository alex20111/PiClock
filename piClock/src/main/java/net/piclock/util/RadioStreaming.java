package net.piclock.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RadioStreaming {
	
	private static final Logger logger = Logger.getLogger( RadioStreaming.class.getName() );
	
	private String httpLink = "";
	private Process process;
	private BufferedWriter out;
	
	private StringBuilder output;
	
	public RadioStreaming(String link) {
		httpLink = link;
	}
	
	public void play() throws IOException {
		logger.log(Level.CONFIG, "PLAY - Http link: " + httpLink);
		final ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("omxplayer", httpLink, "-o","alsa:hw:1,0");
		process = processBuilder.start();
		
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
					System.out.println("---------->>>> " +line);
					output.append(line);
				}
			} catch (IOException e) {
				logger.log(Level.INFO, "error in output", e);
			}finally {
				try {
					

					br.close();

				}catch (IOException i) {
					i.printStackTrace();
				}
			}
			logger.log(Level.CONFIG, "ENDING process builder output thread");
		}).start();
	}

	public void stop() throws IOException {
		logger.log(Level.CONFIG, "STOP");
		out.close();
		process.destroyForcibly();
	
	}
	
	public void writeCommand(String command) throws IOException {
		logger.log(Level.CONFIG, "Write COmmand: " + command);
		out.write(command);
		out.flush();
	}
	
	public String getOutput() {
		return (output != null ? output.toString() : "");
	}
}
