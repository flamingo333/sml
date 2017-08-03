package org.hw.sml.support.el;

import java.util.List;

public class LogicOperation {
    private LogicOperation(){}
    private static final LogicOperation lo=new LogicOperation();
    public static LogicOperation newInstance(){
    	return lo;
    }
	public static boolean or(List<Object> flags){
		for(Object flag:flags){
			if(Boolean.parseBoolean(flag.toString())) return true;
		}
		return false;
	}
	public static boolean and(List<Object> flags){
		for(Object flag:flags){
			if(!Boolean.parseBoolean(flag.toString())) return false;
		}
		return true;
	}
	public static Number plus(Object n1,Object n2){
		if(n1.toString().contains(".")||n2.toString().contains(".")){
			return Double.parseDouble(n1.toString())+Double.parseDouble(n2.toString());
		}else{
			return Long.parseLong(n1.toString())+Long.parseLong(n2.toString());
		}
	}
	public static Number minus(Object n1,Object n2){
		if(n1.toString().contains(".")||n2.toString().contains(".")){
			return Double.parseDouble(n1.toString())-Double.parseDouble(n2.toString());
		}else{
			return Long.parseLong(n1.toString())-Long.parseLong(n2.toString());
		}
	}
}
