package org.hw.sml.tools;

import java.util.Date;

import javax.sql.DataSource;

import org.hw.sml.model.DbType;

public class DbTools {

	public static DbType getDbType(DataSource dataSource){
		DbType type=DbType.oracle;
		for(String fieldName:new String[]{"driverClassName","driverClass","url","jdbcUrl"}){
			try {
				String typeinfo=(String) ClassUtil.getFieldValue(dataSource, fieldName);
				for(DbType dt:DbType.values()){
					if(typeinfo.contains(dt.name())){
						return dt;
					}
				}
			} catch (Exception e) {
			} 
		}
		return type;
	}
	public static DbType getDbType(String info){
		for(DbType dt:DbType.values()){
			if(info.contains(dt.name())){
				return dt;
			}
		}
		return null;
	}
	public static String getDateFormat(Date datetime,DbType dbType){
		String time=DateTools.sdf_mi().format(datetime);
		return getDateFormat("'"+time+"'", dbType);
	}
	public static String getDateFormat(String datetime,DbType dbType){
		if(isMySql(dbType)){
			return "str_to_date("+datetime+", '%Y-%m-%d %H:%i:%s')";
		}else{
			return "to_date("+datetime+",'yyyy-MM-dd hh24:mi:ss')";
		}
	}
	public static boolean isMySql(DbType dbType){
		return dbType!=null&&(dbType.equals(DbType.mysql)||dbType.equals(DbType.mariadb)||dbType.equals(DbType.gbase));
	}
	public static String getDataStrFormat(String datetime,DbType dbType){
		if(dbType!=null&&dbType.equals(DbType.mysql)||dbType.equals(DbType.mariadb)||dbType.equals(DbType.gbase)){
			return "date_format("+datetime+", '%Y-%m-%d %H:%i:%s')";
		}else{
			return "to_char("+datetime+",'yyyy-MM-dd hh24:mi:ss')";
		}
	}
}
