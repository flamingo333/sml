package org.hw.sml.test.transaction;

import org.hw.sml.core.SqlMarkupTemplate;
import org.hw.sml.support.ioc.annotation.Bean;
import org.hw.sml.support.ioc.annotation.Inject;

@Bean
public class DemoImpl implements Demo{
	
	@Inject("sml")
	private SqlMarkupTemplate sml;
	
	public void doit() {
		sml.getJdbc("defJt").execute("update  hw_t set name='3' where id='1'");
		sml.getJdbc("ipmsdm").execute("insert into hw_t(id,name) values('1','2')");
	}
	
}
