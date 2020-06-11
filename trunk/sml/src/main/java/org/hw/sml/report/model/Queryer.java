package org.hw.sml.report.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置报表模块:进行查询ParamCriteria生成
 * @author wen
 *
 */
public class Queryer implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4614154618748966036L;
	
	private String rcptId;
	
	private ParamCriteria paramCriteria;
	
	private boolean isPage;
	
	private boolean nocache=false;
	
	private String toCaseForKey;
	
	public Queryer(){
		paramCriteria=new ParamCriteria();
	}
	public Queryer(String rcptId){
		this.rcptId=rcptId;
		paramCriteria=new ParamCriteria();
		paramCriteria.setRcptId(rcptId);
	}
	private ChartParam chartParam=new ChartParam();
	public Queryer enabledChart(List<String> groupIds){
		paramCriteria.setChart(chartParam);
		chartParam.setGroupid(groupIds);
		return this;
	}
	public Queryer chart(String name,String op){
		chartParam.getFuncs().put(name,op);
		return this;
	}
	public Queryer intendedFields(List<String> intendedFields){
		paramCriteria.setIntendedFields(intendedFields);
		return this;
	}
	public Queryer addQuery(String name,String operator,String value){
		Map<String,List<Operator>> conditions=paramCriteria.getConditionMap();
		if(conditions==null){
			conditions=new LinkedHashMap<String, List<Operator>>();
			paramCriteria.setConditionMap(conditions);
		}
		if(conditions.get(name)==null){
			conditions.put(name,new ArrayList<Operator>());
		}
		if(value!=null){
			conditions.get(name).add(new Operator(operator, value));
		}
		return this;
	}
	public Queryer addOrder(String orderName,String orderType){
		List<String> orderFields=paramCriteria.getOrderByFields();
		if(orderFields==null){
			orderFields=new ArrayList<String>();
			paramCriteria.setOrderByFields(orderFields);
		}
		if(!orderFields.contains(orderName)){
			orderFields.add(orderName);
			paramCriteria.setOrderByType(orderType);
		}
		return this;
	}
	public Queryer limit(int pageNo,int pageSize){
		isPage=true;
		paramCriteria.setPage(pageNo);
		paramCriteria.setRowPerPage(pageSize);
		paramCriteria.setStartIndex((pageNo-1)*pageSize+1);
		return this;
	}
	public String getRcptId() {
		return rcptId;
	}
	public Queryer setRcptId(String rcptId) {
		this.rcptId = rcptId;
		return this;
	}
	public ParamCriteria getParamCriteria() {
		return paramCriteria;
	}
	public boolean isPage() {
		return isPage;
	}
	public void setPage(boolean isPage) {
		this.isPage = isPage;
	}
	public boolean isNocache() {
		return nocache;
	}
	public void setNocache(boolean nocache) {
		this.nocache = nocache;
	}
	public ChartParam getChartParam() {
		return chartParam;
	}
	public Queryer toCaseForKey(String value) {
		this.toCaseForKey=value;
		return this;
	}
	public String getToCaseForKey() {
		return toCaseForKey;
	}
	
}
