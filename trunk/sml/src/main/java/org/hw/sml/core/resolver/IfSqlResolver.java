package org.hw.sml.core.resolver;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.hw.sml.core.resolver.exception.ParamNotConfigException;
import org.hw.sml.core.resolver.exception.ParamNullException;
import org.hw.sml.core.resolver.exception.TagEOFException;
import org.hw.sml.core.resolver.exception.TagException;
import org.hw.sml.model.SMLParam;
import org.hw.sml.model.SMLParams;
import org.hw.sml.support.el.El;
import org.hw.sml.support.el.ElException;
import org.hw.sml.support.el.SmlElContext;
import org.hw.sml.tools.Assert;
import org.hw.sml.tools.DateTools;
import org.hw.sml.tools.MapUtils;
import org.hw.sml.tools.RegexUtils;


/**
 * 复杂逻辑的实现
 * 改变sql走向，借鉴mybatis ibaits语法  不引入ognl xml语法，而是自己实现
 * 减少依赖
 * @author hw
 *后续解决：  if else逻辑    case when 逻辑
 *2016-03-09  empty 把空字符串纳入空
 *2018-11-26  isNull|isNotNull 标签引入
 */
public class IfSqlResolver implements SqlResolver{
	
	private El el;
	public Rst resolve(String dialect, String temp,SMLParams sqlParamMaps) {
		List<String> mathers=null;
		Map<String,Boolean> tempMap=MapUtils.newHashMap();
		Map<String,Object> mapParam=sqlParamMaps.getMap();
		if(temp.contains("<smlParam")){
			mathers=RegexUtils.matchGroup("<smlParam name=\"\\w+\" value=\"\\S+\"/>",temp);
			for(String mather:mathers){
				String tmt=mather;
				if(!temp.contains(tmt)){
					continue;
				}
				String name=RegexUtils.subString(tmt, "name=\"", "\" value=");
				String value=RegexUtils.subString(tmt, "value=\"","\"/>");
				if(value.startsWith("ref:")){
					value=value.replaceFirst("ref:","");
					String[] vs=value.split("\\|");
					Map<String,Object> params=sqlParamMaps.getMap();
					if(MapUtils.getString(params,vs[0])==null){
						throw new ParamNotConfigException("param ["+vs[0]+"]  is not config!");
					}
					sqlParamMaps.add(name,getRefFormatValue(MapUtils.getString(params,vs[0]),vs));
				}else{
					try {
						sqlParamMaps.add(name,SmlElContext.defaultEl().evel(value));
					} catch (ElException e) {
						throw new TagException(mather+" error:["+e.getMessage()+"]",e);
					}
				}
				temp=temp.replace(tmt,"");
			}
			sqlParamMaps.reinit();
		}
		if(temp.contains("<isNotEmpty")){
		//非空函数   \\d*用于嵌套
			mathers=RegexUtils.matchGroup("<isNotEmpty\\d* property=\"\\w+\">",temp);
			for(String mather:mathers){
				String tmt=mather;
				int start=temp.indexOf(tmt);
				if(!temp.contains(tmt)){
					continue;
				}
				//取标签值
				String mark=RegexUtils.subString(tmt, "<", " property=");
				int end=temp.indexOf("</"+mark+">",start);
				if(end==-1){
					throw new TagEOFException(mather+" must has end!");
				}
				//整个逻辑字符串 tm
				String tm=temp.substring(start,end+("</"+mark+">").length());
				boolean flag=false;
				if(!tempMap.containsKey(tmt)){
					String property=RegexUtils.subString(tm,"property=\"","\">");
					//内容
					SMLParam sp=sqlParamMaps.getSmlParam(property);
					if(sp==null){
						throw new ParamNotConfigException(property+" is not config for "+mark);
					}
					flag=sp!=null?(sp.getValue()!=null&&String.valueOf(sp.getValue()).length()>0):false;
					tempMap.put(tmt,flag);
				}else{
					flag=tempMap.get(tmt);
				}
				String content=RegexUtils.subString(tm,">",("</"+mark+">"));
				Assert.notRpeatMark(content,mark);
				if(flag){
					temp=temp.replace(tm, content);
				}else{
					temp=temp.replace(tm," ");
				}
			}
		}
		if(temp.contains("<isNotNull")){
			//非空函数   \\d*用于嵌套
				mathers=RegexUtils.matchGroup("<isNotNull\\d* property=\"\\w+\">",temp);
				for(String mather:mathers){
					String tmt=mather;
					int start=temp.indexOf(tmt);
					if(!temp.contains(tmt)){
						continue;
					}
					//取标签值
					String mark=RegexUtils.subString(tmt, "<", " property=");
					int end=temp.indexOf("</"+mark+">",start);
					if(end==-1){
						throw new TagEOFException(mather+" must has end!");
					}
					//整个逻辑字符串 tm
					String tm=temp.substring(start,end+("</"+mark+">").length());
					boolean flag=false;
					if(!tempMap.containsKey(tmt)){
						String property=RegexUtils.subString(tm,"property=\"","\">");
						//内容
						SMLParam sp=sqlParamMaps.getSmlParam(property);
						if(sp==null){
							throw new ParamNotConfigException(property+" is not config for "+mark);
						}
						flag=sp!=null?(sp.getValue()!=null):false;
						tempMap.put(tmt,flag);
					}else{
						flag=tempMap.get(tmt);
					}
					String content=RegexUtils.subString(tm,">",("</"+mark+">"));
					Assert.notRpeatMark(content,mark);
					if(flag){
						temp=temp.replace(tm, content);
					}else{
						temp=temp.replace(tm," ");
					}
				}
		}
		if(temp.contains("<isEmpty")){
		//空函数
			mathers=RegexUtils.matchGroup("<isEmpty\\d* property=\"\\w+\">",temp);
			for(String mather:mathers){
				String tmt=mather;
				if(!temp.contains(tmt)){
					continue;
				}
				int start=temp.indexOf(tmt);
				String mark=RegexUtils.subString(tmt, "<", " property=");
				int end=temp.indexOf("</"+mark+">",start);
				if(end==-1){
					throw new TagEOFException(mather+" must has end!");
				}
				String tm=temp.substring(start,end+("</"+mark+">").length());
				boolean flag=false;
				if(!tempMap.containsKey(tmt)){
					String property=RegexUtils.subString(tm,"property=\"","\">");
					SMLParam sp=sqlParamMaps.getSmlParam(property);
					if(sp==null){
						throw new ParamNotConfigException(property+" is not config for "+mark);
					}
					flag=sp==null?true:(sp.getValue()==null||String.valueOf(sp.getValue()).length()==0);
					tempMap.put(tmt,flag);
				}else{
					flag=tempMap.get(tmt);
				}
				String content=RegexUtils.subString(tm,">",("</"+mark+">"));
				Assert.notRpeatMark(content,mark);
				if(flag){
					temp=temp.replace(tm, content);
				}else{
					temp=temp.replace(tm," ");
				}
			}
		}
		if(temp.contains("<isNull")){
			//空函数
				mathers=RegexUtils.matchGroup("<isNull\\d* property=\"\\w+\">",temp);
				for(String mather:mathers){
					String tmt=mather;
					if(!temp.contains(tmt)){
						continue;
					}
					int start=temp.indexOf(tmt);
					String mark=RegexUtils.subString(tmt, "<", " property=");
					int end=temp.indexOf("</"+mark+">",start);
					if(end==-1){
						throw new TagEOFException(mather+" must has end!");
					}
					String tm=temp.substring(start,end+("</"+mark+">").length());
					boolean flag=false;
					if(!tempMap.containsKey(tmt)){
						String property=RegexUtils.subString(tm,"property=\"","\">");
						SMLParam sp=sqlParamMaps.getSmlParam(property);
						if(sp==null){
							throw new ParamNotConfigException(property+" is not config for "+mark);
						}
						flag=sp==null?true:(sp.getValue()==null);
						tempMap.put(tmt,flag);
					}else{
						flag=tempMap.get(tmt);
					}
					String content=RegexUtils.subString(tm,">",("</"+mark+">"));
					Assert.notRpeatMark(content,mark);
					if(flag){
						temp=temp.replace(tm, content);
					}else{
						temp=temp.replace(tm," ");
					}
				}
			}
		if(temp.contains("<isEqual")){
			//相等函数
			mathers=RegexUtils.matchGroup("<isEqual\\d* property=\"\\w+\" compareValue=\"\\w+\">",temp);
			for(String mather:mathers){
				String tmt=mather;
				if(!temp.contains(tmt)){
					continue;
				}
				int start=temp.indexOf(tmt);
				String mark=RegexUtils.subString(tmt, "<", " property=");
				int end=temp.indexOf("</"+mark+">",start);
				if(end==-1){
					throw new TagEOFException(mather+" must has end!");
				}
				String tm=temp.substring(start,end+("</"+mark+">").length());
				String property=RegexUtils.subString(tm,"property=\"","\" compareValue");
				String value=RegexUtils.subString(tm,"compareValue=\"","\">");
				String content=RegexUtils.subString(tm,">",("</"+mark+">"));
				Assert.notRpeatMark(content,mark);
				SMLParam sp=sqlParamMaps.getSmlParam(property);
				if(sp==null){
					throw new ParamNotConfigException(property+" is not config for "+mark);
				}
				boolean flag=sp==null?false:(value.equals(sp.getValue()));
				if(flag){
					temp=temp.replace(tm, content);
				}else{
					temp=temp.replace(tm," ");
				}
			}
		}
		if(temp.contains("<if")){
			//最复杂函数实现 引入表达示语言实现
			mathers=RegexUtils.matchGroup("<if\\d* test=\"\\s+\\S*\\s+\">",temp);
			for(String mather:mathers){
				String tmt=mather;
				if(!temp.contains(tmt)){
					continue;
				}
				int start=temp.indexOf(tmt);
				String mark=RegexUtils.subString(tmt, "<", " test=");
				int end=temp.indexOf("</"+mark+">",start);
				if(end==-1){
					throw new TagEOFException(mather+" must has end!");
				}
				String tm=temp.substring(start,end+("</"+mark+">").length());
				String text=RegexUtils.subString(tm,"test=\"","\">");
				String content=RegexUtils.subString(tm,">",("</"+mark+">"));
				Assert.notRpeatMark(content,mark);
				//对text内容进行处理
				//参数报错直接跳出，减少对数据库的压力
				boolean flag=false;
				if(!tempMap.containsKey(tmt)){
					try {
						flag = el.parser(text,mapParam);
					} catch (Exception e) {//
						throw new TagException("jsElP["+text+"]",e);
					}
					tempMap.put(tmt,flag);
				}else{
					flag=tempMap.get(tmt);
				}
				if(flag){
					temp=temp.replace(tm, content);
				}else{
					temp=temp.replace(tm," ");
				}
			}
		}
		return new Rst(temp);
	}
	private String getRefFormatValue(String value, String[] vs) {
		if(vs.length==2){
			String vs1=vs[1];
			//|date@('')
			if(vs1.startsWith("date@")){
				vs1=vs1.substring(7,vs1.length()-2);
				value=parseDate(value.substring(0,vs1.length()),vs1);
			}else if(vs1.startsWith("dates@")){
				vs1=vs1.substring(8,vs1.length()-2);
				StringBuffer sb=new StringBuffer();
				for(String v:value.split(",")){
					sb.append(parseDate(v.substring(0,vs1.length()),vs1)).append(",");
				}
				value=sb.deleteCharAt(sb.length()-1).toString();
			}
		}
		return value;
	}
	private String parseDate(String time,String pattern){
		return new SimpleDateFormat("yyyyMMddHHmmss").format(DateTools.getFormatTime(time,pattern));
	}
	public El getEl() {
		return el;
	}

	public void setEl(El el) {
		this.el = el;
	}


}
