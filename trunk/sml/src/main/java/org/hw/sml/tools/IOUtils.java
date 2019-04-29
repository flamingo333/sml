package org.hw.sml.tools;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

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
		return  toString(new FileInputStream(file),charset);
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
	public static void safeClose(Object obj){
		if(obj!=null){
			try {
			if(obj instanceof Closeable){
					((Closeable)obj).close();
			}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
