package org.hw.sml.jdbc.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hw.sml.jdbc.BatchPreparedStatementSetter;
import org.hw.sml.jdbc.Callback;
import org.hw.sml.jdbc.DataSourceUtils;
import org.hw.sml.jdbc.JdbcAccessor;
import org.hw.sml.jdbc.JdbcTemplate;
import org.hw.sml.jdbc.ResultSetExtractor;
import org.hw.sml.jdbc.RowMapper;
import org.hw.sml.jdbc.exception.SqlException;
import org.hw.sml.tools.Assert;
import org.hw.sml.tools.ClassUtil;
import org.hw.sml.tools.MapUtils;

public class DefaultJdbcTemplate  extends JdbcAccessor  implements JdbcTemplate{
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
		public int update(String sql,Object... params){
			Connection con=null;
			PreparedStatement pst = null;
			int result=0;
			try{
				con=open();
				pst = con.prepareStatement(sql);
				if(params!=null){
					for(int i=0;i<params.length;i++){
						setPreparedState(pst, i+1,params[i]);
					}
				}
				result=pst.executeUpdate();
				commit();
			}catch(SQLException  e){
				rollback();
				throw new SqlException(e.getMessage());
			}finally{
				try{
					if(pst!=null){
						pst.close();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				close();
			}
			return result;
		}
		public int update(List<String> sqls){
			return update(sqls,null);
		}
		public int update(String[] sqls, List<Object[]>... objs) {
			Connection con=null;
			PreparedStatement pst = null;
			if(objs!=null)
			Assert.isTrue(sqls.length==objs.length,"sqls size["+sqls.length+"] != objs size["+objs.length+"]");
			int result=0;
			try {
				con=open();
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
				commit();
				return result;
			}catch(Exception e){
				rollback();
				throw new SqlException(e.getMessage());
			}finally{
				try {
					if(pst!=null){
						pst.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				close();
			}
		}
		public int update(List<String> sqls, List<Object[]> objs) {
			Connection con=null;
			PreparedStatement pst = null;
			if(objs!=null)
			Assert.isTrue(sqls.size()==objs.size(),"sqls size["+sqls.size()+"] != objs size["+objs.size()+"]");
			int result=0;
			try {
				con=open();
				for(int i=0;i<sqls.size();i++){
					pst=con.prepareStatement(sqls.get(i));
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
				commit();
				return result;
			}catch(Exception e){
				rollback();
				throw new SqlException(e.getMessage());
			}finally{
				try {
					if(pst!=null){
						pst.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				close();
			}
		}
		public int[] batchUpdate(String sql,List<Object[]> objs){
			Connection con=null;
			PreparedStatement pst = null;
			if(objs.size()==1){
				return new int[]{update(sql,objs.get(0))};
			}
			try {
				con=open();
				pst=con.prepareStatement(sql);
				for(int i=0;i<objs.size();i++){
					Object[] params=objs.get(i);
					for(int j=0;j<params.length;j++){
						setPreparedState(pst,j+1,params[j]);
  					}
					pst.addBatch();
				}
				int[] result=pst.executeBatch();
				commit();
				return result;
			} catch (Exception e) {
				rollback();
				throw new SqlException(e.getMessage());
			}finally{
				try{
					if(pst!=null)
						pst.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				close();
			}
		}
		public <T> T query(String sql, Object[] params, ResultSetExtractor<T> rset) {
			Connection con=null;
			PreparedStatement pst = null;
			ResultSet rs=null;
			try {
				con=open();
				pst = con.prepareStatement(sql);
				if(params!=null){
					for(int i=0;i<params.length;i++){
						setPreparedState(pst, i+1,params[i]);
					}
				}
				rs=pst.executeQuery();
				return rset.extractData(rs);
			} catch (SQLException e) {
				throw new SqlException(e.getMessage());
			}finally{
				try{
					if(rs!=null)
					rs.close();
					if(pst!=null)
					pst.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				close();
			}
		}
		
		public <T> T queryForObject(String sql,Object[] params,RowMapper<T> rowMapper){
			List<T> result=query(sql, params, rowMapper);
			if(result.size()==0){
				throw new SqlException("not exists objects");
			}
			if(result.size()>1){
				throw new SqlException("has more objects");
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
			return query(sql, params,new MapRowMapper());
		}
		@SuppressWarnings("unchecked")
		public <T> T queryForObject(String sql,Object[] params,Class<T> clazz){
			Map<String,Object> result=queryForMap(sql, params);
			return (T) ClassUtil.convertValueToRequiredType(result.get(result.keySet().iterator().next()),clazz);
		}
		@SuppressWarnings("unchecked")
		public <T> List<T> queryForList(String sql,Object[] params,Class<T> clazz){
			List<Map<String,Object>> trs=queryForList(sql, params);
			List<T> result=MapUtils.newArrayList();
			for(Map<String,Object> tr:trs){
				result.add((T)ClassUtil.convertValueToRequiredType(tr.get(tr.keySet().iterator().next()),clazz));
			}
			return result;
		}
		public void queryForCallback(String sql, Object[] params,
				Callback callback) {
			Connection con = null;
			PreparedStatement stmt = null;
			ResultSet rs=null;
			try{
				con=open();
				stmt=con.prepareStatement(sql);
				if(params!=null){
					for(int i=0;i<params.length;i++){
						setPreparedState(stmt, i+1,params[i]);
					}
				}
				rs=stmt.executeQuery();
				int i=0;
				while(rs.next()){
					callback.call(rs, i++);
				}
			}catch(Exception e){
				throw new SqlException(e.getMessage());
			}finally{
				try{
					if(rs!=null)
						rs.close();
					if(stmt!=null)
						stmt.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				close();
			}
		}
		public void queryForCallback(String sql, Callback callback) {
			queryForCallback(sql,null, callback);
		}
		public <T> List<T> query(String sql,Object[] params,RowMapper<T> rowMapper){
			Connection con = null;
			PreparedStatement stmt = null;
			ResultSet rs=null;
			List<T> result=MapUtils.newArrayList();
			try{
				con =DataSourceUtils.getConnection(dataSource);
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
					result.add(t);
				}
				return result;
			}catch(Exception e){
				throw new SqlException(e.getMessage());
			}finally{
				try{
					if(rs!=null)
						rs.close();
					if(stmt!=null)
						stmt.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				close();
			}
		}
		class MapRowMapper implements  RowMapper<Map<String,Object>> {
			public Map<String, Object> mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();
				Map<String, Object> mapOfColValues = MapUtils.newLinkedHashMap();
				for (int i = 1; i <= columnCount; i++) {
					String key = lookupColumnName(rsmd, i);
					Object obj = getResultSetValue(rs, i);
					mapOfColValues.put(key, obj);
				}
				return mapOfColValues;
			}
		}
		public int[] batchUpdate(String sql, BatchPreparedStatementSetter bs) {
			Connection con=null;
			PreparedStatement pst = null;
			try {
				con=open();
				pst=con.prepareStatement(sql);
				for(int i=0;i<bs.getBatchSize();i++){
					bs.setValues(pst, i);
					pst.addBatch();
				}
				int[] result=pst.executeBatch();
				commit();
				return result;
			} catch (Exception e) {
				rollback();
				throw new SqlException(e.getMessage());
			}finally{
				try{
					if(pst!=null)
						pst.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				close();
			}
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
		private Connection open() throws SQLException{
			return DataSourceUtils.getConnection(dataSource);
		}
		private void commit(){
			DataSourceUtils.commit(dataSource);
		}
		private void rollback(){
			DataSourceUtils.rollback(dataSource);
		}
		private void close(){
			DataSourceUtils.releaseConnection(dataSource );
		}
}
