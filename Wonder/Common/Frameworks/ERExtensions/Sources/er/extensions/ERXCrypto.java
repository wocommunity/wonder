/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.security.*;
import javax.crypto.*;
import java.util.*;
import org.apache.log4j.Category;
import javax.crypto.spec.*;

/* Compilation problems? READ THIS
 *
 * Note: this class requires that you have JCE (javax.crypto and the sun provider)
 * installed JCE is standard in the 1.4 JDK
 * for 1.3 you can find it at http://java.sun.com/products/jce/index-121.html
 *
 * You will have to put the 4 JCE jars in your extensions directory
 * (/Library/Java/Home/lib/ext on X) for ERExtensions to build..
 * And edit /Library/Java/Home/lib/security/java.security, add the following line
 * security.provider.4=com.sun.crypto.provider.SunJCE
 */


/**
 * Provides a nice wrapper around the blowfish cipher and
 * the sha digest algorithms.<br/>
 * <br/>
 * The blowfish cipher is a two-way cipher meaning the original
 * string that was encrypted can be retrieved. The blowfish
 * cipher uses a secret key that should be set in the System
 * properties using the key: <b>ERBlowfishCipherKey</b>. The way
 * that this version of the blowfish cipher is enrcypted it is
 * safe to use as a form value.<br/>
 * <br/>
 * The sha digest uses one-way encryption to form a hash of a
 * given string. The digest formed is safe for use in form values
 * and cookies.
 */
public class ERXCrypto {

    /** logging support */
    public static final Category cat = Category.getInstance(ERXCrypto.class);
    /** Block size of blowfish encrypted strings */
    public final static int BLOCK_SIZE=8;
    
    /**
     * Generates a secret key from the System property
     * <b>ERBlowfishCipherKey</b>. This secret key is
     * used when generating the blowfish cipher.
     * @return a secret key for the blowfish cipher
     */
    private static SecretKey secretKey() throws NoSuchAlgorithmException {
        // FIXME: Should prefix the key as 'er.extensions.ERXBlowfishCipherKey'
        String blowfishKey = System.getProperty("ERBlowfishCipherKey");
        if (blowfishKey == null) {
            NSLog.err.appendln("WARNING: ERBlowfishCipherKey not set in defaults.  Should be set before using the cipher.");
            blowfishKey = "DefaultBlowfishCipherKey";
        }
        return new SecretKeySpec(blowfishKey.getBytes(), "Blowfish");
    }

    /**
     * Creates a blowfish cipher for a given mode.
     * The two possible modes for a blowfish cipher
     * are: ENCRYPT and DECRYPT.
     * @param mode of the cipher (encrypting or decrypting)
     * @return a blowfish cipher initialized with the given
     *		mode and with the <code>secretKey</code>
     *		from the above method.
     */
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

    /** Used to cache the blowfish encryption cipher */
    private static Cipher _encryptCipher;
    /**
     * Method used to return the shared instance of the
     * blowfish encryption cipher.
     * @return blowfish encryption cipher
     */
    private static Cipher encryptCipher() {
        if (_encryptCipher == null) {
            _encryptCipher=createBlowfishCipher(Cipher.ENCRYPT_MODE);
        }
        return _encryptCipher;
    }
    /** Used to cache the blowfish decryption cipher */
    private static Cipher _decryptCipher;
    /**
     * Method used to return the shared instance of the
     * blowfish decryption cipher.
     * @return blowfish decryption cipher
     */
    private static Cipher decryptCipher() {
        if (_decryptCipher == null) {
            _decryptCipher=createBlowfishCipher(Cipher.DECRYPT_MODE);
        }
        return _decryptCipher;
    }

    // DELETEME: Duplicate ivar in ERXExtensions
    private final static String hexDigits = "0123456789abcdef";
    // DELETEME: Duplicate method in ERXExtensions
    public static String bytesToString(byte[] bytes) {
        StringBuffer result=new StringBuffer();
        int length=bytes.length;
        for (int i=0; i<length; i++) {
            result.append( hexDigits.charAt( ( bytes [i] >>> 4 ) & 0xf ) );
            result.append( hexDigits.charAt( bytes [i] & 0xf ) );
        }
        return result.toString();
    }

    /**
     * Sha encodes a given string. The resulting
     * string is safe to use in urls and cookies.
     * From the digest of the string it is nearly
     * impossible to determine what the original
     * string was. Running the same string through
     * the Sha digest multiple times will always
     * produce the same hash.
     * @param string to be put through the sha digest
     * @return hashed form of the given string
     */
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
    
    /**
     * Blowfish encodes a given string using the
     * secret key specified in the System property:
     * <b>ERBlowfishCipherKey</b>. The blowfish cipher is
     * a two way cipher meaning that given the secret key
     * you can de-cipher what the original string is. For
     * one-way encryption look at methods dealing with the
     * Sha alogrithm. To decode a blowfish encoded string
     * use the method: <code>blowfishDecode</code>. The
     * resultant string from encoding a string is safe for
     * use in urls and cookies.
     * @param s string to be encrypted
     * @return encrypted string
     */
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

    /**
     * Decodes a blowfish encoded string. Note that
     * the originally encoded string should have been
     * encoded with the same secret key as is used for
     * the decoding cipher or else you are going to get
     * garbage. To encode a string have a look at
     * <code>blowfishEncode</code>.
     * @param s blowfish encoded string to be decoded
     * @return decode clear text string
     */
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

    /**
     * Decodes all of the values from a given dictionary
     * using blowfish.
     * @param dict dictionary of key value pairs where the
     * 		values are blowfish encoded strings
     * @return a dictionary of decoded key-value pairs
     */
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