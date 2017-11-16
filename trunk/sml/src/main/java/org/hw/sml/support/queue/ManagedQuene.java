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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.hw.sml.support.LoggerHelper;
import org.hw.sml.support.ManagedThread;
import org.hw.sml.tools.DateTools;
import org.hw.sml.tools.MapUtils;
/**
 * quene managed
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
	private int depth=10000;
	
	/**
	 * 消费者数量
	 */
	private int consumerThreadSize=1;
	
	private Map<String,Status> stats=MapUtils.newHashMap();
	
	
	/**
	 * 线程名称
	 */
	private String threadNamePre;
	/**
	 * 队列名称
	 */
	private BlockingQueue<T> queue;
	/**
	 * 异常报错信息 getManageName()+" of manageName has Error taskid:[%s] msg like [%s]! 
	 */
	private String errorMsg; 
	/**
	 * 整个队列进行清理
	 */
	private boolean stop=false;
	/**
	 * 默认非阻塞
	 */
	private boolean fullErrIgnore=true;
	/**
	 * 阻塞情况下休眠时间
	 */
	private int fullErrTimeout=100;
	
	private List<Execute> executes=new ArrayList<Execute>();
	/**
	 * 超时设定
	 */
	private int timeout;
	/**
	 * 日志是否忽略
	 */
	private boolean ignoreLog=true;
	/**
	 * 超时任务是否继续执行
	 */
	private boolean timeoutRunning=false;
	/**
	 * 跳过新增任务，如果任务在执行
	 */
	private boolean skipQueueCaseInExecute;
	/**
	 * 执行或即将执行的任务
	 */
	private  Map<String,Boolean> executingMap;
	/**
	 * 工作线程计数器
	 */
	private AtomicInteger ai=new AtomicInteger(0);
	
	public  void init(){
		if(queue==null){
			queue=new ArrayBlockingQueue<T>(depth);
			LoggerHelper.info(getClass(),"manageName ["+getManageName()+"] has init depth "+depth+" !");
		}
		executingMap=new LinkedHashMap<String, Boolean>();
		for(int i=1;i<=consumerThreadSize;i++){
			Execute execute=new Execute();
			execute.setDaemon(true);
			String threadName=getThreadNamePre()+"-"+ai.getAndIncrement();
			execute.setName(threadName);
			stats.put(threadName,new Status());
			executes.add(execute);
			execute.start();
		}
	}
	public void stop(String threadName){
		for(Execute execute:executes){
			if(execute.getName().equalsIgnoreCase(threadName)){
				execute.shutdown();
			}
		}
	}
	public void destroy(){
		this.stop=true;
		for(Execute execute:executes){
			execute.shutdown();
		}
		executes.clear();
	}
	public void add(T task){
		if(queue.size()>=depth&&fullErrIgnore){
				try {
					Thread.sleep(fullErrTimeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				add(task);
		}else{
			addT(task);
		}
	}
	
	public synchronized void addT(T task){
		if(skipQueueCaseInExecute&&executingMap.containsKey(task.toString())){
			return;
		}
		queue.add(task);
		executingMap.put(task.toString(),true);
		if(!ignoreLog)
			LoggerHelper.info(getClass(),"add "+getManageName()+" total-"+getDepth()+",current-"+queue.size()+".");
			
	}
	
	private class Execute extends ManagedThread{
		protected boolean prepare() {
			return queue!=null;
		}
		protected void doWorkProcess() {
			Task task=null;
			ExecutorService exec=null;
			Future<Integer> future=null;
			try {
				task=queue.take();
				final Task t=task;
				stats.get(Thread.currentThread().getName()).start(t.toString());
				if(timeout<=0)
					task.execute();
				else{
					exec = Executors.newSingleThreadExecutor();
					Callable<Integer> call=new Callable<Integer>() {
						public Integer call() throws Exception {
							return new Inner(t).exe();
						}
					};
					future=exec.submit(call);
					future.get(timeout, TimeUnit.SECONDS);
				}
				stats.get(Thread.currentThread().getName()).success().info(task);
			}  catch (TimeoutException e) {
				LoggerHelper.info(getClass(),"task["+task.toString()+"] timeout!");
				if(future!=null&&!timeoutRunning)
					future.cancel(true);
				else
					executingMap.put(task.toString(),false);
				stats.get(Thread.currentThread().getName()).fail().failInfo(task, e.getMessage());
			}catch (Exception e) {
				e.printStackTrace();
				LoggerHelper.error(getClass(),String.format(getErrorMsg(),task.toString(),e.getMessage()));
				stats.get(Thread.currentThread().getName()).fail().failInfo(task, e.getMessage());
			}finally{
				executingMap.remove(task.toString());
				if(exec!=null){
					if(timeoutRunning)
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
		if(manageName==null){
			manageName=getClass().getSimpleName();
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
		if(consumerThreadSize>=1)
		this.consumerThreadSize = consumerThreadSize;
	}

	public String getThreadNamePre() {
		if(threadNamePre==null){
			threadNamePre=getManageName()+"-woker";
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
		if(errorMsg==null){
			errorMsg=getManageName()+" of manageName has Error taskid:[%s] msg like [%s]!";
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

	 class Inner{
			private Task task;
			public Inner(Task task){
				this.task=task;
			}
			public Integer exe() throws Exception{
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

	public Map<String, Boolean> getExecutingMap() {
		return executingMap;
	}

	public void setExecutingMap(Map<String, Boolean> executingMap) {
		this.executingMap = executingMap;
	}
	
	class Status{
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
		public Status(){
			this.d=System.currentTimeMillis();
			this.h=System.currentTimeMillis();
		}
		public void start(String executeTaskInfo) {
			this.executeTaskInfo=executeTaskInfo;
		}
		public Status success(){
			run();
			this.lastExecuteTime=System.currentTimeMillis();
			this.executiveIncrementTimes++;
			return this;
		}
		public Status failInfo(Task task,String error){
			this.lastExecuteTaskErrorInfo=task.toString();
			this.lastExecuteErrorInfo=error;
			return this;
		}
		public Status info(Task task){
			this.lastExecuteTaskInfo=task.toString();
			return this;
		}
		public Status fail(){
			run();
			this.lastExecuteErrorTime=System.currentTimeMillis();
			this.executiveIncrementErrorTimes++;
			return this;
		}
		public void run(){
			if(d<System.currentTimeMillis()-1000*60*60*24){
				d=System.currentTimeMillis();
				lastExecuteIncrementDay=0;
			}
			if(h<System.currentTimeMillis()-1000*60*60){
				h=System.currentTimeMillis();
				lastExecuteIncrementHour=0;
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
	
}
