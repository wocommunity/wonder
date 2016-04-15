package er.extensions.crypting;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.foundation.ERXProperties;

/**
 * ERXBlowfishCrypter is a blowfish implementation of the crypter interface.
 * 
 * @author mschrag
 * @property er.extensions.ERXBlowfishCipherKey the blowfish key to use
 */
public class ERXBlowfishCrypter extends ERXAbstractBlowfishCrypter {
	private static final Logger log = LoggerFactory.getLogger(ERXBlowfishCrypter.class);

	private String _blowfishKey;
	
	public ERXBlowfishCrypter(String blowfishKey) {
		_blowfishKey = blowfishKey;
	}
	
	public ERXBlowfishCrypter() {
		this(null);
	}
	
	/**
	 * Generates a secret key from the System property
	 * <b>er.extensions.ERXBlowfishCipherKey</b>. This secret key is used when
	 * generating the blowfish cipher.
	 * 
	 * @return a secret key for the blowfish cipher
	 */
	@Override
	protected Key secretBlowfishKey() throws NoSuchAlgorithmException {
		String blowfishKey = _blowfishKey;
		if (blowfishKey == null) {
			blowfishKey = ERXProperties.stringForKey("er.extensions.ERXBlowfishCipherKey");
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
		}
		return new SecretKeySpec(blowfishKey.getBytes(), "Blowfish");
	}
}
