package net.piclock.swing.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.AbstractBorder;

public class RoundedBorder extends AbstractBorder{

	private static final long serialVersionUID = 1L;
	private final Color color;
	private final int gap;
	private RoundRectangle2D borderRec;
	private int thickness = 1;
	
	public RoundedBorder(Color c, int g) {
		color = c;
		gap = g;		
	}	
	public RoundedBorder(Color c, int g, int thickness) {
		color = c;
		gap = g;
		this.thickness = thickness;
	}
	
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		
		g2d.setColor(color);
		int offs = thickness;
        int size = offs + offs;
		
		Shape outer =  new RoundRectangle2D.Double(x,  y, width, height, gap, gap);
		borderRec = new RoundRectangle2D.Double(x+offs,  y+offs, width-size ,height-size, gap, gap);
		Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        path.append(outer, false);
        path.append(borderRec, false);
        
        g2d.fill(path);
		g2d.dispose();
	}
	
	@Override
	public Insets getBorderInsets(Component c) {
		return(getBorderInsets(c, new Insets(0,0,0,0)));
	}
	
	@Override
	public boolean isBorderOpaque() {
		return false;
	}
	public void setThickness(int t){
		thickness = t;
	}
}