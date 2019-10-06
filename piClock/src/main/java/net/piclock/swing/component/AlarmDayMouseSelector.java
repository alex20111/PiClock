package net.piclock.swing.component;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import net.piclock.enums.DayNightCycle;
import net.piclock.main.Constants;

public class AlarmDayMouseSelector extends MouseAdapter implements PropertyChangeListener{
	
	private static final Logger logger = Logger.getLogger( AlarmDayMouseSelector.class.getName() );

	private boolean selected = false;
	private JLabel label;
	private RoundedBorder border;
	private SwingContext sc;
	private DayNightCycle cycle = DayNightCycle.DAY;
	
	public AlarmDayMouseSelector(JLabel label, boolean initSelected) {
		sc = SwingContext.getInstance(); 
		sc.addPropertyChangeListener(Constants.DAY_NIGHT_CYCLE, this);
		
		this.label = label;
		this.selected = initSelected;
		this.border = new RoundedBorder(Color.black, 40,2);
		
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
			if (cycle == DayNightCycle.NIGHT) {
				border.setBorderColor(Color.WHITE);
			}else {
				border.setBorderColor(Color.BLACK);
			}
			
			label.setBorder(new CompoundBorder(border, new EmptyBorder(0,0,0,0)));
			selected = true;
		}
	}
	
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		logger.log(Level.CONFIG, "Property changed: " + evt.getPropertyName());
		
		cycle = (DayNightCycle)evt.getNewValue();
		
		if (cycle == DayNightCycle.DAY) {
			border.setBorderColor(Color.BLACK);
			label.setBorder(new CompoundBorder(border, new EmptyBorder(0,0,0,0)));
		}else if(cycle == DayNightCycle.NIGHT) {
			border.setBorderColor(Color.WHITE);
			label.setBorder(new CompoundBorder(border, new EmptyBorder(0,0,0,0)));
		}
		
	}
}
