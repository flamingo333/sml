package com.eastcom_sw.inas.core.service.jdbc.tools;

import java.util.Map;

import org.hw.sml.tools.DateTools;
import org.hw.sml.tools.MapUtils;



public class SqlFilterHelper {
	public static String metaFilterTableName="SYS_FM_FILTER";
	public static final String CONDITION_PRE="condition_";
	public static final Map<String,String> OPERATORS=MapUtils.newHashMap();
	static{//gt,ge,lt,le,like,notlike,in,eq,notin
		OPERATORS.put("gt",">");
		OPERATORS.put("ge",">=");
		OPERATORS.put("lt","<");
		OPERATORS.put("le","<=");
		OPERATORS.put("eq","=");
		OPERATORS.put("ne","!=");
		OPERATORS.put("like","like");
		OPERATORS.put("notlike","not like");
		OPERATORS.put("in","in");
		OPERATORS.put("notin","not in");
	}
	/**
	 * conditions_in_counts=1,3
	 * conditions_gt_alarm_time=20180512
	 * ...
	 * @param params
	 * @return
	 */
	public static String createConditionSql(Map<String,String> params){
		StringBuffer sb=new StringBuffer();
		for(Map.Entry<String,String> entry:params.entrySet()){
			String value=entry.getValue();
			if(value==null||value.length()==0||!entry.getKey().startsWith(CONDITION_PRE)) continue;
			String key=entry.getKey().replaceFirst(CONDITION_PRE,"");
			if(key.indexOf("_")==-1) continue;
			String type=key.substring(0,key.indexOf("_"));
			String[] fieldNames=key.replaceFirst(type+"_", "").split("@");
			String fieldName=fieldNames[0];
			String fieldType=fieldNames.length==2?fieldNames[1]:"char";
			String operator=OPERATORS.get(type);
			if(operator.contains("in")){
				String[] vs=value.split(",");
				sb.append(" and "+fieldName+" "+operator+" ("+buildInStrs(type,vs)+")");
			}else{
				if(operator.contains("like"))
					sb.append(" and "+fieldName+" "+operator+" '%"+value+"%'");
				else
					sb.append(" and "+fieldName+" "+operator+" "+buildV(fieldType, value)+"");
			}
			
		}
		return sb.toString();
	}
	public static String buildV(String type,String value){
		if(type.equals("date")){
			return "to_date('"+DateTools.getFormatTime(DateTools.parse(value),"yyyy-MM-dd HH:mm:ss")+"','yyyy-mm-dd hh24:mi:ss')";
		}else if(type.equals("number")){
			return value;
		}
		return "'"+value+"'";
	}
	public static String buildInStrs(String type,String[] vs){
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<vs.length;i++){
			sb.append("'"+vs[i]+"',");
		}
		return sb.deleteCharAt(sb.length()-1).toString();
	}
}
