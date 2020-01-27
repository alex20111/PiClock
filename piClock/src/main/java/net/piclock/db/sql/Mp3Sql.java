package net.piclock.db.sql;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import home.db.ColumnType;
import home.db.DBConnection;
import home.db.DbClass;
import home.db.PkCriteria;
import net.piclock.db.entity.Mp3Entity;
import net.piclock.main.Constants;
import net.piclock.server.Mp3GenreFilter;

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
				columns.add(new ColumnType(Mp3Entity.ID, true).INT().setPKCriteria(new PkCriteria().autoIncrement()));
				columns.add(new ColumnType(Mp3Entity.MP3_GENRE, false).INT());
				columns.add(new ColumnType(Mp3Entity.MP3_GENRE_DESC, false).VarChar(500));
				columns.add(new ColumnType(Mp3Entity.MP3_ARTIST, false).VarChar(200));
				columns.add(new ColumnType(Mp3Entity.MP3_NAME, false).VarChar(500));
				columns.add(new ColumnType(Mp3Entity.MP3_FILE_NAME, false).VarChar(200));
				columns.add(new ColumnType(Mp3Entity.MP3_LENGTH, false).INT());

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
					.setParameter(Mp3Entity.MP3_GENRE, mp3.getMp3Genre())
					.setParameter(Mp3Entity.MP3_GENRE_DESC, mp3.getMp3GenreDesc())
					.setParameter(Mp3Entity.MP3_ARTIST, mp3.getArtist())
					.setParameter(Mp3Entity.MP3_NAME, mp3.getMp3Name())
					.setParameter(Mp3Entity.MP3_LENGTH, mp3.getMp3Length())
					.setParameter(Mp3Entity.MP3_FILE_NAME, mp3.getMp3FileName())
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
					.setParameter(Mp3Entity.MP3_GENRE, mp3.getMp3Genre())
					.setParameter(Mp3Entity.MP3_GENRE_DESC, mp3.getMp3GenreDesc())
					.setParameter(Mp3Entity.MP3_ARTIST, mp3.getArtist())
					.setParameter(Mp3Entity.MP3_NAME, mp3.getMp3Name())
					.setParameter(Mp3Entity.MP3_LENGTH, mp3.getMp3Length())
					.setParameter(Mp3Entity.MP3_FILE_NAME, mp3.getMp3FileName())
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

	public List<Mp3GenreFilter> getAllAvailableGenre() throws ClassNotFoundException, SQLException{
		//get the available genre from the DB.

		DBConnection con = null;
		Set<Mp3GenreFilter> filters = new HashSet<Mp3GenreFilter>();
		try {
			con = getConnection();

			ResultSet rs = null;

			rs = con.createSelectQuery("SELECT " + Mp3Entity.MP3_GENRE + " , " + Mp3Entity.MP3_GENRE_DESC
					+ " FROM " + Mp3Entity.TBL_NM + " GROUP BY " + Mp3Entity.MP3_GENRE +" , "+ Mp3Entity.MP3_GENRE_DESC   ).getSelectResultSet();					

			int id = 0;
			if (rs!=null) {
				while(rs.next()) {
					Mp3GenreFilter filter = new Mp3GenreFilter(id,rs.getInt(Mp3Entity.MP3_GENRE), rs.getString(Mp3Entity.MP3_GENRE_DESC));
					filters.add(filter);
					id++;
				}
			}		

		}finally {
			con.close();
		}	

		List<Mp3GenreFilter> filtersList = new ArrayList<>();
		filtersList.addAll(filters);		

		return filtersList;
	}

	public List<Mp3Entity> loadAllMp3ByGenre(Mp3GenreFilter filter) throws SQLException, ClassNotFoundException {

		DBConnection con = null;
		List<Mp3Entity> mp3s = new ArrayList<Mp3Entity>();
		try {
			con = getConnection();

			ResultSet rs = null;

			StringBuilder query = new StringBuilder("SELECT * FROM " + Mp3Entity.TBL_NM);

			if (filter.searchByGenre()){
				query.append(" WHERE " + Mp3Entity.MP3_GENRE + " = " + filter.getKey() + " ;");

			}else if (!Mp3GenreFilter.ALL.equals(filter.getDisplayName())){
				query.append(" WHERE " + Mp3Entity.MP3_GENRE_DESC + " = '" + filter.getSearchName() + "' ;");
			}

			rs = con.createSelectQuery(query.toString())
					.getSelectResultSet();

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
	public void deleteMultiple(List<Integer> mp3Ids) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try {
			con = getConnection();
			List<Object> idsObject = new ArrayList<>(mp3Ids);

			con.deleteInBatch(Mp3Entity.TBL_NM, Mp3Entity.ID, idsObject);

		}finally {
			con.close();
		}

	}
	public Mp3Entity loadMp3FileNameById(int id) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		Mp3Entity mp3 = new Mp3Entity();
		try {
			con = getConnection();

			ResultSet rs = con.createSelectQuery("SELECT "+ Mp3Entity.MP3_NAME+ " , " + Mp3Entity.MP3_FILE_NAME + " FROM " + Mp3Entity.TBL_NM + " where " + Mp3Entity.ID + " =  :pkKeyId ")
					.setParameter("pkKeyId", id)
					.getSelectResultSet();

			if (rs!=null) {
				while(rs.next()) {

					mp3.setMp3FileName(rs.getString(Mp3Entity.MP3_FILE_NAME));
					mp3.setMp3Name(rs.getString(Mp3Entity.MP3_NAME));

				}
			}
		}finally {
			con.close();
		}

		return mp3;
	}
	//	public List<Mp3Entity> loadMp3FileNameListById(List<Mp3Entity> mp3List) throws SQLException, ClassNotFoundException {
	//		DBConnection con = null;
	//		List<Mp3Entity> mp3 = new ArrayList<>();
	//		try {
	//			con = getConnection();
	//			boolean first = false;
	//			
	//			StringBuilder query = new StringBuilder();
	//			query.append("SELECT "+ Mp3Entity.MP3_NAME+ " , " + Mp3Entity.MP3_FILE_NAME + " FROM "
	//					+ Mp3Entity.TBL_NM + " where " );
	//			
	//			for(Mp3Entity m : mp3List){
	//				if (first){
	//					first = false;
	//					query.append(Mp3Entity.ID + " =  " + m.getId());
	//				}else{
	//					query.append(" OR " + Mp3Entity.ID + " =  " + m.getId());
	//				}
	//			}
	//			
	//			ResultSet rs = con.createSelectQuery(query.toString())					
	//					.getSelectResultSet();
	//
	//			if (rs!=null) {
	//				while(rs.next()) {
	//					Mp3Entity m2 = new Mp3Entity();
	//					m2.setMp3FileName(rs.getString(Mp3Entity.MP3_FILE_NAME));
	//					m2.setMp3Name(rs.getString(Mp3Entity.MP3_NAME));
	//					mp3.add(m2);
	//			
	//				}
	//			}
	//		}finally {
	//			con.close();
	//		}
	//
	//		return mp3;
	//	}


	private DBConnection getConnection() throws ClassNotFoundException, SQLException{
		return  new DBConnection("jdbc:h2:" +Constants.DB_URL, Constants.DB_USER, Constants.DB_PASS, DbClass.H2 );
	}
}
