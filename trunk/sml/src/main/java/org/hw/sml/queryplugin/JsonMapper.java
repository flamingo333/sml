package org.hw.sml.queryplugin;
/**
 * @author wen
 */
public interface JsonMapper {
	 <T> T toObj(String json,Class<T> t);
	 String toJson(Object obj);
}
