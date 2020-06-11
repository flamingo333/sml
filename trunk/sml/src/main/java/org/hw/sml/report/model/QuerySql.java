package org.hw.sml.report.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hw.sml.core.build.SqlFilterHelper;
import org.hw.sml.core.resolver.Rst;
import org.hw.sml.model.DbType;
import org.hw.sml.tools.Assert;
import org.hw.sml.tools.DateTools;
import org.hw.sml.tools.MapUtils;
/**
 * 
 * @author hw
 *
 */

public class QuerySql {
	private PiTable piTable;
	private List<PiTableDetail> piTableDetails;
	private ParamCriteria paramCriteria;
	private String tableName;
	private String columnString;
	private String columnFormatString;
	private String conditionString;
	private String orderString;
	private String firstField;
	private String queryString;
	private boolean containChart;
	private String chartSql;
	private Map<String,String> piKV=MapUtils.newLinkedCaseInsensitiveMap();
	private Map<String,PiTableDetail> piMap=MapUtils.newLinkedCaseInsensitiveMap();
	private List<Object> queryParams=new ArrayList<Object>();
	private List<String> columnAppendString=new ArrayList<String>();
	private List<String> columnChartString=new ArrayList<String>();
	private String type=Constants.TYPE_QUERY_QUERY;
	public QuerySql(PiTable piTable, List<PiTableDetail> piTableDetails,
			ParamCriteria pc) {
		this.piTable=piTable;
		this.piTableDetails=piTableDetails;
		this.paramCriteria=pc;
		this.tableName=piTable.getTableName();
		this.type=this.paramCriteria.getType();
		for(PiTableDetail pi:piTableDetails){
			piMap.put(pi.getField(), pi);
		}
		handlerPiTableDetails();
		builderCondition();
		builderOrder();
		builderQuery();
		builderChart();
		//
	}
	
	
	private void builderChart() {
		ChartParam chart=paramCriteria.getChart();
		if(chart==null){
			containChart=false;
			return;
		}
		containChart=true;
		List<String> groupID=chart.getGroupid();
		StringBuffer groupidString=new StringBuffer();
		for(String str:groupID){
			PiTableDetail pi=this.piMap.get(str);
			columnChartString.add(pi.getField()+pi.getFiledReturnType(type));
			groupidString.append(str).append(",");
		}
		StringBuffer funcString=new StringBuffer();
		Map<String,String> funcs=chart.getFuncs();
		for(Map.Entry<String,String> entry:funcs.entrySet()){
			columnChartString.add(entry.getKey());
			funcString.append(entry.getValue()).append(" as ").append(entry.getKey()).append(",");
		}
		groupidString=groupidString.deleteCharAt(groupidString.length()-1);
		this.chartSql="select "+groupidString+","+funcString.deleteCharAt(funcString.length()-1)+" from("+this.queryString+") t group by "+groupidString +" order by "+groupidString;
	}
	public String getChartSql(){
		return this.chartSql;
	}

	public QuerySql(PiTable piTable, List<PiTableDetail> piTableDetails) {
		this.piTable=piTable;
		this.piTableDetails=piTableDetails;
		this.tableName=piTable.getTableName();
		for(PiTableDetail pi:piTableDetails){
			piMap.put(pi.getField(), pi);
		}
		handlerPiTableDetails();
	}


	private void builderQuery() {
		this.queryString=" select "+this.columnFormatString+" from "+this.tableName;
		this.queryString="select "+this.columnString+" from ("+this.queryString+") t where 1=1 "+this.conditionString;
		this.queryString=this.queryString.replace("where 1=1  and","where");
	}


	private void builderOrder() {
		StringBuffer bf=new StringBuffer("");
		List<String> orderFields=paramCriteria.getOrderByFields();
		if(orderFields!=null){
			for(String of:orderFields){
				//Assert.isTrue(piMap.containsKey(of),of+" is not be null for order by,please config it.");
				//去掉此判断
				bf.append(of+",");
			}
		}else{
			if(paramCriteria.getIntendedFields()!=null&&paramCriteria.getIntendedFields().size()>0){
				bf.append(paramCriteria.getIntendedFields().get(0)+",");
			}else{
				bf.append(firstField+",");
			}
		}
		this.orderString=bf.deleteCharAt(bf.length()-1).toString()+" "+(paramCriteria.getOrderByType()==null?"":paramCriteria.getOrderByType());
	}


