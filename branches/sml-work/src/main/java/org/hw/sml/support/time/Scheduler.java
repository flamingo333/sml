package org.hw.sml.support.time;

import java.util.Date;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.hw.sml.plugin.Plugin;
import org.hw.sml.tools.DateTools;

public class Scheduler implements Plugin{
	
	private TimerTask task;
	
	private long delay=1;
	
	private Timer timer;
	
	private Date firstTime;
	
	private Date nextTime;
	
	private boolean fixedRate=false;
	
	private ScheduledExecutorService executor;//= Executors.newScheduledThreadPool(1);  
	
	
	public Scheduler(){
		executor = Executors.newScheduledThreadPool(1);  
		if(firstTime==null){
			firstTime=DateTools.trunc(DateTools.addMinutes(new Date(),1),1000);
		}
	}
	
	public void cancel(){
		executor.shutdown();
	}

	public void init() {
		if(task==null){
			return;
		}
		task.setScheduler(this);
	}
	
	public void destroy() {
		this.timer.cancel();
		
	}

	public TimerTask getTask() {
		return task;
	}

	public void setTask(TimerTask task) {
		this.task = task;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public Date getFirstTime() {
		return firstTime;
	}

	public void setFirstTime(Date firstTime) {
		this.firstTime = firstTime;
	}

	public boolean isFixedRate() {
		return fixedRate;
	}

	public void setFixedRate(boolean fixedRate) {
		this.fixedRate = fixedRate;
	}

	public ScheduledExecutorService getExecutor() {
		return executor;
	}

	public Date getNextTime() {
		return nextTime;
	}

	public void setNextTime(Date nextTime) {
		this.nextTime = nextTime;
	}
	
	
}
