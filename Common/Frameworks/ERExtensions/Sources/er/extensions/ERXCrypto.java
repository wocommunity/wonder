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

/**
 * Provides a wrapper around common encryption and decryption operations.
 * ERXCrypto provides built-in support for DES and Blowfish crypters. You can
 * use the "er.extensions.ERXCrypto.crypters" property to override or provide
 * your own. If you only want DES and/or Blowfish, you don't need to set
 * crypters yourself.
 * 
 * @author ?
 * @property er.extensions.ERXCrypto.default the name of the default crypter
 *           algorithm (default = "Blowfish")
 * @property er.extensions.ERXCrypto.crypters comma-separated list of crypter
 *           algorithms (i.e. "DES,Blowfish")
 * @property er.extensions.ERXCrypto.crypter.[Algorithm] crypter class name,
 *           should be one for each algorithm in crypters list (i.e.
 *           er.extensions.ERXCrypto.crypter.DES)
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

			NSArray<String> crypterAlgorithms = ERXProperties.componentsSeparatedByString("er.extensions.ERXCrypto.crypters", ",");
			if (crypterAlgorithms != null) {
				for (String crypterAlgorithm : crypterAlgorithms) {
					String crypterClassName = ERXProperties.stringForKey("er.extensions.ERXCrypto.crypter." + crypterAlgorithm);
					if (crypterClassName == null) {
						throw new IllegalArgumentException("You did not provide a crypter class definition for 'er.extensions.ERXCrypto.crypter." + crypterAlgorithm + "'.");
					}
					try {
						ERXCrypterInterface crypter = Class.forName(crypterClassName).asSubclass(ERXCrypterInterface.class).newInstance();
						_crypters.setObjectForKey(crypter, crypterAlgorithm);
					}
					catch (Exception e) {
						throw new NSForwardException(e, "Failed to create " + crypterAlgorithm + " crypter '" + crypterClassName + "'.");
					}
				}
			}
		}
		return _crypters;
	}

	/**
	 * Returns the default crypter. By default this is Blowfish, but you can
	 * override the choice by setting er.extensions.ERXCrypto.default.
	 * 
	 * @return the default crypter
	 */
	public static ERXCrypterInterface defaultCrypter() {
		String defaultCrypterAlgorithm = ERXProperties.stringForKeyWithDefault("er.extensions.ERXCrypto.default", ERXCrypto.BLOWFISH);
		return ERXCrypto.crypterForAlgorithm(defaultCrypterAlgorithm);
	}

	/**
	 * Sets the crypter for the given algorithm.
	 * 
	 * @param crypter
	 *            the crypter to use
	 * @param algorithm
	 *            the algorithm name
	 */
	public static void setCrypterForAlgorithm(ERXCrypterInterface crypter, String algorithm) {
		NSMutableDictionary<String, ERXCrypterInterface> crypters = ERXCrypto.crypters();
		crypters.setObjectForKey(crypter, algorithm);
	}

	/**
	 * Returns the crypter for the given algorithm. By default, DES and Blowfish
	 * are available ("DES", "Blowfish", etc).
	 * 
	 * @param algorithm
	 *            the algorithm to lookup
	 * @return the corresponding crypter
	 * @throws IllegalArgumentException
	 *             if there is no crypter for the given algorithm
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
	 * Decodes all of the values from a given dictionary using the default
	 * crypter.
	 * 
	 * @param dict
	 *            dictionary of key value pairs where the values are encoded
	 *            strings
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
	
	/**
	 * Run this with ERXMainRunner passing in the plaintext you want to encrypt
	 * using the default crypter.  This is useful if you are using encrypted 
	 * properties and you need a quick way to know what to set the property
	 * value to.
	 * 
	 * @param args the plaintext to encrypt
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Usage: ERXCrypto [plaintext]");
			System.out.println("       returns the encrypted form of the given plaintext using the default crypter");
			System.exit(0);
		}
		String plaintext = args[0];
		String encrypted = ERXCrypto.defaultCrypter().encrypt(plaintext);
		System.out.println("ERXCrypto.main: Encrypted form of '" + plaintext + "' is '" + encrypted + "'");
	}
}