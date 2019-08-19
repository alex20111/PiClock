package net.piclock.swing.component;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

public class Scroll extends MouseAdapter{

	private Point origin;
	private JComponent panel;
	private boolean mousePressedFirst = false;
	public Scroll (JComponent component){
		panel = component;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		origin = new Point(e.getPoint());
//		System.out.println("MOUSE!!!!!! PRESSED: " + origin);
		mousePressedFirst = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
//		System.out.println("Mouse Released");
		mousePressedFirst = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
//		System.out.println("MOUSE dragged: " + origin);
		if (origin != null && mousePressedFirst) {
			JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, e.getComponent());
			if (viewPort != null) {
				int deltaX = origin.x - e.getX();
				int deltaY = origin.y - e.getY();

				Rectangle view = viewPort.getViewRect();
				view.x += deltaX;
				view.y += deltaY;

				panel.scrollRectToVisible(view);
				
			}
		}
	}
}
