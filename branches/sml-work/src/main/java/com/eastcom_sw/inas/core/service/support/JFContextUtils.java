package com.eastcom_sw.inas.core.service.support;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hw.sml.FrameworkConstant;
import org.hw.sml.core.resolver.SqlResolver;
import org.hw.sml.jdbc.JdbcTemplate;
import org.hw.sml.support.cache.CacheManager;
import org.hw.sml.support.el.El;
import org.hw.sml.tools.Https;

import com.eastcom_sw.inas.core.service.jdbc.AbstractJdbcFTemplate;
import com.eastcom_sw.inas.core.service.jdbc.JsonMapper;
import com.eastcom_sw.inas.core.service.jdbc.SqlParam;
import com.eastcom_sw.inas.core.service.jdbc.SqlParams;
import com.eastcom_sw.inas.core.service.jdbc.SqlTemplate;
import com.eastcom_sw.inas.core.service.jdbc.build.Rslt;
import com.eastcom_sw.inas.core.service.jdbc.resolver.SqlResolvers;
import com.eastcom_sw.inas.core.service.tools.MapUtils;
/**
 * 
 * @author wen
 * 
 *增加  --a=1 --b=2 --c=a+2=2 等字符串查询方式
 */
public class JFContextUtils {
	
	private  AbstractJdbcFTemplate jdbcFTemplate;	
	public  JFContextUtils(AbstractJdbcFTemplate jdbcFtemplate){
		jdbcFTemplate=jdbcFtemplate;
	}
	
	public  CacheManager getCacheManager(){
		return jdbcFTemplate.getCacheManager();
	}
	
