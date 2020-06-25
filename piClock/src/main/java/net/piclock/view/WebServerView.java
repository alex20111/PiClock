package net.piclock.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.piclock.enums.LabelEnums;
import net.piclock.handlers.PiHandler;
import net.piclock.main.Constants;
import net.piclock.server.MiniWebServer;
import net.piclock.swing.component.SwingContext;
import net.piclock.theme.ThemeHandler;

public class WebServerView extends JPanel {

	private static final long serialVersionUID = 1L;
	private JButton btnStart;
	private JButton btnStop;
	private JLabel lblStatusresult;
	private JLabel lblAddressTxt;

	/**
	 * Create the panel.
	 */
	public WebServerView(final JLabel lblWebserverIcon) {
		setSize(new Dimension(800, 480));
		setLayout(new BorderLayout(0, 0));
		setOpaque(false);
		
		ThemeHandler t = (ThemeHandler) SwingContext.getInstance().getSharedObject(Constants.THEMES_HANDLER);
		
		JPanel titlePanel = new JPanel();
		titlePanel.setOpaque(false);
		FlowLayout flowLayout = (FlowLayout) titlePanel.getLayout();
		flowLayout.setVgap(10);
		flowLayout.setHgap(10);
		add(titlePanel, BorderLayout.NORTH);

		JLabel lblWebServer = new JLabel("Web  Server");
		lblWebServer.setFont(new Font("Tahoma", Font.BOLD, 35));
		t.registerLabelTextColor(lblWebServer, LabelEnums.WSERVER_TITLE);
		titlePanel.add(lblWebServer);

		JPanel bodyPanel = new JPanel();
		bodyPanel.setOpaque(false);
		add(bodyPanel, BorderLayout.CENTER);
		bodyPanel.setLayout(new MigLayout("", "[][][][][][grow]", "[][][][][][][grow][]"));

		JLabel lblStatus = new JLabel("Status:");
		t.registerLabelTextColor(lblStatus, LabelEnums.WSERVER_STATUS);
		lblStatus.setFont(new Font("Tahoma", Font.BOLD, 16));
		bodyPanel.add(lblStatus, "cell 1 1,alignx right");

		lblStatusresult = new JLabel();
		lblStatusresult.setFont(new Font("Tahoma", Font.PLAIN, 16));
		bodyPanel.add(lblStatusresult, "cell 2 1 3 1,alignx left");

		JLabel lblAddress = new JLabel("Address: ");
		t.registerLabelTextColor(lblAddress, LabelEnums.WSERVER_ADDRESS);
		lblAddress.setFont(new Font("Tahoma", Font.BOLD, 16));
		bodyPanel.add(lblAddress, "cell 1 2,alignx right");

		lblAddressTxt = new JLabel("");
		t.registerLabelTextColor(lblAddressTxt, LabelEnums.WSERVER_ADDRESS_TXT);
		lblAddressTxt.setFont(new Font("Tahoma", Font.PLAIN, 16));
		bodyPanel.add(lblAddressTxt, "cell 2 2 2 1,alignx left");

		JPanel panel = new JPanel();
		panel.setOpaque(false);
		FlowLayout flowLayout_1 = (FlowLayout) panel.getLayout();
		flowLayout_1.setHgap(15);
		bodyPanel.add(panel, "cell 1 5 2 1,grow");

		btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				start(lblWebserverIcon);
			}
		});
		btnStart.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panel.add(btnStart);

		btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				lblWebserverIcon.setVisible(false);
			}
		});
		btnStop.setEnabled(false);
		btnStop.setFont(new Font("Tahoma", Font.PLAIN, 13));
		panel.add(btnStop);

		JButton btnBack = new JButton("<");
		btnBack.addActionListener(l -> {back();});
		bodyPanel.add(btnBack, "cell 0 7");
		
		start(lblWebserverIcon);
	}
	private void back(){

		SwingContext context = SwingContext.getInstance();
		JPanel cards = (JPanel)context.getSharedObject(Constants.CARD_PANEL);
		CardLayout cl = (CardLayout)(cards.getLayout());
        cl.show(cards, Constants.MAIN_VIEW );
	}
	private void start(JLabel lblWebserverIcon){
		try{
			btnStart.setEnabled(false);
			btnStop.setEnabled(true);

			MiniWebServer server = MiniWebServer.getInstance();
			server.startServer();

			String IP=	PiHandler.getInstance().getIpAddress();
			lblAddressTxt.setText("http://" + IP + "/");
			lblStatusresult.setText("Running");
			lblStatusresult.setForeground(Color.GREEN);
			lblWebserverIcon.setVisible(true);
		}catch(Exception e){
			lblAddressTxt.setText("ERROR while starting");
			btnStart.setEnabled(true);
			btnStop.setEnabled(false);
			lblStatusresult.setText("Stopped");
			lblStatusresult.setForeground(Color.RED);
			e.printStackTrace();
		}
	}

	private void stop(){
		btnStart.setEnabled(true);
		btnStop.setEnabled(false);
		MiniWebServer server = MiniWebServer.getInstance();
		server.stop();
		lblAddressTxt.setText("");
		lblStatusresult.setText("Stopped");
		lblStatusresult.setForeground(Color.RED);
	}

}