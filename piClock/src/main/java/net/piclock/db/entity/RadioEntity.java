package net.piclock.db.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RadioEntity {

	public static final String TBL_NM = "RADIO_STATION";
	public static final String ID = "id";
	public static final String RADIO_NAME = "radio_name";
	public static final String RADIO_LINK = "radio_link";	
	
	private int id = -1;
	private String radioName = "";
	private String radioLink = "";
	
	public RadioEntity(){}
	
	public RadioEntity(ResultSet rs) throws SQLException{
		this.id = rs.getInt(ID);
		this.radioName = rs.getString(RADIO_NAME);
		this.radioLink = rs.getString(RADIO_LINK);
	}	
	public static String checkIfTableExist() { 
		return "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME ='"+TBL_NM+"'"; 
	} 	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getRadioName() {
		return radioName;
	}
	public void setRadioName(String radioName) {
		this.radioName = radioName;
	}
	public String getRadioLink() {
		return radioLink;
	}
	public void setRadioLink(String radioLink) {
		this.radioLink = radioLink;
	}
	@Override
	public String toString() {
		return radioName;
	}	
}
