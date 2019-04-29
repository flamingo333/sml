package org.hw.sml.jdbc.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hw.sml.jdbc.BatchPreparedStatementSetter;
import org.hw.sml.jdbc.Callback;
import org.hw.sml.jdbc.CallbackCycle;
import org.hw.sml.jdbc.ConnectionCallback;
import org.hw.sml.jdbc.DataSourceUtils;
import org.hw.sml.jdbc.JdbcAccessor;
import org.hw.sml.jdbc.JdbcTemplate;
import org.hw.sml.jdbc.ResultSetExtractor;
import org.hw.sml.jdbc.RowMapper;
import org.hw.sml.jdbc.exception.SqlException;
import org.hw.sml.tools.Assert;
import org.hw.sml.tools.ClassUtil;
import org.hw.sml.tools.LinkedCaseInsensitiveMap;

public class DefaultJdbcTemplate  extends JdbcAccessor  implements JdbcTemplate{
		private boolean transactionInversion;
		private  int queryReturnLimit=Integer.MAX_VALUE;
		{
			queryReturnLimit=Integer.parseInt(System.getProperty("sml.jdbc.queryreturnlimit", String.valueOf(Integer.MAX_VALUE)));
		}
		public DefaultJdbcTemplate(){}
		public DefaultJdbcTemplate(DataSource dataSource){
			super(dataSource);
		}
		public void execute(String sql){
			execute(sql,null);
		}
		public void execute(String sql,Object[] params){
			update(sql, params);
		}
		public int update(String sql){
			return update(sql,new Object[]{});
		}
		public int update(final String sql,final Object... params){
			return execute(new ConnectionCallback<Integer>() {
				public Integer doInConnection(Connection con) {
					PreparedStatement pst = null;
					try {
						pst = con.prepareStatement(sql);
						if(params!=null){
							for(int i=0;i<params.length;i++){
								setPreparedState(pst, i+1,params[i]);
							}
						}
						return pst.executeUpdate();
					} catch (SQLException e) {
						throw new SqlException(e,sql);
					}finally{
						safeClose(pst);
					}
				}
			});
		}
		public int update(List<String> sqls){
			return update(sqls,null);
		}
		public int update(final String[] sqls,final List<Object[]>... objs) {
			if(objs!=null)
			Assert.isTrue(sqls.length==objs.length,"sqls size["+sqls.length+"] != objs size["+objs.length+"]");
			return execute(new ConnectionCallback<Integer>() {
				public Integer doInConnection(Connection con) {
					int result=0;
					PreparedStatement pst = null;
					try {
						for(int i=0;i<sqls.length;i++){
							String sql=sqls[i];
							pst=con.prepareStatement(sql);
							for(int j=0;j<objs[i].size();j++){
								Object[] params=objs[i].get(j);
								for(int m=0;m<params.length;m++){
									setPreparedState(pst, m+1, params[m]);
								}
								pst.addBatch();
							}
							result+=pst.executeBatch().length;
						}
						return result;
					}catch (SQLException e) {
						throw new SqlException(e);
					}finally{
						safeClose(pst);
					}
				}
			});
		}
		@Override
		public int updates(final List<String> sqls,final  List<List<Object[]>> objs) {
			if(objs!=null)
			Assert.isTrue(sqls.size()==objs.size(),"sqls size["+sqls.size()+"] != objs size["+objs.size()+"]");
			return execute(new ConnectionCallback<Integer>() {
					public Integer doInConnection(Connection con) {
						int result=0;
						PreparedStatement pst = null;
						try {
							for(int i=0;i<sqls.size();i++){
								String sql=sqls.get(i);
								pst=con.prepareStatement(sql);
								for(int j=0;j<objs.get(i).size();j++){
									Object[] params=objs.get(i).get(j);
									for(int m=0;m<params.length;m++){
										setPreparedState(pst, m+1, params[m]);
									}
									pst.addBatch();
								}
								result+=pst.executeBatch().length;
							}
							return result;
						}catch (SQLException e) {
							throw new SqlException(e);
						}finally{
							safeClose(pst);
						}
					}
				});
		}
		public int update(final List<String> sqls,final List<Object[]> objs) {
			if(objs!=null)
			Assert.isTrue(sqls.size()==objs.size(),"sqls size["+sqls.size()+"] != objs size["+objs.size()+"]");
			return execute(new ConnectionCallback<Integer>() {
				public Integer doInConnection(Connection conn) {
					int result=0;
					PreparedStatement pst=null;
					try {
						for(int i=0;i<sqls.size();i++){
							pst=conn.prepareStatement(sqls.get(i));
							if(objs!=null){
								Object[] params=objs.get(i);
								if(params!=null){
									for(int j=0;j<params.length;j++){
										setPreparedState(pst,j+1,params[j]);
									}
								}
							}
							result+=pst.executeUpdate();
						}
						return result;
					} catch (SQLException e) {
						throw new SqlException(e);
					}finally{
						safeClose(pst);
					}
				}
			});	
		}
		public int[] batchUpdate(final String sql,final List<Object[]> objs){
			if(objs.size()==1){
				return new int[]{update(sql,objs.get(0))};
			}
			return execute(new ConnectionCallback<int[]>() {
				public int[] doInConnection(Connection conn) {
					PreparedStatement pst = null;
					try {
						pst=conn.prepareStatement(sql);
						for(int i=0;i<objs.size();i++){
							Object[] params=objs.get(i);
							for(int j=0;j<params.length;j++){
								setPreparedState(pst,j+1,params[j]);
		  					}
							pst.addBatch();
						}
						return pst.executeBatch();
					} catch (SQLException e) {
						throw new SqlException(e,sql);
					}finally{
						safeClose(pst);
					}
					
				}
			});
		}
		public <T> T query(final String sql,final Object[] params,final ResultSetExtractor<T> rset) {
			return execute(new ConnectionCallback<T>(){
				public T doInConnection(Connection conn) {
					PreparedStatement pst = null;
					ResultSet rs=null;
					try {
						pst = conn.prepareStatement(sql);
						if(params!=null){
							for(int i=0;i<params.length;i++){
								setPreparedState(pst, i+1,params[i]);
							}
						}
						rs=pst.executeQuery();
						return rset.extractData(rs);
					} catch (SQLException e) {
						throw new SqlException(e,sql);
					}finally{
						safeClose(rs);
						safeClose(pst);
					}
				}
			}, false);
		}
		
