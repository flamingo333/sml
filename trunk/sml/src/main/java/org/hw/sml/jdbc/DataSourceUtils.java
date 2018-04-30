package org.hw.sml.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.hw.sml.jdbc.exception.SqlException;
import org.hw.sml.tools.Assert;



public class DataSourceUtils {
	public static  ThreadLocal<Map<String,Connection>> connections=new InheritableThreadLocal<Map<String,Connection>>(){
		protected Map<String, Connection> initialValue() {
			return new HashMap<String,Connection>();
		}
	};
	public static void configIsSqlLog(boolean flag){
		SqlException.isSqlLog=flag;
	}
	static{
		configIsSqlLog(Boolean.getBoolean("sml.vm.jdbc.sql.error.log"));
	}
	//对事务-连接关闭的datasource注册到此集合中
	public static ThreadLocal<Set<String>> transactionManagerKeys=new InheritableThreadLocal<Set<String>>(){
		protected Set<String> initialValue() {
			return new HashSet<String>();
		}
	};
	//private static List<String> transactionManagerKeys=MapUtils.newArrayList();
	public static void registTransactionKeys(String dataSourceKey){
		transactionManagerKeys.get().add(dataSourceKey);
	}
	public static void removeTransactionKeys(String dataSourceKey){
		transactionManagerKeys.get().remove(dataSourceKey);
	}
	public static boolean isTransaction(DataSource dataSource){
		return transactionManagerKeys.get().contains(dataSource.toString());
	}
	public static boolean isConnection(DataSource dataSource){
		return connections.get().containsKey(dataSource.toString());
	}
	public static Connection getConnection(DataSource dataSource) throws SQLException{
		return doGetConnection(dataSource);
	}
	public static void commit(DataSource dataSource){
		if(!transactionManagerKeys.get().contains(dataSource.toString()))
		try {
			getConnection(dataSource).commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void rollback(DataSource dataSource){
		if(!transactionManagerKeys.get().contains(dataSource.toString()))
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
			if(map.containsKey(key)&&!transactionManagerKeys.get().contains(key)){
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
