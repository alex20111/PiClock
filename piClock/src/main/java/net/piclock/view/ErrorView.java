package net.piclock.view;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;

import net.miginfocom.swing.MigLayout;
import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.main.Constants;
import net.piclock.swing.component.DragScrollListener;
import net.piclock.swing.component.SwingContext;

import javax.swing.JLabel;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ErrorView extends JPanel {

	
	private static final long serialVersionUID = 1L;
	private JLabel lblErrorText;
	private List<ErrorInfo> errors;
	
	private JLabel lblTypeTxt;
	private JLabel lblDateTxt;
	
	private JButton btnBackward;
	private JButton btnForward ;
	
	private int idx = 0;
	
	private SwingContext ct ;
	
	/**
	 * Create the panel.
	 */
	public ErrorView() {
		ct = SwingContext.getInstance();
		errors = new ArrayList<>();
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new MigLayout("", "[][grow][][grow]", "[]"));
		
		JButton btnBack = new JButton("<");
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPanel cardsPanel = (JPanel)ct.getSharedObject(Constants.CARD_PANEL);
				CardLayout cardLayout = (CardLayout) cardsPanel.getLayout();
				cardLayout.show(cardsPanel, Constants.MAIN_VIEW);
			}
		});
		btnBack.setFont(new Font("Tahoma", Font.BOLD, 18));
		panel.add(btnBack, "cell 0 0,aligny center");
		
		JLabel lblErrorDisplay = new JLabel("Error display");
		lblErrorDisplay.setFont(new Font("Tahoma", Font.BOLD, 35));
		panel.add(lblErrorDisplay, "cell 2 0");
		
		JPanel centerPanel = new JPanel();
		add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(new MigLayout("", "[][grow]", "[][][grow][]"));
		
		JLabel lblErrorType = new JLabel("Error Type: ");
		lblErrorType.setFont(new Font("Tahoma", Font.BOLD, 15));
		centerPanel.add(lblErrorType, "cell 0 0");
		
		 lblTypeTxt = new JLabel("");
		 lblTypeTxt.setFont(new Font("Tahoma", Font.PLAIN, 13));
		centerPanel.add(lblTypeTxt, "cell 1 0,alignx left");
		
		JLabel lblDate = new JLabel("Date:");
		lblDate.setFont(new Font("Tahoma", Font.BOLD, 15));
		centerPanel.add(lblDate, "cell 0 1");
		
		lblDateTxt = new JLabel("");
		lblDateTxt.setFont(new Font("Tahoma", Font.PLAIN, 13));
		centerPanel.add(lblDateTxt, "cell 1 1");
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		centerPanel.add(scrollPane, "cell 0 2 2 1,grow");
		
		JPanel contentPanel = new JPanel();
		contentPanel.setFont(new Font("Tahoma", Font.BOLD, 15));
		DragScrollListener dl = new DragScrollListener(contentPanel);
		contentPanel.setOpaque(false);
		contentPanel.setLayout(new MigLayout("", "[grow]", "[grow]"));	
		
		
		 lblErrorText = new JLabel("");
		lblErrorText.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblErrorText.setVerticalAlignment(SwingConstants.TOP);
		lblErrorText.setHorizontalAlignment(SwingConstants.LEFT);
		contentPanel.add(lblErrorText, "cell 0 0,aligny top");
		scrollPane.setViewportView(contentPanel);
		
		lblErrorText.addMouseListener(dl);
		lblErrorText.addMouseMotionListener(dl);
		
		contentPanel.addMouseListener(dl);
		contentPanel.addMouseMotionListener(dl);
		
		JPanel buttonPanel = new JPanel();
		centerPanel.add(buttonPanel, "cell 0 3 2 1,grow");
		
		btnBackward = new JButton("Back");
		btnBackward.setEnabled(false);
		btnBackward.addActionListener(l -> {
			idx --;
			
			if (idx >= 0) {
				displayErrors();
				
				if (idx == 0) {
					btnBackward.setEnabled(false);
				}
				
				if (!btnForward.isEnabled()) {
					btnForward.setEnabled(true);
				}
			}
			
		});
		buttonPanel.add(btnBackward);
		
		btnForward = new JButton("Next");
		btnForward.addActionListener(l -> {
			idx ++;
			int size = errors.size() - 1;
			if (size >= idx ) {
			
				displayErrors();
				
				if (size == idx) {
					btnForward.setEnabled(false);
				}
				
				if (!btnBackward.isEnabled()) {
					btnBackward.setEnabled(true);
				}
			}
					
		});
		
		buttonPanel.add(btnForward);
		

	}
	public void populateScreen() {
		
		//1st sort map by error dates:
		errors.clear();		
		
		ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
		
		eh.broadcastOff();
		
		errors.addAll(eh.getErrorAsList());
		
		btnBackward.setEnabled(false);
		btnForward.setEnabled(true);
		
		Collections.sort(errors, new Comparator<ErrorInfo>() {
			@Override
			public int compare(ErrorInfo o1, ErrorInfo o2) {
				 return o1.getDate().compareTo(o2.getDate());
			}
			});		
		
		idx = 0;
		
		displayErrors();		
		
	}
	
	private void displayErrors() {
		lblErrorText.setText("<html> <div style='width: 420px;word-wrap: break-word;' >" + errors.get(idx).getErrorMessage() + "</div></html>" );
		lblDateTxt.setText( DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(errors.get(idx).getDate()));
		lblTypeTxt.setText(errors.get(idx).getType().toString());
	}

}
