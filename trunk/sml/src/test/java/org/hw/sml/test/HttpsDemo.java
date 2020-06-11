package org.hw.sml.test;

import java.lang.reflect.Method;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.hw.sml.support.time.StopWatch;
import org.hw.sml.tools.Https;
import org.hw.sml.tools.Https.Header;

public class HttpsDemo {
	public static void main(String[] args) throws Exception, ClassNotFoundException {
		//zzzz
		//String result=Https.newGetHttps("http://localhost:8080/INAS/sml/invoke/mdmMngService/template/System").bos(new FileOutputStream("d:/temp/t.xlsx")).execute();
		//上传
		//String result=Https.newPostBodyHttps("http://localhost:1202/master/server/proxy/sms/send").charset("utf-8").body("{\"content\":\"test测试\",\"fromNumber\":\"18256075451\"}").execute();
		//System.out.println(result);
		//String result=Https.newGetHttps("http://localhost:1202/master/status").proxy(new Proxy(Proxy.Type.SOCKS,new InetSocketAddress("localhost",1080)),null).execute();
		//System.out.println(result);
		Class clazz=Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
		Method md= clazz.getMethod("encode", byte[].class);  
		DatatypeConverter.printBase64Binary("helloworld".getBytes());
		Object obj=clazz.newInstance();
		int testSize=100000;
		StopWatch sw=new StopWatch("测试base64");
		sw.start("反射");
		for(int i=0;i<testSize;i++){
			md.invoke(obj,"helloworld".getBytes());
		}
		sw.stop();
		sw.start("xml自带");
		for(int i=0;i<testSize;i++){
			DatatypeConverter.printBase64Binary("helloworld".getBytes());
		}
		sw.stop();
		System.out.println(sw.prettyPrint());
		
		//
		//Https https=Https.newGetHttps("http://localhost:8161/admin/queues.jsp").basicAuth("admin:admin");
		//https.execute();
		///Header header=https.getResponseHeader();
		//header.getHeader().put("Cookie",header.getHeader().get("Set-Cookie"));
		//header.getHeader().remove("Set-Cookie");
		//System.out.println(header.getHeader());
		String result=Https.newGetHttps("http://localhost:8161/admin/queues.jsp").basicAuth("admin:admin").execute();
		System.out.println(result);
	}
}
