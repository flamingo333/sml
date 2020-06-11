package org.hw.sml.support.time;

import java.util.List;
import java.util.Map;

import org.hw.sml.support.LoggerHelper;
import org.hw.sml.support.ioc.BeanHelper;
import org.hw.sml.support.ioc.PropertiesHelper;
import org.hw.sml.support.queue.ManagedQuene;
import org.hw.sml.support.queue.Task;
import org.hw.sml.tools.Assert;
import org.hw.sml.tools.ClassUtil;
import org.hw.sml.tools.MapUtils;

//SchedulerPanner.taskMapStatus.put('',false)
public class SchedulerPanner extends ManagedQuene<Task> {
	private int secondIntervals;
	private Map<String, String> taskMapContain = MapUtils.newHashMap();
	private Map<String, Boolean> taskMapStatus = MapUtils.newHashMap();
	private static List<Job> jobs = MapUtils.newArrayList();

	public void reinit() {
		taskMapContain.clear();
		taskMapStatus.clear();
		super.destroy();
		init();
	}

	public void stop(String tasks) {
		String[] ts = tasks.split(",");
		for (String t : ts) {
			taskMapStatus.put(t, false);
		}
	}

	public void enabled(String tasks) {
		String[] ts = tasks.split(",");
		for (String t : ts) {
			taskMapStatus.put(t, true);
		}
	}

	public void init() {
		if (secondIntervals > 1) {
			CronExpression.igNoreSecondCheck = true;
		}
		for (Map.Entry<String, String> entry : BeanHelper.getBean(PropertiesHelper.class).getValues().entrySet()) {
			String key = entry.getKey();
			if (key.startsWith("task-") && key.contains("-")) {
				String beanMethod = key.replaceFirst("task-", "");
				if (beanMethod.split("\\.").length == 1) {
					LoggerHelper.getLogger().warn(getClass(), key + " is error!");
					continue;
				}
				taskMapContain.put(beanMethod, entry.getValue());
			}
		}
		for (Map.Entry<String, Object> bean : BeanHelper.getBeanMap().entrySet()) {
			if (bean.getValue() instanceof Job) {
				Job job = (Job) bean.getValue();
				if (job.getCron() == null) {
					job.execute();
				} else {
					taskMapContain.put("jobBean-" + bean.getKey() + ".execute", job.getCron());
				}
			}
		}
		// 全局初始化job
		for (Job job : jobs) {
			String simpleName = job.getClass().getSimpleName();
			if (job.getCron() == null) {
				job.execute();
			} else {
				BeanHelper.registerBean(simpleName, job);
				taskMapContain.put("job-" + simpleName + ".execute", job.getCron());
			}
		}
		LoggerHelper.getLogger().info(getClass(), "task[" + taskMapContain + "]");
		if (taskMapContain.size() > 0) {
			for (String key : taskMapContain.keySet())
				taskMapStatus.put(key, true);
			super.init();
			Scheduler sd = new Scheduler();
			sd.setTask(new TimerTask() {
				public void execute() {
					task();
				}
			});
			sd.setDelay(secondIntervals);
			sd.init();
		}
		super.setJobMng(new JobMngScheduler());
	}

	public static void regiester(Job job) {
		jobs.add(job);
	}

	public void task() {
		for (Map.Entry<String, String> entry : taskMapContain.entrySet()) {
			try {
				final String key = entry.getKey();
				String value = entry.getValue();
				TaskModel tm = new TaskModel();
				tm.setElp(value);
				tm.init();
				if (!tm.isExecuteNow() || !taskMapStatus.get(entry.getKey())) {
					continue;
				}
				super.add(new SimpleTask(key));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class SimpleTask implements Task {
		private String bn;
		private String mn;
		public SimpleTask(String key) {
			String[] bm = key.substring(key.indexOf("-") + 1).split("\\.");
			bn = bm[0];
			mn = bm[1];
		}
		public void execute() throws Exception {
			Object bean = BeanHelper.getBean(bn);
			Assert.notNull(bean, String.format("Scheduler bean [%s] not null!", bn));
			Class<?> c = bean.getClass();
			ClassUtil.getMethod(c, mn, new Class<?>[] {}).invoke(bean, new Object[] {});
		}
		public String toString() {
			return bn + "-" + mn;
		}

	}

	public Map<String, String> getTaskMapContain() {
		return taskMapContain;
	}

	public void setTaskMapContain(Map<String, String> taskMapContain) {
		this.taskMapContain = taskMapContain;
	}

	public Map<String, Boolean> getTaskMapStatus() {
		return taskMapStatus;
	}

	public void setTaskMapStatus(Map<String, Boolean> taskMapStatus) {
		this.taskMapStatus = taskMapStatus;
	}

	public int getSecondIntervals() {
		return secondIntervals;
	}

	public void setSecondIntervals(int secondIntervals) {
		this.secondIntervals = secondIntervals;
	}
	class JobMngScheduler implements JobMng{
		@Override
		public List<TaskMap> getJobInfo() {
			List<TaskMap> result=MapUtils.newArrayList();
			for(Map.Entry<String,Boolean> entry:taskMapStatus.entrySet()){
				TaskMap tm=new TaskMap();
				tm.setId(entry.getKey());
				tm.setStatus(entry.getValue());
				String elp=taskMapContain.get(entry.getKey());
				tm.setElp(elp);
				result.add(tm);
			}
			return result;
		}

		@Override
		public boolean changeStatus(String id) {
			if(getTaskMapStatus().containsKey(id)){
				boolean flag=getTaskMapStatus().get(id);
				getTaskMapStatus().put(id,!flag);
				return true;
			}
			return false;
		}

		@Override
		public boolean doItNow(String id) {
			 add(new SimpleTask(id));
			 return true;
		}

		@Override
		public Object execute(String cmd) {
			return false;
		}
		
	}
}
