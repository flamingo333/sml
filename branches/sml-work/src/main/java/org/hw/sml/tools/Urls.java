package org.hw.sml.tools;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
/**
 * 统一多参数资源连接操作
 * 按  协议://[用户名:密码@]hostip[:port]/prepath/lastpath?k=v&k=v
//ftp://username:password@10.221.232.135:21/path/file.name?a=1&b=2&c=3&poolClass=
//redis://password@localhost:1679/0?maxTotal=8&maxIdle=19
//jdbc://username:password@10.221.247.43:1521/oracle?servername=ipms&maxTotal=8&maxIdle=19&poolClass=
 * @author wen
 *
 */
public class Urls {
	private String url;
	private String schema;
	private String host;
	private int port;
	private String username;
	private String password;
	private String query;
	private String path;
	private String prePath;
	private String lastPath;
	private Map<String,String> params=MapUtils.newHashMap();
	public Urls(){}
	public Urls(String url){
		this.url=url;
		parse();
	}
	private boolean hasParse;
	public void parse(){
		if(hasParse){
			return;
		}
		hasParse=true;
		String[] so=url.split("://");
		Assert.isTrue(so.length==2, String.format("url [url error]"));
		this.schema=so[0];
		String soo=so[1];
		int spritIndex=soo.indexOf("/");
		if(spritIndex>-1){
			String uri=soo.substring(soo.indexOf("/")+1);
			boolean noQuery=uri.indexOf("?")==-1;
			this.path=uri.substring(0,noQuery?uri.length():uri.indexOf("?"));
			this.lastPath=path.substring(path.lastIndexOf("/")+1);
			boolean isFile=this.lastPath.contains(".");
			if(isFile){
				if(!this.path.equals(this.lastPath))
					this.prePath=path.substring(0,path.lastIndexOf("/"));
				else
					this.prePath="";
			}else{
				this.prePath=this.path;
				this.lastPath=null;
			}
			if(!noQuery){
				this.query=uri.substring(uri.indexOf("?")+1);
				this.params=decodeQueryString(query);
			}
		}
		//
		String hostInfos=soo.substring(0,soo.indexOf("/")==-1?soo.length():soo.indexOf("/"));
		String addr=hostInfos.substring(hostInfos.lastIndexOf("@")==-1?0:hostInfos.lastIndexOf("@")+1);
		String[] addrs=addr.split(":");
		if(addrs.length>0) host=addrs[0];
		if(addrs.length>1) port=Integer.parseInt(addrs[1]);
		if(hostInfos.contains("@")){
			String authInfo=hostInfos.substring(0,hostInfos.lastIndexOf("@"));
			if(authInfo.contains(":")){
				this.username=authInfo.substring(0,authInfo.indexOf(":"));
				this.password=authInfo.substring(authInfo.indexOf(":")+1);
			}else{
				this.password=authInfo;
			}
		}
	}
	public String getUrl() {
		return url;
	}
	public String getSchema() {
		return schema;
	}
	public String getHost() {
		return host;
	}
	public int getPort() {
		return port;
	}
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public String getQuery() {
		return query;
	}
	public String getPath() {
		return path;
	}
	public String getPrePath() {
		return prePath;
	}
	public String getLastPath() {
		return lastPath;
	}
	public Map<String, String> getParams() {
		return params;
	}
	public static Map<String,String> decodeQueryString(String query){
		Map<String,String> params=MapUtils.newLinkedHashMap();
		String[] ps=query.split("&");
		for(String p:ps){
			String[] kvs=p.split("=");
			if(kvs.length==2&&kvs[0].length()>0)
				params.put(kvs[0],decode(kvs[1]));
		}
		return params;
	}
	public static Urls newFtpUrls(String url){
		Urls urls=new Urls(url);
		if(urls.getPort()==0){
			urls.port=21;
		}
		return urls;
	}
	public static String encode(String name){
		try {
			return URLEncoder.encode(name,"utf-8");
		} catch (UnsupportedEncodingException e) {
			return name;
		}
	}
	public static String decode(String name){
		try {
			return URLDecoder.decode(name,"utf-8");
		} catch (UnsupportedEncodingException e) {
			return name;
		}
	}
}
