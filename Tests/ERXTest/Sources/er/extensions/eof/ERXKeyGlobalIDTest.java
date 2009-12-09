
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
import er.erxtest.model.Employee;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Tests of the {@link er.extensions.eof.ERXKeyGlobalID} class. Methods in this class rely on the fact
 * that the objects being used while testing, those being Company and sometimes Employee, have a
 * single-attribute primary key.
 */
public class ERXKeyGlobalIDTest extends ERXTestCase {

	private EOEditingContext ec;
	
	public void setUp() throws Exception {
		ec = ERXEC.newEditingContext();
	}

	public void tearDown() throws Exception {
		ec.revert();
		//ERXTestUtilities.deleteObjectsWithPrefix(ec, Company.ENTITY_NAME, "ERXKeyGlobalID");
	}

	public void testConstructor() {
		
		Company co = (Company)ERXEOControlUtilities.createAndInsertObject(ec, Company.ENTITY_NAME);
		String name = ERXTestUtilities.randomName("ERXKeyGlobalID_Constructor");
		co.setName(name+"_1");
		ec.saveChanges();

		ERXKeyGlobalID xkgid = new ERXKeyGlobalID(Company.ENTITY_NAME, EOUtilities.primaryKeyForObject(ec, co).values().toArray());
		Assert.assertNotNull(xkgid);
	}

	public void testFromData() {

		Company co = (Company)ERXEOControlUtilities.createAndInsertObject(ec, Company.ENTITY_NAME);
		String name = ERXTestUtilities.randomName("ERXKeyGlobalID_FromData");
		co.setName(name+"_1");
		ec.saveChanges();
		
		int pk = ERXTestUtilities.pkOne(ec, co);
		
		ERXKeyGlobalID xkgid = ERXKeyGlobalID.fromData(new NSData((Company.ENTITY_NAME+"."+pk).getBytes()));
		Assert.assertNotNull(xkgid);

		ec.revert();
		//ERXTestUtilities.deleteObjectsWithPrefix(ec, Company.ENTITY_NAME, name);
	}

	public void testFromString() {

		Company co = (Company)EOUtilities.createAndInsertInstance(ec, Company.ENTITY_NAME);
		String name = ERXTestUtilities.randomName("ERXKeyGlobalID_FromString");
		co.setName(name+"_1");
		ec.saveChanges();
		
		int pk = ERXTestUtilities.pkOne(ec, co);

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.fromString(Company.ENTITY_NAME+"."+pk);
		Assert.assertNotNull(xkgid);

		ec.revert();
		//ERXTestUtilities.deleteObjectsWithPrefix(ec, Company.ENTITY_NAME, name);
	}

