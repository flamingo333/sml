package org.hw.sml.core.build.lmaps;

import java.util.List;
import java.util.Map;

import org.hw.sml.core.build.SmlTools;
import org.hw.sml.tools.MapUtils;

public class TreeDataBuilder extends AbstractDataBuilder{

	public Object build(List<Map<String, Object>> datas) {
		String id=getRebuildParam().get("id","id");
		String pid=getRebuildParam().get("pid","pid");
		String children=getRebuildParam().get("children","children");
		String attributes=getRebuildParam().get("attributes","attributes");
		boolean containAttributes=Boolean.valueOf(getRebuildParam().get("containAttributes","true"));
		String removeEles=getRebuildParam().get("removeEles","");
		String attributesList=getRebuildParam().get("attributesList");
		if(SmlTools.isNotEmpty(attributesList)){
				for(Map<String,Object> data:datas){
					Map<String,Object> abs=MapUtils.newHashMap();
					if(containAttributes){
						for(String ab:attributesList.split(",")){
							abs.put(ab,data.get(ab));
						}
						data.put(attributes,abs);
					}
					if(SmlTools.isNotEmpty(removeEles))
					for(String el:removeEles.split(",")){
						data.remove(el);
					}
				}
		}else{
			for(Map<String,Object> data:datas){
				Map<String,Object> abs=MapUtils.newHashMap();
				if(containAttributes){
					data.put(attributes,abs);
				}
				abs.putAll(data);
			}
		}
		String rootId=getRebuildParam().get("rootId",null);
		return MapUtils.createTree(datas, id, pid, children, rootId);
	}

}
