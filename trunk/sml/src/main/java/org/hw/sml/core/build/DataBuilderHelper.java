package org.hw.sml.core.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hw.sml.FrameworkConstant;
import org.hw.sml.context.SmlContextUtils;
import org.hw.sml.core.RebuildParam;
import org.hw.sml.core.build.lmaps.AbstractDataBuilder;
import org.hw.sml.model.SqlTemplate;
import org.hw.sml.support.ClassHelper;
import org.hw.sml.tools.MapUtils;
/**
 * 内置了几类数据参数形式，自己开发中常用到的数据格式
 * @author hw
 *后续通过classpath反射生成DataBuilder类实现需要数据
 */
public class DataBuilderHelper {
	public static Map<Integer,String> classType=new HashMap<Integer,String>();
	public static List<String> splitClass=new ArrayList<String>();
	static String classPathPreFix=FrameworkConstant.getSupportKey("CFG_DEFAULT_BUILDER_CLASS");
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
	public static Object build(RebuildParam rebuildParam,List<Map<String,Object>> datas,SmlContextUtils jfContextUtils, SqlTemplate sqlTemplate){
		AbstractDataBuilder adm=null;
		if(rebuildParam.getFilepath()!=null){
			try {
				adm=(AbstractDataBuilder)ClassHelper.newInstance(rebuildParam.getFilepath(),rebuildParam.getClasspath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			String rebuildPath=getClassPath(rebuildParam);
			try {
				adm=(AbstractDataBuilder) Class.forName(rebuildPath).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(Boolean.valueOf(rebuildParam.get(FrameworkConstant.PARAM_TOLOWERCASEFORKEY)))
			datas=MapUtils.toLowerCaseForKey(datas);
		if(Boolean.valueOf(rebuildParam.get(FrameworkConstant.PARAM_FIELDFILTER))){
			datas=MapUtils.rebuildMp(datas,rebuildParam.getOriFields(),rebuildParam.getNewFields(),true);
		}
		adm.setRebuildParam(rebuildParam);
		adm.setSmlContextUtils(jfContextUtils);
		adm.setSqlTemplate(sqlTemplate);
		return adm.build(datas);
	}
	public static Object build(RebuildParam rebuildParam,List<Map<String,Object>> datas){
		return build(rebuildParam, datas,null,null);
	}
	public static List<Map<String,Object>> unBuild(RebuildParam rebuildParam,Object datas){
		AbstractDataBuilder adm=null;
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
		if(classpath!=null){
			if(!classpath.contains(".")){
				classpath=classPathPreFix+"."+classpath;
			}
			return classpath;
		}
		return classType.get(rebuildParam.getType());
	}
	
}
