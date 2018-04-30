package org.hw.sml.test;
import java.util.Random;

import org.hw.sml.support.LoggerHelper;
import org.hw.sml.support.queue.ManagedQuene;
import org.hw.sml.support.queue.Task;
import org.hw.sml.support.time.StopWatch;


public class TaskDemo {
	public static void main(String[] args) {
		final ManagedQuene<Task> mq=new ManagedQuene<Task>();
		mq.setFullErrIgnore(true);
		mq.setDepth(5);
		mq.setIgnoreLog(false);
		mq.setConsumerThreadSize(3);
		mq.init();
		new Thread(new Runnable() {
			public void run() {
					while(true){
						mq.add(new Task() {
							public void execute() throws Exception {
								int s=new Random().nextInt(100000)+1000;
								Thread.sleep(s);
								LoggerHelper.info(getClass(),s+"");
							}
						});
					}
			}
		}).start();
		try {
			Thread.sleep(10000000000000l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
