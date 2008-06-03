package er.erxtest.tests;

import java.io.IOException;

import junit.framework.TestCase;
import er.extensions.foundation.ERXRuntimeUtilities;
import er.extensions.foundation.ERXRuntimeUtilities.Result;
import er.extensions.foundation.ERXRuntimeUtilities.TimeoutException;

public class ERXRuntimeUtilitiesTest extends TestCase {

	public void testExecuteMultipleFastCommands() throws IOException, TimeoutException {
		for (int i = 0; i < 100; ++i) {
		    Result result = ERXRuntimeUtilities.execute(new String[] {"echo", String.valueOf(i)}, null, null, 0);
		    assertEquals(i + "\n", result.getResponseAsString());
		}
	}
}
