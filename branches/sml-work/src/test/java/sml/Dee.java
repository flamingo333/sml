package sml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;

import org.hw.sml.tools.Https;

public class Dee {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Https https = Https.newPostHttps("http://192.168.1.111:7080/web-dataShare-special/sml/invoke/ftpService/uploadFile/upload");
		https.getParamer().add("path", "/home/newland");
		String rs = https.upFile()
				.body(Https.newUpFile(URLEncoder.encode("黄文.txt","utf-8"), new FileInputStream("d:/temp/t.txt")))
				.execute();
		System.out.println(rs);
	}
}
