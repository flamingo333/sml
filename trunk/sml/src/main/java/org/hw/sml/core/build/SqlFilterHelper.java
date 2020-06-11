package org.hw.sml.core.build;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hw.sml.core.resolver.Rst;
import org.hw.sml.model.DbType;
import org.hw.sml.tools.DateTools;
import org.hw.sml.tools.DbTools;
import org.hw.sml.tools.MapUtils;
import org.hw.sml.tools.Maps;



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
		OPERATORS.put("ilike","ilike");
		OPERATORS.put("notlike","not like");
		OPERATORS.put("notilike","not ilike");
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
	public static Rst createConditionSqlReturnRst(Map<String,String> params,DbType dbType){
		Rst rst=new Rst();
		rst.setDbtype(dbType);
		List<Object> paramObjects=MapUtils.newArrayList();
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
					sb.append(" and "+fieldName+" "+operator+" ("+buildInStrsPre(type,vs)+")");
					paramObjects.addAll(getObjects(fieldType, vs));
				}else{
					if(operator.contains("like")){
						if(operator.contains("ilike")){
							operator=operator.replace("ilike","like");
							value=value.toLowerCase();
							sb.append(" and lower("+fieldName+") "+operator);
						}else{
							sb.append(" and "+fieldName+" "+operator);
						}
						sb.append(" "+buildLikeValuePre(dbType));
						paramObjects.add(value);
					}else{
						sb.append(" and "+fieldName+" "+operator+"?");
						paramObjects.addAll(getObjects(fieldType,new String[]{value}));
					}
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
					sb.append((i==0?"":" or ")+value.get("fieldName")+" "+operator+" ("+buildInStrsPre(value.get("type"),vs)+")");
					paramObjects.addAll(getObjects(value.get("type"), vs));
				}else{
					String vtype=value.get("fieldType");
					String vt=value.get("value");
					if(operator.contains("like")){
						if(operator.contains("ilike")){
							operator=operator.replace("ilike","like");
							vt=vt.toLowerCase();
							sb.append((i==0?"":" or lower(")+value.get("fieldName")+") "+operator+" ");
						}else{
							sb.append((i==0?"":" or ")+value.get("fieldName")+" "+operator+" ");
						}
						sb.append(buildLikeValuePre(dbType));
						paramObjects.add(vt);
					}else{
						sb.append((i==0?"":" or ")+value.get("fieldName")+" "+operator+" ?"
						+" ");
						paramObjects.addAll(getObjects(vtype,new String[]{vt}));
					}
				}
				i++;
			}
			sb.append(") ");
		}
		String sql= sb.toString();
		rst.setSqlString(sql);
		rst.setParamObjects(paramObjects);
		return rst;
	}
	public static String buildLikeValuePre(DbType dbType){
		if(dbType.equals(DbType.oracle)){
			return "'%'||?||'%'";
		}else{
			return "concat('%',?,'%')";
		}
	}
	public static String createConditionSql(Map<String,String> params,DbType dbType){
		return createConditionSqlReturnRst(params, dbType).getPrettySqlString();
	}
	public static String createConditionSql(Map<String,String> params){
		return createConditionSql(params,DbType.oracle);
	}
	public static String buildV(DbType dbType,String type,String value){
		if(type.equals("date")){
			return DbTools.getDateFormat(DateTools.parse(value), dbType);
		}else if(type.equals("number")){
			return value;
		}
		return "'"+value+"'";
	}
	public static String buildInStrsPre(String type,String[] vs){
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<vs.length;i++){
			sb.append("?,");
		}
		return sb.deleteCharAt(sb.length()-1).toString();
	}
	public static String buildInStrs(String type,String[] vs){
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<vs.length;i++){
			sb.append("'"+vs[i]+"',");
		}
		return sb.deleteCharAt(sb.length()-1).toString();
	}
	public static List<Object> getObjects(String type,String[] vs){
		List<Object> objects=MapUtils.newArrayList();
		for(int i=0;i<vs.length;i++){
			if(type.equalsIgnoreCase("date")){
				objects.add(DateTools.parse(vs[i]));
			}else{
				objects.add(vs[i]);
			}
		}
		return objects;
	}
	public static void main(String[] args) {
		Map<String,String> params=MapUtils.newHashMap();
		Map<String,String> params2=MapUtils.newHashMap();
		params.put("condition_eq_f1@date","20190809");
		params.put("condition_eq_f2","2");
		params.put("condition_or1eq_f3","4");
		params.put("condition_or1notin_f4","5");
		params.put("condition_or2ilike_f3","A");
		params.put("condition_or2like_f4","5");
		params.put("condition_or2eq_f5@date","2017-09-23");
		System.out.println(createConditionSqlReturnRst(params,DbType.oracle).getPrettySqlString());
	}
}
