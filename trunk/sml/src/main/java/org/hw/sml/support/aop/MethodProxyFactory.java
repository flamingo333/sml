package org.hw.sml.support.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hw.sml.support.LoggerHelper;
import org.hw.sml.tools.ClassUtil;

public class MethodProxyFactory {
	private static Aop aop;
	private static boolean  iscglib=false;
	static{
		iscglib=ClassUtil.hasClass("net.sf.cglib.proxy.MethodInterceptor");
		if(!iscglib){
			aop=new JdkAop();
			LoggerHelper.getLogger().warn(MethodProxyFactory.class,"cglib not support!");
		}
		else
			aop=ClassUtil.newInstance("org.hw.sml.support.aop.CgLibAop");
	}
    @SuppressWarnings("unchecked")
	public static <T> T newInstance(Class<T> methodInterface,InvocationHandler invocationHandler) {
    	return (T) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(), 
                new Class[]{methodInterface}, 
                invocationHandler);
    }
    @SuppressWarnings("unchecked")
   	public static  <T> T newProxyInstance(Class<T> methodInterface,Object proxyTarget,Aspect ... aspect) {
    	Aspects aspects=new Aspects();
    	aspects.setProxyTarget(proxyTarget);
    	if(aspect!=null)
    		aspects.setAspects(Arrays.asList(aspect));
    	return (T) Proxy.newProxyInstance(
                   Thread.currentThread().getContextClassLoader(), 
                   new Class[]{methodInterface}, 
                   aspects);
    }
   	@SuppressWarnings("unchecked")
	public static <T> T newProxyInstance(Object proxyTarget,AbstractAspect ... aspects) {
   		if(proxyTarget.getClass().isPrimitive()||proxyTarget.getClass().isArray()||proxyTarget instanceof Collection||proxyTarget instanceof Number||proxyTarget instanceof Map||proxyTarget instanceof Aspect||aspects==null||aspects.length==0){
   			return (T)proxyTarget;
   		}
   		List<AbstractAspect> lst=new ArrayList<AbstractAspect>();
   		for(AbstractAspect aa:aspects){
   			if(aa.isPackageProxy(proxyTarget)){
   				lst.add(aa);
   			}
   		}
   		if(lst.isEmpty()){
   			return (T)proxyTarget;
   		}
	    T obj= aop.newProxyInstance(proxyTarget, lst.toArray(new AbstractAspect[]{}));
	    return obj;
    }
}
