package org.hw.sml.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hw.sml.tools.LinkedCaseInsensitiveMap;
import org.hw.sml.tools.MapUtils;

public abstract class JdbcAccessor {
	private String charTransCode;
	private String[] charTransCodes;
	public JdbcAccessor(){
		
	}
	
	public JdbcAccessor(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	protected DataSource dataSource;
	public DataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	public static Object getResultSetValue(ResultSet rs,int index) throws SQLException{
		return getResultSetValue0(rs, index,null);
	}
	public static  Object getResultSetValue0(ResultSet rs, int index,String[] ctc) throws SQLException {
		Object obj = rs.getObject(index);
		String className = null;
		if (obj != null) {
			className = obj.getClass().getName();
		}else{
			return obj;
		}
		if (obj instanceof Blob) {
			obj = rs.getBytes(index);
		}
		else if (obj instanceof Clob||obj instanceof String) {
			obj = rs.getString(index);
			if(obj!=null&&ctc!=null&&ctc.length==2){
				obj=toBytes(obj.toString(), ctc[0],ctc[1]);
			}
		}
		else if (className != null &&
				("oracle.sql.TIMESTAMP".equals(className) ||
				"oracle.sql.TIMESTAMPTZ".equals(className))) {
			obj = rs.getTimestamp(index);
		}else if(className!=null&&obj instanceof ResultSet){
			ResultSet resultSet=(ResultSet) obj;
			List<Map<String,Object>> resultT=MapUtils.newArrayList();
			while(resultSet.next()){
				resultT.add(new MapRowMapper().mapRow(resultSet,0));
			}
			DataSourceUtils.safeClose(resultSet);
			obj=resultT;
		}
		else if (className != null && className.startsWith("oracle.sql.DATE")) {
			String metaDataClassName = rs.getMetaData().getColumnClassName(index);
			if ("java.sql.Timestamp".equals(metaDataClassName) ||
					"oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
				obj = rs.getTimestamp(index);
			}
			else {
				obj = rs.getDate(index);
			}
		}
		else if (obj != null && obj instanceof java.sql.Date) {
			if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
				obj = rs.getTimestamp(index);
			}
		}else if(obj!=null&&obj instanceof java.sql.Array){
			obj=((java.sql.Array)obj).getArray();
		}
		return obj;
	}
	public static String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
		String name = resultSetMetaData.getColumnLabel(columnIndex);
		if (name == null || name.length() < 1) {
			name = resultSetMetaData.getColumnName(columnIndex);
		}
		return name;
	}
	public static void setPreparedState(PreparedStatement ps,int paramIndex,Object inValue) throws SQLException{
		if(inValue==null){
			ps.setNull(paramIndex,Types.NULL);
		}else if (isStringValue(inValue.getClass())) {
			String v=inValue.toString();
			//if(v.length()<4000)
			ps.setString(paramIndex,v);
			//else
			//	ps.setClob(paramIndex,new StringReader(v));
		}
		else if (isDateValue(inValue.getClass())) {
			ps.setTimestamp(paramIndex, new java.sql.Timestamp(((java.util.Date) inValue).getTime()));
		}
		else if (inValue instanceof Calendar) {
			Calendar cal = (Calendar) inValue;
			ps.setTimestamp(paramIndex, new java.sql.Timestamp(cal.getTime().getTime()), cal);
		}else if(inValue instanceof InputStream){
			ps.setBlob(paramIndex,(InputStream)inValue);
		}else if(inValue instanceof byte[]){
			ps.setBlob(paramIndex,new ByteArrayInputStream((byte[])inValue));
		}
		else {
			ps.setObject(paramIndex, inValue);
		}
	}
	private static boolean isStringValue(Class<?> inValueType) {
		return (CharSequence.class.isAssignableFrom(inValueType) ||
				StringWriter.class.isAssignableFrom(inValueType));
	}
	private static boolean isDateValue(Class<?> inValueType) {
		return (java.util.Date.class.isAssignableFrom(inValueType) &&
				!(java.sql.Date.class.isAssignableFrom(inValueType) ||
						java.sql.Time.class.isAssignableFrom(inValueType) ||
						java.sql.Timestamp.class.isAssignableFrom(inValueType)));
	}
	private static String toBytes(String val,String fromCode,String toCode){
		if(val==null){
			return val;
		}
		try {
			return new String(val.getBytes(fromCode),toCode);
		} catch (UnsupportedEncodingException e) {
			return val;
		}
	}

	public String[] getCharTransCodes() {
		if(charTransCodes==null&&charTransCode!=null){
			charTransCodes=charTransCode.split(",");
		}
		return charTransCodes;
	}

	public void setCharTransCode(String charTransCode) {
		this.charTransCode = charTransCode;
	}

	private static  class MapRowMapper implements  RowMapper<Map<String,Object>> {
		public Map<String, Object> mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			Map<String, Object> mapOfColValues = new LinkedCaseInsensitiveMap<Object>();
			for (int i = 1; i <= columnCount; i++) {
				String key = lookupColumnName(rsmd, i);
				Object obj = getResultSetValue(rs, i);
				mapOfColValues.put(key, obj);
			}
			return mapOfColValues;
		}
	}
}
