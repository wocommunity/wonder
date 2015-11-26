package com.webobjects.appserver;

import er.erxtest.ERXTestCase;

import junit.framework.Assert;

public class WOTimerTest extends ERXTestCase {

    public void testHashCode() {

        WOTimer timer1 = new WOTimer(1000, this, "selectorName", null, null, false);
        WOTimer timer2 = new WOTimer(1001, this, "selectorName", null, null, false);

        Assert.assertTrue(timer1.hashCode() != timer2.hashCode());

        WOTimer timer3 = new WOTimer(1000, this, "selector1", ic1, IClass1.class, false);
        WOTimer timer4 = new WOTimer(1000, this, "selector2", ic2, IClass2.class, false);

        // XXX This should fail. The timer1 and timer2 objects are observably different, but their hashCodes are the same. And they are not "equal". See below.
        //
        Assert.assertTrue(timer3.hashCode() == timer4.hashCode());

    }

    public void testEquals() {

        WOTimer timer1 = new WOTimer(1000, this, "selectorName", null, null, false);
        WOTimer timer2 = new WOTimer(1001, this, "selectorName", null, null, false);

        // XXX This should fail. The objects are observably different but they are "equal" to each other. Yet their hashCodes differ. See above.
        //
        Assert.assertTrue(timer1.equals(timer2));

        WOTimer timer3 = new WOTimer(1000, this, "selector1", ic1, IClass1.class, false);
        WOTimer timer4 = new WOTimer(1000, this, "selector2", ic2, IClass2.class, false);

        Assert.assertTrue( ! timer3.equals(timer4));
    }

    public void selectorName() { }

    public void selector1(IClass1 ic1param) { }
    public void selector2(IClass2 ic2param) { }

    public static class IClass1 { }
    public static class IClass2 { }

    IClass1 ic1 = new IClass1();
    IClass2 ic2 = new IClass2();
}
