//
// ERXCrypto.java
// Project ERExtensions
//
// Created by patrice on Tue Jan 29 2002
//
// A simple wrapper around Blowfish
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.security.*;
import javax.crypto.*;
import java.util.*;
import org.apache.log4j.Category;
import javax.crypto.spec.*;


// this class requires that you have JCE (javax.crypto and the sun provider) installed
// JCE is standard in the 1.4 JDK
// for 1.3 you can find it at http://java.sun.com/products/jce/index-121.html
// All,
//
// you will have to put the 4 JCE jars in your extensions directory
// (/Library/Java/Home/lib/ext on X) for ERExtensions to build..
// And edit /Library/Java/Home/lib/security/java.security, add the following line
// security.provider.4=com.sun.crypto.provider.SunJCE

public class ERXCrypto {
    
    public static final Category cat = Category.getInstance(ERXCrypto.class);
    
    private static SecretKey secretKey() throws NoSuchAlgorithmException {
        String blowfishKey = NSProperties.stringForKey("ERBlowfishCipherKey");
        if (blowfishKey == null) {
            NSLog.err.appendln("WARNING: ERBlowfishCipherKey not set in defaults.  Should be set before using the cipher.");
            blowfishKey = "DefaultBlowfishCipherKey";
        }
        return new SecretKeySpec(blowfishKey.getBytes(), "Blowfish");
    }

    public final static int BLOCK_SIZE=8;
    

    private static Cipher createBlowfishCipher(int mode) {
        Cipher cipher=null;
        try {
            cipher = Cipher.getInstance("Blowfish/ECB/NoPadding");
            cipher.init(mode, secretKey());
        } catch (Exception e) {
            cat.error("Caught exception trying to create cipher "+e+" - "+ERXUtilities.stackTrace(e));
        }
        return cipher;
    }

    private static Cipher _encryptCipher;
    private static Cipher encryptCipher() {
        if (_encryptCipher == null) {
            _encryptCipher=createBlowfishCipher(Cipher.ENCRYPT_MODE);
        }
        return _encryptCipher;
    }

    private static Cipher _decryptCipher;
    private static Cipher decryptCipher() {
        if (_decryptCipher == null) {
            _decryptCipher=createBlowfishCipher(Cipher.DECRYPT_MODE);
        }
        return _decryptCipher;
    }
    
    private final static String hexDigits = "0123456789abcdef";
    public static String bytesToString(byte[] bytes) {
        StringBuffer result=new StringBuffer();
        int length=bytes.length;
        for (int i=0; i<length; i++) {
            result.append( hexDigits.charAt( ( bytes [i] >>> 4 ) & 0xf ) );
            result.append( hexDigits.charAt( bytes [i] & 0xf ) );
        }
        return result.toString();
    }


    public static String shaEncode(String string) {

        byte[] buf=string.getBytes();
        MessageDigest md;
        try {
            md= MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new NSForwardException(e);
        }
        md.update(buf);
        return bytesToString(md.digest());
    }
    

    // provide encoding suitable for use in a URL
    public static String blowfishEncode(String s) {
        StringBuffer result=new StringBuffer();
        int pos=0,length=s.length();
        byte[] bytesToEncrypt=new byte[BLOCK_SIZE];
        byte[] encryptedBytes=null;
        while (pos<length) {
            int k=0;
            for (int j=pos; j<length && j<pos+BLOCK_SIZE; k++,j++) {
                char c=s.charAt(j);
                bytesToEncrypt[k]=(byte)c;
            }
            if (k<BLOCK_SIZE) {
                for (int l=k; l<BLOCK_SIZE; l++) bytesToEncrypt[l]=0;
            }
            try {
                encryptedBytes=encryptCipher().doFinal(bytesToEncrypt);
                byte[] redec=decryptCipher().doFinal(encryptedBytes);
            } catch (Exception e) {
                System.out.println("Caught "+e+" - "+ERXUtilities.stackTrace(e));
                throw new NSForwardException(e);
            }
            for (k=0; k<BLOCK_SIZE; k++) {
                result.append( hexDigits.charAt( ( encryptedBytes [k] >>> 4 ) & 0xf ) );
                result.append( hexDigits.charAt( encryptedBytes[k] & 0xf )
                               );
            }
            pos+=BLOCK_SIZE;
        }
        return result.toString();
    }


    public static String blowfishDecode(String s) {
        int length=s.length();
        if (length%16!=0) {
            return null;
        }
        StringBuffer result=new StringBuffer();
        byte[] clearText=null;
        byte[] encryptedBytes=new byte[BLOCK_SIZE];

        int i=0;
        for (int j=0; j<length;) {

            char c1=s.charAt(j++); int b1=c1<'a' ? c1-'0' : c1-'a'+10;
            char c2=s.charAt(j++); int b2=c2<'a' ? c2-'0' : c2-'a'+10;
            encryptedBytes[i++]=(byte)((b1<<4)+b2);
            if (i==BLOCK_SIZE) {
                // we filled a block
                try {
                    clearText=decryptCipher().doFinal(encryptedBytes);
                } catch (Exception e) {
                    throw new NSForwardException(e);
                }
                for (int k=0; k<BLOCK_SIZE;k++)
                    result.append((char)clearText[k]);
                i=0;
            }
        }

        if (i!=0) {
            for (int j=i;j<BLOCK_SIZE;i++) encryptedBytes[j]=0;
            try {
                clearText=decryptCipher().doFinal(encryptedBytes);
            } catch (Exception e) {
                throw new NSForwardException(e);
            }        
            for (int k=0; k<BLOCK_SIZE;k++) result.append((char)clearText[k]);
        }

        return result.toString();
    }

    public static NSMutableDictionary decodedFormValuesDictionary(NSDictionary dict) {
        NSMutableDictionary result = new NSMutableDictionary();
        for (Enumeration e = dict.allKeys().objectEnumerator();
             e.hasMoreElements();) {
            String key = (String)e.nextElement();
            NSArray objects = (NSArray)dict.objectForKey(key);
            String value = (blowfishDecode((String)objects.lastObject())).trim();
            result.setObjectForKey(value, key);
        }
        return result;
    }


    
}


