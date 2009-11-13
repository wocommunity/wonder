
package er.extensions.foundation;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ERXUtilitiesTest extends TestCase {

    public void testStackTrace() {
        String trace = ERXUtilities.stackTrace();

        Assert.assertTrue(trace.indexOf("ERXUtilitiesTest.java") > 0);
        Assert.assertTrue(trace.indexOf("ERXUtilities.java") < 0);
    }
}
