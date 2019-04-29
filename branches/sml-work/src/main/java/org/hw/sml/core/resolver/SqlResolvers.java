package org.hw.sml.core.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hw.sml.support.el.El;
import org.hw.sml.tools.MapUtils;
import org.hw.sml.tools.RegexUtils;

import com.eastcom_sw.inas.core.service.jdbc.SqlParams;

public class SqlResolvers {
	private Map<String,SqlResolver> alias=MapUtils.newHashMap();
	{
		alias.put("if",new IfSqlResolver());
		alias.put("foreach",new ForeachResolver());
		alias.put("select",new SelectSqlResolver());
		alias.put("jdbcType",new ParamTypeResolver());
		alias.put("param",new ParamSqlResolver());
	}
	
	private El el;
	
	private List<SqlResolver> extResolvers=new ArrayList<SqlResolver>();
	
	private List<SqlResolver> sqlResolvers;
	public SqlResolvers(El el){
		this.el=el;
	}
	public void init(){
		sqlResolvers=new ArrayList<SqlResolver>();
		sqlResolvers.add(alias.get("if"));
		sqlResolvers.add(alias.get("select"));
		sqlResolvers.addAll(extResolvers);
		sqlResolvers.add(alias.get("jdbcType"));
		sqlResolvers.add(alias.get("foreach"));
		sqlResolvers.add(alias.get("param"));
	}
	
	public void add(SqlResolver sqlResolver){
		sqlResolvers.add(sqlResolver);
	}
	
	public synchronized  Rst  resolverLinks(String sql,SqlParams sqlParams){
		//#resolvers=(if,select,jdbcType,foreach,param)#
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
		List<Object> paramsObject=new ArrayList<Object>();
		for(SqlResolver sqlResolver:srs){
			sqlResolver.setEl(el);
			Rst subRst=sqlResolver.resolve(null, sql,sqlParams);
			sql=subRst.getSqlString();
			if(subRst.getParamObjects()!=null&&subRst.getParamObjects().size()>0){
				paramsObject.addAll(subRst.getParamObjects());
			}
		}
		return new Rst(sql,paramsObject);
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
