package org.hw.sml.core.build.lmaps;

import java.util.List;
import java.util.Map;

import org.hw.sml.tools.MapUtils;

public class GroupDataBuilder extends AbstractDataBuilder{
	public Object build(List<Map<String, Object>> datas) {
		return MapUtils.groupMpLst(datas, rebuildParam.getGroupname(), rebuildParam.getGroupFields());
	}
}
