package org.hw.sml.tools;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Maps<K,V> {
	
	private Map<K,V> map;
	
	public Maps(){
		map=new LinkedHashMap<K,V>();
	}
	public Maps(Map<K,V> map){
		this.map=map;
	}
	public Maps<K,V> put(K k,V v){
		map.put(k, v);
		return this;
	}
	public Maps<K,V> remove(K k){
		 map.remove(k);
		 return this;
	}
	
	public Map<K, V> getMap() {
		return map;
	}
	public static Map<String,String> newMap(String ...strs){
		Map<String,String> result=new HashMap<String,String>();
		for(int i=0;i<strs.length;i+=2){
			result.put(strs[i],strs[i+1]);
		}
		return result;
	}
	
}
