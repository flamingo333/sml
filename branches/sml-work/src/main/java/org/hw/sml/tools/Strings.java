package org.hw.sml.tools;

import java.util.List;

public class Strings {
	private String elp;
	
	public Strings(String elp){
		this.elp=elp;
	}
	public Strings(byte[] bs){
		this.elp=new String(bs);
	}
	public boolean isNull(){
		return this.elp==null;
	}
	public boolean isEmpty(){
		return this.elp==null||this.elp.trim().length()==0;
	}
	public Strings assertNotNull(){
		Assert.isTrue(!isNull(),"elp must not null!");
		return this;
	}
	public String[] splitToken(char splitToken,char includedSplitTokenStart,char includeSplitTokenEnd){
		assertNotNull();
		List<String> eles=MapUtils.newArrayList();
		char token=splitToken;
		char start=includedSplitTokenStart;
		char end=includeSplitTokenEnd;
		boolean isClosed=true;
		boolean isInnerClosed=true;
		StringBuilder sb=new StringBuilder();
		for(char c:elp.toCharArray()){
			if(c==token&&isClosed){
				eles.add(sb.toString());
				isClosed=true;isInnerClosed=true;
				sb=new StringBuilder();
			}else{ 
				if(c==start){
				    if(isClosed)isClosed=false;
				    else if(!isClosed&&isInnerClosed)isInnerClosed=false;
				}else if(c==end){
					if(!isInnerClosed&&!isClosed) isInnerClosed=true;
					else if(isInnerClosed)isClosed=true;
				}
				sb.append(c);
			}
		}
		eles.add(sb.toString());
		return eles.toArray(new String[]{});
	}
	public String toLowerCaseFirst(){
		return elp.substring(0,1).toLowerCase()+elp.substring(1);
	}
	public String toUpperCaseFirst(){
		return elp.substring(0,1).toUpperCase()+elp.substring(1);
	}
}
