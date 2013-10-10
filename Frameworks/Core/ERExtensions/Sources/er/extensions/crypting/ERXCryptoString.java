package er.extensions.crypting;


public class ERXCryptoString implements Comparable<ERXCryptoString> {
	private final String value;
	
	public ERXCryptoString(String clearText) {
		value = clearText == null ? "" : clearText;
	}
	
	public static ERXCryptoString createInstanceFromCryptoString(String encryptedString) {
		return encryptedString==null?null:new ERXCryptoString(decryptText(encryptedString));
	}
	
	private static String encryptText(String clearText) {
		return ERXCrypto.defaultCrypter().encrypt(clearText);
	}

	private static String decryptText(String encryptedText) {
		return ERXCrypto.defaultCrypter().decrypt(encryptedText);
	}

	public int compareTo(ERXCryptoString cryptoString) {
		return value.compareTo(cryptoString.toString());
	}

	@Override
	public boolean equals(Object object) {
		return (object instanceof ERXCryptoString)?value.equals(object.toString()):false;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
	
	public String toCryptoString() {
		return encryptText(value);
	}

	@Override
	public String toString() {
		return value;
	}
}
