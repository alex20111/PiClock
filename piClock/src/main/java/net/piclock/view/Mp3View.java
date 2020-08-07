package net.piclock.view;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.mpatric.mp3agic.ID3v1Genres;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;

import java.awt.FlowLayout;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import net.miginfocom.swing.MigLayout;
import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.bean.SortMp3ByName;
import net.piclock.bean.VolumeConfig;
import net.piclock.db.entity.Mp3Entity;
import net.piclock.db.sql.Mp3Sql;
import net.piclock.handlers.PiHandler;
import net.piclock.main.Constants;
import net.piclock.swing.component.Message;
import net.piclock.swing.component.MessageListener;
import net.piclock.swing.component.PopupSlider;
import net.piclock.swing.component.RoundedBorder;
import net.piclock.swing.component.Scroll;
import net.piclock.swing.component.SwingContext;
import net.piclock.thread.ScreenAutoClose;
import net.piclock.util.VolumeIndicator;
import net.piclock.util.WordUtils;

public class Mp3View extends JPanel implements MessageListener {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger( Mp3View.class.getName() );

	private String BTN_VOL_ALARM = "Set Volume";
	private String BTN_VOL_MP3	 = "Vol";

	private JLabel lblSelectionTxt;
	private JTable table;
	private TableRowSorter<TableModel> sorter;
	private Mp3Sql sql;

	private JScrollPane scrollPane;

	private JLabel lblTrack;
	private JLabel lblArtist; 
	private JLabel lblCategory;
	private JButton btnAlarmMp3 ;
	private JButton btnPlay;
	private JButton btnStop;

	private int sliderLastVal  		  = 25;  //slider for the music selection
	private Selection selection;

	private SwingContext ct;
	private JButton btnBack;
	private JButton btnVol;
	private JPanel btnPanel;

	private boolean setVolumeForAlarm = false;
	private int selectedVolume = -1;

	//slider table
	private Hashtable<Integer, JLabel> sliderTable;
	private boolean allowSliderChangeEvent = true;

	//handler
	private PiHandler handler;

	private JLabel lblMp3MainIcon;

//	private Mp3Player mp3Player;

