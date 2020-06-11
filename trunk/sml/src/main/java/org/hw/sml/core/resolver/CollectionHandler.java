package org.hw.sml.core.resolver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hw.sml.core.build.SmlTools;
import org.hw.sml.core.resolver.exception.ParamNotConfigException;
import org.hw.sml.jdbc.JdbcTemplate;
import org.hw.sml.support.SmlAppContextUtils;
import org.hw.sml.tools.MapUtils;
import org.hw.sml.tools.RegexUtils;

public class CollectionHandler {
	private String id;
	
	private Map<String,String> paramMapper=MapUtils.newHashMap();
	
	private String ref;
	
	private Map<String,Object> params=MapUtils.newHashMap();
	
	private String result="ori";
	
	private String dsId;
	
	private String sql;
	
	private boolean isOk=true;
	
	public CollectionHandler(String id,String ref,String result){
		this.id=id;
		this.ref=ref;
		String[] els=ref.split("\\[");
		if(els.length==2){
			dsId=els[0];
			String[] ps=els[1].replace("]","").split(",");
			for(String p:ps){
				String tp[]=p.split(":");
				paramMapper.put(tp[0],tp.length==2?tp[1]:tp[0]);
			}
		}else{
			isOk=false;
		}
		if(!Arrays.asList("ori","map","list","array").contains(result)){
			isOk=false;
		}
		this.result=result;
	}
	public CollectionHandler(String id,String ref,String result,String sql){
		this(id, ref, result);
		if(!Arrays.asList("map","list","array").contains(result)){
			isOk=false;
		}
		this.sql=sql.trim();
	}
	public String getId() {
		return id;
	}
	public String getRef() {
		return ref;
	}
	public String getResult() {
		return result;
	}
	public String getSql() {
		return sql;
	}
	public Map<String, String> getParamMapper() {
		return paramMapper;
	}
	public void setParamMapper(Map<String, String> paramMapper) {
		this.paramMapper = paramMapper;
	}
	public String getDsId() {
		return dsId;
	}
	public boolean isOk() {
		return isOk;
	}
	public boolean check(Map<String, Object> kvs, Map<String, Object> obj) {
		params.clear();
		for(String key:paramMapper.keySet()){
			Object value=obj.get(key);
			if(SmlTools.isEmpty(value)){
				value=kvs.get(key);
			}
			if(SmlTools.isNotEmpty(value)){
				params.put(paramMapper.get(key),value);
			}else{
				return false;
			}
		}
		return true;
	}
	private List<Object> paramObject=MapUtils.newArrayList();
	public void doIt(Map<String, Object> result) {
		paramObject.clear();
		if(sql==null){
			result.put(id,SmlAppContextUtils.getSmlContextUtils().query(dsId,SmlTools.rebuildSimpleKv(params)));
		}else{
			JdbcTemplate jt=SmlAppContextUtils.getSmlContextUtils().getJdbc(dsId);
			String sqlT=sql;
			List<String> rep=RegexUtils.matchGroup(":\\w+",sqlT);
			for(String re:rep){
				String p=re.substring(1);
				if(!params.containsKey(p)){
					throw new ParamNotConfigException(re+" not found!");
				}
				Object obj=params.get(p);
				if(obj instanceof List){
					paramObject.addAll((List)obj);
					sqlT=sqlT.replace(re,re(((List)obj).size()));
				}else{
					paramObject.add(obj);
					sqlT=sqlT.replace(re,"?");
				}
			}
			//LoggerHelper.getLogger().debug(getClass(),"collection:["+sql+"]"+paramObject);
			if(this.result.equals("list")){
				result.put(id,jt.queryForList(sqlT,paramObject.toArray(new Object[]{})));
			}else if(this.result.equals("map")){
				result.put(id,jt.queryForMap(sqlT,paramObject.toArray(new Object[]{})));
			}else if(this.result.equals("array")){
				result.put(id,jt.queryForList(sqlT,String.class,paramObject.toArray(new Object[]{})));
			}
		}
	}
	
	private String re(int size){
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<size;i++){
			sb.append("?,");
		}
		return sb.deleteCharAt(sb.length()-1).toString();
	}
	public Map<String, Object> getParams() {
		return params;
	}
}
