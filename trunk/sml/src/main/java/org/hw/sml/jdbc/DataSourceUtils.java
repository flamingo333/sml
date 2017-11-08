package org.hw.sml.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hw.sml.jdbc.exception.SqlException;
import org.hw.sml.tools.Assert;
import org.hw.sml.tools.MapUtils;



public class DataSourceUtils {
	private static  ThreadLocal<Map<String,Connection>> connections=new ThreadLocal<Map<String,Connection>>(){
		protected Map<String, Connection> initialValue() {
			return new HashMap<String,Connection>();
		}
	};
	public static void configIsSqlLog(boolean flag){
		SqlException.isSqlLog=flag;
	}
	static{
		configIsSqlLog(Boolean.valueOf(System.getProperty("sml.vm.jdbc.sql.error.log")));
	}
	//对事务-连接关闭的datasource注册到此集合中
	private static List<String> transactionManagerKeys=MapUtils.newArrayList();
	public static void registTransactionManager(String dataSourceKey){
		transactionManagerKeys.add(dataSourceKey);
	}
	public static Connection getConnection(DataSource dataSource) throws SQLException{
		return doGetConnection(dataSource);
	}
	public static void commit(DataSource dataSource){
		if(!transactionManagerKeys.contains(dataSource.toString()))
		try {
			getConnection(dataSource).commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void rollback(DataSource dataSource){
		if(!transactionManagerKeys.contains(dataSource.toString()))
		try {
			getConnection(dataSource).rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static Connection doGetConnection(DataSource dataSource) throws SQLException {
		Assert.notNull(dataSource, "No DataSource specified");
		String key=dataSource.toString();
		Map<String,Connection> map=connections.get();
		if(!map.containsKey(key)){
			Connection con=dataSource.getConnection();
			con.setAutoCommit(false);
			map.put(key,con);
		}
		return map.get(key);
	}
	public static void releaseConnection(DataSource dataSource) {
		Map<String,Connection> map=connections.get();
		String key=dataSource.toString();
		try {
			if(map.containsKey(key)&&!transactionManagerKeys.contains(key)){
				Connection con=map.remove(key);
				if(con!=null&&!con.isClosed()){
					safeClose(con);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void safeClose(Object  close){
		try {
			if(close!=null){
				if(close instanceof Statement)
					((Statement)close).close();
				else if(close instanceof ResultSet)
					((ResultSet)close).close();
				else if(close instanceof Connection){
					((Connection)close).close();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
