package org.hw.sml.tools;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.DatatypeConverter;
/**
 * httpclient  get|post|put|delete
 * @author wen
 *
 */
public class Https {
	public static final String METHOD_GET="GET";
	public static final String METHOD_POST="POST";
	public static final String METHOD_PUT="PUT";
	public static final String METHOD_DELETE="DELETE";
	byte[] bytes=new byte[512];
	private boolean keepAlive=true;
	private int readTimeout;
	private List<ConnectionPre> connectionLinks=MapUtils.newArrayList();
	private List<ConnectionPre> connectionAfter=MapUtils.newArrayList();
	private Failover[] failovers;
	private boolean bosClose=true;
	private boolean isClose=true;
	
	private static ArrayJsonMapper jsonMapper;
	public static void bindJsonMapper(ArrayJsonMapper arrayJsonMapper){
		jsonMapper=arrayJsonMapper;
	}
	
	public Https withReadTimeout(int readTimeout){
		this.readTimeout=readTimeout;
		return this;
	}
	public Https registerConnectionPre(ConnectionPre connectionPre){
		connectionLinks.add(connectionPre);
		return this;
	}
	public Https registerConnectionAfter(ConnectionPre connectionPre){
		connectionAfter.add(connectionPre);
		return this;
	}
	public Https failover(String ... urls){
		failovers=new Failover[urls.length];
		for(int i=0;i<urls.length;i++){
			failovers[i]=new Failover(urls[i],1);
		}
		return this;
	}
	public static class Failover{
		private String url;
		private int retry=1;
		private int timewait=0;
		public Failover(String url,int retry){
			this.url=url;
			this.retry=retry;
		}
		public Failover(String url,int retry,int timewait){
			this.url=url;
			this.retry=retry;
			this.timewait=timewait;
		}
	}
	public Https failover(Failover ...fos){
		List<Failover> fail=MapUtils.newArrayList();
		for(Failover fo:fos){
			for(int i=0;i<fo.retry;i++){
				fail.add(fo);
			}
		}
		this.failovers=fail.toArray(new Failover[0]);
		return this;
	}
	public Https registerTrust(){
		return registerConnectionPre(new Trust());
	}
	/**
	 *url:  
	 *http:// 
	 *https://
	 *failover:http://localhost:8080/a/b/c,http://localhost:8080/b/c/d 
	 * 
	 */
	private Https(String url){
		if(url.startsWith("failover:")){
			url=url.replaceFirst("failover:","");
			String[] urls=url.split(",");
			if(urls.length>1){
				failovers=new Failover[urls.length-1];
			}
			for(int i=0;i<urls.length;i++){
				if(i==0){
					this.url=urls[i];
				}else{
					failovers[i-1]=new Failover(urls[i],1);
				}
			}
		}else
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
	private boolean cache=false;
	private String boundary;
	private Paramer paramer=new Paramer();
	private Proxy proxy;
	private Header responseHeader;
	public Https charset(String charset){
		this.charset=charset;
		return this;
	}
	public Https cache(boolean cache){
		this.cache=cache;
		return this;
	}
	public Https header(String name,String value){
		getHeader().put(name, value);
		return this;
	}
	public Https param(String name,Object value){
		getParamer().add1(name, value);
		return this;
	}
	public Https methodToPut(){
		return method(METHOD_PUT);
	}
	public Https methodToDelete(){
		return method(METHOD_DELETE);
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
	public Https proxy(String host,int port,String auths){
		return proxy(new Proxy(Proxy.Type.SOCKS,new InetSocketAddress(host,port)), auths);
	}
	public Https proxy(String host,int port){
		return proxy(host, port, null);
	}
	public Https method(String method){
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
	public Https retry(int retry){
		if(retry>1)
			this.failover(new Failover(url,retry-1));
		return this;
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
		public Map<String,Object> getParams(){
			return params;
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
		private Map<String,String> header=new LinkedCaseInsensitiveMap<String>();
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
	public Https bosClose(boolean bosClose){
		this.bosClose=bosClose;
		return this;
	}
	public Https isClose(boolean isClose){
		this.isClose=isClose;
		return this;
	}
	private String responseMessage;
	public String getResponseMessage(){
		return responseMessage;
	}
	public Https buildUrl(String qps){
		if(qps!=null&&(this.method.equals(METHOD_GET)||body!=null)) url+=(url.contains("?")?"&":"?")+qps;
		return this;
	}
	static AtomicInteger urlChoose=new AtomicInteger(0);
	public byte[] query() throws IOException{
		byte[] bs=null;
		try{
			bs=this.query0();
			return bs;
		}catch(IOException e){
			int urlChooseIndx=urlChoose.getAndIncrement();
			if((e instanceof ConnectException||e instanceof SocketTimeoutException||getResponseStatus()==503)&&failovers!=null&&failovers.length>urlChooseIndx){
				this.url=failovers[urlChooseIndx].url;
				try {
					Thread.sleep(failovers[urlChooseIndx].timewait);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				return query();
			}else
				throw e;
		}
	}
	public byte[] query0() throws IOException{
		boolean isOk=true;
		String qps=this.paramer.builder(header.requestCharset);
		buildUrl(qps);
		URL realUrl = new URL(url);
		HttpURLConnection conn = null;
				
		//
		InputStream is=null;
		OutputStream out=null;
		DataOutputStream ds=null;
		try{
			conn=(HttpURLConnection) (proxy==null?realUrl.openConnection():realUrl.openConnection(proxy));
			for(Map.Entry<String,String> entry:header.header.entrySet())
				conn.addRequestProperty(entry.getKey(),entry.getValue());
			if(connectTimeout!=0)
				conn.setConnectTimeout(connectTimeout);
			if(readTimeout>0){
				conn.setReadTimeout(readTimeout);
			}
			conn.setDoOutput(true);
			conn.setRequestMethod(this.method);
			if(url.startsWith("https")&&connectionLinks.isEmpty()){
				registerTrust();
			}
			for(ConnectionPre connectionPre:connectionLinks){
				connectionPre.doConnectionBefore(conn);
			}
			if(!this.method.equals(METHOD_GET)){
				conn.setDoInput(true);
				//conn.setUseCaches(cache);
				out=conn.getOutputStream();
				if(body!=null){
					if(body instanceof String)
						out.write(body.toString().getBytes(header.requestCharset));
					else if(isUpload&&body.getClass().isArray()&&Array.get(body,0) instanceof UpFile){
						ds=new DataOutputStream(out);
						for(Map.Entry<String,Object> entry:this.paramer.params.entrySet()){
							ds.writeBytes("--"+boundary+"\r\n");
							ds.writeBytes("Content-Disposition: form-data; name=\""+entry.getKey()+"\"\r\n");
							ds.writeBytes("\r\n");
							ds.write(entry.getValue().toString().getBytes(this.header.requestCharset));
							ds.writeBytes("\r\n");
						}
						for(UpFile uf:((UpFile[])body)){
							ds.writeBytes("--"+boundary+"\r\n");
							ds.writeBytes("Content-Disposition: form-data; name=\""+uf.formname+"\";filename=\""+uf.name+"\"\r\n");
							ds.writeBytes("Content-Type: application/octet-stream;charset="+header.requestCharset+"\r\n");
							ds.writeBytes("\r\n");
							int dst=-1;
							while((dst=uf.is.read(bytes))!=-1){
								ds.write(bytes,0,dst);
								ds.flush();
							}
							ds.writeBytes("\r\n");
							ds.flush();
						}
						ds.writeBytes("--"+boundary+"--\r\n");
						ds.writeBytes("\r\n");
					}else if(body instanceof InputStream){
						IOUtils.copy(out,(InputStream)body);
					}else{
						out.write((byte[])body);
					}
				}else if(qps!=null){
					out.write(qps.getBytes());
				}
				out.flush();
			}
			conn.connect();
			for(ConnectionPre connectionPre:connectionAfter){
				connectionPre.doConnectionBefore(conn);
			}
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
			isOk=!(e instanceof ConnectException||e instanceof SocketTimeoutException)&&failovers!=null;
			throw e;
		}finally{
				if(conn!=null&&isOk){
					this.responseStatus=conn.getResponseCode();
					this.responseMessage=conn.getResponseMessage();
				}
				if(conn!=null&&!keepAlive&&isOk)
					conn.disconnect();
				if(out!=null)
					out.close();
				if(is!=null&&isClose)
					is.close();
				if(ds!=null)
					ds.close();
				if(bos!=null&&bosClose){
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
	public Https body(InputStream requestBody){
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
	public <T> List<T> queryForList(Class<T> clazz) throws IOException{
		return jsonMapper.toArray(execute(),clazz);
	}
	public <T> T queryForObject(Class<T> clazz) throws IOException{
		if(clazz.isAssignableFrom(String.class)){
			return (T)execute();
		}
		return jsonMapper.toObj(execute(),clazz);
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
		public String formname;
		public String name;
		public InputStream is;
		private static AtomicInteger it=new AtomicInteger(0);
		public UpFile(String name,InputStream is){
			this.name=name;
			this.is=is;
			this.formname="file"+it.getAndIncrement();
		}
		public UpFile(String formname,String filename,InputStream is){
			this.formname=formname;
			this.name=filename;
			this.is=is;
		}
	}
	public static interface ConnectionPre{
		void doConnectionBefore(HttpURLConnection conn) throws IOException;
	}
	public static class Trust implements ConnectionPre{
		public void doConnectionBefore(HttpURLConnection conn) throws IOException {
			TrustManager[] tm = { new MyX509TrustManager() };
	        SSLContext sslContext;
			try {
				sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null,tm,new java.security.SecureRandom());    
				SSLSocketFactory ssf = sslContext.getSocketFactory();
				HttpsURLConnection newConn=(HttpsURLConnection) conn;
				newConn.setSSLSocketFactory(ssf);
				newConn.setHostnameVerifier(new HostnameVerifier() {
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				});
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
		}
	}
	public static  class MyX509TrustManager implements  X509TrustManager{
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}  
	} 
	public static <T> T getForObject(String url,Class<T> clazz,String...urlVs) throws IOException{
		url=buildUrl(url,urlVs);
		return newGetHttps(url).queryForObject(clazz);
	}
	public static <T> T postForObject(String url,Class<T> clazz,Object requestEntity) throws IOException{
		String requestBody=buildRequestBody(requestEntity);
		return newPostBodyHttps(url).body(requestBody).queryForObject(clazz);
	}
	public static <T> List<T> getForList(String url,Class<T> clazz,String ... urlVs) throws IOException{
		url=buildUrl(url,urlVs);
		return newGetHttps(url).queryForList(clazz);
	}
	public static <T> T postForList(String url,Class<T> clazz,Object requestEntity) throws IOException{
		String requestBody=buildRequestBody(requestEntity);
		return newPostBodyHttps(url).body(requestBody).queryForObject(clazz);
	}
	private static String buildUrl(String url,String[] urlVs){
		if(urlVs!=null){
			for(int i=0;i<urlVs.length;i++){
				try {
					url=url.replace("{"+i+"}",URLEncoder.encode(urlVs[i],"utf-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return url;
	}
	private static String buildRequestBody(Object entity){
		if(entity==null){
			return null;
		}else if(entity instanceof String){
			return (String)entity;
		}else{
			return jsonMapper.toJson(entity);
		}
	}
}
