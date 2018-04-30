package org.hw.sml.test.aop;

import org.hw.sml.support.aop.AbstractAspect;
import org.hw.sml.support.aop.Invocation;
import org.hw.sml.support.ioc.annotation.Bean;

@Bean
public class LogAspect1 extends AbstractAspect{
	{
		setOrderId(2);
		setPackageMatchs("org.hw.sml.test.bean.A.(test2|test3)");
	}

	public void doBefore(Invocation invocation)  throws Throwable{
		invocation.getExtInfo().put("start",System.currentTimeMillis());
	}
	public void doAfter(Invocation invocation)  throws Throwable{
		Long start=(Long) invocation.getExtInfo().get("start");
		System.out.println(invocation.getTarget().getClass().getName()+"."+invocation.getMethod().getName()+"耗时2："+(System.currentTimeMillis()-start));
	}

}
