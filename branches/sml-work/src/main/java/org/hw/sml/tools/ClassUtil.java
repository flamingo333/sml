package org.hw.sml.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

public class ClassUtil {

    public static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
    public static String getClassPath() {
        String classpath = "";
        URL resource = getClassLoader().getResource("");
        if (resource != null) {
            classpath = resource.getPath();
        }
        return classpath;
    }
    public static Class<?> loadClass(String className) {
        return loadClass(className, true);
    }
    @SuppressWarnings("unchecked")
	public static <T> T newInstance(Class<T> t,Class<?>[] parameterTypes,Object[] paramsValues) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException{
    	@SuppressWarnings("rawtypes")
		Constructor[] cs= t.getConstructors();
    	for(Constructor<T> c:cs){
    		if(isAssignableFrom(c.getParameterTypes(),parameterTypes)){
    			return c.newInstance(paramsValues);
    		}
    	}
    	throw new IllegalArgumentException("not find constructor ["+t+"]");
    }
    /**
     * 加载类
     */
    public static Class<?> loadClass(String className, boolean isInitialized) {
        Class<?> cls;
        try {
            cls = Class.forName(className, isInitialized, getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return cls;
    }
    @SuppressWarnings("unchecked")
   	public static <T> T newInstance(String classpath,Class<T> clazz){
       	try {
   			return (T)loadClass(classpath).newInstance();
   		} catch (InstantiationException e) {
   			e.printStackTrace();
   		} catch (IllegalAccessException e) {
   			e.printStackTrace();
   		}
       	return null;
       }
    @SuppressWarnings("unchecked")
	public static <T> T newInstance(String classpath){
    	try {
			return (T) loadClass(classpath).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
    	return null;
    }
    public static boolean isShort(Class<?> type) {
        return type.equals(short.class) || type.equals(Short.class);
    }
    public static boolean isInt(Class<?> type) {
        return type.equals(int.class) || type.equals(Integer.class);
    }
    public static boolean isLong(Class<?> type) {
        return type.equals(long.class) || type.equals(Long.class);
    }
    public static boolean isDouble(Class<?> type) {
        return type.equals(double.class) || type.equals(Double.class);
    }
    public static boolean isFloat(Class<?> type) {
        return type.equals(float.class) || type.equals(Float.class);
    }
    public static boolean isString(Class<?> type) {
        return type.equals(String.class);
    }
    public static boolean isBoolean(Class<?> type){
    	return type.equals(boolean.class)||type.equals(Boolean.class);
    }
    public static boolean isChar(Class<?> type){
    	return type.equals(char.class)||type.equals(Character.class);
    }
    public static boolean isByte(Class<?> type){
    	return type.equals(byte.class)||type.equals(Byte.class);
    }
    public static boolean isAssignableFrom(Class<?> elSrc,Class<?> clsrc){
    	if(elSrc.isPrimitive()){
			return (isInt(elSrc)&&isInt(clsrc))||
					(isDouble(elSrc)&&isDouble(clsrc))||(isShort(elSrc)&&isShort(clsrc))||(isLong(elSrc)&&isLong(clsrc))||(isChar(elSrc)&&isChar(clsrc))||(isBoolean(elSrc)&&isBoolean(clsrc))||(isFloat(elSrc)&&isFloat(clsrc))||(isByte(elSrc)&&isByte(clsrc));
		}
    	return elSrc.equals(Object.class)||elSrc.isAssignableFrom(clsrc);
    }
    public static boolean isAssignableFrom(Class<?>[] elSrc,Class<?>[] clsrc){
    	if(clsrc==null&&elSrc==null){
    		return true;
    	}
    	if(clsrc==null){return false;}
    	if(elSrc==null){return false;}
    	if(clsrc.length==elSrc.length){
    		for(int i=0;i<clsrc.length;i++){
    			if(!isAssignableFrom(elSrc[i], clsrc[i])){
    				return false;
    			}
    		}
    	}else{
    		return false;
    	}
    	return true;
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object convertValueToRequiredType(Object value, Class requiredType) {
    	if(isInt(requiredType)){
    		requiredType=Integer.class;
    	}else if(isLong(requiredType)){
    		requiredType=Long.class;
    	}else if(isDouble(requiredType)){
    		requiredType=Double.class;
    	}else if(isFloat(requiredType)){
    		requiredType=Float.class;
    	}
    	if(isBoolean(requiredType)){
    		return Boolean.valueOf(String.valueOf(value));
    	}
		if (String.class.equals(requiredType)) {
			return value.toString();
		}
		else if (Number.class.isAssignableFrom(requiredType)) {
			if (value instanceof Number) {
				return NumberUtils.convertNumberToTargetClass(((Number) value), requiredType);
			}
			else {
				return NumberUtils.parseNumber(value.toString(), requiredType);
			}
		}else{
			return value;
		}
		
	}
    public static Field[] getFields(Class<?> clazz){
    	Field[] fields=clazz.getFields();
		Field[] dfs=clazz.getDeclaredFields();
		Field[] result=getFieldts(fields,dfs);
    	while(clazz.getSuperclass()!=null){
    		clazz=clazz.getSuperclass();
    		Field[] fs=clazz.getDeclaredFields();
    		Field[] ds=clazz.getDeclaredFields();
    		Field[] f=getFieldts(fs,ds);
    		result=getFieldts(result,f);
    	}
    	return result;
    }
    private static Field[]  getFieldts(Field[] fields,Field[] dfs){
    	Field[] result=new Field[fields.length+dfs.length];
    	for(int i=0;i<fields.length;i++){
    		result[i]=fields[i];
    	}
    	for(int i=fields.length;i<fields.length+dfs.length;i++){
    		result[i]=dfs[i-fields.length];
    	}
    	return result;
    }
    public static Method[] getMethods(Class<?> clazz){
    	List<Method> methods=MapUtils.newArrayList();
    	Method[] fields=clazz.getMethods();
    	Method[] dfs=clazz.getDeclaredMethods();
    	Method[] result=getMethodts(fields,dfs);
    	while(clazz.getSuperclass()!=null&&!clazz.getSuperclass().equals(Object.class)){
    		Method[] fs=clazz.getSuperclass().getDeclaredMethods();
    		Method[] ds=clazz.getSuperclass().getDeclaredMethods();
    		Method[] f=getMethodts(fs,ds);
    		result=getMethodts(result,f);
    		clazz=clazz.getSuperclass();
    	}
    	for(Method method:result){
    		if(!methods.contains(method))
    		methods.add(method);
    	}
    	return methods.toArray(new Method[]{});
    }
    private static Method[] getMethodts(Method[] fields,Method[] dfs){
    	Method[] result=new Method[fields.length+dfs.length];
    	for(int i=0;i<fields.length;i++){
    		result[i]=fields[i];
    	}
    	for(int i=fields.length;i<fields.length+dfs.length;i++){
    		result[i]=dfs[i-fields.length];
    	}
    	return result;
    }
    public static Method getMethod(Class<?> clazz,String name){
    	return getMethod(clazz, name,null);
    }
    public static Method getMethod(Class<?> clazz,String name,Class<?>[] pt){
    	if(clazz==null){
    		return null;
    	}
    	Method[] ms=clazz.getDeclaredMethods();
    	for(Method m:ms){
    		if(m.getName().equals(name)){
    			if(pt==null||(isAssignableFrom(m.getParameterTypes(), pt)))
    			return m;
    		}
    	}
    	ms=clazz.getMethods();
    	for(Method m:ms){
    		if(m.getName().equals(name)){
    			if(pt==null||(isAssignableFrom(m.getParameterTypes(), pt)))
    			return m;
    		}
    	}
    	return getMethod(clazz.getSuperclass(),name,pt);
    }
    public static Field getField(Class<?> clazz,String name){
    	if(clazz==null){
    		return null;
    	}
    	Field[] fs=clazz.getDeclaredFields();
    	for(Field f:fs){
    		if(f.getName().equals(name)){
    			return f;
    		}
    	}
    	fs=clazz.getFields();
    	for(Field f:fs){
    		if(f.getName().equals(name)){
    			return f;
    		}
    	}
    	return getField(clazz.getSuperclass(), name);
    }
    public  static boolean hasClass(String classPath){
    	try{
    		Class.forName(classPath);
    	}catch(Exception e){
    		return false;
    	}
    	return true;
    }
    public static Object getFieldValue(Object bean,String fieldName) throws IllegalArgumentException, IllegalAccessException{
    	if(bean.getClass().isArray()){
    		if(fieldName.equals("length")){
    			return ((Object[])bean).length;
    		}
    	}
    	Field field=getField(bean.getClass(),fieldName);
    	Assert.notNull(field,"bean ["+bean.getClass()+"]-"+fieldName+" not field!");
    	field.setAccessible(true);
    	return convertValueToRequiredType(field.get(bean),field.getType());
    }
    public static Object invokeMethod(Object bean,String methodName,Class<?>[] parameterTypes,Object[] paramValues) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
    	Method method=null;
    	try{
    		 method=bean.getClass().getMethod(methodName, parameterTypes);
    		 method.setAccessible(true);
    		return	method.invoke(bean,paramValues);
    	}catch(Exception e){
    		method=getMethod(bean.getClass(),methodName,parameterTypes);
    		method.setAccessible(true);
    		return method.invoke(bean,paramValues);
    	}
    	
    }
}
