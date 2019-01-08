package com.webobjects.foundation;

import org.junit.Assert;

import er.erxtest.ERXTestCase;

public class NSKeyValueCodingTest extends ERXTestCase {

    public void testValueForKey() {

        Assert.assertEquals( "method getIvar", (new KVC001()).valueForKey("ivar") );
        Assert.assertEquals( "method ivar", (new KVC002()).valueForKey("ivar") );
        Assert.assertEquals( "method isIvar", (new KVC003()).valueForKey("ivar") );
        Assert.assertEquals( "method _getIvar", (new KVC004()).valueForKey("ivar") );
        Assert.assertEquals( "method _ivar", (new KVC005()).valueForKey("ivar") );
        Assert.assertEquals( "method _isIvar", (new KVC006()).valueForKey("ivar") );

        Assert.assertEquals( "field _ivar", (new KVC007()).valueForKey("ivar") );
        Assert.assertEquals( "field _isIvar", (new KVC008()).valueForKey("ivar") );
        Assert.assertEquals( "field ivar", (new KVC009()).valueForKey("ivar") );
        Assert.assertEquals( "field isIvar", (new KVC010()).valueForKey("ivar") );

        try {
            Assert.assertEquals( null, (new KVC00X()).valueForKey("ivar") );
            Assert.fail("KVC004 should have thrown UnknownKeyException");
        } catch (com.webobjects.foundation.NSKeyValueCoding.UnknownKeyException e) { }
    }

    // TODO - the testTakeValueForKey method still needs to be done. -rrk

    class KVC001 implements NSKeyValueCoding {

        public Object valueForKey(String key) { return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key); }
        public void takeValueForKey(Object value, String key) { NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key); }

        public String ivar = "field ivar";
        public String isIvar = "field isIvar";
        public String _ivar = "field _ivar";
        public String _isIvar = "field _isIvar";

        public Object getIvar() { return "method getIvar"; }
        public Object ivar() { return "method ivar"; }
        public Object isIvar() { return "method isIvar"; }
        public Object _getIvar() { return "method _getIvar"; }
        public Object _ivar() { return "method _ivar"; }
        public Object _isIvar() { return "method _isIvar"; }
    }

    class KVC002 implements NSKeyValueCoding {

        public Object valueForKey(String key) { return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key); }
        public void takeValueForKey(Object value, String key) { NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key); }

        public String ivar = "field ivar";
        public String isIvar = "field isIvar";
        public String _ivar = "field _ivar";
        public String _isIvar = "field _isIvar";

        public Object ivar() { return "method ivar"; }
        public Object isIvar() { return "method isIvar"; }
        public Object _getIvar() { return "method _getIvar"; }
        public Object _ivar() { return "method _ivar"; }
        public Object _isIvar() { return "method _isIvar"; }
    }

    class KVC003 implements NSKeyValueCoding {

        public Object valueForKey(String key) { return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key); }
        public void takeValueForKey(Object value, String key) { NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key); }

        public String ivar = "field ivar";
        public String isIvar = "field isIvar";
        public String _ivar = "field _ivar";
        public String _isIvar = "field _isIvar";

        public Object isIvar() { return "method isIvar"; }
        public Object _getIvar() { return "method _getIvar"; }
        public Object _ivar() { return "method _ivar"; }
        public Object _isIvar() { return "method _isIvar"; }
    }

    class KVC004 implements NSKeyValueCoding {

        public Object valueForKey(String key) { return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key); }
        public void takeValueForKey(Object value, String key) { NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key); }

        public String ivar = "field ivar";
        public String isIvar = "field isIvar";
        public String _ivar = "field _ivar";
        public String _isIvar = "field _isIvar";

        public Object _getIvar() { return "method _getIvar"; }
        public Object _ivar() { return "method _ivar"; }
        public Object _isIvar() { return "method _isIvar"; }
    }

    class KVC005 implements NSKeyValueCoding {

        public Object valueForKey(String key) { return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key); }
        public void takeValueForKey(Object value, String key) { NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key); }

        public String ivar = "field ivar";
        public String isIvar = "field isIvar";
        public String _ivar = "field _ivar";
        public String _isIvar = "field _isIvar";

        public Object _ivar() { return "method _ivar"; }
        public Object _isIvar() { return "method _isIvar"; }
    }

    class KVC006 implements NSKeyValueCoding {

        public Object valueForKey(String key) { return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key); }
        public void takeValueForKey(Object value, String key) { NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key); }

        public String ivar = "field ivar";
        public String isIvar = "field isIvar";
        public String _ivar = "field _ivar";
        public String _isIvar = "field _isIvar";

        public Object _isIvar() { return "method _isIvar"; }
    }

    class KVC007 implements NSKeyValueCoding {

        public Object valueForKey(String key) { return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key); }
        public void takeValueForKey(Object value, String key) { NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key); }

        public String ivar = "field ivar";
        public String isIvar = "field isIvar";
        public String _ivar = "field _ivar";
        public String _isIvar = "field _isIvar";
    }   

    class KVC008 implements NSKeyValueCoding {

        public Object valueForKey(String key) { return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key); }
        public void takeValueForKey(Object value, String key) { NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key); }

        public String ivar = "field ivar";
        public String isIvar = "field isIvar";
        public String _isIvar = "field _isIvar";
    }   

    class KVC009 implements NSKeyValueCoding {

        public Object valueForKey(String key) { return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key); }
        public void takeValueForKey(Object value, String key) { NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key); }

        public String ivar = "field ivar";
        public String isIvar = "field isIvar";
    }   

    class KVC010 implements NSKeyValueCoding {

        public Object valueForKey(String key) { return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key); }
        public void takeValueForKey(Object value, String key) { NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key); }

        public String isIvar = "field isIvar";
    }   

    class KVC00X implements NSKeyValueCoding {

        public Object valueForKey(String key) { return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key); }
        public void takeValueForKey(Object value, String key) { NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key); }

        public String getIvar = "field getIvar";
        public String _getIvar = "field _getIvar";
    }
}
