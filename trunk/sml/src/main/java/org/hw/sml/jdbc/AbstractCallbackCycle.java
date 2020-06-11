package org.hw.sml.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.hw.sml.jdbc.impl.DefaultJdbcTemplate;
import org.hw.sml.tools.MapUtils;

public abstract class AbstractCallbackCycle implements CallbackCycle {
	protected List<String> headers;
	@Override
	public void call(ResultSet rs, int rowNum) throws SQLException {
		if (rowNum==0) {
			headers=MapUtils.newArrayList();
            ResultSetMetaData rsmd = rs.getMetaData();
            int iterNum = rsmd.getColumnCount();
            for (int i = 0; i < iterNum; i++) {
                String columnLabel = rsmd.getColumnLabel(i + 1);
                headers.add(columnLabel);
            }
		}
		callProxy(rs, rowNum);
	}
	public abstract void callProxy(ResultSet rs,int rowNum) throws SQLException;
	
	public Object getResultSetValue(ResultSet rs,int columnIndex) throws SQLException{
		return DefaultJdbcTemplate.getResultSetValue(rs, columnIndex);
	}
}
