package org.hw.sml.support.el;

public class BeanType {
	private Object v;
	private Class<?> c;
	public BeanType(Object v,Class<?> c){
		this.v=v;
		this.c=c;
	}
	public BeanType() {
	}
	public Object getV() {
		return v;
	}
	public void setV(Object v) {
		this.v = v;
	}
	public Class<?> getC() {
		return c;
	}
	public void setC(Class<?> c) {
		this.c = c;
	}
	
}
