package org.hw.sml.support.el;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import org.hw.sml.tools.Assert;
import org.hw.sml.tools.ClassUtil;
import org.hw.sml.tools.MapUtils;
import org.hw.sml.tools.Strings;


public  class SmlElContext extends ElContext{
	public SmlElContext(){
		super();
	}
	public BeanType evelBeanType(String elp) throws ElException {
		elp=elp.trim();
		if(elp.startsWith("(")&&elp.endsWith(")")){
			elp=elp.substring(1, elp.length()-1);
		}
		Object value=null;
		try{
			if(elp.startsWith("${")&&elp.endsWith("}")){
				value= getValue(elp);
			}else if(elp.startsWith("#{")&&elp.endsWith("}")){
					String keyElp=elp.substring(2,elp.length()-1);
				if(elp.contains("."))
					value=loopElp(keyElp);
				else if(keyElp.startsWith("(")&&keyElp.endsWith(")")){
					value=evel(keyElp);
				}else if(keyElp.contains("[")&&keyElp.endsWith("]")){
					String elps[]=keyElp.split("\\[");
					String bn=elps[0];int index=Integer.parseInt(elps[1].substring(0,elps[1].length()-1));
					Assert.isTrue(beanMap.containsKey(bn),"bean "+bn+" is not exists!");
					Object b=beanMap.get(bn);
					if(b.getClass().isArray()){
						value=Array.get(b, index);
					}else if(b instanceof List){
						value= ((List<?>)b).get(index);
					}else{
						Assert.isTrue(false, "elp["+elp+"] is not a array or list!");
					}
				}else{
					Assert.isTrue(beanMap.containsKey(keyElp),"bean "+keyElp+" is not exists!");
					value=beanMap.get(keyElp);
				}
			}else{
				String keyP=elp;
				BeanType b=new BeanType();
				if(keyP.startsWith("''")&&keyP.endsWith("''")&&keyP.length()>=4){
					b.setV(keyP.substring(2,keyP.length()-2).charAt(0));b.setC(Character.class);
				}else if(keyP.startsWith("'")&&keyP.endsWith("'")){
					b.setV(keyP.substring(1, keyP.length()-1));b.setC(String.class);
				} else if(keyP.startsWith("\"")&&keyP.endsWith("\"")){
					b.setV(keyP.substring(1,keyP.length()-1).charAt(0));b.setC(char.class);
				}else if((keyP.endsWith("l")||keyP.endsWith("L"))&&keyP.length()>1&&keyP.substring(0,keyP.length()-1).matches("-?\\d+")){
					b.setV(Long.parseLong(keyP.substring(0, keyP.length()-1)));b.setC(keyP.endsWith("L")?Long.class:long.class);
				}else if((keyP.endsWith("d")||keyP.endsWith("D"))&&keyP.length()>1&&keyP.substring(0,keyP.length()-1).matches("^-?(\\d+\\.?\\d*)$")){
					b.setV(Double.parseDouble(keyP.substring(0, keyP.length()-1)));b.setC(keyP.endsWith("D")?Double.class:double.class);
				}else if((keyP.endsWith("f")||keyP.endsWith("F"))&&keyP.length()>1&&keyP.substring(0,keyP.length()-1).matches("^-?(\\d+\\.?\\d*)$")){
					b.setV(Float.parseFloat(keyP.substring(0, keyP.length()-1)));b.setC(keyP.endsWith("F")?Float.class:float.class);
				}else if((keyP.endsWith("s")||keyP.endsWith("S"))&&keyP.length()>1&&keyP.substring(0,keyP.length()-1).matches("-?\\d+")){
					b.setV(Short.parseShort(keyP.substring(0, keyP.length()-1)));b.setC(keyP.endsWith("S")?Short.class:short.class);
				}else if((keyP.endsWith("i")||keyP.endsWith("I"))&&keyP.length()>1&&keyP.substring(0,keyP.length()-1).matches("-?\\d+")){
					b.setV(Integer.parseInt(keyP.substring(0, keyP.length()-1)));b.setC(keyP.endsWith("I")?Integer.class:int.class);
				}else if(keyP.equalsIgnoreCase("true")||keyP.equalsIgnoreCase("false")){			
					b.setV(Boolean.valueOf(keyP));b.setC((keyP.equals("TRUE")||keyP.equals("FLASE"))?Boolean.class:boolean.class);
				}else if(keyP.startsWith("{")&&keyP.endsWith("}")){
					Map<Object,Object> map=MapUtils.newLinkedHashMap();
					String[] kvps=new Strings(keyP.substring(1,keyP.length()-1)).splitToken(',','(',')');
					for(String kvp:kvps){
						if(kvp.length()==0){
							continue;
						}
						String[] kkvp=new Strings(kvp).splitToken(':', '(', ')');
						map.put(evel(kkvp[0]),evel(kkvp[1]));
					}
					b.setV(map);
					b.setC(Map.class);
				}else if(keyP.startsWith("[")&&keyP.endsWith("]")){
					List<Object> lst=MapUtils.newArrayList();
					String[] kvps=new Strings(keyP.substring(1,keyP.length()-1)).splitToken(',','(',')');
					for(String kvp:kvps){
						lst.add(evel(kvp));
					}
					b.setV(lst);
					b.setC(List.class);
				}else{
					b.setV(keyP);b.setC(Object.class);
				}
				return b;
			}
		}catch(Exception e){
			throw new ElException("elp["+elp+"]is error",e);
		}
		return new BeanType(value,value==null?null:value.getClass());
	}
	public  Object loopElp(String elp) throws IllegalArgumentException, IllegalAccessException, ElException{
		String elps[]=new Strings(elp).splitToken('.','(',')');
		Object bean=null;
		if(elps[0].contains("[")&&elps[0].endsWith("]")){
			bean=evel("#{"+elps[0]+"}");
		}if(elps[0].startsWith("(")&&elps[0].endsWith(")")){
			bean=evel(elps[0]);
		}else{
			bean=getBean(elps[0]);
		}
		if(elps.length==1){
			return bean;
		}
		return loopElp(bean,elps[1],elps,1);
	}
	private  Object loopElp(Object bean,String bnelp,String[] ss,int pos) throws IllegalArgumentException, IllegalAccessException, ElException{
		Object value=null;
		if(bnelp.contains("(")&&bnelp.endsWith(")")){
			String mn=bnelp.substring(0,bnelp.indexOf("("));
			String clpP=bnelp.substring(bnelp.indexOf("(")+1,bnelp.length()-1);
			String[] clpBeans=new String[0];
			if(!clpP.equals(""))
			 clpBeans=new Strings(clpP).splitToken(',', '(',')');
			Object[] consts=new Object[clpBeans.length];
			Class<?>[] constCls=new Class<?>[clpBeans.length];
			for(int i=0;i<consts.length;i++){
				String keyP=clpBeans[i];
				BeanType b=evelBeanType(keyP);
				consts[i]=b.getV();
				constCls[i]=b.getC();
			}
			try {
				value=ClassUtil.invokeMethod(bean, mn, constCls, consts);
			}  catch (Exception e) {
				Assert.isTrue(false,"elp-["+bnelp+"] error["+e+"]!");
			}
		}else{
			value= ClassUtil.getFieldValue(bean,bnelp);
		}
		if(value==null){
			return null;
		}
		if(ss.length==pos+1){
			return value;
		}
		return loopElp(value,ss[pos+1],ss,pos+1);
	}
	public String getValue(String key){
		if(key.startsWith("${")&&key.endsWith("}")){
			key=key.substring(2,key.length()-1);
		}
		return properties.get(key);
	}
}
