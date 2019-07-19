package net.piclock.db.sql;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import home.db.ColumnType;
import home.db.DBConnection;
import home.db.DbClass;
import home.db.PkCriteria;
import net.piclock.db.entity.AlarmEntity;
import net.piclock.main.Constants;

public class AlarmSql {
	
	private static final Logger logger = Logger.getLogger( AlarmSql.class.getName() );
	
	public boolean CreateAlarmTable() throws ClassNotFoundException, SQLException, IOException {
		
		logger.log(Level.INFO, "CreateAlarmTable");
		
		DBConnection con = null;
		boolean exist = false;
		try {
			con = getConnection();
			
			ResultSet rs = con.createSelectQuery(AlarmEntity.checkIfTableExist()).getSelectResultSet();
			
			exist = rs.next();
			
			if (!exist) {
				logger.log(Level.INFO, "Alarm table does not exist , creating");
				List<ColumnType> columns = new ArrayList<ColumnType>();					
				columns.add(new ColumnType(AlarmEntity.ID, true).INT().setPkCriteria(new PkCriteria().autoIncrement()));
				columns.add(new ColumnType(AlarmEntity.TIME_HOUR, false).VarChar(2));
				columns.add(new ColumnType(AlarmEntity.TIME_MIN, false).VarChar(2));
				columns.add(new ColumnType(AlarmEntity.ALARM_SOUND, false).VarChar(10));
				columns.add(new ColumnType(AlarmEntity.REPEAT, false).VarChar(100));
				columns.add(new ColumnType(AlarmEntity.ACTIVE, false).Boolean());
				columns.add(new ColumnType(AlarmEntity.RADIO_ID, false).INT());
				columns.add(new ColumnType(AlarmEntity.MP3_ID, false).INT());
				columns.add(new ColumnType(AlarmEntity.VOLUME, false).INT());
				columns.add(new ColumnType(AlarmEntity.ALARM_SHUTDOWN, false).INT());
				
				con.createTable(AlarmEntity.TBL_NM, columns);				
			}
		}finally {
			if (con != null) {
			con.close();
			}
		}		
		return exist;
	}
	
	public int add(AlarmEntity alarm) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		int pk = -1;
		try {
			con = getConnection();

			pk = con.buildAddQuery(AlarmEntity.TBL_NM)
			.setParameter(AlarmEntity.ALARM_SOUND, alarm.getAlarmSound())
			.setParameter(AlarmEntity.TIME_HOUR, alarm.getHour())
			.setParameter(AlarmEntity.TIME_MIN, alarm.getMinutes())
			.setParameter(AlarmEntity.ACTIVE, alarm.isActive())
			.setParameter(AlarmEntity.REPEAT, alarm.getRepeatString())
			.setParameter(AlarmEntity.RADIO_ID, alarm.getRadioId())
			.setParameter(AlarmEntity.MP3_ID, alarm.getRadioId())
			.setParameter(AlarmEntity.VOLUME, alarm.getVolume())
			.setParameter(AlarmEntity.ALARM_SHUTDOWN, alarm.getAlarmShutdown())

			.add();
		}finally {
			con.close();
		}		
		return pk;
	}	
	public void update(AlarmEntity alarm) throws ClassNotFoundException, SQLException {
		DBConnection con = null;
		try {
			con = getConnection();

			int upd = con.buildUpdateQuery(AlarmEntity.TBL_NM)
					.setParameter(AlarmEntity.ALARM_SOUND, alarm.getAlarmSound())
					.setParameter(AlarmEntity.TIME_HOUR, alarm.getHour())
					.setParameter(AlarmEntity.TIME_MIN, alarm.getMinutes())
					.setParameter(AlarmEntity.ACTIVE, alarm.isActive())
					.setParameter(AlarmEntity.REPEAT, alarm.getRepeatString())
					.setParameter(AlarmEntity.RADIO_ID, alarm.getRadioId())
					.setParameter(AlarmEntity.MP3_ID, alarm.getMp3Id())
					.setParameter(AlarmEntity.VOLUME, alarm.getVolume())
					.setParameter(AlarmEntity.ALARM_SHUTDOWN, alarm.getAlarmShutdown()).
					addUpdWhereClause("Where "+AlarmEntity.ID+" = :idValue", alarm.getId()).update();
//					.update(AlarmEntity.ID, alarm.getId());

			if (upd < 1) {
				throw new SQLException("Error updating config. " + upd);
			}

		}finally {
			con.close();
		}
	}
	public List<AlarmEntity> loadAllAlarms() throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		List<AlarmEntity> alarms = new ArrayList<AlarmEntity>();
		try {
			con = getConnection();

			ResultSet rs = con.createSelectQuery("SELECT * FROM " + AlarmEntity.TBL_NM).getSelectResultSet();

			if (rs!=null) {
				while(rs.next()) {
					AlarmEntity alarm  = new AlarmEntity(rs);
					alarms.add(alarm);
				}
			}
		}finally {
			con.close();
		}

		return alarms;
	}
	public AlarmEntity loadAlarmById(int id) throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		AlarmEntity alarm = null;
		try {
			con = getConnection();

			ResultSet rs = con.createSelectQuery("SELECT * FROM " + AlarmEntity.TBL_NM + " where " + AlarmEntity.ID + " =  :pkKeyId ")
					.setParameter("pkKeyId", id)
					.getSelectResultSet();
			
			if (rs!=null) {
				while(rs.next()) {
					 alarm  = new AlarmEntity(rs);
			
				}
			}
		}finally {
			con.close();
		}

		return alarm;
	}
	public AlarmEntity loadActiveAlarm() throws SQLException, ClassNotFoundException {
		DBConnection con = null;
		AlarmEntity alarm = null;
		try {
			con = getConnection();

			ResultSet rs = con.createSelectQuery("SELECT * FROM " + AlarmEntity.TBL_NM + " where " + AlarmEntity.ACTIVE + " =  :activeId ")
					.setParameter("activeId", true)
					.getSelectResultSet();
			
			if (rs!=null) {
				while(rs.next()) {
					 alarm  = new AlarmEntity(rs);
			
				}
			}
		}finally {
			con.close();
		}

		return alarm;
	}
	public void delete(int alarmId) throws SQLException, ClassNotFoundException{

		DBConnection con = null;
		try {
			con = getConnection();

			String delete = "DELETE FROM " + AlarmEntity.TBL_NM + " where id = :alarmId";
			con.createSelectQuery(delete)
			.setParameter("alarmId", alarmId).delete();

		}finally {
			con.close();
		}

	}	
	
	public List<AlarmEntity> loadAlarmByRadioId(int radioId) throws SQLException, ClassNotFoundException{
		DBConnection con = null;
		List<AlarmEntity> alarms = new ArrayList<AlarmEntity>();
		try {
			con = getConnection();

			ResultSet rs = con.createSelectQuery("SELECT * FROM " + AlarmEntity.TBL_NM + " where " + AlarmEntity.RADIO_ID + " =  :radId ")
					.setParameter("radId", radioId)
					.getSelectResultSet();

			if (rs!=null) {
				while(rs.next()) {
					AlarmEntity alarm  = new AlarmEntity(rs);
					alarms.add(alarm);
				}
			}
		}finally {
			con.close();
		}

		return alarms;
	}
	private DBConnection getConnection() throws ClassNotFoundException, SQLException{
		return  new DBConnection("jdbc:h2:"+Constants.DB_URL, Constants.DB_USER, Constants.DB_PASS, DbClass.H2 );
	}
}