package er.extensions.crypting;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.foundation.ERXProperties;

/**
 * ERXAESCrypter is an AES implementation of the crypter interface.
 * 
 * @author qdolan
 * @property er.extensions.ERXAESCipherKey the cipher key to use
 */
public class ERXAESCrypter extends ERXAbstractAESCrypter {
	private static final Logger log = LoggerFactory.getLogger(ERXAESCrypter.class);

	/**
	 * Generates a secret key from the System property
	 * <b>er.extensions.ERXAESCipherKey</b>. This secret key is used when
	 * generating the AES cipher.
	 * 
	 * @return a secret key for the cipher
	 */
	@Override
	protected Key secretKey() throws NoSuchAlgorithmException {
		String secretKey = ERXProperties.stringForKey("er.extensions.ERXAESCipherKey");
		if (secretKey == null) {
			log.warn("er.extensions.ERXAESCipherKey not set in defaults.  Should be set before using the cipher.");
			secretKey = "DefaultCipherKey";
		}
		return new SecretKeySpec(secretKey.getBytes(), "AES");
	}
}
