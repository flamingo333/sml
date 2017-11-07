package org.hw.sml.support;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import org.hw.sml.context.SmlContextUtils;
import org.hw.sml.core.SqlMarkupAbstractTemplate;
import org.hw.sml.support.jmx.SmlAgent;
import org.hw.sml.tools.Assert;

public class SmlAppContextUtils {
	static{
		try {
			SmlAgent sml=new SmlAgent();
			ObjectName name = new ObjectName("org.hw.sml.support.jmx:type=SmlAgent");
			ManagementFactory.getPlatformMBeanServer().registerMBean(sml, name);
		} catch (Exception e) {
		}   
	}
	public static Map<String,SqlMarkupAbstractTemplate> sqlMarkupAbstractTemplates=new HashMap<String, SqlMarkupAbstractTemplate>();
	
	public static void put(String key,SqlMarkupAbstractTemplate sqlMarkupAbstractTemplate){
		sqlMarkupAbstractTemplates.put(key, sqlMarkupAbstractTemplate);
	}
	public static SqlMarkupAbstractTemplate getSqlMarkupAbstractTemplate(){
		return getSqlMarkupAbstractTemplate("default");
	}
	public static SqlMarkupAbstractTemplate getSqlMarkupAbstractTemplate(String key){
		SqlMarkupAbstractTemplate temp= sqlMarkupAbstractTemplates.get(key);
		Assert.notNull(temp, key+ "  not init for smlMarkup template!");
		return temp;
	}
	public static  SmlContextUtils getSmlContextUtils(String key){
		return new SmlContextUtils(getSqlMarkupAbstractTemplate(key));
	}
	public static SmlContextUtils getSmlContextUtils(){
		return new SmlContextUtils(getSqlMarkupAbstractTemplate());
	}
}