	private void builderCondition() {
		Map<String,List<Operator>> map=paramCriteria.getConditionMap();
		if(map==null){
			this.conditionString="";
		}else{
			StringBuffer bf=new StringBuffer();
			Map<String,String> params=MapUtils.newLinkedCaseInsensitiveMap();
			for(Map.Entry<String,List<Operator>> entry:map.entrySet()){
				String field=entry.getKey();
				for(Operator op:entry.getValue()){
					//增加条件并绑定参数
					PiTableDetail pit=piMap.get(field);
					Assert.notNull(pit,"param ["+field+"] is not exists in table["+tableName+"]");
					String fieldType=pit.getFieldType();
					Assert.notNull(fieldType,"fieldType is not be null,please config it.");
					String[] fts=fieldType.split("@");
					String ft=fts[0].toLowerCase();
					String param="condition_"+op.getOperator()+"_"+field;
					if(ft.contains("time")||ft.contains("date")){
						param=param+"@date";
					}
					params.put(param,op.getValue());
				}
			}
			Rst rst=SqlFilterHelper.createConditionSqlReturnRst(params,DbType.valueOf(paramCriteria.getSqlType().toLowerCase()));
			bf.append(rst.getSqlString());
			queryParams.addAll(rst.getParamObjects());
			//无奈放开最大操作给页面，危险系数直接放大
			if(paramCriteria.getSqlAppend()!=null){
				bf.append(paramCriteria.getSqlAppend());
			}
			this.conditionString=bf.toString();
		}
	}


	private void handlerPiTableDetails() {
		List<String> ifs=paramCriteria==null?null:paramCriteria.getIntendedFields();
		Map<String,String> maps=MapUtils.newLinkedCaseInsensitiveMap();
		StringBuffer columnS=new StringBuffer();
		StringBuffer columnSF=new StringBuffer();
		boolean flag=ifs==null||ifs.size()==0;
		if(!flag){
			for(String ift:ifs){
				maps.put(ift,ift);
			}
		}
		for(int i=0;i< piTableDetails.size();i++){
			PiTableDetail pi=piTableDetails.get(i);
			if(this.firstField==null)
		    this.firstField=pi.getField();
			if((flag||maps.containsKey(pi.getField()))&&pi.contain(type)){
				columnS.append(sense(pi.getFormat())+" as "+sense(pi.getField()) +",");//
				columnSF.append(sense(pi.getField())+",");
				columnAppendString.add(pi.getField()+pi.getFiledReturnType(type));
				piKV.put(pi.getField(),pi.getFieldZn());
				
			}
		}
		this.columnFormatString=columnS.deleteCharAt(columnS.length()-1).toString();
		this.columnString=columnSF.deleteCharAt(columnSF.length()-1).toString();
	}
	private String sense(String name){
		if((!name.startsWith("`"))&&Arrays.asList("mysql","mariadb","gbase").contains(paramCriteria.getSqlType())&&Arrays.asList("describe").contains(name.toLowerCase())){
			return "`"+name+'`';
		}
		return name;
	}

	
	public String toString(){
		//其它数据库自己可以判断
		if(paramCriteria.getSqlType().equalsIgnoreCase("SYABASEIQ")){
			return builderIq();
		}else if(paramCriteria.getSqlType().equalsIgnoreCase("ORACLE")){
			return builderOracle();
		}else if(paramCriteria.getSqlType().equalsIgnoreCase(DbType.mysql.name())||paramCriteria.getSqlType().equalsIgnoreCase(DbType.mariadb.name())){
			return builderMysql();
		}else if(paramCriteria.getSqlType().equalsIgnoreCase(DbType.postgresql.name())){
			return builderPg();
		}
		return  builderIq();
	}
	private String builderOracle() {
	   StringBuilder pagingSelect = new StringBuilder();
       pagingSelect.append("select * from (select row_.*, rownum rownum_ from ( ");
       pagingSelect.append(queryString+" order by "+orderString);
       pagingSelect.append(" ) row_ where rownum < ").append("?").append(" )  where rownum_ >= ").append("?");
       return pagingSelect.toString();
	}
	private String builderMysql(){
		 StringBuilder pagingSelect = new StringBuilder();
		 pagingSelect.append(queryString+" order by "+orderString);
		 return pagingSelect.append(" limit ?,?").toString();
	}
	private String builderPg(){
		 StringBuilder pagingSelect = new StringBuilder();
		 pagingSelect.append(queryString+" order by "+orderString);
		 return pagingSelect.append(" limit ? offset ?").toString();
	}

