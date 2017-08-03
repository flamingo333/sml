package org.hw.sml.support.el;

import java.util.Map;

import org.hw.sml.tools.DateTools;
import org.hw.sml.tools.MapUtils;

public abstract class ElContext{
	public ElContext(){
		
	}
	protected Map<String,Object> beanMap=MapUtils.newHashMap();
	
	protected Map<String,String> properties=MapUtils.newHashMap();
	@SuppressWarnings("unchecked")
	public  <T extends ElContext>T withBeanMap(Map<String,Object> beanMap){
		this.beanMap=beanMap;
		return (T)this;
	}
	@SuppressWarnings("unchecked")
	public  <T extends ElContext>T withProperties(Map<String,String> properties){
		this.properties=properties;
		return (T)this;
	}
	@SuppressWarnings("unchecked")
	public <T extends ElContext> T init(){
		withBean("smlElHelper",this);
		withBean("smlDateHelper",DateTools.newInstance());
		withBean("smlMapHelper",MapUtils.newInstance());
		withBean("lo",LogicOperation.newInstance());
		return (T)this;
	}
	@SuppressWarnings("unchecked")
	public  <T extends ElContext>T  withProperty(String key,String value){
		properties.put(key, value);
		return (T)this;
	}
	@SuppressWarnings("unchecked")
	public <T extends ElContext>T withBean(String key,Object value){
		beanMap.put(key, value);
		return (T)this;
	}
	public  Object evel(String elp) throws ElException{
		BeanType b= evelBeanType(elp);
		return b==null?null:b.getV();
	}
	public Object getBean(String key){
		return beanMap.get(key);
	}
	public String getValue(String key){
		return properties.get(key);
	}
	public abstract BeanType evelBeanType(String elp) throws ElException;
}
