package er.extensions.eof;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFaultHandler;
import com.webobjects.eocontrol.EOFaulting;
import com.webobjects.eocontrol.EOGenericRecord;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSArray;

import er.erxtest.ERXTestCase;
import er.erxtest.ERXTestUtilities;
import er.erxtest.model.Company;
import er.erxtest.model.Employee;

public class ERXEOControlUtilitiesTest extends ERXTestCase {

	@Override
	@Before
	public void setUp() throws Exception {
		ERXTestUtilities.deleteAllObjects();
	}

	@Test
	public void testCreateAndInsertObjectEOEditingContextClassOfT() {
		EOEditingContext ec = ERXEC.newEditingContext();
		Company co = ERXEOControlUtilities.createAndInsertObject(ec, Company.class);
		//Only testing for nullness since the above should throw a class cast ex if
		//co is not a company.
		Assert.assertNotNull(co);
		Assert.assertTrue(ec.insertedObjects().contains(co));
	}

	public void testRootEntity() {
		EOEditingContext ec = ERXEC.newEditingContext();

		// using vertical inheritance
		EOGenericRecord eo1 = (EOGenericRecord)EOUtilities.createAndInsertInstance(ec, "EmployeeVI");
		assertEquals("Person", ERXEOControlUtilities.rootEntityName(eo1));
		assertEquals(EOModelGroup.defaultGroup().entityNamed("Person"), ERXEOControlUtilities.rootEntity(eo1));

		// using horizontal inheritance
		EOGenericRecord eo2 = (EOGenericRecord)EOUtilities.createAndInsertInstance(ec, "EmployeeHI");
		assertEquals("Person", ERXEOControlUtilities.rootEntityName(eo2));
		assertEquals(EOModelGroup.defaultGroup().entityNamed("Person"), ERXEOControlUtilities.rootEntity(eo2));
	}

