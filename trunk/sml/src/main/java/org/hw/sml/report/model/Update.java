package org.hw.sml.report.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hw.sml.core.IfIdNotException;
import org.hw.sml.tools.Assert;
import org.hw.sml.tools.DateTools;
import org.hw.sml.tools.MapUtils;
/**
 * 不依赖于配置的更新操作
 * @author wen
 *修复debug   如果修改字段写错时,进行校验
 */
public class Update extends Criteria {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8961222448940694098L;

	private String dbId;
	
	private String tableName;
	
	public static Update newUpdate(){
		return new Update();
	}
	public static Update newUpdate(String tableName){
		Update update=new Update();
		update.setTableName(tableName);
		return update;
	}
	public static Update newUpdate(String tableName,List<String> conditions){
		Update update=newUpdate(tableName);
		update.setConditions(conditions);
		return update;
	}
	public static Update newUpdate(String tableName,Map<String,Object> data){
		Update update=newUpdate(tableName);
		update.setData(data);
		return update;
	}
	public static Update newUpdate(String tableName,List<String> conditions,Map<String,Object> data){
		Update update=newUpdate(tableName, data);
		update.setConditions(conditions);
		return update;
	}
	
	
	private String type=Constants.TYPE_INSERT;
	
	private boolean quot=false;
	
	private String quotChar="`";
	
	public static boolean forceMapper=false;
	
	private List<Map<String,Object>> datas=new ArrayList<Map<String,Object>>();
	
	private Map<String,Object> data=new LinkedHashMap<String, Object>();
	
	private List<String> conditions=new ArrayList<String>();
	
	private static Map<String,String> tableMapping=MapUtils.newConcurrentHashMap();
	
	private boolean clearRefIf=false;
	
	private boolean isUpdate=false;
	private Set<String> keys;
	
	
	public static void registerTableMapping(String key,String value){
		tableMapping.put(key,value);
	}
	
	public String updateSqlForInsert(){
		StringBuffer sb=new StringBuffer("insert into "+tableName+"(");
		for(Map.Entry<String,Object> entry:data.entrySet()){
			//参数绑定
			String field=entry.getKey();
			if(field.startsWith("old.")){
				continue;
			}
			String[] fts=field.split("@");
			String ft=fts[0];
			ft=quotBuild(ft);
			sb.append(ft+",");
		}
		sb.deleteCharAt(sb.length()-1).append(") values(");
		for(int i=0;i<data.size();i++){
			sb.append("?,");
		}
		sb.deleteCharAt(sb.length()-1).append(")");
		return sb.toString();
	}
	public String updateSqlForUpdate(){
		Assert.isTrue(conditions.size()>0||!isUpdate,"不允许更新全表["+tableName+"]操作");
		StringBuffer sb=new StringBuffer("update "+tableName+" set ");
		for(Map.Entry<String,Object> entry:data.entrySet()){
			//参数绑定
			String field=entry.getKey();
			String[] fts=field.split("@");
			String ft=fts[0];
			if(conditions.contains(ft)){
				continue;
			}
			ft=quotBuild(ft);
			sb.append(ft+"=?,");
		}
		sb.deleteCharAt(sb.length()-1).append(" where 1=1");
		for(Map.Entry<String,Object> entry:data.entrySet()){
			String field=entry.getKey();
			String[] fts=field.split("@");
			String ft=fts[0];
			String[] no=ft.split("\\.");
			String cn=no.length==2?no[1]:no[0];
			if(conditions.contains(ft)){
				cn=quotBuild(cn);
				sb.append(" and "+cn+"=?");
			}
		}
		return sb.toString().replace("where 1=1 and", "where");
	}
	public String updateSqlForDelete(){
		StringBuffer sb=new StringBuffer("delete from "+tableName+" where 1=1");
		Assert.isTrue(data.size()>0||!type.equals(Constants.TYPE_DELETE),"不允许删除表["+tableName+"]操作");
		for(Map.Entry<String,Object> entry:data.entrySet()){
			//参数绑定
			String field=entry.getKey();
			String[] fts=field.split("@");
			String ft=fts[0];
			ft=quotBuild(ft);
			sb.append(" and "+ft+"=?");
		}
		return sb.toString().replace("where 1=1 and", "where");
	}
	public String updateSqlForAdu(boolean exists){
		if(exists){
			return updateSqlForUpdate();
		}else{
			return updateSqlForInsert();
		}
	}
	public void init(){
		if(datas.size()>0){
			data=datas.get(0);
		}else{
			if(data.size()>0)
				datas.add(data);
		}
		keys=data.keySet();
		isUpdate=Arrays.asList(Constants.TYPE_UPDATE,Constants.TYPE_ADU).contains(type);
		assertCondigion();
	} 
	public Update put(String name,Object value){
		data.put(name, value);
		return this;
	}
	public String getUpateSql(){
		init();
		if(type.equalsIgnoreCase(Constants.TYPE_INSERT)){
			return updateSqlForInsert();
		}else if(type.equalsIgnoreCase(Constants.TYPE_UPDATE)){
			return updateSqlForUpdate();
		}else if(type.equalsIgnoreCase(Constants.TYPE_DELETE)){
			return updateSqlForDelete();
		}
		return null;
	}
	public String isExistSql(){
		StringBuffer sb=new StringBuffer();
		sb.append("select count(1) from "+tableName+" where 1=1");
		for(Map.Entry<String,Object> entry:data.entrySet()){
			//参数绑定
			String field=entry.getKey();
			String[] fts=field.split("@");
			String ft=fts[0];
			if(conditions.contains(ft)){
				ft=ft.replace("old.","");
				ft=quotBuild(ft);
				sb.append(" and "+ft+"=?");
			}
		}
		return sb.toString().replace("where 1=1 and", "where");
	}
	public Object[] getExistParams(){
		List<Object> object=new ArrayList<Object>();
		for(Map.Entry<String,Object> entry:data.entrySet()){
			//参数绑定
			String field=entry.getKey();
			String[] fts=field.split("@");
			String ft=fts[0];
			if(conditions.contains(ft)){
				if(fts.length==1)
					object.add(entry.getValue());
				else
					object.add(DateTools.parse(String.valueOf(entry.getValue())));
			}
		}
		return object.toArray(new Object[]{});
	}
	
