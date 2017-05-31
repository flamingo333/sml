package org.hw.sml.test.bean;

import org.hw.sml.support.ioc.annotation.Bean;
import org.hw.sml.support.ioc.annotation.Init;
import org.hw.sml.support.ioc.annotation.Val;

@Bean("hwld")
public class Helloworld {
	
	@Val(value="CFG_DEFAULT_DIALECT1",required=false)
	private String name;
	
	@Val("CFG_JDBC_SQL")
	private String value;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Init
	private void i(){
		System.out.println(name+"---->"+value);
	}
}
