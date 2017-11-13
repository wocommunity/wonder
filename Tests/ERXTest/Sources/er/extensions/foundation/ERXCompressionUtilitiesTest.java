package er.extensions.foundation;

import org.apache.commons.lang3.CharEncoding;
import org.junit.Assert;
import org.junit.Test;

public class ERXCompressionUtilitiesTest {

	@Test
	public void gzipAndGunzipByteArray() throws Exception {
		String input = "Test";
		byte[] gzippedByteArray = ERXCompressionUtilities.gzipByteArray(input.getBytes(CharEncoding.UTF_8));

		byte[] gunzippedByteArray = ERXCompressionUtilities.gunzipByteArray(gzippedByteArray);
		String output = new String(gunzippedByteArray, CharEncoding.UTF_8);

		Assert.assertEquals(input, output);
	}

	@Test
	public void deflateAndInflateByteArray() throws Exception {
		String input = "Test";
		byte[] deflatedByteArray = ERXCompressionUtilities.deflateByteArray(input.getBytes(CharEncoding.UTF_8));

		byte[] inflatedByteArray = ERXCompressionUtilities.inflateByteArray(deflatedByteArray);
		String output = new String(inflatedByteArray, CharEncoding.UTF_8);

		Assert.assertEquals(input, output);
	}

}
