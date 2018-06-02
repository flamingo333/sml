package org.hw.sml.support.aop;

public interface Aop {
	public <T> T newProxyInstance(Object proxyTarget,Aspect ...aspects);
}
