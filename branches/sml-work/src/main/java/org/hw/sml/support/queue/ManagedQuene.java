package org.hw.sml.support.queue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.hw.sml.support.LoggerHelper;
import org.hw.sml.support.ManagedThread;
import org.hw.sml.tools.DateTools;
import org.hw.sml.tools.MapUtils;

/**
 * quene managed
 * 
 * @author wen
 *
 */
public class ManagedQuene<T extends Task> {

	/**
	 * 队列管理名称
	 */
	private String manageName;
	/**
	 * 队列深度
	 */
	private int depth = 10000;

	/**
	 * 消费者数量
	 */
	private int consumerThreadSize = 1;

	private boolean jdkPool = false;

	private Map<String, Status> stats = MapUtils.newHashMap();

	/**
	 * 线程名称
	 */
	private String threadNamePre;
	/**
	 * 队列名称
	 */
	private BlockingQueue<T> queue;
	/**
	 * 异常报错信息 getManageName()+" of manageName has Error taskid:[%s] msg like
	 * [%s]!
	 */
	private String errorMsg;
	/**
	 * 整个队列进行清理
	 */
	private boolean stop = false;
	/**
	 * 默认非阻塞
	 */
	private boolean fullErrIgnore = true;
	/**
	 * 阻塞情况下休眠时间
	 */
	private int fullErrTimeout = 100;

	private List<Execute> executes = new ArrayList<Execute>();
	/**
	 * 超时设定
	 */
	private int timeout;
	/**
	 * 日志是否忽略
	 */
	private boolean ignoreLog = true;
	/**
	 * 超时任务是否继续执行
	 */
	private boolean timeoutRunning = false;
	/**
	 * 跳过新增任务，如果任务在执行
	 */
	private boolean skipQueueCaseInExecute;
	/**
	 * 执行或即将执行的任务
	 */
	private Map<String, Long> executingMap;
	/**
	 * 工作线程计数器
	 */
	private AtomicInteger ai = new AtomicInteger(0);
	
	private boolean allowCoreThreadTimeOut=true;
	/**
	 * 任务清除超时s
	 */
	private long taskClearTimeout = 60 * 60 * 2;

	private int coreSize = 1;

	private long keepAliveTime = 60*2;

	private ThreadPoolExecutor threadPoolExecutor;

	private BlockingQueue<Runnable> workQueue;

	public void init() {
		executingMap = new LinkedHashMap<String, Long>();
		if (!jdkPool) {
			if (queue == null) {
				queue = new ArrayBlockingQueue<T>(depth);
				LoggerHelper.getLogger().info(getClass(),
						"manageName [" + getManageName() + "] has init depth " + depth + " !");
			}
			for (int i = 1; i <= consumerThreadSize; i++) {
				Execute execute = new Execute();
				execute.setDaemon(true);
				String threadName = getThreadNamePre() + "-" + ai.getAndIncrement();
				execute.setName(threadName);
				stats.put(threadName, new Status());
				executes.add(execute);
				execute.start();
			}
		} else {
			if (workQueue == null) {
				workQueue = new ArrayBlockingQueue<Runnable>(depth);
			}
			threadPoolExecutor = new ThreadPoolExecutor(coreSize, consumerThreadSize, keepAliveTime, TimeUnit.SECONDS,
					workQueue);
			threadPoolExecutor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
			threadPoolExecutor.setThreadFactory(new DefaultThreadFactory());
			if (!fullErrIgnore)
				threadPoolExecutor.setRejectedExecutionHandler(new WaitingRejectedExecutionHandler());
		}
	}

	public void stop(String threadName) {
		if (!jdkPool) {
			for (Execute execute : executes) {
				if (execute.getName().equalsIgnoreCase(threadName)) {
					execute.shutdown();
				}
			}
		}
	}

	public void destroy() {
		if (jdkPool) {
			threadPoolExecutor.shutdownNow();
		} else {
			this.stop = true;
			for (Execute execute : executes) {
				execute.shutdown();
			}
			executes.clear();
		}
	}

