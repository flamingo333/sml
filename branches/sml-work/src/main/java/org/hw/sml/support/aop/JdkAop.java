package org.hw.sml.support.aop;

import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.hw.sml.tools.ClassUtil;

public class JdkAop implements Aop{
	@SuppressWarnings("unchecked")
	public <T> T newProxyInstance(Object proxyTarget, Aspect... aspect) {
		if(ClassUtil.getInterfaces(proxyTarget.getClass()).length==0){
			return (T) proxyTarget;
		}
		Aspects aspects=new Aspects();
    	aspects.setProxyTarget(proxyTarget);
    	if(aspect!=null)
    		aspects.setAspects(Arrays.asList(aspect));
    	T t= (T) Proxy.newProxyInstance(
                   Thread.currentThread().getContextClassLoader(), 
                   ClassUtil.getInterfaces(proxyTarget.getClass()), 
                   aspects);
    	return t;
	}

}
