package org.hw.sml.core.resolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hw.sml.core.resolver.Rst;
import org.hw.sml.support.el.El;
import org.hw.sml.tools.Assert;
import org.hw.sml.tools.ClassUtil;
import org.hw.sml.tools.MapUtils;

import com.eastcom_sw.inas.core.service.jdbc.SqlParam;
import com.eastcom_sw.inas.core.service.jdbc.SqlParams;
import com.eastcom_sw.inas.core.service.tools.RegexUtils;
/**
 * 
 * @author wen
 *
 */
public class ParamTypeResolver implements SqlResolver{

	public Rst resolve(String dialect, String temp,SqlParams sqlParamMaps) {
		List<String> mathers=null;
		//对数据进行
		if(temp.contains("<jdbcType")){
			mathers=RegexUtils.matchGroup("<jdbcType name=\"\\w+\" type=\"\\S+\"(/?)>",temp);
			for(String mather:mathers){
				String tmt=mather;
				if(!temp.contains(tmt)){
					continue;
				}
				
				if(tmt.endsWith("/>")){
					String name=RegexUtils.subString(tmt,"name=\"", "\" type=\"");
					String type=RegexUtils.subString(tmt,"type=\"","\"/>");
					SqlParam sp=sqlParamMaps.getSqlParam(name);
					if(sp!=null){
						sp.setType(type);
						if((sp.getValue()==null||sp.getValue().toString().length()==0))
						    sp.setValue(null);
						else{
							if(!sp.getValue().getClass().isArray()){
								sp.handlerValue(String.valueOf(sp.getValue()));
							}
						}
					}
					temp=temp.replace(tmt,"");
				}else{
					//取标签值
					String name=RegexUtils.subString(tmt, "name=\"", "\" type=\"");
					String type=RegexUtils.subString(tmt,"type=\"","\">");
					int start=temp.indexOf(tmt);
					int end=temp.indexOf("</jdbcType>",start);
					Assert.isTrue(end!=-1,mather+" must has end!");
					String tm=temp.substring(start,end+("</jdbcType>").length());
					String content=RegexUtils.subString(tm,">",("</jdbcType>"));
					SqlParam sp=sqlParamMaps.getSqlParam(name);
					if(sp!=null){
						sp.setType(type);
						if((sp.getValue()==null||sp.getValue().toString().length()==0))
						    sp.setValue(null);
						else{
							sp.handlerValue(String.valueOf(sp.getValue()));
						}
						Object value=sp.getValue();
						if(value!=null){
							if(value.getClass().isArray()){
								Object[] tos=(Object[])value;
								for(int i=0;i<tos.length;i++){
									tos[i]=JsEngine.evel(content.replace("@value",getValue(tos[i])));
								}
							}else{
								sp.setValue(JsEngine.evel(content.replace("@value",getValue(value))));
							}
						}
					}
					temp=temp.replace(tm,"");
				}
			}
		}
		if(temp.contains("<import")){
			mathers=RegexUtils.matchGroup("<import id=\"\\w+\" classpath=\"[\\w|.]+\"/>",temp);
			Map<String,Object> classpaths=MapUtils.newHashMap();
			for(String mather:mathers){
				String id=RegexUtils.subString(mather, "<import id=\"","\" classpath");
				String cp=RegexUtils.subString(mather, "classpath=\"","\"/>");
				Assert.isTrue(ClassUtil.hasClass(cp), "class "+cp+" not exists!");
				classpaths.put(id,ClassUtil.newInstance(cp));
				temp=temp.replace(mather,"");
			}
			mathers=RegexUtils.matchGroup("\\$\\{\\w+.\\w+\\([\\S|,]+\\)\\}", temp);
			for(String mather:mathers){
				String[] sss=RegexUtils.matchSubString("\\$\\{(.\\w+).(.*?)\\((.*?)\\)\\}",mather);
				Object bean=classpaths.get(sss[0]);
				Assert.notNull(bean,sss[0]+" is not import class!");
				Method m;
				try {
					m = bean.getClass().getMethod(sss[1],getClassPath(sss[2]));
					String result=(String) m.invoke(bean,sss[2].split(","));
					temp=temp.replace(mather,result);
				} catch (NoSuchMethodException e) {
					Assert.isTrue(false,sss[0]+"-"+sss[1]+" method not find!");
				} catch (InvocationTargetException e) {
					Assert.isTrue(false,"inner error "+e.getTargetException());
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return new Rst(temp);
	}
	private String getValue(Object v){
		if(v==null){
			return null;
		}else{
			if(v instanceof Date){
				return String.valueOf(((Date)v).getTime());
			}else{
				return String.valueOf(v);
			}
		}
	}
	private Class[] getClassPath(String sss){
		Class[] c=new Class[sss.split(",").length];
		for(int i=0;i<c.length;i++){
			c[i]=String.class;
		}
		return c;
	}
	@Override
	public void setEl(El el) {
		
	}
	public static void main(String[] args) {
		String v="201501010000";
		//<smlParam name="oldtime" value="ref:time"/>
		String jsElp="var v='201501010000';var dateV=parseDate(v);var dateLV= dateV.getTime()-24*60*60*1000;new Date(dateLV).format('yyyyMMddhhmmss')";
		System.out.println(JsEngine.evel(jsElp));
		
	}
}
