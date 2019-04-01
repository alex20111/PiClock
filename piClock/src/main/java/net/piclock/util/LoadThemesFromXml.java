package net.piclock.util;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.piclock.enums.BackgroundEnum;
import net.piclock.enums.IconEnum;
import net.piclock.enums.LabelEnums;
import net.piclock.theme.BackgroundTheme;
import net.piclock.theme.IconTheme;
import net.piclock.theme.LabelTheme;
import net.piclock.theme.ThemeEnum;

public class LoadThemesFromXml {
	
	private static final Logger logger = Logger.getLogger( LoadThemesFromXml.class.getName() );

	public static Map<ThemeEnum, List<BackgroundTheme>> loadThemeFromXml() throws ParserConfigurationException, SAXException, IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{

		Map<ThemeEnum, List<BackgroundTheme>> themeMap = new HashMap<ThemeEnum, List<BackgroundTheme>>();

		File fXmlFile = new File("theme.xml");

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("theme");

		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				if (nNode.hasChildNodes()){

					//theme sname start
					NodeList domList =  nNode.getChildNodes();
					ThemeEnum themeName = ThemeEnum.valueOf(eElement.getAttribute("name"));


					List<BackgroundTheme> backgroundList = new ArrayList<BackgroundTheme>();

					for(int dom = 0; dom < domList.getLength() ; dom++){
						Node nNode2 = domList.item(dom);
						if (nNode2.getNodeType() == Node.ELEMENT_NODE){

							Element domNode = (Element)nNode2;

							String imageFolder = domNode.getAttribute("imageFolder");
							//background							
							BackgroundTheme backTheme = new BackgroundTheme(BackgroundEnum.valueOf(domNode.getAttribute("name")), 
									domNode.getAttribute("image"), imageFolder);

							Map<LabelEnums, LabelTheme> labelMap = new HashMap<LabelEnums, LabelTheme>();
							Map<IconEnum, IconTheme> labelIconMap = new HashMap<IconEnum, IconTheme>();

							//get labels and icons
							getLabelsIcons(nNode2, labelMap, labelIconMap , imageFolder);				

							backTheme.setLabels(labelMap);
							backTheme.setLabelIconMap(labelIconMap); 
							backgroundList.add(backTheme);

						}

						themeMap.put(themeName, backgroundList);
					}
				}
			}
		}

		return themeMap;
	}


	private static void getLabelsIcons(Node nNode2,  Map<LabelEnums, LabelTheme> labelMap,Map<IconEnum, IconTheme> labelIconMap, String imageFolder) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{

		if (nNode2.hasChildNodes()){
			NodeList linkList =  nNode2.getChildNodes();

			for(int lnk = 0; lnk < linkList.getLength() ; lnk++){
				Node lnkNode = linkList.item(lnk);
				if (lnkNode.getNodeType() == Node.ELEMENT_NODE){

					//labels/Icon tag   <label> or <icon>
					Element lnkElee = (Element)lnkNode;
					String tagValue = lnkElee.getAttribute("name");

					if (lnkNode.hasChildNodes()){
						NodeList lbl =  lnkNode.getChildNodes();

						for(int jk = 0 ; jk < lbl.getLength() ; jk++){
							Node jkNode = lbl.item(jk);
							if (jkNode.getNodeType() == Node.ELEMENT_NODE){

								//detail on labels or icons
								Element ts = (Element)jkNode;
								if ("labels".equals(tagValue)){		

									LabelTheme lblTheme = new LabelTheme(LabelEnums.valueOf(ts.getAttribute("name")),
											convertString(ts.getAttribute("colorDay")), convertString(ts.getAttribute("colorNight")));
									labelMap.put(lblTheme.getName(), lblTheme);

								}else if ("icons".equals(tagValue)){
									IconTheme iconTheme = new IconTheme(IconEnum.valueOf(ts.getAttribute("name")),
											ts.getAttribute("iconDay"), ts.getAttribute("iconNight"), imageFolder);
									labelIconMap.put(iconTheme.getName(), iconTheme);											
								}else{
									logger.log(Level.SEVERE, "No Theme found for attribute: " + ts.getAttribute("name"));
								}
							}
						}
					}
				}
			}
		}
	}

	private static Color convertString(String color) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{

		Color c = null;
		if (color.startsWith("#")){
			c = Color.decode(color);
		}else{
			c = (Color) Color.class.getField(color).get(null);
		}
		return  c;
	}
}
