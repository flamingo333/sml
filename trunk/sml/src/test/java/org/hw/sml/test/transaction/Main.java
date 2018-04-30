package org.hw.sml.test.transaction;

import org.hw.sml.support.ioc.BeanHelper;

public class Main {
	public static void main(String[] args) {
		Demo demo=BeanHelper.getBean(Demo.class);
		demo.doit();
		//System.out.println("org.hw.sml.test.transaction.DemoImpl.at4".matches("org.hw.sml.test.transaction.(.*?)Impl.(do(.*?)|at(.*?))"));
	}
}
