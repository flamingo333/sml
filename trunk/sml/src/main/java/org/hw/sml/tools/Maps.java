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
	@SuppressWarnings({"unchecked", "rawtypes" })
	public static Map<String,String> newMap(String ...strs){
		return (Map)newMapObject(strs);
	}
	public static Map<String,Object> newMapObject(Object ... objs){
		Map<String,Object> result=new HashMap<String,Object>();
		for(int i=0;i<objs.length;i+=2){
			result.put(objs[i].toString(),objs[i+1]);
		}
		return result;
	}
	public static void main(String[] args) {
		System.out.println(Maps.newMap("a","a"));
	}
}
