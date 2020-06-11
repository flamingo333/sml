package org.hw.sml.tools;

import java.util.List;

import com.eastcom_sw.inas.core.service.jdbc.JsonMapper;

/**
 * @author wen
 */
public interface ArrayJsonMapper extends JsonMapper{
	 <T> List<T> toArray(String json,Class<T> t);

	String toJson(Object entity);
}
