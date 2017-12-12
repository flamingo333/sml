package org.hw.sml.core;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hw.sml.FrameworkConstant;
import org.hw.sml.jdbc.RowMapper;
import org.hw.sml.model.SqlTemplate;


public class SqlMarkupTemplate extends  SqlMarkupAbstractTemplate{	
	public SqlTemplate getSqlTemplate(String id){
		String key=CACHE_PRE+":"+id+":getSqlTemplate";
		if(getCacheManager().get(key)==null){
			SqlTemplate sqt= getSqlTemplateWithOutCache(id);
			getCacheManager().set(key,sqt,cacheMinutes);
		}
		SqlTemplate stp= ((SqlTemplate) getCacheManager().get(key)).clone();
		reInitSqlTemplate(stp);
		return stp;
	}
	
	private SqlTemplate getSqlTemplateWithOutCache(String id) {
		try{
			SqlTemplate sqt= getJdbc("defJt").queryForObject(FrameworkConstant.getSupportKey(frameworkMark,"CFG_JDBC_SQL"),new Object[]{id},new RowMapper<SqlTemplate>(){
				public SqlTemplate mapRow(ResultSet rs, int arg1)
						throws SQLException {
					SqlTemplate st=new SqlTemplate();
					st.setId(rs.getString("id"));
					st.setMainSql(rs.getString("mainsql"));
					String rebuildParam=rs.getString("rebuild_info");
					st.setRebuildInfo(rebuildParam);
					String conditionInfo=rs.getString("condition_info");
					st.setConditionInfo(conditionInfo);
					st.setIsCache(rs.getInt("cache_enabled"));
					st.setCacheMinutes(rs.getInt("cache_minutes"));
					st.setDbid(rs.getString("db_id"));
					return st;
				}
			});
			return sqt;
		}catch(Exception e){
			throw new IllegalArgumentException("ifId:["+id+"] not exists or can't get ifInfo from datasource! detail:["+e.getMessage()+"]");
		}
	}
	
	
	
}
