package org.hw.sml.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hw.sml.context.SmlContextUtils;
import org.hw.sml.jdbc.JdbcTemplate;
import org.hw.sml.model.Result;
import org.hw.sml.report.model.Constants;
import org.hw.sml.report.model.Update;
import org.hw.sml.support.LoggerHelper;
import org.hw.sml.support.ioc.annotation.Inject;
import org.hw.sml.support.log.Loggers;
import org.hw.sml.tools.MapUtils;

public class DelegatedSqlMarkupAbstractTemplate {
	
	@Inject
	protected SqlMarkupAbstractTemplate sqlMarkupAbstractTemplate;
	
	protected SmlContextUtils smlContextUtils;
	
	protected String mark="";
	
	protected String tableName;
	
	protected Map<String,String> mapper=new HashMap<String,String>();
	
	protected Loggers logger=LoggerHelper.getLogger();
	
	
	/**
	 * 增加数据
	 * @param update
	 * @return
	 * @throws Exception
	 */
	public int add(Update update) throws Exception{
		if(tableName!=null)
		update.setTableName(tableName);
		update.setType(Constants.TYPE_INSERT);
		update.init();
		if(!update.getInLog())
		logger.debug(getClass(),"executeSql add:["+update.getUpateSql()+"]");
		int[] flag = getJdbc(update.getDbId()).batchUpdate(update.getUpateSql(), update.getObjects());
		return affectRecord(flag);
	}
	public int update(List<Update> updates){
		List<String> sqls=MapUtils.newArrayList();
		List<List<Object[]>> params=MapUtils.newArrayList();
		String dbid=null;
		for(Update update:updates){
			dbid=update.getDbId();
			update.init();
			sqls.add(update.getUpateSql());
			params.add(update.getObjects());
		}
		logger.debug(getClass(),"sqls:["+sqls+"]");
		return getJdbc(dbid).updates(sqls, params);
	}
	/**
	 * 更新数据
	 * @param update
	 * @return
	 * @throws Exception
	 */
	public int update(Update update) throws Exception{
		if(tableName!=null)
		update.setTableName(tableName);
		update.setType(Constants.TYPE_UPDATE);
		update.init();
		if(!update.getInLog())
		logger.debug(getClass(),"executeSql update:["+update.getUpateSql()+"]");
		int[] flag = getJdbc(update.getDbId()).batchUpdate(update.getUpateSql(), update.getObjects());
		return affectRecord(flag);
	}
	/**
	 * 删除数据
	 * @param update
	 * @return
	 * @throws Exception
	 */
	public int delete(Update update) throws Exception{
		if(tableName!=null)
		update.setTableName(tableName);
		update.setType(Constants.TYPE_DELETE);
		update.init();
		if(!update.getInLog())
		logger.debug(getClass(),"executeSql delete:["+update.getUpateSql()+"]");
		int[] flag = getJdbc(update.getDbId()).batchUpdate(update.getUpateSql(), update.getObjects());
		return affectRecord(flag);
	}
	/**
	 * 存在更新，不存在新加数据
	 * @param update
	 * @return
	 * @throws Exception
	 */
	public int adu(Update update) throws Exception{
		if(tableName!=null)
		update.setTableName(tableName);
		update.setType(Constants.TYPE_ADU);
		update.init();
		boolean exists=getJdbc(update.getDbId()).queryForInt(update.isExistSql(), update.getExistParams())>0;
		if(!update.getInLog())
		logger.debug(getClass(),"executeSql adu:["+update.getUpdateSqlForAdu(exists)+"]");
		int flag = getJdbc(update.getDbId()).update(update.getUpdateSqlForAdu(exists),update.getObjectForAdu(exists));
		return flag;
	}
	/**
	 * 根据id查单条记录
	 * @param params
	 * @return
	 */
	public Map<String,Object> queryById(Map<String,String> params){
		String ifId=params.get("ifId");
		if(ifId==null){
		   ifId=mapper.get("queryById")==null?(getClassName()+"-"+"queryById"):mapper.get("queryById");
		}
		return getSmlContextUtils().query(ifId,params);
	}
	/**
	 * 根据查询条件查多条记录
	 * @param params
	 * @return
	 */
	public List<Map<String,Object>> query(Map<String,String> params){
		String ifId=params.get("ifId");
		if(ifId==null){
		   ifId=mapper.get("query")==null?(getClassName()+"-"+"query"):mapper.get("query");
		}
		return getSmlContextUtils().query(ifId,params);
	}
	/**
	 * 查询返回任意配置返回值
	 * @param params
	 * @return
	 */
	public <T> T queryComm(Map<String,String> params){
		return query(params.get("ifId"),params);
	}
	public <T> T query(String ifId,Map<String,String> params){
		return getSmlContextUtils().query(ifId,params);
	}
	/**
	 * 分页查询
	 * @param params
	 * @return
	 */
	public Result page(Map<String,String> params){
		String ifId=params.get("ifId");
		if(ifId==null){
			ifId=mapper.get("page")==null?(getClassName()+"-"+"page"):mapper.get("page");
		}
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
	public int update(Map<String,String> params){
		return getSmlContextUtils().update(params);
	}
	public Rslt queryRslt(String dbid,String sql,Map<String,String> params){
		return sqlMarkupAbstractTemplate.queryRslt(dbid, sql, params);
	}
	public List<Map<String,Object>> queryForList(String dbid,String sql,Map<String,String> params){
		return sqlMarkupAbstractTemplate.querySql(dbid, sql, params);
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


	public String getMark() {
		return mark;
	}
	public void setMark(String mark) {
		this.mark = mark;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public Map<String, String> getMapper() {
		return mapper;
	}
	public void setMapper(Map<String, String> mapper) {
		this.mapper = mapper;
	}
	public String getClassName(){
		return mark.length()>0?(mark+"-"+this.getClass().getSimpleName()):this.getClass().getSimpleName();
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