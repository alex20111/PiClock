package net.piclock.swing.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Dictionary;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.SliderUI;
import javax.swing.SwingConstants;
import javax.swing.JLabel;

public class PopupSlider extends JPanel {//implements ThumbPosition{

	private static final long serialVersionUID = 1L;
	private MySliderUI  UI;
	private int orientation = JSlider.HORIZONTAL;
	
	JSlider slider;
	
	JLabel lblValue;
	
	private boolean useTableLabel = false;
	
	public PopupSlider(){
		super();
		setPreferredSize(new Dimension(100, 400));

		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(0, 0));
//		super(orientation, min, max, value);
		slider = new JSlider(SwingConstants.VERTICAL, 1, 22, 12);
		add(slider, BorderLayout.CENTER);
		this.orientation = orientation;
		
		lblValue = new JLabel("Value");
		lblValue.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblValue.setHorizontalAlignment(SwingConstants.CENTER);
		lblValue.setText("1");
		add(lblValue, BorderLayout.NORTH);
		initClass();

	}
	public PopupSlider(int orientation, int min, int max, int value){
		
		setPreferredSize(new Dimension(100, 400));
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(0, 0));
		
		slider = new JSlider(orientation, min, max, value);
		add(slider, BorderLayout.CENTER);
		this.orientation = orientation;
		
//		RoundedBorder rb = new RoundedBorder(Color.black, 100, 2);
		
		lblValue = new JLabel("");
		lblValue.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblValue.setHorizontalAlignment(SwingConstants.CENTER);
		lblValue.setText(String.valueOf(slider.getValue()));
//		lblValue.setBorder(rb);
		
		setOpaque(false);
		
		add(lblValue, BorderLayout.NORTH);
		
		initClass();
	}
	
	public void setPaintTicks(boolean painTicks) {
		slider.setPaintTicks(painTicks);
	}
	
	public void setPaintLabels(boolean labels) {
		slider.setPaintLabels(labels);
	}
	
	public void setLabelTable(Dictionary labels) {
		slider.setLabelTable(labels);
	}
	public Dictionary getLabelTable() {
		return slider.getLabelTable();
	}
	
	public JSlider getSlider() {
		return slider;
	}
	
	
	public void customUI(SliderUI ui){
		setUI(ui);
	}
	public void setThumbColor(Color color){
		UI.setThumbColor(color);
	}

	public void setThumbDimension(Dimension dim){
		UI.setDimensions(dim);
	}
	
	public void useTableLabelText(boolean useLabel) {
		if (useLabel) {
			Dictionary dic = slider.getLabelTable();
			JLabel l = (JLabel)dic.get(slider.getValue());
			lblValue.setText(l.getText());
		}else {
			lblValue.setText(String.valueOf(slider.getValue()));
		}
		this.useTableLabel = useLabel;
	}
	
	private void initClass(){	
		 UI = new MySliderUI(slider);	
		
		slider.setUI(UI);

		slider.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				

				if (useTableLabel) {
					Dictionary dic = slider.getLabelTable();
					JLabel l = (JLabel)dic.get(slider.getValue());
					lblValue.setText(l.getText());

				}else {
					lblValue.setText(String.valueOf(slider.getValue()));
				}
			}
		});

	}
}