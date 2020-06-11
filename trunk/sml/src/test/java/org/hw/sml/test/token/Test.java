package org.hw.sml.test.token;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Date;




import oracle.net.ano.CryptoDataPacket;

import org.hw.sml.support.log.DelegatedDefaultLog;
import org.hw.sml.support.log.Loggers;
import org.hw.sml.support.security.CyptoUtils;
import org.hw.sml.support.time.StopWatch;
import org.hw.sml.tools.ClassUtil;
class D{
	private Number g;
	private BigDecimal d;
}
public class Test extends D{
	static String test= "(sml).a.getJdbc('def.Jt').b.dataSource.toString('....').equals(#{sml.data().a().a.b.c(#{a.b.c().end.c.e})}).toString().length()";
	private String a;
	private Integer b;
	private int c;
	private double d;
	private  final static  long e=0;
	public Date t;
	
	public String getA() {
		return a;
	}

	public void setA(String a) {
		this.a = a;
	}

	public Integer getB() {
		return b;
	}

	public void setB(Integer b) {
		this.b = b;
	}

	public int getC() {
		return c;
	}

	public void setC(int c) {
		this.c = c;
	}

	public double getD() {
		return d;
	}

	public void setD(double d) {
		this.d = d;
	}

	
	public Date getT() {
		return t;
	}

	public void setT(Date t) {
		this.t = t;
	}

	//
	public static void  main(String[] args) throws SecurityException, NoSuchFieldException {
		System.out.println(CyptoUtils.encode("47cb9f15d1dab90243b5317399f31a24","Bt2D*4d="));
	}
}
