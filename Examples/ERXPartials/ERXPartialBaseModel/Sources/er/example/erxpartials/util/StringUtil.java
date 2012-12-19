package er.example.erxpartials.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.webobjects.foundation.NSForwardException;

import sun.misc.BASE64Encoder;

public class StringUtil {

	public static String encryptString(String string) {
		String digest = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			digest = new BASE64Encoder().encode(md.digest(string.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			// TODO check this exception out
			throw new NSForwardException(e);
		}
		return digest;
	}

}
