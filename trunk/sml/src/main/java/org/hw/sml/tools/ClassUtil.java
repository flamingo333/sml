package org.hw.sml.tools;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    public static Class<?>[] getInterfaces(Class<?> targetClass){
    	if(targetClass.getInterfaces().length==0){
    		Class<?> superC=targetClass.getSuperclass();
    		if(superC==null){
    			return targetClass.getInterfaces();
    		}else{
    			return getInterfaces(superC);
    		}
    	}else{
    		return targetClass.getInterfaces();
    	}
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
    public static boolean isDate(Class<?> type){
    	return Date.class.isAssignableFrom(type);
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
    public static boolean isSingleType(Class<?> type){
    	return type.isPrimitive()||isInt(type)||isShort(type)||isLong(type)||isFloat(type)||isByte(type)||isString(type)||isDouble(type)||isDate(type)||Number.class.isAssignableFrom(type)||isChar(type)||isBoolean(type);
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
    	if(value==null){
    		return null;
    	}
    	if(isInt(requiredType)){
    		requiredType=Integer.class;
    	}else if(isShort(requiredType)){
    		requiredType=Short.class;
    	}else if(isLong(requiredType)){
    		requiredType=Long.class;
    	}else if(isDouble(requiredType)){
    		requiredType=Double.class;
    	}else if(isFloat(requiredType)){
    		requiredType=Float.class;
    	}
    	if (String.class.equals(requiredType)) {
			return value.toString();
		}else if (Number.class.isAssignableFrom(requiredType)) {
			if (value instanceof Number) {
				return NumberUtils.convertNumberToTargetClass(((Number) value), requiredType);
			}
			else {
				return NumberUtils.parseNumber(value.toString(), requiredType);
			}
		}else if(isChar(requiredType)){
    		if(value.toString().length()>0)
    			return String.valueOf(value).charAt(0);
    		else 
    			return null;
    	}else if(isBoolean(requiredType)){
    		return Boolean.valueOf(String.valueOf(value));
    	}else if(isDate(requiredType)&&(value instanceof Date)){
    		Date tv=(Date)value;
    		if(requiredType.equals(Timestamp.class)){
    			return new Timestamp(tv.getTime());
    		}else if(requiredType.equals(Time.class)){
    			return new Time(tv.getTime());
    		}
    		return tv;
    	}else{
			return value;
		}
	}
    public static Field[] getFields(Class<?> clazz){
    	Field[] result=clazz.getDeclaredFields();
    	while(clazz.getSuperclass()!=null){
    		clazz=clazz.getSuperclass();
    		result=getFieldts(result,getFields(clazz));
    	}
    	return result;
    }
    public static Field[] getPubFields(Class<?> clazz){
    	Field[] result=clazz.getFields();
    	if(clazz.getSuperclass()!=null){
    		clazz=clazz.getSuperclass();
    		result=getFieldts(result,getPubFields(clazz));
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
    	Method[] result=clazz.getDeclaredMethods();
    	if(clazz.getSuperclass()!=null&&!clazz.getSuperclass().equals(Object.class)){
    		result=getMethodts(result,getMethods(clazz.getSuperclass()));
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
    	return getField(clazz.getSuperclass(), name);
    }
    public static void injectFieldValue(Object obj,String fieldName,Object value) throws Exception{
    	Field field=getField(obj.getClass(),fieldName);
    	field.setAccessible(true);
    	field.set(obj,convertValueToRequiredType(value,(Class<?>)field.getGenericType()));
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
    			return Array.getLength(bean);
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
    		try{
	    		method=getMethod(bean.getClass(),methodName,parameterTypes);
	    		method.setAccessible(true);
	    		return method.invoke(bean,paramValues);
    		}catch(Exception e2){
    			if(methodName.startsWith("set")||methodName.startsWith("is")){
    				String filed=new Strings(methodName.substring(methodName.startsWith("set")?3:2)).toLowerCaseFirst();
    				try {
						injectFieldValue(bean, filed, paramValues[0]);
					} catch (Exception e1) {
						throw new NoSuchMethodException(e1.getMessage());
					}
    				return null;
    			}else{
    				throw new NoSuchMethodException();
    			}
    		}
    	}
    }
    public static <T,V> T mapToBean(Map<String,V> c,Class<T> t) throws Exception{
    	T bean=t.newInstance();
    	return mapToBean(c,bean);
    }
    public static <T,V> T mapToBean(Map<String,V> c,T bean) throws Exception{
    	if(!(c instanceof LinkedCaseInsensitiveMap)){
    		Map<String,V> t=new LinkedCaseInsensitiveMap<V>();
    		t.putAll(c);
    		c=t;
    	}
    	Field[] fields=getFields(bean.getClass());
    	for(Field field:fields){
    		if(Modifier.toString(field.getModifiers()).contains("static")||c.get(field.getName())==null){
    			continue;
    		}
    		field.setAccessible(true);
    		field.set(bean,convertValueToRequiredType(c.get(field.getName()),field.getType()));
    	}
    	return bean;
    }
    public static Map<String,Object> beanToMap(Object bean) throws Exception{
    	Map<String,Object> result=MapUtils.newHashMap();
    	for(Field field:getFields(bean.getClass())){
    		if(Modifier.toString(field.getModifiers()).contains("static")){
    			continue;
    		}
    		field.setAccessible(true);
    		result.put(field.getName(),field.get(bean));
    	}
    	return result;
    }
    public static Method getSetMethod(Class<?> clazz,String name){
    	Method method=getMethod(clazz,"set"+new Strings(name).toUpperCaseFirst());
    	if(method==null&&name.startsWith("is")){
    			method=getMethod(clazz, "set"+new Strings(name.replaceFirst("is", "")).toUpperCaseFirst());
    	}
    	return method;
    }
}