	/**
	 * Test case where
	 * <li>the object is a new unsaved object.
	 */
	public void testObjectCountForToManyRelationship_NewEO() {
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {
			Company c = (Company) EOUtilities.createAndInsertInstance(ec, Company.ENTITY_NAME);

			// No employees
			Integer count = ERXEOControlUtilities.objectCountForToManyRelationship(c, Company.EMPLOYEES_KEY);
			assertEquals(0, count.intValue());
			
			@SuppressWarnings("unused")
			Employee e1 = c.createEmployeesRelationship();
			@SuppressWarnings("unused")
			Employee e2 = c.createEmployeesRelationship();
			@SuppressWarnings("unused")
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
		
		EOEditingContext ec = ERXEC.newEditingContext();
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
		// Make no assumptions about state of stack or if OSC pool active
		EOObjectStoreCoordinator osc = new EOObjectStoreCoordinator();
		// Setup
		EOGlobalID cGid = ERXTestUtilities.createCompanyAnd3Employees();
		EOGlobalID cGid0 = ERXTestUtilities.createCompanyAndNoEmployees();
		
		EOEditingContext ec1 = ERXEC.newEditingContext(osc);
		EOEditingContext ec2 = ERXEC.newEditingContext(osc);
		
		// Fetch again into this ec to ensure snapshots are in the database
		Company c;
		// Expect relationship to be a fault
		Object relationshipValue;
		ec1.lock();
		try {
			// Fetch Company with cGid
			c = (Company) ec1.faultForGlobalID(cGid, ec1);
			// c will be a fault
			assertTrue(EOFaultHandler.isFault(c));
			
			relationshipValue = c.storedValueForKey(Company.EMPLOYEES_KEY);
			
			// c will no longer be a fault
			assertFalse(EOFaultHandler.isFault(c));
			
			// relationshipValue will be a fault
			assertTrue(EOFaultHandler.isFault(relationshipValue));
			// Fire the fault to fetch and to ensure snapshot is registered in eodb
			ERXTestUtilities.fireFault(relationshipValue);

			// -----------------------------------------
			// Fetch Company with cGid0
			c = (Company) ec1.faultForGlobalID(cGid0, ec1);
			relationshipValue = c.storedValueForKey(Company.EMPLOYEES_KEY);
			assertTrue(EOFaultHandler.isFault(relationshipValue));
			// Fire the fault to fetch and to ensure snapshot is registered in eodb
			ERXTestUtilities.fireFault(relationshipValue);

		} finally {
			ec1.unlock();
		}
		
		
		// Fetch
		ec2.lock();
		try {
			// Fetch Company with cGid
			c = (Company) ec2.faultForGlobalID(cGid, ec2);
			// Expect relationship to be a fault
			relationshipValue = c.storedValueForKey(Company.EMPLOYEES_KEY);
			assertTrue(EOFaultHandler.isFault(relationshipValue));
			
			// There will be a snapshot in the db
			NSArray toManySnapshot = ERXTestUtilities.snapshotArrayForRelationshipInObject(c, Company.EMPLOYEES_KEY);
			assertNotNull(toManySnapshot);
			
			// Count will come from snapshot cache
			Integer count = ERXEOControlUtilities.objectCountForToManyRelationship(c, Company.EMPLOYEES_KEY);
			assertEquals(3, count.intValue());
			
			// -----------------------------------------
			// Fetch Company with cGid0
			c = (Company) ec2.faultForGlobalID(cGid0, ec2);
			// Expect relationship to be a fault
			relationshipValue = c.storedValueForKey(Company.EMPLOYEES_KEY);
			assertTrue(EOFaultHandler.isFault(relationshipValue));

			// There will be a snapshot in the db
			toManySnapshot = ERXTestUtilities.snapshotArrayForRelationshipInObject(c, Company.EMPLOYEES_KEY);
			assertNotNull(toManySnapshot);
			
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
			
			// There will be no snapshot in the db
			NSArray toManySnapshot = ERXTestUtilities.snapshotArrayForRelationshipInObject(c, Company.EMPLOYEES_KEY);
			assertNull(toManySnapshot);
			
			// This will perform SQL count against the database
			Integer count = ERXEOControlUtilities.objectCountForToManyRelationship(c, Company.EMPLOYEES_KEY);
			assertEquals(3, count.intValue());

		} finally {
			ec1.unlock();
		}
	}

	/**
	 * Test obvious invalid arguments.
	 */
	public void testObjectCountForToManyRelationship_BadArgumentsFailure() {
		// Setup
		EOGlobalID cGid = ERXTestUtilities.createCompanyAnd3Employees();

		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {

			// Test using null eo
			Integer count;

			try {
				// Company is null
				count = ERXEOControlUtilities.objectCountForToManyRelationship(null, Employee.COMPANY_KEY);
			} catch (NullPointerException exception) {
				assertEquals("object argument cannot be null", exception.getMessage());
			}

			// Test using a key that is not a toMany on a new object
			Employee e = (Employee) EOUtilities.createAndInsertInstance(ec, Employee.ENTITY_NAME);

			try {
				// key is null
				count = ERXEOControlUtilities.objectCountForToManyRelationship(e, null);
			} catch (NullPointerException exception) {
				assertEquals("relationshipName argument cannot be null", exception.getMessage());
			}

			try {
				// Company is null
				count = ERXEOControlUtilities.objectCountForToManyRelationship(e, Employee.COMPANY_KEY);
			} catch (IllegalArgumentException exception) {
				assertEquals(
						"The attribute named 'company' in the entity named 'Employee' is not a toMany relationship! Expected an NSArray, but got null.",
						exception.getMessage());
			}

			Company c = (Company) EOUtilities.createAndInsertInstance(ec, Company.ENTITY_NAME);
			e.setCompanyRelationship(c);

			try {
				// Company is not null
				count = ERXEOControlUtilities.objectCountForToManyRelationship(e, Employee.COMPANY_KEY);
			} catch (IllegalArgumentException exception) {
				assertEquals(
						"The attribute named 'company' in the entity named 'Employee' is not a toMany relationship! Expected an NSArray, but got a er.erxtest.model.Company",
						exception.getMessage());
			}

			// ----------------------------------------------------------
			// Test using a key that is not a toMany on an existing object

			c = (Company) ec.faultForGlobalID(cGid, ec);

			try {
				// address1 attribute is null
				count = ERXEOControlUtilities.objectCountForToManyRelationship(c, Company.ADDRESS1_KEY);
			} catch (IllegalArgumentException exception) {
				assertEquals(
						"The attribute named 'address1' in the entity named 'Company' is not a toMany relationship! Expected an NSArray, but got null.",
						exception.getMessage());
			}

			e = c.employees().objectAtIndex(0);

			EOGlobalID eGid = ec.globalIDForObject(e);
			try {
				// Company is not null
				count = ERXEOControlUtilities.objectCountForToManyRelationship(e, Employee.COMPANY_KEY);
			} catch (IllegalArgumentException exception) {
				assertEquals(
						"The attribute named 'company' in the entity named 'Employee' is not a toMany relationship! Expected an NSArray, but got a er.erxtest.model.Company",
						exception.getMessage());
			}

			try {
				// Department to-one is null
				count = ERXEOControlUtilities.objectCountForToManyRelationship(e, Employee.DEPARTMENT_KEY);
			} catch (IllegalArgumentException exception) {
				assertEquals(
						"The attribute named 'department' in the entity named 'Employee' is not a toMany relationship! Expected an NSArray, but got null.",
						exception.getMessage());
			}

			// Test failure where key is to-one fault
			ec.revert();
			ec.invalidateAllObjects();

			e = (Employee) ec.faultForGlobalID(eGid, ec);
			assertTrue(EOFaultHandler.isFault(e.storedValueForKey(Employee.COMPANY_KEY)));
			try {
				// Company is a to-one fault
				count = ERXEOControlUtilities.objectCountForToManyRelationship(e, Employee.COMPANY_KEY);
			} catch (IllegalArgumentException exception) {
				assertEquals(
						"The attribute named 'company' in the entity named 'Employee' is not a toMany relationship!",
						exception.getMessage());
			}

		} finally {
			ec.unlock();
		}
	}


}

