package org.hw.sml.report.model;


public class ResultVo {
	private Object data;
	private String message;
	private int code=200;
	private String stackTrace;
	public ResultVo(Object data){
		this.data=data;
	}
	public ResultVo(int code,String message){
		this.code=code;
		this.message=message;
	}
	public ResultVo(int code,Exception e){
		this.code=code;
		this.message=e.getMessage();
		this.stackTrace=getErrInfo(e,20);
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getStackTrace() {
		return stackTrace;
	}
	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
	public static String getErrInfo(Exception e,int size){
		StringBuilder sb=new StringBuilder();
		int i=0;
		sb.append(e.toString());
		for(StackTraceElement ste:e.getStackTrace()){
			i++;
			sb.append("\n\t");
			sb.append(ste);
			if(i>size){
				break;
			}
		}
		return sb.toString();
	}
}
