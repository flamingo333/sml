package org.hw.sml.support;

import java.util.HashMap;
import java.util.Map;

import org.hw.sml.tools.Assert;

import com.eastcom_sw.inas.core.service.jdbc.AbstractJdbcFTemplate;
import com.eastcom_sw.inas.core.service.support.JFContextUtils;

public class SmlAppContextUtils {
	public static Map<String,AbstractJdbcFTemplate> sqlMarkupAbstractTemplates=new HashMap<String, AbstractJdbcFTemplate>();
	
	public static void put(String key,AbstractJdbcFTemplate sqlMarkupAbstractTemplate){
		sqlMarkupAbstractTemplates.put(key, sqlMarkupAbstractTemplate);
	}
	public static AbstractJdbcFTemplate getSqlMarkupAbstractTemplate(){
		return getSqlMarkupAbstractTemplate("default");
	}
	public static AbstractJdbcFTemplate getSqlMarkupAbstractTemplate(String key){
		AbstractJdbcFTemplate temp= sqlMarkupAbstractTemplates.get(key);
		Assert.notNull(temp, key+ "  not init for smlMarkup template!");
		return temp;
	}
	public static  JFContextUtils getSmlContextUtils(String key){
		return getSqlMarkupAbstractTemplate(key).getJfContextUtils();
	}
	public static JFContextUtils getSmlContextUtils(){
		return getSqlMarkupAbstractTemplate().getJfContextUtils();
	}
}
