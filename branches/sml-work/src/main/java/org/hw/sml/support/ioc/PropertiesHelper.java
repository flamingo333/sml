package org.hw.sml.support.ioc;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hw.sml.tools.MapUtils;
import org.hw.sml.tools.RegexUtils;

public class PropertiesHelper {
	public  Map<String,String> propertiesMap=MapUtils.newLinkedHashMap();
	
	public PropertiesHelper(){
	}
	public PropertiesHelper withProperties(Properties properties){
		Enumeration<Object> keys=properties.keys();
		while(keys.hasMoreElements()){
			String key=(String) keys.nextElement();
			propertiesMap.put(key,properties.getProperty(key));
		}
		return this;
	}
	public Map<String,String> getValuesByKeyStart(String keyStart){
		Map<String, String> result=MapUtils.newLinkedHashMap();
		for(Map.Entry<String,String> entry:propertiesMap.entrySet()){
			if(entry.getKey().startsWith(keyStart)){
				result.put(entry.getKey(),entry.getValue());
			}
		}
		return result;
	}
	public PropertiesHelper renameValue(String withOutStartKey){
		for(Map.Entry<String,String> entry:propertiesMap.entrySet()){
			if(!entry.getKey().startsWith("bean-")){
				String value=entry.getValue();
				List<String> ms=RegexUtils.matchGroup("\\$\\{[\\w|.|-]+\\}",value);
				if(ms.size()==0) continue;
				for(String m:ms){
					String vt=getValue(m.substring(2,m.length()-1));
					if(vt!=null)
						value=value.replace(m,vt);
				}
				propertiesMap.put(entry.getKey(),value);
			}
		}
		return this;
	}
	public String getValue(String key) {
		return propertiesMap.get(key);
	}
	public Map<String,String> getValues(){
		return propertiesMap;
	}
}