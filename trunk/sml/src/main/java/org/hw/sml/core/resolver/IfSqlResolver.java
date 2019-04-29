package org.hw.sml.core.resolver;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hw.sml.core.build.SmlTools;
import org.hw.sml.core.resolver.exception.ParamNotConfigException;
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
	public  static String isNull="isNull";
	public  static String isNotNull="isNotNull";
	public  static String isEmpty="isEmpty";
	public  static String isNotEmpty="isNotEmpty";
	
	final static Pattern EMPTY_NULL=Pattern.compile("<("+isNotEmpty+"\\d*|"+isEmpty+"\\d*|"+isNull+"\\d*|"+isNotNull+"\\d*)\\s+property=\"(\\w+)\">");
	final static Pattern SMLPARAM=Pattern.compile("<smlParam\\s+name=\"(\\w+)\"\\s+value=\"(\\S+)\"/>");
	final static Pattern IF=Pattern.compile("<(if\\d*)\\s+test=\"((?!\">).)+\">");
	private El el;
	public Rst resolve(String dialect, String temp,SMLParams sqlParamMaps) {
		List<String> mathers=null;
		Map<String,Boolean> tempMap=MapUtils.newHashMap();
		Map<String,Object> mapParam=sqlParamMaps.getMap();
		Matcher matcher=SMLPARAM.matcher(temp);
		while(matcher.find()){
			String mather=matcher.group();
			if(!temp.contains(mather)){
				continue;
			}
			String[] subGroups=RegexUtils.group2Array(matcher);
			String name=subGroups[0];
			String value=subGroups[1];
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
			temp=temp.replace(mather,"");
			sqlParamMaps.reinit();
		}
		matcher=EMPTY_NULL.matcher(temp);
		while(matcher.find()){
			String mather=matcher.group();
			if(!temp.contains(mather)){
				continue;
			}
			String[] subGroups=RegexUtils.group2Array(matcher);
			String mark=subGroups[0];
			String property=subGroups[1];
			int start=temp.indexOf(mather);
			int end=temp.indexOf("</"+mark+">",start);
			if(end==-1){
				throw new TagEOFException(mather+" must has end!");
			}
			//整个逻辑字符串 tm
			String tm=temp.substring(start,end+("</"+mark+">").length());
			boolean flag=false;
			if(!tempMap.containsKey(mather)){
				//内容
				SMLParam sp=sqlParamMaps.getSmlParam(property);
				if(sp==null){
					throw new ParamNotConfigException(property+" is not config for "+mark);
				}
				flag=(mark.startsWith(isNotEmpty)&&SmlTools.isNotEmpty(sp.getValue()))||
					(mark.startsWith(isEmpty)&&SmlTools.isEmpty(sp.getValue()))||
					(mark.startsWith(isNull)&&sp.getValue()==null)||
					(mark.startsWith(isNotNull)&&sp.getValue()!=null);
				tempMap.put(mather,flag);
			}else{
				flag=tempMap.get(mather);
			}
			String content=RegexUtils.subString(tm,">",("</"+mark+">"));
			Assert.notRpeatMark(content,mark);
			if(flag){
				temp=temp.replace(tm, content);
			}else{
				temp=temp.replace(tm," ");
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
		matcher=IF.matcher(temp);
		while(matcher.find()){
			String mather=matcher.group();
			if(!temp.contains(mather)){
				continue;
			}
			String[] subGroups=RegexUtils.group2Array(matcher);
			String mark=subGroups[0];
			//String text=subGroups[1];
			String text=RegexUtils.subString(mather,"test=\"","\">").trim();
			int start=temp.indexOf(mather);
			int end=temp.indexOf("</"+mark+">",start);
			if(end==-1){
				throw new TagEOFException(mather+" must has end!");
			}
			String tm=temp.substring(start,end+("</"+mark+">").length());
			String content=RegexUtils.subString(tm,">",("</"+mark+">"));
			Assert.notRpeatMark(content,mark);
			//对text内容进行处理
			//参数报错直接跳出，减少对数据库的压力
			boolean flag=false;
			if(!tempMap.containsKey(mather)){
				try {
					flag = el.parser(text,mapParam);
				} catch (Exception e) {//
					throw new TagException("jsElP["+text+"]",e);
				}
				tempMap.put(mather,flag);
			}else{
				flag=tempMap.get(mather);
			}
			if(flag){
				temp=temp.replace(tm, content);
			}else{
				temp=temp.replace(tm," ");
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

	public static void main(String[] args) {
		Matcher matcher=IF.matcher("<if test=\" '@a'>12&&a=\"1\" \">helloworld</if><if test=\"'@a'=1& 1=1\">helloworld</if>");
		while(matcher.find()){
			String tmt=matcher.group();
			System.out.println(tmt);
			String[] subGroups=RegexUtils.group2Array(matcher);
			String mark=subGroups[0];
			String text=subGroups[1];
			System.out.println(Arrays.asList(subGroups));
			String text1=RegexUtils.subString(tmt,"test=\"","\">");
			System.out.println(Arrays.asList(mark,text,text1));
		}
	}
}
