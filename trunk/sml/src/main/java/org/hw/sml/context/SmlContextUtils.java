package org.hw.sml.context;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hw.sml.FrameworkConstant;
import org.hw.sml.core.Rslt;
import org.hw.sml.core.SqlMarkupAbstractTemplate;
import org.hw.sml.core.resolver.Rst;
import org.hw.sml.core.resolver.SqlResolver;
import org.hw.sml.core.resolver.SqlResolvers;
import org.hw.sml.jdbc.JdbcTemplate;
import org.hw.sml.model.SMLParam;
import org.hw.sml.model.SMLParams;
import org.hw.sml.model.SqlTemplate;
import org.hw.sml.queryplugin.JsonMapper;
import org.hw.sml.support.cache.CacheManager;
import org.hw.sml.support.el.El;
import org.hw.sml.tools.Https;
import org.hw.sml.tools.MapUtils;



public class SmlContextUtils {
	
	private  SqlMarkupAbstractTemplate sqlMarkupAbstractTemplate;	
	public  SmlContextUtils(SqlMarkupAbstractTemplate sqlMarkupAbstractTemplate){
		this.sqlMarkupAbstractTemplate=sqlMarkupAbstractTemplate;
	}
	
	public  CacheManager getCacheManager(){
		return sqlMarkupAbstractTemplate.getCacheManager();
	}
	
