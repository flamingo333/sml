package org.hw.sml;

import java.io.InputStream;
import java.util.Properties;

import org.hw.sml.support.LoggerHelper;

public class FrameworkConstant {
	public static enum Type{
		FRAMEWORK_CFG_JDBC_SQL,
		FRAMEWORK_CFG_REPORT_SQL,
		FRAMEWORK_CFG_REPORT_DETAIL_SQL,
		FRAMEWORK_CFG_DEFAULT_BUILDER_CLASS
	}
	
	public static String CFG_JDBC_INFO="sml.properties";
	public static final String AUTHOR="huangwen";
	public static final String AUTHKEY="5296D518F084D2B01DC1F360BE4DBFF1";
	public static String CFG_JDBC_SQL="select id,mainsql,rebuild_info jsonrebuildparammap,condition_info jsonconditionmap,cache_enabled iscache,cache_minutes cacheminutes,db_id dbid from  DM_CO_BA_CFG_RCPT_IF where id=?";
	public static String CFG_REPORT_SQL="select id id,rcpt_name as tablename,name description,db_id from DM_CO_BA_CFG_RCPT where id=?";
	public static String CFG_REPORT_DETAIL_SQL="select rcpt_id as table_id,kpi_name_en as field_name,kpi_name_ch as field_name_zn,format,field_type,id as order_index,length,for_insert,for_update,for_import,for_import_update,for_query,is_query from DM_CO_BA_CFG_RCPT_DETAIL where rcpt_id=? and enabled=1 order by id";
	public static String CFG_DEFAULT_BUILDER_CLASS="com.eastcom_sw.inas.core.service.jdbc.build.lmaps";
	public static final String PARAM_TOLOWERCASEFORKEY="toLowerCaseForKey";
	public static final String PARAM_SQLFORMAT="formatSql";
	public static final String PARAM_IGLOG="igLog";
	public static final String PARAM_QUERYTYPE="queryType";
	public static final String PARAM_FLUSHCACHE="FLUSHCACHE";
	public static final String PARAM_OPLINKS="opLinks";
	public static final String PARAM_ISREMOTEPRAMS="isRemoteParams";
	public static final String PARAM_FIELDFILTER="igFieldFilter";
	public static boolean isExit=false;
	public static String getSupportKey(String frameworkMark,Type type){
		if(type.equals(Type.FRAMEWORK_CFG_JDBC_SQL)){
			if(frameworkMark.equals("default")){
				return CFG_JDBC_SQL;
			}else{
				return properties.getProperty(frameworkMark+".CFG_JDBC_SQL");
			}
		}else if(type.equals(Type.FRAMEWORK_CFG_REPORT_SQL)){
			if(frameworkMark.equals("default")){
				return CFG_REPORT_SQL;
			}else{
				return properties.getProperty(frameworkMark+".CFG_REPORT_SQL");
			}
		}else if(type.equals(Type.FRAMEWORK_CFG_REPORT_DETAIL_SQL)){
			if(frameworkMark.equals("default")){
				return CFG_REPORT_DETAIL_SQL;
			}else{
				return properties.getProperty(frameworkMark+".CFG_REPORT_DETAIL_SQL");
			}
		}else{
			return null;
		}
	}
	public static Properties properties=new Properties();
	public static Properties otherProperties=new Properties();
	static {
		try{
			InputStream is=FrameworkConstant.class.getClassLoader().getResourceAsStream(CFG_JDBC_INFO);
			properties.load(is);
			reset("CFG_JDBC_SQL");
			reset("CFG_REPORT_SQL");
			reset("CFG_REPORT_DETAIL_SQL");
			reset("CFG_DEFAULT_BUILDER_CLASS");
		}catch(Exception e){
			LoggerHelper.getLogger().error(FrameworkConstant.class,"FrameworkConstant can't found, use default config !");
		}
		try{
			InputStream is=FrameworkConstant.class.getClassLoader().getResourceAsStream(CFG_JDBC_INFO);
			otherProperties.load(is);
			String propertyFilesStr=properties.getProperty("file-properties");
			if(propertyFilesStr!=null){
				for(String file:propertyFilesStr.split(",")){
					otherProperties.load(FrameworkConstant.class.getClassLoader().getResourceAsStream(file));
					LoggerHelper.getLogger().info(FrameworkConstant.class,"load properties--->"+file);
				}
			}
		}catch(Exception e){
		}
	}
	static void reset(String key){
		String value=String.valueOf(getProperty(key));
		if(value==null||value.trim().length()==0||value.equals("null")){
			return;
		}
		if(key.equals("CFG_JDBC_SQL")){
			CFG_JDBC_SQL=value;
		}else if(key.equals("CFG_REPORT_SQL")){
			CFG_REPORT_SQL=value;
		}else if(key.equals("CFG_REPORT_DETAIL_SQL")){
			CFG_REPORT_DETAIL_SQL=value;
		}else if(key.equals("CFG_DEFAULT_BUILDER_CLASS")){
			CFG_DEFAULT_BUILDER_CLASS=value;
		}
		LoggerHelper.getLogger().warn(FrameworkConstant.class,key+" is  reset used it --->["+value+"]");
	}
	public static String getProperty(String key){
		String result=null;
		result=properties.getProperty(key);
		if(result==null)
			result=otherProperties.getProperty(key);
		return result;
	}
}
