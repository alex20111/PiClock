package net.piclock.db.sql;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import home.db.ColumnType;
import home.db.DBConnection;
import home.db.DbClass;
import home.db.PkCriteria;
import net.piclock.db.entity.RadioEntity;
import net.piclock.main.Constants;

public class RadioSql {
	
	public boolean CreateConfigTable() throws ClassNotFoundException, SQLException, IOException {
		DBConnection con = null;
		boolean exist = false;
		try {
			con = getConnection();
			
			ResultSet rs = con.createSelectQuery(RadioEntity.checkIfTableExist()).getSelectResultSet();
			
			exist = rs.next();
			
			System.out.println("Exist:  " + exist);
			
			if (!exist) {
//				PkCriteria crt = new PkCriteria();
//				crt.autoIncrement();
				List<ColumnType> columns = new ArrayList<ColumnType>();					
				columns.add(new ColumnType(RadioEntity.ID, true).INT().setPkCriteria(new PkCriteria().autoIncrement()));
				columns.add(new ColumnType(RadioEntity.RADIO_NAME, false).VarChar(200));
				columns.add(new ColumnType(RadioEntity.RADIO_LINK, false).VarChar(500));				
				
				con.createTable(RadioEntity.TBL_NM, columns);				
			}
		}finally {
			if (con != null) {
			con.close();
			}
		}		
		return exist;
	}
	
	public int add(RadioEntity radio) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		int pk = -1;
		try {
			con = getConnection();

			pk = con.buildAddQuery(RadioEntity.TBL_NM)
			.setParameter(RadioEntity.RADIO_NAME, radio.getRadioName())
			.setParameter(RadioEntity.RADIO_LINK, radio.getRadioLink())

			.add();
		}finally {
			con.close();
		}		
		return pk;
	}	
	public void update(RadioEntity radio) throws ClassNotFoundException, SQLException {
		DBConnection con = null;
		try {
			con = getConnection();

			int upd = con.buildUpdateQuery(RadioEntity.TBL_NM)
					.setParameter(RadioEntity.RADIO_NAME, radio.getRadioName())
					.setParameter(RadioEntity.RADIO_LINK, radio.getRadioLink()).
					addUpdWhereClause("Where "+RadioEntity.ID+" = :idValue", radio.getId()).update();
//					.update(RadioEntity.ID, radio.getId());//TODO

			if (upd < 1) {
				throw new SQLException("Error updating config. " + upd);
			}

		}finally {
			con.close();
		}
	}
	public List<RadioEntity> loadAllRadios() throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		List<RadioEntity> radios = new ArrayList<RadioEntity>();
		try {
			con = getConnection();

			ResultSet rs = con.createSelectQuery("SELECT * FROM " + RadioEntity.TBL_NM).getSelectResultSet();

			if (rs!=null) {
				while(rs.next()) {
					RadioEntity radio  = new RadioEntity(rs);
					radios.add(radio);
				}
			}
		}finally {
			con.close();
		}

		return radios;
	}
	public RadioEntity loadRadioById(int id) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		RadioEntity radio = null;
		try {
			con = getConnection();

			ResultSet rs = con.createSelectQuery("SELECT * FROM " + RadioEntity.TBL_NM + " where " + RadioEntity.ID + " =  :pkKeyId ")
					.setParameter("pkKeyId", id)
					.getSelectResultSet();
			
			if (rs!=null) {
				while(rs.next()) {
					 radio  = new RadioEntity(rs);
			
				}
			}
		}finally {
			con.close();
		}

		return radio;
	}
	public void delete(int radioId) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try {
			con = getConnection();

			String delete = "DELETE FROM " + RadioEntity.TBL_NM + " where id = :radioId";
			con.createSelectQuery(delete)
			.setParameter("radioId", radioId).delete();

		}finally {
			con.close();
		}

	}	
	private DBConnection getConnection() throws ClassNotFoundException, SQLException{
		return  new DBConnection("jdbc:h2:"+Constants.DB_URL, Constants.DB_USER, Constants.DB_PASS, DbClass.H2 );
	}
}
