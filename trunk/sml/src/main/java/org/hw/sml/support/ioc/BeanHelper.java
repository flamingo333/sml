package org.hw.sml.support.ioc;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hw.sml.FrameworkConstant;
import org.hw.sml.support.ClassHelper;
import org.hw.sml.support.LoggerHelper;
import org.hw.sml.support.aop.AbstractAspect;
import org.hw.sml.support.aop.MethodProxyFactory;
import org.hw.sml.support.el.BeanType;
import org.hw.sml.support.el.ElContext;
import org.hw.sml.support.el.ElException;
import org.hw.sml.support.el.SmlElContext;
import org.hw.sml.support.ioc.annotation.Bean;
import org.hw.sml.support.ioc.annotation.Init;
import org.hw.sml.support.ioc.annotation.Inject;
import org.hw.sml.support.ioc.annotation.Stop;
import org.hw.sml.support.ioc.annotation.Val;
import org.hw.sml.support.time.SchedulerPanner;
import org.hw.sml.support.time.annotation.Scheduler;
import org.hw.sml.tools.Assert;
import org.hw.sml.tools.ClassUtil;
import org.hw.sml.tools.MapUtils;
import org.hw.sml.tools.Strings;


@SuppressWarnings({ "unchecked", "rawtypes" })
public class BeanHelper {
	private BeanHelper(){}
	public  static final  String IOC_BEAN_SCAN="ioc-bean-scan";
	public static final String INIT_BEAN_ELP="elp-init-";
	private static  Map<String,Object> beanMap=MapUtils.newLinkedHashMap();
	private static  Map<String,Object> propertyInitBeanMap=MapUtils.newLinkedHashMap();
	private static Map<String,Boolean> beanErrInfo=MapUtils.newLinkedHashMap();
	private static ElContext smlElContext=new SmlElContext();
	private static PropertiesHelper propertiesHelper=new PropertiesHelper();
	public static final String KEY_BEAN_PREFIX="bean-";
	static{
		try {
			propertiesHelper.withProperties(FrameworkConstant.otherProperties).renameValue(KEY_BEAN_PREFIX).renameValue(KEY_BEAN_PREFIX);
			smlElContext.withBeanMap(beanMap).withProperties(propertiesHelper.getValues()).init();
			beanMap.put("smlBeanHelper", new BeanHelper());
			beanMap.put("smlPropertiesHelper",propertiesHelper);
			String packageName=getValue(IOC_BEAN_SCAN);
			List<Class<?>> classes=MapUtils.newArrayList();
			boolean isAnnotationScan=packageName!=null&&packageName.trim().length()>0;
			boolean isAopOpened=Boolean.valueOf(getValue("sml.aop.status"));
			if(isAnnotationScan){
				for(String pn:packageName.split(",| ")){
					List<Class<?>> cls=ClassHelper.getClassListByAnnotation(pn, Bean.class);
					classes.addAll(cls);
				}
			}
			//对属性文件bean读取解析
			for(Map.Entry<String,String> entry:propertiesHelper.getValuesByKeyStart(KEY_BEAN_PREFIX).entrySet()){
				String beanName=entry.getKey().replaceFirst(KEY_BEAN_PREFIX,"");
				Map<String,String> beanKeyValue=getBeanKeyValue(entry.getKey());
				String classpath=beanKeyValue.get("class");
				Assert.notNull(classpath,"bean["+beanName+"] class is null!");
				Assert.isTrue(!beanMap.containsKey(beanName),"bean["+beanName+"] name is conflict! ["+classpath+"]-["+(beanMap.get(beanName)!=null?beanMap.get(beanName).getClass().getName():"")+"]");
				Object bean=null;
				if(classpath.startsWith("[")&&classpath.endsWith("]")){
					classpath=classpath.substring(1,classpath.length()-1);
					bean=Array.newInstance(Class.forName(classpath),beanKeyValue.size()-1);
				}else if(classpath.endsWith(")")&&classpath.contains("(")){
					//通过构造初始化bean
					String clp=classpath.substring(0,classpath.indexOf("("));
					String clpBeanElp=classpath.substring(classpath.indexOf("(")+1,classpath.length()-1);
					String[] clpBeans=new Strings(clpBeanElp).splitToken(',','(',')');
					Object[] consts=new Object[clpBeans.length];
					Class<?>[] constCls=new Class<?>[clpBeans.length];
					for(int i=0;i<consts.length;i++){
						String keyP=clpBeans[i];
						BeanType b=smlElContext.evelBeanType(keyP);
						consts[i]=b.getV();
						constCls[i]=b.getC();
					}
					bean=ClassUtil.newInstance(Class.forName(clp), constCls, consts);
				}else{
					if(!Boolean.valueOf(beanKeyValue.get("passErr"))){
						bean=ClassUtil.newInstance(classpath);
					}else{
						try{
							bean=ClassUtil.newInstance(classpath);
						}catch(Exception e){
							e.printStackTrace();
							beanErrInfo.put(beanName,false);
						}
					}
				}
				beanMap.put(beanName,bean);
				propertyInitBeanMap.put(beanName,bean);
			}
			//查找所有Bean注解并生成对象
			if(isAnnotationScan){
				for(Class<?> clazz:classes){
					Bean bean=clazz.getAnnotation(Bean.class);
					String beanName=bean.value();
					if(new Strings(bean.value()).isEmpty()){
						beanName=new Strings(clazz.getSimpleName()).toLowerCaseFirst();
					}
					beanMap.put(beanName,clazz.newInstance());
				}
			}
			if(isAopOpened){
				//aop切面编程-1找出切面bean
				List<AbstractAspect> aspects=MapUtils.newArrayList();
				for(Map.Entry<String,Object> beans:beanMap.entrySet()){
					Object bean=beans.getValue();
					if(bean instanceof AbstractAspect){
						aspects.add((AbstractAspect)bean);
					}
				}
				//排序
				Collections.sort(aspects);
				for(Map.Entry<String,Object> beans:beanMap.entrySet()){
					Object bean=beans.getValue();
					if(!(bean instanceof AbstractAspect)){
						bean=MethodProxyFactory.newProxyInstance(bean, aspects.toArray(new AbstractAspect[]{}));
						beans.setValue(bean);
					}
				}
			}
			//初始化属性值
			for(Map.Entry<String,Object> entry:propertyInitBeanMap.entrySet()){
				String beanName=entry.getKey();
				Object bean=entry.getValue();
				if(beanErrInfo.containsKey(beanName)){
					continue;
				}
				//如果bean属于map类
				Map<String,String> pvs=getBeanKeyValue(beanName);
				int i=0;
				for(Map.Entry<String,String> et:pvs.entrySet()){
					String k=et.getKey();
					if(k.startsWith("p-")){
						String[] ktoken=getPorM(k);
						String fieldName=ktoken[1];
						String fieldType=ktoken[2];
						if(bean instanceof Map){
								((Map) bean).put(fieldName,getValue(fieldType,et.getValue()));
						}else if(bean instanceof List){
								((List) bean).add(getValue(fieldType,et.getValue()));
						}else if(bean.getClass().isArray()){
								Array.set(bean, i++,ClassUtil.convertValueToRequiredType(getValue(fieldType,et.getValue()),bean.getClass().getComponentType()));
						}else{
							Field field=ClassUtil.getField(bean.getClass(),fieldName);
							if(fieldType!=null&&!fieldType.startsWith("m"))
							Assert.notNull(field, "bean["+beanName+"-"+bean.getClass()+"] has not field["+fieldName+"]");
							if(field!=null)
							field.setAccessible(true);
							if(fieldType==null||fieldType.equals("v")||fieldType.equals("b")){
								Assert.notNull(getValue(fieldType,et.getValue()),beanName+"-property["+et.getValue()+"] is not configed!");
								Assert.notNull(field, "bean["+beanName+"-"+bean.getClass()+"] has no field["+fieldName+"]");
								Object value=ClassUtil.convertValueToRequiredType(getValue(fieldType,et.getValue()), field.getType());
								Assert.notNull(value, "bean["+beanName+"-"+bean.getClass()+"] has no field "+fieldType+"["+et.getValue()+"]");
								field.set(bean,value.equals("")?null:value);
							}else if(fieldType.equals("m")||fieldType.equals("mv")||fieldType.equals("mb")){
								String methodName="set"+new Strings(fieldName).toUpperCaseFirst();
								Method method=ClassUtil.getMethod(bean.getClass(),methodName);
								method.setAccessible(true);
								Assert.notNull(method, "bean["+beanName+"-"+bean.getClass()+"] has not method["+methodName+"] for field["+fieldName+"]!");
								Object value=ClassUtil.convertValueToRequiredType(getValue(fieldType.replace("m",""),et.getValue()),method.getGenericParameterTypes()[0].getClass());
								Assert.notNull(value, "bean["+beanName+"-"+bean.getClass()+"] has not method["+methodName+"] for field "+fieldType+" params["+et.getValue()+"]!");
								method.invoke(bean,value.equals("")?null:value);
							}
							else
								field.set(bean,ClassUtil.convertValueToRequiredType(et.getValue(), field.getType()));
						}
					}else if(k.startsWith("m-")){
						String[] ktoken=getPorM(k);
						String methodName=ktoken[1];
						String methodType=ktoken[2];
						Method method=ClassUtil.getMethod(bean.getClass(),methodName);
						Assert.notNull(method, "bean["+beanName+"-"+bean.getClass()+"] has no method["+methodName+"]!");
						Object value=ClassUtil.convertValueToRequiredType(getValue(methodType,et.getValue()),method.getGenericParameterTypes()[0].getClass());
						Assert.notNull(value,"bean["+beanName+"-"+bean.getClass()+"] method ["+methodName+"] for params "+methodType+"["+et.getValue()+"]");
						method.invoke(bean, value.equals("")?null:value);
					}
				}
			}
			//注解类字段进行注入或赋值
			if(isAnnotationScan){
				//查询所有字段inject进行赋值
				for(Class<?> clazz:classes){
					Bean bean=clazz.getAnnotation(Bean.class);
					String beanName=bean.value();
					if(new Strings(beanName).isEmpty())	beanName=new Strings(clazz.getSimpleName()).toLowerCaseFirst();
					//字段注入方式
					Field[] fields=ClassUtil.getFields(clazz);
					for(Field filed:fields){
						Inject inject=filed.getAnnotation(Inject.class);
						if(inject==null)	continue;
						String injectName=inject.value();
						Strings injectStrings=new Strings(injectName);
						if(injectStrings.isEmpty())	injectName=new Strings(filed.getType().getSimpleName()).toLowerCaseFirst();
						filed.setAccessible(true);
						Object v= beanMap.get(injectName)==null?beanMap.get(filed.getName()):beanMap.get(injectName);
						if(inject.required())
						Assert.notNull(v, "beanName:["+beanName+"-"+bean.getClass()+"],field inject ["+filed.getName()+"] v is null");
						if(v!=null)
						filed.set(beanMap.get(beanName),v.equals("")?null:v);
					}
					//方法注入方式
					Method[] methods=ClassUtil.getMethods(clazz);
					for(Method method:methods){
						Inject inject=method.getAnnotation(Inject.class);
						if(inject==null){
							continue;
						}
						String injectName=inject.value();
						Strings injectStrings=new Strings(injectName);
						if(injectStrings.isEmpty()){
							injectName=new Strings(method.getParameterTypes()[0].getSimpleName()).toLowerCaseFirst();
						}
						method.setAccessible(true);
						Object v=beanMap.get(injectName)==null?beanMap.get(method.getParameterTypes()[0]):beanMap.get(injectName);
						Assert.notNull(v, "beanName:["+beanName+"-"+bean.getClass()+"],method inject ["+method.getName()+" params ] v is null");
						method.invoke(beanMap.get(beanName),v);
					}
				}
				
				//@Val进行赋值
				for(Class<?> clazz:classes){
					Bean bean=clazz.getAnnotation(Bean.class);
					String beanName=bean.value();
					if(beanName==null||beanName.trim().length()==0){
						beanName=new Strings(clazz.getSimpleName()).toLowerCaseFirst();
					}
					//
					Field[] fields=ClassUtil.getFields(clazz);
					for(Field filed:fields){
						Val config=filed.getAnnotation(Val.class);
						if(config==null){
							continue;
						}
						String configName=config.value();
						Assert.notNull(configName, "beanName:"+beanName+"-"+bean.getClass()+",field config "+filed.getName()+" is null");
						filed.setAccessible(true);
						if(config.required())
						Assert.notNull(getValue(configName), "beanName:["+beanName+"-"+bean.getClass()+"],field value "+filed.getName()+" is null");
						if(getValue(configName)!=null)
						filed.set(beanMap.get(beanName),ClassUtil.convertValueToRequiredType(getValue(configName,config.isEvel()),filed.getType()));
					}
					//方法注入方式
					Method[] methods=ClassUtil.getMethods(clazz);
					for(Method method:methods){
						Val val=method.getAnnotation(Val.class);
						if(val==null){
							continue;
						}
						String configName=val.value();
						method.setAccessible(true);
						Assert.notNull(getValue(configName), "beanName:["+beanName+"-"+bean.getClass()+"],method param "+method.getName()+" is null");
						method.invoke(beanMap.get(beanName),ClassUtil.convertValueToRequiredType(getValue(configName,val.isEvel()),method.getParameterTypes()[0]));
					}
				}
			}
			//初始化属性文件配置中方法或注入关闭勾子
			for(Map.Entry<String,Object> entry:propertyInitBeanMap.entrySet()){
				String beanName=entry.getKey();
				if(beanErrInfo.containsKey(beanName)){
					continue;
				}
				final Object bean=entry.getValue();
				Map<String,String> pvs=getBeanKeyValue(beanName);
				for(final Map.Entry<String,String> et:pvs.entrySet()){
					String k=et.getKey();
					String methodName=et.getValue();
					if(k.equals("init-method")){
						final Method method=ClassUtil.getMethod(bean.getClass(),methodName);
						Assert.notNull(method, "bean["+beanName+"-"+bean.getClass()+"] has not init-method["+methodName+"]");
						method.setAccessible(true);
						boolean isDelay=Boolean.valueOf(pvs.get("isDelay"));
						LoggerHelper.getLogger().info(BeanHelper.class,"beanName["+beanName+"] init-method["+methodName+"] isDelay["+(isDelay?MapUtils.getString(pvs,"sleep","0")+"s":"false")+"]...");
						methodInvoke(bean, method, Boolean.valueOf(pvs.get("igErr")), isDelay,Long.parseLong(MapUtils.getString(pvs,"sleep","0")));
					}else if(k.equals("stop-method")||k.equals("destroy-method")){
						final Method method=ClassUtil.getMethod(bean.getClass(),methodName);
						Assert.notNull(method, "bean["+beanName+"-"+bean.getClass()+"] has not stop-method["+methodName+"]");
						method.setAccessible(true);
						Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
							public void run() {
								try {
									method.invoke(bean,new Object[]{});
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}));
					}
				}
			}
			//初始化注解方法
			if(packageName!=null&&packageName.trim().length()>0){
				//@Init方法
				initAnnotationInvoke(classes);
				//@Stop方法销毁
				for(Class<?> clazz:classes){
					Bean bean=clazz.getAnnotation(Bean.class);
					 String beanName=bean.value();
					if(beanName==null||beanName.trim().length()==0){
						beanName=new Strings(clazz.getSimpleName()).toLowerCaseFirst();
					}
					final String tempBean=beanName;
					for(final Method method:ClassUtil.getMethods(clazz)){
						Stop stop=method.getAnnotation(Stop.class);
						if(stop!=null){
							method.setAccessible(true);
							Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
								public void run() {
									try {
										method.invoke(beanMap.get(tempBean), new Object[]{});
									} catch (Exception e) {
										e.printStackTrace();
									} 
								}
							}));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} 
		boolean externalSchedulerPanner=getBean(SchedulerPanner.class)==null;
		if(externalSchedulerPanner){
			SchedulerPanner schedulerPanner=new SchedulerPanner();
			schedulerPanner.setConsumerThreadSize(MapUtils.getInt(propertiesHelper.getValues(),"sml.server.scheduler.consumerThreadSize",2));
			schedulerPanner.setDepth(MapUtils.getInt(propertiesHelper.getValues(),"sml.server.scheduler.depth",10000));
			schedulerPanner.setSkipQueueCaseInExecute(MapUtils.getBoolean(propertiesHelper.getValues(),"sml.server.scheduler.skipQueueCaseInExecute",true));
			beanMap.put("schedulerPanner",schedulerPanner);
		}
		//扫描注解类任务调度
		SchedulerPanner schedulerPanner=getBean(SchedulerPanner.class);
		for(Map.Entry<String,Object> beans:beanMap.entrySet()){
			if(beanErrInfo.containsKey(beans.getKey())){
				continue;
			}
			for(Method method:ClassUtil.getMethods(beans.getValue().getClass())){
				Scheduler scheduler=method.getAnnotation(Scheduler.class);
				if(scheduler==null) continue;
				schedulerPanner.getTaskMapContain().put("anno-"+beans.getKey()+"."+method.getName(),MapUtils.getString(propertiesHelper.getValues(),scheduler.value(),scheduler.value()));
			}
		}
		if(externalSchedulerPanner)
		schedulerPanner.init();
		LoggerHelper.getLogger().info(BeanHelper.class,"bean initd--->"+beanMap.keySet());
		//执行      sml.initElp.=
		for(Map.Entry<String,String> entry:propertiesHelper.getValuesByKeyStart(INIT_BEAN_ELP).entrySet()){
			try {
				evelV(entry.getValue());
			} catch (Exception e) {
				LoggerHelper.getLogger().error(BeanHelper.class,String.format("elp init [%s] error:[%s]",entry.getKey(),e.getMessage()));
			}
		}
	}
	public static Object evelV(String elp) throws ElException{
		return smlElContext.evel(elp);
	}
	
