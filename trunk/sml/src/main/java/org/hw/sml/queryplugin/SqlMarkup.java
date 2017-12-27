package org.hw.sml.queryplugin;

import java.util.List;
import java.util.Map;

import org.hw.sml.core.Rslt;
import org.hw.sml.jdbc.JdbcTemplate;
import org.hw.sml.model.SqlTemplate;
import org.hw.sml.plugin.Plugin;
import org.hw.sml.support.cache.CacheManager;


public interface  SqlMarkup extends Plugin{
	 static final String CACHE_PRE="jdbc";
	 CacheManager getCacheManager();
	 JdbcTemplate getJdbc(String dbid);
	 SqlTemplate getSqlTemplate(String id);
	 <T> List<T> querySql(SqlTemplate st);
	 Object builder(SqlTemplate st);
	 Rslt queryRslt(SqlTemplate st);
	 <T> List<T> querySql(String dbid,String sql,Map<String,String> params);
	 int update(SqlTemplate st);
	 Rslt queryRslt(String dbid,String sql,Map<String,String> params);
}
