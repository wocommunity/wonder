/**
 Copyright (c) 2001-2006, CodeFab, Inc. and individual contributors
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the CodeFab, Inc. nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package er.quartzscheduler.util;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOCustomObject;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;


/**
 * <code>MockEditingContext</code> is a subclass of <code>EOEditingContext</code>
 * that can be used for fast in-memory testing of business objects.
 * <p>
 * Unit tests for logic which use fetch specifications or save to an editing context
 * can be relative slow because full roundtrips to a database are being performed.
 * Testing the same logic with a MockEditingContext ensures that nothing is being
 * saved to or read from a database resulting in shorter execution time of the unit test.
 * Also, you don't risk invalidating the persistent data with broken test cases.
 * <p>
 * Assuming there is an <code>EOCustomObject</code> called <code>Person</code> with the
 * attributes <code>name</code>, <code>age</code>, <code>nationality</code> and
 * <code>partner</code>. <code>nationality</code> is of entity <code>Country</code>
 * for which there is a constant list of objects in the database. We want to write a test for the 
 * static Person method <code>createFromDescription(String, EOEditingContext)</code>.
 * <pre>
 * import org.junit.*;
 * import static org.junit.Assert.*;
 * import org.wounittest.*;
 * import static org.wounittest.WOAssert.*;

 *
 * public class PersonTest extends EOFTest {
 *     Person jane;
 *
 *     &#64;Before
 *     public void setUp() throws Exception {
 *         super.setUp();
 *         mockEditingContext().setEntityNamesToGetFromDatabase(new NSArray("Country"));
 *         jane = (Person)mockEditingContext().createSavedObject("Person");
 *         jane.setName("Jane Doe");
 *         jane.setNationalityRelationship(Country.withName("Canada", mockEditingContext()));
 *     }
 *
 *     &#64;Test
 *     public void creationFromDescriptionWithExistingPartner() {
 *         Person newPerson = Person.createFromDescription("John Doe, 34, Canada, Jane Doe",
 *                                                         mockEditingContext());
 *         assertEquals("John Doe", newPerson.name());
 *         assertEquals(34, newPerson.age());
 *         assertSame(Country.withName("Canada", mockEditingContext()), newPerson.nationality());
 *         assertSame(jane, newPerson.partner());
 *     }
 *
 *     &#64;Test
 *     public void creationFromDescriptionWithUnknownPartner() {
 *         Person newPerson = Person.createFromDescription("Bla Fasel, 12, Germany, Jeniffer Doe",
 *                                                         mockEditingContext());
 *         assertEquals("Blah Fasel", newPerson.name());
 *         assertEquals(12, newPerson.age());
 *         assertSame(Country.withName("Germany", mockEditingContext()), newPerson.nationality());
 *         assertNull(newPerson.partner());
 *     }
 *
 *     &#64;Test(expected = UnknownCountryException.class)
 *     public void creationFromDescriptionWithUnknownCountry() {
 *         Person.createFromDescription("Bla Fasel, 12, Wawaland, Jane Doe",
 *                                      mockEditingContext());
 *     }
 *
 * }
 * </pre>
 * <p>
 * Additional information can be found in
 * <a href="http://wounittest.cvs.sourceforge.net/wounittest/WOUnitTest2/test/org/wounittest/MockEditingContextTest.java?view=markup">WOUTMockEditingContextTest.java</a>.
 */

public class MockEditingContext extends EOEditingContext {
    protected static int fakePrimaryKeyCounter = 1;
    protected NSMutableArray ignoredObjects = new NSMutableArray();
    protected NSArray entityNamesToGetFromDatabase = new NSArray();

    /**
     * Constructs a MockEditingContext. Using a <code>MockObjectStore</code> as parent object store.
     */
    public MockEditingContext() {
        _initWithParentObjectStore(new MockObjectStore());
    }

    /**
     * Defines which entities should be fetched from the rootObjectStore.
     * This can be useful for tests which depend on some data being present in the database.
     * @param  theEntityNamesToGetFromDatabase  array of entity names which should be fetched from the database
     */
    public void setEntityNamesToGetFromDatabase(final NSArray theEntityNamesToGetFromDatabase) {
        entityNamesToGetFromDatabase = theEntityNamesToGetFromDatabase;
    }

    /**
     * Overwritten to return the <code>defaultParentObjectStore</code>.
     * @see    com.webobjects.eocontrol.EOEditingContext EOEditingContext.defaultParentObjectStore() 
     */
    @Override
    public EOObjectStore rootObjectStore() {
        return EOEditingContext.defaultParentObjectStore();
    }

    /**
     * Overrides the implementation inherited from EOEditingContext to fetch objects from the array of <code>registeredObjects</code> of the receiver instead of going to the database.
     * Only entities defined with <code>setEntityNamesToGetFromDatabase</code> are still being fetched from the database using the <code>rootObjectStore</code>.
     * Throws <code>UnsupportedOperationException</code> if <code>aFetchSpecification</code> is configured to return raw rows.
     * Hints are ignored.
     * @param  aFetchSpecification  the criteria specified for fetch
     * @param  anEditingContext  the destination EOEditingContext, needs to be the same as the receiver
     */
//    @Override
//    public NSArray objectsWithFetchSpecification(final EOFetchSpecification aFetchSpecification, final EOEditingContext anEditingContext) {
//        if (entityNamesToGetFromDatabase.containsObject(aFetchSpecification.entityName()))
//            return rootObjectStore().objectsWithFetchSpecification(aFetchSpecification, anEditingContext);
//        if (anEditingContext != this)
//            throw new IllegalArgumentException("MockEditingContext doesn't support other editing contexts");
//        return EOFetcher.objectsWithFetchSpecification(anEditingContext, aFetchSpecification, EOFetcher.EC);
//    }

