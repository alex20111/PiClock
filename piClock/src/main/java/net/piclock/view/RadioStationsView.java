package net.piclock.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import home.misc.Exec;
import net.miginfocom.swing.MigLayout;
import net.piclock.db.entity.RadioEntity;
import net.piclock.db.sql.RadioSql;
import net.piclock.main.Constants;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.ImageUtils;

public class RadioStationsView extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger( RadioStationsView.class.getName() );
	private JComboBox<RadioEntity> radioStations;
	private JButton btnReload;
	
	private SwingContext ct;	
	private JLabel lblRadioIcon;
	
//	private SwingContext ct = SwingContext.getInstance();
	/**
	 * Create the panel.
	 * @return 
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public  RadioStationsView(JLabel radioIcon) throws IOException, ClassNotFoundException, SQLException {
		
		this.lblRadioIcon = radioIcon;
		setLayout(new BorderLayout(0, 0));
		
		ct = SwingContext.getInstance();
		setOpaque(false);
		
		
		JPanel titlePanel = new JPanel();
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.setLayout(new MigLayout("", "[][grow 70][][grow]", "[]"));
		titlePanel.setOpaque(false);
		JButton back = new JButton("<"); 
		back.setPreferredSize(new Dimension(41, 30));
		titlePanel.add(back, "cell 0 0");
		
		back.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.log(Level.CONFIG,"Back");
				JPanel cardsPanel = (JPanel)ct.getSharedObject(Constants.CARD_PANEL);
				
				CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
				cardLayout.show(cardsPanel, Constants.MAIN_VIEW);	
				
			}
		});
		
		JLabel lblRadio = new JLabel("Radio");
		lblRadio.setFont(new Font("Tahoma", Font.BOLD, 35));
		titlePanel.add(lblRadio, "cell 2 0");
		
		JPanel bodyPanel = new JPanel();
		bodyPanel.setOpaque(false);
		add(bodyPanel, BorderLayout.CENTER);
		bodyPanel.setLayout(new MigLayout("", "[][grow 50][][][grow][][][grow 50]", "[][][][100px][][]"));
		
		JLabel lblSelectRadio = new JLabel("Select Radio:");
		lblSelectRadio.setFont(new Font("Tahoma", Font.PLAIN, 16));
		bodyPanel.add(lblSelectRadio, "cell 2 1,alignx trailing");
		
		radioStations = new JComboBox<RadioEntity>();
		radioStations.setPreferredSize(new Dimension(28, 35));
		bodyPanel.add(radioStations, "cell 3 1 3 1,growx");
		
		btnReload = new JButton("Reload");
		btnReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnReload.setText("Loading");

				try {
					
					loadAllRadioStations();
					
				
				} catch (ClassNotFoundException | SQLException e1) {		
					logger.log(Level.SEVERE, "Error in loading" , e1);
				}
				btnReload.setText("Reload");
			}
		});
		btnReload.setPreferredSize(new Dimension(65, 30));
		btnReload.setFont(new Font("Tahoma", Font.PLAIN, 16));
		bodyPanel.add(btnReload, "cell 6 1");
		
		JPanel btnPanel = new JPanel();
		btnPanel.setOpaque(false);
		FlowLayout flowLayout = (FlowLayout) btnPanel.getLayout();
		flowLayout.setHgap(15);
		bodyPanel.add(btnPanel, "cell 0 4 8 1,grow");
		
		ImageUtils ut = ImageUtils.getInstance();
		
		JButton btnPlay = new JButton();
		btnPlay.setIcon(ut.getImage("play.png"));
		btnPanel.add(btnPlay);
		
		btnPlay.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(radioStations.getSelectedIndex() != -1) {
					
				
				lblRadioIcon.setVisible(true);
				
				
				RadioEntity re = (RadioEntity)radioStations.getSelectedItem();
				System.out.println("Playing : " + re.getRadioName());
				
//				Exec exec = new Exec();
//				exec.addCommand(cmd)
				
				}
			}
		});
		
		JButton btnStop = new JButton();
		btnStop.setIcon(ut.getImage("stop.png"));
		btnPanel.add(btnStop);
		
		btnStop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				lblRadioIcon.setVisible(false);
				
			}
		});
		
		setOpaque(false);
		loadAllRadioStations();

	}
	private void loadAllRadioStations() throws ClassNotFoundException, SQLException {
		RadioSql sql = new RadioSql();

		
		radioStations.removeAll();
		for(RadioEntity radio : sql.loadAllRadios()){
			radioStations.addItem(radio);
		}

	}

}