	public  JdbcTemplate getJdbc(String dbid){
		return sqlMarkupAbstractTemplate.getJdbc(dbid);
	}
	public  JsonMapper getJsonMapper(){
		return sqlMarkupAbstractTemplate.getJsonMapper();
	}
	public  El getEl(){
		return sqlMarkupAbstractTemplate.getEl();
	}
	public  SqlMarkupAbstractTemplate getSqlMarkupAbstractTemplate(){
		return sqlMarkupAbstractTemplate;
	}
	public  SqlResolvers getSqlResolvers(){
		return sqlMarkupAbstractTemplate.getSqlResolvers();
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
		SqlTemplate st=sqlMarkupAbstractTemplate.getSqlTemplate(ifId);
		return (T)query(st,params);
	}
	public  List<Map<String,Object>> query(String dbid,String sql,Map<String,String> params){
		return sqlMarkupAbstractTemplate.querySql(dbid, sql, params);
	}
	@SuppressWarnings("unchecked")
	public <T> T query(String ifId,String paramsStr){
		if(paramsStr.trim().startsWith("{")&&paramsStr.trim().endsWith("}"))
			return (T)query(ifId,sqlMarkupAbstractTemplate.getJsonMapper().toObj(paramsStr,Map.class));
		return (T)query(ifId,MapUtils.transMapFromStr(paramsStr));
	}
	@SuppressWarnings("unchecked")
	public <T> T query(String paramsStr){
		if(paramsStr.trim().startsWith("{")&&paramsStr.trim().endsWith("}"))
			return (T)query(sqlMarkupAbstractTemplate.getJsonMapper().toObj(paramsStr,Map.class));
		return (T)query(MapUtils.transMapFromStr(paramsStr));
	}
	@SuppressWarnings("unchecked")
	public  <T> T query(SqlTemplate st,Map<String,String> params){
		if(Boolean.valueOf(params.get(FrameworkConstant.PARAM_FLUSHCACHE))){
			clear(st.getId());
		}
		reInitSqlParam(st, getJdbc(st.getDbid()), params);
		return (T)sqlMarkupAbstractTemplate.builder(st);
	}
	public  Rslt queryRslt(SqlTemplate st,Map<String,String> params){
		reInitSqlParam(st, getJdbc(st.getDbid()), params);
		return sqlMarkupAbstractTemplate.queryRslt(st);
	}
	public  Rslt queryRslt(String ifId,Map<String,String> params){
		SqlTemplate st=sqlMarkupAbstractTemplate.getSqlTemplate(ifId);
		reInitSqlParam(st, getJdbc(st.getDbid()), params);
		return sqlMarkupAbstractTemplate.queryRslt(st);
	}
	public Rslt queryRslt(String dbid,String sql,Map<String,String> params){
		return sqlMarkupAbstractTemplate.queryRslt(dbid, sql, params);
	}
	public Rst queryRst(String ifId,Map<String,String> params){
		if(params==null){params=new HashMap<String,String>();}
		params.put(FrameworkConstant.PARAM_SHOWSQL,"true");
		SqlTemplate st=sqlMarkupAbstractTemplate.getSqlTemplate(ifId);
		reInitSqlParam(st, getJdbc(st.getDbid()), params);
		return sqlMarkupAbstractTemplate.getSqlResolvers().resolverLinks(st.getMainSql(), st.getSmlParams());
	}
	public int update(String ifId,Map<String,String> params){
		if(Boolean.valueOf(params.get(FrameworkConstant.PARAM_FLUSHCACHE))){
			clear(ifId);
			
		}
		SqlTemplate st=sqlMarkupAbstractTemplate.getSqlTemplate(ifId);
		reInitSqlParam(st, getJdbc(st.getDbid()), params);
		return sqlMarkupAbstractTemplate.update(st);
	}
	public int update(Map<String,String> params){
		return update(params.get("ifId"), params);
	}
	@SuppressWarnings("unchecked")
	public int update(String paramsStr){
		if(paramsStr.trim().startsWith("{")&&paramsStr.trim().endsWith("}"))
			return update(sqlMarkupAbstractTemplate.getJsonMapper().toObj(paramsStr,Map.class));
		return update(MapUtils.transMapFromStr(paramsStr));
	}
	public void registDataSource(String dbid,DataSource dataSource){
		sqlMarkupAbstractTemplate.getDss().put(dbid,dataSource);
		sqlMarkupAbstractTemplate.getJts().put(dbid,sqlMarkupAbstractTemplate.newJdbcTemplate(dataSource));
	}
	public  int clear(String keyStart){
		if(!isNotBlank(keyStart))
			return getCacheManager().clearKeyStart(SqlMarkupAbstractTemplate.CACHE_PRE);
		return getCacheManager().clearKeyStart(SqlMarkupAbstractTemplate.CACHE_PRE+":"+keyStart);
	}
	public  int getCacheSize(String keyStart){
		if(!isNotBlank(keyStart))
			return getCacheManager().getKeyStart(SqlMarkupAbstractTemplate.CACHE_PRE+":").size();
		return getCacheManager().getKeyStart(SqlMarkupAbstractTemplate.CACHE_PRE+":"+keyStart+":").size();
	}
	
	
	public static void reInitSqlParam(SqlTemplate st, JdbcTemplate jdbc,
			Map<String, String> params) {
		boolean isRpt=MapUtils.getString(params,FrameworkConstant.PARAM_ISREMOTEPRAMS,"false").equalsIgnoreCase("true");
		if(isRpt){
			SMLParams smlParams=new SMLParams();
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
			st.setSmlParams(smlParams);
			smlParams.reinit();
		}else{
			List<SMLParam> lst=st.getSmlParams().getSmlParams();
			for(SMLParam sp:lst){
				String name=sp.getName();
				String value=params.get(name);
				if(isNotBlank(value)){
					sp.handlerValue(value);
				}else{
					boolean showsql=Boolean.valueOf(params.get(FrameworkConstant.PARAM_SHOWSQL));
					sp.handlerValue(sp.getDefaultValue()==null?(showsql?MapUtils.get(params,FrameworkConstant.PARAM_SHOWSQL_DEFAULTV,"1"):null):sp.getDefaultValue());
				}
			}
			st.getSmlParams().reinit();
		}
	}
	
	
	public static boolean isNotBlank(Object val) {
		return val != null && String.valueOf(val).trim().length() > 0;
	}
	
	//--ext
	public static String queryFromUrl(String contentType,String accept,String url,byte[] requestBody) throws IOException{
		Https https=Https.newPostHttps(url);
		return https.head(https.getHeader().put("Content-Type",contentType).put("Accept",accept)).body(requestBody).execute();
	}
	public static String queryFromUrl(String url,byte[] requestBody) throws IOException{
		return queryFromUrl("application/json; charset=UTF-8", "application/json; charset=UTF-8", url, requestBody);
	}
	public static String queryFromUrl(String url,String requestBody) throws IOException{
		Charset charset=Charset.forName("utf-8");
		return new String(queryFromUrl(url, requestBody.getBytes(charset)).getBytes());
	}
}
