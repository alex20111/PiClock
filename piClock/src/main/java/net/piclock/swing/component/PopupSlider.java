package net.piclock.swing.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JSlider;
import javax.swing.plaf.SliderUI;

public class PopupSlider extends JSlider implements ThumbPosition{

	private static final long serialVersionUID = 1L;
	private SliderPopupListener sp;
	private MySliderUI  UI;
	private int orientation = JSlider.HORIZONTAL;
	
	public PopupSlider(){
		super();
		initClass();
	}
	public PopupSlider(int orientation, int min, int max, int value){
		super(orientation, min, max, value);
		this.orientation = orientation;
		initClass();
	}
	
	public void setPopupLabelDimension(Dimension dim){
		sp.setLabelDimensions(dim);
	}
	public void useLabelTableText(boolean b){
		sp.useLabelTableText(b);
	}
	public void setPopupLabelFont(Font font){
		sp.setLabelFont(font);
	}
	
	public void customUI(SliderUI ui){
		setUI(ui);
	}
	public void setThumbColor(Color color){
		UI.setThumbColor(color);
	}
	public void popupBorderThickness(int t){
		sp.setPopupBorderThickness(t);
	}
	public void setThumbDimension(Dimension dim){
		UI.setDimensions(dim);
	}
	
	private void initClass(){				
		 UI = new MySliderUI(this);
		 UI.addListener(this);
		
		 sp = new SliderPopupListener(orientation);
		 addMouseMotionListener(sp);
		 addMouseListener(sp);		
		 
		 setUI(UI);
	}
	@Override
	public void position(int x, int y, Dimension dim) {
		sp.setThumbYCoord(y);
	}
}