package org.hw.sml.support.aop;


public  interface Aspect{
	public boolean isProxy(Invocation invocation) throws Throwable;
	
	public void doBefore(Invocation invocation)  throws Throwable;
	
	public void doAfter(Invocation invocation) throws Throwable;
	
	public void doException(Invocation invocation) throws Throwable;
}