		public <T> T queryForObject(String sql,Object[] params,RowMapper<T> rowMapper){
			List<T> result=query(sql, params, rowMapper);
			if(result.size()==0){
				throw new SqlException(new Exception("not exists objects"),sql);
			}
			if(result.size()>1){
				throw new SqlException(new Exception("has more objects"),sql);
			}
			return result.get(0);
		}
		public int queryForInt(String sql,Object... params){
			return queryForObject(sql, params, Integer.class);
		}
		public long queryForLong(String sql,Object... params) {
			return queryForObject(sql, params, Long.class);
		}
		public Map<String,Object> queryForMap(String sql,Object... params){
			return queryForObject(sql, params, new MapRowMapper());
		}
		public List<Map<String,Object>> queryForList(String sql,Object... params){
			return query(sql,params,new MapRowMapper());
		}
		@SuppressWarnings("unchecked")
		public <T> T queryForObject(String sql,Object[] params,Class<T> clazz){
			Map<String,Object> result=queryForMap(sql, params);
			if(ClassUtil.isSingleType(clazz))
				return (T) ClassUtil.convertValueToRequiredType(result.get(result.keySet().iterator().next()),clazz);
			else if(Map.class.isAssignableFrom(clazz)){
				return (T) result;
			}else{
				try {
					return ClassUtil.mapToBean(result,clazz);
				} catch (Exception e) {
					throw new SqlException(e,sql);
				}
			}
		}
		@SuppressWarnings("unchecked")
		public <T> List<T> queryForList(String sql,Object[] params,final Class<T> clazz){
			if(ClassUtil.isSingleType(clazz)){
				List<Map<String,Object>> trs=queryForList(sql, params);
				List<T> result=new ArrayList<T>();
				for(Map<String,Object> tr:trs){
					result.add((T)ClassUtil.convertValueToRequiredType(tr.get(tr.keySet().iterator().next()),clazz));
				}
				return result;
			}else if(Map.class.isAssignableFrom(clazz)){
				return (List<T>) queryForList(sql, params);
			}else{
				List<T> trs=query(sql, params,new RowMapper<T>() {
					MapRowMapper mapper=new MapRowMapper();
					public T mapRow(ResultSet rs, int rowNum)
						throws SQLException {
						Map<String,Object> map=mapper.mapRow(rs,rowNum);
						try {
							return ClassUtil.mapToBean(map,clazz);
						} catch (Exception e) {
							throw new SQLException(e.getMessage());
						}
					}
				});
				return trs;
			}
		}
		public void queryForCallback(final String sql,final Object[] params,
				final Callback callback) {
			execute(new ConnectionCallback<Object>(){
				public Object doInConnection(Connection conn) {
					PreparedStatement stmt = null;
					ResultSet rs=null;
					try {
						stmt=conn.prepareStatement(sql);
						if(params!=null){
							for(int i=0;i<params.length;i++){
								setPreparedState(stmt, i+1,params[i]);
							}
						}
						rs=stmt.executeQuery();
						int i=0;
						if(callback instanceof CallbackCycle){
							((CallbackCycle) callback).start();
						}
						while(rs.next()){
							callback.call(rs, i++);
						}
						if(callback instanceof CallbackCycle){
							((CallbackCycle) callback).end();
						}
						return null;
					} catch (SQLException e) {
						throw new SqlException(e,sql);
					}finally{
						safeClose(rs);
						safeClose(stmt);
					}
				}
				
			},false);
		}
		public void queryForCallback(String sql, Callback callback) {
			queryForCallback(sql,null, callback);
		}
		public <T> List<T> query(final String sql,final Object[] params,final RowMapper<T> rowMapper){
			return execute(new ConnectionCallback<List<T>>(){
				public List<T> doInConnection(Connection con) {
					PreparedStatement stmt = null;
					ResultSet rs=null;
					List<T> result=new ArrayList<T>();
					try {
						stmt=con.prepareStatement(sql);
						if(params!=null){
							for(int i=0;i<params.length;i++){
								setPreparedState(stmt, i+1,params[i]);
							}
						}
						rs=stmt.executeQuery();
						int i=0;
						while(rs.next()){
							T t=rowMapper.mapRow(rs,i++);
							if(i>queryReturnLimit){
								break;
							}
							result.add(t);
						}
						return result;
					} catch (SQLException e) {
						throw new SqlException(e,sql);
					}finally{
						safeClose(rs);
						safeClose(stmt);
					}
				}
			},false);
		}
		public int[] batchUpdate(final String sql,final BatchPreparedStatementSetter bs) {
			return execute(new ConnectionCallback<int[]>(){
				public int[] doInConnection(Connection con) {
					PreparedStatement pst = null;
					try {
						pst=con.prepareStatement(sql);
						for(int i=0;i<bs.getBatchSize();i++){
							bs.setValues(pst, i);
							pst.addBatch();
						}
						int[] result=pst.executeBatch();
						return result;
					} catch (SQLException e) {
						throw new SqlException(e,sql);
					}finally{
						safeClose(pst);
					}
				}
			});
		}
		public int queryForInt(String sql) {
			return queryForInt(sql,new Object[]{});
		}
		public long queryForLong(String sql) {
			return queryForLong(sql,new Object[]{});
		}
		public Map<String, Object> queryForMap(String sql) {
			return queryForMap(sql, new Object[]{});
		}
		public List<Map<String, Object>> queryForList(String sql) {
			return queryForList(sql,new Object[]{});
		}
		
