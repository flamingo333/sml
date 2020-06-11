package org.hw.sml.report.model;

import java.io.Serializable;
import java.util.Map;

import org.hw.sml.core.build.SqlFilterHelper;
import org.hw.sml.tools.MapUtils;
/**
 * 对比   operator 操作符  value值
 * @author hw
 * in  
 */
public class Operator  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 449881066045875921L;
	private String operator;
	private String value;
	private String type;
	private String split=",";
	
	public Operator() {
		super();
	}
	public Operator(String operator, String value) {
		super();
		this.operator = operator;
		this.value = value;
	}
	public boolean isLike(){
		if(operator==null){
			return false;
		}
		return this.operator.trim().equalsIgnoreCase("like")||this.operator.trim().equalsIgnoreCase("ilike")||this.operator.trim().equalsIgnoreCase("not like")||this.operator.trim().equalsIgnoreCase("not ilike");
	}
	public boolean isIlike(){
		if(isLike()){
			if(this.operator.trim().contains("ilike")){
				return true;
			}
		}
		return false;
	}
	
	public String getOperator() {
		return operator;
	}
	private static Map<String,String> ops=MapUtils.invert(SqlFilterHelper.OPERATORS);
	public void setOperator(String operator) {
		this.operator = operator;
		if(ops.containsKey(this.operator)){
			this.operator=ops.get(operator);
		}
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public boolean isIn() {
		if(operator==null){
			return false;
		}
		return this.operator.trim().equalsIgnoreCase("in")||this.operator.trim().equalsIgnoreCase("not in");
	}
	public String getSplit() {
		return split;
	}
	public void setSplit(String split) {
		this.split = split;
	}

	
	
}