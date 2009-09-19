
package er.extensions.eof;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import er.extensions.ERExtensionsTest;


/**
 * Test the static methods in the ERXEOAccessUtilities class. This class has an inner
 * class that actually contains the test methods. We can take the available models
 * (or the ones compatible with these tests) and run the tests multiple times. The
 * tests can be parameterized to use different models or different configurations.
 */
public class ERXEOAccessUtilitiesTest extends TestCase {

    EOModel model = null;
    EOEditingContext ec = null;

    public ERXEOAccessUtilitiesTest(String name) { super(name); }

    static String buildRoot; static { buildRoot = System.getProperty("build.root"); }

    public void testAll() {

        TestSuite suite = ERExtensionsTest.suite;

        // Right now, I just have to two models. I could look for *"BusinessModel" models instead.
        //
        // But I have to disable the use of the second model right now. If I run the tests using the
        // Memory model, the tests pass. If I run the tests using the MySQL model, the tests pass.
        // If I run both with both models, different tests using each will fail. There are obviously
        // start-up or object cacheing issues here that need to be worked out. -rrk

        NSArray<String> methods = ERExtensionsTest.testMethodsForClassName("er.extensions.eof.ERXEOAccessUtilitiesTest$Tests");

        for (int idx = 0; idx < methods.count(); idx++) {
            String testName = methods.get(idx);

            suite.addTest(new Tests(testName, "MemoryBusinessModel"));
            //suite.addTest(new Tests(testName, "MySQLBusinessModel"));

            // It would be nice if these were found in TestResources....
            //
            //suite.addTest(new Tests(testName, "PostgresBusinessModel"));
            //suite.addTest(new Tests(testName, "FrontBaseBusinessModel"));
            //suite.addTest(new Tests(testName, "OpenBaseBusinessModel"));
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
            } catch (java.net.MalformedURLException mue) { System.out.println("mue: "+mue); }
            model = EOModelGroup.defaultGroup().modelNamed(modelName);
            ec = new EOEditingContext();
        }

        public void testEntityMatchingString() {
            EOEntity found1 = ERXEOAccessUtilities.entityMatchingString(ec, "SomeCompanyPlace");
            Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(model.entityNamed("Company"), found1));

