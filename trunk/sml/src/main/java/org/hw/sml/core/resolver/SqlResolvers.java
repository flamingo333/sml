package org.hw.sml.core.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hw.sml.model.SMLParams;
import org.hw.sml.support.el.El;
import org.hw.sml.tools.MapUtils;

public class SqlResolvers {
	
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
		sqlResolvers.add(new IfSqlResolver());
		sqlResolvers.add(new SelectSqlResolver());
		sqlResolvers.add(new ForeachResolver());
		sqlResolvers.add(new ParamTypeResolver());
		sqlResolvers.addAll(extResolvers);
		sqlResolvers.add(new ParamSqlResolver());
		return this;
	}
	
	public SqlResolvers add(SqlResolver sqlResolver){
		sqlResolvers.add(sqlResolver);
		return this;
	}
	
	public  Rst resolverLinks(String sql,SMLParams smlParams){
		List<Object> paramsObject=new ArrayList<Object>();
		Map<String,Object> extInfo=MapUtils.newHashMap(); 
		for(SqlResolver sqlResolver:sqlResolvers){
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
