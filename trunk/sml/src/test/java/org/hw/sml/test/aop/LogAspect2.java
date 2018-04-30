package org.hw.sml.test.aop;

import org.hw.sml.support.aop.AbstractAspect;
import org.hw.sml.support.aop.Invocation;
import org.hw.sml.support.ioc.annotation.Bean;

@Bean
public class LogAspect2 extends AbstractAspect{
	{
		setOrderId(3);
		setPackageMatchs("org.hw.sml.test.bean.A.(test\\d+)");
	}

	public void doBefore(Invocation invocation)  throws Throwable{
		invocation.getExtInfo().put("start",System.currentTimeMillis());
	}
	public void doAfter(Invocation invocation)  throws Throwable{
		Long start=(Long) invocation.getExtInfo().get("start");
		System.out.println(invocation.getTarget().getClass().getName()+"."+invocation.getMethod().getName()+"耗时3："+(System.currentTimeMillis()-start));
	}

}
