package net.piclock.db.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Mp3Entity {
	public static final String TBL_NM 		= "MP3_LIST";
	public static final String ID 			= "id";
	public static final String MP3_NAME 	= "mp3_name";
	public static final String MP3_FILE_NAME 	= "mp3_file_name";
	public static final String MP3_ARTIST 	= "mp3_artist";	
	public static final String MP3_GENRE 	= "mp3_genre";
	public static final String MP3_GENRE_DESC = "mp3_genre_desc";
	public static final String MP3_LENGTH 	= "mp3_length";

	private int id = -1;
	private String mp3Name 		= "";
	private String mp3FileName 		= "";
	private String artist 		= "";
	private int mp3Genre 		= -1;
	private String mp3GenreDesc 	= "";
	private int mp3Length 		= -1;

	public Mp3Entity(){}

	public Mp3Entity(ResultSet rs) throws SQLException{
		this.id = rs.getInt(ID);
		this.mp3Name = rs.getString(MP3_NAME);
		this.mp3FileName = rs.getString(MP3_FILE_NAME);
		this.artist = rs.getString(MP3_ARTIST);
		this.mp3Genre = rs.getInt(MP3_GENRE);
		this.mp3GenreDesc = rs.getString(MP3_GENRE_DESC);
		this.mp3Length = rs.getInt(MP3_LENGTH);
	}	
	public static String checkIfTableExist() { 
		return "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME ='"+TBL_NM+"'"; 
	} 	
	public String getMp3Name() {
		return mp3Name;
	}

	public String getMp3FileName() {
		return mp3FileName;
	}

	public void setMp3FileName(String mp3FileName) {
		this.mp3FileName = mp3FileName;
	}

	public void setMp3Name(String mp3Name) {
		this.mp3Name = mp3Name;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public int getMp3Length() {
		return mp3Length;
	}

	public void setMp3Length(int mp3Length) {
		this.mp3Length = mp3Length;
	}

	public int getMp3Genre() {
		return mp3Genre;
	}

	public void setMp3Genre(int mp3Genre) {
		this.mp3Genre = mp3Genre;
	}

	public String getMp3GenreDesc() {
		return mp3GenreDesc;
	}

	public void setMp3GenreDesc(String mp3GenreDesc) {
		this.mp3GenreDesc = mp3GenreDesc;
	}

	@Override
	public String toString() {
		return "Mp3Entity [id=" + id + ", mp3Name=" + mp3Name + ", mp3FileName=" + mp3FileName + ", artist=" + artist
				+ ", mp3Genre=" + mp3Genre + ", mp3GenreDesc=" + mp3GenreDesc + ", mp3Length=" + mp3Length + "]";
	}	
}
