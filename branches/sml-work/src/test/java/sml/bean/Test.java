package sml.bean;

import java.util.Date;

import org.hw.sml.support.ioc.BeanHelper;
import org.hw.sml.support.ioc.annotation.Bean;
import org.hw.sml.support.time.annotation.Scheduler;
import org.hw.sml.tools.DateTools;

@Bean
public class Test {
	@Scheduler("min1")
	public void test(){
		System.out.println(DateTools.sdf_mis.format(new Date()));
	}
	public static void main(String[] args) {
		BeanHelper.start();
	}
}
