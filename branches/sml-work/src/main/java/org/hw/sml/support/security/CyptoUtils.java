package org.hw.sml.support.security;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * 加密解密工具包
 */
public class CyptoUtils {

	public static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";
	private static final byte[] IV="12345678".getBytes();
	  public static String decode(String key,String data) {
	    	if(data == null)
	    		return null;
	        try {
		    	DESKeySpec dks = new DESKeySpec(key.getBytes());
		    	SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
	            Key secretKey = keyFactory.generateSecret(dks);
	            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
	            IvParameterSpec iv = new IvParameterSpec(IV);
	            AlgorithmParameterSpec paramSpec = iv;
	            cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
	            return new String(cipher.doFinal(hex2byte(data.getBytes())));
	        } catch (Exception e){
	    		return data;
	        }
	    }
    
    private static byte[] hex2byte(byte[] b) {
        if((b.length%2)!=0)
            throw new IllegalArgumentException();
		byte[] b2 = new byte[b.length/2];
		for (int n = 0; n < b.length; n+=2) {
		    String item = new String(b,n,2);
		    b2[n/2] = (byte)Integer.parseInt(item,16);
		}
        return b2;
    }
   
}
