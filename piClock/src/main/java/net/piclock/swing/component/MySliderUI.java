package net.piclock.swing.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

public class MySliderUI extends BasicSliderUI {

  
    private Color thumbColor = new Color(131, 127, 211);
    
    private Dimension dim = new Dimension(19, 28);
    
    private List<ThumbPosition> listeners = new ArrayList<>();

    public MySliderUI(JSlider slider) {
        super(slider);
    }
    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(g, c);
    }
    
    @Override
    protected Dimension getThumbSize() {
    	return dim;
    }
    
    @Override
    public void paintThumb(Graphics g) {
    	
    	 Graphics2D g2d = (Graphics2D) g;
    	 
    	 fireThumbPositionListeners(thumbRect.x, thumbRect.y, thumbRect.getSize());
    
    	 int x = thumbRect.x + 1;
    	 int y = thumbRect.y + 1;
         
         RoundRectangle2D shape = new RoundRectangle2D.Float(x,y, thumbRect.width - 3, thumbRect.height - 3, 14, 18);
         
         g2d.setPaint(new Color(81, 83, 186));
         g2d.fill(shape);
 
         Stroke old = g2d.getStroke();
         g2d.setStroke(new BasicStroke(2f));
         g2d.setPaint(thumbColor);
         g2d.draw(shape);
         g2d.setStroke(old);         
    }
    
    public void setDimensions(Dimension d){
    	dim = d;
    }
    public void setThumbColor(Color color){
    	this.thumbColor = color;
    }
    
    public void addListener(ThumbPosition tp){
    	listeners.add(tp);
    }
    private void fireThumbPositionListeners(int x, int y, Dimension dim){
    	for(ThumbPosition p : listeners){
    		p.position(x, y, dim);
    	}
    }

}