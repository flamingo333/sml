package org.hw.sml.core.build;

import java.util.Collection;
import java.util.Map;

import org.hw.sml.core.RebuildParam;
import org.hw.sml.model.SMLParam;
import org.hw.sml.model.SMLParams;
import org.hw.sml.model.SqlTemplate;
import org.hw.sml.tools.MapUtils;

public class SmlTools {
	public static SqlTemplate toSqlTemplate(String dbid,String sql,Map<String,String> params){
		SqlTemplate st=new SqlTemplate();
		st.setDbid(dbid);
		st.setMainSql(sql);
		st.setId("anon");
		SMLParams sp=new SMLParams();
		for(Map.Entry<String,String> entry:params.entrySet()){
			sp.add(entry.getKey(),entry.getValue());
		}
		st.setSmlParams(sp.reinit());
		return st;
	}
	public static SMLParams toSplParams(String sqlPStr){
		String split=",";
		SMLParams sqlParams=new SMLParams();
		String[] sps=sqlPStr.split("\n");
		int i=0;
		for(String sp:sps){
			if(i==0&&sp.endsWith("#")){//首行配置分隔符信息
				String tp=sp.replace("#","");
				if(!tp.equals("")){
					split=tp;
				}
				continue;
			}
			i++;
			SMLParam sqlParam=new SMLParam();
			if(sp.startsWith("#")){
				continue;//注解标签
			}
			String[] sfs=sp.split(split);
			if(sfs.length<1){
				continue;
			}
			if(sfs.length>=1){
				sqlParam.setName(sfs[0]);
			}
			if(sfs.length>=2){
				sqlParam.setType(sfs[1]);
			}
			if(sfs.length>=3){
				if(sfs[2]!=null&&sfs[2].trim().length()>0)
				sqlParam.setDefaultValue(sfs[2]);
			}
			if(sfs.length>=4){
				sqlParam.setDescr(sfs[3]);
			}
			if(sfs.length>=5&&sfs[4].matches("\\d+")){
				sqlParam.setEnabled(Integer.parseInt(sfs[4]));
			}
			sqlParams.getSmlParams().add(sqlParam);
		}
		sqlParams.reinit();
		return sqlParams;
	}
	
	public static RebuildParam toRebuildParam(String repaStr){
		RebuildParam rebuildParam=new RebuildParam();
		String[] sps=repaStr.split("\n");
		for(int i=0;i<sps.length;i++){
			String sp=sps[i].trim();
			if(sp.startsWith("#")){
				continue;
			}
			if(i==0){
				if(sp.matches("\\d+")){
					rebuildParam.setType(Integer.parseInt(sp));
				}else{
					rebuildParam.setClasspath(sp);
				}
			}else{
				String[] ss=sp.split("=");
				if(ss.length==1){
					rebuildParam.getExtMap().put(ss[0],null);
				}
				if(ss.length<2){
					continue;
				}
				String ss1=sp.substring(ss[0].length()+1);
				if(ss.length>=2){
					rebuildParam.getExtMap().put(ss[0],ss1);
				}
				if(ss[0].equals("oriFields")){
					rebuildParam.setOriFields(ss1.split(","));
				}else if(ss[0].equals("newFields")){
					rebuildParam.setNewFields(ss1.split(","));
				}else if(ss[0].equals("groupFields")){
					rebuildParam.setGroupFields(ss1.split(","));
				}else if(ss[0].equals("groupname")){
					rebuildParam.setGroupname(ss1);
				}else if(ss[0].equals("index")){
					rebuildParam.setIndex(ss1);
				}else if(ss[0].equals("topN")&&ss[0].matches("\\d+")){
					rebuildParam.setTopN(Integer.parseInt(ss1));
				}else if(ss[0].equals("orderName")){
					rebuildParam.setOrderName(ss1);
				}else if(ss[0].equals("orderType")){
					rebuildParam.setOrderType(ss1);
				}else if(ss[0].equals("filepath")){
					rebuildParam.setFilepath(ss1);
				}else{
					rebuildParam.getExtMap().put(ss[0],ss1);
				}
			}
		}
		return rebuildParam;
	}
	public static boolean isEmpty(Object obj){
		return obj==null||obj.toString().trim().length()==0;
	}
	public static boolean isJsonStr(String jsonStr){
		return !isEmpty(jsonStr)&&jsonStr.startsWith("{")&&jsonStr.endsWith("}");
	}
	public static Map<String,String> rebuildSimpleKv(Map<String,Object> param){
		return rebuildSimpleKv(null, param);
	}
	@SuppressWarnings("unchecked")
	private static Map<String,String> rebuildSimpleKv(String keyPre,Map<String,Object> param){
		Map<String,String> result=MapUtils.newHashMap();
		for(Map.Entry<String,Object> entry:param.entrySet()){
			String key=keyPre==null?entry.getKey():keyPre+"."+entry.getKey();
			Object value=entry.getValue();
			if(value==null){
				result.put(key,null);
			}else if(value instanceof Map){
				result.putAll(rebuildSimpleKv(key,(Map<String,Object>)value));
			}else if(value instanceof Collection){
				Collection<?> cls=(Collection<?>) value;
				StringBuffer sb=new StringBuffer();
				for(Object cl:cls){
					sb.append(cl).append(",");
				}
				result.put(key,sb.deleteCharAt(sb.length()-1).toString());
			}else if(value.getClass().isArray()){
				Object[] objs=(Object[]) value;
				StringBuffer sb=new StringBuffer();
				for(Object cl:objs){
					sb.append(cl).append(",");
				}
				result.put(key,sb.deleteCharAt(sb.length()-1).toString());
			}else{
				result.put(key,String.valueOf(value));
			}
		}
		return result;
	}
	
}
