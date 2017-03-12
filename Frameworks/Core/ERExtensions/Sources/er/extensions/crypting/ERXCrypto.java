/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.crypting;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;

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
	/**
	 * The constant for the DES encryption algorithm.
	 */
	public static final String DES = "DES";

	/**
	 * The constant for the Blowfish encryption algorithm.
	 */
	public static final String BLOWFISH = "Blowfish";

	/**
	 * The constant for the AES encryption algorithm.
	 */
	public static final String AES = "AES";

	
	private static NSMutableDictionary<String, ERXCrypterInterface> _crypters;

	private static synchronized NSMutableDictionary<String, ERXCrypterInterface> crypters() {
		if (_crypters == null) {
			_crypters = new NSMutableDictionary<>();
			_crypters.setObjectForKey(new ERXDESCrypter(), ERXCrypto.DES);
			_crypters.setObjectForKey(new ERXBlowfishCrypter(), ERXCrypto.BLOWFISH);
			_crypters.setObjectForKey(new ERXAESCrypter(), ERXCrypto.AES);

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
		NSMutableDictionary<String, String> result = new NSMutableDictionary<>();
		for (String key: dict.allKeys()) {
			NSArray<String> objects = dict.objectForKey(key);
			String value = ERXCrypto.defaultCrypter().decrypt(objects.lastObject()).trim();
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
	 * 
	 * @param v the string to encode
	 * @return the encoded string
	 */
	public static String base64HashedString(String v) {
		String base64HashedPassword = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(v.getBytes());
			base64HashedPassword = Base64.encodeBase64String(md.digest()); 
		}
		catch (NoSuchAlgorithmException e) {
			throw new NSForwardException(e, "Couldn't find the SHA hash algorithm; perhaps you do not have the SunJCE security provider installed properly?");
		}
		return base64HashedPassword;
	}

	/**
	 * SHA-1 encodes a given string. The resulting string is safe to use in urls
	 * and cookies. From the digest of the string it is nearly impossible to
	 * determine what the original string was. Running the same string through
	 * the SHA-1 digest multiple times will always produce the same hash.
	 * 
	 * @param text
	 *            to be put through the sha digest
	 * @return hashed form of the given string
	 */
	public static String shaEncode(String text) {
		return algorithmEncode(text, "SHA");
	}
	
	/**
	 * SHA-256 encodes a given string. The resulting string is safe to use in urls
	 * and cookies. From the digest of the string it is nearly impossible to
	 * determine what the original string was. Running the same string through
	 * the SHA-256 digest multiple times will always produce the same hash.
	 * 
	 * @param text
	 *            to be put through the sha digest
	 * @return hashed form of the given string
	 */
	public static String sha256Encode(String text) {
		return algorithmEncode(text, "SHA-256");
	}
	
	/**
	 * SHA-384 encodes a given string. The resulting string is safe to use in urls
	 * and cookies. From the digest of the string it is nearly impossible to
	 * determine what the original string was. Running the same string through
	 * the SHA-384 digest multiple times will always produce the same hash.
	 * 
	 * @param text
	 *            to be put through the sha digest
	 * @return hashed form of the given string
	 */
	public static String sha384Encode(String text) {
		return algorithmEncode(text, "SHA-384");
	}
	
	/**
	 * SHA-512 encodes a given string. The resulting string is safe to use in urls
	 * and cookies. From the digest of the string it is nearly impossible to
	 * determine what the original string was. Running the same string through
	 * the SHA-512 digest multiple times will always produce the same hash.
	 * 
	 * @param text
	 *            to be put through the sha digest
	 * @return hashed form of the given string
	 */
	public static String sha512Encode(String text) {
		return algorithmEncode(text, "SHA-512");
	}
	
	/**
	 * MD5 encodes a given string. The resulting string is safe to use in urls
	 * and cookies. From the digest of the string it is nearly impossible to
	 * determine what the original string was. Running the same string through
	 * the MD5 digest multiple times will always produce the same hash.
	 * 
	 * @param text
	 *            to be put through the sha digest
	 * @return hashed form of the given string
	 */
	public static String md5Encode(String text) {
		return algorithmEncode(text, "MD5");
	}
	
	/**
	 * Encodes a given string with a given algorithm. The resulting string is safe
	 * to use in urls and cookies. From the digest of the string it is nearly
	 * impossible to determine what the original string was. Running the same string
	 * through the algorithm digest multiple times will always produce the same hash.
	 * 
	 * @param text
	 *            to be put through the algorithm digest
	 * @param algorithmName
	 *            the algorithm to use (e.g. SHA, SHA-256, ...)
	 * @return hashed form of the given string
	 */
	public static String algorithmEncode(String text, String algorithmName) {
		if (text == null || algorithmName == null) {
			return text;
		}
		byte[] buf = text.getBytes();
		try {
			MessageDigest md = MessageDigest.getInstance(algorithmName);
			md.update(buf);
			return ERXStringUtilities.byteArrayToHexString(md.digest());
		}
		catch (NoSuchAlgorithmException ex) {
			throw new NSForwardException(ex, "Couldn't find the algorithm '" + algorithmName
					+ "'; perhaps you do not have the SunJCE security provider installed properly?");
		}
	}

	/**
	 * Base64 encodes the passed in byte[]
	 * 
	 * @param byteArray the byte array to encode
	 * @return the encoded string
	 */
	public static String base64Encode(byte[] byteArray) {
		return Base64.encodeBase64String(byteArray);
	}
	
	/**
	 * Base64url encodes the passed in byte[]
	 * 
	 * @param byteArray the byte array to URL encode
	 * @return the encoded string
	 */
	public static String base64urlEncode(byte[] byteArray) {
		return Base64.encodeBase64URLSafeString(byteArray);
	}

	/**
	 * Base64 decodes the passed in String
	 * 
	 * @param s the string to decode
	 * @return a byte array of the decoded string
	 * @throws IOException if the decode fails
	 */
	// TODO remove throws declaration when API change is possible
	public static byte[] base64Decode(String s) throws IOException {
		return Base64.decodeBase64(s);
	}

	/**
	 * Run this with ERXMainRunner passing in the plaintext you want to encrypt
	 * using the default crypter. This is useful if you are using encrypted 
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
