package org.hw.sml.support.time;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.hw.sml.tools.DateTools;

public class CronExpression
{
  private String elp;
  private static String[] M_ENUMS = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };

  private static String[] W_ENUMS = { "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN" };

  public CronExpression() {
  }
  public CronExpression(String elp) { this.elp = elp.toUpperCase().replaceAll("\\s{1,}", " "); }

  public boolean valid(Date now) {
    String[] ts = this.elp.split(" ");

    boolean bankNum = ts.length > 5;

    if (bankNum) {
      String ss = ts[0]; String mi = ts[1]; String hh = ts[2]; String dd = ts[3]; String mm = ts[4]; String ww = ts[5]; String yy = ts.length == 7 ? ts[6] : "*";
      String seconds = new SimpleDateFormat("s").format(now);
      boolean secondsRight = isRight(ss, seconds);

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

  private boolean isRight(String eleStr, String currentStr) {
    int currentInt = Integer.parseInt(currentStr);
    boolean moreR = !eleStr.matches("\\d+/\\d+");
    boolean result = false;
    if (moreR) {
      if ((eleStr.equals("*")) || (eleStr.equals("?")) || (Arrays.asList(eleStr.split(",")).contains(currentStr))) {
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
    return this;
  }
}