package org.hw.sml.support.ioc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

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
	public static Map<String,Object> getValuesWithoutPrefix(String keyStart,Map<String,String> kvs){
		Map<String, Object> result=MapUtils.newLinkedHashMap();
		keyStart=keyStart.endsWith(".")?keyStart:keyStart+".";
		for(Map.Entry<String,String> entry:kvs.entrySet()){
			if(entry.getKey().startsWith(keyStart)){
				String key=entry.getKey();
				key=key.replace(keyStart,"");
				if(!key.contains("."))
					result.put(key,entry.getValue());
				else{
					String[] ss=key.split("\\.",2);
					if(!result.containsKey(ss[0])){
						if(ss[1].startsWith("list-")){
							result.put(ss[0],new ArrayList<String>());
						}else if(ss[1].startsWith("set-")){
							result.put(ss[0],new TreeSet<String>());
						}else{
							result.put(ss[0],new HashMap<String,String>());
						}
					}
					if(ss[1].startsWith("list-")||ss[1].startsWith("set-")){
						((Collection<String>)result.get(ss[0])).add(entry.getValue());
					}else{
						((Map<String,String>)result.get(ss[0])).put(ss[1],entry.getValue());
					}
				}
			}
		}
		return result;
	}
	@SuppressWarnings("unchecked")
	public Map<String,Object> getValuesWithoutPrifix(String keyStart){
		return getValuesWithoutPrefix(keyStart,propertiesMap);
	}
	public PropertiesHelper renameValue(String withOutStartKey){
		for(Map.Entry<String,String> entry:propertiesMap.entrySet()){
			if(!entry.getKey().startsWith(withOutStartKey)){
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
	public boolean exists(String[] conditionalOnExistsVals) {
		for(String cev:conditionalOnExistsVals){
			if(!propertiesMap.containsKey(cev)){
				return false;
			}
		}
		return true;
	}
	public boolean isTrueVal(String[] conditionOnMatchVals) {
		for(String cev:conditionOnMatchVals){
			if(!Boolean.valueOf(propertiesMap.get(cev))){
				return false;
			}
		}
		return true;
	}
	public String getsValue(String ... keys) {
		String value=null;
		for(String key:keys){
			value=propertiesMap.get(key);
			if(value!=null){
				break;
			}
		}
		return value;
	}
	public boolean isTrueValMissingTrue(String conditionOnMatchValMissingTrue) {
		if(propertiesMap.containsKey(conditionOnMatchValMissingTrue)){
			return Boolean.valueOf(propertiesMap.get(conditionOnMatchValMissingTrue));
		}else{
			return true;
		}
	}
}