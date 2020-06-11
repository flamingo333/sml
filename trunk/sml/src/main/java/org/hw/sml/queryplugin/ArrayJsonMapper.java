package org.hw.sml.queryplugin;

import java.util.List;

/**
 * @author wen
 */
public interface ArrayJsonMapper extends JsonMapper{
	 <T> List<T> toArray(String json,Class<T> t);
}
