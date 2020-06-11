package org.hw.sml.core;

import java.util.List;
import java.util.Map;

import org.hw.sml.FrameworkConstant;
import org.hw.sml.context.SmlContextUtils;
import org.hw.sml.core.build.DataBuilderHelper;
import org.hw.sml.core.build.SmlTools;
import org.hw.sml.core.resolver.CollectionHandler;
import org.hw.sml.core.resolver.Rst;
import org.hw.sml.core.resolver.SqlResolvers;
import org.hw.sml.model.SMLParams;
import org.hw.sml.model.SqlTemplate;
import org.hw.sml.queryplugin.JsonMapper;
import org.hw.sml.queryplugin.SqlMarkup;
import org.hw.sml.support.SmlAppContextUtils;
import org.hw.sml.support.Source;
import org.hw.sml.support.el.El;
import org.hw.sml.support.el.JsEl;
import org.hw.sml.support.el.Links;
import org.hw.sml.support.sentinel.DefaultSentinel;
import org.hw.sml.support.sentinel.Sentinel;
import org.hw.sml.tools.Assert;
import org.hw.sml.tools.ClassUtil;
import org.hw.sml.tools.MapUtils;

public abstract class SqlMarkupAbstractTemplate extends Source implements SqlMarkup {
	/**
	 *日志开关
	 */
	protected boolean isLogger=true;
	
	protected JsonMapper jsonMapper;
	
	protected int cacheMinutes;
	
	protected Sentinel sentinel;
	
	protected boolean openAllSentinel=true;
	
	protected El el;
	
	protected SqlResolvers sqlResolvers;
	public final static String CACHE_DATA=CACHE_PRE+":%s:mergeSql:"; 

	
	public void init(){
		super.init();
		SmlAppContextUtils.put(frameworkMark,this);
		if(this.jsonMapper==null){
			logger.warn(getClass(),"not dependency json mapper, can't use json config!");
		}
		if(el==null){
			el=new JsEl();
		}
		if(this.sqlResolvers==null){
			SqlResolvers sqlResolvers=new SqlResolvers(getEl());
			sqlResolvers.init();
			this.sqlResolvers=sqlResolvers;
			logger.info(getClass(),"sqlResolvers start... has resolvers ["+(this.sqlResolvers.getSqlResolvers().size())+"]");
		}
		if(this.cacheManager==null){
			super.cacheManager=getCacheManager();
		}
		if(this.sentinel==null){
			this.sentinel=new DefaultSentinel();
		}
	}
	

