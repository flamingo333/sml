package org.hw.sml.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class IOUtils {
	public static String toString(InputStream is) throws IOException{
		return toString(is,Charset.defaultCharset().name());
	}
	public static String toString(InputStream is, String charset)
			throws IOException {
		Assert.notNull(is, "inputstream is null!");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(baos, is);
		return baos.toString(charset);
	}
	public static byte[] toBytes(InputStream is, String charset) throws IOException{
		Assert.notNull(is, "inputstream is null!");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(baos, is);
		return baos.toByteArray();
	}
	public static String toString(File file,String charset) throws FileNotFoundException, IOException{
		InputStream is=new FileInputStream(file);
		String result=  toString(is,charset);
		safeClose(is);
		return result;
	}
	public static String toString(String filePath,String charset) throws FileNotFoundException, IOException{
		return toString(new File(filePath), charset);
	}
	public static void copy(OutputStream os,InputStream is) throws IOException{
		byte[] bs = new byte[512];
		int temp = -1;
		while ((temp = is.read(bs)) != -1) {
			os.write(bs, 0, temp);
		}
	}
	public static String parseByte2HexStr(byte buf[]) {
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < buf.length; i++) {
	        String hex = Integer.toHexString(buf[i] & 0xFF);
	        if (hex.length() == 1) {
	            hex = '0' + hex;
	        }
	            sb.append(hex.toUpperCase());
	        }
	        return sb.toString();
	}
	public static byte[] parseHexStr2Byte(String hexStr) {
	    if (hexStr.length() < 1)
	        return null;
	        byte[] result = new byte[hexStr.length() / 2];
	        for (int i = 0; i < hexStr.length() / 2; i++) {
	            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
	            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
	            result[i] = (byte) (high * 16 + low);
	        }
	        return result;
	}
	public static <T> long readLine(InputStream is,String charset,Process<T> process){
		BufferedReader br=null;
		try {
			br=new BufferedReader(new InputStreamReader(is,charset));
			String line=null;
			long start=0;
			while((line=br.readLine())!=null){
				process.process(start,line);
				start++;
			}
			return start;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}finally{
			process.end();
			safeClose(br);
		}
	}
	
	public static interface Process<T>{
		public void process(long index,String line);
		public void end();
	}
	
	public static abstract class OsProcess implements Process<String>{
		protected BufferedWriter bw;
		public OsProcess(){
			
		}
		public OsProcess(BufferedWriter bw){
			this.bw=bw;
		}
		public abstract String doLine(long index,String line);
		public void process(long index,String line) {
			try{
				String temp=doLine(index,line);
				if(temp!=null){
					bw.append(temp);
					bw.newLine();
				}
				//bw.flush();
			}catch(IOException e){
				throw new RuntimeException(e.getMessage());
			}
		}
		public void end() {
			safeClose(bw);
		}
	}
	
	public static void safeClose(Object obj){
		if(obj!=null){
			try {
			if(obj instanceof Closeable){
					((Closeable)obj).close();;
			}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void safeDeleteFile(File file){
		if(file!=null){
			try{
				file.delete();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
