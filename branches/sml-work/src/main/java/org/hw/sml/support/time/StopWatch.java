package org.hw.sml.support.time;

import java.text.NumberFormat;
import java.util.Map;

import org.hw.sml.tools.Assert;
import org.hw.sml.tools.MapUtils;

public class StopWatch {
	private String taskName;
	private String currentSteps;
	private Map<String,Long> steps=MapUtils.newLinkedHashMap();
	private long runStart;
	private long stepTime;
	private long total;
	public StopWatch(String taskName){
		this.taskName=taskName;
		runStart=System.currentTimeMillis();
	}
	public synchronized void start(String step){
		currentSteps=step;
		stepTime=System.currentTimeMillis();
	}
	public synchronized void stop(){
		Assert.notNull(currentSteps,"taskName-["+taskName+"] not running !");
		steps.put(currentSteps,System.currentTimeMillis()-stepTime);
		currentSteps=null;
		total=System.currentTimeMillis()-runStart;
	}
	public String prettyPrint(){
		StringBuilder sb = new StringBuilder(taskName+" cost:"+total+" ms\n");
		sb.append("-----------------------------------------\n");
		sb.append("ms     %     Task name\n");
		sb.append("-----------------------------------------\n");
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumIntegerDigits(5);
		nf.setGroupingUsed(false);
		NumberFormat pf = NumberFormat.getPercentInstance();
		pf.setMinimumIntegerDigits(3);
		pf.setGroupingUsed(false);
		for (Map.Entry<String,Long>  task : steps.entrySet()) {
			sb.append(nf.format(task.getValue())).append("  ");
			sb.append(pf.format(task.getValue()*1.0/total)).append("  ");
			sb.append(task.getKey()).append("\n");
		}
		return sb.toString();
	}
}
