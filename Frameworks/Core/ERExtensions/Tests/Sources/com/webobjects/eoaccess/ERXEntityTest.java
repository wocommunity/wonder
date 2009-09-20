
package com.webobjects.eoaccess;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

import com.webobjects.foundation.NSArray;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.ERExtensionsTest;

/* Test the ERXEntity methods. These tests are extremely minimal.
 * This class exists in order to deal with some issues in inheritance, and
 * this system is not configured to use models that use inheritance yet.
 *
 * @author kiddyr@sourceforge.net
 */
public class ERXEntityTest extends TestCase {

    static String buildRoot;
    static {
         buildRoot = System.getProperty("build.root");
    }

    public void testAll() {

        TestSuite suite = er.extensions.ERExtensionsTest.suite;

        // See note in er.extensions.eof.ERXEOAccessUtilities.testAll() -rrk

        NSArray<String> methods = er.extensions.ERExtensionsTest.testMethodsForClassName("com.webobjects.eoaccess.ERXEntityTest$Tests");

        for (int idx = 0; idx < methods.count(); idx++) {
            String testName = methods.get(idx);

            suite.addTest(new Tests(testName, "MemoryBusinessModel"));
            //suite.addTest(new Tests(testName, "MySQLBusinessModel"));
        }
    }

    public static class Tests extends TestCase {

        EOEditingContext ec;
        String modelName;
        EOModel model;

        public Tests(String name, String param) {
           super(name);
           modelName = param;
        }

        String config() {
            return "modelName: \""+modelName+"\"";
        }

        public void setUp() throws Exception {
            super.setUp();

            if (ec != null) ec.dispose();
            if (model != null) model.dispose();

            EOModelGroup.setDefaultGroup(new EOModelGroup());

            try {
                EOModelGroup.defaultGroup().addModel(new EOModel(new java.net.URL("file://"+buildRoot+"/ERExtensions.framework/TestResources/"+modelName+".eomodeld")));
            } catch (java.net.MalformedURLException mue) { System.out.println(this.config()+", mue: "+mue); }
            model = EOModelGroup.defaultGroup().modelNamed(modelName);
            ec = new EOEditingContext();
        }

        public void testConstructor() {
            ERXEntity entity = new ERXEntity();
            Assert.assertNotNull(this.config(), entity);
        }

        public void testPlistConstructor() {

            NSDictionary plist = null;

            try {
                plist = NSPropertyListSerialization.dictionaryWithPathURL(
                            new java.net.URL("file://"+buildRoot+"/ERExtensions.framework/TestResources/"+modelName+".eomodeld/Company.plist"));
        } catch (java.net.MalformedURLException mue) { Assert.fail(this.config()+", mue: "+mue.getMessage()); }

            // TODO - we probably want to verify here that the values returned by the ERXEntity and found in the plist are the same.
            //
            Assert.assertNotNull(this.config(), new ERXEntity(plist, model));
        }

        public void testAnyAttributeNamed() {

            // Should this not return just a "new ERXEntity()"? It returns null.
            //
            //Assert.assertNotNull(new ERXEntity(null, null));
        }

        public void testHasExternalName() {

            NSDictionary plist = null;
            try {
                plist = NSPropertyListSerialization.dictionaryWithPathURL(
                            new java.net.URL("file://"+buildRoot+"/ERExtensions.framework/TestResources/"+modelName+".eomodeld/Company.plist"));
            } catch (java.net.MalformedURLException mue) { Assert.fail(this.config()+", mue: "+mue.getMessage()); }

            ERXEntity erxentity = new ERXEntity(plist, model);

            Assert.assertTrue(this.config(), erxentity.hasExternalName());
        }

        public void testSetClassDescription() {

            EOEntity entity1 = EOModelGroup.defaultGroup().entityNamed("Company");
            EOClassDescription desc = entity1.classDescriptionForInstances();

            Assert.assertNotNull(desc);

            NSDictionary plist = null;
            try {
                plist = NSPropertyListSerialization.dictionaryWithPathURL(
                                                    new java.net.URL("file://"+buildRoot+"/ERExtensions.framework/TestResources/"+modelName+".eomodeld/Employee.plist"));
            } catch (java.net.MalformedURLException mue) { Assert.fail(this.config()+", mue: "+mue.getMessage()); }

            ERXEntity entity2 = new ERXEntity(plist, model);

            // Using a mis-matched EOClassDescription here, but doing that on purpose so we can verify the superclass did not just ignore the set.
            //
            entity2.setClassDescription(desc);

            Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(desc, entity2.classDescriptionForInstances()));
        }
    }
}
