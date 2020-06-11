package org.hw.sml.core.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hw.sml.model.SMLParams;
import org.hw.sml.support.el.El;
import org.hw.sml.tools.MapUtils;
import org.hw.sml.tools.RegexUtils;

public class SqlResolvers {
	
	private Map<String,SqlResolver> alias=MapUtils.newHashMap();
	public void register(String aliasName,SqlResolver sqlResolver){
		alias.put(aliasName,sqlResolver);
	}
	{
		register("if",new IfSqlResolver());
		register("foreach",new ForeachResolver());
		register("select",new SelectSqlResolver());
		register("jdbcType",new ParamTypeResolver());
		register("param",new ParamSqlResolver());
		register("collection",new CollectionResolver());
	}
	
	private El el;
	
	private List<SqlResolver> extResolvers=new ArrayList<SqlResolver>();
	
	private List<SqlResolver> sqlResolvers;
	public SqlResolvers(){

	}
	public SqlResolvers(El el){
		this.el=el;
	}
	public SqlResolvers init(){
		sqlResolvers=new ArrayList<SqlResolver>();
		sqlResolvers.add(alias.get("if"));
		sqlResolvers.add(alias.get("select"));
		sqlResolvers.add(alias.get("jdbcType"));
		sqlResolvers.add(alias.get("foreach"));
		sqlResolvers.add(alias.get("collection"));
		sqlResolvers.addAll(extResolvers);
		sqlResolvers.add(alias.get("param"));
		return this;
	}
	
	public SqlResolvers add(SqlResolver sqlResolver){
		sqlResolvers.add(sqlResolver);
		return this;
	}
	
	public  Rst resolverLinks(String sql,SMLParams smlParams){
		List<Object> paramsObject=new ArrayList<Object>();
		Map<String,Object> extInfo=MapUtils.newHashMap(); 
		List<SqlResolver> srs=sqlResolvers;
		if(sql.trim().startsWith("#resolvers")){
			String[] sqls=sql.split("\\)#",2);
			sql=sqls[1];
			String[] aliass=RegexUtils.subString(sqls[0]+")","(",")").split(",");
			srs=MapUtils.newArrayList();
			for(String al:aliass){
				if(!srs.contains(alias.get(al))&&alias.containsKey(al)){
					srs.add(alias.get(al));
				}
			}
			if(!srs.contains(alias.get("param"))){
				srs.add(alias.get("param"));
			}
		}
		for(SqlResolver sqlResolver:srs){
			sqlResolver.setEl(el);
			Rst subRst=sqlResolver.resolve(null, sql,smlParams);
			sql=subRst.getSqlString();
			if(subRst.getParamObjects()!=null&&subRst.getParamObjects().size()>0){
				paramsObject.addAll(subRst.getParamObjects());
			}
			if(subRst.getExtInfo()!=null&&subRst.getExtInfo().size()>0){
				extInfo.putAll(subRst.getExtInfo());
			}
		}
		return new Rst(sql,paramsObject).setExtInfo(extInfo);
	}


	public El getEl() {
		return el;
	}

	public void setEl(El el) {
		this.el = el;
	}

	public List<SqlResolver> getSqlResolvers() {
		return sqlResolvers;
	}

	public void setSqlResolvers(List<SqlResolver> sqlResolvers) {
		this.sqlResolvers = sqlResolvers;
	}

	
	public List<SqlResolver> getExtResolvers() {
		return extResolvers;
	}
	public void setExtResolvers(List<SqlResolver> extResolvers) {
		this.extResolvers = extResolvers;
	}
	
}
