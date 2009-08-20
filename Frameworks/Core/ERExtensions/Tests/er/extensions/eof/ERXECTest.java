
package er.extensions.eof;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webobjects.eocontrol.EOEditingContext;

public class ERXECTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testConstructor() {
        Assert.assertNotNull(new ERXEC());
    }

    public void testConstructorWithObjectStore() {

        EOEditingContext parentEC1 = new EOEditingContext();
        EOEditingContext parentEC2 = new EOEditingContext();

        ERXEC ec1 = new ERXEC(parentEC1);
        Assert.assertNotNull(ec1);

        ERXEC ec2 = new ERXEC(parentEC2);
        Assert.assertNotNull(ec2);

        Assert.assertEquals(parentEC1, ec1.parentObjectStore());
        Assert.assertEquals(parentEC2, ec2.parentObjectStore());

        ERXEC parentEC3 = new ERXEC();

        ERXEC ec3 = new ERXEC(parentEC3);
        Assert.assertNotNull(ec3);

        Assert.assertEquals(parentEC3, ec3.parentObjectStore());
    }
}