    /**
     * Convenience cover method for <code>insertSavedObject</code>.
     * Creates a new Custom Object for the specified entity, inserts it into the receiver using <code>insertSavedObject</code>, and returns the new object.
     * @param  anEntityName  the name of entity
     */
    public EOCustomObject createSavedObject(final String anEntityName) {
        EOEntity entity = EOUtilities.entityNamed(this, anEntityName);
        EOEnterpriseObject object = entity.classDescriptionForInstances().createInstanceWithEditingContext(this, null);
        if (!(object instanceof EOCustomObject))
            throw new IllegalArgumentException("The entity is not an EOCustomObject and can't be used with createSavedObject().");
        insertSavedObject((EOCustomObject)object);
        return (EOCustomObject)object;
    }

    /**
     * Inserts a Custom Object into the receiver and makes it look as if it was fetched from the database.
     * The object will get a non-temporary global id.
     * The receiver will not observe the object as defined in EOObserving, which means after changing the object, the receiver will not validate or save it.
     * Doesn't work with EOCustomObject subclasses that have a compound primary key.
     * Note that awakeFromInsertion() will be called on the object because that method often contains useful initialization, and awakeFromFetch() will NOT be called because it might depend on data that we don't care to set up.
     * @param  anObject  the Custom Object
     */
    public void insertSavedObject(final EOCustomObject anObject) {
        recordObject(anObject, assignFakeGlobalIDToObject(anObject));
        anObject.awakeFromInsertion(this);
        ignoredObjects.addObject(anObject);
    }

    /**
     * Internal helper method for <code>insertSavedObject</code>.
     */
    protected EOGlobalID assignFakeGlobalIDToObject(final EOCustomObject anObject) {
        EOEntity entity = EOUtilities.entityNamed(this, anObject.entityName());
        NSArray primaryKeyAttributes = entity.primaryKeyAttributes();
        if (primaryKeyAttributes.count() != 1)
            throw new IllegalArgumentException(entity.name() + " has a compound primary key and can't be used with insertSavedObject().");
        NSDictionary primaryKeyDictionary = new NSDictionary(fakePrimaryKeyCounter++, ((EOAttribute)primaryKeyAttributes.objectAtIndex(0)).name());
        EOGlobalID globalID = entity.globalIDForRow(primaryKeyDictionary);
        anObject.__setGlobalID(globalID);
        return globalID;
    }

    /**
     * Overrides the implementation inherited from EOEditingContext to ignore objects registered with <code>insertSavedObject</code>.
     * @param  anObject  the object whose state is to be recorded
     */
    @Override
    public void objectWillChange(final Object anObject) {
        if (!ignoredObjects.containsObject(anObject))
            super.objectWillChange(anObject);
    }

    /**
     * Extends the implementation inherited from EOEditingContext to delete ignoredObjects.
     */
    @Override
    public void dispose() {
        ignoredObjects.removeAllObjects();
        ignoredObjects = null;
        super.dispose();
    }

}

class MockObjectStore extends EOObjectStore {

    @Override
    public EOEnterpriseObject faultForGlobalID(final EOGlobalID aGlobalid, final EOEditingContext anEditingcontext) {
        return null;
    }

    @Override
    public EOEnterpriseObject faultForRawRow(final NSDictionary aRow, final String anEntityName, final EOEditingContext anEditingContext) {
        return null;
    }

    @Override
    public NSArray arrayFaultWithSourceGlobalID(final EOGlobalID aGlobalId, final String aRelationshipName, final EOEditingContext anEditingContext) {
        return NSArray.EmptyArray;
    }

    @Override
    public NSArray objectsForSourceGlobalID(final EOGlobalID aGlobalId, final String aRelationshipName, final EOEditingContext anEditingContext) {
        return NSArray.EmptyArray;
    }

    @Override
    public NSArray objectsWithFetchSpecification(final EOFetchSpecification aFetchSpecification, final EOEditingContext anEditingContext) {
        return NSArray.EmptyArray;
    }

    @Override
    public boolean isObjectLockedWithGlobalID(final EOGlobalID aGlobalId, final EOEditingContext anEditingContext) {
        return false;
    }

    @Override
    public void initializeObject(final EOEnterpriseObject anEnterpriseObject, final EOGlobalID aGlobalId, final EOEditingContext anEditingContext) {
        if (((MockEditingContext)anEditingContext).entityNamesToGetFromDatabase.containsObject(anEnterpriseObject.entityName())) {
            EOObjectStore rootObjectStore = anEditingContext.rootObjectStore();
            rootObjectStore.lock();
            try {
                rootObjectStore.initializeObject(anEnterpriseObject, aGlobalId, anEditingContext);
            } finally {
                rootObjectStore.unlock();
            }
        }
    }

    @Override
    public void lockObjectWithGlobalID(final EOGlobalID aGlobalId, final EOEditingContext anEditingContext) {
    }

    @Override
    public void lock() {
    }

    @Override
    public void unlock() {
    }

    @Override
    public void refaultObject(final EOEnterpriseObject anEnterpriseObject, final EOGlobalID aGlobalId, final EOEditingContext anEditingContext) {
    }

    @Override
    public void invalidateObjectsWithGlobalIDs(final NSArray globalIds) {
    }

    @Override
    public void invalidateAllObjects() {
    }

    @Override
    public void saveChangesInEditingContext(final EOEditingContext anEditingContext) {
    }

    @Override
    public void editingContextDidForgetObjectWithGlobalID(final EOEditingContext anEditingContext, final EOGlobalID aGlobalId) {
    }

}
