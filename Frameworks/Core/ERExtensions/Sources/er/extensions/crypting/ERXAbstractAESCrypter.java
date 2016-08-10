package er.extensions.crypting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Key;

import javax.crypto.Cipher;

import com.webobjects.foundation.NSForwardException;

import er.extensions.foundation.ERXStringUtilities;

/**
 * ERXAbstractAESCrypter is an AES implementation of the crypter
 * interface that allows subclasses to override the source of the cipher key.
 * The AES cipher is a two-way cipher meaning the original string that was
 * encrypted can be retrieved. The way that this version of the AES cipher
 * is encrypted it is safe to use as a form value.
 * 
 * @author qdolan
 */
public abstract class ERXAbstractAESCrypter implements ERXCrypterInterface {
	
	/**
	 * Cipher instances are not thread safe so we need to create one 
	 * for each thread using a ThreadLocal object 
	 */
	class ThreadLocalCipher extends ThreadLocal {
		private final int _mode;
		
		public ThreadLocalCipher(int mode) {
			_mode = mode;
		}
		
		@Override
		protected Object initialValue() {
			return createCipher(_mode);
		}
		
		@Override
		public Cipher get() {
			return (Cipher) super.get();
		}
	}

	/** Block size of encrypted strings */
	private int _blockSize;

	/** Used to cache the encryption cipher */
	private ThreadLocalCipher _encryptCipher;

	/** Used to cache the decryption cipher */
	private ThreadLocalCipher _decryptCipher;
	
	/** Used to cache the secret key */
	private Key _secretKey;

	public ERXAbstractAESCrypter() {
		_blockSize = 16;
	}

	/**
	 * Sets the block size to use for this cipher.
	 * 
	 * @param blockSize
	 *            the block size to use for this cipher
	 */
	public void setBlockSize(int blockSize) {
		_blockSize = blockSize;
	}

	/**
	 * Returns the block size for this cipher.
	 * 
	 * @return the block size for this cipher
	 */
	public int blockSize() {
		return _blockSize;
	}
	
	/**
	 * Returns the secret key to use for this cipher.
	 * 
	 * @return a secret key for the cipher
	 */
	protected abstract Key secretKey() throws Exception;

	private Key _secretKey() {
		if (_secretKey == null) {
			try {
				_secretKey = secretKey();
			}
			catch (Exception e) {
				throw new NSForwardException(e);
			}
		}
		return _secretKey;
	}

	/**
	 * Creates an AES cipher for a given mode. The two possible modes for a
	 * cipher are: ENCRYPT and DECRYPT.
	 * 
	 * @param mode
	 *            of the cipher (encrypting or decrypting)
	 * @return an AES cipher initialized with the given mode and with the
	 *         <code>secretKey</code> from the above method.
	 */
	protected Cipher createCipher(int mode) {
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(mode, _secretKey());
			return cipher;
		}
		catch (java.security.NoSuchAlgorithmException ex) {
			throw new NSForwardException(ex, "Couldn't find the AES algorithm; perhaps you do not have the SunJCE security provider installed properly?");
		}
		catch (Exception e) {
			throw new NSForwardException(e);
		}
	}

	/**
	 * Method used to return the shared instance of the encryption cipher.
	 * 
	 * @return AES encryption cipher
	 */
	protected Cipher encryptCipher() {
		if (_encryptCipher == null) {
			_encryptCipher = new ThreadLocalCipher(Cipher.ENCRYPT_MODE);
		}
		return _encryptCipher.get();
	}

	/**
	 * Method used to return the shared instance of the decryption cipher.
	 * 
	 * @return decryption cipher
	 */
	protected Cipher decryptCipher() {
		if (_decryptCipher == null) {
			_decryptCipher = new ThreadLocalCipher(Cipher.DECRYPT_MODE);
		}
		return _decryptCipher.get();
	}

	/**
	 * Decodes an AES encoded string. Note that the originally encoded
	 * string should have been encoded with the same secret key as is used for
	 * the decoding cipher or else you are going to get garbage. To encode a
	 * string have a look at <code>encrypt</code>.
	 * 
	 * @param cryptedText
	 *            AES encoded string to be decoded
	 * @return decode clear text string
	 */
	public String decrypt(String cryptedText) {
		if (cryptedText == null) {
			return null;
		}
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] clearText = null;
		byte[] decodedBytes = null;

		try {
			decodedBytes = ERXCrypto.base64Decode(cryptedText);
		}
		catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}

		int length = decodedBytes.length;
		for (int j = 0; j < length;) {
			byte[] encryptedBytes = new byte[_blockSize];
			System.arraycopy(decodedBytes, j, encryptedBytes, 0, Math.min(length - j, _blockSize));
			try {
				clearText = decryptCipher().doFinal(encryptedBytes);
			}
			catch (Exception e) {
				throw new NSForwardException(e);
			}
			for (int k = 0; k < _blockSize; k++) {
				if (clearText[k] != 0) {
					result.write(clearText[k]);
				}
			}
			j+= _blockSize;
		}
		return ERXStringUtilities.fromUTF8Bytes(result.toByteArray());
	}

	/**
	 * AES encodes a given string using the secret key specified in the
	 * System property: <b>er.extensions.ERXAESCipherKey</b>. The AES cipher is a
	 * two way cipher meaning that given the secret key you can de-cipher what
	 * the original string is. For one-way encryption look at methods dealing
	 * with the SHA algorithm. To decode an AES encoded string use the
	 * method: <code>decrypt</code>. The resultant string from encoding 
	 * a string is base64url encoded and safe for use in urls and cookies.
	 * 
	 * @param clearText
	 *            string to be encrypted
	 * @return encrypted string
	 */
	public String encrypt(String clearText) {
		if (clearText == null) {
			return null;
		}
		byte clearTextBytes[] = ERXStringUtilities.toUTF8Bytes(clearText);
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		int pos = 0, length = clearTextBytes.length;
		byte[] encryptedBytes;
		while (pos < length) {
			byte[] bytesToEncrypt = new byte[_blockSize];
	        System.arraycopy(clearTextBytes, pos, bytesToEncrypt, 0,
	                         Math.min(length - pos, _blockSize));
			try {
				encryptedBytes = encryptCipher().doFinal(bytesToEncrypt);
				result.write(encryptedBytes);
			}
			catch (Exception e) {
				throw new NSForwardException(e);
			}
			pos += _blockSize;
		}
		return ERXCrypto.base64urlEncode(result.toByteArray());
	}

}
