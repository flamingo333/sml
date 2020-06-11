package org.hw.sml.test.transaction;

import org.hw.sml.core.SqlMarkupTemplate;
import org.hw.sml.jdbc.JdbcTemplate;
import org.hw.sml.jdbc.impl.DefaultJdbcTemplate;
import org.hw.sml.support.ioc.annotation.Bean;
import org.hw.sml.support.ioc.annotation.Inject;
import org.hw.sml.tools.ClassUtil;

@Bean
public class DemoImpl implements Demo{
	
	@Inject("sml")
	private SqlMarkupTemplate sml;
	
	public void doit() {
		DefaultJdbcTemplate jdbcTemplate=(DefaultJdbcTemplate) sml.getJdbc("defJt");
		try {
			System.out.println(ClassUtil.getFieldValue(jdbcTemplate,"transactionInversion"));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sml.getJdbc("ipmsdm").execute("insert into hw_t(id,name) values('4','4')");
		sml.getJdbc("defJt").execute("update  hw_t set name='22' where id='2'");
		sml.getJdbc("ipmsdm").execute("insert into hw_t(id,name) values('1','2')");
	}
	
}