            EOEntity found2 = ERXEOAccessUtilities.entityMatchingString(ec, "SomeEmployeeThing");
            Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(model.entityNamed("Employee"), found2));

            EOEntity found3 = ERXEOAccessUtilities.entityMatchingString(ec, "CompanyThing");
            Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(model.entityNamed("Company"), found3));

            EOEntity found4 = ERXEOAccessUtilities.entityMatchingString(ec, "ThatCompany");
            Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(model.entityNamed("Company"), found4));

            EOEntity found5 = ERXEOAccessUtilities.entityMatchingString(ec, "Company");
            Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(model.entityNamed("Company"), found5));

            EOEntity found6 = ERXEOAccessUtilities.entityMatchingString(ec, "SomeGarbage");
            Assert.assertNull(this.config(), found6);

            EOEntity found7 = ERXEOAccessUtilities.entityMatchingString(ec, null);
            Assert.assertNull(this.config(), found7);

            EOEntity found8 = ERXEOAccessUtilities.entityMatchingString(null, "SomeThing");
            Assert.assertNull(this.config(), found8);

            EOEntity found9 = ERXEOAccessUtilities.entityMatchingString(null, null);
            Assert.assertNull(this.config(), found9);
        }

        public void testEntityUsingTable() {
            EOEntity found1 = ERXEOAccessUtilities.entityUsingTable(ec, "GarbageName");
            Assert.assertNull(this.config(), found1);
        }

        public void testEntityWithNamedIsShared() {
            Assert.assertFalse(ERXEOAccessUtilities.entityWithNamedIsShared(ec, "Company"));
            Assert.assertFalse(ERXEOAccessUtilities.entityWithNamedIsShared(ec, "Employee"));
            Assert.assertFalse(ERXEOAccessUtilities.entityWithNamedIsShared(null, "Company"));

            try {
                boolean check = ERXEOAccessUtilities.entityWithNamedIsShared(ec, null);
                Assert.fail();
            } catch (java.lang.IllegalStateException ise) { /* Good! */ }
        }

        public void testEntityNamed() {
            EOEntity found1 = ERXEOAccessUtilities.entityNamed(ec, "Company");
            Assert.assertNotNull(this.config(), found1);
            Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(EOModelGroup.defaultGroup().entityNamed("Company").name(), found1.name()));
    
            EOEntity found2 = ERXEOAccessUtilities.entityNamed(ec, "Employee");
            Assert.assertNotNull(this.config(), found2);
            Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(EOModelGroup.defaultGroup().entityNamed("Employee").name(), found2.name()));

            EOEntity found3 = ERXEOAccessUtilities.entityNamed(null, "Company");
            Assert.assertNotNull(this.config(), found3);
            Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(EOModelGroup.defaultGroup().entityNamed("Company").name(), found3.name()));
    
            Assert.assertNull(this.config(), ERXEOAccessUtilities.entityNamed(ec, null));
        }

        public void testModelGroup() {
            EOModelGroup referenceGroup = EOModelGroup.defaultGroup();
    
            EOModelGroup group1 = ERXEOAccessUtilities.modelGroup(ec);
            Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(referenceGroup, group1));
    
            EOModelGroup group2 = ERXEOAccessUtilities.modelGroup(null);
            Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(referenceGroup, group2));
        }

        public void testDestinationEntityForKeyPath() {
            // public static EOEntity destinationEntityForKeyPath(com.webobjects.eoaccess.EOEntity, String);

            EOEntity companyEntity = EOModelGroup.defaultGroup().entityNamed("Company");
            EOEntity employeeEntity = EOModelGroup.defaultGroup().entityNamed("Employee");
    
            EOEntity entity1 = ERXEOAccessUtilities.destinationEntityForKeyPath(companyEntity, "employees");
            Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(employeeEntity, entity1));
    
            EOEntity entity2 = ERXEOAccessUtilities.destinationEntityForKeyPath(employeeEntity, "company");
            Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(companyEntity, entity2));
        }
    
        public void testEntityForEo() {
            // public static EOEntity entityForEo(EOEnterpriseObject);

            EOEntity companyEntity = EOModelGroup.defaultGroup().entityNamed("Company");
            Assert.assertNotNull(this.config(), companyEntity);

            EOEntity employeeEntity = EOModelGroup.defaultGroup().entityNamed("Employee");
            Assert.assertNotNull(this.config(), employeeEntity);

            EOEnterpriseObject eo1 = EOUtilities.createAndInsertInstance(ec, "Company");
            Assert.assertNotNull(this.config(), eo1);

            EOEntity entity1 = ERXEOAccessUtilities.entityForEo(eo1);
            Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(companyEntity, entity1));

            EOEnterpriseObject eo2 = EOUtilities.createAndInsertInstance(ec, "Employee");
            Assert.assertNotNull(this.config(), eo2);

            EOEntity entity2 = ERXEOAccessUtilities.entityForEo(eo2);
            Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(employeeEntity, entity2));
        }