	public void _testEquals() {

		Employee emp = (Employee)ERXEOControlUtilities.createAndInsertObject(ec, Employee.ENTITY_NAME);
		Company co = (Company)ERXEOControlUtilities.createAndInsertObject(ec, Company.ENTITY_NAME);
		
		String name = ERXTestUtilities.randomName("ERXKeyGlobalID_Equals");

		emp.setFirstName("Bob");
		emp.setLastName(name+"_1");
		emp.setManager(Boolean.FALSE);
		
		co.setName(name+"_2");
		co.addToEmployees(emp);

		ec.saveChanges();

		EOGlobalID gid = ec.globalIDForObject(emp);

		Assert.assertFalse(ERXValueUtilities.isNull(emp));
		Assert.assertFalse(gid.isTemporary());

		EOEnterpriseObject emp2 = ERXEOControlUtilities.localInstanceOfObject(ERXEC.newEditingContext(), emp);	

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)gid);
		Assert.assertEquals(xkgid, xkgid);
		
		EOKeyGlobalID gid2 = (EOKeyGlobalID)ec.globalIDForObject(emp2);
		ERXKeyGlobalID xkgid2 = ERXKeyGlobalID.globalIDForGID(gid2);

		Assert.assertEquals(xkgid, xkgid2);
		
		ec.revert();
		//ERXTestUtilities.deleteObjectsWithPrefix(ec, Company.ENTITY_NAME, name);
	}

	public void testGlobalIDForGID() {

		Company co = (Company)ERXEOControlUtilities.createAndInsertObject(ec, Company.ENTITY_NAME);
		String name = ERXTestUtilities.randomName("ERXKeyGlobalID_GlobalIDForGID");
		co.setName(name+"_1");
		ec.saveChanges();

		EOGlobalID gid = ec.globalIDForObject(co);

		ERXKeyGlobalID xkgid1 = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)gid);
		Assert.assertEquals(gid, xkgid1.globalID());
		
		ec.revert();
		//ERXTestUtilities.deleteObjectsWithPrefix(ec, Company.ENTITY_NAME, name);
	}

	@SuppressWarnings("boxing")
	public void testGlobalID() {

		Company co = (Company)ERXEOControlUtilities.createAndInsertObject(ec, Company.ENTITY_NAME);
		String name = ERXTestUtilities.randomName("ERXKeyGlobalID_GlobalID");
		co.setName(name+"_1");
		ec.saveChanges();

		EOGlobalID gid = ec.globalIDForObject(co);

		int pk = ERXTestUtilities.pkOne(ec, co);

		ERXKeyGlobalID xkgid1 = new ERXKeyGlobalID(Company.ENTITY_NAME, new Integer[] { pk });
		Assert.assertEquals(gid, xkgid1.globalID());
				
		ERXKeyGlobalID xkgid2 = ERXKeyGlobalID.fromData(new NSData((Company.ENTITY_NAME+"."+pk).getBytes()));
		Assert.assertEquals(gid, xkgid2.globalID());

		ERXKeyGlobalID xkgid3 = ERXKeyGlobalID.fromString(Company.ENTITY_NAME+"."+pk);
		Assert.assertEquals(gid, xkgid3.globalID());
		
		ec.revert();
		//ERXTestUtilities.deleteObjectsWithPrefix(ec, Company.ENTITY_NAME, name);
	}
	
	public void test_keyValuesNoCopy() {
		Company co = (Company)ERXEOControlUtilities.createAndInsertObject(ec, Company.ENTITY_NAME);
		String name = ERXTestUtilities.randomName("ERXKeyGlobalID_KeyValuesNoCopy");
		co.setName(name+"_1");
		ec.saveChanges();

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)ec.globalIDForObject(co));

		// How to test the no-copy-ness? -rrk
		//
		assertEquals(xkgid.keyValues(), xkgid._keyValuesNoCopy());

		ec.revert();
		//ERXTestUtilities.deleteObjectsWithPrefix(ec, Company.ENTITY_NAME, name);
	}

	public void testHashCode() {
		Company co = (Company)ERXEOControlUtilities.createAndInsertObject(ec, Company.ENTITY_NAME);
		String name = ERXTestUtilities.randomName("ERXKeyGlobalID_KeyValuesNoCopy");
		co.setName(name+"_1");
		ec.saveChanges();

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)ec.globalIDForObject(co));

		// Is there a way to figure out what the hashCode _should_ be without just asking for it?
		// Asking for it would make this test tautological. -rrk
		//
		Assert.assertTrue(xkgid.hashCode() != 0);

		ec.revert();
		//ERXTestUtilities.deleteObjectsWithPrefix(ec, Company.ENTITY_NAME, name);
	}
    
	public void testKeyCount() {
		Company co = (Company)ERXEOControlUtilities.createAndInsertObject(ec, Company.ENTITY_NAME);
		String name = ERXTestUtilities.randomName("ERXKeyGlobalID_KeyValuesNoCopy");
		co.setName(name+"_1");
		ec.saveChanges();

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)ec.globalIDForObject(co));

		Assert.assertEquals(1, xkgid.keyCount());

		ec.revert();
		//ERXTestUtilities.deleteObjectsWithPrefix(ec, Company.ENTITY_NAME, name);
	}
	           
	@SuppressWarnings("boxing")
	public void testKeyValues() {

		Company co = (Company)ERXEOControlUtilities.createAndInsertObject(ec, Company.ENTITY_NAME);
		String name = ERXTestUtilities.randomName("ERXKeyGlobalID_KeyValuesNoCopy");
		co.setName(name+"_1");
		ec.saveChanges();

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)ec.globalIDForObject(co));

		int pk = ERXTestUtilities.pkOne(ec, co);

		Integer[] values = new Integer[] { pk };
		assertEquals(values, xkgid.keyValues());

		ec.revert();
		//ERXTestUtilities.deleteObjectsWithPrefix(ec, Company.ENTITY_NAME, name);
	}

	public void testToString() {

		Company co = (Company)ERXEOControlUtilities.createAndInsertObject(ec, Company.ENTITY_NAME);
		String name = ERXTestUtilities.randomName("ERXKeyGlobalID_KeyValuesNoCopy");
		co.setName(name+"_1");
		ec.saveChanges();

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)ec.globalIDForObject(co));

		int pk = ERXTestUtilities.pkOne(ec, co);

		Assert.assertEquals("_EOIntegralKeyGlobalID["+Company.ENTITY_NAME+" (java.lang.Integer)"+pk+"]", xkgid.toString());

		ec.revert();
		//ERXTestUtilities.deleteObjectsWithPrefix(ec, Company.ENTITY_NAME, name);
	}
}
