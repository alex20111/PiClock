package net.piclock.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.nanohttpd.protocols.http.NanoHTTPD.ResponseException;
import org.nanohttpd.protocols.http.response.Response;

import home.miniHttp.HttpBase;
import home.miniHttp.HttpHandler;
import home.miniHttp.StaticPageHandler;
import net.piclock.enums.BackgroundEnum;
import net.piclock.main.Constants;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;


public class BackgroundHandler extends HttpBase implements HttpHandler{
	private static final Logger logger = Logger.getLogger( BackgroundHandler.class.getName() );
	
	private  String BACKGROUND_PAGE = "themePage";

	private String btnSave = "";
	private String dirPath = "/home/pi/piClock/img/";

	private  String SUNNY 		= "sunny-day-background.jpg";
	private  String SUNNY_NIGHT = "clear-sky-night.jpg";
	private  String CLOUDY 		= "clouds.jpg";
	private  String CLOUDY_NIGHT = "cloudy-night.jpg";		
	private  String RAIN 		= "rain-orig-480.jpg";
	private  String RAIN_NIGHT 	= "rain-night.jpg";
	private  String FOG 		= "fog.jpg";
	private  String FOG_NIGHT 	= "fog-night.jpg";
	private  String THUNDER 	= "thunder.jpg";
	private  String THUNDER_NIGHT 	= "thunder-night.jpg";
	private  String SNOW 		= "snowing.jpg";
	private  String SNOW_NIGHT 	= "snowy-night.png";


	private String message = "";
	private String errorMessage = "";

	@Override
	public Response handleRequest() {
		try {
			String webPage = "Page not found";//default message
			message = "";
			errorMessage = "";

			if (btnSave != null && btnSave.length()> 0 && gotFiles()){
				//save file
				saveAndResize();
				
				if (errorMessage.length() == 0){
					message = "Save success";
					//TODO fire theme refresh..
				}
			}

			String img1 = encodeImage(new File(dirPath +SUNNY));
			String img2 = encodeImage(new File(dirPath + SUNNY_NIGHT));
			String img3 = encodeImage(new File(dirPath + CLOUDY));
			String img4 = encodeImage(new File(dirPath + CLOUDY_NIGHT));
			String img5 = encodeImage(new File(dirPath + RAIN));
			String img6 = encodeImage(new File(dirPath + RAIN_NIGHT));
			String img7 = encodeImage(new File(dirPath + FOG));
			String img8 = encodeImage(new File(dirPath + FOG_NIGHT));
			String img9 = encodeImage(new File(dirPath + THUNDER));
			String img10 = encodeImage(new File(dirPath + THUNDER_NIGHT));
			String img11 = encodeImage(new File(dirPath + SNOW));
			String img12 = encodeImage(new File(dirPath + SNOW_NIGHT));


			//create main page with values
			List<File> webPageFiles = getWebPageOnDisk(BACKGROUND_PAGE);

			if (webPageFiles != null && webPageFiles.size() > 0){
				Map<String, String> values = new HashMap<String, String>();
				values.put("img1", img1);
				values.put("img2", img2);
				values.put("img3", img3);
				values.put("img4", img4);
				values.put("img5", img5);
				values.put("img6", img6);
				values.put("img7", img7);
				values.put("img8", img8);
				values.put("img9", img9);
				values.put("img10", img10);
				values.put("img11", img11);
				values.put("img12", img12);
				
				if (message.length() > 0){
					message = generateSuccessMessage(message);
				}else {
					message = "";
				}
				values.put("message", message);
				if (errorMessage.length() > 0){
					errorMessage = generateErrorMessage(errorMessage);
				}else{
					errorMessage = "";
				}
				values.put("errors", errorMessage);
				webPage =	StaticPageHandler.processPage(webPageFiles, values);

			}else{
				webPage = "Web page not found";
			}	

			return Response.newFixedLengthResponse(webPage);
		} catch (Exception e1) {
			logger.log(Level.SEVERE, "error in background handler", e1);
		}
		return Response.newFixedLengthResponse("Error");
	}

	@Override
	public void handleParameters(Map<String, List<String>> params) {
		btnSave = "";
		if (params.get("btnSave") != null && params.get("btnSave").size() > 0){ 
			btnSave = params.get("btnSave").get(0); 			 			
		}
	}

