/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.apache.log4j.Logger;

import com.webobjects.foundation.*;

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
    public static final Logger log = Logger.getLogger(ERXCrypto.class);
    /** Block size of blowfish encrypted strings */
    public final static int BLOCK_SIZE=8;
    
    /**
     * Generates a secret key from the System property
     * <b>er.extensions.ERXBlowfishCipherKey</b>. This secret key is
     * used when generating the blowfish cipher.
     * @return a secret key for the blowfish cipher
     */
    private static Key secretBlowfishKey() throws NoSuchAlgorithmException {
        String blowfishKey = System.getProperty("er.extensions.ERXBlowfishCipherKey");
        if (blowfishKey == null) {
            log.warn("er.extensions.ERXBlowfishCipherKey not set in defaults.  Should be set before using the cipher.");
            blowfishKey = System.getProperty("ERBlowfishCipherKey");
            if(blowfishKey == null ) 
                blowfishKey = "DefaultCipherKey";
            else
                log.warn("ERBlowfishCipherKey is deprecated, please use er.extensions.ERXBlowfishCipherKey");
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
            cipher.init(mode, secretBlowfishKey());
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new NSForwardException(ex, "Couldn't find the Blowfish algorithm; perhaps you do not have the SunJCE security provider installed properly?");
        } catch (Exception e) {
            throw new NSForwardException(e);
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

    /**
     * @deprecated use <code>ERXStringUtilities.byteArrayToHexString</code> instead.
     */
    public static String bytesToString(byte[] bytes) {
        return ERXStringUtilities.byteArrayToHexString(bytes);
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
        if( string == null )
            return string;
        byte[] buf = string.getBytes();
        MessageDigest md;
        try {
            md= MessageDigest.getInstance("SHA");
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new NSForwardException(ex, "Couldn't find the SHA algorithm; perhaps you do not have the SunJCE security provider installed properly?");
        }
        md.update(buf);
        return ERXStringUtilities.byteArrayToHexString(md.digest());
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
        if( s == null )
            return s;
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
            } catch (Exception e) {
                throw new NSForwardException(e);
            }
            for (k=0; k<BLOCK_SIZE; k++) {
                result.append( ERXStringUtilities.HEX_CHARS[( encryptedBytes [k] >>> 4 ) & 0xf]);
                result.append( ERXStringUtilities.HEX_CHARS[encryptedBytes[k] & 0xf]);
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
        if( s == null )
            return null;
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
                for (int k=0; k<BLOCK_SIZE;k++) {
                    if( clearText[k] != 0 )
                        result.append((char)clearText[k]);
                }
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
            for (int k=0; k<BLOCK_SIZE;k++) {
                    result.append((char)clearText[k]);
            }
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




    

    private static String _secretKeyPathFramework;
    public static void setSecretKeyPathFramework(String v){
        _secretKeyPathFramework = v;
    }

    private static String _secretKeyPath;
    public static void setSecretKeyPath(String v){
        _secretKeyPath = v;
    }


    /*
     * Hashing and encryption methods
     */
    
    /**
     * Uses the SHA hash algorithm found in the Sun JCE to hash the
     * passed in String. This String is then base64 encoded and
     * returned.
     */
    public static String base64HashedString(String v) {
        String base64HashedPassword = null;
        try{
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(v.getBytes());
            String hashedPassword = new String(md.digest());
            sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
            base64HashedPassword = enc.encode(hashedPassword.getBytes());
        } catch(java.security.NoSuchAlgorithmException e) {
            throw new NSForwardException(e, "Couldn't find the SHA hash algorithm; perhaps you do not have the SunJCE security provider installed properly?");
        }
        return base64HashedPassword;
    }

    /**
     * Returns the DES java.security.Key found in the key file. The Key is
     * cached once it's found so further hits to the disk are unnecessary.
     * If the key file cannot be found, the method creates a
     * key and writes out a key file.
     */
    private static Key _secretDESKey = null;
    private static Key secretDESKey() {
        if(_secretDESKey == null){
            InputStream is = null;
            if(_secretKeyPath != null) {
                try {
                    is = new FileInputStream(new File(_secretKeyPath));
                } catch (FileNotFoundException e) {
                    log.warn("Couldn't recover Secret key file, generating new");
                    try {
                        KeyGenerator gen = KeyGenerator.getInstance("DES");
                        gen.init(new SecureRandom());
                        _secretDESKey = gen.generateKey();
                        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(_secretKeyPath)));
                        out.writeObject(_secretDESKey);
                        out.close();
                        is = new FileInputStream(new File(_secretKeyPath));
                    } catch (java.security.NoSuchAlgorithmException ex) {
                        throw new NSForwardException(ex, "Couldn't find the DES algorithm; perhaps you do not have the SunJCE security provider installed properly?");
                    } catch (Exception ex) {
                        throw NSForwardException._runtimeExceptionForThrowable(ex);
                    }
                }
            } else {
                String fn = "SecretKey.ser";
                is = ERXFileUtilities.inputStreamForResourceNamed(fn, _secretKeyPathFramework, null);
            }
            if(is != null) {
                log.debug("About to try to recover key");
                
                try {
                    ObjectInputStream in = new ObjectInputStream(is);
                    _secretDESKey = (Key)in.readObject();
                    in.close();
                } catch(Exception e) {
                    throw NSForwardException._runtimeExceptionForThrowable(e);
                }
            } else {
                throw new RuntimeException("No secret key found. You should add a 'Secret.ser' file into your app's resources or use setSecretKeyPath(String aPath)");
            }
        }
        return _secretDESKey;
    }
    
    
    /**
     * DES Encrypts and then base64 encodes the passed in String using the
     * secret key returned by <code>secretKey</code>. The base64 encoding is
     * performed to ensure that the encrypted string can be stored in places
     * that don't support extended character sets.
     */
    public static String base64EncryptedString(String v){
        return base64EncryptedString(v, secretDESKey());
    }
    
    /**
     * DES Encrypts and then base64 encodes the passed in String using the
     * passed in secret key. The base64 encoding is
     * performed to ensure that the encrypted string can be stored in places
     * that don't support extended character sets.
     */
    public static String base64EncryptedString(String v, Key sKey){
        if( v == null )
            return v;
        String encBase64String = null;
        try{
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, sKey);
            byte[] stringBytes = v.getBytes("UTF8");
            stringBytes = ERXCompressionUtilities.deflateByteArray(stringBytes);
            byte[] raw = cipher.doFinal(stringBytes);
            encBase64String = base64Encode(raw);
        } catch(java.security.NoSuchAlgorithmException ex) {
            throw new NSForwardException(ex, "Couldn't find the DES algorithm; perhaps you do not have the SunJCE security provider installed properly?");
        } catch(Exception ex) {
            throw new NSForwardException(ex);
        }
        return encBase64String;
    }
    
    /**
     * Base64 encodes the passed in byte[]
     */
    public static String base64Encode(byte[] byteArray) {
        sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
        String base64String = enc.encode(byteArray);
        return base64String;
    }
    
    /**
     * Base64 decodes the passed in String
     */
    public static byte[] base64Decode(String s) throws IOException {
        sun.misc.BASE64Decoder enc = new sun.misc.BASE64Decoder();
        byte[] raw = enc.decodeBuffer(s);
        return raw;
    }
    /**
     * Base64 decodes and then DES decrypts the passed in string using the
     * secret key returned by <code>secretKey</code>.
     */
    public static String decryptedBase64String(String v){
        return decryptedBase64String(v, secretDESKey());
    }
    
    /**
     * Base64 decodes and then DES decrypts the passed in string using the
     * passed in secret key.
     */
    public static String decryptedBase64String(String v, Key sKey){
        if( v == null )
            return v;
        String decString = null;
        try{
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretDESKey());
            byte[] raw = base64Decode(v);
            byte[] stringBytes = cipher.doFinal(raw);
            stringBytes = ERXCompressionUtilities.inflateByteArray(stringBytes);
            decString = new String(stringBytes, "UTF8");
        } catch(java.security.NoSuchAlgorithmException ex) {
            throw new NSForwardException(ex, "Couldn't find the DES algorithm; perhaps you do not have the SunJCE security provider installed properly?");
        } catch(Exception ex) {
            throw new NSForwardException(ex);
        }
        return decString;
    }
}