/*
 TODO:
    // What kind of sequence is needed here?
    public static java.lang.Number getNextValFromSequenceNamed(EOEditingContext, String, String);

    public static void evaluateSQLWithEntityNamed(EOEditingContext, String, String);
    public static void evaluateSQLWithEntity(EOEditingContext, EOEntity, String);

    public static String sqlForFetchSpecification(EOEditingContext, com.webobjects.eocontrol.EOFetchSpecification);

    public static NSArray rawRowsForSQLExpression(EOEditingContext, String, EOSQLExpression);
    public static NSArray rawRowsForSQLExpression(EOEditingContext, EOModel, EOSQLExpression, com.webobjects.foundation.NSArray);

    public static EOSQLExpression sqlExpressionForFetchSpecification(EOEditingContext, com.webobjects.eocontrol.EOFetchSpecification, long, long);

    public static int rowCountForFetchSpecification(EOEditingContext, com.webobjects.eocontrol.EOFetchSpecification);

    public static EOAttribute createAggregateAttribute(EOEditingContext, String, String, String);
    public static EOAttribute createAggregateAttribute(EOEditingContext, String, String, String, java.lang.Class, String);
    public static EOAttribute createAggregateAttribute(EOEditingContext, String, String, String, java.lang.Class, String, String);
    public static EOAttribute createAggregateAttribute(EOEditingContext, String, String, String, java.lang.Class, String, String, String);

    public static String createSchemaSQLForEntitiesInModelWithNameAndOptionsForOracle9(NSArray, String, com.webobjects.foundation.NSDictionary);
    public static String createSchemaSQLForEntitiesInModelWithNameAndOptions(NSArray, String, com.webobjects.foundation.NSDictionary);
    public static String createSchemaSQLForEntitiesWithOptions(NSArray, EODatabaseContext, com.webobjects.foundation.NSDictionary);
    public static String createSchemaSQLForEntitiesInModelWithName(NSArray, String);
    public static String createSchemaSQLForEntitiesInDatabaseContext(NSArray, EODatabaseContext, boolean, boolean);

    public static String createIndexSQLForEntitiesForOracle(NSArray);
    public static String createIndexSQLForEntities(NSArray);
    public static String createIndexSQLForEntities(NSArray, com.webobjects.foundation.NSArray);

    // Model with inheritance needed to test this.
    public static boolean entityUsesSeparateTable(EOEntity);

    public static EOAttribute attributeWithColumnNameFromEntity(String, com.webobjects.eoaccess.EOEntity);

    public static boolean isOptimisticLockingFailure(EOGeneralAdaptorException);

    public static NSArray snapshotsForObjectsFromRelationshipNamed(com.webobjects.foundation.NSArray, String);

    public static NSDictionary primaryKeyDictionaryForEntity(EOEditingContext, String);
    public static NSArray primaryKeysForObjects(com.webobjects.foundation.NSArray);
    public static NSArray primaryKeysForNewRows(EOEditingContext, String, int);

    public static EORelationship lastRelationship(com.webobjects.eoaccess.EORelationship);

    public static NSArray attributePathForKeyPath(EOEntity, String);

    public static String sqlWhereClauseStringForKey(EOSQLExpression, String, NSArray);

    public static EODatabaseContext databaseContextForObject(EOEnterpriseObject);
    public static EODatabaseContext databaseContextForEntityNamed(EOObjectStoreCoordinator, String);

    public static boolean closeDatabaseConnections(EOObjectStoreCoordinator);

    public static NSArray classPropertiesNotInParent(EOEntity, boolean, boolean, boolean);

    public static NSArray externalNamesForEntity(EOEntity, boolean);
    public static NSArray externalNamesForEntityNamed(String, boolean);

    public static EOEntity rootEntityForEntity(com.webobjects.eoaccess.EOEntity);
    public static EOEntity rootEntityForEntityNamed(String);

    public static void logExpression(EOAdaptorChannel, EOSQLExpression, long);
    public static String createLogString(EOAdaptorChannel, EOSQLExpression, long);

    public static EOQualifier qualifierFromAttributes(NSArray, com.webobjects.foundation.NSDictionary);
    public static NSArray relationshipsForAttribute(EOEntity, com.webobjects.eoaccess.EOAttribute);

    public static EOAttribute sourceAttributeForRelationship(com.webobjects.eoaccess.EOEntity, String);
    public static EOAttribute sourceAttributeForRelationship(com.webobjects.eoaccess.EORelationship);

    public static String sourceColumnForRelationship(EOEntity, String);
    public static String sourceColumnForRelationship(EORelationship);

    public static EOEnterpriseObject refetchFailedObject(com.webobjects.eocontrol.EOEditingContext, EOGeneralAdaptorException);

    public static void reapplyChanges(EOEnterpriseObject, EOGeneralAdaptorException);

    public static int deleteRowsDescribedByQualifier(EOEditingContext, String, com.webobjects.eocontrol.EOQualifier);
    public static int updateRowsDescribedByQualifier(EOEditingContext, String, com.webobjects.eocontrol.EOQualifier, NSDictionary);

    public static int insertRow(EOEditingContext, String, NSDictionary);
    public static int insertRows(EOEditingContext, String, java.util.List);

    public static String guessPluginName(EOModel);
    public static String guessPluginNameForConnectionDictionary(NSDictionary);

    public static EOFetchSpecification localizeFetchSpecification(com.webobjects.eocontrol.EOEditingContext, com.webobjects.eocontrol.EOFetchSpecification);

    public static void batchFetchRelationship(EODatabaseContext, String, String, NSArray, EOEditingContext, boolean);
    public static void batchFetchRelationship(EODatabaseContext, com.webobjects.eoaccess.EORelationship, NSArray, EOEditingContext, boolean);

    public static NSSet verifyAllSnapshots();

    public static void makeEditableSharedEntityNamed(String);

    public static EORelationship createRelationship(String, String, String, String, String, boolean, int, boolean, boolean, boolean);
    public static EORelationship createFlattenedRelationship(String, String, String, int, boolean, boolean);
*/
    }
}
