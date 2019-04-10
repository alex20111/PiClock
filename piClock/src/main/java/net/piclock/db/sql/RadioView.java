package net.piclock.db.sql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.piclock.db.entity.RadioEntity;
import net.piclock.view.RadioStationsView;

public class RadioView extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger( RadioView.class.getName() );
	private JComboBox<RadioEntity> radioStations;
	private JButton btnReload;

//	private SwingContext ct = SwingContext.getInstance();
	/**
	 * Create the panel.
	 * @return 
	 * @throws IOException 
	 */
	public  RadioView() throws IOException {
		setLayout(new BorderLayout(0, 0));
		
		JPanel titlePanel = new JPanel();
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.setLayout(new MigLayout("", "[][grow 70][][grow]", "[]"));
		
		JButton back = new JButton("<"); 
		back.setPreferredSize(new Dimension(41, 30));
		titlePanel.add(back, "cell 0 0");
		
		back.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.log(Level.CONFIG,"Back");
//				JPanel cardsPanel = (JPanel)ct.getSharedObject(Constants.CARD_PANEL);
//				
//				CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
//				cardLayout.show(cardsPanel, Constants.MAIN_VIEW);	
				
			}
		});
		
		JLabel lblRadio = new JLabel("Radio");
		lblRadio.setFont(new Font("Tahoma", Font.BOLD, 35));
		titlePanel.add(lblRadio, "cell 2 0");
		
		JPanel bodyPanel = new JPanel();
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
				RadioSql sql = new RadioSql();

				btnReload.setText("Loading");

				try {
					radioStations.removeAll();
					for(RadioEntity radio : sql.loadAllRadios()){
						radioStations.addItem(radio);
					}
				} catch (ClassNotFoundException | SQLException e1) {		
					e1.printStackTrace();
				}
				btnReload.setText("Reload");
			}
		});
		btnReload.setPreferredSize(new Dimension(65, 30));
		btnReload.setFont(new Font("Tahoma", Font.PLAIN, 16));
		bodyPanel.add(btnReload, "cell 6 1");
		
		JPanel btnPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) btnPanel.getLayout();
		flowLayout.setHgap(15);
		bodyPanel.add(btnPanel, "cell 0 4 8 1,grow");
		
//		ImageUtils ut = ImageUtils.getInstance();
		
		JButton btnPlay = new JButton();
		btnPlay.setIcon(new ImageIcon("C:\\DEV\\workspace\\Tests\\img\\play.png"));
		btnPanel.add(btnPlay);
		
		JButton btnStop = new JButton();
		btnStop.setIcon(new ImageIcon("C:\\DEV\\workspace\\Tests\\img\\stop.png"));
		btnPanel.add(btnStop);
		
		setOpaque(false);
		

		

	}
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RadioStationsView f = new RadioStationsView();
					JFrame frame = new JFrame();
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.setBounds(100, 100, 800, 480);
					frame.setContentPane(f);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}