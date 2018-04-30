package sml;

import java.io.IOException;

import org.hw.sml.tools.Https;

public class HttpsDemo {
	public static void main(String[] args) throws IOException {
		Https https=Https.newGetHttps("http://192.168.193.128");
		System.out.println(https.execute());
		System.out.println(https.getResponseStatus());
		System.out.println(https.getResponseHeader().getHeader());
	}
}	
