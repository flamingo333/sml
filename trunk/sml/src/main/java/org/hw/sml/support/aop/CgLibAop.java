package org.hw.sml.support.aop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class CgLibAop implements Aop{
	@SuppressWarnings("unchecked")
	public  <T> T newProxyInstance(final Object proxyTarget,final Aspect ... aspects) {
		return (T) net.sf.cglib.proxy.Enhancer.create(proxyTarget.getClass(), new net.sf.cglib.proxy.MethodInterceptor() {
			public Object intercept(final Object obj, Method method,final Object[] args,
					final net.sf.cglib.proxy.MethodProxy proxy) throws Throwable {
				Invocation invocation=new Invocation(proxyTarget, method, args){
					public Object invoke() throws Throwable {
						return proxy.invokeSuper(obj, args);
					}
				};
				Aspect[] newAspects=Aspects.filter(Arrays.asList(aspects), invocation);
				if(newAspects!=null){
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
				if(aspects!=null){
					for(Aspect aspect:newAspects){
						aspect.doAfter(invocation);
					}
				}
				return invocation.getValue();
			}
		});
    }
}
