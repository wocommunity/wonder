/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.io.IOException;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableDictionary;

/*
 * Compilation problems? READ THIS
 * 
 * Note: this class requires that you have JCE (javax.crypto and the sun
 * provider) installed JCE is standard in the 1.4 JDK for 1.3 you can find it at
 * http://java.sun.com/products/jce/index-121.html
 * 
 * You will have to put the 4 JCE jars in your extensions directory
 * (/Library/Java/Home/lib/ext on X) for ERExtensions to build.. And edit
 * /Library/Java/Home/lib/security/java.security, add the following line
 * security.provider.4=com.sun.crypto.provider.SunJCE
 */

/**
 * Provides a wrapper around common encryption and decryption operations.
 * 
 * @author ?
 */
public class ERXCrypto {
	/** logging support */
	public static final Logger log = Logger.getLogger(ERXCrypto.class);

	/**
	 * The constant for the DES encryption algorithm.
	 */
	public static final String DES = "DES";
	
	/**
	 * The constant for the Blowfish encryption algorithm.
	 */
	public static final String BLOWFISH = "Blowfish";

	private static NSMutableDictionary<String, ERXCrypterInterface> _crypters;

	private static synchronized NSMutableDictionary<String, ERXCrypterInterface> crypters() {
		if (_crypters == null) {
			_crypters = new NSMutableDictionary<String, ERXCrypterInterface>();
			_crypters.setObjectForKey(new ERXDESCrypter(), ERXCrypto.DES);
			_crypters.setObjectForKey(new ERXBlowfishCrypter(), ERXCrypto.BLOWFISH);
		}
		return _crypters;
	}

	/**
	 * Returns the default crypter.  By default this is Blowfish, but you can
	 * override the choice by setting er.extensions.defaultCrypterAlgorithm.
	 * 
	 * @return the default crypter
	 */
	public static ERXCrypterInterface defaultCrypter() {
		String defaultCrypterAlgorithm = ERXProperties.stringForKeyWithDefault("er.extensions.defaultCrypterAlgorithm", ERXCrypto.BLOWFISH);
		return ERXCrypto.crypterForAlgorithm(defaultCrypterAlgorithm);
	}

	/**
	 * Sets the crypter for the given algorithm.
	 * 
	 * @param crypter the crypter to use
	 * @param algorithm the algorithm name
	 */
	public static void setCrypterForAlgorithm(ERXCrypterInterface crypter, String algorithm) {
		NSMutableDictionary<String, ERXCrypterInterface> crypters = ERXCrypto.crypters();
		crypters.setObjectForKey(crypter, algorithm);
	}

	/**
	 * Returns the crypter for the given algorithm.  By default, DES and Blowfish are
	 * available ("DES", "Blowfish", etc).
	 * 
	 * @param algorithm the algorithm to lookup
	 * @return the corresponding crypter
	 * @throws IllegalArgumentException if there is no crypter for the given algorithm
	 */
	public static ERXCrypterInterface crypterForAlgorithm(String algorithm) {
		NSMutableDictionary<String, ERXCrypterInterface> crypters = ERXCrypto.crypters();
		ERXCrypterInterface crypter = crypters.objectForKey(algorithm);
		if (crypter == null) {
			throw new IllegalArgumentException("Unknown encryption algorithm '" + algorithm + "'.");
		}
		return crypter;
	}

	/**
	 * Decodes all of the values from a given dictionary using the default crypter.
	 * 
	 * @param dict
	 *            dictionary of key value pairs where the values are
	 *            encoded strings
	 * @return a dictionary of decoded key-value pairs
	 */
	public static NSMutableDictionary<String, String> decodedFormValuesDictionary(NSDictionary<String, NSArray<String>> dict) {
		NSMutableDictionary<String, String> result = new NSMutableDictionary<String, String>();
		for (Enumeration e = dict.allKeys().objectEnumerator(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			NSArray<String> objects = dict.objectForKey(key);
			String value = ERXCrypto.defaultCrypter().encrypt(objects.lastObject()).trim();
			result.setObjectForKey(value, key);
		}
		return result;
	}

	/*
	 * Hashing and encryption methods
	 */

	/**
	 * Uses the SHA hash algorithm found in the Sun JCE to hash the passed in
	 * String. This String is then base64 encoded and returned.
	 */
	public static String base64HashedString(String v) {
		String base64HashedPassword = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(v.getBytes());
			String hashedPassword = new String(md.digest());
			sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
			base64HashedPassword = enc.encode(hashedPassword.getBytes());
		}
		catch (java.security.NoSuchAlgorithmException e) {
			throw new NSForwardException(e, "Couldn't find the SHA hash algorithm; perhaps you do not have the SunJCE security provider installed properly?");
		}
		return base64HashedPassword;
	}

