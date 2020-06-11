package org.hw.sml.core.resolver;

import java.util.List;

import org.hw.sml.model.SMLParams;
import org.hw.sml.support.el.El;
import org.hw.sml.tools.MapUtils;
import org.hw.sml.tools.Maps;
import org.hw.sml.tools.RegexUtils;
/**
 * 
 * @author wen
 *
 */
public class CollectionResolver implements SqlResolver{
	
	public Rst resolve(String dialect,String temp,SMLParams sqlParamMaps) {
		List<String> mathers=null;
		//对数据进行
		if(temp.contains("<collection")){
			List<CollectionHandler> collections=MapUtils.newArrayList();
			mathers=RegexUtils.matchGroup("<collection id=\"\\w+\" ref=\"\\S+\" result=\"\\w+\"(/?)>",temp);
			for(String mather:mathers){
				String tmt=mather;
				if(!temp.contains(tmt)){
					continue;
				}
				String id=RegexUtils.subString(tmt, "id=\"", "\" ref=\"");
				String ref=RegexUtils.subString(tmt,"ref=\"","\" result=\"");
				if(tmt.endsWith("/>")){
					String result=RegexUtils.subString(tmt,"result=\"","\"/>");
					collections.add(new CollectionHandler(id, ref, result));
					temp=temp.replace(tmt,"");
				}else{
					String result=RegexUtils.subString(tmt,"result=\"","\">");
					int start=temp.indexOf(tmt);
					int end=temp.indexOf("</collection>",start);
					String tm=temp.substring(start,end+("</collection>").length());
					String content=RegexUtils.subString(tm,">",("</collection>"));
					temp=temp.replace(tm,"");
					collections.add(new CollectionHandler(id,ref, result,content));
				}
			}
			Rst rst=new Rst(temp);
			rst.setExtInfo(Maps.newMapObject("collections",collections));
			return rst;
		}
		
		return new Rst(temp);
	}
	public void setEl(El el) {
	}
}
