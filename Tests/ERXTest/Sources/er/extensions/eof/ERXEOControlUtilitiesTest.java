package er.extensions.eof;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFaultHandler;
import com.webobjects.eocontrol.EOFaulting;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol._EOCheapCopyMutableArray;
import com.webobjects.foundation.NSArray;

import er.erxtest.ERXTestCase;
import er.erxtest.ERXTestUtilities;
import er.erxtest.model.Company;
import er.erxtest.model.Employee;
import er.extensions.jdbc.ERXJDBCConnectionAnalyzer;

public class ERXEOControlUtilitiesTest extends ERXTestCase {

	private EOEditingContext ec;
	
	@Before
	public void setUp() throws Exception {
		ERXTestUtilities.deleteAllObjects();
		ec = ERXEC.newEditingContext();
	}

	@Test
	public void testCreateAndInsertObjectEOEditingContextClassOfT() {
		Company co = ERXEOControlUtilities.createAndInsertObject(ec, Company.class);
		//Only testing for nullness since the above should throw a class cast ex if
		//co is not a company.
		Assert.assertNotNull(co);
		Assert.assertTrue(ec.insertedObjects().contains(co));
	}
	
	/**
	 * Test case where the object is a new unsaved object.
	 */
	public void testObjectCountForToManyRelationship_NewEO() {
		ec.lock();
		try {
			Company c = ERXEOControlUtilities.createAndInsertObject(ec, Company.class);
			
			// No employees
			Integer count = ERXEOControlUtilities.objectCountForToManyRelationship(c, Company.EMPLOYEES_KEY);
			assertEquals(0, count.intValue());
			
			Employee e1 = c.createEmployeesRelationship();
			Employee e2 = c.createEmployeesRelationship();
			Employee e3 = c.createEmployeesRelationship();
			// Expecting a new object for this call
			assertTrue(ERXEOControlUtilities.isNewObject(c));
			count = ERXEOControlUtilities.objectCountForToManyRelationship(c, Company.EMPLOYEES_KEY);
			assertEquals(3, count.intValue());
		} finally {
			ec.unlock();
		}
	}
	
	/**
	 * Test case where
	 * <li> the relationship is not a fault.
	 */
	public void testObjectCountForToManyRelationship_NoFault() {
		EOGlobalID cGid = ERXTestUtilities.createCompanyAnd3Employees();

		ec.lock();
		try {
			Company c = (Company) ec.faultForGlobalID(cGid, ec);
			// Add a new item to employees relationship
			@SuppressWarnings("unused")
			Employee e4 = c.createEmployeesRelationship();
			// I expect relationship to NOT be a fault
			Object relationshipValue = c.storedValueForKey(Company.EMPLOYEES_KEY);
			assertFalse(EOFaultHandler.isFault(relationshipValue));
			Integer count = ERXEOControlUtilities.objectCountForToManyRelationship(c, Company.EMPLOYEES_KEY);
			assertEquals(4, count.intValue());
		} finally {
			ec.unlock();
		}
		
		cGid = ERXTestUtilities.createCompanyAndNoEmployees();
		ec.lock();
		try {
			Company c = (Company) ec.faultForGlobalID(cGid, ec);
			// Fire the fault
			Object relationshipValue = c.storedValueForKey(Company.EMPLOYEES_KEY);
			assertTrue(EOFaultHandler.isFault(relationshipValue));
			EOFaulting fault = (EOFaulting) relationshipValue;
			// Fire the fault
			fault.faultHandler().completeInitializationOfObject(fault);
			
			// Now I expect relationship to NOT be a fault
			assertFalse(EOFaultHandler.isFault(relationshipValue));
			Integer count = ERXEOControlUtilities.objectCountForToManyRelationship(c, Company.EMPLOYEES_KEY);
			assertEquals(0, count.intValue());
		} finally {
			ec.unlock();
		}
	}
	
	/**
	 * Test case where
	 * <li> the relationship is a fault.
	 * <li> pre-existing fresh-enough snapshot exists in the EOF stack
	 */
	public void testObjectCountForToManyRelationship_Snapshot() {

		// Setup
		EOGlobalID cGid = ERXTestUtilities.createCompanyAnd3Employees();
		EOGlobalID cGid0 = ERXTestUtilities.createCompanyAndNoEmployees();
		
		EOEditingContext ec1 = ERXEC.newEditingContext();
		EOEditingContext ec2 = ERXEC.newEditingContext();
		
		// Fetch again into this ec to ensure snapshots are in the database
		Company c;
		// Expect relationship to be a fault
		Object relationshipValue;
		ec1.lock();
		try {
			c = (Company) ec.faultForGlobalID(cGid, ec1);
			relationshipValue = c.storedValueForKey(Company.EMPLOYEES_KEY);
			assertTrue(EOFaultHandler.isFault(relationshipValue));
			// Fire the fault to ensure snapshot is now in dbc
			@SuppressWarnings("unused")
			int size = ((NSArray) relationshipValue).count();
			assertEquals(3, size);
			
			c = (Company) ec.faultForGlobalID(cGid0, ec1);
			relationshipValue = c.storedValueForKey(Company.EMPLOYEES_KEY);
			assertTrue(EOFaultHandler.isFault(relationshipValue));
			// Fire the fault to ensure snapshot is now in dbc
			size = ((NSArray) relationshipValue).count();
			assertEquals(0, size);
			
			
		} finally {
			ec1.unlock();
		}
		
		
		// Fetch
		ec2.lock();
		try {
			c = (Company) ec2.faultForGlobalID(cGid, ec2);
			// Expect relationship to be a fault
			relationshipValue = c.storedValueForKey(Company.EMPLOYEES_KEY);
			assertTrue(EOFaultHandler.isFault(relationshipValue));
			
			// Count will come from snapshot cache
			Integer count = ERXEOControlUtilities.objectCountForToManyRelationship(c, Company.EMPLOYEES_KEY);
			assertEquals(3, count.intValue());
			
			c = (Company) ec2.faultForGlobalID(cGid0, ec2);
			// Expect relationship to be a fault
			relationshipValue = c.storedValueForKey(Company.EMPLOYEES_KEY);
			assertTrue(EOFaultHandler.isFault(relationshipValue));
			
			// Count will come from snapshot cache
			count = ERXEOControlUtilities.objectCountForToManyRelationship(c, Company.EMPLOYEES_KEY);
			assertEquals(0, count.intValue());

		} finally {
			ec2.unlock();
		}

	}
	
	/**
	 * Test case where
	 * <li> The relationship is a fault.
	 * <li> A snapshot array does not exist.
	 * <li> The count is done in the database
	 */
	public void testObjectCountForToManyRelationship_DatabaseCount() {
		// Setup
		EOGlobalID cGid = ERXTestUtilities.createCompanyAnd3Employees();

		// Virgin OSC with no snapshots
		EOObjectStoreCoordinator osc = new EOObjectStoreCoordinator();
		EOEditingContext ec1 = ERXEC.newEditingContext(osc);
		ec1.lock();
		try {
			Company c = (Company) ec1.faultForGlobalID(cGid, ec1);
			// We expect relationship to be a fault
			Object relationshipValue = c.storedValueForKey(Company.EMPLOYEES_KEY);
			assertTrue(EOFaultHandler.isFault(relationshipValue));
			// This will perform SQL count against the database
			Integer count = ERXEOControlUtilities.objectCountForToManyRelationship(c, Company.EMPLOYEES_KEY);
			assertEquals(3, count.intValue());

		} finally {
			ec1.unlock();
		}
	}


}