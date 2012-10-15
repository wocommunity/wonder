package com.webobjects.eoaccess;

import junit.framework.Assert;

import com.webobjects.eoaccess.ERXModel;

import er.erxtest.ERXTestCase;

public class ERXModelTest extends ERXTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testConstructor() {
        Assert.assertNotNull(new ERXModel());
    }
}
