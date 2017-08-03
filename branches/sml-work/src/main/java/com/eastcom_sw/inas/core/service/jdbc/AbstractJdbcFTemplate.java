package com.eastcom_sw.inas.core.service.jdbc;

import java.util.List;
import java.util.Map;

import org.hw.sml.FrameworkConstant;
import org.hw.sml.core.resolver.Rst;
import org.hw.sml.support.LoggerHelper;
import org.hw.sml.support.SmlAppContextUtils;
import org.hw.sml.support.Source;
import org.hw.sml.support.el.El;
import org.hw.sml.support.el.JsEl;
import org.hw.sml.support.el.Links;
import org.hw.sml.tools.MapUtils;

import com.eastcom_sw.inas.core.service.jdbc.build.DataBuilderHelper;
import com.eastcom_sw.inas.core.service.jdbc.build.Rslt;
import com.eastcom_sw.inas.core.service.jdbc.resolver.SqlResolvers;
import com.eastcom_sw.inas.core.service.jdbc.tools.SmlTools;
import com.eastcom_sw.inas.core.service.support.JFContextUtils;
import com.eastcom_sw.inas.core.service.tools.Assert;

public abstract class AbstractJdbcFTemplate extends Source implements IJdbcFTemplate {
	/**
	 *日志开关
	 */
	protected boolean isLogger=true;
	
	protected JsonMapper jsonMapper;
	
	protected int cacheMinutes;
	
	protected El el;
	
	protected SqlResolvers sqlResolvers;

	protected JFContextUtils jfContextUtils;
	
	public void init(){
		super.init();
		this.jfContextUtils=new JFContextUtils(this);
		SmlAppContextUtils.put(frameworkMark, this);
		if(this.jsonMapper==null){
			LoggerHelper.warn(getClass(),"not dependency json mapper, can't used json config!");
		}
		if(el==null){
			el=new JsEl();
		}
		if(this.sqlResolvers==null){
			SqlResolvers sqlResolvers=new SqlResolvers(getEl());
			sqlResolvers.init();
			this.sqlResolvers=sqlResolvers;
			LoggerHelper.info(getClass(),"sqlResolvers start... has resolvers ["+(this.sqlResolvers.getSqlResolvers().size())+"]");
		}
		if(this.cacheManager==null){
			super.cacheManager=getCacheManager();
		}
	}
	
