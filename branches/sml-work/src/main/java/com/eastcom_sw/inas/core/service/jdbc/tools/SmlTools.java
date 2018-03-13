package com.eastcom_sw.inas.core.service.jdbc.tools;

import java.util.Map;

import org.hw.sml.core.resolver.JsEngine;

import com.eastcom_sw.inas.core.service.jdbc.RebuildParam;
import com.eastcom_sw.inas.core.service.jdbc.SqlParam;
import com.eastcom_sw.inas.core.service.jdbc.SqlParams;
import com.eastcom_sw.inas.core.service.jdbc.SqlTemplate;

public class SmlTools {
	public static SqlParams toSplParams(String sqlPStr){
		String split=",";
		SqlParams sqlParams=new SqlParams();
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
			SqlParam sqlParam=new SqlParam();
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
			sqlParams.getSqlParams().add(sqlParam);
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
	public static String resolveJsEpl(String elp){
		//js{}
		String result="";
		result =JsEngine.evel("new date().getday()").toString();
		return result;
	}
	public static SqlTemplate toSqlTemplate(String dbid,String sql,Map<String,String> params){
		SqlTemplate st=new SqlTemplate();
		st.setDbid(dbid);
		st.setMainSql(sql);
		SqlParams sp=new SqlParams();
		for(Map.Entry<String,String> entry:params.entrySet()){
			sp.add(entry.getKey(),entry.getValue());
		}
		st.setSqlParamMap(sp.reinit());
		return st;
	}
}
