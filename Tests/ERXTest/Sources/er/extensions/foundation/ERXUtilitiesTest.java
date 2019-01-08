
package er.extensions.foundation;

import org.junit.Assert;

import er.erxtest.ERXTestCase;

public class ERXUtilitiesTest extends ERXTestCase {

    public void testStackTrace() {
        String trace = ERXUtilities.stackTrace();

        Assert.assertTrue(trace.indexOf("ERXUtilitiesTest.java") > 0);
        Assert.assertTrue(trace.indexOf("ERXUtilities.java") < 0);
    }
}
