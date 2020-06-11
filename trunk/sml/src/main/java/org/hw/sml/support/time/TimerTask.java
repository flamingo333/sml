package org.hw.sml.support.time;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class TimerTask implements Runnable{
	
	private int times=Integer.MAX_VALUE;
	
	private int counts=1;
	
	
	private Scheduler scheduler;
	
	public abstract void execute();
	public void run(){
		execute();
		if(counts++==times){
			scheduler.cancel();
		}else{
			repeat();
		}
	}
	public int getTimes() {
		return times;
	}
	public void setTimes(int times) {
		this.times = times;
	}
	public Scheduler getScheduler() {
		return scheduler;
	}
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
		repeat();
	}
	public void repeat(){
		long firstTime=scheduler.getFirstTime().getTime();
		long delay=System.currentTimeMillis()-firstTime;
			if(delay<0)
				delay=-delay;
			else
				delay=scheduler.getDelay()*1000-delay%(scheduler.getDelay()*1000);
		scheduler.setNextTime(new Date(System.currentTimeMillis()+delay));
		scheduler.getExecutor().schedule(this,delay,TimeUnit.MILLISECONDS);
	}
	
	
	
}
