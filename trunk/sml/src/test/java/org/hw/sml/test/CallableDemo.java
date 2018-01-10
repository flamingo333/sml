package org.hw.sml.test;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.hw.sml.support.CallableHelper;
import org.hw.sml.tools.MapUtils;
import org.junit.Test;


public class CallableDemo {
	//一台打印机打印一份文件需要0.2s
	class Print{
		public  void print(int i){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	@Test
	public void test1(){
		test(1);
	}
	@Test
	public void test2(){
		test(5);
	}
	//现在1000份文件需要多久打印完
	@SuppressWarnings("unchecked")
	public  void test(int size) {
		List<Callable<String>> e=MapUtils.newArrayList();
		for(int i=1;i<=10;i++){
			final int temp=i;
			Callable<String> callable=new Callable<String>() {
				public String call() throws Exception {
					new Print().print(temp);
					return temp+"";
				}
			};
			e.add(callable);
		}
		//一台打印机花费
		long start=System.currentTimeMillis();
		CallableHelper.callresults(1,size,e.toArray(new Callable[e.size()]));
		System.out.println(size+"台打印机花费-->"+(System.currentTimeMillis()-start));
		
	}
}
