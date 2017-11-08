package org.hw.sml.jdbc;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;


public interface JdbcTemplate{
	public <T> T execute(ConnectionCallback<T> connectionCallback);
	public abstract void execute(String sql);
	public abstract void execute(String sql,Object[] params);
	public abstract int update(String sql,Object... params);
	public  abstract int update(String sql);
	public abstract int update(List<String> sqls,List<Object[]> objs);
	public abstract int update(List<String> sqls);
	public abstract int update(String[] sqls,List<Object[]>... objs);
	public abstract int[] batchUpdate(String sql,List<Object[]> objs);
	public abstract int[] batchUpdate(String sql,BatchPreparedStatementSetter bs);
	public abstract <T> T query(String sql, Object[] params, ResultSetExtractor<T> rset);
	public abstract <T> T query(String sql,ResultSetExtractor<T> rset,Object ...params);
	public abstract <T> List<T> query(String sql,Object[] params,RowMapper<T> rowMapper);
	public abstract <T> List<T> query(String sql,RowMapper<T> rowMapper,Object... params);
	public abstract <T> T queryForObject(String sql,Object[] params,RowMapper<T> rowMapper);
	public abstract void queryForCallback(String sql,Object[] params,Callback callBack);
	public abstract void queryForCallback(String sql,Callback callBack);
	public abstract <T> T queryForObject(String sql,RowMapper<T> rowMapper,Object... params);
	public abstract int queryForInt(String sql,Object... params);
	public abstract int queryForInt(String sql);
	public abstract long queryForLong(String sql,Object... params) ;
	public abstract long queryForLong(String sql);
	public abstract Map<String,Object> queryForMap(String sql,Object... params);
	public abstract Map<String,Object> queryForMap(String sql);
	public abstract List<Map<String,Object>> queryForList(String sql,Object... params);
	public abstract List<Map<String,Object>> queryForList(String sql);
	public abstract <T> T queryForObject(String sql,Object[] params,Class<T> clazz);
	public abstract <T> T queryForObject(String sql,Class<T> clazz,Object... params);
	public abstract <T> T queryForObject(String sql,Class<T> clazz);
	public abstract <T> List<T> queryForList(String sql,Object[] params,Class<T> clazz);
	public abstract <T> List<T> queryForList(String sql,Class<T> clazz,Object... params);
	public abstract <T> List<T> queryForList(String sql,Class<T> clazz);
	public abstract void setDataSource(DataSource dataSource);	
}
