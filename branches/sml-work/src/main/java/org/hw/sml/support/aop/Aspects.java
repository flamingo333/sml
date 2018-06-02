package org.hw.sml.support.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Aspects implements InvocationHandler{
	private Object proxyTarget;
	private List<Aspect> aspects;
	
	public Object invoke(final Object proxy,final Method method,final Object[] args)
			throws Throwable {
		Invocation invocation=new Invocation(proxyTarget, method, args){
			public Object invoke() throws Throwable{
				return method.invoke(proxyTarget, args);
			}
		};
		Aspect[] newAspects=filter(aspects, invocation);
		if(newAspects!=null&&newAspects.length>0){
			for(Aspect aspect:newAspects){
				aspect.doBefore(invocation);
			}
		}
		invocation.proceed();
		if(invocation.getThrowable()!=null){
			if(newAspects!=null&&newAspects.length>0){
				for(Aspect aspect:newAspects){
					aspect.doException(invocation);
				}
			}
			if(invocation.getThrowable() instanceof InvocationTargetException)
				throw ((InvocationTargetException)invocation.getThrowable()).getTargetException();
			else
				throw invocation.getThrowable();
		}
		if(newAspects!=null&&newAspects.length>0){
			for(Aspect aspect:newAspects){
				aspect.doAfter(invocation);
			}
		}
		return invocation.getValue();
		
	}
	public Object getProxyTarget() {
		return proxyTarget;
	}
	public void setProxyTarget(Object proxyTarget) {
		this.proxyTarget = proxyTarget;
	}
	public List<Aspect> getAspects() {
		return aspects;
	}
	public void setAspects(List<Aspect> aspects) {
		this.aspects = aspects;
	}
	public static Aspect[] filter(List<Aspect> aspects,Invocation invocation) throws Throwable{
		List<Aspect> lst=new ArrayList<Aspect>();
		for(Aspect aspect:aspects){
			//System.out.println(invocation.getMethod().getName()+"+"+aspect.isProxy(invocation));
			if(aspect.isProxy(invocation)){
				lst.add(aspect);
			}
		}
		return lst.toArray(new Aspect[]{});
	}
}