	@SuppressWarnings("unchecked")
	public <T> List<T> querySql(SqlTemplate st){
		SqlResolvers sqlResolvers=getSqlResolvers();
		long parserStart=System.currentTimeMillis();
		Rst rst=sqlResolvers.resolverLinks(st.getMainSql(),st.getSmlParams());
		Assert.isTrue(!SmlTools.isEmpty(rst.getSqlString()),"ifId["+ st.getId()+"] querySql config error parser is null");
		rst.setDbtype(getDbType(st.getDbid()));
		long parseEnd=System.currentTimeMillis();
		List<Object> paramsObject=rst.getParamObjects();
		String key=String.format(CACHE_DATA,st.getId())+rst.hashCode();
		String resultMap=MapUtils.getString(rst.getExtInfo(),FrameworkConstant.PARAM_RESULTMAP);
		Class<T> resultClassType=(Class<T>) (resultMap==null?Map.class:ClassUtil.loadClass(resultMap));
		List<T> result=null;
		if(openAllSentinel&&st.getIsCache()==1){
			try{
				sentinel.canAccess(key);
				result= run(key, st, rst, paramsObject, resultClassType,parseEnd-parserStart);
			}catch(RuntimeException e){
				throw e;
			}finally{
				sentinel.release(key);
			}
		}else{
			result=run0(key, st, rst, paramsObject, resultClassType,parseEnd-parserStart);
		}
		return (List<T>) result;
	}
	private <T> List<T> run(String key,SqlTemplate st,Rst rst,List<Object> paramsObject,Class<T> resultClassType,long cost){
		synchronized (sentinel.get(key)) {
			return run0(key, st, rst, paramsObject, resultClassType,cost);
		}
	}
	private <T> List<T> run0(String key,SqlTemplate st,Rst rst,List<Object> paramsObject,Class<T> resultClassType,long cost){
		List<T> result=null;
		Object tt= getCacheManager().get(key);
		if(tt!=null&&!Boolean.valueOf(st.getSmlParams().getValue(FrameworkConstant.PARAM_RECACHE,"false").toString())){
			return (List<T>) tt;
		}
		if(isLogger&&!Boolean.valueOf(st.getSmlParams().getValue(FrameworkConstant.PARAM_IGLOG,"false").toString()))
			logger.info(getClass(),"ifId["+st.getId()+"]-sql["+rst.getPrettySqlString()+"],sqlParseUseTime["+(cost)+"ms]");
		int queryReturnLimit=Boolean.valueOf(st.getSmlParams().getValue(FrameworkConstant.PARAM_TEST,"false").toString())?1000:Integer.MAX_VALUE;
		result= getJdbc(st.getDbid()).queryForList(rst.getSqlString(),paramsObject.toArray(new Object[]{}),resultClassType,queryReturnLimit);
		Object obj=rst.getExtInfo().get("collections");
		if(SmlTools.isNotEmpty(obj)&&resultClassType.equals(Map.class)){
			collection(st,result,(List<CollectionHandler>)obj);
		}
		if(st.getIsCache()==1)
		getCacheManager().set(key, result, st.getCacheMinutes());
		return result;
	}
	


	public <T> List<T> querySql(String dbid,String sql,Map<String,String> params){
		return querySql(SmlTools.toSqlTemplate(dbid,sql, params));
	}
	public int update(SqlTemplate st){
		int result=0;
		st.getSmlParams().add("__update__", "true").reinit();
		SqlResolvers sqlResolvers=getSqlResolvers();
		boolean isLinks=st.getSmlParams().getMapParams().keySet().contains(FrameworkConstant.PARAM_OPLINKS);
		if(!isLinks){
			Rst rst=sqlResolvers.resolverLinks(st.getMainSql(), st.getSmlParams());
			List<Object> paramsObject=rst.getParamObjects();
			if(isLogger&&!Boolean.valueOf(st.getSmlParams().getValue(FrameworkConstant.PARAM_IGLOG,"false").toString()))
			logger.info(getClass(),"ifId["+st.getId()+"]-sql["+rst.getSqlString()+"],params"+paramsObject.toString()+"]");
			result=getJdbc(st.getDbid()).update(rst.getSqlString(),paramsObject.toArray(new Object[]{}));
		}else{
			//links oparator
			String[] links=new Links(st.getSmlParams().getSqlParamFromList(FrameworkConstant.PARAM_OPLINKS).getValue().toString()).parseLinks().getOpLinks();
			List<String> linkSqls=MapUtils.newArrayList();
			List<Object[]> linkParams=MapUtils.newArrayList();
			List<String> isRealDo=MapUtils.newArrayList(); 
			for(String link:links){
				st.getSmlParams().getSmlParam(FrameworkConstant.PARAM_OPLINKS).setValue(link);
				Rst rst=sqlResolvers.resolverLinks(st.getMainSql(), st.getSmlParams());
				if(isLogger&&!Boolean.valueOf(st.getSmlParams().getValue(FrameworkConstant.PARAM_IGLOG,"false").toString()))
				logger.info(getClass(),"ifId["+st.getId()+"]-links["+link+"]-sql["+rst.getSqlString()+"],params"+rst.getParamObjects().toString()+"]");
				String realDo=String.valueOf((rst.getSqlString()+rst.getParamObjects()).hashCode());
				if(!isRealDo.contains(realDo)){
					linkSqls.add(rst.getSqlString());
					linkParams.add(rst.getParamObjects().toArray(new Object[]{}));
					isRealDo.add(realDo);
				}
			}
			result=getJdbc(st.getDbid()).update(linkSqls,linkParams);
		}
		String values=st.getSmlParams().getValue(FrameworkConstant.PARAM_FLUSHCACHEKEYS,"").toString();
		if(result>0&&!SmlTools.isEmpty(values)){
			for(String key:values.split("\\|")){
				cacheManager.clearKeyStart(String.format(CACHE_DATA,key));
			}
		}
		return result;
	}
	public void reInitSqlTemplate(SqlTemplate st){
				//以json格式返回
				if(SmlTools.isJsonStr(st.getConditionInfo())){
					if(st.getConditionInfo().contains("\"sqlParams\"")){
						st.setConditionInfo(st.getConditionInfo().replace("\"sqlParams\"", "\"smlParams\""));
					}
					if(jsonMapper!=null){
						st.setSmlParams(jsonMapper.toObj(st.getConditionInfo(),SMLParams.class).reinit());
					}
				}else{
					st.setSmlParams(SmlTools.toSplParams(st.getConditionInfo()));
				}
				if(SmlTools.isJsonStr(st.getRebuildInfo())){
					if(jsonMapper!=null){
						st.setRebuildParam(jsonMapper.toObj(st.getRebuildInfo(),RebuildParam.class));
					}
				}else{
					st.setRebuildParam(SmlTools.toRebuildParam(st.getRebuildInfo()));
				}
	}
	public Object builder(SqlTemplate sqlTemplate){
		return DataBuilderHelper.build(sqlTemplate.getRebuildParam(),querySql(sqlTemplate),new SmlContextUtils(this),sqlTemplate);
	}
	
