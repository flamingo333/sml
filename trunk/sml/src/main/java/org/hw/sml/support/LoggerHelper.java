package org.hw.sml.support;

import org.hw.sml.support.log.Loggers;
import org.hw.sml.tools.ClassUtil;


public class LoggerHelper {
	public static enum Type{
		def,log4j,slf4j
	}
	private static final String defCp="org.hw.sml.support.log.DelegatedDefaultLog";
	private static final String log4jCp="org.hw.sml.support.log.DelegatedLog4j";
	private static final String slf4jCp="org.hw.sml.support.log.DelegatedSlf4j";
	private static Loggers logger;
	static{
		String classPath=defCp;
		boolean flag=ClassUtil.hasClass("org.slf4j.LoggerFactory");
		if(flag){
			classPath=slf4jCp;
		}else{
			flag=ClassUtil.hasClass("org.apache.log4j.Logger");
			if(flag){
				classPath=log4jCp;
			}
		}
		logger=ClassUtil.newInstance(classPath);
		logger.info(LoggerHelper.class,classPath);
	}
	public static Loggers getLogger(){
		return logger;
	}
	public static Loggers getLogger(Type type){
		String cp=defCp;
		if(type.equals(Type.slf4j)){
			cp=slf4jCp;
		}else if(type.equals(Type.log4j)){
			cp=log4jCp;
		}
		return ClassUtil.newInstance(cp);
	}
	public static  void debug(Class<?> c,String msg){
		logger.debug(c,msg);
	}
	public static  void info(Class<?> c,String msg){
		logger.info(c,msg);
	}
	public static  void warn(Class<?> c,String msg){
		logger.warn(c,msg);
	}
	public static  void error(Class<?> c,String msg){
		logger.error(c,msg);
	}
	
}
