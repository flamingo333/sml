package org.hw.sml.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * 
 * 时间封装类，用于时间的处理  
 * parse  对常用时间条件的处理基本可以解析适配常用的格式
 * @author hw
 *
 */
public class DateTools {
	private static final DateTools dt=new DateTools();
	public static DateTools newInstance(){
		return dt;
	}
	public static final long DAY_TIME_MILLS = 24 * 60 * 60 * 1000;
	public static final long HOUR_TIME_MILLS = 60 * 60 * 1000;
	public static Date date(){
		return new Date();
	}
	public static String uuid(){
		return UUID.randomUUID().toString();
	}
	public static SimpleDateFormat sdf_mi(){
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	public static SimpleDateFormat sdf_mis(){
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	}
	/** 格式化取整时间 */
	public static Date trunc(Date date, int type) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if (type == Calendar.DAY_OF_MONTH) {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.MILLISECOND, 0);
		} else if (type == Calendar.HOUR_OF_DAY) {
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
		} else if (type == Calendar.MINUTE) {
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.add(Calendar.MINUTE,-cal.get(Calendar.MINUTE)%10);//对分钟进行整十处理
		} else if (type == Calendar.MONTH) {
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.MILLISECOND, 0);
		}else if(type==Calendar.DAY_OF_WEEK){
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.DAY_OF_WEEK,2);
		}else if(type==1000){
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
		}
		return cal.getTime();
	}

	/** 增加天数 */
	public static Date addDays(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, days);
		return cal.getTime();
	}

	/** 增加小时数 */
	public static Date addHours(Date date, int hours) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR_OF_DAY, hours);
		return cal.getTime();
	}

	/** 增加月数 */
	public static Date addMonths(Date date, int months) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, months);
		return cal.getTime();
	}
	/**增加年数*/
	public static Date addYears(Date date,int years){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.YEAR, years);
		return cal.getTime();
	}
	/** 增加分钟数 */
	public static Date addMinutes(Date date, int minutes) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MINUTE, minutes);
		return cal.getTime();
	}
	public static Date add(int type,Date date,int times){
		if(type==Calendar.DAY_OF_MONTH){
			return addDays(date, times);
		}else if(type==Calendar.YEAR){
			return addYears(date, times);
		}else if(type==Calendar.HOUR_OF_DAY){
			return addHours(date, times);
		}else if(type==Calendar.MONTH){
			return addMonths(date, times);
		}else if(type==Calendar.MINUTE){
			return addMinutes(date, times);
		}else if(type==Calendar.DAY_OF_WEEK){
			return addDays(date,times*7);
		}
		return date;
	}
	public static Date addSeconds(Date date, int seconds) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.SECOND, seconds);
		return cal.getTime();
	}
	public static Date addMills(Date date,int mills){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MILLISECOND,mills);
		return cal.getTime();
	}

	public static Date parseDate(String timestamp, String... format) {
		String[] fms = format;
		SimpleDateFormat sdf = null;
		for (String fm : fms) {
			if (timestamp.length() == fm.length()) {
				sdf = new SimpleDateFormat(fm);
				try {
					return sdf.parse(timestamp);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	public static int getYear(Date date){
		return get(date,Calendar.YEAR);
	}
	public static int getDay(Date date){
		return get(date,Calendar.DAY_OF_MONTH);
	}
	public static int getDayOfWeek(Date date){
		return get(date,Calendar.DAY_OF_WEEK);
	}
	public static int getMonth(Date date){
		return get(date,Calendar.MONTH)+1;
	}
	public static int getHour(Date date){
		return get(date,Calendar.HOUR_OF_DAY);
	}
	public static int getMinite(Date date){
		return get(date,Calendar.MINUTE);
	}
	public static int getSecond(Date date){
		return get(date,Calendar.SECOND);
	}
	public static int get(Date date,int field){
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		return cal.get(field);
	}
	/**
	 * 
	 * @param timestamp
	 * @return
	 */
	public static Date parse(String timestamp){
		if(timestamp==null||timestamp.trim().length()==0){
			return null;
		}
		if(timestamp.matches("\\d{13}")){
			return new Date(Long.parseLong(timestamp));
		}
		if(timestamp.matches("\\d{4}/\\d{1,2}/\\d{1,2}")){
			try {
				return new SimpleDateFormat("yyyy/MM/dd").parse(timestamp);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if(timestamp.matches("\\d{4}/\\d{1,2}/\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}")){
			try {
				return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(timestamp);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		timestamp=trim(timestamp,' ','-',':','_','年','月','日','时','分','/','.','T','Z');
		int length=timestamp.length();
		String format="yyyyMMddHHmmssS";
		Date date=null;
		try {
			return new SimpleDateFormat(format.substring(0,length)).parse(timestamp);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	public static Date getFormatTime(String time,String format){
		try {
			return new SimpleDateFormat(format).parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String getFormatTime(Date time,String format){
		if(time==null){
			return null;
		}
		return new SimpleDateFormat(format).format(time);
	}

	public static String trim(String dateformat,char ...chs){
		for(char c:chs)
			dateformat=dateformat.replace(String.valueOf(c),"");
		
		return dateformat;
	}
	public static List<String> buildTime(Date startTime,Date endTime,String format,int minute){
		List<String> result=new ArrayList<String>();
		Date temp=startTime;
		for(int i=0;i<10000;i++){
			result.add(getFormatTime(temp,format));
			temp=addMinutes(temp,minute);
			if(temp.after(endTime)) break;
		}
		return result;
	}
	public static Date parseFromString(String target,String mather,int index){
		List<String> mathers=RegexUtils.matchGroup(mather,target);
		if(mathers.size()<index){
			return null;
		}else{
			return parse(mathers.get(index));
		}
	}
	
	public static void main(String[] args) {
		System.out.println(DateTools.parse("2045"));
	}
	
}
