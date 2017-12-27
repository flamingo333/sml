package org.hw.sml.support.log;

import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.spi.LocationAwareLogger;

public class DelegatedSlf4j implements Loggers{
	private static Marker maker=MarkerFactory.getMarker("SML");
	public static final String FQCN=DelegatedSlf4j.class.getSimpleName();
	
	public void debug(Class<?> clazz, String msg) {
		getLogger(clazz).log(maker, FQCN,LocationAwareLogger.DEBUG_INT, msg, null,null);
	}
	public void info(Class<?> clazz, String msg) {
		getLogger(clazz).log(maker, FQCN,LocationAwareLogger.INFO_INT, msg, null,null);
	}
	public void warn(Class<?> clazz, String msg) {
		getLogger(clazz).log(maker, FQCN,LocationAwareLogger.WARN_INT, msg, null,null);
	}
	public void error(Class<?> clazz, String msg) {
		getLogger(clazz).log(maker, FQCN,LocationAwareLogger.ERROR_INT, msg, null,null);
	}
	public LocationAwareLogger getLogger(Class<?> clazz){
		LocationAwareLogger logger=(LocationAwareLogger) LoggerFactory.getLogger(clazz);
		return logger;
	}
}
