package net.piclock.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LogConfig {

//	OFF SEVERE	WARNING	INFO CONFIG FINE FINER FINEST ALL
	public void configLogs(String logFileName,Level baseLevel, boolean consolDisplayOn,  boolean fileLogOn) throws SecurityException, IOException{
		LogManager.getLogManager().reset();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");		

		Logger l = Logger.getLogger("");

		if (fileLogOn){		
			FileHandler fh = new FileHandler(logFileName + sdf.format(new Date()), false);	
			fh.setFormatter(new OneLineFormatter());
			fh.setLevel(baseLevel);
			l.addHandler(fh);				 
		}
		l.setUseParentHandlers(false);
		
		if (consolDisplayOn){
			ConsoleHandler ch = new ConsoleHandler();
			ch.setFormatter(new OneLineFormatter());
			ch.setLevel(baseLevel);
			l.addHandler(ch);	
		}
		l.setLevel(baseLevel);
	}
}