	public List<Map<String,Object>> querySql(String dbid,String sql,Map<String,String> params){
		return mergeSql(SmlTools.toSqlTemplate(dbid, sql, params));
	}
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> mergeSql(SqlTemplate st){
		SqlResolvers sqlResolvers=getSqlResolvers();
		long parserStart=System.currentTimeMillis();
		Rst rst=sqlResolvers.resolverLinks(st.getMainSql(), st.getSqlParamMap());
		long parseEnd=System.currentTimeMillis();
		List<Object> paramsObject=rst.getParamObjects();
		String key=CACHE_PRE+":"+st.getId()+":mergeSql"+rst.getSqlString()+paramsObject.toString();
		if(getCacheManager().get(key)!=null){
			return (List<Map<String,Object>>) getCacheManager().get(key);
		}
		if(isLogger&&(st.getSqlParamMap().getSqlParam("igLog")==null||st.getSqlParamMap().getSqlParam(FrameworkConstant.PARAM_IGLOG).getValue().equals("false")))
			LoggerHelper.info(getClass(),"ifId["+st.getId()+"]-sql["+rst.getPrettySqlString()+"],sqlParseUseTime["+(parseEnd-parserStart)+"ms]");
		Assert.isTrue(rst.getSqlString()!=null&&rst.getSqlString().length()>0, "querySql config error parser is null");
		List<Map<String,Object>> result= getJdbc(st.getDbid()).queryForList(rst.getSqlString(),paramsObject.toArray(new Object[]{}));
		if(st.getIsCache()==1)
		getCacheManager().set(key, result, st.getCacheMinutes());
		return result;
	}
	public int update(SqlTemplate st){
		int result=0;
		SqlResolvers sqlResolvers=getSqlResolvers();
		boolean isLinks=st.getSqlParamMap().getMapParams().keySet().contains(FrameworkConstant.PARAM_OPLINKS);
		if(!isLinks){
			Rst rst=sqlResolvers.resolverLinks(st.getMainSql(), st.getSqlParamMap());
			List<Object> paramsObject=rst.getParamObjects();
			LoggerHelper.info(getClass(),"ifId["+st.getId()+"]-sql["+rst.getSqlString()+"],params"+paramsObject.toString()+"]");
			result=getJdbc(st.getDbid()).update(rst.getSqlString(),paramsObject.toArray(new Object[]{}));
		}else{
			//links oparator
			String[] links=new Links(st.getSqlParamMap().getSqlParamFromList(FrameworkConstant.PARAM_OPLINKS).getValue().toString()).parseLinks().getOpLinks();
			List<String> linkSqls=MapUtils.newArrayList();
			List<Object[]> linkParams=MapUtils.newArrayList();
			for(String link:links){
				st.getSqlParamMap().getSqlParam(FrameworkConstant.PARAM_OPLINKS).setValue(link);
				Rst rst=sqlResolvers.resolverLinks(st.getMainSql(), st.getSqlParamMap());
				LoggerHelper.info(getClass(),"ifId["+st.getId()+"]-links["+link+"]-sql["+rst.getSqlString()+"],params"+rst.getParamObjects().toString()+"]");
				linkSqls.add(rst.getSqlString());
				linkParams.add(rst.getParamObjects().toArray(new Object[]{}));
			}
			result=getJdbc(st.getDbid()).update(linkSqls,linkParams);
		}
		return result;
	}
	protected void reInitSqlTemplate(SqlTemplate st){
			if(st.getJsonConditionMap()!=null){
				//以json格式返回
				if(st.getJsonConditionMap().startsWith("{")&&st.getJsonConditionMap().endsWith("}")){
					if(jsonMapper!=null){
						st.setSqlParamMap(jsonMapper.toObj(st.getJsonConditionMap(),SqlParams.class));
					}
				}else{
					st.setSqlParamMap(SmlTools.toSplParams(st.getJsonConditionMap()));
				}
			}
			if(st.getJsonRebuildParamMap()!=null){
				if(st.getJsonRebuildParamMap().startsWith("{")&&st.getJsonRebuildParamMap().endsWith("}")){
					if(jsonMapper!=null){
						st.setRebuildParam(jsonMapper.toObj(st.getJsonRebuildParamMap(),RebuildParam.class));
					}
				}else{
					st.setRebuildParam(SmlTools.toRebuildParam(st.getJsonRebuildParamMap()));
				}
			}
			for(SqlParam sp:st.getSqlParamMap().getSqlParams()){
				sp.setJdbcTemplate(getJdbc(st.getDbid()));
			}
	}
	public Object builder(SqlTemplate sqlTemplate){
		return DataBuilderHelper.build(sqlTemplate.getRebuildParam(),mergeSql(sqlTemplate),jfContextUtils,sqlTemplate);
	}
	
	public Rslt queryRslt(SqlTemplate st){
		SqlResolvers sqlResolvers=getSqlResolvers();
		Rst rst=sqlResolvers.resolverLinks(st.getMainSql(), st.getSqlParamMap());
		String sqlString=rst.getSqlString();
		List<Object> paramsObject=rst.getParamObjects();
		if(isLogger&&(st.getSqlParamMap().getSqlParam("igLog")==null||st.getSqlParamMap().getSqlParam("igLog").getValue().equals("false")))
		LoggerHelper.info(getClass(),"sql["+rst.getPrettySqlString()+"]");
		return getJdbc(st.getDbid()).query(sqlString,paramsObject.toArray(new Object[]{}), new Rset());
	}
	public Rslt queryRslt(String dbid,String sql,Map<String,String> params){
		return queryRslt(SmlTools.toSqlTemplate(dbid, sql, params));
	}

	


	public int getCacheMinutes() {
		return cacheMinutes;
	}


	public void setCacheMinutes(int cacheMinutes) {
		this.cacheMinutes = cacheMinutes;
	}

	public JsonMapper getJsonMapper() {
		return jsonMapper;
	}

	public void setJsonMapper(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	public El getEl() {
		return el;
	}

	public void setEl(El el) {
		this.el = el;
	}

	
	public SqlResolvers getSqlResolvers() {
		return sqlResolvers;
	}
	public void setSqlResolvers(SqlResolvers sqlResolvers) {
		this.sqlResolvers = sqlResolvers;
	}
	public JFContextUtils getJfContextUtils() {
		return jfContextUtils;
	}
	public void setJfContextUtils(JFContextUtils jfContextUtils) {
		this.jfContextUtils = jfContextUtils;
	}

	public boolean getIsLogger() {
		return isLogger;
	}

	public void setIsLogger(boolean isLogger) {
		this.isLogger = isLogger;
	}
	
	

}
