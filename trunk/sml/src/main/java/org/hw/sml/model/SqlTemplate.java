package org.hw.sml.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.hw.sml.core.RebuildParam;
import org.hw.sml.model.SMLParams;
import org.hw.sml.tools.SerializationUtils;
/**
 * @author hw
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SqlTemplate")
@XmlRootElement(name = "SqlTemplate")
public class SqlTemplate   implements Cloneable,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -251852631091770199L;
	/**
	 * 
	 */
	//
	@XmlElement(name="ID")
	private String id;
	//主体sql
	@XmlElement(name="MAINSQL")
	private String mainSql;
	
	//主体sql
	@XmlElement(name="REBUILD_INFO")
	private String rebuildInfo;
	
	@XmlElement(name="CONDITION_INFO")
	private String conditionInfo;
	
	@XmlElement(name="CACHE_ENABLED")
	private Integer isCache=0;
	
	@XmlElement(name="CACHE_MINUTES")
	private int cacheMinutes=5;
	
	@XmlElement(name="DBID")
	private String dbid;
	@XmlElement(name="DESCRIBE")
	private String remark;
	
	@XmlTransient
	private  RebuildParam rebuildParam;
	
	@XmlTransient
	private SMLParams SmlParams=new SMLParams();
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMainSql() {
		return mainSql;
	}

	public void setMainSql(String mainSql) {
		this.mainSql = mainSql;
	}

	
	public Integer getIsCache() {
		return isCache==null?0:isCache;
	}

	public void setIsCache(Integer isCache) {
		this.isCache = isCache;
	}

	public int getCacheMinutes() {
		return cacheMinutes;
	}

	public void setCacheMinutes(int cacheMinutes) {
		this.cacheMinutes = cacheMinutes;
	}

	public String getDbid() {
		return dbid;
	}

	public void setDbid(String dbid) {
		this.dbid = dbid;
	}
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public String getRebuildInfo() {
		return rebuildInfo;
	}

	public void setRebuildInfo(String rebuildInfo) {
		this.rebuildInfo = rebuildInfo;
	}

	public String getConditionInfo() {
		return conditionInfo;
	}

	public void setConditionInfo(String conditionInfo) {
		this.conditionInfo = conditionInfo;
	}

	public RebuildParam getRebuildParam() {
		return rebuildParam;
	}

	public void setRebuildParam(RebuildParam rebuildParam) {
		this.rebuildParam = rebuildParam;
	}

	public SMLParams getSmlParams() {
		return SmlParams;
	}

	public void setSmlParams(SMLParams smlParams) {
		SmlParams = smlParams;
	}
	public SqlTemplate clone() {
		return (SqlTemplate) SerializationUtils.clone(this);
	}
	
}
