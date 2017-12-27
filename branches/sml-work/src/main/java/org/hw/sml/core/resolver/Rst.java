package org.hw.sml.core.resolver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Rst{
	private String sqlString;
	private List<Object> paramObjects;
	
	public Rst() {
		super();
	}
	
	public Rst(String sqlString) {
		super();
		this.sqlString = sqlString;
	}

	public Rst(String sqlString, List<Object> paramObjects) {
		super();
		this.sqlString = sqlString;
		this.paramObjects = paramObjects;
	}
	public String getPrettySqlString(){
		String prettySql=sqlString;
		for(Object obj:paramObjects){
			prettySql=prettySql.replaceFirst("\\?",obj==null?"null":(obj instanceof Date?("to_date('"+(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date)obj))+"','yyyy-mm-dd hh24:mi:ss')"):("'"+String.valueOf(obj)+"'")));
		}
		return prettySql;
	}
	public String getSqlString() {
		return sqlString;
	}
	public void setSqlString(String sqlString) {
		this.sqlString = sqlString;
	}
	public List<Object> getParamObjects() {
		return paramObjects;
	}
	public void setParamObjects(List<Object> paramObjects) {
		this.paramObjects = paramObjects;
	}
	@Override
	public int hashCode(){
		return this.toString().hashCode();
	}
	@Override
	public String toString(){
		return sqlString+paramObjects;
	}
}
