package org.hw.sml.core.resolver;


import java.util.List;
import java.util.Map;

import org.hw.sml.FrameworkConstant;
import org.hw.sml.core.resolver.exception.TagEOFException;
import org.hw.sml.model.SMLParams;
import org.hw.sml.support.el.El;
import org.hw.sml.tools.Assert;
import org.hw.sml.tools.MapUtils;
import org.hw.sml.tools.RegexUtils;
/**
 * 
 * @author hw
 * 后续处理：增加数据库
 */
public class SelectSqlResolver implements SqlResolver{
	public synchronized Rst resolve(String dialect, String temp,SMLParams sqlParamMaps) {
		List<String> mathers=null;
		Map<String,Object> selectResult=MapUtils.newHashMap();
		if(temp.contains("<select")){
		//单个sql，反复出现的逻辑选择
			mathers=RegexUtils.matchGroup("<select\\d* id=\"\\w+\">",temp);
			for(String mather:mathers){
				String tmt=mather;
				int start=temp.indexOf(tmt);
				if(!temp.contains(tmt)){
					continue;
				}
				//取标签值
				String mark=RegexUtils.subString(tmt, "<", " id=");
				int end=temp.indexOf("</"+mark+">",start);
				if(end==-1){
					throw new TagEOFException(mather+" must has end!");
				}
				//整个逻辑字符串 tm
				String tm=temp.substring(start,end+("</"+mark+">").length());
				//属性值
				String id=RegexUtils.subString(tm,"id=\"","\">");
				//内容
				String content=RegexUtils.subString(tm,">",("</"+mark+">"));
				if(id.equals(FrameworkConstant.PARAM_RESULTMAP)){
					selectResult.put(id,content);
				}
				Assert.notRpeatMark(content,mark);
				String replaceId="<included id=\""+id+"\"/>";
				temp=temp.replace(tm,"");
				temp=temp.replace(replaceId,content);
			}
		}
		return new Rst(temp).setExtInfo(selectResult);
	}
	


	public void setEl(El el) {
		
	}


	
	
}
