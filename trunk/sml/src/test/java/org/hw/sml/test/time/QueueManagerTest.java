package org.hw.sml.test.time;

import org.hw.sml.support.queue.ManagedQuene;
import org.hw.sml.support.queue.Task;
import org.hw.sml.tools.ClassUtil;

public class QueueManagerTest {
	public static void main(String[] args) throws InterruptedException {
		ManagedQuene<Task> mq=new ManagedQuene<Task>();
		mq.init();
		for(int i=0;i<100;i++){
			final int t=i;
			mq.add(new Task() {
				public void execute() throws Exception {
					System.out.println(t);
					Thread.sleep(1000000);
				}
			});
		}
		Thread.sleep(1000);
		//mq.stop("ManagedQuene-woker-0");
		//mq.stop("");
	}
}