	public  JdbcTemplate getJdbc(String dbid){
		return jdbcFTemplate.getJdbc(dbid);
	}
	public  JsonMapper getJsonMapper(){
		return jdbcFTemplate.getJsonMapper();
	}
	public  El getEl(){
		return jdbcFTemplate.getEl();
	}
	public  AbstractJdbcFTemplate getJdbcFTemplate(){
		return jdbcFTemplate;
	}
	public  SqlResolvers getSqlResolvers(){
		return jdbcFTemplate.getSqlResolvers();
	}
	public  void registSqlReolvers(SqlResolver sqlResolver){
		getSqlResolvers().getExtResolvers().add(sqlResolver);
		getSqlResolvers().init();
	}
	//
	@SuppressWarnings("unchecked")
	public  <T> T query(Map<String,String> params){
		return (T)query(params.get("ifId"),params);
	}
	@SuppressWarnings("unchecked")
	public  <T> T query(String ifId,Map<String,String> params){
		SqlTemplate st=jdbcFTemplate.getSqlTemplate(ifId);
		return (T)query(st,params);
	}
	@SuppressWarnings("unchecked")
	public <T> T query(String ifId,String paramsStr){
		if(paramsStr.trim().startsWith("{")&&paramsStr.trim().endsWith("}"))
			return (T)query(ifId,jdbcFTemplate.getJsonMapper().toObj(paramsStr,Map.class));
		return (T)query(ifId,MapUtils.transMapFromStr(paramsStr));
	}
	@SuppressWarnings("unchecked")
	public <T> T query(String paramsStr){
		if(paramsStr.trim().startsWith("{")&&paramsStr.trim().endsWith("}"))
			return (T)query(jdbcFTemplate.getJsonMapper().toObj(paramsStr,Map.class));
		return (T)query(MapUtils.transMapFromStr(paramsStr));
	}
	@SuppressWarnings("unchecked")
	public  <T> T query(SqlTemplate st,Map<String,String> params){
		if(Boolean.valueOf(params.get(FrameworkConstant.PARAM_FLUSHCACHE))){
			clear(st.getId());
		}
		reInitSqlParam(st, getJdbc(st.getDbid()), params);
		return (T)jdbcFTemplate.builder(st);
	}
	public  Rslt queryRslt(SqlTemplate st,Map<String,String> params){
		reInitSqlParam(st, getJdbc(st.getDbid()), params);
		return jdbcFTemplate.queryRslt(st);
	}
	public  Rslt queryRslt(String ifId,Map<String,String> params){
		SqlTemplate st=jdbcFTemplate.getSqlTemplate(ifId);
		reInitSqlParam(st, getJdbc(st.getDbid()), params);
		return jdbcFTemplate.queryRslt(st);
	}
	public int update(String ifId,Map<String,String> params){
		if(Boolean.valueOf(params.get(FrameworkConstant.PARAM_FLUSHCACHE))){
			clear(ifId);
		}
		SqlTemplate st=jdbcFTemplate.getSqlTemplate(ifId);
		reInitSqlParam(st, getJdbc(st.getDbid()), params);
		return jdbcFTemplate.update(st);
	}
	public int update(Map<String,String> params){
		return update(params.get("ifId"), params);
	}
	@SuppressWarnings("unchecked")
	public int update(String paramsStr){
		if(paramsStr.trim().startsWith("{")&&paramsStr.trim().endsWith("}"))
			return update(jdbcFTemplate.getJsonMapper().toObj(paramsStr,Map.class));
		return update(MapUtils.transMapFromStr(paramsStr));
	}
	
	
	public  int clear(String keyStart){
		if(!isNotBlank(keyStart))
			return getCacheManager().clearKeyStart(AbstractJdbcFTemplate.CACHE_PRE);
		return getCacheManager().clearKeyStart(AbstractJdbcFTemplate.CACHE_PRE+":"+keyStart+":");
	}
	public  int getCacheSize(String keyStart){
		if(!isNotBlank(keyStart))
			return getCacheManager().getKeyStart(AbstractJdbcFTemplate.CACHE_PRE+":").size();
		return getCacheManager().getKeyStart(AbstractJdbcFTemplate.CACHE_PRE+":"+keyStart+":").size();
	}
	
	
	public static void reInitSqlParam(SqlTemplate st, JdbcTemplate jdbc,
			Map<String, String> params) {
		boolean isRpt=MapUtils.getString(params,FrameworkConstant.PARAM_ISREMOTEPRAMS,"false").equalsIgnoreCase("true");
		if(isRpt){
			SqlParams smlParams=new SqlParams();
			for(Map.Entry<String,String> entry:params.entrySet()){
				String pn=entry.getKey();
				if(pn.endsWith("-type")){
					continue;
				}
				String[] pns=pn.split("@");
				if(pns.length==1){
					smlParams.add(pns[0],entry.getValue(),MapUtils.getString(params,pns[0]+"-type","char"));
				}else{
					smlParams.add(pns[0],entry.getValue(),pns[1]);
				}
			}
			st.setSqlParamMap(smlParams);
			smlParams.reinit();
		}else{
			List<SqlParam> lst=st.getSqlParamMap().getSqlParams();
			for(SqlParam sp:lst){
				String name=sp.getName();
				String value=params.get(name);
				if(isNotBlank(value)){
					sp.handlerValue(value);
				}else{
					sp.handlerDefaultValue(jdbc);
				}
			}
		}
	}
	public void registDataSource(String dbid,DataSource dataSource){
		jdbcFTemplate.getDss().put(dbid,dataSource);
		jdbcFTemplate.getJts().put(dbid,jdbcFTemplate.newJdbcTemplate(dataSource));
	}
	public Rslt queryRslt(String dbid,String sql,Map<String,String> params){
		return jdbcFTemplate.queryRslt(dbid, sql, params);
	}
	public  List<Map<String,Object>> query(String dbid,String sql,Map<String,String> params){
		return jdbcFTemplate.querySql(dbid, sql, params);
	}
	public static boolean isNotBlank(Object val) {
		return val != null && String.valueOf(val).trim().length() > 0;
	}
	public static String queryFromUrl(String contentType,String accept,String url,byte[] requestBody) throws IOException{
		Https https=Https.newPostHttps(url);
		return https.head(https.getHeader().put("Content-Type",contentType).put("Accept",accept)).body(requestBody).execute();
	}
	public static String queryFromUrl(String url,byte[] requestBody) throws IOException{
		return queryFromUrl("application/json;charset=UTF-8", "application/json;charset=UTF-8", url, requestBody);
	}
	public static String queryFromUrl(String url,String requestBody) throws IOException{
		Charset charset=Charset.forName("utf-8");
		return new String(queryFromUrl(url, requestBody.getBytes(charset)).getBytes());
	}
}