	private void saveAndResize() throws IOException, ResponseException{
		
		StringBuilder sb = new StringBuilder();
		
		boolean fireRefresh = false;
		
		BackgroundEnum currEnum = (BackgroundEnum)SwingContext.getInstance().getSharedObject(Constants.CURRENT_BACKGROUND);
		
		for(Map.Entry<String, String> fileStr : getFiles().entrySet()){
			if (fileStr.getValue() != null && fileStr.getValue().length() > 0){

				String fileFull = getParameters().get(fileStr.getKey()).get(0);
				String formatName = fileFull.substring(fileFull.lastIndexOf(".") + 1);

				if (formatName.length() > 0 &&
						(formatName.equalsIgnoreCase("jpg") || formatName.equalsIgnoreCase("bmp")
								|| formatName.equalsIgnoreCase("jpeg") || formatName.equalsIgnoreCase("gif")
								|| formatName.equalsIgnoreCase("png")) ){

					File tmpFile = new File(fileStr.getValue());
					BufferedImage outputImage = resize(tmpFile, 480, 800);

					//save	
					if (fileStr.getKey().equals("img1File")){									
						ImageIO.write(outputImage, formatName, new File(dirPath + SUNNY) );
						if (currEnum == BackgroundEnum.SUNNY) {
							fireRefresh = true;
						}
					}else if (fileStr.getKey().equals("img2File")){				
						ImageIO.write(outputImage, formatName , new File(dirPath + SUNNY_NIGHT) );
						if (currEnum == BackgroundEnum.SUNNY) {
							fireRefresh = true;
						}
					}else if (fileStr.getKey().equals("img3File")){				
						ImageIO.write(outputImage, formatName , new File(dirPath + CLOUDY) );
						if (currEnum == BackgroundEnum.CLOUDY) {
							fireRefresh = true;
						}
					}else if (fileStr.getKey().equals("img4File")){				
						ImageIO.write(outputImage, formatName , new File(dirPath + CLOUDY_NIGHT) );
						if (currEnum == BackgroundEnum.CLOUDY) {
							fireRefresh = true;
						}
					}else if (fileStr.getKey().equals("img5File")){				
						ImageIO.write(outputImage, formatName , new File(dirPath + RAIN) );
						if (currEnum == BackgroundEnum.RAIN) {
							fireRefresh = true;
						}
					}else if (fileStr.getKey().equals("img6File")){				
						ImageIO.write(outputImage, formatName , new File(dirPath + RAIN_NIGHT) );
						if (currEnum == BackgroundEnum.RAIN) {
							fireRefresh = true;
						}
					}else if (fileStr.getKey().equals("img7File")){				
						ImageIO.write(outputImage, formatName , new File(dirPath + FOG) );
						if (currEnum == BackgroundEnum.FOG) {
							fireRefresh = true;
						}
					}else if (fileStr.getKey().equals("img8File")){				
						ImageIO.write(outputImage, formatName , new File(dirPath + FOG_NIGHT) );
						if (currEnum == BackgroundEnum.FOG) {
							fireRefresh = true;
						}
					}else if (fileStr.getKey().equals("img9File")){				
						ImageIO.write(outputImage, formatName , new File(dirPath + THUNDER) );
						if (currEnum == BackgroundEnum.THUNDER) {
							fireRefresh = true;
						}
					}else if (fileStr.getKey().equals("img10File")){				
						ImageIO.write(outputImage, formatName , new File(dirPath + THUNDER_NIGHT) );
						if (currEnum == BackgroundEnum.THUNDER) {
							fireRefresh = true;
						}
					}else if (fileStr.getKey().equals("img11File")){				
						ImageIO.write(outputImage, formatName , new File(dirPath + SNOW) );
						if (currEnum == BackgroundEnum.SNOW) {
							fireRefresh = true;
						}
					}else if (fileStr.getKey().equals("img12File")){				
						ImageIO.write(outputImage, formatName , new File(dirPath + SNOW_NIGHT) );
						if (currEnum == BackgroundEnum.SNOW) {
							fireRefresh = true;
						}
					}
				}else{
					sb.append("File not the right format: " + fileFull + "\n");
				}
			}
		}
		
		if (sb.length() > 0){
			sb.deleteCharAt(sb.length() - 1);
			errorMessage = sb.toString();
		}else if (fireRefresh) {
			logger.config("Refreshing current background");
			ThemeHandler th = (ThemeHandler)SwingContext.getInstance().getSharedObject(Constants.THEMES_HANDLER);
			th.refreshCurrentBackground();
		}
			
	}

	private String encodeImage(File image) throws IOException {

		BufferedImage outputImage = resize(image, 140, 250);

		// extracts extension of output file
		String formatName = image.getName().substring(image.getName()
				.lastIndexOf(".") + 1);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( outputImage, formatName , baos );
		baos.flush();		

		return DatatypeConverter.printBase64Binary(baos.toByteArray());
	}

	private BufferedImage resize(File imgFile, int height, int width) throws IOException{

		BufferedImage originalImage = ImageIO.read(imgFile);

		BufferedImage newImg = Scalr.resize(originalImage,Mode.FIT_EXACT, width, height);

		return newImg;

	}
}


