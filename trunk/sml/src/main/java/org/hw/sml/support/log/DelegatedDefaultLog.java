package org.hw.sml.support.log;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DelegatedDefaultLog implements Loggers{
	
	private void log(Class<?> c,Level level, String msg, Throwable ex)
	  {
	    Logger logger = Logger.getLogger(c.getSimpleName());
	    if (logger.isLoggable(level))
	    {
	      Throwable dummyException = new Throwable();
	      StackTraceElement[] locations = dummyException.getStackTrace();

	      String cname = "unknown";
	      String method = "unknown";
	      if ((locations != null) && (locations.length > 2)) {
	        StackTraceElement caller = locations[2];
	        cname = caller.getClassName();
	        method = caller.getMethodName();
	      }
	      if (ex == null)
	        logger.logp(level, cname, method, msg);
	      else
	        logger.logp(level, cname, method, msg, ex);
	    }
	  }
	public void debug(Class<?> clazz, String msg) {
		log(clazz,Level.INFO, msg,null);
	}

	public void info(Class<?> clazz, String msg) {
		log(clazz,Level.INFO, msg,null);
	}

	public void warn(Class<?> clazz, String msg) {
		log(clazz,Level.WARNING, msg,null);
	}

	public void error(Class<?> clazz, String msg) {
		log(clazz,Level.SEVERE, msg,null);
	}
}