	/**
	 * Sha encodes a given string. The resulting string is safe to use in urls
	 * and cookies. From the digest of the string it is nearly impossible to
	 * determine what the original string was. Running the same string through
	 * the Sha digest multiple times will always produce the same hash.
	 * 
	 * @param string
	 *            to be put through the sha digest
	 * @return hashed form of the given string
	 */
	public static String shaEncode(String text) {
		if (text == null) {
			return text;
		}
		byte[] buf = text.getBytes();
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(buf);
			return ERXStringUtilities.byteArrayToHexString(md.digest());
		}
		catch (java.security.NoSuchAlgorithmException ex) {
			throw new NSForwardException(ex, "Couldn't find the SHA algorithm; perhaps you do not have the SunJCE security provider installed properly?");
		}
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
	 * @deprecated use <code>ERXStringUtilities.byteArrayToHexString</code>
	 *             instead.
	 */
	@Deprecated
	public static String bytesToString(byte[] bytes) {
		return ERXStringUtilities.byteArrayToHexString(bytes);
	}

	/**
	 * @deprecated use ERXDESCrypter and/or
	 *             ERXCrypto.crypterForAlgorithm(ERXCrypto.DES)
	 */
	@Deprecated
	public static String base64EncryptedString(String clearText) {
		return ERXCrypto.crypterForAlgorithm(ERXCrypto.DES).encrypt(clearText);
	}

	/**
	 * @deprecated use ERXDESCrypter and/or
	 *             ERXCrypto.crypterForAlgorithm(ERXCrypto.DES)
	 */
	@Deprecated
	public static String base64EncryptedString(String clearText, Key secretKey) {
		return ((ERXDESCrypter) ERXCrypto.crypterForAlgorithm(ERXCrypto.DES)).encrypt(clearText, secretKey);
	}

	/**
	 * @deprecated use ERXDESCrypter and/or
	 *             ERXCrypto.crypterForAlgorithm(ERXCrypto.DES)
	 */
	@Deprecated
	public static String decryptedBase64String(String encryptedText) {
		return ERXCrypto.crypterForAlgorithm(ERXCrypto.DES).decrypt(encryptedText);
	}

	/**
	 * @deprecated use ERXDESCrypter and/or
	 *             ERXCrypto.crypterForAlgorithm(ERXCrypto.DES)
	 */
	@Deprecated
	public static String decryptedBase64String(String encryptedText, Key secretKey) {
		return ((ERXDESCrypter) ERXCrypto.crypterForAlgorithm(ERXCrypto.DES)).decrypt(encryptedText, secretKey);
	}

	/**
	 * @deprecated use ERXBlowfishCrypter and/or
	 *             ERXCrypto.crypterForAlgorithm(ERXCrypto.BLOWFISH)
	 */
	@Deprecated
	public static String blowfishEncode(String clearText) {
		return ERXCrypto.crypterForAlgorithm(ERXCrypto.BLOWFISH).encrypt(clearText);
	}

	/**
	 * @deprecated use ERXBlowfishCrypter and/or
	 *             ERXCrypto.crypterForAlgorithm(ERXCrypto.BLOWFISH)
	 */
	@Deprecated
	public static String blowfishDecode(String encryptedText) {
		return ERXCrypto.crypterForAlgorithm(ERXCrypto.BLOWFISH).decrypt(encryptedText);
	}

	/**
	 * @deprecated use ERXDESCrypter and/or
	 *             ERXCrypto.crypterForAlgorithm(ERXCrypto.DES)
	 */
	@Deprecated
	public static void setSecretKeyPathFramework(String secretKeyPathFramework) {
		((ERXDESCrypter) ERXCrypto.crypterForAlgorithm(ERXCrypto.DES)).setSecretKeyPathFramework(secretKeyPathFramework);
	}

	/**
	 * @deprecated use ERXDESCrypter and/or
	 *             ERXCrypto.crypterForAlgorithm(ERXCrypto.DES)
	 */
	@Deprecated
	public static void setSecretKeyPath(String secretKeyPath) {
		((ERXDESCrypter) ERXCrypto.crypterForAlgorithm(ERXCrypto.DES)).setSecretKeyPath(secretKeyPath);
	}
}