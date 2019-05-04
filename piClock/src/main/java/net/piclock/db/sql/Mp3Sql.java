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
import net.piclock.db.entity.Mp3Entity;
import net.piclock.main.Constants;

public class Mp3Sql {
	public boolean CreateMp3Table() throws ClassNotFoundException, SQLException, IOException {
		DBConnection con = null;
		boolean exist = false;
		try {
			con = getConnection();

			ResultSet rs = con.createSelectQuery(Mp3Entity.checkIfTableExist()).getSelectResultSet();

			exist = rs.next();

			if (!exist) {
				List<ColumnType> columns = new ArrayList<ColumnType>();					
				columns.add(new ColumnType(Mp3Entity.ID, true).INT().setPkCriteria(new PkCriteria().autoIncrement()));
				columns.add(new ColumnType(Mp3Entity.MP3_CATG, false).VarChar(200));
				columns.add(new ColumnType(Mp3Entity.MP3_NAME, false).VarChar(500));				

				con.createTable(Mp3Entity.TBL_NM, columns);				
			}
		}finally {
			if (con != null) {
				con.close();
			}
		}		
		return exist;
	}

	public int add(Mp3Entity mp3) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		int pk = -1;
		try {
			con = getConnection();

			pk = con.buildAddQuery(Mp3Entity.TBL_NM)
					.setParameter(Mp3Entity.MP3_CATG, mp3.getMp3Catg())
					.setParameter(Mp3Entity.MP3_NAME, mp3.getMp3Name())
					.add();
		}finally {
			con.close();
		}		
		return pk;
	}	
	public void update(Mp3Entity mp3) throws ClassNotFoundException, SQLException {
		DBConnection con = null;
		try {
			con = getConnection();

			int upd = con.buildUpdateQuery(Mp3Entity.TBL_NM)
					.setParameter(Mp3Entity.MP3_CATG, mp3.getMp3Catg())
					.setParameter(Mp3Entity.MP3_NAME, mp3.getMp3Name())
					.addUpdWhereClause("Where "+Mp3Entity.ID+" = :idValue", mp3.getId()).update();
//					.update(Mp3Entity.ID, mp3.getId());

			if (upd < 1) {
				throw new SQLException("Error updating mp3. " + upd);
			}

		}finally {
			con.close();
		}
	}
	public List<Mp3Entity> loadAllMp3() throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		List<Mp3Entity> mp3s = new ArrayList<Mp3Entity>();
		try {
			con = getConnection();

			ResultSet rs = con.createSelectQuery("SELECT * FROM " + Mp3Entity.TBL_NM).getSelectResultSet();

			if (rs!=null) {
				while(rs.next()) {
					Mp3Entity mp3  = new Mp3Entity(rs);
					mp3s.add(mp3);
				}
			}
		}finally {
			con.close();
		}

		return mp3s;
	}
	public List<Mp3Entity> loadAllMp3ByCatg(String catg) throws SQLException, ClassNotFoundException {
		System.out.println("loadAllMp3ByCatg -------------------->  " + catg);
		DBConnection con = null;
		List<Mp3Entity> mp3s = new ArrayList<Mp3Entity>();
		try {
			con = getConnection();

			ResultSet rs = null;
			
			if (catg.equals("All")){
				 rs = con.createSelectQuery("SELECT * FROM " + Mp3Entity.TBL_NM )
							.getSelectResultSet();
			}else{
				rs = con.createSelectQuery("SELECT * FROM " + Mp3Entity.TBL_NM + " where " + Mp3Entity.MP3_CATG + " =  :catgId ")
					.setParameter("catgId", catg)
					.getSelectResultSet();
			}
			if (rs!=null) {
				while(rs.next()) {
					Mp3Entity mp3  = new Mp3Entity(rs);
					mp3s.add(mp3);
				}
			}
		}finally {
			con.close();
		}

		return mp3s;
	}
	public Mp3Entity loadMp3ById(int id) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		Mp3Entity mp3 = null;
		try {
			con = getConnection();

			ResultSet rs = con.createSelectQuery("SELECT * FROM " + Mp3Entity.TBL_NM + " where " + Mp3Entity.ID + " =  :pkKeyId ")
					.setParameter("pkKeyId", id)
					.getSelectResultSet();

			if (rs!=null) {
				while(rs.next()) {
					mp3  = new Mp3Entity(rs);

				}
			}
		}finally {
			con.close();
		}

		return mp3;
	}
	public void delete(int mp3Id) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try {
			con = getConnection();

			String delete = "DELETE FROM " + Mp3Entity.TBL_NM + " where id = :mp3Id";
			con.createSelectQuery(delete)
			.setParameter("mp3Id", mp3Id).delete();

		}finally {
			con.close();
		}

	}	
	private DBConnection getConnection() throws ClassNotFoundException, SQLException{
		return  new DBConnection("jdbc:h2:"+Constants.DB_URL, Constants.DB_USER, Constants.DB_PASS, DbClass.H2 );
	}
}
