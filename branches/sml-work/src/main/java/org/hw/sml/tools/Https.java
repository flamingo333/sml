package org.hw.sml.tools;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;
/**
 * httpclient  get|post
 * @author wen
 *
 */
public class Https {
	
	public static final String METHOD_GET="GET";
	public static final String METHOD_POST="POST";
	byte[] bytes=new byte[512];
	private boolean keepAlive=false;
	private Https(String url){
		this.url=url;
	}
	private OutputStream bos=new ByteArrayOutputStream();
	public static Https newGetHttps(String url){
		return new Https(url);
	}
	public static Https newPostHttps(String url){
		return new Https(url).method(METHOD_POST);
	}
	public static Https newPostBodyHttps(String url){
		Https https= new Https(url).method(METHOD_POST);
		https.getHeader().put("Content-Type","application/json");
		return https;
	}
	public Https keepAlive(boolean ka){
		this.keepAlive=ka;
		return this;
	}
	public Https auth(String type,String credentials){
		getHeader().put("Authorization", type+" "+credentials);
		return this;
	}
	public Https basicAuth(String credentials){
		return auth("Basic",DatatypeConverter.printBase64Binary(credentials.getBytes()));
	}
	public static Https newPostFormHttps(String url){
		Https https= new Https(url).method(METHOD_POST);
		https.getHeader().put("Content-Type","application/x-www-form-urlencoded");
		return https;
	}
	
	public Https bos(OutputStream os){
		this.bos=os;
		return this;
	}
	private String method=METHOD_GET;
	private String charset="utf-8";
	private String url;
	private Header header=new Header("application/json","*/*");
	private Object body;
	private int connectTimeout;
	private boolean isUpload=false;
	private String boundary;
	private Paramer paramer=new Paramer();
	private Proxy proxy;
	private Header responseHeader;
	public Https charset(String charset){
		this.charset=charset;
		return this;
	}
	public Https connectTimeout(int timeout){
		this.connectTimeout=timeout;
		return this;
	}
	public Https proxy(Proxy proxy,String auths){
		this.proxy=proxy;
		if(auths!=null)
		getHeader().put("Proxy-Authorization", "Basic "+DatatypeConverter.printBase64Binary(auths.getBytes()));
		return this;
	}
	private Https method(String method){
		this.method=method;
		return this;
	}
	public Https head(Header header){
		this.header=header;
		return this;
	}
	public String getMethod() {
		return method;
	}
	public String getCharset() {
		return charset;
	}
	public String getUrl() {
		return url;
	}
	public Header getHeader() {
		return header;
	}
	public Https upFile(String boundary){
		isUpload=true;
		this.boundary=boundary;
	   this.header.put("Content-Type","multipart/form-data;boundary="+boundary);
	   return this;
	}
	public Https upFile(){
		return upFile(String.valueOf(System.currentTimeMillis()));
	}
	public Https param(Paramer paramer){
		this.paramer=paramer;
		return this;
	}
	
