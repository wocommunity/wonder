package er.extensions;

/**
 * Provides a simple interface on top of various encryption algorithms.
 * 
 * @author ?
 */
public interface ERXCrypterInterface {
	/**
	 * Returns a String version of the encrypted clear text.
	 * 
	 * @param clearText
	 *            the text to encrypt
	 * @return an encrypted version of the text
	 */
	public String encrypt(String clearText);

	/**
	 * Returns the decrypted String of the encrypted text.
	 * 
	 * @param cryptedText
	 *            the string to decrypt
	 * @return the decrypted string
	 */
	public String decrypt(String cryptedText);
}
