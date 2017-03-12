package er.extensions.foundation;

import java.security.SecureRandom;

import com.webobjects.foundation.NSData;

/**
 * Helper functions for UUID
 *
 */
public class UUIDUtilities  {
	private static final String validHexCharacters = "ABCDEF0123456789abcdef";
	static final SecureRandom randomGenerator = new SecureRandom();

    public final static int typeByteOffset = 6;
    public final static int variationByteOffset = 8;

	
	/**
	 * Generate a type 4 UUID from a secure random source like the java UUID class.
	 * @return a new UUID in a NSData
	 */
    public static NSData generateAsNSData() {
		return new NSData(generateAsByteArray());
	}

	/**
	 * Generate a type 4 UUID from a secure random source like the java UUID class.
	 * @return a new UUID in a byte[]
	 */
    private static byte[] generateAsByteArray() {
		byte[] value = new byte[16];
		randomGenerator.nextBytes(value);
		
		value[typeByteOffset] &= 0xF; // Clear the type part of the byte
		value[typeByteOffset] |= 0x40; // Set the type part of the byte to random
		value[variationByteOffset] &= 0x3F; // Clear the variant part of the byte
		value[variationByteOffset] |= 0x80; // set to IETF variant
		
		return value;
	}

    /**
     * Decode a string representing a UUID in hex. 
     * All non hex char are filtered before the decoding, any formats are accepted.
     * @param uuid the string representing a UUID
     * @return the decoded UUID in a NSData
     */
	static public NSData decodeStringAsNSData(String uuid) {
		return new NSData(decodeStringAsByteArray(uuid));
	}
		
    /**
     * Decode a string representing a UUID in hex. 
     * All non hex char are filtered before the decoding, any formats are accepted.
     * @param uuid the string representing a UUID
     * @return the decoded UUID in a byte[]
     */
	static public byte[] decodeStringAsByteArray(String uuid) {
		if (uuid == null) {
			throw new IllegalArgumentException("Null is not a valid UUID value.");
		}
		String cleannedUuid = ERXStringUtilities.removeExceptCharacters(uuid, validHexCharacters);
		if (cleannedUuid.length() != 32) {
			throw new IllegalArgumentException("\""+uuid+"\" is not a valid hex string representing a byte[16].");
		}
		
		try {
			return ERXStringUtilities.hexStringToByteArray(cleannedUuid);
		}
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("\""+uuid+"\" is not a valid hex string representing a byte[16].", e);
		}
	}
		
	/**
	 * Encode a UUID in a String using the usual format 12345678-1234-1234-1234-123456789ABC 
	 * @param uuid a NSData containing a UUID
	 * @return the encoded UUID
	 */
	static public String encodeAsPrettyString(NSData uuid) {
		return encodeAsPrettyString(uuid.bytes());
	}

	/**
	 * Encode a UUID in a String using the usual format 12345678-1234-1234-1234-123456789ABC 
	 * @param uuid a byte[] containing a UUID
	 * @return the encoded UUID
	 */
	static public String encodeAsPrettyString(byte[] uuid) {		
		String rawString = encodeAsString(uuid);
		
		StringBuilder formattedString = new StringBuilder();
		formattedString.append(rawString.substring(0, 8));
		formattedString.append("-");
		formattedString.append(rawString.substring(8, 12));
		formattedString.append("-");
		formattedString.append(rawString.substring(12, 16));
		formattedString.append("-");
		formattedString.append(rawString.substring(16, 20));
		formattedString.append("-");
		formattedString.append(rawString.substring(20, 32));
		return formattedString.toString();
	}
	
	/**
	 * Encode a UUID in a String in hex 
	 * @param uuid a NSData containing a UUID
	 * @return the encoded UUID
	 */
	static public String encodeAsString(NSData uuid) {
		return encodeAsString(uuid.bytes());
	}
	
	/**
	 * Encode a UUID in a String in hex 
	 * @param uuid a byte[] containing a UUID
	 * @return the encoded UUID
	 */
	static public String encodeAsString(byte[] uuid) {
		if (uuid == null) {
			throw new IllegalArgumentException("Null is not a valid UUID value.");
		}
		
		String rawString = ERXStringUtilities.byteArrayToHexString(uuid);
		if (uuid.length != 16) {
			throw new IllegalArgumentException("\""+rawString+"\" is not a byte[16].");
		}
		return rawString;
	}
}
