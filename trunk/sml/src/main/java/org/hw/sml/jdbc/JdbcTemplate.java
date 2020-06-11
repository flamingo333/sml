package org.hw.sml.jdbc;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;


public interface JdbcTemplate{
	  <T> T execute(ConnectionCallback<T> connectionCallback);
	  void execute(String sql);
	  void execute(String sql,Object[] params);
	  int update(String sql,Object... params);
	  int update(String sql);
	  int update(List<String> sqls,List<Object[]> objs);
	  int update(List<String> sqls);
	  int update(String[] sqls,List<Object[]>... objs);
	  int updates(List<String> sqls,List<List<Object[]>> objs);
	  int[] batchUpdate(String sql,List<Object[]> objs);
	  int[] batchUpdate(String sql,BatchPreparedStatementSetter bs);
	  <T> T query(String sql, Object[] params, ResultSetExtractor<T> rset);
	  <T> T query(String sql,ResultSetExtractor<T> rset,Object ...params);
	  <T> List<T> query(String sql,Object[] params,RowMapper<T> rowMapper);
	  <T> List<T> query(String sql,RowMapper<T> rowMapper,Object... params);
	  <T> T queryForObject(String sql,Object[] params,RowMapper<T> rowMapper);
	  void queryForCallback(String sql,Object[] params,Callback callBack);
	  void queryForCallback(String sql,Callback callBack);
	  <T> T queryForObject(String sql,RowMapper<T> rowMapper,Object... params);
	  int queryForInt(String sql,Object... params);
	  int queryForInt(String sql);
	  long queryForLong(String sql,Object... params) ;
	  long queryForLong(String sql);
	  Map<String,Object> queryForMap(String sql,Object... params);
	  Map<String,Object> queryForMap(String sql);
	  List<Map<String,Object>> queryForList(String sql,Object... params);
	  List<Map<String,Object>> queryForList(String sql);
	  <T> T queryForObject(String sql,Object[] params,Class<T> clazz);
	  <T> T queryForObject(String sql,Class<T> clazz,Object... params);
	  <T> T queryForObject(String sql,Class<T> clazz);
	  <T> List<T> queryForList(String sql,Object[] params,Class<T> clazz);
	  <T> List<T> queryForList(String sql,Object[] params,Class<T> clazz,int limit);
	  <T> List<T> queryForList(String sql,Class<T> clazz,Object... params);
	  <T> List<T> queryForList(String sql,Class<T> clazz);
	  void setDataSource(DataSource dataSource);	
	  DataSource getDataSource();	
}
