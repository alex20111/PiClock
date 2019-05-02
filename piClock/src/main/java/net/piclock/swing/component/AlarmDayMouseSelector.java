package net.piclock.swing.component;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public class AlarmDayMouseSelector extends MouseAdapter{

	private boolean selected = false;
	private JLabel label;
	private RoundedBorder border;
	
	public AlarmDayMouseSelector(JLabel label, boolean initSelected) {
		this.label = label;
		this.selected = initSelected;
		
		if (selected) {
			label.setBorder(new CompoundBorder(border, new EmptyBorder(0,0,0,0)));
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(selected) {
			label.setBorder(new EmptyBorder(0,0,0,0));
			selected = false;
		}else {
			label.setBorder(new CompoundBorder(border, new EmptyBorder(0,0,0,0)));
			selected = true;
		}
	}
	
	public boolean isSelected() {
		return selected;
	}
}
