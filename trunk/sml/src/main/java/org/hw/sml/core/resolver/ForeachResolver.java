package org.hw.sml.core.resolver;

import java.util.List;

import org.hw.sml.model.SMLParam;
import org.hw.sml.model.SMLParams;
import org.hw.sml.support.el.El;
import org.hw.sml.tools.Assert;
import org.hw.sml.tools.RegexUtils;
/**
 * 
 * @author wen
 * foreach 语法
 * property:参数必须为数组array-char等
 * mid：中间连接符
 *
 */
public class ForeachResolver implements SqlResolver{
	public Rst resolve(String dialect, String temp, SMLParams smlParams) {
		List<String> mathers=null;
		if(temp.contains("<foreach")){
			mathers=RegexUtils.matchGroup("<foreach\\d* property=\"\\w+\" mid=\"\\S*\">",temp);
			for(String mather:mathers){
				String tmt=mather;
				int start=temp.indexOf(tmt);
				if(!temp.contains(tmt)){
					continue;
				}
				String mark=RegexUtils.subString(tmt, "<", " property=");
				int end=temp.indexOf("</"+mark+">",start);
				Assert.isTrue(end!=-1,mather+" must has end!");
				//整个逻辑字符串 tm
				String tm=temp.substring(start,end+("</"+mark+">").length());
				String property=RegexUtils.subString(tm,"property=\"","\" mid=");
				String fix=RegexUtils.subString(tm,"mid=\"","\">");
				SMLParam sp=smlParams.getSmlParam(property);
				Assert.notNull(sp, property+" is not config for "+mark);
				Assert.isTrue(sp.getValue().getClass().isArray(),property+" is not a array for "+mark);
				String content=RegexUtils.subString(tm,">",("</"+mark+">"));
				Object[] objs=(Object[]) sp.getValue();
				StringBuffer sb=new StringBuffer();
				for(int i=0;i<objs.length;i++){
					Object obj=objs[i];
					sb.append(content.replace("@value",obj.toString()));
					if(i<objs.length-1){
						sb.append(fix);
					}
				}
				temp=temp.replace(tm, sb.toString());
			}
		}
		return new Rst(temp);
	}

	public void setEl(El el) {
	}
	
}