	public static void initAnnotationInvoke(List<Class<?>> classes) throws Exception{
		//@Init方法
		for(Class<?> clazz:classes){
			Bean bean=clazz.getAnnotation(Bean.class);
			String beanName=bean.value();
			if(beanName==null||beanName.trim().length()==0){
				beanName=new Strings(clazz.getSimpleName()).toLowerCaseFirst();
			}
			Method[] ms=ClassUtil.getMethods(clazz);
			List<String> initdMethod=MapUtils.newArrayList();
			for(Method method:ms){
				Init init=method.getAnnotation(Init.class);
				if(init!=null){
					method.setAccessible(true);
					if(initdMethod.contains(method.getName())){
						continue;
					}
					initdMethod.add(method.getName());
					methodInvoke(beanMap.get(beanName), method, init.igErr(), init.isDelay(), init.sleep());
				}
			}
		}
	}
	public static int start(){
		return start(new String[]{});
	}
	public static int start(String[] args){
		return 0;
	}
	public static <T> T getBean(String name){
		return (T)beanMap.get(name);
	}
	public static <T> T getBean(Class<T> t){
		for(Map.Entry<String,Object> entry:beanMap.entrySet()){
			Object val=entry.getValue();
			if(val.getClass().equals(t))
				return (T)val;
			if(t.isInstance(val))
				return (T)val;
		}
		return null;
	}
	public static <T> List<T> getBeans(Class<T> t){
		List<T> result=MapUtils.newArrayList();
		for(Map.Entry<String,Object> entry:beanMap.entrySet()){
			Object val=entry.getValue();
			if(val.getClass().equals(t))
				result.add((T)val);
			else if(t.isInstance(val))
				result.add((T)val);
		}
		return result;
	}
	public static String getValue(String key){
		return propertiesHelper.getValue(key);
	}
	public static Object getValue(String key,boolean isEvel) throws ElException{
		if(!isEvel)
			return propertiesHelper.getValue(key);
		else
			return evelV(propertiesHelper.getValue(key));
	}
	public static Object getValue(String type,String key) throws IllegalArgumentException, IllegalAccessException, ElException{
		if(type==null){
			
		}else if(type.equals("v")){
			return getValue(key);
		}else if(type.equals("b")){
			if(!beanErrInfo.containsKey(key))
				return smlElContext.getBean(key);
			else
				return "";
		}
		if(key.startsWith("${")&&key.endsWith("}")){
			return evelV(key);
		}else if(key.startsWith("#{")&&key.endsWith("}")){
			String keyElp=key.substring(2,key.length()-1);
			 if(!beanErrInfo.containsKey(keyElp))
				return smlElContext.evel(key);
			 else
			    return "";
		}
		return smlElContext.evel(key);
	}
	public static Map<String,String> getBeanKeyValue(String key){
		if(!key.startsWith(KEY_BEAN_PREFIX)){
			key=KEY_BEAN_PREFIX+key;
		}
		String value=getValue(key);
		Assert.notNull(value,key+" not found!");
		return MapUtils.transMapFromStr(value);
	}
	public static Map<String,Object> getBeanMap(){
		return beanMap;
	}
	private static String[] getPorM(String key){
		String[] pms= key.split("-");
		return new String[]{pms[0],pms[1],pms.length==3?pms[2]:null};
	}
	private static void methodInvoke(final Object bean,final Method method,boolean igErr,boolean isDelay,final long ms) throws Exception{
		if(isDelay){
			Thread thread=new Thread(new Runnable(){
				public void run() {
					try {
						Thread.sleep(ms*1000);
						method.invoke(bean,new Object[]{});
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}});
			thread.setName(bean.getClass().getSimpleName()+"."+method.getName());
			thread.start();
			LoggerHelper.getLogger().info(BeanHelper.class,"bean["+bean.getClass()+"]"+method.getName()+" lazy load sleep "+ms+" s!");
		}else{
			if(igErr){
				try {
					method.invoke(bean,new Object[]{});
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}else{
				method.invoke(bean,new Object[]{});
			}
		}
	}
	public static void main(String[] args) {
		BeanHelper.start(args);
	}
}