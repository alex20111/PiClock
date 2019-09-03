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

public class Mp3Streaming {

	private static final Logger logger = Logger.getLogger( Mp3Streaming.class.getName() );

	private String mp3Link = "";
	private Process process;
	private BufferedWriter out;

	private StringBuilder output;
	private boolean processFinished = false;
	private SwingContext ct;

	private boolean mp3Playing = false;

	public Mp3Streaming(String link) {
		processFinished = false;
		mp3Playing = false;
		mp3Link = "/home/pi/piClock/mp3/" + link;
		ct = SwingContext.getInstance();
	}

	public void play() throws IOException {
		//omxplayer file.mp3 --vol -1000 -o alsa:hw:1,0
		logger.log(Level.CONFIG, "PLAY - MP3 File: " + mp3Link);
		final ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("omxplayer", mp3Link,"--vol","-1000", "-o","alsa:hw:1,0");
		process = processBuilder.start();

		mp3Playing = true;
		out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));		

		new Thread(() -> {
			logger.log(Level.CONFIG, "Starting process builder output thread");
			output = new StringBuilder();
			String line;
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			try{				
				boolean playNext = false;
				while((line = br.readLine()) != null) {
					output.append(line);
					if (!playNext) {
						if (line != null && line.contains("Audio codec") && !playNext) {
							playNext = true;
							logger.log(Level.CONFIG, "Play next music");
							ct.sendMessage(Constants.MP3_PLAY_NEXT, new Message("play Next mp3" + line));

						}else if (line != null && line.contains("have a nice day")) {
							logger.log(Level.CONFIG, "Throwing error message: " + line);
							ct.sendMessage(Constants.MP3_STREAM_ERROR, new Message("Mp3 Stream not found. Message: " + line));
							mp3Playing = false;
							break;
						}else if(line != null && line.contains("File")) {
							logger.log(Level.CONFIG, "Throwing error message: " + line);
							ct.sendMessage(Constants.MP3_STREAM_ERROR, new Message("Mp3 Stream file not found. Message: " + line));
							mp3Playing = false;
							break;
						}
					}

					logger.log(Level.CONFIG,"Output recived from mp3Streaming" + line);
				}
			} catch (IOException e) {
				mp3Playing = false;
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
		mp3Playing = false;
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
		logger.log(Level.CONFIG, "mp3 STOP BREAK, COUNT:"+ count);
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

	public boolean isMp3Playing() {
		return mp3Playing;
	}
}
