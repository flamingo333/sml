package org.hw.sml.core.build.lmaps;

import java.util.List;
import java.util.Map;

import org.hw.sml.tools.MapUtils;

public class SingleDataBuilder extends AbstractDataBuilder{
	public Object build(List<Map<String, Object>> datas) {
		Map<String,Object> map=MapUtils.newLinkedHashMap();
		int size=datas.size();
		if(size>0&&rebuildParam.getIndex().equals("last")){
			map=datas.get(size-1);
		}else{
			int index=Integer.parseInt(rebuildParam.getIndex());
			if(size>index){
				map=datas.get(index);
			}
		}
		map=MapUtils.rebuildMp(map,rebuildParam.getOriFields(),rebuildParam.getNewFields());
		return map;
	}

}
