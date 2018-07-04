package com.eastcom_sw.inas.core.service.jdbc.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hw.sml.tools.DateTools;
import org.hw.sml.tools.MapUtils;

import com.eastcom_sw.inas.core.service.tools.Maps;



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
		Map<String,List<Map<String,String>>> orConditions=MapUtils.newHashMap();
		for(Map.Entry<String,String> entry:params.entrySet()){
			String value=entry.getValue();
			if(value==null||value.length()==0||!entry.getKey().startsWith(CONDITION_PRE)) continue;
			String key=entry.getKey().replaceFirst(CONDITION_PRE,"");
			if(key.indexOf("_")==-1) continue;
			String type=key.substring(0,key.indexOf("_"));
			String[] fieldNames=key.replaceFirst(type+"_", "").split("@");
			String fieldName=fieldNames[0];
			String fieldType=fieldNames.length==2?fieldNames[1]:"char";
			if(type.startsWith("or")){
				String group=type.substring(2,3);
				String realType=type.substring(3);
				if(!orConditions.containsKey(group)){
					orConditions.put(group,new ArrayList<Map<String,String>>());
				}
				orConditions.get(group).add(new Maps<String,String>().put("type",realType).put("value",value).put("fieldType",fieldType).put("fieldName",fieldName).getMap());
			}else{
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
		}
		for(Map.Entry<String,List<Map<String,String>>> entry:orConditions.entrySet()){
			List<Map<String,String>> values=entry.getValue();
			int i=0;
			sb.append(" and (");
			for(Map<String,String> value:values){
				String operator=OPERATORS.get(value.get("type"));
				if(operator.contains("in")){
					String[] vs=value.get("value").split(",");
					sb.append((i==0?"":" or ")+value.get("fieldName")+" "+operator+" ("+buildInStrs(value.get("type"),vs)+")");
				}else{
					if(operator.contains("like"))
						sb.append((i==0?"":" or ")+value.get("fieldName")+" "+operator+" '%"+value.get("value")+"%'");
					else
						sb.append((i==0?"":" or ")+value.get("fieldName")+" "+operator+" "+buildV(value.get("fieldType"), value.get("value")
						)+"");
				}
				i++;
			}
			sb.append(") ");
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
	public static void main(String[] args) {
		Map<String,String> params=MapUtils.newHashMap();
		params.put("condition_eq_f1","1");
		params.put("condition_eq_f2","2");
		//params.put("condition_or1eq_f3","4");
		//params.put("condition_or1notin_f4","5");
		//params.put("condition_or2like_f3","4");
		//params.put("condition_or2like_f4","5");
		System.out.println(createConditionSql(params));
	}
}
