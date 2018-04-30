package org.hw.sml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hw.sml.support.LoggerHelper;
import org.hw.sml.tools.ClassUtil;

public class FrameworkConstant {
	public static Map<String,String> smlCfgs=new HashMap<String,String>(){
		private static final long serialVersionUID = 7973549437067244525L;
		{
			put("CFG_JDBC_SQL", "select id,mainsql,rebuild_info,condition_info,cache_enabled,cache_minutes,db_id  from  DM_CO_BA_CFG_RCPT_IF where id=?");
			put("CFG_REPORT_SQL", "select id id,rcpt_name as tablename,name description,db_id from DM_CO_BA_CFG_RCPT where id=?");
			put("CFG_REPORT_DETAIL_SQL", "select rcpt_id as table_id,kpi_name_en as field_name,kpi_name_ch as field_name_zn,format,field_type,id as order_index,length,for_insert,for_update,for_import,for_import_update,for_query,is_query from DM_CO_BA_CFG_RCPT_DETAIL where rcpt_id=? and enabled=1 order by id");
			put("CFG_DEFAULT_BUILDER_CLASS", "org.hw.sml.core.build.lmaps");
			put("AUTHKEY", "5296D518F084D2B01DC1F360BE4DBFF1");
		}
	};
	public static String DEFAULT="default",VERSION="1.0",AUTHOR="huangwen";
	public static String CFG_JDBC_INFO="sml.properties";
	public static String PARAM_TOLOWERCASEFORKEY="toLowerCaseForKey",PARAM_SQLFORMAT="formatSql",PARAM_IGLOG="igLog",PARAM_QUERYTYPE="queryType",PARAM_FLUSHCACHE="FLUSHCACHE",PARAM_OPLINKS="opLinks",PARAM_ISREMOTEPRAMS="isRemoteParams",PARAM_FIELDFILTER="igFieldFilter",PARAM_SHOWSQL="showSql",PARAM_SHOWSQL_DEFAULTV="defaultValue",PARAM_RESULTMAP="resultMap",PARAM_FLUSHCACHEKEYS="flushCacheKeys";
	public static String getSupportKey(String type){
		return getSupportKey(DEFAULT, type);
	}
	public static String getSupportKey(String frameworkMark,String type){
		if(frameworkMark.equalsIgnoreCase(DEFAULT)){
			return smlCfgs.get(type);
		}else{
			return getProperty(frameworkMark+"."+type);
		}
	}
	public static Properties otherProperties=new Properties();
	static {
		try{
			InputStream is=ClassUtil.getClassLoader().getResourceAsStream(System.getProperty("smlProperties",CFG_JDBC_INFO));
			otherProperties.load(is);
			reset();
			String propertyFilesStr=otherProperties.getProperty("file-properties");
			String profile=otherProperties.getProperty("sml.profile.active");
			if(propertyFilesStr!=null){
				for(String file:propertyFilesStr.split(",")){
					String name=file;
					InputStream ist=null;
					try{
						name=getName(profile,file);
						ist=ClassUtil.getClassLoader().getResourceAsStream(name);
					}catch(Exception e){
					}finally{
						if(ist==null){
							name=file;
							ist=ClassUtil.getClassLoader().getResourceAsStream(name);
						}
					}
					otherProperties.load(ist);
					LoggerHelper.getLogger().info(FrameworkConstant.class,"load properties--->"+name);
				}
			}
			sysenv();
		}catch(Exception e){
		}
	}
	static void reset(){
		for(String key:smlCfgs.keySet()){
			String value=String.valueOf(getProperty(key));
			if(value==null||value.trim().length()==0||value.equals("null"))
				continue;
			smlCfgs.put(key, value);
			LoggerHelper.getLogger().info(FrameworkConstant.class,key+" is  reset used it --->["+value+"]");
		}
	}
	static void sysenv(){
		for(Map.Entry<Object,Object> entry:otherProperties.entrySet()){
			String key=entry.getKey().toString();
			if(key.startsWith("sml.vm.")){
				System.setProperty(key.toString(),entry.getValue().toString());
			}
		}
	}
	private static String getProperty(String key){
		String result=otherProperties.getProperty(key);
		return result==null?System.getProperty(key):result;
	}
	private static String getName(String profile,String name){
		if(profile==null||profile.length()==0){
			return name;
		}
		return name.substring(0,name.lastIndexOf("."))+"-"+profile+name.substring(name.lastIndexOf("."));
	}
	
}
