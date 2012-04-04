
package er.extensions.excel;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import er.erxtest.ERXTestCase;
import er.erxtest.ERXTestUtilities;

public class EGSimpleWorkbookHelperTest extends ERXTestCase {

    public void testNotNull() {
        Assert.asserNotNull(new EGSimpleWorkbookHelper());
    }
}