	private void assertCondigion() {
		//Assert.notNull(tableName,"tableName must not null!");
		boolean flag=true;
		if(isUpdate){
			for(String condition:conditions){
				boolean flag2=false;
				for(String key:data.keySet()){
					if(key.split("@")[0].equals(condition)){
						flag2=true;
						break;
					}
				}
				flag=flag2;
				Assert.isTrue(flag,"字段："+condition+"不存在!");
			}
		}
	}
	public List<Object[]> objectsForInsert(){
		List<Object[]> objects=new ArrayList<Object[]>();
		for(Map<String,Object> dt:datas){
			Object[] object=new Object[dt.size()];
			int i=0;
			for(String key:keys){
				Object value=dt.get(key);
				if(key.startsWith("old.")){
					continue;
				}
				String[] keyInfo=key.split("@");
				object[i]=value;
				if(keyInfo.length==2){
					//if(keyInfo[1].equals("date")){
						object[i]=DateTools.parse(String.valueOf(value));
					/*}else if(keyInfo[1].equals("seq")){
						object[i]=String.valueOf(System.currentTimeMillis());
					}else if(keyInfo[1].equals("uuid")){
						object[i]=String.valueOf(UUID.randomUUID().toString().replace("-",""));
					}*/
				}
				i++;
			}
			objects.add(object);
		}
		return objects;
	}
	public Object[] objectForAdu(boolean exists){
		if(exists){
			return objectsForUpdate().get(0);
		}else{
			return objectsForInsert().get(0);
		}
	}
	public List<Object[]> objectsForDelete(){
		List<Object[]> objects=new ArrayList<Object[]>();
		for(Map<String,Object> dt:datas){
			List<Object> object=new ArrayList<Object>();
			for(String key:keys){
				//参数绑定
				String field=key;
				Object value=dt.get(field);
				String[] fts=field.split("@");
				if(fts.length==1)
					object.add(value);
				else
					object.add(DateTools.parse(String.valueOf(value)));
			}
			objects.add(object.toArray(new Object[]{}));
		}
		return objects;
	}
	public List<Object[]> objectsForUpdate(){
		List<Object[]> objects=new ArrayList<Object[]>();
		for(Map<String,Object> dt:datas){
			List<Object> object=new ArrayList<Object>();
			for(Map.Entry<String,Object> entry:dt.entrySet()){
				String field=entry.getKey();
				String[] fts=field.split("@");
				String ft=fts[0];
				if(conditions.contains(ft)){
					continue;
				}
				if(fts.length==1)
					object.add(entry.getValue());
				else
					object.add(DateTools.parse(String.valueOf(entry.getValue())));
			}
			for(Map.Entry<String,Object> entry:dt.entrySet()){
				String field=entry.getKey();
				String[] fts=field.split("@");
				String ft=fts[0];
				if(conditions.contains(ft)){
					if(fts.length==1)
						object.add(entry.getValue());
					else
						object.add(DateTools.parse(String.valueOf(entry.getValue())));
				}
				
			}
			objects.add(object.toArray(new Object[]{}));
		}
		return objects;
	}
	public List<Object[]> getObjects(){
		if(type.equalsIgnoreCase(Constants.TYPE_INSERT)){
			return objectsForInsert();
		}else if(type.equalsIgnoreCase(Constants.TYPE_UPDATE)){
			return objectsForUpdate();
		}else if(type.equalsIgnoreCase(Constants.TYPE_DELETE)){
			return objectsForDelete();
		}
		return null;
	}
	
	public String getTableName() {
		return tableName;
	}
	public Update setTableName(String tableName) {
		if(forceMapper&&tableMapping.containsKey(tableName)){
			throw new IfIdNotException("tableName not exists!");
		}
		this.tableName = MapUtils.getString(tableMapping,tableName,tableName);
		return this;
	}
	public String getType() {
		return type;
	}
	public Update setType(String type) {
		this.type = type;
		return this;
	}
	public List<Map<String, Object>> getDatas() {
		return datas;
	}
	public Update setDatas(List<Map<String, Object>> datas) {
		this.datas = datas;
		return this;
	}
	public Map<String, Object> getData() {
		return data;
	}
	public Update setData(Map<String, Object> data) {
		this.data = data;
		return this;
	}
	public String getDbId() {
		return dbId;
	}
	public Update setDbId(String dbId) {
		this.dbId = dbId;
		return this;
	}
	
	public List<String> getConditions() {
		return conditions;
	}
	public void setConditions(List<String> conditions) {
		this.conditions = conditions;
	}

	public void setQuot(boolean quot) {
		this.quot = quot;
	}
	private String quotBuild(String ft){
		if(quot){
			return quotChar+ft+quotChar;
		}
		return ft;
	}

	public void setQuotChar(String quotChar) {
		this.quotChar = quotChar;
	}

	public static void setForceMapper(boolean forceMapper) {
		Update.forceMapper = forceMapper;
	}

	public void setClearRefIf(boolean clearRefIf) {
		this.clearRefIf = clearRefIf;
	}

	public boolean isClearRefIf() {
		return clearRefIf;
	}
	
	
	
}
