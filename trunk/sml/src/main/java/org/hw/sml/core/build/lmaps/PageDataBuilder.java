package org.hw.sml.core.build.lmaps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hw.sml.FrameworkConstant;
import org.hw.sml.model.Result;
import org.hw.sml.tools.MapUtils;
/**
 * 通过rebuildParam来包装数据结构
 * extMap k-v  可无限设定参数
 * 		limitMark   每页大小标识
 * 		pageMark    当前页值标识
 * @author wen
 */
public class PageDataBuilder extends AbstractDataBuilder {
	public Object build(List<Map<String, Object>> datas) {
		String[] oriFields=rebuildParam.getOriFields();
		String[] newFields=rebuildParam.getNewFields();
		int page=Integer.parseInt(String.valueOf(sqlTemplate.getSmlParams().getSmlParam(MapUtils.getString(rebuildParam.getExtMap(),"pageMark","page")).getValue()));
		int limit=Integer.parseInt(String.valueOf(sqlTemplate.getSmlParams().getSmlParam(MapUtils.getString(rebuildParam.getExtMap(),"limitMark","limit")).getValue()));
		Result result=new Result();
		result.setPage(page);
		result.setLimit(limit);
		if(sqlTemplate.getSmlParams().getSmlParam(FrameworkConstant.PARAM_QUERYTYPE).getValue().equals("count")){
			Map<String,Object> countData=datas.get(0);
			Long count=Long.parseLong(String.valueOf(countData.get(countData.keySet().iterator().next())));
			result.setCount(count);
			result.setExtInfo(countData);
			if(count>0){
				sqlTemplate.getSmlParams().getSmlParam(FrameworkConstant.PARAM_QUERYTYPE).setValue("select");
				List<Map<String,Object>> data=smlContextUtils.getSqlMarkupAbstractTemplate().querySql(sqlTemplate);
				data=toCase(data);
				countData=toCase(countData);
				if(oriFields!=null&&newFields!=null){
					data=MapUtils.rebuildMp(data,oriFields,newFields,Boolean.valueOf(rebuildParam.get(FrameworkConstant.PARAM_FIELDFILTER)));
				}
				result.setDatas(data);
			}else{
				result.setDatas(new ArrayList<Map<String,Object>>());
			}
		}else{
			if(oriFields!=null&&newFields!=null){
				datas=MapUtils.rebuildMp(datas,oriFields,newFields,Boolean.valueOf(rebuildParam.get(FrameworkConstant.PARAM_FIELDFILTER)));
			}
			datas=toCase(datas);
			result.setDatas(datas);
			int size=datas.size();
			if(size<limit&&page==1){
				result.setCount(Long.valueOf(size));
			}else{
				sqlTemplate.getSmlParams().getSmlParam(FrameworkConstant.PARAM_QUERYTYPE).setValue("count");
				List<Map<String,Object>> data=smlContextUtils.getSqlMarkupAbstractTemplate().querySql(sqlTemplate);
				Map<String,Object> countData=data.get(0);
				Long count=Long.parseLong(String.valueOf(countData.get(countData.keySet().iterator().next())));
				result.setExtInfo(toCase(countData));
				result.setCount(count);
			}
		}
		return result;
	}
	private List<Map<String,Object>> toCase(List<Map<String,Object>> datas){
		if(rebuildParam.get(FrameworkConstant.PARAM_TOCASEFORKEY)!=null){
			datas=MapUtils.toCaseForKey((List<Map<String,Object>>)datas,rebuildParam.get(FrameworkConstant.PARAM_TOCASEFORKEY));
		}else{
			if(Boolean.valueOf(rebuildParam.get(FrameworkConstant.PARAM_TOLOWERCASEFORKEY)))
				datas=MapUtils.toLowerCaseForKey((List<Map<String,Object>>)datas);
		}
		return datas;
	}
	private Map<String,Object> toCase(Map<String,Object> datas){
		if(rebuildParam.get(FrameworkConstant.PARAM_TOCASEFORKEY)!=null){
			datas=MapUtils.toCaseForKey(datas,rebuildParam.get(FrameworkConstant.PARAM_TOCASEFORKEY));
		}else{
			if(Boolean.valueOf(rebuildParam.get(FrameworkConstant.PARAM_TOLOWERCASEFORKEY)))
				datas=MapUtils.toLowerCaseForKey(datas);
		}
		return datas;
	}
}