package org.hw.sml.support;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.hw.sml.support.clazz.AnnotationClassTemplate;
import org.hw.sml.support.clazz.ClassTemplate;
import org.hw.sml.support.clazz.SupperClassTemplate;

/**
 * 加载修改后builderclasses，包含新增
 * 这样可以实现服务不需要重启，可自由改变返回结果格式
 * @author hw
 */
public class ClassHelper {
	
	/**
	 * 动态加载jar包并加载class生成对象
	 * @param filepath
	 * @param className
	 * @return
	 * @throws Exception
	 */
	public static Object newInstance(String filepath,String className) throws Exception{
		URLClassLoader cl=new URLClassLoader(new URL[]{new URL(filepath)});
		return cl.loadClass(className).newInstance();
	}
	
	  /**
     * 获取指定包名中的所有类
     */
    public static List<Class<?>> getClassList(String packageName){
    	  return new ClassTemplate(packageName) {
              @Override
              public boolean checkAddClass(Class<?> cls) {
                  String className = cls.getName();
                  String pkgName = className.substring(0, className.lastIndexOf("."));
                  return pkgName.startsWith(packageName);
              }
          }.getClassList();
    }

    /**
     * 获取指定包名中指定注解的相关类
     */
    public static  List<Class<?>> getClassListByAnnotation(String packageName, Class<? extends Annotation> annotationClass){
    	 return new AnnotationClassTemplate(packageName, annotationClass) {
             public boolean checkAddClass(Class<?> cls) {
                 return cls.isAnnotationPresent(annotationClass);
             }
         }.getClassList();
    }

    /**
     * 获取指定包名中指定父类或接口的相关类
     */
    public static List<Class<?>> getClassListBySuper(String packageName, Class<?> superClass){
    	 return new SupperClassTemplate(packageName, superClass) {
             @Override
             public boolean checkAddClass(Class<?> cls) {
                 return superClass.isAssignableFrom(cls) && !superClass.equals(cls);
             }
         }.getClassList();
    }
	
}
