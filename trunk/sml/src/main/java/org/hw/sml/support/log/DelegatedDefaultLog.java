package org.hw.sml.support.log;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DelegatedDefaultLog implements Loggers{
	public void debug(Class<?> clazz, String msg) {
		Logger.getLogger(clazz.getSimpleName()).log(Level.INFO, msg);
	}

	public void info(Class<?> clazz, String msg) {
		Logger.getLogger(clazz.getSimpleName()).log(Level.INFO, msg);
	}

	public void warn(Class<?> clazz, String msg) {
		Logger.getLogger(clazz.getSimpleName()).log(Level.WARNING, msg);
	}

	public void error(Class<?> clazz, String msg) {
		Logger.getLogger(clazz.getSimpleName()).log(Level.SEVERE, msg);
	}

}