	public class Paramer{
		private String queryParamStr;
		private Map<String,Object> params=MapUtils.newLinkedHashMap();
		public Paramer(){}
		public Paramer(String queryParamStr){this.queryParamStr=queryParamStr;}
		public Paramer param(String queryParamStr){this.queryParamStr=queryParamStr;return this;}
		public Paramer add(String name,String value){
			return add1(name,value);
		}
		public Paramer add(String name,String[] value){
			return add1(name,value);
		}
		private Paramer add1(String name,Object value){
			if(value!=null)
				params.put(name,value);
			return this;
		}
		public String builder(String charset){
			if(queryParamStr==null&&params.size()>0){
				StringBuilder sb=new StringBuilder();
				int i=0;
				for(Map.Entry<String,Object> entry:params.entrySet()){
					try {
						if(!entry.getValue().getClass().isArray())
							sb.append((sb.lastIndexOf("&")==sb.length()-1?"":"&")+entry.getKey()+"="+URLEncoder.encode(entry.getValue().toString(),charset));
						else{
							String[] ps=(String[]) entry.getValue();
							for(String p:ps)
							sb.append((sb.lastIndexOf("&")==sb.length()-1?"":"&")+entry.getKey()+"="+URLEncoder.encode(p,charset));
						}
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					i++;
				}
				this.queryParamStr=sb.toString();
			}
			return queryParamStr;
		}
	}
	public  class Header{
		public Header(String contentType,String accept){
			this.put("Content-Type",contentType);
			this.put("Accept", accept);
		}
		private String requestCharset=charset;
		private String responseCharset=charset;
		private Map<String,String> header=MapUtils.newLinkedHashMap();
		public Header put(String name,String value){
			if(name==null||value==null){
				return this;
			}
			header.put(name, value);
			String keyToLower=name.toLowerCase().trim();
			String valueToLower=value.toLowerCase().trim();
			try{
				if(isUpload) return this;
				if(valueToLower.contains("charset")){
					if(keyToLower.equals("content-type"))
						this.requestCharset=valueToLower.split("=")[1].replace(";","");
					else if(keyToLower.equals("accept"))
						this.responseCharset=valueToLower.split("=")[1].replace(";","");
				}
				if(keyToLower.equals("content-type")||keyToLower.equals("accept")){
					if(!valueToLower.contains("charset")){
						header.put(name, value+";charset="+charset);
					}
				}
			}catch(Exception e){}
			return this;
		}
		public String getRequestCharset() {
			return requestCharset;
		}
		public String getResponseCharset() {
			return responseCharset;
		}
		public Map<String, String> getHeader() {
			return header;
		}
		public void setHeader(Map<String, String> header) {
			this.header = header;
		}
		
	}
	private int responseStatus;
	public int getResponseStatus(){
		return responseStatus;
	}
	private String responseMessage;
	public String getResponseMessage(){
		return responseMessage;
	}
	public Https buildUrl(String qps){
		if(qps!=null&&(this.method.equals(METHOD_GET)||body!=null)) url+=(url.contains("?")?"&":"?")+qps;
		return this;
	}
	public byte[] query() throws IOException{
		String qps=this.paramer.builder(header.requestCharset);
		buildUrl(qps);
		URL realUrl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) (proxy==null?realUrl.openConnection():realUrl.openConnection(proxy));
		for(Map.Entry<String,String> entry:header.header.entrySet())
			conn.addRequestProperty(entry.getKey(),entry.getValue());
		if(connectTimeout!=0)
			conn.setConnectTimeout(connectTimeout);
		//
		conn.setDoOutput(true);
		conn.setRequestMethod(this.method);
		InputStream is=null;
		OutputStream out=null;
		DataOutputStream ds=null;
		try{
			if(this.method.equals(METHOD_POST)){
				conn.setDoInput(true);
				conn.setUseCaches(false);
				out=conn.getOutputStream();
				if(body!=null){
					if(body instanceof String)
						out.write(body.toString().getBytes(header.requestCharset));
					else if(isUpload&&body.getClass().isArray()&&Array.get(body,0) instanceof UpFile){
						ds=new DataOutputStream(out);
						int i=0;
						for(Map.Entry<String,Object> entry:this.paramer.params.entrySet()){
							ds.writeBytes("--"+boundary+"\r\n");
							ds.writeBytes("Content-Disposition: form-data; name=\""+entry.getKey()+"\"\r\n");
							ds.writeBytes("\r\n");
							ds.write(entry.getValue().toString().getBytes(this.header.requestCharset));
							ds.writeBytes("\r\n");
						}
						for(UpFile uf:((UpFile[])body)){
							ds.writeBytes("--"+boundary+"\r\n");
							ds.writeBytes("Content-Disposition: form-data; name=\"file"+(i++)+"\";filename=\""+uf.name+"\"\r\n");
							ds.writeBytes("Content-Type: application/octet-stream;charset="+header.requestCharset+"\r\n");
							ds.writeBytes("\r\n");
							int dst=-1;
							while((dst=uf.is.read(bytes))!=-1){
								ds.write(bytes,0,dst);
							}
							ds.writeBytes("\r\n");
							ds.flush();
						}
						ds.writeBytes("--"+boundary+"--\r\n");
						ds.writeBytes("\r\n");
					}else
						out.write((byte[])body);
				}else if(qps!=null){
					out.write(qps.getBytes());
				}
				out.flush();
			}
			conn.connect();
			if(conn.getResponseCode()==200)
				is=conn.getInputStream();
			else
				is=conn.getErrorStream();
			if(is==null){
				is=conn.getInputStream();
			}
			int temp=-1;
			while((temp=is.read(bytes))!=-1){
				bos.write(bytes,0,temp);
			}
			responseHeader=new Header(null,null);
			for(Map.Entry<String,List<String>> entry:conn.getHeaderFields().entrySet()){
				responseHeader.put(entry.getKey(),entry.getValue().get(0));
			}
		}catch(IOException e){
			throw e;
		}finally{
			this.responseStatus=conn.getResponseCode();
			this.responseMessage=conn.getResponseMessage();
			
			if(conn!=null&&!keepAlive)
				conn.disconnect();
			if(out!=null)
				out.close();
			if(is!=null)
				is.close();
			if(ds!=null)
				ds.close();
			if(bos!=null){
				bos.close();
			}
		}
		return (bos instanceof ByteArrayOutputStream)?((ByteArrayOutputStream)bos).toByteArray():new byte[0];
	}
	public Https body(String requestBody){
		this.body=requestBody;
		return this;
	}
	public Https body(byte[] requestBody){
		this.body=requestBody;
		return this;
	}
	public Header getResponseHeader(){
		return this.responseHeader;
	}
	public Https body(UpFile ... uf){
		this.body=uf;
		return this;
	}
	public String execute() throws IOException{
		return new String(query(),header.responseCharset);
	}
	
	public Object getBody() {
		return body;
	}
	public Paramer getParamer() {
		return paramer;
	}
	public static UpFile newUpFile(String name,InputStream is){
		return new UpFile(name, is);
	}
	public static class UpFile{
		public String name;
		public InputStream is;
		public UpFile(String name,InputStream is){
			this.name=name;
			this.is=is;
		}
	}
}
