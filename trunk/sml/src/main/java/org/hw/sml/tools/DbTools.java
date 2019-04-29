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
	public static String getDateFormat(Date datetime,DbType dbType){
		if(dbType.equals(DbType.mysql)||dbType.equals(DbType.mariadb)){
			return "str_to_date('"+DateTools.sdf_mi().format(datetime)+"', '%Y-%m-%d %H:%i:%s')";
		}else{
			return "to_date('"+DateTools.sdf_mi().format(datetime)+"','yyyy-MM-dd hh24:mi:ss')";
		}
	}
}
