
package er.extensions.eof;

import junit.framework.Assert;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSData;

import er.erxtest.ERXTestCase;
import er.erxtest.ERXTestUtilities;
import er.erxtest.model.Company;

/**
 * Tests of the {@link er.extensions.eof.ERXKeyGlobalID} class. Methods in this class rely on the fact
 * that the objects being used while testing, those being Company and sometimes Employee, have a
 * single-attribute primary key.
 */
public class ERXKeyGlobalIDTest extends ERXTestCase {

	private EOEditingContext ec;
	private Company co;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		ec = ERXEC.newEditingContext();
		co = Company.createCompany(ec, "Foobar.com");
		ec.saveChanges();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();

//		ec.deleteObject(co);
//		ec.saveChanges();		
	}

	public void testConstructor() {

		ERXKeyGlobalID xkgid = new ERXKeyGlobalID(Company.ENTITY_NAME, EOUtilities.primaryKeyForObject(ec, co).values().toArray());
		Assert.assertNotNull(xkgid);
	}

	public void testFromData() {

		int pk = ERXTestUtilities.pkOne(ec, co);
		
		ERXKeyGlobalID xkgid = ERXKeyGlobalID.fromData(new NSData((Company.ENTITY_NAME+"."+pk).getBytes()));
		Assert.assertNotNull(xkgid);
	}

	public void testFromString() {
	
		int pk = ERXTestUtilities.pkOne(ec, co);

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.fromString(Company.ENTITY_NAME+"."+pk);
		Assert.assertNotNull(xkgid);
	}

	@SuppressWarnings("boxing")
	public void testEquals() {

		EOKeyGlobalID kgid1 = EOKeyGlobalID.globalIDWithEntityName(Company.ENTITY_NAME, new Integer[] { ERXTestUtilities.pkOne(ec, co) } ); 
		
		ERXKeyGlobalID xkgid1 = ERXKeyGlobalID.globalIDForGID(kgid1);
		ERXKeyGlobalID xkgid2 = ERXKeyGlobalID.globalIDForGID(kgid1);

		Assert.assertFalse(kgid1.equals(xkgid1));
		Assert.assertFalse(xkgid1.equals(kgid1));

		Assert.assertTrue(xkgid1.equals(xkgid2));
		Assert.assertTrue(xkgid2.equals(xkgid1));
		
		EOEditingContext ec2 = ERXEC.newEditingContext();
		EOEnterpriseObject co2 = ERXEOControlUtilities.localInstanceOfObject(ec2, co);

		EOKeyGlobalID kgid2 = EOKeyGlobalID.globalIDWithEntityName(Company.ENTITY_NAME, new Integer[] { ERXTestUtilities.pkOne(ec2, co2) } ); 
		ERXKeyGlobalID xkgid3 = ERXKeyGlobalID.globalIDForGID(kgid2);

		Assert.assertFalse(kgid2.equals(xkgid3));
		Assert.assertFalse(xkgid3.equals(kgid2));

		Assert.assertTrue(xkgid1.equals(xkgid3));
		Assert.assertTrue(xkgid3.equals(xkgid1));	
	}

	public void testGlobalIDForGID() {

		EOGlobalID gid = ec.globalIDForObject(co);

		ERXKeyGlobalID xkgid1 = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)gid);
		Assert.assertEquals(gid, xkgid1.globalID());
	}

	@SuppressWarnings("boxing")
	public void testGlobalID() {

		EOGlobalID gid = ec.globalIDForObject(co);

		int pk = ERXTestUtilities.pkOne(ec, co);

		ERXKeyGlobalID xkgid1 = new ERXKeyGlobalID(Company.ENTITY_NAME, new Integer[] { pk });
		Assert.assertEquals(gid, xkgid1.globalID());
				
		ERXKeyGlobalID xkgid2 = ERXKeyGlobalID.fromData(new NSData((Company.ENTITY_NAME+"."+pk).getBytes()));
		Assert.assertEquals(gid, xkgid2.globalID());

		ERXKeyGlobalID xkgid3 = ERXKeyGlobalID.fromString(Company.ENTITY_NAME+"."+pk);
		Assert.assertEquals(gid, xkgid3.globalID());
	}
	
	public void test_keyValuesNoCopy() {

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)ec.globalIDForObject(co));

		// How to test the no-copy-ness? -rrk
		//
		assertEquals(xkgid.keyValues(), xkgid._keyValuesNoCopy());
	}

	public void testHashCode() {

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)ec.globalIDForObject(co));

		// Is there a way to figure out what the hashCode _should_ be without just asking for it?
		// Asking for it would make this test tautological. -rrk
		//
		Assert.assertTrue(xkgid.hashCode() != 0);
	}
    
	public void testKeyCount() {

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)ec.globalIDForObject(co));

		Assert.assertEquals(1, xkgid.keyCount());
	}
	           
	@SuppressWarnings("boxing")
	public void testKeyValues() {

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)ec.globalIDForObject(co));

		int pk = ERXTestUtilities.pkOne(ec, co);

		Integer[] values = new Integer[] { pk };
		assertEquals(values, xkgid.keyValues());
	}

	public void testToString() {

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)ec.globalIDForObject(co));

		int pk = ERXTestUtilities.pkOne(ec, co);

		Assert.assertEquals("_EOIntegralKeyGlobalID["+Company.ENTITY_NAME+" (java.lang.Integer)"+pk+"]", xkgid.toString());
	}
}
