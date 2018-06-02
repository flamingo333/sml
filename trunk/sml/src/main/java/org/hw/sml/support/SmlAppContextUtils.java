package org.hw.sml.support;

import java.util.HashMap;
import java.util.Map;

import org.hw.sml.context.SmlContextUtils;
import org.hw.sml.core.SqlMarkupAbstractTemplate;
import org.hw.sml.tools.Assert;

public class SmlAppContextUtils {
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
