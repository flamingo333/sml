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
	public static  ThreadLocal<Map<DataSource,Connection>> connections=new InheritableThreadLocal<Map<DataSource,Connection>>(){
		protected Map<DataSource, Connection> initialValue() {
			return new HashMap<DataSource,Connection>();
		}
	};
	public static void configIsSqlLog(boolean flag){
		SqlException.isSqlLog=flag;
	}
	static{
		configIsSqlLog(Boolean.getBoolean("sml.vm.jdbc.sql.error.log"));
	}
	//对事务-连接关闭的datasource注册到此集合中
	public static ThreadLocal<Set<Connection>> transactionManagerKeys=new InheritableThreadLocal<Set<Connection>>(){
		protected Set<Connection> initialValue() {
			return new HashSet<Connection>();
		}
	};
	//private static List<String> transactionManagerKeys=MapUtils.newArrayList();
	public static void registTransactionKeys(DataSource dataSource) throws SQLException{
		Connection conn=getConnection(dataSource);
		transactionManagerKeys.get().add(conn);
	}
	public static void removeTransactionKeys(DataSource dataSource) throws SQLException{
		Connection conn=getConnection(dataSource);
		transactionManagerKeys.get().remove(conn);
	}
	public static boolean isTransaction(DataSource dataSource) throws SQLException{
		Connection conn=getConnection(dataSource);
		return transactionManagerKeys.get().contains(conn);
	}
	public static boolean isConnection(DataSource dataSource){
		return connections.get().containsKey(dataSource);
	}
	public static Connection getConnection(DataSource dataSource) throws SQLException{
		return doGetConnection(dataSource);
	}
	public static void commit(DataSource dataSource) throws SQLException{
		Connection conn=getConnection(dataSource);
		if(!transactionManagerKeys.get().contains(conn))
		try {
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void rollback(DataSource dataSource) throws SQLException{
		Connection conn=getConnection(dataSource);
		if(!transactionManagerKeys.get().contains(conn))
		try {
			conn.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static Connection doGetConnection(DataSource dataSource) throws SQLException {
		Assert.notNull(dataSource, "No DataSource specified");
		Map<DataSource,Connection> map=connections.get();
		if(!map.containsKey(dataSource)){
			Connection con=dataSource.getConnection();
			con.setAutoCommit(false);
			map.put(dataSource,con);
		}
		return map.get(dataSource);
	}
	public static void releaseConnection(DataSource dataSource) {
		Map<DataSource,Connection> map=connections.get();
		Connection key=map.get(dataSource);
		try {
			if(map.containsKey(dataSource)&&!transactionManagerKeys.get().contains(key)){
				Connection con=map.remove(dataSource);
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
