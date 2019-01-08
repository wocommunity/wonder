
package er.extensions.eof;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;

import er.erxtest.ERXTestCase;
import er.erxtest.ERXTestUtilities;
import er.erxtest.model.Company;
import er.erxtest.model.Employee;

public class ERXECTest extends ERXTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testConstructor() {
        Assert.assertNotNull(new ERXEC());
    }

    public void testConstructorWithObjectStore() {

        EOEditingContext parentEC1 = new EOEditingContext();
        EOEditingContext parentEC2 = new EOEditingContext();

        ERXEC ec1 = new ERXEC(parentEC1);
        Assert.assertNotNull(ec1);

        ERXEC ec2 = new ERXEC(parentEC2);
        Assert.assertNotNull(ec2);

        Assert.assertEquals(parentEC1, ec1.parentObjectStore());
        Assert.assertEquals(parentEC2, ec2.parentObjectStore());

        ERXEC parentEC3 = new ERXEC();

        ERXEC ec3 = new ERXEC(parentEC3);
        Assert.assertNotNull(ec3);

        Assert.assertEquals(parentEC3, ec3.parentObjectStore());
    }
    
    public void testNestedECs() {
    	try {
	    	EOEditingContext ec = ERXEC.newEditingContext();
    		Company c = (Company) EOUtilities.createAndInsertInstance(ec, Company.ENTITY_NAME);
    		c.setName("Name");
    		ec.saveChanges();
    		EOEditingContext nested = ERXEC.newEditingContext(ec);
    		Company nestC = c.localInstanceIn(nested);
    		Employee e = (Employee) EOUtilities.createAndInsertInstance(nested, Employee.ENTITY_NAME);
    		e.setFirstName("First");
    		e.setLastName("Last");
    		e.setManager(Boolean.FALSE);
    		e.addObjectToBothSidesOfRelationshipWithKey(nestC, Employee.COMPANY_KEY);
    		nested.saveChanges();
    		ec.saveChanges();
	    	System.gc();
    		c.delete();
    		ec.saveChanges();
    	} catch (Exception e) {
    		e.printStackTrace();
    		Assert.fail(e.getMessage());
    	}
    }
    
    public void testSerializablilty() throws IOException, ClassNotFoundException {
		EOEditingContext ec = ERXEC.newEditingContext();
		Company.createCompany(ec, "Some fruit company");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		NSData data = null;

		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(ec);
			oos.flush();
			byte[] bytes = baos.toByteArray();
			data = new NSData(bytes);
		} finally {
			if (oos != null) {
				oos.close();
			}
		}
		
		Object object = null;
		byte[] bytes = data.bytes();
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
			object = ois.readObject();
		} finally {
			if (ois != null) {
				ois.close();
			}
		}

		EOEditingContext ec2 = (EOEditingContext) object;
		
		Assert.assertNotSame(ec, ec2);
		ec.dispose();
		ec2.saveChanges();
    }

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#finalize()}.
	 */
	public void _testFinalize() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#invalidateAllObjects()}.
	 */
	public void _testInvalidateAllObjects() {

		Company toBeDeleted = null;
		Company toBeModified = null;

		ERXEC ec = new ERXEC();
		
		NSArray<Company> rows = EOUtilities.objectsForEntityNamed(ec, Company.ENTITY_NAME);

		String name = ERXTestUtilities.randomName("ERXEC_IAO");
		
		if (rows.count() < 1) {
			((Company)EOUtilities.createAndInsertInstance(ec, Company.ENTITY_NAME)).setName(name+"_1");
			ec.saveChanges();
			rows = EOUtilities.objectsForEntityNamed(ec, Company.ENTITY_NAME);
		}
		if (rows.count() < 2) {
			((Company)EOUtilities.createAndInsertInstance(ec, Company.ENTITY_NAME)).setName(name+"_2");
			ec.saveChanges();
			rows = EOUtilities.objectsForEntityNamed(ec, Company.ENTITY_NAME);
		}

		toBeDeleted = rows.get(0);
		toBeModified = rows.get(1);

		String deletedName = toBeDeleted.name();
		String preModifiedName = toBeModified.name();
		
		// Getting ready to modify the "toBeModified"...
		//
		int preModifiedNameCount = EOUtilities.objectsMatchingKeyAndValue(ec, Company.ENTITY_NAME, "name", preModifiedName).count();
		toBeModified.setName(name+"_3");
		
		// Now to add an object...
		//
		((Company)EOUtilities.createAndInsertInstance(ec, Company.ENTITY_NAME)).setName(name+"_4");

		// And now to delete...
		//
		ec.deleteObject(toBeDeleted);

		// invalidating...
		//
		ec.invalidateAllObjects();
		rows = EOUtilities.objectsForEntityNamed(ec, Company.ENTITY_NAME);

		// Check that delete will not happen
		//
		Assert.assertEquals(0, ec.deletedObjects().count());

		// Check that insert will not happen
		//
		Assert.assertEquals(0, ec.insertedObjects().count());
		
		// Check that modify will not happen
		//
		Assert.assertEquals(0, EOQualifier.filteredArrayWithQualifier(ec.registeredObjects(), EOQualifier.qualifierWithQualifierFormat("name = "+name+"_3'", null)).count());

		ec.saveChanges();

		// Check that delete did not happen
		//
		Assert.assertEquals(1, EOUtilities.objectsMatchingKeyAndValue(ec, Company.ENTITY_NAME, "name", deletedName).count());

		// Check that insert did not happen
		//
		Assert.assertEquals(0, EOUtilities.objectsMatchingKeyAndValue(ec, Company.ENTITY_NAME, "name", name+"_4").count());
		
		// Check that modify did not happen
		//
		Assert.assertEquals(preModifiedNameCount, EOUtilities.objectsMatchingKeyAndValue(ec, Company.ENTITY_NAME, "name", preModifiedName).count());

		ERXTestUtilities.deleteObjectsWithPrefix(ec, Company.ENTITY_NAME, name);
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#lock()}.
	 */
	public void _testLock() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#unlock()}.
	 */
	public void _testUnlock() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#dispose()}.
	 */
	public void _testDispose() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#reset()}.
	 */
	public void _testReset() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#processRecentChanges()}.
	 */
	public void _testProcessRecentChanges() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#hasChanges()}.
	 */
	public void _testHasChanges() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#saveChanges()}.
	 */
	public void _testSaveChanges() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#refaultAllObjects()}.
	 */
	public void _testRefaultAllObjects() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#revert()}.
	 */
	public void testRevert() {
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#lockObjectStore()}.
	 */
	public void _testLockObjectStore() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#unlockObjectStore()}.
	 */
	public void _testUnlockObjectStore() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#undo()}.
	 */
	public void _testUndo() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#redo()}.
	 */
	public void _testRedo() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#safeLocking()}.
	 */
	public void _testSafeLocking() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#defaultAutomaticLockUnlock()}.
	 */
	public void _testDefaultAutomaticLockUnlock() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#defaultCoalesceAutoLocks()}.
	 */
	public void _testDefaultCoalesceAutoLocks() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#useUnlocker()}.
	 */
	public void _testUseUnlocker() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#setUseUnlocker(boolean)}.
	 */
	public void _testSetUseUnlocker() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#traceOpenLocks()}.
	 */
	public void _testTraceOpenLocks() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#markOpenLocks()}.
	 */
	public void _testMarkOpenLocks() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#setTraceOpenLocks(boolean)}.
	 */
	public void _testSetTraceOpenLocks() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#setMarkOpenLocks(boolean)}.
	 */
	public void _testSetMarkOpenLocks() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#pushLockedContextForCurrentThread(com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testPushLockedContextForCurrentThread() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#popLockedContextForCurrentThread(com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testPopLockedContextForCurrentThread() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#unlockAllContextsForCurrentThread()}.
	 */
	public void _testUnlockAllContextsForCurrentThread() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#_initWithParentObjectStore(com.webobjects.eocontrol.EOObjectStore)}.
	 */
	public void _test_initWithParentObjectStoreEOObjectStore() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#deleteObjects(com.webobjects.foundation.NSArray)}.
	 */
	public void _testDeleteObjects() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#useAutoLock()}.
	 */
	public void _testUseAutoLock() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#setUseAutoLock(boolean)}.
	 */
	public void _testSetUseAutoLock() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#coalesceAutoLocks()}.
	 */
	public void _testCoalesceAutoLocks() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#setCoalesceAutoLocks(boolean)}.
	 */
	public void _testSetCoalesceAutoLocks() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#creationTrace()}.
	 */
	public void _testCreationTrace() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#openLockTraces()}.
	 */
	public void _testOpenLockTraces() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#autoLock(java.lang.String)}.
	 */
	public void _testAutoLock() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#autoUnlock(boolean)}.
	 */
	public void _testAutoUnlock() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#isAutoLocked()}.
	 */
	public void _testIsAutoLocked() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#_checkOpenLockTraces()}.
	 */
	public void _test_checkOpenLockTraces() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#recordObject(com.webobjects.eocontrol.EOEnterpriseObject, com.webobjects.eocontrol.EOGlobalID)}.
	 */
	public void _testRecordObjectEOEnterpriseObjectEOGlobalID() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#forgetObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testForgetObjectEOEnterpriseObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#updatedObjects()}.
	 */
	public void _testUpdatedObjects() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#registeredObjects()}.
	 */
	public void _testRegisteredObjects() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#insertedObjects()}.
	 */
	public void _testInsertedObjects() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#deletedObjects()}.
	 */
	public void _testDeletedObjects() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#setSharedEditingContext(com.webobjects.eocontrol.EOSharedEditingContext)}.
	 */
	public void _testSetSharedEditingContextEOSharedEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#objectForGlobalID(com.webobjects.eocontrol.EOGlobalID)}.
	 */
	public void _testObjectForGlobalIDEOGlobalID() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#globalIDForObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testGlobalIDForObjectEOEnterpriseObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#committedSnapshotForObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testCommittedSnapshotForObjectEOEnterpriseObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#currentEventSnapshotForObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testCurrentEventSnapshotForObjectEOEnterpriseObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#objectWillChange(java.lang.Object)}.
	 */
	public void _testObjectWillChangeObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#insertObjectWithGlobalID(com.webobjects.eocontrol.EOEnterpriseObject, com.webobjects.eocontrol.EOGlobalID)}.
	 */
	public void _testInsertObjectWithGlobalIDEOEnterpriseObjectEOGlobalID() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#insertObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testInsertObjectEOEnterpriseObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#deleteObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testDeleteObjectEOEnterpriseObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#willSaveChanges(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray)}.
	 */
	public void _testWillSaveChanges() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#didSaveChanges(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray)}.
	 */
	public void _testDidSaveChanges() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#saveChangesTolerantly(boolean, boolean)}.
	 */
	public void _testSaveChangesTolerantlyBooleanBoolean() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#saveChangesTolerantly()}.
	 */
	public void _testSaveChangesTolerantly() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#saveChangesTolerantly(boolean)}.
	 */
	public void _testSaveChangesTolerantlyBoolean() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#setOptions(boolean, boolean, boolean)}.
	 */
	public void _testSetOptions() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#_saveChanges()}.
	 */
	public void _test_saveChanges() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#faultForGlobalID(com.webobjects.eocontrol.EOGlobalID, com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testFaultForGlobalIDEOGlobalIDEOEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#arrayFaultWithSourceGlobalID(com.webobjects.eocontrol.EOGlobalID, java.lang.String, com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testArrayFaultWithSourceGlobalIDEOGlobalIDStringEOEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#initializeObject(com.webobjects.eocontrol.EOEnterpriseObject, com.webobjects.eocontrol.EOGlobalID, com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testInitializeObjectEOEnterpriseObjectEOGlobalIDEOEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#editingContextDidForgetObjectWithGlobalID(com.webobjects.eocontrol.EOEditingContext, com.webobjects.eocontrol.EOGlobalID)}.
	 */
	public void _testEditingContextDidForgetObjectWithGlobalIDEOEditingContextEOGlobalID() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#objectsForSourceGlobalID(com.webobjects.eocontrol.EOGlobalID, java.lang.String, com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testObjectsForSourceGlobalIDEOGlobalIDStringEOEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#refaultObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testRefaultObjectEOEnterpriseObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#refaultObject(com.webobjects.eocontrol.EOEnterpriseObject, com.webobjects.eocontrol.EOGlobalID, com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testRefaultObjectEOEnterpriseObjectEOGlobalIDEOEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#objectsWithFetchSpecification(com.webobjects.eocontrol.EOFetchSpecification, com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testObjectsWithFetchSpecificationEOFetchSpecificationEOEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#saveChangesInEditingContext(com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testSaveChangesInEditingContextEOEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#invalidateObjectsWithGlobalIDs(com.webobjects.foundation.NSArray)}.
	 */
	public void _testInvalidateObjectsWithGlobalIDsNSArray() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#lockObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testLockObjectEOEnterpriseObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#saveChanges(java.lang.Object)}.
	 */
	public void _testSaveChangesObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#refreshObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testRefreshObjectEOEnterpriseObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#invokeRemoteMethod(com.webobjects.eocontrol.EOEditingContext, com.webobjects.eocontrol.EOGlobalID, java.lang.String, java.lang.Class[], java.lang.Object[])}.
	 */
	public void _testInvokeRemoteMethodEOEditingContextEOGlobalIDStringClassArrayObjectArray() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#_objectsChangedInStore(com.webobjects.foundation.NSNotification)}.
	 */
	public void _test_objectsChangedInStoreNSNotification() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#_processObjectStoreChanges(com.webobjects.foundation.NSDictionary)}.
	 */
	public void _test_processObjectStoreChangesNSDictionary() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#setDelegate(java.lang.Object)}.
	 */
	public void _testSetDelegateObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#_factory()}.
	 */
	public void _testFactory() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#setFactory(er.extensions.eof.ERXEC.Factory)}.
	 */
	public void _testSetFactory() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#newEditingContext()}.
	 */
	public void _testNewEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#newTolerantEditingContext(com.webobjects.eocontrol.EOObjectStore, boolean, boolean)}.
	 */
	public void _testNewTolerantEditingContextEOObjectStoreBooleanBoolean() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#newTolerantEditingContext()}.
	 */
	public void _testNewTolerantEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#newTolerantEditingContext(com.webobjects.eocontrol.EOObjectStore)}.
	 */
	public void _testNewTolerantEditingContextEOObjectStore() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#saveChangesTolerantly(com.webobjects.eocontrol.EOEditingContext, boolean, boolean)}.
	 */
	public void _testSaveChangesTolerantlyEOEditingContextBooleanBoolean() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#saveChangesTolerantly(com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testSaveChangesTolerantlyEOEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#newEditingContext(com.webobjects.eocontrol.EOObjectStore, boolean)}.
	 */
	public void _testNewEditingContextEOObjectStoreBoolean() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#newEditingContext(boolean)}.
	 */
	public void _testNewEditingContextBoolean() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#newEditingContext(com.webobjects.eocontrol.EOObjectStore)}.
	 */
	public void _testNewEditingContextEOObjectStore() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#registerOpenEditingContextLockSignalHandler()}.
	 */
	public void _testRegisterOpenEditingContextLockSignalHandler() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#registerOpenEditingContextLockSignalHandler(java.lang.String)}.
	 */
	public void _testRegisterOpenEditingContextLockSignalHandlerString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEC#outstandingLockDescription()}.
	 */
	public void _testOutstandingLockDescription() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#faultForGlobalID(com.webobjects.eocontrol.EOGlobalID, com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testFaultForGlobalIDEOGlobalIDEOEditingContext1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#faultForRawRow(com.webobjects.foundation.NSDictionary, java.lang.String, com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testFaultForRawRowNSDictionaryStringEOEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#arrayFaultWithSourceGlobalID(com.webobjects.eocontrol.EOGlobalID, java.lang.String, com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testArrayFaultWithSourceGlobalIDEOGlobalIDStringEOEditingContext1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#initializeObject(com.webobjects.eocontrol.EOEnterpriseObject, com.webobjects.eocontrol.EOGlobalID, com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testInitializeObjectEOEnterpriseObjectEOGlobalIDEOEditingContext1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#editingContextDidForgetObjectWithGlobalID(com.webobjects.eocontrol.EOEditingContext, com.webobjects.eocontrol.EOGlobalID)}.
	 */
	public void _testEditingContextDidForgetObjectWithGlobalIDEOEditingContextEOGlobalID1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#objectsForSourceGlobalID(com.webobjects.eocontrol.EOGlobalID, java.lang.String, com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testObjectsForSourceGlobalIDEOGlobalIDStringEOEditingContext1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#refaultObject(com.webobjects.eocontrol.EOEnterpriseObject, com.webobjects.eocontrol.EOGlobalID, com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testRefaultObjectEOEnterpriseObjectEOGlobalIDEOEditingContext1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#saveChangesInEditingContext(com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testSaveChangesInEditingContextEOEditingContext1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#objectsWithFetchSpecification(com.webobjects.eocontrol.EOFetchSpecification, com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testObjectsWithFetchSpecificationEOFetchSpecificationEOEditingContext1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#isObjectLockedWithGlobalID(com.webobjects.eocontrol.EOGlobalID, com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testIsObjectLockedWithGlobalID() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#lockObjectWithGlobalID(com.webobjects.eocontrol.EOGlobalID, com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testLockObjectWithGlobalID() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#invalidateObjectsWithGlobalIDs(com.webobjects.foundation.NSArray)}.
	 */
	public void _testInvalidateObjectsWithGlobalIDsNSArrayOfEOGlobalID() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#invokeRemoteMethod(com.webobjects.eocontrol.EOEditingContext, com.webobjects.eocontrol.EOGlobalID, java.lang.String, java.lang.Class[], java.lang.Object[])}.
	 */
	public void _testInvokeRemoteMethodEOEditingContextEOGlobalIDStringClassArrayObjectArray1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_invalidateObjectsDuringSave()}.
	 */
	public void _test_invalidateObjectsDuringSave() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_mergeValueForKey(com.webobjects.eocontrol.EOEnterpriseObject, java.lang.Object, java.lang.String)}.
	 */
	public void _test_mergeValueForKey() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#setInstancesRetainRegisteredObjects(boolean)}.
	 */
	public void _testSetInstancesRetainRegisteredObjects() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#instancesRetainRegisteredObjects()}.
	 */
	public void _testInstancesRetainRegisteredObjects() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#setRetainsRegisteredObjects(boolean)}.
	 */
	public void _testSetRetainsRegisteredObjects() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#retainsRegisteredObjects()}.
	 */
	public void _testRetainsRegisteredObjects() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_referenceQueue()}.
	 */
	public void _test_referenceQueue() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#defaultFetchTimestampLag()}.
	 */
	public void _testDefaultFetchTimestampLag() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#setDefaultFetchTimestampLag(long)}.
	 */
	public void _testSetDefaultFetchTimestampLag() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#setUsesContextRelativeEncoding(boolean)}.
	 */
	public void _testSetUsesContextRelativeEncoding() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#usesContextRelativeEncoding()}.
	 */
	public void _testUsesContextRelativeEncoding() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_valuesForObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _test_valuesForObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_applyValuesToObject(com.webobjects.foundation.NSArray, com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _test_applyValuesToObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#encodeObjectWithCoder(com.webobjects.eocontrol.EOEnterpriseObject, com.webobjects.foundation.NSCoder)}.
	 */
	public void _testEncodeObjectWithCoder() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#initObjectWithCoder(com.webobjects.eocontrol.EOEnterpriseObject, com.webobjects.foundation.NSCoder)}.
	 */
	public void _testInitObjectWithCoder() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#encodeWithKeyValueArchiver(com.webobjects.eocontrol.EOKeyValueArchiver)}.
	 */
	public void _testEncodeWithKeyValueArchiver() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#decodeWithKeyValueUnarchiver(com.webobjects.eocontrol.EOKeyValueUnarchiver)}.
	 */
	public void _testDecodeWithKeyValueUnarchiver() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_initWithParentObjectStore(com.webobjects.eocontrol.EOObjectStore)}.
	 */
	public void _test_initWithParentObjectStoreEOObjectStore1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#EOEditingContext(com.webobjects.eocontrol.EOObjectStore)}.
	 */
	public void _testEOEditingContextEOObjectStore() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#EOEditingContext()}.
	 */
	public void _testEOEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#setInvalidatesObjectsWhenFinalized(boolean)}.
	 */
	public void _testSetInvalidatesObjectsWhenFinalized() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#invalidatesObjectsWhenFinalized()}.
	 */
	public void _testInvalidatesObjectsWhenFinalized() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#fetchTimestamp()}.
	 */
	public void _testFetchTimestamp() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#setFetchTimestamp(long)}.
	 */
	public void _testSetFetchTimestamp() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#parentObjectStore()}.
	 */
	public void _testParentObjectStore() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#rootObjectStore()}.
	 */
	public void _testRootObjectStore() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#setUndoManager(com.webobjects.foundation.NSUndoManager)}.
	 */
	public void _testSetUndoManager() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#undoManager()}.
	 */
	public void _testUndoManager() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#setDelegate(java.lang.Object)}.
	 */
	public void _testSetDelegateObject1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#delegate()}.
	 */
	public void _testDelegate() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#addEditor(java.lang.Object)}.
	 */
	public void _testAddEditor() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#removeEditor(java.lang.Object)}.
	 */
	public void _testRemoveEditor() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#editors()}.
	 */
	public void _testEditors() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#setMessageHandler(java.lang.Object)}.
	 */
	public void _testSetMessageHandler() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#messageHandler()}.
	 */
	public void _testMessageHandler() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#recordObject(com.webobjects.eocontrol.EOEnterpriseObject, com.webobjects.eocontrol.EOGlobalID)}.
	 */
	public void _testRecordObjectEOEnterpriseObjectEOGlobalID1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#forgetObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testForgetObjectEOEnterpriseObject1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_undoUpdate(java.lang.Object)}.
	 */
	public void _test_undoUpdate() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_clearChangedThisTransaction(java.lang.Number)}.
	 */
	public void _test_clearChangedThisTransaction() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_processRecentChanges()}.
	 */
	public void _test_processRecentChanges() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_undoManagerCheckpoint(com.webobjects.foundation.NSNotification)}.
	 */
	public void _test_undoManagerCheckpoint() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_noop(java.lang.Object)}.
	 */
	public void _test_noop() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_processGlobalIDChanges(com.webobjects.foundation.NSDictionary)}.
	 */
	public void _test_processGlobalIDChanges() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_globalIDChanged(com.webobjects.foundation.NSNotification)}.
	 */
	public void _test_globalIDChanged() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_processEndOfEventNotification(java.lang.Object)}.
	 */
	public void _test_processEndOfEventNotification() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#updatedObjects()}.
	 */
	public void _testUpdatedObjects1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_globalIDsForRegisteredObjects()}.
	 */
	public void _test_globalIDsForRegisteredObjects() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#registeredObjects()}.
	 */
	public void _testRegisteredObjects1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#isInserted(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testIsInserted() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#isDeleted(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testIsDeleted() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#isUpdated(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testIsUpdated() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#insertedObjects()}.
	 */
	public void _testInsertedObjects1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#deletedObjects()}.
	 */
	public void _testDeletedObjects1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#propagatesDeletesAtEndOfEvent()}.
	 */
	public void _testPropagatesDeletesAtEndOfEvent() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#setPropagatesDeletesAtEndOfEvent(boolean)}.
	 */
	public void _testSetPropagatesDeletesAtEndOfEvent() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#stopsValidationAfterFirstError()}.
	 */
	public void _testStopsValidationAfterFirstError() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#setStopsValidationAfterFirstError(boolean)}.
	 */
	public void _testSetStopsValidationAfterFirstError() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#locksObjectsBeforeFirstModification()}.
	 */
	public void _testLocksObjectsBeforeFirstModification() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#setLocksObjectsBeforeFirstModification(boolean)}.
	 */
	public void _testSetLocksObjectsBeforeFirstModification() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#sharedEditingContext()}.
	 */
	public void _testSharedEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#setSharedEditingContext(com.webobjects.eocontrol.EOSharedEditingContext)}.
	 */
	public void _testSetSharedEditingContextEOSharedEditingContext1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_objectsInitializedInSharedContext(com.webobjects.foundation.NSNotification)}.
	 */
	public void _test_objectsInitializedInSharedContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_processInitializedObjectsInSharedContext(com.webobjects.foundation.NSDictionary)}.
	 */
	public void _test_processInitializedObjectsInSharedContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_defaultSharedEditingContextWasInitialized(com.webobjects.foundation.NSNotification)}.
	 */
	public void _test_defaultSharedEditingContextWasInitialized() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_defaultEditingContextNowInitialized(com.webobjects.foundation.NSDictionary)}.
	 */
	public void _test_defaultEditingContextNowInitialized() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_eoForGID(com.webobjects.eocontrol.EOGlobalID)}.
	 */
	public void _test_eoForGID() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_clearOriginalSnapshotForObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _test_clearOriginalSnapshotForObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_clearOriginalSnapshotAndInitializeRec(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _test_clearOriginalSnapshotAndInitializeRec() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#objectForGlobalID(com.webobjects.eocontrol.EOGlobalID)}.
	 */
	public void _testObjectForGlobalIDEOGlobalID1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_localObjectForGlobalID(com.webobjects.eocontrol.EOGlobalID)}.
	 */
	public void _test_localObjectForGlobalID() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_retainCountForObjectWithGlobalID(com.webobjects.eocontrol.EOGlobalID)}.
	 */
	public void _test_retainCountForObjectWithGlobalID() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#globalIDForObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testGlobalIDForObjectEOEnterpriseObject1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#committedSnapshotForObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testCommittedSnapshotForObjectEOEnterpriseObject1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#currentEventSnapshotForObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testCurrentEventSnapshotForObjectEOEnterpriseObject1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#objectWillChange(java.lang.Object)}.
	 */
	public void _testObjectWillChangeObject1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_insertObjectWithGlobalID(com.webobjects.eocontrol.EOEnterpriseObject, com.webobjects.eocontrol.EOGlobalID)}.
	 */
	public void _test_insertObjectWithGlobalID() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#insertObjectWithGlobalID(com.webobjects.eocontrol.EOEnterpriseObject, com.webobjects.eocontrol.EOGlobalID)}.
	 */
	public void _testInsertObjectWithGlobalIDEOEnterpriseObjectEOGlobalID1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#insertObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testInsertObjectEOEnterpriseObject1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_undoDelete(java.lang.Object)}.
	 */
	public void _test_undoDelete() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#deleteObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testDeleteObjectEOEnterpriseObject1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#editorsHaveChanges()}.
	 */
	public void _testEditorsHaveChanges() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_editorHasChanges(java.lang.Object)}.
	 */
	public void _test_editorHasChanges() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#tryToSaveChanges()}.
	 */
	public void _testTryToSaveChanges() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_objectBasedChangeInfoForGIDInfo(com.webobjects.foundation.NSDictionary)}.
	 */
	public void _test_objectBasedChangeInfoForGIDInfo() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_newUncommittedChangesForObject(com.webobjects.eocontrol.EOEnterpriseObject, com.webobjects.foundation.NSDictionary)}.
	 */
	public void _test_newUncommittedChangesForObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_mergeObjectWithChanges(com.webobjects.eocontrol.EOEnterpriseObject, com.webobjects.foundation.NSArray)}.
	 */
	public void _test_mergeObjectWithChanges() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_processObjectStoreChanges(com.webobjects.foundation.NSDictionary)}.
	 */
	public void _test_processObjectStoreChangesNSDictionary1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_objectsChangedInStore(com.webobjects.foundation.NSNotification)}.
	 */
	public void _test_objectsChangedInStoreNSNotification1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#faultForRawRow(com.webobjects.foundation.NSDictionary, java.lang.String)}.
	 */
	public void _testFaultForRawRowNSDictionaryString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#refaultObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testRefaultObjectEOEnterpriseObject1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_resetAllChanges()}.
	 */
	public void _test_resetAllChanges() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_resetAllChanges(com.webobjects.foundation.NSDictionary)}.
	 */
	public void _test_resetAllChangesNSDictionary() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_invalidatedAllObjectsInStore(com.webobjects.foundation.NSNotification)}.
	 */
	public void _test_invalidatedAllObjectsInStore() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#objectsWithFetchSpecification(com.webobjects.eocontrol.EOFetchSpecification)}.
	 */
	public void _testObjectsWithFetchSpecificationEOFetchSpecification() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#lockObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testLockObjectEOEnterpriseObject1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#tryLock()}.
	 */
	public void _testTryLock() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_processNotificationQueue()}.
	 */
	public void _test_processNotificationQueue() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#saveChanges(java.lang.Object)}.
	 */
	public void _testSaveChangesObject1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#refreshObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testRefreshObjectEOEnterpriseObject1() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#refreshAllObjects()}.
	 */
	public void _testRefreshAllObjects() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#refetch()}.
	 */
	public void _testRefetch() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#refaultObjects()}.
	 */
	public void _testRefaultObjects() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#setSubstitutionEditingContext(com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void _testSetSubstitutionEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#substitutionEditingContext()}.
	 */
	public void _testSubstitutionEditingContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#setDefaultParentObjectStore(com.webobjects.eocontrol.EOObjectStore)}.
	 */
	public void _testSetDefaultParentObjectStore() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#defaultParentObjectStore()}.
	 */
	public void _testDefaultParentObjectStore() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_globalIDsForObjects(com.webobjects.foundation.NSArray)}.
	 */
	public void _test_globalIDsForObjects() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_EOAssertSafeMultiThreadedAccess()}.
	 */
	public void _test_EOAssertSafeMultiThreadedAccess() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_EOAssertSafeMultiThreadedReadAccess(java.lang.String)}.
	 */
	public void _test_EOAssertSafeMultiThreadedReadAccess() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_EOAssertSafeMultiThreadedAccess(java.lang.String)}.
	 */
	public void _test_EOAssertSafeMultiThreadedAccessString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#_willObjectBeForgottenNextPRC(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _test_willObjectBeForgottenNextPRC() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOEditingContext#readResolve()}.
	 */
	public void _testReadResolve() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOObjectStore#EOObjectStore()}.
	 */
	public void _testEOObjectStore() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOObjectStore#_suppressAssertLock()}.
	 */
	public void _test_suppressAssertLock() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOObjectStore#_resetAssertLock()}.
	 */
	public void _test_resetAssertLock() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOObjectStore#_checkAssertLock()}.
	 */
	public void _test_checkAssertLock() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOObjectStore#setUserInfo(java.util.Map)}.
	 */
	public void _testSetUserInfo() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOObjectStore#userInfo()}.
	 */
	public void _testUserInfo() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOObjectStore#userInfoForKey(java.lang.String)}.
	 */
	public void _testUserInfoForKey() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.webobjects.eocontrol.EOObjectStore#setUserInfoForKey(java.lang.Object, java.lang.String)}.
	 */
	public void _testSetUserInfoForKey() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#Object()}.
	 */
	public void _testObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#getClass()}.
	 */
	public void _testGetClass() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#hashCode()}.
	 */
	public void _testHashCode() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)}.
	 */
	public void _testEquals() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#clone()}.
	 */
	public void _testClone() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#toString()}.
	 */
	public void _testToString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#notify()}.
	 */
	public void _testNotify() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#notifyAll()}.
	 */
	public void _testNotifyAll() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#wait(long)}.
	 */
	public void _testWaitLong() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#wait(long, int)}.
	 */
	public void _testWaitLongInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#wait()}.
	 */
	public void _testWait() {
		fail("Not yet implemented");
	}
}
