package org.hw.sml.jdbc.transaction;

import java.sql.Connection;

import javax.sql.DataSource;

import org.hw.sml.jdbc.DataSourceUtils;
import org.hw.sml.support.aop.AbstractAspect;
import org.hw.sml.support.aop.Invocation;

public class TransactionManager extends AbstractAspect{
	
	private DataSource dataSource;

	public void doBefore(Invocation invocation) throws Throwable {
		DataSourceUtils.registTransactionKeys(dataSource);
	}
	
	public void doException(Invocation invocation) throws Throwable {
		boolean isConn=DataSourceUtils.isConnection(dataSource);
		if(isConn){
			Connection conn=DataSourceUtils.doGetConnection(dataSource);
			conn.rollback();
		}
		DataSourceUtils.removeTransactionKeys(dataSource);
		DataSourceUtils.releaseConnection(dataSource);
	}

	public void doAfter(Invocation invocation) throws Throwable {
		boolean isConn=DataSourceUtils.isConnection(dataSource);
		if(isConn){
			Connection conn=DataSourceUtils.doGetConnection(dataSource);
			conn.commit();
		}
		DataSourceUtils.removeTransactionKeys(dataSource);
		DataSourceUtils.releaseConnection(dataSource);
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	

}
