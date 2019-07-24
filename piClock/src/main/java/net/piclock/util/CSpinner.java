package net.piclock.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JSpinner.ListEditor;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.plaf.basic.BasicArrowButton;

public class CSpinner {
	
	public CSpinner( JSpinner spinner , int width, int height) { //, int buttonWidth, int buttonHeight) {
	    spinner.setLayout(null);
//	    int fontSize = 160;
	    int buttonSize = height / 2;
	    Component c = spinner.getComponent(0);
	    if (c instanceof BasicArrowButton) {
	        BasicArrowButton b = (BasicArrowButton) c;
	        b.setBounds(width + 5, 2, 0, 0);
	        b.setSize(buttonSize + 10, buttonSize);
	    }
	    Component c2 = spinner.getComponent(1);
	    if (c2 instanceof BasicArrowButton) {
	        BasicArrowButton b = (BasicArrowButton) c2;
	        b.setBounds(width + 5, buttonSize + 2, 0, 0);
	        b.setSize(buttonSize + 10, buttonSize);
	    }
	    Component c3 = spinner.getComponent(2);
	    if (c3 instanceof NumberEditor || c3 instanceof ListEditor) {
	        DefaultEditor ne = (DefaultEditor) c3;
	        ne.setBounds(2, 2, 10, 20);
	        ne.setSize(width, height);
	        ne.setAlignmentY(0);
	    }
	    spinner.setFont(new Font("Tahoma", Font.PLAIN, height));
	    spinner.setSize(new Dimension(279, 179));
	}
		
		
	}


