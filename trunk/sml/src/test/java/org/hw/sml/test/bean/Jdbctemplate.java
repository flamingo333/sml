package org.hw.sml.test.bean;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.hw.sml.core.SqlMarkupAbstractTemplate;
import org.hw.sml.core.SqlMarkupTemplate;
import org.hw.sml.jdbc.impl.DefaultJdbcTemplate;
import org.hw.sml.support.ioc.BeanHelper;
import org.hw.sml.tools.ClassUtil;

public class Jdbctemplate {
	
	public static void main(String[] args) throws Exception {
		Map<String,Object> result=new HashMap<String,Object>();
		result.put("a","hw");
		result.put("b",0);
		result.put("c","34.0");
		result.put("f","111");
		result.put("g",new Date(System.currentTimeMillis()));
		result.put("h","111111111111111111111111");
		result.put("bb","true");
		System.out.println(ClassUtil.mapToBean(result,TestA.class));
		Time t;
		Timestamp tt;
		System.out.println(int.class.isPrimitive());
		DataSource ds=BeanHelper.getBean("datasource2");
		SqlMarkupTemplate smt=new SqlMarkupTemplate();
		smt.getDss().put("defJt",ds);
		smt.init();
		//System.out.println(new DefaultJdbcTemplate(ds).queryForObject("select 'hw' as a,systimestamp as g,123322113124324324243 as e from dual", TestA.class));
		Object obj=smt.querySql("defJt","<select id=\"resultMap\">java.lang.String</select>select count(1) as total,'hw' as a,sysdate as g from dual",new HashMap<String,String>());
		System.out.println(obj);
	}
}
