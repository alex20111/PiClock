package net.piclock.main;

import java.io.IOException;
import java.sql.SQLException;

import net.piclock.db.entity.RadioEntity;
import net.piclock.db.sql.RadioSql;

public class TempRadioLoader {

	public static void main(String args[]) throws ClassNotFoundException, SQLException, IOException {
		RadioSql sql = new RadioSql();
		
		sql.CreateRadioTable();
		
		RadioEntity radio = new RadioEntity();
		radio.setRadioLink("http://live.leanstream.co/CKQBFM");
		radio.setRadioName("106.9");
		radio.setTrackNbr(1);
		
		sql.add(radio);
		
	}
}
