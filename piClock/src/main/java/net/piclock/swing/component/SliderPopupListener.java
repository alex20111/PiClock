package net.piclock.swing.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Dictionary;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.sun.awt.AWTUtilities;

class SliderPopupListener extends MouseAdapter {

	private final JWindow toolTip = new JWindow();
	private final JLabel label = new JLabel("", SwingConstants.CENTER);
	private Dimension size = new Dimension(30, 20);
	private int sliderOrientation = -1;
	private boolean useTableLableText = false;
	private RoundedBorder rb;
	private int thumbYCoord = -1;

	private int xCoord = -1;

	@SuppressWarnings("rawtypes")
	private Optional<Dictionary> dic = Optional.empty();

	public SliderPopupListener() {
		super();
		rb = new RoundedBorder(Color.black, 100, 2);
		
		label.setOpaque(false);
		label.setBackground(UIManager.getColor("ToolTip.background"));
		label.setBorder(rb);
		label.setBackground(Color.pink);
		toolTip.add(label);
		toolTip.setSize(size);
		toolTip.setAlwaysOnTop(true);
		AWTUtilities.setWindowOpaque(toolTip, false); 

	}
	public SliderPopupListener(int sliderOrientation) {
		this();
		this.sliderOrientation = sliderOrientation;

	}

	protected void updateToolTip(MouseEvent e) {
		JSlider slider = (JSlider) e.getComponent();

		if (!dic.isPresent() && useTableLableText){
			dic = Optional.ofNullable(slider.getLabelTable());
		}

		if (dic.isPresent() && useTableLableText){
			JLabel l = (JLabel)dic.get().get(slider.getValue());
			label.setText(l.getText());
		}else{
			label.setText(String.format("%03d", slider.getValue()));	
		}
		
		Point pt = e.getPoint();

		if (sliderOrientation == JSlider.HORIZONTAL){
			pt.y = -size.height;
			System.out.println("PT4: " + pt + " thumbYCoord: " + thumbYCoord );
		}else{      
			pt.x = xCoord - 70; //(Distance from the thumb.)
		}

		if (sliderOrientation == JSlider.HORIZONTAL){
			pt.translate(-15, 0); //dimensions of the thumb
		}else{
			pt.y = (thumbYCoord );
			pt.translate(0, -15); //dimensions of the thumb -- alignement beside the thumb (Centered)

		}
		SwingUtilities.convertPointToScreen(pt, e.getComponent());
		toolTip.setLocation(pt);
	}

	@Override public void mouseDragged(MouseEvent e) {
		updateToolTip(e);
	}

	@Override public void mousePressed(MouseEvent e) {

		if (UIManager.getBoolean("Slider.onlyLeftMouseButtonDrag") && SwingUtilities.isLeftMouseButton(e)) {
			xCoord = e.getPoint().x;  
			updateToolTip(e);   
			toolTip.setVisible(true);
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) 
	{
		toolTip.setVisible(false);
		xCoord = -1;
	}

	public void setLabelFont(Font font){
		label.setFont(font);
	}

	public void setLabelDimensions(Dimension dim){
		size = dim;
		toolTip.setSize(size);
	}
	public void useLabelTableText(boolean use){
		useTableLableText = use;
	}
	public void setPopupBorderThickness(int t){
		rb.setThickness(t);
		label.setBorder(rb);
	}
	public void setThumbYCoord(int y){
		thumbYCoord = y;
	}	
}