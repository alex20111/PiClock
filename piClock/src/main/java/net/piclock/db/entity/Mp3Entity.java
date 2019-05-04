package net.piclock.db.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Mp3Entity {
	public static final String TBL_NM = "MP3_LIST";
	public static final String ID = "id";
	public static final String MP3_NAME = "mp3_name";
	public static final String MP3_CATG = "mp3_category";	
	
	private int id = -1;
	private String mp3Name = "";
	private String mp3Catg = "";
	
	public Mp3Entity(){}
	
	public Mp3Entity(ResultSet rs) throws SQLException{
		this.id = rs.getInt(ID);
		this.mp3Name = rs.getString(MP3_NAME);
		this.mp3Catg = rs.getString(MP3_CATG);
	}	
	public static String checkIfTableExist() { 
		return "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME ='"+TBL_NM+"'"; 
	} 	
	public String getMp3Name() {
		return mp3Name;
	}

	public void setMp3Name(String mp3Name) {
		this.mp3Name = mp3Name;
	}

	public String getMp3Catg() {
		return mp3Catg;
	}

	public void setMp3Catg(String mp3Catg) {
		this.mp3Catg = mp3Catg;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return mp3Name;
	}	
}