	private String builderIq(){
		return getIqPre(this.columnString,orderString)+queryString+
		getIqEnd(paramCriteria.getStartIndex(),paramCriteria.getRowPerPage());
	}
	public String getQuerySql(){
		return this.queryString;
	}
	public PiTable getPiTable() {
		return piTable;
	}
	public void setPiTable(PiTable piTable) {
		this.piTable = piTable;
	}
	public List<PiTableDetail> getPiTableDetails() {
		return piTableDetails;
	}
	public void setPiTableDetails(List<PiTableDetail> piTableDetails) {
		this.piTableDetails = piTableDetails;
	}
	public ParamCriteria getParamCriteria() {
		return paramCriteria;
	}
	public void setParamCriteria(ParamCriteria paramCriteria) {
		this.paramCriteria = paramCriteria;
	}
	private  String getIqPre(String preFields,String order){
		return "select "+preFields+" from ( "+
			    "select a.*,row_number() over (order by "+order+") as rn  from (";
	}
	private  String getIqEnd(long start,int limit){
		return ") a ) b where b.rn >=? and b.rn<?";//+(start+limit);
	}

	public List<String> getReturnFields(){
		List<String> it=this.paramCriteria.getIntendedFields();
		if(it!=null&&it.size()>0){
			List<String> newC=new ArrayList<String>();
			for(String ii:it){
				for(String i2:this.columnAppendString){
					String iis[] =i2.split("@");
					if(ii.equals(iis[0])){
						newC.add(i2);
						break;
					}
				}
			}
			return newC;
		}
		return this.columnAppendString;
	}
	public List<Object> getQueryParam(){
		List<Object> objs=new ArrayList<Object>();
		objs.addAll(this.queryParams);
		if(paramCriteria.getSqlType().equalsIgnoreCase(Constants.TYPE_SQL_ORACLE)){
			objs.add(paramCriteria.getStartIndex()+paramCriteria.getRowPerPage());
			objs.add(paramCriteria.getStartIndex());
		}else if(paramCriteria.getSqlType().equalsIgnoreCase(DbType.mysql.name())||paramCriteria.getSqlType().equalsIgnoreCase(DbType.mariadb.name())){
			objs.add(paramCriteria.getStartIndex()-1);
			objs.add(paramCriteria.getRowPerPage());
		}else if(paramCriteria.getSqlType().equalsIgnoreCase(DbType.postgresql.name())){
			objs.add(paramCriteria.getRowPerPage());
			objs.add(paramCriteria.getStartIndex()-1);
		}
		return objs;
	}
	public List<Object> getQueryParamWithOutPage(){
		return this.queryParams;
	}
	public List<Map<String,String>> getPiKV() {
		List<Map<String,String>> datas=new ArrayList<Map<String,String>>();
		for(Map.Entry<String,String> entry:this.piKV.entrySet()){
			Map<String,String> data=new HashMap<String,String>();
			data.put("field",entry.getKey());
			data.put("head",entry.getValue());
			data.put("length",piMap.get(entry.getKey()).getLength());
			data.put("isQuery", String.valueOf(piMap.get(entry.getKey()).getIsQuery()));
			datas.add(data);
		}
		return datas;
	}
	
	private String buildStr(Operator op,String ft){
		String[] ss=op.getValue().split(op.getSplit());
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<ss.length;i++){
			sb.append("?");
			queryParams.add(handerFiledValue(ss[i], ft));
			if(i<ss.length-1){
				sb.append(",");
			}
		}
		return sb.toString();
	}
	public String[] getClumnFields(){
		if(paramCriteria.getIntendedFields()!=null&&paramCriteria.getIntendedFields().size()>0){
			return paramCriteria.getIntendedFields().toArray(new String[]{});
		}
		return columnString.split(",");
	}
	public String[] getHeadFields(){
		String[] cf=getClumnFields();
		String[] hf=new String[cf.length];
		for(int i=0;i<cf.length;i++){
			hf[i]=this.piKV.get(cf[i]);
		}
		return hf;
	}
	
	private Object handerFiledValue(String value,String filedType){
		if(filedType.contains("date")||filedType.contains("time")){//对于时间格式
			return DateTools.parse(value);
		}else{
			return value;
		}
	}

	public boolean isContainChart() {
		return containChart;
	}


	public void setContainChart(boolean containChart) {
		this.containChart = containChart;
	}


	public List<String> getColumnChartFields() {
		return columnChartString;
	}

	private String buildLike(){
		if(paramCriteria.getSqlType().equalsIgnoreCase(DbType.oracle.name())){
			return "'%'||?||'%'";
		}else{
			return "concat('%',?,'%')";
		}
	}

	
}
