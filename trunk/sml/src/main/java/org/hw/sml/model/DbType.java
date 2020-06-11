package org.hw.sml.model;
public  enum DbType{
	mysql("com.mysql.jdbc.Driver"),
	gbase("com.gbase.jdbc.Driver"),
	oracle("oracle.jdbc.driver.OracleDriver"),
	mariadb("org.mariadb.jdbc.Driver"),
	sqlserver("com.microsoft.sqlserver.jdbc.SQLServerDriver"),
	db2("com.ibm.db2.jdbc.app.DB2Driver"),
	sybase("com.sybase.jdbc.SybDriver"),
	postgresql("org.postgresql.Driver"),
	sqlite("org.sqlite.JDBC"),
	hsqldb("org.hsqldb.jdbcDriver"),
	h2("org.h2.Driver"),
	hive("org.apache.hive.jdbc.HiveDriver");
	String driverClassName;
	DbType(String driverClassName){
		this.driverClassName=driverClassName;
	}
	public String getDriverClassName(){
		return driverClassName;
	}
}