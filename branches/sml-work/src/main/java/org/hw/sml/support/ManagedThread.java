package org.hw.sml.support;


public abstract class ManagedThread extends Thread {
	
	protected  long counts;

	protected boolean stopFlag = false;

	private Object waitLock = new Object();

	abstract protected boolean prepare();

	/**
	 * 主体运行逻辑
	 */
	abstract protected void doWorkProcess();

	abstract protected void cleanup();

	/**
	 * 是否满足退出条件
	 * 
	 * @return
	 */
	abstract protected boolean extraExitCondition();

	public void run() {
		LoggerHelper.getLogger().info(getClass(),this.getName() + " started ...");

		if (!prepare()) {
			LoggerHelper.getLogger().error(getClass(),this.getName() + " prepare failure , thread exit ...");
			return;
		}

		while (!(stopFlag && extraExitCondition())) {
			doWorkProcess();
		}
		cleanup();
		LoggerHelper.getLogger().info(getClass(),this.getName() + " stopped ...");
	}

	/**
	 * 线程进入等待阻塞模式，timeout = 0 永久等待，直至被唤醒，否则等 timeout 毫秒后自动唤醒
	 * 
	 * @param timeout
	 */
	public void relax(long timeout) {
		synchronized (waitLock) {
			try {
				waitLock.wait(timeout);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void shutdown() {
		stopFlag = true;
		synchronized (waitLock) {
			waitLock.notifyAll();
		}
		this.interrupt();

	}

	public void wakeup() {
		synchronized (waitLock) {
			waitLock.notifyAll();
		}
	}

	public long getCounts() {
		return counts;
	}

	public void setCounts(long counts) {
		this.counts = counts;
	}
	
}
