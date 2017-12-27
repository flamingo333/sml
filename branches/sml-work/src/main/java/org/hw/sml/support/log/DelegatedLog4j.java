package org.hw.sml.support.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class DelegatedLog4j implements Loggers{
	private static final String FQCN = DelegatedLog4j.class.getName();
	public void debug(Class<?> clazz, String msg) {
		Logger.getLogger(clazz).log(FQCN, Level.DEBUG, msg,null);
	}

	public void info(Class<?> clazz, String msg) {
		Logger.getLogger(clazz).log(FQCN, Level.INFO, msg,null);
	}

	public void warn(Class<?> clazz, String msg) {
		Logger.getLogger(clazz).log(FQCN, Level.WARN, msg,null);
	}

	public void error(Class<?> clazz, String msg) {
		Logger.getLogger(clazz).log(FQCN, Level.ERROR, msg,null);
	}
}
