package org.hw.sml.test;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.hw.sml.context.SmlContextUtils;
import org.hw.sml.core.resolver.JsEngine;
import org.hw.sml.support.el.ElException;
import org.hw.sml.support.ioc.BeanHelper;
import org.hw.sml.support.time.StopWatch;
import org.hw.sml.tools.Https;
import org.junit.internal.runners.statements.RunAfters;

public class Test {
	 static class DefaultThreadFactory implements ThreadFactory {
	        static final AtomicInteger poolNumber = new AtomicInteger(1);
	        final ThreadGroup group;
	        final AtomicInteger threadNumber = new AtomicInteger(1);
	        final String namePrefix;

	        DefaultThreadFactory() {
	            SecurityManager s = System.getSecurityManager();
	            group = (s != null)? s.getThreadGroup() :
	                                 Thread.currentThread().getThreadGroup();
	            namePrefix = "pool-" +
	                          poolNumber.getAndIncrement() +
	                         "-thread-";
	        }

	        public Thread newThread(Runnable r) {
	            Thread t = new Thread(group, r,
	                                  namePrefix + threadNumber.getAndIncrement(),
	                                  0);
	            if (t.isDaemon())
	                t.setDaemon(false);
	            if (t.getPriority() != Thread.NORM_PRIORITY)
	                t.setPriority(Thread.NORM_PRIORITY);
	            return t;
	        }
	    }
	public static void main1(String[] args) throws IOException, ElException {
		BeanHelper.start();
		JsEngine.evel("");
		StopWatch sw=new StopWatch("eval");
		sw.start("java");
		int times=10000;
		for(int i=0;i<times;i++){
			Object result=BeanHelper.evelV("#{(1+1.0)}");
			if(i==1)
				System.out.println(result);
		}
		sw.stop();
		sw.start("js");
		for(int i=0;i<times;i++){
			Object result=JsEngine.evel("eval(1+1)");
			if(i==1)
				System.out.println(result);
		}
		sw.stop();
		System.out.println(sw.prettyPrint());
		for(int i=0;i<100;i++){
			new Thread(new Runnable() {
				
				public void run() {
					while(true){
						try {
							System.out.println(Https.newPostBodyHttps("http://localhost:8081/post?a=1").body("{\"a2\":1}").execute());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
	}
	
	public static void main(String[] args) {

	}
}
