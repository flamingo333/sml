package org.hw.sml.support;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
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
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (InstanceAlreadyExistsException e) {
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			e.printStackTrace();
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
