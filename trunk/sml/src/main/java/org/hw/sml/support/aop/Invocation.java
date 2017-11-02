package org.hw.sml.support.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class Invocation {
	private Object target;
	private Method method;
	private Object[] args;
	private Object value;
	private Throwable throwable;
	private boolean isExecute;
	private Map<String,Object> extInfo=new HashMap<String,Object>();
	public Invocation(Object target, Method method, Object[] args) {
		this.target = target;
		this.method = method;
		this.args = args;
	}

	public Object getTarget() {
		return this.target;
	}

	public Method getMethod() {
		return this.method;
	}

	public Object[] getArgs() {
		return this.args;
	}
	
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object proceed() throws Throwable {
		if(!isExecute){
			try{
				value=invoke();
				isExecute=true;
			}catch(Throwable e){
				setThrowable(e);
			}
		}
		return value;
	}
	public abstract Object invoke() throws Throwable;

	public Map<String, Object> getExtInfo() {
		return extInfo;
	}

	public void setExtInfo(Map<String, Object> extInfo) {
		this.extInfo = extInfo;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}
	
}