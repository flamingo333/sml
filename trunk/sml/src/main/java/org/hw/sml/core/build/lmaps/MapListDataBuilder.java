package org.hw.sml.core.build.lmaps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapListDataBuilder extends AbstractDataBuilder{
	public Object build(List<Map<String, Object>> datas) {
		Map<String,List<Object>> result=new LinkedHashMap<String, List<Object>>();
		for(Map<String,Object> data :datas){
			String key=rebuildParam.get("key")==null?"key":rebuildParam.get("key");
			String value=rebuildParam.get("value")==null?"value":rebuildParam.get("value");
			if(!result.containsKey(key)){
				result.put(key,new ArrayList<Object>());
			}
			result.get(key).add(data.get(value));
		}
		return result;
	}

}