	public Rslt queryRslt(SqlTemplate st){
		SqlResolvers sqlResolvers=getSqlResolvers();
		Rst rst=sqlResolvers.resolverLinks(st.getMainSql(), st.getSmlParams());
		String sqlString=rst.getSqlString();
		List<Object> paramsObject=rst.getParamObjects();
		if(isLogger&&!Boolean.valueOf(st.getSmlParams().getValue(FrameworkConstant.PARAM_IGLOG,"false").toString()))
		logger.info(getClass(),"sql["+rst.getPrettySqlString()+"]");
		return getJdbc(st.getDbid()).query(sqlString,paramsObject.toArray(new Object[]{}), new Rset());
	}

	public Rslt queryRslt(String dbid,String sql,Map<String,String> params){
		return queryRslt(SmlTools.toSqlTemplate(dbid, sql, params));
	}
	
	private <T> List<T> collection(SqlTemplate st,List<T> result, List<CollectionHandler> doAfters) {
		Map<String,Object> kvs=st.getSmlParams().getMap();
		for(CollectionHandler doAfter:doAfters){
			for(T t:result){
				if(t instanceof Map){
					Map<String,Object> obj=(Map<String, Object>) t;
					if(!doAfter.isOk()){
						obj.put(doAfter.getId(),null);
					}else{
						if(!doAfter.check(kvs,obj)){
							obj.put(doAfter.getId(),null);
						}else{
							doAfter.doIt(obj);
						}
					}
				}
			}
		}
		return result;
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

	public boolean getIsLogger() {
		return isLogger;
	}

	public void setIsLogger(boolean isLogger) {
		this.isLogger = isLogger;
	}


	public Sentinel getSentinel() {
		return sentinel;
	}


	public void setSentinel(Sentinel sentinel) {
		this.sentinel = sentinel;
	}


	public boolean isOpenAllSentinel() {
		return openAllSentinel;
	}


	public void setOpenAllSentinel(boolean openAllSentinel) {
		this.openAllSentinel = openAllSentinel;
	}
	
}
