package net.piclock.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.main.Constants;
import net.piclock.swing.component.SwingContext;

public class ImageUtils {
	private static final Logger logger = Logger.getLogger( ImageUtils.class.getName() );
	private static ImageUtils imageUtils = new ImageUtils();
			
	private ImageIcon weatherNA ;
	private ImageIcon weatherAlertIcon;
	private ImageIcon weatherLoader;
	private ImageIcon buttonLoader;
	private ImageIcon warningIcon;
	
	public static ImageUtils getInstance(){
		return imageUtils;
	}
	
	private ImageUtils(){
		try{
			weatherNA = getImage("NotAvailable.png");
			weatherAlertIcon = getImage("weather-alert-48.png");
			warningIcon = getImage("warning-48.png");
			weatherLoader = getAnimatedImage("loader-weather.gif");
			buttonLoader = getAnimatedImage("loader-btn.gif");
			
			
		}catch(Exception e){
			ErrorHandler eh = (ErrorHandler)SwingContext.getInstance().getSharedObject(Constants.ERROR_HANDLER);
			eh.addError(ErrorType.GENERAL, new ErrorInfo(new FormatStackTrace(e).getFormattedException()));
			logger.log(Level.SEVERE, "Error loading images", e);
		}
	}
		
	/**
	 * Get the required image.. if not found fall back on the default image if any.
	 * @param imgName
	 * @param defaultImage
	 * @return
	 * @throws IOException
	 */
	public ImageIcon getImage(String imgName, int height, int width) throws IOException{
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File ("img" + File.separatorChar + imgName));
			
			if (height > 0 && width > 0) {
				Image dimg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
				return new ImageIcon(dimg);
			}
			
			
		} catch (IOException e) {
			img = ImageIO.read(new File ("img" + File.separatorChar + Constants.NA_ICON));
		}
		return new ImageIcon(img);
	}
	
	public ImageIcon getImage(String imgName ) throws IOException{
		return getImage(imgName, 0, 0);
	}
	
	public ImageIcon getAnimatedImage(String imgName) {
		return new ImageIcon("img"+ File.separatorChar + imgName);
	}

	public ImageIcon getWeatherNA() {
		return weatherNA;
	}

	public ImageIcon getWeatherAlertIcon() {
		return weatherAlertIcon;
	}

	public ImageIcon getWeatherLoader() {
		return weatherLoader;
	}

	public ImageIcon getButtonLoader() {
		return buttonLoader;
	}

	public ImageIcon getWarningIcon() {
		return warningIcon;
	}
	

		
}