	public void add(T task) {
		if (!jdkPool) {
			if (queue.size() >= depth && fullErrIgnore) {
				try {
					Thread.sleep(fullErrTimeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				add(task);
			} else {
				addT(task);
			}
		} else {
			addT(task);
		}
	}

	public synchronized void addT(final T task) {
		if (skipQueueCaseInExecute && executingMap.containsKey(task.toString())
				&& executingMap.get(task.toString()) + taskClearTimeout * 1000 > System.currentTimeMillis()) {
			return;
		}
		if (!jdkPool) {
			if (queue.size() < depth)
				executingMap.put(task.toString(), System.currentTimeMillis());
			queue.add(task);
			if (!ignoreLog)
				LoggerHelper.getLogger().info(getClass(),
						"add " + getManageName() + " total-" + getDepth() + ",current-" + queue.size() + ".");
		} else {
			executingMap.put(task.toString(), System.currentTimeMillis());
			threadPoolExecutor.execute(new Runnable() {
				public void run() {
					try {
						task.execute();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						executingMap.remove(task.toString());
					}
				}
			});
		}

	}

	private class Execute extends ManagedThread {
		protected boolean prepare() {
			return queue != null;
		}

		protected void doWorkProcess() {
			Task task = null;
			ExecutorService exec = null;
			Future<Integer> future = null;
			String treadName = Thread.currentThread().getName();
			try {
				task = queue.take();
				final Task t = task;
				stats.get(treadName).start(t.toString());
				if (timeout <= 0)
					task.execute();
				else {
					exec = Executors.newSingleThreadExecutor();
					Callable<Integer> call = new Callable<Integer>() {
						public Integer call() throws Exception {
							return new Inner(t).exe();
						}
					};
					future = exec.submit(call);
					future.get(timeout, TimeUnit.SECONDS);
				}
				stats.get(treadName).success().info(task);
			} catch (TimeoutException e) {
				LoggerHelper.getLogger().info(getClass(), "task[" + task.toString() + "] timeout!");
				if (future != null && !timeoutRunning)
					future.cancel(true);
				else
					executingMap.remove(task.toString());
				stats.get(treadName).fail().failInfo(task, e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				LoggerHelper.getLogger().error(getClass(), String.format(getErrorMsg(), task, e.getMessage()));
				if (task != null)
					stats.get(treadName).fail().failInfo(task, e.getMessage());
			} finally {
				if (task != null)
					executingMap.remove(task.toString());
				if (exec != null) {
					if (timeoutRunning)
						exec.shutdown();
					else
						exec.shutdownNow();
				}

			}
		}

		protected void cleanup() {
		}

		protected boolean extraExitCondition() {
			return this.isInterrupted();
		}
	}

	public String getManageName() {
		if (manageName == null) {
			manageName = getClass().getSimpleName();
		}
		return manageName;
	}

	public void setManageName(String manageName) {
		this.manageName = manageName;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getConsumerThreadSize() {
		return consumerThreadSize;
	}

	public void setConsumerThreadSize(int consumerThreadSize) {
		if (consumerThreadSize >= 1)
			this.consumerThreadSize = consumerThreadSize;
	}

	public String getThreadNamePre() {
		if (threadNamePre == null) {
			threadNamePre = getManageName() + "-woker";
		}
		return threadNamePre;
	}

	public void setThreadNamePre(String threadNamePre) {
		this.threadNamePre = threadNamePre;
	}

	public BlockingQueue<T> getQueue() {
		return queue;
	}

	public void setQueue(BlockingQueue<T> queue) {
		this.queue = queue;
	}

	public String getErrorMsg() {
		if (errorMsg == null) {
			errorMsg = getManageName() + " of manageName has Error taskid:[%s] msg like [%s]!";
		}
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	class Inner {
		private Task task;

		public Inner(Task task) {
			this.task = task;
		}

		public Integer exe() throws Exception {
			task.execute();
			return 1;
		}
	}

	public boolean isIgnoreLog() {
		return ignoreLog;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public void setIgnoreLog(boolean ignoreLog) {
		this.ignoreLog = ignoreLog;
	}

	public boolean isFullErrIgnore() {
		return fullErrIgnore;
	}

	public void setFullErrIgnore(boolean fullErrIgnore) {
		this.fullErrIgnore = fullErrIgnore;
	}

	public int getFullErrTimeout() {
		return fullErrTimeout;
	}

	public void setFullErrTimeout(int fullErrTimeout) {
		this.fullErrTimeout = fullErrTimeout;
	}

	public boolean isTimeoutRunning() {
		return timeoutRunning;
	}

	public void setTimeoutRunning(boolean timeoutRunning) {
		this.timeoutRunning = timeoutRunning;
	}

	public boolean isSkipQueueCaseInExecute() {
		return skipQueueCaseInExecute;
	}

	public void setSkipQueueCaseInExecute(boolean skipQueueCaseInExecute) {
		this.skipQueueCaseInExecute = skipQueueCaseInExecute;
	}

	public Map<String, Long> getExecutingMap() {
		return executingMap;
	}

	public void setExecutingMap(Map<String, Long> executingMap) {
		this.executingMap = executingMap;
	}

	class Status {
		private long lastExecuteTime;
		private long lastExecuteErrorTime;
		private int executiveIncrementTimes;
		private int executiveIncrementErrorTimes;
		private long d;
		private long h;
		private int lastExecuteIncrementDay;
		private int lastExecuteIncrementHour;
		private String lastExecuteErrorInfo;
		private String lastExecuteTaskInfo;
		private String lastExecuteTaskErrorInfo;
		private String executeTaskInfo;

		public Status() {
			this.d = System.currentTimeMillis();
			this.h = System.currentTimeMillis();
		}

		public void start(String executeTaskInfo) {
			this.executeTaskInfo = executeTaskInfo;
		}

		public Status success() {
			run();
			this.lastExecuteTime = System.currentTimeMillis();
			this.executiveIncrementTimes++;
			return this;
		}

		public Status failInfo(Task task, String error) {
			this.lastExecuteTaskErrorInfo = task.toString();
			this.lastExecuteErrorInfo = error;
			return this;
		}

		public Status info(Task task) {
			this.lastExecuteTaskInfo = task.toString();
			return this;
		}

		public Status fail() {
			run();
			this.lastExecuteErrorTime = System.currentTimeMillis();
			this.executiveIncrementErrorTimes++;
			return this;
		}

		public void run() {
			if (d < System.currentTimeMillis() - 1000 * 60 * 60 * 24) {
				d = System.currentTimeMillis();
				lastExecuteIncrementDay = 0;
			}
			if (h < System.currentTimeMillis() - 1000 * 60 * 60) {
				h = System.currentTimeMillis();
				lastExecuteIncrementHour = 0;
			}
			lastExecuteIncrementDay++;
			lastExecuteIncrementHour++;
		}

		public String getLastExecuteTime() {
			return DateTools.sdf_mis.format(lastExecuteTime);
		}

		public String getLastExecuteErrorTime() {
			return DateTools.sdf_mis.format(lastExecuteErrorTime);
		}

		public int getExecutiveIncrementTimes() {
			return executiveIncrementTimes;
		}

		public int getExecutiveIncrementErrorTimes() {
			return executiveIncrementErrorTimes;
		}

		public int getLastExecuteIncrementDay() {
			return lastExecuteIncrementDay;
		}

		public int getLastExecuteIncrementHour() {
			return lastExecuteIncrementHour;
		}

		public String getLastExecuteErrorInfo() {
			return lastExecuteErrorInfo;
		}

		public String getLastExecuteTaskInfo() {
			return lastExecuteTaskInfo;
		}

		public String getLastExecuteTaskErrorInfo() {
			return lastExecuteTaskErrorInfo;
		}

		public String getD() {
			return DateTools.sdf_mis.format(d);
		}

		public String getH() {
			return DateTools.sdf_mis.format(h);
		}

		public String getExecuteTaskInfo() {
			return executeTaskInfo;
		}
	}

	public boolean getJdkPool() {
		return jdkPool;
	}

	public void setJdkPool(boolean jdkPool) {
		this.jdkPool = jdkPool;
	}

	public int getCoreSize() {
		return coreSize;
	}

	public void setCoreSize(int coreSize) {
		this.coreSize = coreSize;
	}

	public long getTaskClearTimeout() {
		return taskClearTimeout;
	}
	
	public boolean getAllowCoreThreadTimeOut() {
		return allowCoreThreadTimeOut;
	}

	public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
		this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
	}

	public long getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public void setTaskClearTimeout(long taskClearTimeout) {
		this.taskClearTimeout = taskClearTimeout;
	}

	class DefaultThreadFactory implements ThreadFactory {
		final ThreadGroup group;
		final AtomicInteger threadNumber = new AtomicInteger(1);
		final String namePrefix;

		DefaultThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = getThreadNamePre() + "-";
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			LoggerHelper.getLogger().debug(getClass(),t.getName()+" starting ...");
			return t;
		}
	}

	class WaitingRejectedExecutionHandler implements RejectedExecutionHandler {
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executor.execute(r);
		}

	}
}