		public <T> List<T> queryForList(String sql, Class<T> clazz) {
			return queryForList(sql,null, clazz);
		}
		public <T> T queryForObject(String sql, Class<T> clazz) {
			return queryForObject(sql,null,clazz);
		}
		public <T> T query(String sql, ResultSetExtractor<T> rset,
				Object... params) {
			return query(sql, params, rset);
		}
		public <T> List<T> query(String sql, RowMapper<T> rowMapper,
				Object... params) {
			return query(sql,params,rowMapper);
		}
		public <T> T queryForObject(String sql, RowMapper<T> rowMapper,
				Object... params) {
			return queryForObject(sql,params,rowMapper);
		}
		public <T> T queryForObject(String sql, Class<T> clazz,
				Object... params) {
			return queryForObject(sql, params,clazz);
		}
		public <T> List<T> queryForList(String sql, Class<T> clazz,
				Object... params) {
			return queryForList(sql, params, clazz);
		}
		private void safeClose(Object  close){
			DataSourceUtils.safeClose(close);
		}
		public <T> T execute(ConnectionCallback<T> connectionCallback){
			return execute(connectionCallback,true);
		}
		public <T> T execute(ConnectionCallback<T> connectionCallback,boolean updatetype) {
			if(!transactionInversion){
				return execute1(connectionCallback, updatetype);
			}else{
				return execute2(connectionCallback, updatetype);
			}
		}
		private <T> T execute2(ConnectionCallback<T> connectionCallback,boolean updatetype) {
			Connection con=null;
			try{
				con=DataSourceUtils.getConnection(dataSource);
				T t= connectionCallback.doInConnection(con);
				if(updatetype)
					DataSourceUtils.commit(dataSource);
				return t;
			}catch(Exception  e){
				if(updatetype)
					DataSourceUtils.rollback(dataSource);
				throw new SqlException(e.getMessage());
			}finally{
				DataSourceUtils.releaseConnection(dataSource);
			}
		}
		private  <T> T execute1(ConnectionCallback<T> connectionCallback,boolean updatetype) {
			Connection con=null;
			try{
				con=dataSource.getConnection();
				if(updatetype)
				con.setAutoCommit(false);
				T t= connectionCallback.doInConnection(con);
				if(updatetype)
					con.commit();
				return t;
			}catch(Exception  e){
				if(updatetype)
					try {
						con.rollback();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				throw new SqlException(e.getMessage());
			}finally{
				safeClose(con);
			}
		}
		class MapRowMapper implements  RowMapper<Map<String,Object>> {
			public Map<String, Object> mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();
				Map<String, Object> mapOfColValues = new LinkedCaseInsensitiveMap<Object>();
				for (int i = 1; i <= columnCount; i++) {
					String key = lookupColumnName(rsmd, i);
					Object obj = getResultSetValue0(rs, i,getCharTransCodes());
					mapOfColValues.put(key, obj);
				}
				return mapOfColValues;
			}
		}
		
}
