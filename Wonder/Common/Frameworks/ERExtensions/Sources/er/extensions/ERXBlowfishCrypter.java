package er.extensions;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.spec.SecretKeySpec;

/**
 * ERXBlowfishCrypter is a blowfish implementation of the crypter interface.
 * 
 * @author mschrag
 * @property er.extensions.ERXBlowfishCipherKey the blowfish key to use
 */
public class ERXBlowfishCrypter extends ERXAbstractBlowfishCrypter {
	/**
	 * Generates a secret key from the System property
	 * <b>er.extensions.ERXBlowfishCipherKey</b>. This secret key is used when
	 * generating the blowfish cipher.
	 * 
	 * @return a secret key for the blowfish cipher
	 */
	@Override
	protected Key secretBlowfishKey() throws NoSuchAlgorithmException {
		String blowfishKey = ERXProperties.stringForKey("er.extensions.ERXBlowfishCipherKey");
		if (blowfishKey == null) {
			log.warn("er.extensions.ERXBlowfishCipherKey not set in defaults.  Should be set before using the cipher.");
			blowfishKey = ERXProperties.stringForKey("ERBlowfishCipherKey");
			if (blowfishKey == null) {
				blowfishKey = "DefaultCipherKey";
			}
			else {
				log.warn("ERBlowfishCipherKey is deprecated, please use er.extensions.ERXBlowfishCipherKey");
			}
		}
		return new SecretKeySpec(blowfishKey.getBytes(), "Blowfish");
	}
}
