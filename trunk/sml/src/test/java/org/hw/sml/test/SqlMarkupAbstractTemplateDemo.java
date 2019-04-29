package org.hw.sml.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.hw.sml.context.SmlContextUtils;
import org.hw.sml.core.SqlMarkupTemplate;
import org.hw.sml.core.resolver.JsEngine;
import org.hw.sml.jdbc.JdbcTemplate;
import org.hw.sml.jdbc.impl.DefaultDataSource;
import org.hw.sml.jdbc.impl.DefaultJdbcTemplate;
import org.hw.sml.support.SmlAppContextUtils;
import org.junit.Test;



public class SqlMarkupAbstractTemplateDemo {
	@Test
	public static  void testQuery() throws SQLException, FileNotFoundException, IOException {
		DefaultDataSource dataSource2=new DefaultDataSource();
		dataSource2.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		dataSource2.setUrl("jdbc:oracle:thin:@10.222.23.185:1521/ipms");
		dataSource2.setUsername("ipmsdm");
		dataSource2.setPassword("SHipmsdm!23$");
		DefaultDataSource dataSource = new DefaultDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://23.247.25.117:3306/hw");
		dataSource.setUsername("root");
		dataSource.setPassword("hlw");
		//库集
		Map<String,DataSource> dss=new HashMap<String,DataSource>();
		dss.put("defJt", dataSource2);
		JdbcTemplate jdbcTemplate=new DefaultJdbcTemplate();
		jdbcTemplate.setDataSource(dataSource2);
		SqlMarkupTemplate st=new SqlMarkupTemplate();
		JsEngine.evel("'a'");
		st.setDss(dss);
		st.init();
		System.out.println(new SmlContextUtils(st).query("area-pm",""));
		System.out.println(new SmlContextUtils(st).query("area-pm",""));
		//new SmlContextUtils(st).query("defJt","select fa as b from DM_RE_BA_HOT where 1=1  <if test=\" '@a'=='1' \">and 1<2</if>",new Maps<String,String>().put("a","1").getMap());
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<10000;i++){
			sb.append(i);
		}
		//int i=jdbcTemplate.update("insert into hw_test2(id1,file_) values(?,?)",new Object[]{sb.substring(0,121),new FileInputStream("e:/temp/2017-09-20_18-00-45_CfgBackup-sys-cfg.zip")});
		//System.out.println(i);
		for(int i=0;i<10;i++){
			String sql=SmlAppContextUtils.getSmlContextUtils().queryRst("area-pm",null).getPrettySqlString();
			System.out.println(sql);
		}
		//Map<String,Object> result= jdbcTemplate.queryForMap("select file_ from hw_test2 where id='2'");
		//System.out.println(result);
		
	}
	public static void main(String[] args) throws SQLException, InterruptedException, FileNotFoundException, IOException {
		testQuery();
	}
}
