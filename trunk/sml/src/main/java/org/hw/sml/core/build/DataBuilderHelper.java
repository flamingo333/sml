package org.hw.sml.core.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hw.sml.FrameworkConstant;
import org.hw.sml.context.SmlContextUtils;
import org.hw.sml.core.RebuildParam;
import org.hw.sml.core.build.lmaps.AbstractDataBuilder;
import org.hw.sml.core.build.lmaps.DefaultDataBuilder;
import org.hw.sml.model.SqlTemplate;
import org.hw.sml.support.ClassHelper;
import org.hw.sml.tools.ClassUtil;
import org.hw.sml.tools.MapUtils;
/**
 * 内置了几类数据参数形式，自己开发中常用到的数据格式
 * @author hw
 *后续通过classpath反射生成DataBuilder类实现需要数据
 */
public class DataBuilderHelper {
	private static Map<String,BuilderFactory> buildFactorys=MapUtils.newHashMap();
	public static Map<Integer,String> classType=new HashMap<Integer,String>();
	public static List<String> splitClass=new ArrayList<String>();
	static String classPathPreFix=FrameworkConstant.getSupportKey("CFG_DEFAULT_BUILDER_CLASS");
    static AbstractDataBuilder DEFAULT=new DefaultDataBuilder();
    public static void registerBuildFactory(String schema,BuilderFactory builderFactory){
    	if(buildFactorys.containsKey(schema)){
			throw new RuntimeException("conflict buildFactory exists schema["+schema+"], current is["+buildFactorys.get(schema).getClass()+"]!");
    	}
    	buildFactorys.put(schema,builderFactory);
    }
	static{
		classType.put(0,classPathPreFix+".DefaultDataBuilder");
		classType.put(1,classPathPreFix+".FieldDataBuilder");
		classType.put(2,classPathPreFix+".GroupDataBuilder");
		classType.put(3,classPathPreFix+".GroupFieldDataBuilder");
		classType.put(4,classPathPreFix+".SingleDataBuilder");
		classType.put(5,classPathPreFix+".Group2FieldDataBuilder");
		classType.put(6,classPathPreFix+".OrderDataBuilder");
		
		
		splitClass.add(classPathPreFix+".PageSplitDataBuilder");
		splitClass.add(classPathPreFix+".PageDataBuilder");
	}
	public static boolean isPageSplit(String classpath){
		if(classpath==null){
			return false;
		}
		if(!classpath.contains(".")){
			classpath=classPathPreFix+"."+classpath;
		}
		return splitClass.contains(classpath);
	}
	@SuppressWarnings("unchecked")
	public static <T> Object build(RebuildParam rebuildParam,List<T> datas,SmlContextUtils jfContextUtils, SqlTemplate sqlTemplate){
		AbstractDataBuilder adm=DEFAULT;
		//if(datas.isEmpty()||!(datas.get(0) instanceof Map)){
		//	return datas;
		//}
		if(rebuildParam.getFilepath()!=null){
			try {
				adm=(AbstractDataBuilder)ClassHelper.newInstance(rebuildParam.getFilepath(),rebuildParam.getClasspath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(!buildFactorys.isEmpty()&&rebuildParam.getClasspath().contains(":")){
			String[] cls=rebuildParam.getClasspath().split(":",2);
			BuilderFactory builderFactory=buildFactorys.get(cls[0]);
			if(builderFactory!=null){
				AbstractDataBuilder at=builderFactory.getBuilder(cls[1]);
				if(at!=null){
					adm=at;
				}
			}
		}else{
			String rebuildPath=getClassPath(rebuildParam);
			try {
				adm=(AbstractDataBuilder) Class.forName(rebuildPath).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(rebuildParam.get(FrameworkConstant.PARAM_TOCASEFORKEY)!=null){
			datas=(List<T>)MapUtils.toCaseForKey((List<Map<String,Object>>)datas,rebuildParam.get(FrameworkConstant.PARAM_TOCASEFORKEY));
		}else{
			if(Boolean.valueOf(rebuildParam.get(FrameworkConstant.PARAM_TOLOWERCASEFORKEY)))
				datas=(List<T>) MapUtils.toLowerCaseForKey((List<Map<String,Object>>)datas);
		}
		if(Boolean.valueOf(rebuildParam.get(FrameworkConstant.PARAM_FIELDFILTER))){
			datas=(List<T>) MapUtils.rebuildMp((List<Map<String,Object>>)datas,rebuildParam.getOriFields(),rebuildParam.getNewFields(),true);
		}
		adm.setRebuildParam(rebuildParam);
		adm.setSmlContextUtils(jfContextUtils);
		adm.setSqlTemplate(sqlTemplate);
		Object value= adm.build((List<Map<String,Object>>)datas);
		if(rebuildParam.get("valueHandler")!=null){
			try{
			value=((ValueHandler)ClassUtil.newInstance(rebuildParam.get("valueHandler"))).handler(value,rebuildParam);
			}catch(Exception e){e.printStackTrace();}
		}
		return value;
	}
	public static <T> Object build(RebuildParam rebuildParam,List<T> datas){
		return build(rebuildParam, datas,null,null);
	}
	public static List<Map<String,Object>> unBuild(RebuildParam rebuildParam,Object datas){
		AbstractDataBuilder adm=DEFAULT;
		String rebuildPath=getClassPath(rebuildParam);
		try {
			adm=(AbstractDataBuilder) Class.forName(rebuildPath).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		adm.setRebuildParam(rebuildParam);
		return adm.unBuild(datas);
	}
	
	
	
	public static String getClassPath(RebuildParam rebuildParam){
		String classpath=rebuildParam.getClasspath();
		if(SmlTools.isNotEmpty(classpath)){
			if(!classpath.contains(".")){
				classpath=classPathPreFix+"."+classpath;
			}
			return classpath;
		}
		return classType.get(rebuildParam.getType());
	}
	
}
