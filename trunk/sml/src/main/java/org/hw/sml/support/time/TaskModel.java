package org.hw.sml.support.time;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * 调度父类
 * @author hw
 *
 */

public class TaskModel implements Serializable{
	private static final long serialVersionUID = -7035741153143372300L;
	
	private String elp;//crontab  0 0 10,14,16 * * ?
	
	private String timeType;
	/**
	 * 调度分钟点，0-59，多个‘,’分隔
	 */
	private String timeInvokePoint="00";
	/**
	 * 调度时间集，根据timeType枚举hour:0-59,day,0-24,week0-7
	 */
	private String timePoint="00";
	/**
	 * 是否启用
	 */
	private String enabled="1";//1、启动、0、停用
	
	
	public void init(){
		if(elp!=null){
			if(elp.split(" ").length>5&&!elp.startsWith("cron|")){
				elp="cron|"+elp;
			}
			String[] elps=elp.split("\\|");
 			if(elps.length>=1){
				timeType=elps[0];
			}
 			if(elps.length>=2){
 				timeInvokePoint=elps[1];
 			}
 			if(elps.length>=3){
 				timePoint=elps[2];
 			}
		}
	}
	
	
	public String getElp() {
		return elp;
	}

	public void setElp(String elp) {
		this.elp = elp;
	}
	public boolean isExecuteNow(){
	    	boolean flag=false;
	    	if(timeType.equalsIgnoreCase(TimeType.min1.toString())){
	    		flag=true;
	    	}else if(timeType.equalsIgnoreCase(TimeType.min5.toString())){
	    		flag=(Integer.parseInt(getMinute())%5==0);
	    	}else if(timeType.equalsIgnoreCase(TimeType.min10.toString())){
	    		flag=(Integer.parseInt(getMinute())%10==0);
	    	}else if(timeType.equalsIgnoreCase(TimeType.min15.toString())){
	    		flag=(Integer.parseInt(getMinute())%15==0);
	    	}else if(timeType.equalsIgnoreCase(TimeType.hour.toString())){
	    		flag=indexOf(getArrayTimeInvokePoint(),getMinute())>-1;
	    	}else if(timeType.equalsIgnoreCase(TimeType.day.toString())){
	    		flag=indexOf(getArrayTimepoint(),getHour24())>-1&&indexOf(getArrayTimeInvokePoint(),getMinute())>-1;
	    	}else if(timeType.equalsIgnoreCase(TimeType.week.toString())){
	    		flag=indexOf(getArrayTimepoint(),getWeekHour())>-1&&indexOf(getArrayTimeInvokePoint(),getMinute())>-1;
	    	}else if(timeType.equalsIgnoreCase(TimeType.month.toString())){
	    		flag=indexOf(getArrayTimepoint(),getMonthHour())>-1&&indexOf(getArrayTimeInvokePoint(),getMinute())>-1;
	    	}else if(timeType.equalsIgnoreCase(TimeType.cron.toString())){
	    		flag=new CronExpression(timeInvokePoint).isExecuteNow();
	    	}
	    	return flag;
	   }
	  private String[] getArrayTimeInvokePoint() {
		  if(timeInvokePoint==null){
				return new String[]{};
			}
			return trim(this.timeInvokePoint.split(","));
	}


	private String[] getArrayTimepoint() {
		  if(timePoint==null){
				return new String[]{};
			}
			return trim(this.timePoint.split(","));
	  }
	public int indexOf(String[] array,String ele){
			for(int i=0;i<array.length;i++){
				if(array[i].equals(ele)){
					return i;
				}
			}
			return -1;
		}
	  protected String[] trim(String[] ori){
			if(ori==null){
				return null;
			}
			String[] newStrs=new String[ori.length];
			for(int i=0;i<ori.length;i++){
				newStrs[i]=ori[i].trim();
			}
			return newStrs;
		}
		private String getHour24(){
			String hours=new SimpleDateFormat("HH").format(new Date());
			return hours;
		}
		private String getMinute(){
			String minute=new SimpleDateFormat("mm").format(new Date());
			return minute;
		}
		private String getWeekHour(){
			String week_hour=getWeekDay()+"_"+getHour24();
			return week_hour;
		}
		private String getMonthHour(){
			String month_hour=new SimpleDateFormat("dd_HH").format(new Date());
			return month_hour;
		}
		@SuppressWarnings("deprecation")
		private String getWeekDay(){
			String week_day=lpad(String.valueOf(new Date().getDay()));
			if(week_day.equals("00")){
				return "07";
			}
			return week_day;
		}
		
		@SuppressWarnings("unused")
		private String getMonthDay(){
			String month_day=new SimpleDateFormat("dd").format(new Date());
			return month_day;
		}
		
		private String lpad(String t){
			if(t.length()<2){
				return "0"+t;
			}
			return t;
		}
		
		public String getEnabled() {
			return enabled;
		}
		public void setEnabled(String enabled) {
			this.enabled = enabled;
		}
		public String getTimeType() {
			return timeType;
		}
		public void setTimeType(String timeType) {
			this.timeType = timeType;
		}
		public String getTimeInvokePoint() {
			return timeInvokePoint;
		}
		public void setTimeInvokePoint(String timeInvokePoint) {
			this.timeInvokePoint = timeInvokePoint;
		}
		public String getTimePoint() {
			return timePoint;
		}
		public void setTimePoint(String timePoint) {
			this.timePoint = timePoint;
		}
	
		public static void main(String[] args) {
			TaskModel tm=new TaskModel();
			tm.setElp("cron|0 1/10 15 18 * *");
			tm.init();
			System.out.println(tm.isExecuteNow());
		}
}