	/**
	 * Create the panel.
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings({ "serial", "rawtypes" })
	public Mp3View(JLabel lblMp3Icon) throws IOException, ClassNotFoundException, SQLException {

//		mp3Player = Mp3Player.getInstance();

		lblMp3MainIcon = lblMp3Icon;
		handler = PiHandler.getInstance();

		sql = new Mp3Sql();

		ct = SwingContext.getInstance();

		ct.addMessageChangeListener(Constants.B_VISIBLE_FRM_BUZZ_SEL, this); //this is to make the button visible to selecte a mp3.
		ct.addMessageChangeListener(Constants.VOLUME_SENT_FOR_CONFIG_MP3, this);
		ct.addMessageChangeListener(Constants.RELOAD_FROM_WEB, this);
		ct.addMessageChangeListener(Constants.MUSIC_TOGGELED, this);
				ct.addMessageChangeListener(Constants.MP3_PLAYER_ERROR, this);


		setLayout(new BorderLayout(0, 0));
		setSize(800, 480);
		setOpaque(false);

		JPanel titlePanel = new JPanel();
		add(titlePanel, BorderLayout.NORTH);

		JLabel lblTitle = new JLabel("MP3");
		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 24));
		titlePanel.add(lblTitle);
		titlePanel.setOpaque(false);

		JPanel panelView = new JPanel();
		panelView.setBorder(new EmptyBorder(0, 5, 2, 0));
		add(panelView, BorderLayout.CENTER);
		panelView.setLayout(new BorderLayout(0, 0));
		panelView.setOpaque(false);

		scrollPane = new JScrollPane();
		panelView.add(scrollPane, BorderLayout.CENTER);

		lblSelectionTxt = new JLabel("");
		lblSelectionTxt.setFont(new Font("Tahoma", Font.PLAIN, 14));
		scrollPane.setColumnHeaderView(lblSelectionTxt);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		centerRenderer.setVerticalAlignment( JLabel.TOP );		

		DefaultTableModel model = new DefaultTableModel(new Object[] { "Track Name", "Artist" , "Category","Time", " "}, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				//all cells false
				return false;
			}
		};

		table = new JTable(model);			

		Scroll scroll = new Scroll(table);
		table.addMouseListener(scroll); 
		table.addMouseMotionListener(scroll); 		

		TableColumnModel tcm = table.getColumnModel();

		tcm.getColumn(1).setCellRenderer( centerRenderer );
		tcm.getColumn(2).setCellRenderer( centerRenderer );
		tcm.getColumn(3).setCellRenderer( centerRenderer );

		tcm.getColumn(0).setPreferredWidth(360);
		tcm.getColumn(1).setPreferredWidth(160);
		tcm.getColumn(2).setPreferredWidth(75);
		tcm.getColumn(3).setPreferredWidth(70);

		sorter = new TableRowSorter<TableModel>(table.getModel());
		table.setRowSorter(sorter);

		scrollPane.setViewportView(table);
		scrollPane.getViewport().setOpaque(false);

		JPanel contentPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) contentPanel.getLayout();
		flowLayout.setHgap(10);
		flowLayout.setAlignment(FlowLayout.LEFT);
		panelView.add(contentPanel, BorderLayout.NORTH);

		lblTrack = new JLabel("Track");
		lblTrack.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				setSelected(lblTrack);	

				List<RowSorter.SortKey> sortKeys = new ArrayList<>();
				sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
				sorter.setSortKeys(sortKeys);
				sorter.sort();
				selection = Selection.TRACK;				
			}

		});
		lblTrack.setFont(new Font("Tahoma", Font.BOLD, 16));
		contentPanel.add(lblTrack);
		contentPanel.setOpaque(false);

		lblArtist = new JLabel("Artist");
		lblArtist.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblArtist.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				setSelected(lblArtist);

				List<RowSorter.SortKey> sortKeys = new ArrayList<>();
				sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
				sorter.setSortKeys(sortKeys);
				sorter.sort();
				selection = Selection.ARTIST;
			}
		});
		contentPanel.add(lblArtist);

		lblCategory = new JLabel("Category");
		lblCategory.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblCategory.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {

				setSelected(lblCategory);	

				List<RowSorter.SortKey> sortKeys = new ArrayList<>();
				sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
				sorter.setSortKeys(sortKeys);
				sorter.sort();
				selection = Selection.CATG;
			}
		});
		contentPanel.add(lblCategory);	

		//Slider
		PopupSlider s = new PopupSlider(JSlider.VERTICAL, 0, 25, 25);
		s.getSlider().setOpaque(false);
		s.setOpaque(false);

		sliderTable = new Hashtable<Integer, JLabel>();
		sliderTable.put (0, new JLabel("Z"));
		sliderTable.put (1, new JLabel("Y"));
		sliderTable.put (2, new JLabel("X"));
		sliderTable.put (3, new JLabel("W"));
		sliderTable.put (4, new JLabel("V"));
		sliderTable.put (5, new JLabel("U"));
		sliderTable.put (6, new JLabel("T"));
		sliderTable.put (7, new JLabel("S"));
		sliderTable.put (8, new JLabel("R"));
		sliderTable.put (9, new JLabel("Q"));
		sliderTable.put (10, new JLabel("P"));
		sliderTable.put (11, new JLabel("O"));
		sliderTable.put (12, new JLabel("N"));
		sliderTable.put (13, new JLabel("M"));
		sliderTable.put (14, new JLabel("L"));
		sliderTable.put (15, new JLabel("K"));
		sliderTable.put (16, new JLabel("J"));
		sliderTable.put (17, new JLabel("I"));
		sliderTable.put (18, new JLabel("H"));
		sliderTable.put (19, new JLabel("G"));
		sliderTable.put (20, new JLabel("F"));
		sliderTable.put (21, new JLabel("E"));
		sliderTable.put (22, new JLabel("D"));
		sliderTable.put (23, new JLabel("C"));
		sliderTable.put (24, new JLabel("B"));
		sliderTable.put (25, new JLabel("A"));
		s.setLabelTable (sliderTable);

		s.setPaintTicks(true);
		s.setPaintLabels(true);
		s.useTableLabelText(true);

		//		s.popupBorderThickness(2);
		s.setBorder(new EmptyBorder(10, 10, 10, 15));

		s.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
				allowSliderChangeEvent = true;
			}
		});


		panelView.add(s, BorderLayout.EAST);
		final Dictionary dic = s.getLabelTable();

		s.getSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				if (allowSliderChangeEvent){
					int value = s.getSlider().getValue();
					JLabel text = (JLabel)dic.get(value);

					boolean goingUp = false;
					if (value > sliderLastVal){

						goingUp = true;
					}

					sliderLastVal = value;

					DefaultTableModel d = (DefaultTableModel) table.getModel();
					for(int i = 0; i < table.getRowCount() ; i ++){

						String val = (String)d.getValueAt(table.convertRowIndexToModel(i), selection.getRowSel());

						if (val.contains("<html>")  ){
							val = val.replaceAll("\\<.*?>","").trim() ;
						}

						if (val.toUpperCase().startsWith(text.getText())){
							int conv = -1;
							if (goingUp){
								conv = table.convertRowIndexToModel(table.convertRowIndexToView(i));
							}else{
								//the 18 is to position at the top when scrolling to visible.
								conv = table.convertRowIndexToModel(table.convertRowIndexToView(i)) + 5;
							}

							table.scrollRectToVisible(table.getCellRect(conv,0, true)); 
							break;
						}
					}
				}
			}
		});

		setSelected(lblTrack);


		btnPanel = new JPanel();
		add(btnPanel, BorderLayout.SOUTH);
		btnPanel.setLayout(new MigLayout("hidemode 3", "[][grow][][][][grow][]", "[]"));

		btnBack = new JButton("<");
		btnBack.setSize(45, 37);
		btnPanel.add(btnBack, "cell 0 0");

		btnPlay = new JButton("P");
		btnPanel.add(btnPlay, "cell 2 0");

		btnStop = new JButton("S");
		btnPanel.add(btnStop, "cell 3 0");

		btnAlarmMp3 = new JButton("Select for alarm");
		btnPanel.add(btnAlarmMp3, "cell 4 0");

		btnPanel.setOpaque(false);

		btnVol = new JButton(BTN_VOL_MP3);
		btnVol.addActionListener(l -> {	

			VolumeConfig config = new VolumeConfig(selectedVolume);
			config.setMp3Id(getMp3IdFromTable());
			config.setFromAlarm(setVolumeForAlarm);
			config.setMsgPropertyName(Constants.VOLUME_SENT_FOR_CONFIG_MP3);
			VolumeNew vol = new VolumeNew(config); 

			vol.setVisible(true);

		});
		btnPanel.add(btnVol, "cell 6 0");
		btnAlarmMp3.setVisible(false);
		btnAlarmMp3.addActionListener(l -> {
			//NO: put result as shared object alarm will get shared object (cannot do it since it's not a modale popup)
			Message msg = new Message();
			//			System.out.println("vtable.getSelectedRow() " + table.getSelectedRow() );
			if (table.getSelectedRow() > -1   ){
				try{
					int id = (int)table.getModel().getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 4);		
					if (id != -1){
						msg.addIntToMessageList(0);
						msg.addIntToMessageList(id);
						msg.addIntToMessageList(selectedVolume);
						String trackName = (String)table.getModel().getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0);
						msg.addStringToMessageList(trackName);

						table.clearSelection();
					}
					else{
						msg.addIntToMessageList(-1);
					}
				}catch(Exception ex){
					logger.log(Level.SEVERE,"Error in selecting mp3", ex);
					msg.addIntToMessageList(-1);
				}


			}else{
				msg.addIntToMessageList(-1);
			}
			ct.sendMessage(Constants.MP3_INFO, msg);
			JPanel contentPane = (JPanel)ct.getSharedObject(Constants.CARD_PANEL);
			CardLayout cardLayout = (CardLayout) contentPane.getLayout(); 
			cardLayout.show(contentPane, Constants.ALARM_VIEW);

			btnMp3Mode();

			((BuzzerOptionDialog)ct.getSharedObject(Constants.BUZZER_OPTION_PANEL)).setVisible(true);			

		});
		btnStop.addActionListener(l ->{

			btnStop.setEnabled(false);
			try {

				handler.playMp3(false, null, -1);
				fireVolumeIconChange(false);
				lblMp3MainIcon.setVisible(false);
			} catch (IllegalStateException | InterruptedException | IOException e1) {

				logger.log(Level.SEVERE, "Error in stopping music", e1);

			}

		});
		btnPlay.addActionListener(l ->{	
			try {
				if (table.getSelectedRow() != -1 ){

					int id = (int)table.getModel().getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 4);		
					if (id >= 0){						

//						
						List<String> mp3Names = new ArrayList<String>();						
						
						Map<Integer, String> mp3 = sql.loadAllMp3WithIdAndFileName();
						
						Set<Integer> key = mp3.keySet();
						
						for(Integer keya : key) {
							if (keya.intValue() == id) {
								mp3Names.add(0, mp3.get(keya));
							}else {
								mp3Names.add(mp3.get(keya));
							}
						}

						btnStop.setEnabled(true);
						fireVolumeIconChange(true);
						lblMp3MainIcon.setVisible(true);
						
						handler.playMp3(true, mp3Names, selectedVolume);	
						
						Thread.sleep(2000);
						
					}
				}
			}catch (IllegalStateException | InterruptedException | IOException | ClassNotFoundException | SQLException e){
				logger.log(Level.CONFIG, "Error while trying to play mp3", e);
			}
		});
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ScreenAutoClose.stop();
				} catch (InterruptedException e1) {
					logger.log(Level.SEVERE, "Error", e1);
				}
				
				JPanel contentPane = (JPanel)ct.getSharedObject(Constants.CARD_PANEL); 
				CardLayout cardLayout = (CardLayout) contentPane.getLayout(); 
				cardLayout.show(contentPane, Constants.MAIN_VIEW);
			}
		});
		selection = Selection.TRACK;

		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);

		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()){

					//verify if the slider selection is consistent with the table selection
					DefaultTableModel mod = (DefaultTableModel)table.getModel();

					int selectedRow = table.getSelectedRow();

					if (selectedRow != -1){

						int id = (int)table.getModel().getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 4);	

						if (id >=0 ){
							btnPlay.setEnabled(true);

							String cellValue = (String)mod.getValueAt(table.convertRowIndexToModel(selectedRow), selection.getRowSel());

							if (cellValue.contains("<html>")  ){
								cellValue = cellValue.replaceAll("\\<.*?>","").trim() ;
							}

							if (cellValue != null && cellValue.length() > 1){
								allowSliderChangeEvent = false;
								s.getSlider().setValue(setSliderValue(cellValue.substring(0, 1)));		    				
							}
						}

					}else{
						btnPlay.setEnabled(false);		    	
					}	    	
				}
			}
		});

		table.setSelectionModel( selectionModel);

		btnPlay.setEnabled(false);
		btnStop.setEnabled(false);		

		loadAllMp3();

		//check if any loaded , if none loaded, disable play button and add message in table.
		if (table.getRowCount() == 0 ){
			addRow(null);

		}

	}

	private void addRow(Mp3Entity mp3){
		DefaultTableModel model = (DefaultTableModel)table.getModel();

		if (mp3 != null){
			String genre = mp3.getMp3Genre() > -1 ? ID3v1Genres.GENRES[mp3.getMp3Genre()] : mp3.getMp3GenreDesc();
			String artist = "<html>  " + WordUtils.wrap(mp3.getArtist() != null 
					&& mp3.getArtist().length() > 0 ? mp3.getArtist() : "unknown" , 23, "\n", true)+ "</html>";

			String duration = "0";
			if (mp3.getMp3Length() > 0){
				duration = String.format("%d:%02d", 
						TimeUnit.SECONDS.toMinutes(mp3.getMp3Length()),
						mp3.getMp3Length() - 
						TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(mp3.getMp3Length())) 
						);
			}

			model.addRow(new Object[]{mp3.getMp3Name(), artist, genre != null && genre.trim().length() > 0 ? genre : "unknown" ,
					duration, mp3.getId()});

		}else{
			model.addRow(new Object[]{"No mp3 found !!!","", "", 0,-1});			
		}
	}

	private void setSelected (JLabel label){	
		//1st unselect all
		lblTrack.setForeground(Color.BLACK);
		lblTrack.setBorder(null);		 
		lblArtist.setForeground(Color.BLACK);
		lblArtist.setBorder(null);
		lblCategory.setForeground(Color.BLACK);
		lblCategory.setBorder(null);

		//then set the selected one
		label.setForeground(Color.GREEN);
		label.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(Color.black, 20, 1),
				new EmptyBorder(5, 5, 5, 5) ) );
	}

	private void loadAllMp3() throws ClassNotFoundException, SQLException, IOException{

		sql.CreateMp3Table();

		List<Mp3Entity> ent = sql.loadAllMp3();

		Collections.sort(ent, new SortMp3ByName());

		for(Mp3Entity e : ent){
			addRow(e);
		}

		table.setRowHeight(50);
	}


	@Override
	public synchronized void message(Message message) {
		//Value received from the buzzer view.. id mp3 was selected, the Id will be passed back for selection
		if (message.getPropertyName().equals(Constants.B_VISIBLE_FRM_BUZZ_SEL)){ //to switch button visible
			logger.log(Level.CONFIG,"MP3 screen -  message: " + message);

			int msgSelId = (int)message.getMessagePerIndex(0); //id of the mp3 that was selected
			if (table.getModel().getRowCount()> 0 &&  msgSelId >= 0){//if any items in the table, select the one passed from the buzzer view

				DefaultTableModel d = (DefaultTableModel) table.getModel();
				for(int i = 0; i < table.getRowCount() ; i ++){
					int id = (int)d.getValueAt(i, 4);

					if (id == msgSelId){
						int sel = table.convertRowIndexToView(i);						
						table.addRowSelectionInterval(sel,sel);
						break;
					}
				}				

				btnAlarmMp3.setText("Select for alarm");

				if (btnStop.isEnabled()){
					//this usually means that it was playing.. stop it
					btnStop.doClick();
				}

			}else if ( (table.getModel().getRowCount() == 1 && msgSelId == -1 ) || table.getModel().getRowCount() == 0 ){
				btnAlarmMp3.setText("Back to alarm");
			}else{
				btnAlarmMp3.setText("Select for alarm");
			}

			selectedVolume = (int) message.getMessagePerIndex(1);

			btnAlarmMode();
		}else if (message.getPropertyName().equals(Constants.RELOAD_FROM_WEB)){

			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.setRowCount(0);

			try {
				loadAllMp3();
			} catch (ClassNotFoundException | SQLException | IOException e) {

				logger.log(Level.SEVERE, "Error loading all mp3", e);
			}

		}else if(message.getPropertyName().equals(Constants.VOLUME_SENT_FOR_CONFIG_MP3)){
			selectedVolume = (Integer)message.getMessagePerIndex(0);
			logger.log(Level.CONFIG,"MP screen -volume selected from previous card: " + selectedVolume );
		}else if (message.getPropertyName().equals(Constants.MUSIC_TOGGELED)) {
			logger.log(Level.CONFIG, "Music toggeled: " + message);
			String msg = (String) message.getFirstMessage();
			if (msg.equals("mp3off")) {
				logger.log(Level.CONFIG, "Music toggeled ---- OOOOFFFF: " + message);
				lblMp3MainIcon.setVisible(false);
				btnStop.setEnabled(false);
			}
		}else if(message.getPropertyName().equals(Constants.MP3_PLAYER_ERROR)) {
			btnPlay.setEnabled(true);;
			btnStop.setEnabled(false);
			ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
			eh.addError(ErrorType.MP3, new ErrorInfo((String)message.getFirstMessage()));
			try {
				handler.playMp3(false, null, -1);
				fireVolumeIconChange(false);
				lblMp3MainIcon.setVisible(false);
			} catch (IllegalStateException | InterruptedException | IOException e) {
			 logger.log(Level.SEVERE, "errorrororr: " + e);
			}
		}
		//		else if(message.getPropertyName().equals(Constants.MP3_PLAY_NEXT)) {
		//			logger.log(Level.CONFIG, "Play next mp3: " + message.getFirstMessage());
		////			playNext = true;
		//		}
	}

	private void btnMp3Mode(){
		btnStop.setVisible(true);
		btnPlay.setVisible(true);
		btnAlarmMp3.setVisible(false);
		btnVol.setText(BTN_VOL_MP3);
		btnBack.setVisible(true);
		setVolumeForAlarm = false;
	}	
	private void btnAlarmMode(){
		btnStop.setVisible(false);
		btnPlay.setVisible(false);
		btnAlarmMp3.setVisible(true);
		btnVol.setText(BTN_VOL_ALARM);
		btnBack.setVisible(false);
		setVolumeForAlarm = true;
	}
	private int getMp3IdFromTable(){
		int row = table.getSelectedRow();

		if (row > -1){
			return (Integer)table.getModel().getValueAt(table.convertRowIndexToModel(row), 4);
		}
		return -1;
	}
	private int setSliderValue(String c){
		for (Map.Entry<Integer, JLabel> slMp : sliderTable.entrySet()){
			if (slMp.getValue().getText().equalsIgnoreCase(c)){			
				return slMp.getKey();				
			}
		}		
		return 25;
	}


	private void fireVolumeIconChange(boolean displayOn) {
		VolumeIndicator vi = (VolumeIndicator)ct.getSharedObject(Constants.MP3_VOLUME_ICON_TRIGGER);			

		if (vi == null) {
			vi =  new VolumeIndicator();
			vi.setMp3Playing(displayOn);
			ct.putSharedObject(Constants.MP3_VOLUME_ICON_TRIGGER, vi);
		}else {

			VolumeIndicator viNew = new VolumeIndicator();
			viNew.setMp3Playing(displayOn);
			viNew.setRadioPlaying(vi.isRadioPlaying());
			ct.putSharedObject(Constants.MP3_VOLUME_ICON_TRIGGER, viNew);

		}
	}

	enum Selection{
		TRACK(0), ARTIST(1), CATG(2);

		private int rowSel;

		private Selection(int rowSelected){
			rowSel = rowSelected;
		}
		public int getRowSel(){
			return rowSel;
		}
	}
}
