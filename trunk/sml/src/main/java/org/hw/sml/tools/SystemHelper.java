package org.hw.sml.tools;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.hw.sml.support.ioc.BeanHelper;

public class SystemHelper {
	public static Runtime rt=Runtime.getRuntime();
	public static RuntimeMXBean rm=ManagementFactory.getRuntimeMXBean();
	public static DecimalFormat df=new DecimalFormat("#.##");
	public static AtomicInteger webAccessCount=new AtomicInteger(0);
	public static long getPid(){
		return Long.parseLong(rm.getName().split("@")[0]);
	}
	public static String getHostName(){
		return rm.getName().split("@")[1];
	}
	public static String getServerContextPath(){
		String contextPath=get("sml.tomcat.contextPath","sml.server.contextPath","server.contextPath");
		if(contextPath==null){
			return "";
		}
		return contextPath.startsWith("/")?contextPath.substring(1):contextPath;
	}
	private static String get(String ...strings){
		 return BeanHelper.getsValue(strings);
	}
	public static int getServerPort(){
		try{
			return Integer.parseInt(get("sml.tomcat.port","sml.server.port","server.port"));
		}catch(Exception e){
			return -1;
		}
	}
	public static int activeCount(){
		return Thread.activeCount();
	}
	public static long totalMemory(){
		return rt.totalMemory();
	}
	public static Date getStartTime(){
		return new Date(rm.getStartTime());
	}
	public static long maxMemory(){
		return rt.maxMemory();
	}
	public static double useMemoryUtility(){
		return totalMemory()*100.00/maxMemory();
	}
	public static int availableProcessors(){
		return rt.availableProcessors();
	}
	public static Map<String,Object> status(){
		Map<String,Object> result= new Maps<String,Object>()
				.put("pid",SystemHelper.getPid())
				.put("host",SystemHelper.getHostName())
				.put("useMemory",df.format(SystemHelper.totalMemory()/1024.0/1024))
				.put("maxMemory",df.format(SystemHelper.maxMemory()/1024.0/1024))
				.put("useMemoryUtility",df.format(SystemHelper.useMemoryUtility()))
				//.put("availableProcessors",SystemHelper.availableProcessors())
				.put("upTime",DateTools.sdf_mi().format(SystemHelper.getStartTime()))
				.put("serverPort",SystemHelper.getServerPort())
				.put("serverContextPath",SystemHelper.getServerContextPath())
				.put("activeCount",SystemHelper.activeCount())
				.put("webAccessCount",webAccessCount.intValue())
				.getMap();
				try {
					result.put("remoteIp",InetAddress.getLocalHost().getHostAddress());
				} catch (UnknownHostException e) {
					result.put("remoteIp","localhost");
				}
				return result;
	}
	public static void main(String[] args) {
		System.out.println(status());
	}
	
}
