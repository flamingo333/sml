package org.hw.sml.support.log;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DelegatedDefaultLog implements Loggers{
	static Logger logger;
	static{
		logger=Logger.getAnonymousLogger();
		logger.setLevel(Level.ALL);
		ConsoleHandler ch=new ConsoleHandler();
		ch.setLevel(Level.ALL);
		logger.addHandler(ch);
	}
	public void debug(Class<?> clazz, String msg) {
		logger.log(Level.INFO, msg);
	}

	public void info(Class<?> clazz, String msg) {
		logger.log(Level.INFO, msg);
	}

	public void warn(Class<?> clazz, String msg) {
		logger.log(Level.WARNING, msg);
	}

	public void error(Class<?> clazz, String msg) {
		logger.log(Level.SEVERE, msg);
	}

}
