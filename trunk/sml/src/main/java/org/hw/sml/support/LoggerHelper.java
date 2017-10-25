package org.hw.sml.support;

import org.hw.sml.support.log.Loggers;
import org.hw.sml.tools.ClassUtil;


public class LoggerHelper {
	public static Loggers logger;
	static{
		String classPath="org.hw.sml.support.log.DelegatedDefaultLog";
		boolean flag=ClassUtil.hasClass("org.slf4j.LoggerFactory");
		if(flag){
			classPath="org.hw.sml.support.log.DelegatedSlf4j";
		}else{
			flag=ClassUtil.hasClass("org.apache.log4j.Logger");
			if(flag){
				classPath="org.hw.sml.support.log.DelegatedLog4j";
			}
		}
		logger=ClassUtil.newInstance(classPath);
		info(LoggerHelper.class,classPath);
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
