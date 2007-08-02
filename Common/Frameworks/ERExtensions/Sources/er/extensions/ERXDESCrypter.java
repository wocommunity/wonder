package er.extensions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSForwardException;

/**
 * ERXDESCrypto is a DES implementation of the crypter interface.
 * 
 * @author mschrag
 */
public class ERXDESCrypter implements ERXCrypterInterface {
	public static final Logger log = Logger.getLogger(ERXCrypto.class);

	private Key _secretDESKey;
	private String _secretKeyPathFramework;
	private String _secretKeyPath;

	public ERXDESCrypter() {
	}

	public ERXDESCrypter(String secretKeyPathFramework, String secretKeyPath) {
		_secretKeyPathFramework = secretKeyPathFramework;
		_secretKeyPath = secretKeyPath;
	}

	public void setSecretKeyPathFramework(String secretKeyPathFramework) {
		_secretKeyPathFramework = secretKeyPathFramework;
	}

	public void setSecretKeyPath(String secretKeyPath) {
		_secretKeyPath = secretKeyPath;
	}

	/**
	 * Returns the DES java.security.Key found in the key file. The Key is
	 * cached once it's found so further hits to the disk are unnecessary. If
	 * the key file cannot be found, the method creates a key and writes out a
	 * key file.
	 */
	protected Key defaultSecretKey() {
		if (_secretDESKey == null) {
			InputStream is = null;
			if (_secretKeyPath != null) {
				try {
					is = new FileInputStream(new File(_secretKeyPath));
				}
				catch (FileNotFoundException e) {
					log.warn("Couldn't recover Secret key file, generating new");
					try {
						KeyGenerator gen = KeyGenerator.getInstance("DES");
						gen.init(new SecureRandom());
						_secretDESKey = gen.generateKey();
						ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(_secretKeyPath)));
						out.writeObject(_secretDESKey);
						out.close();
						is = new FileInputStream(new File(_secretKeyPath));
					}
					catch (java.security.NoSuchAlgorithmException ex) {
						throw new NSForwardException(ex, "Couldn't find the DES algorithm; perhaps you do not have the SunJCE security provider installed properly?");
					}
					catch (Exception ex) {
						throw NSForwardException._runtimeExceptionForThrowable(ex);
					}
				}
			}
			else {
				String fn = "SecretKey.ser";
				is = ERXFileUtilities.inputStreamForResourceNamed(fn, _secretKeyPathFramework, null);
			}
			if (is != null) {
				log.debug("About to try to recover key");

				try {
					ObjectInputStream in = new ObjectInputStream(is);
					_secretDESKey = (Key) in.readObject();
					in.close();
				}
				catch (Exception e) {
					throw NSForwardException._runtimeExceptionForThrowable(e);
				}
			}
			else {
				throw new RuntimeException("No secret key found. You should add a 'Secret.ser' file into your app's resources or use setSecretKeyPath(String aPath)");
			}
		}
		return _secretDESKey;
	}

	/**
	 * Base64 decodes and then DES decrypts the passed in string using the
	 * secret key returned by <code>secretKey</code>.
	 */
	public String decrypt(String cryptedText) {
		return decrypt(cryptedText, defaultSecretKey());
	}

	/**
	 * Base64 decodes and then DES decrypts the passed in string using the
	 * passed in secret key.
	 */
	public String decrypt(String cryptedText, Key secretKey) {
		if (cryptedText == null) {
			return cryptedText;
		}
		try {
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] raw = ERXCrypto.base64Decode(cryptedText);
			byte[] stringBytes = cipher.doFinal(raw);
			stringBytes = ERXCompressionUtilities.inflateByteArray(stringBytes);
			String decString = new String(stringBytes, "UTF8");
			return decString;
		}
		catch (java.security.NoSuchAlgorithmException ex) {
			throw new NSForwardException(ex, "Couldn't find the DES algorithm; perhaps you do not have the SunJCE security provider installed properly?");
		}
		catch (Exception ex) {
			throw new NSForwardException(ex);
		}
	}

	/**
	 * DES Encrypts and then base64 encodes the passed in String using the
	 * secret key returned by <code>secretKey</code>. The base64 encoding is
	 * performed to ensure that the encrypted string can be stored in places
	 * that don't support extended character sets.
	 */
	public String encrypt(String clearText) {
		return encrypt(clearText, defaultSecretKey());
	}

	/**
	 * DES Encrypts and then base64 encodes the passed in String using the
	 * passed in secret key. The base64 encoding is performed to ensure that the
	 * encrypted string can be stored in places that don't support extended
	 * character sets.
	 */
	public String encrypt(String clearText, Key secretKey) {
		if (clearText == null) {
			return clearText;
		}

		try {
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] stringBytes = clearText.getBytes("UTF8");
			stringBytes = ERXCompressionUtilities.deflateByteArray(stringBytes);
			byte[] raw = cipher.doFinal(stringBytes);
			String encBase64String = ERXCrypto.base64Encode(raw);
			return encBase64String;
		}
		catch (java.security.NoSuchAlgorithmException ex) {
			throw new NSForwardException(ex, "Couldn't find the DES algorithm; perhaps you do not have the SunJCE security provider installed properly?");
		}
		catch (Exception ex) {
			throw new NSForwardException(ex);
		}
	}

}
