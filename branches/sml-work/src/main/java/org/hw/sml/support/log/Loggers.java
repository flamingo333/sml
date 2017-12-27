package org.hw.sml.support.log;


public abstract  interface Loggers {
	public void debug(Class<?> clazz,String msg);
	public void info(Class<?> clazz,String msg);
	public void warn(Class<?> clazz,String msg);
	public void error(Class<?> clazz,String msg);
}
