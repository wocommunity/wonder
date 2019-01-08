
package er.extensions.eof;

import org.junit.Assert;

import com.webobjects.eoaccess.EOModelGroup;

import er.erxtest.ERXTestCase;

public class ERXModelGroupTest extends ERXTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testConstructor() {
        Assert.assertNotNull(new ERXModelGroup());
    }

    public void testSettingEOModelGroupClass() {
        Assert.assertEquals("er.extensions.eof.SortOfModelGroup", EOModelGroup.defaultGroup().getClass().getName());
    }
}
