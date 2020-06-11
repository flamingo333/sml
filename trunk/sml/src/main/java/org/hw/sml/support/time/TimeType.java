package org.hw.sml.support.time;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
/**
 * 时间类型枚举
 * @author hw
 *[min1,min5,min10,15min,hour,day,week,month]
 */


public enum TimeType {
	min1("min1"),
	min5("min5"),
	min10("min10"),
	min15("min15"),
	min30("min30"),
	hour("hour"),
	day("day"),
	week("week"),
	month("month"),
	cron("cron");
	private String value;
	private TimeType(String value) {
	     this.value = value;
	}
	@Override
	public String toString() {
	      return this.value;
	}
	
	
	//下面为辅助操作
	private static final Map<String, String> nameMap = new HashMap<String, String>();
	private static final Map<String, TimeType> valueMap = new HashMap<String, TimeType>();

	static {
		for (TimeType type : EnumSet.allOf(TimeType.class)) {
			nameMap.put(type.name(), type.value);
			valueMap.put(type.value, type);
		}
	}
	public static String getValue(String name){
		String value=nameMap.get(name);
		return value==null?name:value;
	}
	public static TimeType getEnum(String value){
		return valueMap.get(getValue(value));
	}
	public static TimeType[] getEnums(String[] values){
		TimeType[] ats=new TimeType[values.length];
		for(int i=0;i<values.length;i++){
			ats[i]=getEnum(values[i]);
		}
		return ats;
	}
}
