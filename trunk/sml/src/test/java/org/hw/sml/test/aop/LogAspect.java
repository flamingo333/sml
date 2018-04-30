package org.hw.sml.test.aop;

import java.lang.reflect.InvocationTargetException;

import org.hw.sml.support.aop.AbstractAspect;
import org.hw.sml.support.aop.Invocation;
import org.hw.sml.support.ioc.annotation.Bean;

@Bean
public class LogAspect extends AbstractAspect{
	{
		setOrderId(1);
		setPackageMatchs("org.hw.sml.test.bean.*.*");
	}

	public void doBefore(Invocation invocation)  throws Throwable{
		invocation.getExtInfo().put("start",System.currentTimeMillis());
	}
	public void doAfter(Invocation invocation)  throws Throwable{
		Long start=(Long) invocation.getExtInfo().get("start");
		System.out.println(invocation.getTarget().getClass().getName()+"."+invocation.getMethod().getName()+"耗时："+(System.currentTimeMillis()-start));
	}
	@Override
	public  void doException(Invocation invocation) throws Throwable{
		Throwable e=invocation.getThrowable();
		if(e instanceof InvocationTargetException){
			e=((InvocationTargetException) e).getTargetException();
			System.out.println(e);
		}else{
			System.out.println(e);
		}
	}
}
