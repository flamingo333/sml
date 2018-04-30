package org.hw.sml.test.bean;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;

public class TestA{
	public static final String ma="1";
	private String a;
	private int b;
	private float c;
	private double e;
	private Long f;
	private Time g;
	private BigDecimal h;
	private Boolean bb;
	public String getA() {
		return a;
	}
	public void setA(String a) {
		this.a = a;
	}
	public int getB() {
		return b;
	}
	public void setB(int b) {
		this.b = b;
	}
	public float getC() {
		return c;
	}
	public void setC(float c) {
		this.c = c;
	}
	public double getE() {
		return e;
	}
	public void setE(double e) {
		this.e = e;
	}
	public Long getF() {
		return f;
	}
	public void setF(Long f) {
		this.f = f;
	}
	public Time getG() {
		return g;
	}
	public void setG(Time g) {
		this.g = g;
	}
	public BigDecimal getH() {
		return h;
	}
	public void setH(BigDecimal h) {
		this.h = h;
	}
	@Override
	public String toString() {
		return "A [a=" + a + ", b=" + b + ", c=" + c + ", e=" + e + ", f=" + f
				+ ", g=" + g + ", h=" + h +",bb="+bb+ "]";
	}
	
}