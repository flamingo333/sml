package com.eastcom_sw.inas.core.service.jdbc.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hw.sml.FrameworkConstant;

import com.eastcom_sw.inas.core.service.jdbc.RebuildParam;
import com.eastcom_sw.inas.core.service.jdbc.SqlTemplate;
import com.eastcom_sw.inas.core.service.jdbc.build.lmaps.AbstractDataBuilder;
import com.eastcom_sw.inas.core.service.support.ClassHelper;
import com.eastcom_sw.inas.core.service.support.JFContextUtils;
import com.eastcom_sw.inas.core.service.tools.MapUtils;
/**
 * 内置了几类数据参数形式，自己开发中常用到的数据格式
 * @author hw
 *后续通过classpath反射生成DataBuilder类实现需要数据
 */
public class DataBuilderHelper {
	public static Map<Integer,String> classType=new HashMap<Integer,String>();
	public static List<String> splitClass=new ArrayList<String>();
	static{
		classType.put(0,FrameworkConstant.CFG_DEFAULT_BUILDER_CLASS+".DefaultDataBuilder");
		classType.put(1,FrameworkConstant.CFG_DEFAULT_BUILDER_CLASS+".FieldDataBuilder");
		classType.put(2,FrameworkConstant.CFG_DEFAULT_BUILDER_CLASS+".GroupDataBuilder");
		classType.put(3,FrameworkConstant.CFG_DEFAULT_BUILDER_CLASS+".GroupFieldDataBuilder");
		classType.put(4,FrameworkConstant.CFG_DEFAULT_BUILDER_CLASS+".SingleDataBuilder");
		classType.put(5,FrameworkConstant.CFG_DEFAULT_BUILDER_CLASS+".Group2FieldDataBuilder");
		classType.put(6,FrameworkConstant.CFG_DEFAULT_BUILDER_CLASS+".OrderDataBuilder");
		
		classType.put(10,FrameworkConstant.CFG_DEFAULT_BUILDER_CLASS+".OpLinkDataBuilder");
		
		
		splitClass.add(FrameworkConstant.CFG_DEFAULT_BUILDER_CLASS+".PageSplitDataBuilder");
		splitClass.add(FrameworkConstant.CFG_DEFAULT_BUILDER_CLASS+".PageDataBuilder");
	}
	public static boolean isPageSplit(String classpath){
		if(classpath==null){
			return false;
		}
		if(!classpath.contains(".")){
			classpath=FrameworkConstant.CFG_DEFAULT_BUILDER_CLASS+"."+classpath;
		}
		return splitClass.contains(classpath);
	}
	public static Object build(RebuildParam rebuildParam,List<Map<String,Object>> datas,JFContextUtils jfContextUtils, SqlTemplate sqlTemplate){
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
		if(Boolean.valueOf(rebuildParam.get(FrameworkConstant.PARAM_FIELDFILTER)))
			datas=MapUtils.rebuildMp(datas,rebuildParam.getOriFields(),rebuildParam.getNewFields(), true);
		adm.setRebuildParam(rebuildParam);
		adm.setJfContextUtils(jfContextUtils);
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
				classpath=FrameworkConstant.CFG_DEFAULT_BUILDER_CLASS+"."+classpath;
			}
			return classpath;
		}
		return classType.get(rebuildParam.getType());
	}
	
}
