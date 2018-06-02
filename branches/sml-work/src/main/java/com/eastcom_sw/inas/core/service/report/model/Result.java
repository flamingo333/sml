package com.eastcom_sw.inas.core.service.report.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hw.sml.tools.MapUtils;
/**
 * 为分页简单的写的包装类
 * @author wen
 *
 */
public class Result {
	private int page;
	private int limit;
	private Long count;
	private Map<String,Object> extInfo=MapUtils.newHashMap();
	private List<Map<String,Object>> datas=new ArrayList<Map<String,Object>>();
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	public List<Map<String, Object>> getDatas() {
		return datas;
	}
	public void setDatas(List<Map<String, Object>> datas) {
		this.datas = datas;
	}
	
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public Map<String, Object> getExtInfo() {
		return extInfo;
	}
	public void setExtInfo(Map<String, Object> extInfo) {
		this.extInfo = extInfo;
	}
	@Override
	public String toString() {
		return "Result [count=" + count + ", datas=" + datas + "]";
	}
	
}
