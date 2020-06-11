package org.hw.sml.core;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hw.sml.FrameworkConstant;
import org.hw.sml.context.SmlContextUtils;
import org.hw.sml.core.build.SmlTools;
import org.hw.sml.core.resolver.Rst;
import org.hw.sml.jdbc.AbstractCallbackCycle;
import org.hw.sml.jdbc.JdbcTemplate;
import org.hw.sml.model.Result;
import org.hw.sml.model.SqlTemplate;
import org.hw.sml.report.model.Constants;
import org.hw.sml.report.model.Update;
import org.hw.sml.support.LoggerHelper;
import org.hw.sml.support.cache.CacheManager;
import org.hw.sml.support.el.Links;
import org.hw.sml.support.ioc.annotation.Inject;
import org.hw.sml.support.log.Loggers;
import org.hw.sml.tools.MapUtils;

public class DelegatedSqlMarkupAbstractTemplate {
	@Inject(required=false)
	protected SqlMarkupAbstractTemplate sqlMarkupAbstractTemplate;
	protected SmlContextUtils smlContextUtils;
	protected Loggers logger=LoggerHelper.getLogger();
	/**
	 * 增加数据
	 * @throws Exception
	 */
	public int add(Update update){
		update.setType(Constants.TYPE_INSERT);
		update.init();
		if(!update.getInLog())
		logger.debug(getClass(),"executeSql add:["+update.getUpateSql()+"]");
		int[] flag = getJdbc(update.getDbId()).batchUpdate(update.getUpateSql(), update.getObjects());
		int affectRecord=affectRecord(flag);
		clearRefCache(update.isClearRefIf()&&affectRecord>0,update.getTableName());
		return affectRecord(flag);
	}
	public int update(List<Update> updates){
		List<String> sqls=MapUtils.newArrayList();
		List<List<Object[]>> params=MapUtils.newArrayList();
		String dbid=null;
		for(Update update:updates){
			dbid=update.getDbId();
			update.init();
			if(update.getData().isEmpty()){
				continue;
			}
			sqls.add(update.getUpateSql());
			params.add(update.getObjects());
		}
		logger.debug(getClass(),"sqls:["+sqls+"]");
		if(sqls.size()==0){
			return 0;
		}
		return getJdbc(dbid).updates(sqls, params);
	}
	/**
	 * 更新数据
	 * @param update
	 * @return
	 * @throws Exception
	 */
	public int update(Update update) throws Exception{
		update.setType(Constants.TYPE_UPDATE);
		update.init();
		if(!update.getInLog())
		logger.debug(getClass(),"executeSql update:["+update.getUpateSql()+"]");
		int[] flag = getJdbc(update.getDbId()).batchUpdate(update.getUpateSql(), update.getObjects());
		int affectRecord=affectRecord(flag);
		clearRefCache(update.isClearRefIf()&&affectRecord>0,update.getTableName());
		return affectRecord;
	}
	private void clearRefCache(boolean clearRefIf, String tableName) {
		if(clearRefIf){
			clearRefCache(tableName.toLowerCase());
		}
	}
	public int clearRefCache(String tableName){
		CacheManager cacheManager=sqlMarkupAbstractTemplate.getCacheManager();
		Map<String,Object> caches=cacheManager.getKeyStart(SqlMarkupAbstractTemplate.CACHE_PRE);
		int i=0;
		for(String cacheKey:caches.keySet()){
			if(cacheKey.endsWith("sqlTemplate")){
				SqlTemplate sqlTemplate=(SqlTemplate) cacheManager.get(cacheKey);
				if(sqlTemplate.getIsCache()==1&&sqlTemplate.getMainSql()!=null&&sqlTemplate.getMainSql().toLowerCase().contains(tableName)){
					i+=clear(sqlTemplate.getId());
				}
			}
		}
		return i;
	}
	/**
	 * 删除数据
	 * @param update
	 * @return
	 * @throws Exception
	 */
	public int delete(Update update) throws Exception{
		update.setType(Constants.TYPE_DELETE);
		update.init();
		if(!update.getInLog())
		logger.debug(getClass(),"executeSql delete:["+update.getUpateSql()+"]");
		int[] flag = getJdbc(update.getDbId()).batchUpdate(update.getUpateSql(), update.getObjects());
		int affectRecord=affectRecord(flag);
		clearRefCache(update.isClearRefIf()&&affectRecord>0,update.getTableName());
		return affectRecord;
	}
	/**
	 * 存在更新，不存在新加数据
	 * @param update
	 * @return
	 * @throws Exception
	 */
	public int adu(Update update) throws Exception{
		update.setType(Constants.TYPE_ADU);
		update.init();
		boolean exists=getJdbc(update.getDbId()).queryForInt(update.isExistSql(), update.getExistParams())>0;
		if(!update.getInLog())
		logger.debug(getClass(),"executeSql adu:["+update.updateSqlForAdu(exists)+"]");
		int flag = getJdbc(update.getDbId()).update(update.updateSqlForAdu(exists),update.objectForAdu(exists));
		clearRefCache(update.isClearRefIf()&&flag>0,update.getTableName());
		return flag;
	}
	public <T> T queryComm(Map<String,String> params){
		return query(params.get("ifId"),params);
	}
	public <T> T query(String ifId,Map<String,String> params){
		return getSmlContextUtils().query(ifId,params);
	}
	/**
	 * 配置更新
	 * @param ifId
	 * @param params
	 * @return
	 */
	public int update(String ifId,Map<String,String> params){
		return getSmlContextUtils().update(ifId, params);
	}
	public int update(String ifId,List<Map<String,Object>> lst){
		int result;
		SqlTemplate sqlTemplate=getSqlMarkupAbstractTemplate().getSqlTemplate(ifId);
		List<String> sqls=MapUtils.newArrayList();
		List<Object[]> params=MapUtils.newArrayList();
		for(Map<String,Object> obj:lst){
			SqlTemplate st=sqlTemplate.clone();
			getSqlMarkupAbstractTemplate().reInitSqlTemplate(st);
			Map<String,String> pa=SmlTools.rebuildSimpleKv(obj);
			SmlContextUtils.reInitSqlParam(st,getJdbc(st.getDbid()),pa);
			boolean isLinks=st.getSmlParams().getMapParams().keySet().contains(FrameworkConstant.PARAM_OPLINKS);
			if(!isLinks){
				Rst rst=getSqlMarkupAbstractTemplate().getSqlResolvers().resolverLinks(st.getMainSql(),st.getSmlParams());
				sqls.add(rst.getSqlString());
				params.add(rst.getParamObjects().toArray(new Object[]{}));
			}else{
				String[] links=new Links(st.getSmlParams().getSqlParamFromList(FrameworkConstant.PARAM_OPLINKS).getValue().toString()).parseLinks().getOpLinks();
				for(String link:links){
					st.getSmlParams().getSmlParam(FrameworkConstant.PARAM_OPLINKS).setValue(link);
					Rst rst=getSqlMarkupAbstractTemplate().getSqlResolvers().resolverLinks(st.getMainSql(), st.getSmlParams());
					sqls.add(rst.getSqlString());
					params.add(rst.getParamObjects().toArray(new Object[]{}));
				}
			}
		}
		boolean batch=true;
		String s=sqls.get(0);
		for(String sql:sqls){
			if(!s.equals(sql)){
				batch=false;
				break;
			}
		}
		if(batch){
			 result=getJdbc(sqlTemplate.getDbid()).batchUpdate(s,params).length;
		}else
			result= getJdbc(sqlTemplate.getDbid()).update(sqls,params);
		return result;
	}
	public Map<String,Object> updateReturnMap(String ifId,Map<String,String> params){
		return getSmlContextUtils().updateReturnMap(ifId, params);
	}
	public int update(Map<String,String> params){
		return getSmlContextUtils().update(params);
	}
	public Rslt queryRslt(String dbid,String sql,Map<String,String> params){
		return sqlMarkupAbstractTemplate.queryRslt(dbid, sql, params);
	}
	public void queryForCallback(String id,Map<String,String> params,AbstractCallbackCycle callback){
		SqlTemplate st=sqlMarkupAbstractTemplate.getSqlTemplate(id);
		SmlContextUtils.reInitSqlParam(st,getJdbc(st.getDbid()),params);
		Rst rst=sqlMarkupAbstractTemplate.getSqlResolvers().resolverLinks(st.getMainSql(),st.getSmlParams());
		LoggerHelper.getLogger().debug(getClass(),"ifId["+id+"]:"+rst.getPrettySqlString());
		queryForCallback(st.getDbid(),rst.getSqlString(),rst.getParamObjects().toArray(new Object[]{}),callback);
	}
	public void queryForCallback(String dbid,String sql,Object[] params,AbstractCallbackCycle callback){
		getJdbc(dbid).queryForCallback(sql,params,callback);
	}
	public void queryStream(String id,Map<String,String> params,final OutputStream os){
		queryForCallback(id, params,new StreamCallback(os));
	}
	public  class StreamCallback extends AbstractCallbackCycle{
		private OutputStream os;
		private int limit=Integer.MAX_VALUE;
		public StreamCallback(OutputStream os){
			this.os=os;
		}
		public StreamCallback(OutputStream os,int limit){
			this.os=os;
			this.limit=limit;
		}
		public void start() {
			try {
				os.write("[".getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public void end() {
			try {
				os.write("]".getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public void exchange(Map<String,Object> obj){
			
		}
		public void callProxy(ResultSet rs, int rowNum) throws SQLException {
			Map<String,Object> map=MapUtils.newHashMap();
			for(int i=0;i<super.headers.size();i++){
				map.put(headers.get(i),super.getResultSetValue(rs,i+1));
			}
			exchange(map);
			if(rowNum>limit){
				return;
			}
			try {
				if(rowNum>0){
					os.write(",".getBytes());
				}
				os.write(sqlMarkupAbstractTemplate.getJsonMapper().toJson(map).getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public List<Map<String,Object>> queryForList(String dbid,String sql,Map<String,String> params){
		return sqlMarkupAbstractTemplate.querySql(dbid, sql, params);
	}
	public Result page(Map<String,String> params){
		return page(params.get("ifId"),params);
	}
	public Result page(String ifId,Map<String,String> params){
		return getSmlContextUtils().query(ifId,params);
	}
	public void registDataSource(String dbid,DataSource dataSource){
		 getSmlContextUtils().registDataSource(dbid, dataSource);
	}
	private int affectRecord(int[] rc){
		if(rc.length==1){
			return rc[0];
		}else
			return rc.length;
	}
	public JdbcTemplate getJdbc(String dbid){
		return sqlMarkupAbstractTemplate.getJdbc(dbid);
	}
	
	public int clear(String parameter) {
		return getSmlContextUtils().clear(parameter);
	}
	public SqlMarkupAbstractTemplate getSqlMarkupAbstractTemplate() {
		return sqlMarkupAbstractTemplate;
	}
	public void setSqlMarkupAbstractTemplate(
			SqlMarkupAbstractTemplate sqlMarkupAbstractTemplate) {
		this.sqlMarkupAbstractTemplate = sqlMarkupAbstractTemplate;
	}
	public SmlContextUtils getSmlContextUtils(){
		if(this.smlContextUtils==null){
			smlContextUtils=new SmlContextUtils(sqlMarkupAbstractTemplate);
		}
		return smlContextUtils;
	}
}