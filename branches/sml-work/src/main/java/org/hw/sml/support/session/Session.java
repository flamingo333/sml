package org.hw.sml.support.session;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hw.sml.support.LoggerHelper;

import com.eastcom_sw.inas.core.service.tools.DateTools;
/**
 * 用于控制接口并发访问及每天访问次数限定目前实现为天
 * @author wen
 *
 */
public abstract class Session implements Serializable{
	protected int maxOnline=-1;
	protected int currentOnline;
	 class Access implements Serializable{
		private String id;
		//累计访问次数
		private long accessDistCount;
		//峰值在线访问控制
		private int maxAccessCountOnline=-1;
		//天访问峰值
		private int maxAccessCountPerDay=-1;
		//当前访问控制
		private int currentAccessCountOnline;
		private int accessCountPerDay;
		private Date first_access_time=new Date();
		private Date last_access_time;
		public synchronized void access() throws AccessWeakException{
			Date date=new Date();
			if(DateTools.addDays(first_access_time,1).before(date)){
				reset();
			}
			this.last_access_time=date;
			accessDistCount++;
			if(isAccess()){
				currentAccessCountOnline++;
				accessCountPerDay++;
				currentOnline++;
			}else{
				throw new  AccessWeakException();
			}
		}
		private boolean isAccess() {
			return (currentAccessCountOnline<maxAccessCountOnline||maxAccessCountOnline==-1)
					&&(maxAccessCountPerDay==-1||accessCountPerDay<maxAccessCountPerDay)
					&&(maxOnline==-1||currentOnline<maxOnline);
		}
		public synchronized void leave(){
			if(currentAccessCountOnline>0){
				currentAccessCountOnline--;
			}
			if(currentOnline>0)
			currentOnline--;
		}
		public synchronized void reset(){
			this.accessCountPerDay=0;
			this.first_access_time=new Date();
			LoggerHelper.getLogger().info(getClass(),"reset session");
		}
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public long getAccessDistCount() {
			return accessDistCount;
		}
		public void setAccessDistCount(long accessDistCount) {
			this.accessDistCount = accessDistCount;
		}
		
		public int getMaxAccessCountOnline() {
			return maxAccessCountOnline;
		}
		public void setMaxAccessCountOnline(int maxAccessCountOnline) {
			this.maxAccessCountOnline = maxAccessCountOnline;
		}
		
		public int getCurrentAccessCountOnline() {
			return currentAccessCountOnline;
		}
		public void setCurrentAccessCountOnline(int currentAccessCountOnline) {
			this.currentAccessCountOnline = currentAccessCountOnline;
		}
		public Date getFirst_access_time() {
			return first_access_time;
		}
		public void setFirst_access_time(Date first_access_time) {
			this.first_access_time = first_access_time;
		}
		public Date getLast_access_time() {
			return last_access_time;
		}
		public void setLast_access_time(Date last_access_time) {
			this.last_access_time = last_access_time;
		}
		public int getMaxAccessCountPerDay() {
			return maxAccessCountPerDay;
		}
		public void setMaxAccessCountPerDay(int maxAccessCountPerDay) {
			this.maxAccessCountPerDay = maxAccessCountPerDay;
		}
		public int getAccessCountPerDay() {
			return accessCountPerDay;
		}
		public void setAccessCountPerDay(int accessCountPerDay) {
			this.accessCountPerDay = accessCountPerDay;
		}
		
		
	}
	protected Map<String,Access> accessContain=new LinkedHashMap<String,Access>();
	public abstract void verify(String id) throws AccessUnAuthException;
	public void access(String id) throws AccessUnAuthException,AccessWeakException{
		verify(id);
		getAccess(id).access();
	}
	public void leave(String id){
		getAccess(id).leave();
	}
	public Access getAccess(String id){
		return accessContain.get(id);
	}
	
	class AccessWeakException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	class AccessUnAuthException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	public Map<String, Access> getAccessContain() {
		return accessContain;
	}
	public void setAccessContain(Map<String, Access> accessContain) {
		this.accessContain = accessContain;
	}
	public synchronized void clear(){
		accessContain.clear();
	}
	
	public int getMaxOnline() {
		return maxOnline;
	}
	public void setMaxOnline(int maxOnline) {
		this.maxOnline = maxOnline;
	}
	public int getCurrentOnline() {
		return currentOnline;
	}
	public void setCurrentOnline(int currentOnline) {
		this.currentOnline = currentOnline;
	}
	
}
