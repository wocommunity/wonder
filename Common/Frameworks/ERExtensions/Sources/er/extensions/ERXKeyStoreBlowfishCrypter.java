package er.extensions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.spec.SecretKeySpec;

/**
 * <p>
 * ERXKeyStoreBlowfishCrypter is a blowfish implementation of the crypter
 * interface that loads its secret key from a java keystore.
 * </p>
 * <p>
 * Because the java keystore API is so damn ridiculous, you can use the main
 * method provided in this class to generate the keystore with your blowfish
 * password in it.
 * </p>
 * <p>
 * If you want to just use all of the default values, you can run this class's
 * main method from inside eclipse with the arguments:
 * </p>
 * 
 * <code>
 * "" "" "" "" "yourblowfishpassword"
 * </code>
 * 
 * <p>
 * The easiest way to choose to use this crypter is to override the default
 * blowfish crypter with the Properties entries:
 * </p>
 * 
 * <code>
 * er.extensions.ERXCrypto.crypters=Blowfish
 * er.extensions.ERXCrypto.crypter.Blowfish=er.extensions.ERXKeyStoreBlowfishCrypter
 * </code>
 * 
 * @author mschrag
 * @property er.extensions.ERXKeyStoreBlowfishCrypter.keystorePath the path of
 *           the keystore that contains the blowfish key for this crypter. The
 *           default keystore path is
 *           "~/.er.extensions.ERXKeyStoreBlowfishCrypter.keystore"
 * @property er.extensions.ERXKeyStoreBlowfishCrypter.keystorePassword the
 *           keystore password (if necessary)
 * @property er.extensions.ERXKeyStoreBlowfishCrypter.keyAlias the alias of the
 *           blowfish key in the keystore
 * @property er.extensions.ERXKeyStoreBlowfishCrypter.keyPassword the password
 *           of the blowfish key in the keystore (if necessary)
 */
public class ERXKeyStoreBlowfishCrypter extends ERXAbstractBlowfishCrypter {
	private static final String KEYSTORE_TYPE = "JCEKS";
	private static final String DEFAULT_KEY_ALIAS = "er.extensions.ERXBlowfishCipherKey";
	private static final String DEFAULT_KEY_PASSWORD = "er.extensions.ERXBlowfishCipherKey";
	private static final String DEFAULT_KEYSTORE_PASSWORD = "er.extensions.ERXBlowfishCipherKey";

	private static final String defaultKeyStorePath() {
		return new File(System.getProperty("user.home"), ".er.extensions.ERXKeyStoreBlowfishCrypter.keystore").getAbsolutePath();
	}

	@Override
	protected Key secretBlowfishKey() throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
		String keystorePath = ERXProperties.stringForKeyWithDefault("er.extensions.ERXKeyStoreBlowfishCrypter.keystorePath", ERXKeyStoreBlowfishCrypter.defaultKeyStorePath());
		File keystoreFile = new File(keystorePath);
		KeyStore keyStore = KeyStore.getInstance(ERXKeyStoreBlowfishCrypter.KEYSTORE_TYPE);
		if (!keystoreFile.exists()) {
			throw new FileNotFoundException("There is no keystore '" + keystoreFile.getAbsolutePath() + "'.  Run ERXKeyStoreBlowfishCrypter's main method for help.  Oh, and to hell with whoever wrote Java's KeyStore API.");
		}

		String keystorePasswordStr = ERXProperties.stringForKeyWithDefault("er.extensions.ERXKeyStoreBlowfishCrypter.keystorePassword", ERXKeyStoreBlowfishCrypter.DEFAULT_KEYSTORE_PASSWORD);
		char[] keystorePassword = null;
		if (keystorePasswordStr != null) {
			keystorePassword = keystorePasswordStr.toCharArray();
		}

		FileInputStream keystoreInputStream = new FileInputStream(keystoreFile);
		try {
			keyStore.load(keystoreInputStream, keystorePassword);
		}
		finally {
			keystoreInputStream.close();
		}

		String alias = ERXProperties.stringForKeyWithDefault("er.extensions.ERXKeyStoreBlowfishCrypter.keyAlias", ERXKeyStoreBlowfishCrypter.DEFAULT_KEY_ALIAS);

		// We can't use ERXProperties.decryptedStringForKey because it would
		// potentially result in an infinite look if this is the default
		// crypter.
		String keyPasswordStr = ERXProperties.stringForKeyWithDefault("er.extensions.ERXKeyStoreBlowfishCrypter.keyPassword", ERXKeyStoreBlowfishCrypter.DEFAULT_KEY_PASSWORD);
		char[] keyPassword = keyPasswordStr.toCharArray();

		Key key = keyStore.getKey(alias, keyPassword);
		return key;
	}

	public static void main(String[] args) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, FileNotFoundException, IOException {
		if (args.length != 5) {
			System.out.println("ERXKeyStoreBlowfishCrypter.main: [keystorePath] [keystorePassword] [keyAlias] [keyPassword] [blowfishKey]");
			System.out.println("                                 Note: you can set any of the parameters to \"\" to use the default values except for blowfishKey.");
			System.exit(0);
		}
		String keystorePath = ERXKeyStoreBlowfishCrypter.defaultKeyStorePath();
		if (args[0].length() > 0) {
			keystorePath = args[0];
		}

		String keystorePasswordStr = ERXKeyStoreBlowfishCrypter.DEFAULT_KEYSTORE_PASSWORD;
		if (args[1].length() > 0) {
			keystorePasswordStr = args[1];
		}
		char[] keystorePassword = keystorePasswordStr.toCharArray();

		String keyAlias = ERXKeyStoreBlowfishCrypter.DEFAULT_KEY_ALIAS;
		if (args[2].length() > 0) {
			keyAlias = args[2];
		}

		String keyPasswordStr = ERXKeyStoreBlowfishCrypter.DEFAULT_KEY_PASSWORD;
		if (args[3].length() > 0) {
			keyPasswordStr = args[3];
		}
		char[] keyPassword = keyPasswordStr.toCharArray();

		String blowfishKey = args[4];

		KeyStore keyStore = KeyStore.getInstance(ERXKeyStoreBlowfishCrypter.KEYSTORE_TYPE);

		File keystoreFile = new File(keystorePath);
		if (keystoreFile.exists()) {
			FileInputStream keystoreInputStream = new FileInputStream(keystoreFile);
			try {
				keyStore.load(keystoreInputStream, keystorePassword);
			}
			finally {
				keystoreInputStream.close();
			}
		}
		else {
			keyStore.load(null, keystorePassword);
		}

		SecretKeySpec key = new SecretKeySpec(blowfishKey.getBytes(), "Blowfish");
		keyStore.setKeyEntry(keyAlias, key, keyPassword, null);

		FileOutputStream keystoreOutputStream = new FileOutputStream(keystoreFile);
		try {
			keyStore.store(keystoreOutputStream, keystorePassword);
		}
		finally {
			keystoreOutputStream.close();
		}

		System.out.println("ERXKeyStoreBlowfishCrypter.main: Generated the keystore '" + keystorePath + "'.");
	}
}
