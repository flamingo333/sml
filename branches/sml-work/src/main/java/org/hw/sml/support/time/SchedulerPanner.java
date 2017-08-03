package org.hw.sml.support.time;

import java.util.Map;

import org.hw.sml.support.LoggerHelper;
import org.hw.sml.support.ioc.BeanHelper;
import org.hw.sml.support.ioc.PropertiesHelper;
import org.hw.sml.support.queue.ManagedQuene;
import org.hw.sml.support.queue.Task;
import org.hw.sml.tools.ClassUtil;
import org.hw.sml.tools.MapUtils;

public class SchedulerPanner extends ManagedQuene<Task>{
	private  Map<String,String> taskMapContain=MapUtils.newHashMap();
	private  Map<String,Boolean> taskMapStatus=MapUtils.newHashMap();
	public void init(){
		for(Map.Entry<String,String> entry:BeanHelper.getBean(PropertiesHelper.class).getValues().entrySet()){
			String key=entry.getKey();
			if(key.startsWith("task-")&&key.contains("-")){
				String beanMethod=key.replaceFirst("task-","");
				if(beanMethod.split("\\.").length==1){
					LoggerHelper.warn(getClass(),key+" is error!");
					continue;
				}
				taskMapContain.put(beanMethod,entry.getValue());
			}
		}
		LoggerHelper.info(getClass(),"task["+taskMapContain+"]");
		if(taskMapContain.size()>0){
			for(String key:taskMapContain.keySet())
			taskMapStatus.put(key,true);
			super.init();
			Scheduler sd=new Scheduler();
			sd.setTask(new TimerTask() {
				public void execute() {
					task();
				}
			});
			sd.setDelay(60);
			sd.init();
		}
	}
	public void task(){
		for(Map.Entry<String,String> entry:taskMapContain.entrySet()){
			try {
				final String key=entry.getKey();
				String value=entry.getValue();
				TaskModel tm=new TaskModel();
				tm.setElp(value);
				tm.init();
				if(!tm.isExecuteNow()||!taskMapStatus.get(key)){
					continue;
				}
				super.add(new Task() {
					String[] bm=key.split("-")[1].split("\\.");
					String bn=bm[0];
					String mn=bm[1];
					Object bean=BeanHelper.getBean(bn);
					Class<?> c=bean.getClass();
					public void execute() throws Exception {
						ClassUtil.getMethod(c,mn,new Class<?>[]{}).invoke(bean,new Object[]{});
					}
					public String toString(){
						return bn+"-"+mn;
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
	public  Map<String, String> getTaskMapContain() {
		return taskMapContain;
	}
	public  void setTaskMapContain(Map<String, String> taskMapContain) {
		this.taskMapContain = taskMapContain;
	}
	public Map<String, Boolean> getTaskMapStatus() {
		return taskMapStatus;
	}
	public void setTaskMapStatus(Map<String, Boolean> taskMapStatus) {
		this.taskMapStatus = taskMapStatus;
	}
	public static void main(String[] args) {
		SchedulerPanner sp=new SchedulerPanner();
		sp.init();
		System.out.println(sp);
	}
	
}
