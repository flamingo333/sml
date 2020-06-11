package org.hw.sml.support.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.hw.sml.tools.DateTools;

public class CronExpression
{
  public static boolean igNoreSecondCheck=false;
  private String elp;
  private static String[] M_ENUMS = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };

  private static String[] W_ENUMS = { "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN" };
   
  public CronExpression() {
  }
  String ss,mi,hh,dd,mm,ww,yy;
  boolean bankNum,isInit;
  private void init(){
	  isInit=true;
	  String[] ts = this.elp.split(" ");
	  bankNum = ts.length > 5;
	  ss = ts[0];mi = ts[1];hh = ts[2];dd = ts[3]; mm = ts[4];ww =ts[5];yy = ts.length == 7 ? ts[6] : "*";
  }
  public CronExpression(String elp) { 
	  this.elp = elp.toUpperCase().replaceAll("\\s{1,}", " ");
	  init();
  }
  
  public boolean valid(Date now) {
	if(!isInit){
		init();
	}
    if (bankNum) {
      String seconds = new SimpleDateFormat("s").format(now);
      boolean secondsRight = igNoreSecondCheck||isRight(ss, seconds);
      if (secondsRight) {
        String minute = new SimpleDateFormat("m").format(now);
        boolean miRight = isRight(mi, minute);
        if (miRight) {
          String hour = new SimpleDateFormat("H").format(now);
          boolean hourRight = isRight(hh, hour);
          if (hourRight) {
            String dayOfMonth = new SimpleDateFormat("d").format(now);
            boolean dayRight = (isRight(dd, dayOfMonth)) || ((dd.equals("L")) && (new SimpleDateFormat("dd").format(DateTools.addDays(now, 1)).equals("01")));
            if (dayRight) {
              String month = new SimpleDateFormat("M").format(now);
              for (int i = 1; i <= M_ENUMS.length; i++) {
                mm = mm.replace(M_ENUMS[(i - 1)], String.valueOf(i));
              }
              boolean monthRight = isRight(mm, month);
              if (monthRight) {
                String dayOfWeek = getWeekDay(now);
                for (int i = 1; i <= W_ENUMS.length; i++) {
                  ww = ww.replace(W_ENUMS[(i - 1)], String.valueOf(i));
                }
                boolean weekRight = (isRight(ww, dayOfWeek)) || 
                  ((dayOfWeek.equals(ww.substring(0, 1))) && 
                  (ww.contains("L")) && (Integer.parseInt(new SimpleDateFormat("d").format(DateTools.addDays(now, 7))) < Integer.parseInt(dayOfWeek))) || (
                  (ww.contains("#")) && (ww.split("#")[1].equals(String.valueOf(getWeekNumOfMonth(now)))));

                if (weekRight) {
                  String year = new SimpleDateFormat("yyyy").format(now);
                  boolean yearRight = isRight(yy, year);
                  return yearRight;
                }
              }
            }
          }
        }
      }
    }
    return false;
  }
  public boolean isExecuteNow() {
    try {
      return valid(new Date()); } catch (Exception e) {
    }
    return false;
  }
  private boolean contains(String eles,String cur){
	  boolean flag=false;
	  for(String ele:eles.split(",")){
		  if(ele.startsWith("0")&&ele.length()==2){
			  flag=ele.equals("0"+cur);
		  }else{
			  flag=ele.equals(cur);
		  }
		  if(flag){
			  break;
		  }
	  }
	  return flag;
  }
  private boolean isRight(String eleStr, String currentStr) {
    int currentInt = Integer.parseInt(currentStr);
    boolean moreR = !eleStr.matches("\\d+/\\d+");
    boolean result = false;
    if (moreR) {
      if ((eleStr.equals("*")) || (eleStr.equals("?")) || (contains(eleStr,currentStr))) {
        result = true;
      }
      else if (eleStr.contains("-")) {
        String[] es = eleStr.split("-");
        result = (currentInt >= Integer.parseInt(es[0])) && (currentInt <= Integer.parseInt(es[1]));
      }
    }
    else
    {
      String[] cis = eleStr.split("/");
      int mint = Integer.parseInt(cis[0]);
      int rangeint = Integer.parseInt(cis[1]);
      result = (currentInt - mint >= 0) && ((currentInt - mint) % rangeint == 0);
    }
    return result;
  }

  private String getWeekDay(Date date)
  {
    @SuppressWarnings("deprecation")
	String week_day = String.valueOf(date.getDay());
    if (week_day.equals("0")) {
      return "7";
    }
    return week_day;
  }
  private int getWeekNumOfMonth(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    return cal.get(4);
  }
  public String getElp() {
    return this.elp;
  }
  public CronExpression setElp(String elp) {
    this.elp = elp;
    isInit=false;
    return this;
  }
  public Date nextTime(){
	  return getNextTime(DateTools.trunc(new Date(),Calendar.MINUTE));
  }
  public Date getNextTime(Date date){
	  boolean checkMin=mi!="*"&&mi.matches("\\d+"),
			  checkHour=hh!="*"&&hh.matches("\\d+"),
			  checkDay=dd.matches("\\d+")&&(dd!="*"||dd!="?")&&(ww.equals("*")||ww.equals("?"));
	  Calendar cal=Calendar.getInstance();
	  cal.setTime(date);
	  int count=0;
	  int maxLength=20000;
	  for(int i=1;i<Integer.MAX_VALUE;i++){
		  cal.add(Calendar.MINUTE,1);
		  boolean flag=valid(cal.getTime());
		  if(count++==maxLength) return new Date(0);
		  if(flag){
			  return cal.getTime();
		  }
		  if(checkMin&&DateTools.getMinite(cal.getTime())==Integer.parseInt(mi)){
			  break;
		  }
	  }
	  for(int i=1;i<Integer.MAX_VALUE;i++){
		  cal.add(Calendar.HOUR_OF_DAY,1);
		  boolean flag=valid(cal.getTime());
		  if(count++==maxLength) return new Date(0);
		  if(flag){
			  return cal.getTime();
		  }
		  if(checkHour&&DateTools.getHour(cal.getTime())==Integer.parseInt(hh)){
			  break;
		  }
	  }
	  for(int i=1;i<Integer.MAX_VALUE;i++){
		  cal.add(Calendar.DAY_OF_MONTH,1);
		  boolean flag=valid(cal.getTime());
		  if(count++==maxLength) return new Date(0);
		  if(flag){
			  return cal.getTime();
		  }
		  if(checkDay&&DateTools.getDay(cal.getTime())==Integer.parseInt(dd)){
			  break;
		  }
	  }
	  for(int i=1;i<Integer.MAX_VALUE;i++){
		  cal.add(Calendar.MONTH,1);
		  boolean flag=valid(cal.getTime());
		  if(count++==maxLength) return new Date(0);
		  if(flag){
			  return cal.getTime();
		  }
	  }
	  return date;
  }
  public static void main(String[] args) {
	 Date date=DateTools.trunc(new Date(),Calendar.MINUTE);
	 CronExpression ce=new CronExpression("0 0 9 1 * ?");
	 StopWatch sw=new StopWatch();
	 for(int i=0;i<20;i++){
		 sw.start(i+"");
		 date=ce.getNextTime(date);
		 System.err.println(DateTools.sdf_mi().format(date));
	 }
	 System.out.println(sw.prettyPrint());
  }
}