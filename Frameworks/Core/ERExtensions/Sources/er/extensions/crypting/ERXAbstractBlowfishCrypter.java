package er.extensions.crypting;

import java.io.ByteArrayOutputStream;
import java.security.Key;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSForwardException;

import er.extensions.foundation.ERXStringUtilities;

/**
 * ERXAbstractBlowfishCrypter is a blowfish implementation of the crypter
 * interface that allows subclasses to override the source of the blowfish key.
 * The blowfish cipher is a two-way cipher meaning the original string that was
 * encrypted can be retrieved. The way that this version of the blowfish cipher
 * is encrypted it is safe to use as a form value.
 * 
 * @author mschrag
 */
public abstract class ERXAbstractBlowfishCrypter implements ERXCrypterInterface {
	public static final Logger log = Logger.getLogger(ERXCrypto.class);

	/** Block size of blowfish encrypted strings */
	private int _blockSize;

	/** Used to cache the blowfish encryption cipher */
	private Cipher _encryptCipher;

	/** Used to cache the blowfish decryption cipher */
	private Cipher _decryptCipher;

	public ERXAbstractBlowfishCrypter() {
		_blockSize = 8;
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
	 * @return a secret key for the blowfish cipher
	 */
	protected abstract Key secretBlowfishKey() throws Exception;

	/**
	 * Creates a blowfish cipher for a given mode. The two possible modes for a
	 * blowfish cipher are: ENCRYPT and DECRYPT.
	 * 
	 * @param mode
	 *            of the cipher (encrypting or decrypting)
	 * @return a blowfish cipher initialized with the given mode and with the
	 *         <code>secretKey</code> from the above method.
	 */
	protected Cipher createBlowfishCipher(int mode) {
		try {
			Cipher cipher = Cipher.getInstance("Blowfish/ECB/NoPadding");
			cipher.init(mode, secretBlowfishKey());
			return cipher;
		}
		catch (java.security.NoSuchAlgorithmException ex) {
			throw new NSForwardException(ex, "Couldn't find the Blowfish algorithm; perhaps you do not have the SunJCE security provider installed properly?");
		}
		catch (Exception e) {
			throw new NSForwardException(e);
		}
	}

	/**
	 * Method used to return the shared instance of the blowfish encryption
	 * cipher.
	 * 
	 * @return blowfish encryption cipher
	 */
	protected Cipher encryptCipher() {
		if (_encryptCipher == null) {
			_encryptCipher = createBlowfishCipher(Cipher.ENCRYPT_MODE);
		}
		return _encryptCipher;
	}

	/**
	 * Method used to return the shared instance of the blowfish decryption
	 * cipher.
	 * 
	 * @return blowfish decryption cipher
	 */
	protected Cipher decryptCipher() {
		if (_decryptCipher == null) {
			_decryptCipher = createBlowfishCipher(Cipher.DECRYPT_MODE);
		}
		return _decryptCipher;
	}

	/**
	 * Decodes a blowfish encoded string. Note that the originally encoded
	 * string should have been encoded with the same secret key as is used for
	 * the decoding cipher or else you are going to get garbage. To encode a
	 * string have a look at <code>blowfishEncode</code>.
	 * 
	 * @param cryptedText
	 *            blowfish encoded string to be decoded
	 * @return decode clear text string
	 */
	public String decrypt(String cryptedText) {
		if (cryptedText == null) {
			return null;
		}
		int length = cryptedText.length();
		if (length % 16 != 0) {
			return null;
		}
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] clearText = null;
		byte[] encryptedBytes = new byte[_blockSize];

		int i = 0;
		for (int j = 0; j < length;) {

			char c1 = cryptedText.charAt(j++);
			int b1 = c1 < 'a' ? c1 - '0' : c1 - 'a' + 10;
			char c2 = cryptedText.charAt(j++);
			int b2 = c2 < 'a' ? c2 - '0' : c2 - 'a' + 10;
			encryptedBytes[i++] = (byte) ((b1 << 4) + b2);
			if (i == _blockSize) {
				// we filled a block
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
				i = 0;
			}
		}

		if (i != 0) {
			for (int j = i; j < _blockSize; j++) {
				encryptedBytes[j] = 0;
			}
			try {
				clearText = decryptCipher().doFinal(encryptedBytes);
			}
			catch (Exception e) {
				throw new NSForwardException(e);
			}
			for (int k = 0; k < _blockSize; k++) {
				result.write(clearText[k]);
			}
		}
		return ERXStringUtilities.fromUTF8Bytes(result.toByteArray());
	}

	/**
	 * Blowfish encodes a given string using the secret key specified in the
	 * System property: <b>ERBlowfishCipherKey</b>. The blowfish cipher is a
	 * two way cipher meaning that given the secret key you can de-cipher what
	 * the original string is. For one-way encryption look at methods dealing
	 * with the Sha alogrithm. To decode a blowfish encoded string use the
	 * method: <code>blowfishDecode</code>. The resultant string from
	 * encoding a string is safe for use in urls and cookies.
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
		StringBuilder result = new StringBuilder();
		int pos = 0, length = clearTextBytes.length;
		byte[] bytesToEncrypt = new byte[_blockSize];
		byte[] encryptedBytes = null;
		while (pos < length) {
			int k = 0;
			for (int j = pos; j < length && j < pos + _blockSize; k++, j++) {
				bytesToEncrypt[k] = clearTextBytes[j];
			}
			if (k < _blockSize) {
				for (int l = k; l < _blockSize; l++) {
					bytesToEncrypt[l] = 0;
				}
			}

			try {
				encryptedBytes = encryptCipher().doFinal(bytesToEncrypt);
			}
			catch (Exception e) {
				throw new NSForwardException(e);
			}
			for (k = 0; k < _blockSize; k++) {
				result.append(ERXStringUtilities.HEX_CHARS[(encryptedBytes[k] >>> 4) & 0xf]);
				result.append(ERXStringUtilities.HEX_CHARS[encryptedBytes[k] & 0xf]);
			}
			pos += _blockSize;
		}
		return result.toString();
	}

}
