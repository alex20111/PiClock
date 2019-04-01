package net.piclock.swing.component;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class KeyBoard extends JDialog implements ActionListener {
	private static final long serialVersionUID = -8054254838392492878L;
	private final JPanel contentPanel = new JPanel();
	private JFormattedTextField passTxt;
	
	private StringBuilder passBuilder = new StringBuilder();
	private boolean upperCase = false;
	JButton[] btn = new JButton[100];
	
	private String[] chars = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
	private String[] charsUp = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
	private String[] special = {"!","@","#","$","%","^","&","*","(",")","-","+",",","."};
	private String[] numbers = {"1","2","3","4","5","6","7","8","9","0"};

	/**
	 * Create the dialog.
	 */
	public KeyBoard() {
		 setUndecorated(true);

		setModalityType(ModalityType.APPLICATION_MODAL);
		
		
		setSize(450, 300);
		getContentPane().setLayout(new BorderLayout(0, 0));
		contentPanel.setLayout(new WrapLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel);
		
		JPanel bottomPanel = new JPanel();
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		passTxt = new JFormattedTextField();
		passTxt.setColumns(10);
		bottomPanel.add(passTxt);
		
		JButton btnOk = new JButton("Ok");
		btnOk.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				
			}
		});
		bottomPanel.add(btnOk);
		
		JButton btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				passBuilder = new StringBuilder();
				passTxt.setText("");
			}			
		});
		bottomPanel.add(btnClear);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				passBuilder = new StringBuilder();
				passTxt.setText("");
				setVisible(false);
			}
		});
		bottomPanel.add(btnCancel);
	
		//chars
		for(int i = 0 ; i < chars.length ; i ++){
			btn[i] =  new JButton(chars[i]);
			contentPanel.add(btn[i]);
			btn[i].addActionListener(this);
		}
		
		//numbers
		int totNumbers = chars.length + numbers.length;
		
		int z = 0;
		for(int i = chars.length ; i < totNumbers ; i ++){
			btn[i] =  new JButton(numbers[z]);
			contentPanel.add(btn[i]);
			btn[i].addActionListener(this);
			z++;
		}
		
		//special
		int totSpecial = totNumbers + special.length;
		int f = 0;
		for(int i = totNumbers ; i < totSpecial ; i ++){
			btn[i] =  new JButton(special[f]);
			contentPanel.add(btn[i]);
			btn[i].addActionListener(this);
			f++;
		}
		
		int idx = totSpecial + 1;
		btn[idx] =  new JButton("Upper");
		contentPanel.add(btn[idx]);
		btn[idx].addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				if (!upperCase){
					for(int i = 0 ; i < chars.length ; i ++){
						btn[i].setText(charsUp[i]);				
					}
					upperCase = true;
				}else{
					for(int i = 0 ; i < chars.length ; i ++){
						btn[i].setText(chars[i]);				
					}
					upperCase = false;
				}
			}
		});		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		JButton btn = (JButton)e.getSource();
		
		passBuilder.append(btn.getText());
		passTxt.setText(passBuilder.toString());		
	}
	
	public String getText(){
		return passTxt.getText();
	}
	public void setText(String text){
		 passTxt.setText(text);
	}
}
