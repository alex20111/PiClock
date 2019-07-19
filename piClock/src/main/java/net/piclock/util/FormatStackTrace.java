package net.piclock.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class FormatStackTrace {

	private String formattedException = "";
	
	public FormatStackTrace(Throwable exception) {
		StringWriter writer = new StringWriter();
        PrintWriter printWriter= new PrintWriter(writer);
        exception.printStackTrace(printWriter);
        
        formattedException  = writer.toString();
	}
	
	public String getFormattedException() {
		return formattedException;
	}
	
	
}
