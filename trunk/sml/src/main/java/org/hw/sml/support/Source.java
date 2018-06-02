package org.hw.sml.support;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.hw.sml.jdbc.JdbcTemplate;
import org.hw.sml.plugin.Plugin;
import org.hw.sml.support.cache.CacheManager;
import org.hw.sml.support.cache.DefaultCacheManager;
import org.hw.sml.support.log.Loggers;
import org.hw.sml.tools.ClassUtil;



public class Source implements Plugin{
	
	protected Loggers logger=LoggerHelper.getLogger();
	
	protected Object lock=new Object();
	
	protected String jdbcClassPath="org.hw.sml.jdbc.impl.DefaultJdbcTemplate";
	
	protected String frameworkMark="default";
	
	protected JdbcTemplate defJt;
	
	protected Map<String,JdbcTemplate> jts=new HashMap<String,JdbcTemplate>();
	
	protected Map<String,DataSource> dss=new HashMap<String,DataSource>();
	
	protected CacheManager cacheManager;
	
	protected boolean transactionInversion;
	
	public JdbcTemplate newJdbcTemplate(DataSource dataSource){
		JdbcTemplate jt=ClassUtil.newInstance(jdbcClassPath,JdbcTemplate.class);
		if(transactionInversion){
			try {
				ClassUtil.injectFieldValue(jt,"transactionInversion",transactionInversion);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		jt.setDataSource(dataSource);
		return jt;
	}
	
	public void init(){
		if(jts.size()==0){
			for(Map.Entry<String,DataSource> entry:dss.entrySet()){
				jts.put(entry.getKey(),newJdbcTemplate(entry.getValue()));
				logger.info(getClass(),"init jdbc-template["+entry.getKey()+"].");
			}
			if(this.defJt==null){
				this.defJt=jts.get("defJt");
			}
		}
	}
	
	public JdbcTemplate getJdbc(String dbid){
		JdbcTemplate jt=jts.get(dbid);
		if(jt!=null){
			return jt;
		}
		return defJt;
	}

	public JdbcTemplate getDefJt() {
		return defJt;
	}

	public void setDefJt(JdbcTemplate defJt) {
		this.defJt = defJt;
	}

	public Map<String, JdbcTemplate> getJts() {
		return jts;
	}

	public void setJts(Map<String, JdbcTemplate> jts) {
		this.jts = jts;
	}

	public Map<String, DataSource> getDss() {
		return dss;
	}

	public void setDss(Map<String, DataSource> dss) {
		this.dss = dss;
	}

	public String getFrameworkMark() {
		return frameworkMark;
	}

	public void setFrameworkMark(String frameworkMark) {
		this.frameworkMark = frameworkMark;
	}

	public CacheManager getCacheManager() {
		if(cacheManager==null){
			this.cacheManager= DefaultCacheManager.newInstance();
		}
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public Object getLock() {
		return lock;
	}

	public void setLock(Object lock) {
		this.lock = lock;
	}
	
	public String getJdbcClassPath() {
		return jdbcClassPath;
	}

	public void setJdbcClassPath(String jdbcClassPath) {
		this.jdbcClassPath = jdbcClassPath;
	}

	public boolean isTransactionInversion() {
		return transactionInversion;
	}

	public void setTransactionInversion(boolean transactionInversion) {
		this.transactionInversion = transactionInversion;
	}

	@Override
	public void destroy() {
		cacheManager.destroy();
		dss=null;
		jts=null;
	}
}
