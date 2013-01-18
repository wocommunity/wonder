
package com.webobjects.eoaccess;

import java.net.URL;

import junit.framework.Assert;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.erxtest.ERXTestCase;
import er.extensions.eof.ERXEC;

/* Test the ERXEntity methods. These tests are extremely minimal.
 * This class exists in order to deal with some issues in inheritance, and
 * this system is not configured to use models that use inheritance yet.
 *
 * @author Ray Kiddy, kiddyr@users.sourceforge.net
 */
public class ERXEntityTest extends ERXTestCase {

	static String buildRoot;
	static {
		buildRoot = System.getProperty("build.root");
	}
	EOEditingContext ec;
	String adaptorName = "Memory";
	String modelName;
	EOModel model;

	@Override
	public void setUp() throws Exception {
		super.setUp();

//		if (ec != null) ec.dispose();
//		if (model != null) model.dispose();
//
//		EOModelGroup.setDefaultGroup(new EOModelGroup());
//
//		modelName = "ERXTest";
//
//		URL modelUrl = ERXFileUtilities.pathURLForResourceNamed(modelName+".eomodeld", null, null);
//
//		EOModelGroup.defaultGroup().addModel(new EOModel(modelUrl));
//
//		model = EOModelGroup.defaultGroup().modelNamed(modelName);
//		model.setConnectionDictionary(ERExtensionsTest.connectionDict(adaptorName));

		model = EOModelGroup.defaultGroup().modelNamed("ERXTest");
		ec = ERXEC.newEditingContext();
	}

	public void testOne() { }
	
	public void testConstructor() {
		ERXEntity entity = new ERXEntity();
		Assert.assertNotNull(entity);
	}

	public void testPlistConstructor() {
		URL entityUrl = null;        	
		try {
			entityUrl = new java.net.URL(model.pathURL()+"/Company.plist");
		} catch (java.net.MalformedURLException e) { throw new IllegalArgumentException(e.getMessage()); }

		NSDictionary plist = (NSDictionary)NSPropertyListSerialization.propertyListWithPathURL(entityUrl);

		Assert.assertNotNull(new ERXEntity(plist, model));
	}

	public void _testAnyAttributeNamed() {

		// Should this not return just a "new ERXEntity()"? It returns null.
		//
		//Assert.assertNotNull(new ERXEntity(null, null));
	}

	public void testHasExternalName() {
		URL entityUrl = null;        	
		try {
			entityUrl = new java.net.URL(model.pathURL()+"/Company.plist");
		} catch (java.net.MalformedURLException e) { throw new IllegalArgumentException(e.getMessage()); }

		NSDictionary plist = (NSDictionary)NSPropertyListSerialization.propertyListWithPathURL(entityUrl);

		ERXEntity erxentity = new ERXEntity(plist, model);

		Assert.assertTrue(erxentity.hasExternalName());
	}

	public void testSetClassDescription() {

		EOEntity entity1 = EOModelGroup.defaultGroup().entityNamed("Company");
		EOClassDescription desc = entity1.classDescriptionForInstances();

		Assert.assertNotNull(desc);

		URL entityUrl = null;        	
		try {
			entityUrl = new java.net.URL(model.pathURL()+"/Employee.plist");
		} catch (java.net.MalformedURLException e) { throw new IllegalArgumentException(e.getMessage()); }

		NSDictionary plist = (NSDictionary)NSPropertyListSerialization.propertyListWithPathURL(entityUrl);

		ERXEntity entity2 = new ERXEntity(plist, model);

		// Using a mis-matched EOClassDescription here, but doing that on purpose so we can verify the superclass did not just ignore the set.
		//
		entity2.setClassDescription(desc);

		//Assert.assertTrue(ERExtensionsTest.equalsForEOAccessObjects(desc, entity2.classDescriptionForInstances()));
	}
}
