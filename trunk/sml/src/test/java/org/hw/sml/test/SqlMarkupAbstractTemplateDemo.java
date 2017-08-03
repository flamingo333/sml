package org.hw.sml.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.stream.FileImageInputStream;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.hw.sml.core.SqlMarkupTemplate;
import org.hw.sml.core.resolver.JsEngine;
import org.hw.sml.jdbc.JdbcTemplate;
import org.hw.sml.jdbc.impl.DefaultDataSource;
import org.hw.sml.jdbc.impl.DefaultJdbcTemplate;
import org.hw.sml.queryplugin.JsonMapper;
import org.hw.sml.support.CallableHelper;
import org.hw.sml.tools.Maps;
import org.junit.Test;



public class SqlMarkupAbstractTemplateDemo {
	@Test
	public static  void testQuery() throws SQLException, FileNotFoundException, IOException {
		DefaultDataSource dataSource2=new DefaultDataSource();
		dataSource2.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		dataSource2.setUrl("jdbc:oracle:thin:@10.221.247.43:1521/ipms");
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
		System.out.println(st.getSmlContextUtils().query("area-pm",""));
		st.getSmlContextUtils().query("defJt","select 1 from dual where 1=1  <if test=\" '@a'=='1' \">and 1<2</if>",new Maps<String,String>().put("a","1").getMap());
	}
	public static void main(String[] args) throws SQLException, InterruptedException, FileNotFoundException, IOException {
		testQuery();
	}
}
