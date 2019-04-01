package net.piclock.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

import net.piclock.main.RadioLinks;

public class LoadRadioStreams {	
	private static final Logger logger = Logger.getLogger( LoadRadioStreams.class.getName() );

	public static List<RadioLinks> loadRadioFromXml() throws ParserConfigurationException, SAXException, IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		logger.log(Level.CONFIG, "loadRadioFromXml()");
		
		List<RadioLinks> radioList = new ArrayList<RadioLinks>();
		
		File fXmlFile = new File("radio.xml");

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("radio");
		
		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;				
				
				RadioLinks radio = new RadioLinks( eElement.getAttribute("name"),
						eElement.getAttribute("stream"));
				
				radioList.add(radio);
			}
		}
		return radioList;
	}
}
