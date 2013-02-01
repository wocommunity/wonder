package er.extensions.foundation;

import java.io.IOException;

import er.erxtest.ERXTestCase;
import er.extensions.foundation.ERXRuntimeUtilities.Result;
import er.extensions.foundation.ERXRuntimeUtilities.TimeoutException;

public class ERXRuntimeUtilitiesTest extends ERXTestCase {

	public void testExecuteMultipleFastCommands() throws IOException, TimeoutException {
		for (int i = 0; i < 100; ++i) {
		    Result result = ERXRuntimeUtilities.execute(new String[] {"echo", String.valueOf(i)}, null, null, 0);
		    assertEquals(i + "\n", result.getResponseAsString());
		}
	}
}
