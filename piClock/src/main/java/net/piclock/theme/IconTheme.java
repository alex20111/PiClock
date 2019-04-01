package net.piclock.theme;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import net.piclock.enums.DayNightCycle;
import net.piclock.enums.IconEnum;

public class IconTheme {

	private IconEnum name;
	private String imgDayFileName = "";
	private String imgNightFileName = "";
	
	private String imageFolder = "";
	
	public IconTheme(){}
	public IconTheme(IconEnum name, String day, String night, String imageFolder){
		this.name = name;
		this.imgDayFileName = day;
		this.imgNightFileName = night;
		this.imageFolder = imageFolder;
	}	
	
	public IconEnum getName() {
		return name;
	}
	public void setName(IconEnum name) {
		this.name = name;
	}
	public String getImgDayFileName() {
		return imgDayFileName;
	}
	public void setImgDayFileName(String imgDayFileName) {
		this.imgDayFileName = imgDayFileName;
	}
	public String getImgNightFileName() {
		return imgNightFileName;
	}
	public void setImgNightFileName(String imgNightFileName) {
		this.imgNightFileName = imgNightFileName;
	}
	public ImageIcon getImage(DayNightCycle cycle) throws IOException{
		
		BufferedImage img = null;
		if(cycle == DayNightCycle.NIGHT){
			img = ImageIO.read(new File (imageFolder + File.separatorChar + imgNightFileName));
		}else{
			img = ImageIO.read(new File (imageFolder + File.separatorChar + imgDayFileName));
		}
		
		return new ImageIcon(img);
	}
	public String getImageFolder() {
		return imageFolder;
	}
	public void setImageFolder(String imageFolder) {
		this.imageFolder = imageFolder;
	}
	@Override
	public String toString() {
		return "IconTheme [name=" + name + ", imgDayFileName=" + imgDayFileName + ", imgNightFileName="
				+ imgNightFileName + ", imageFolder=" + imageFolder + "]";
	}
}