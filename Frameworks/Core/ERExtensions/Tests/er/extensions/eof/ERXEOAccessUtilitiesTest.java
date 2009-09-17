
package er.extensions.eof;

import com.webobjects.foundation.NSDictionary;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;

import com.webobjects.eocontrol.EOEditingContext;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ERXEOAccessUtilitiesTest extends TestCase {

    EOModel ajaxModel = null;
    EOEditingContext ec = null;

    static String buildRoot;
    static {
         buildRoot = System.getProperty("build.root");
    }

    void loadModels() {

        EOModelGroup.setDefaultGroup(new EOModelGroup());

        try {
            EOModelGroup.defaultGroup().addModel(new EOModel(new java.net.URL("file://"+buildRoot+"/AjaxExample.woa/Contents/Resources/AjaxExample.eomodeld")));
        } catch (java.net.MalformedURLException mue) { System.out.println("mue: "+mue); }
        ajaxModel = EOModelGroup.defaultGroup().modelNamed("AjaxExample");
        ec = new EOEditingContext();
    }

    static boolean eoModelGroupsEqual(EOModelGroup group1, EOModelGroup group2) {
        if (group1 == null && group2 == null) return true;
        if (group1 == null && group2 != null) return false;
        if (group1 != null && group2 == null) return false;

        if (group1.models().count() != group2.models().count()) return false;

        java.util.Enumeration<EOModel> models = group1.models().objectEnumerator();

        while (models.hasMoreElements()) {
            EOModel model1 = models.nextElement();
            EOModel model2 = group2.modelNamed(model1.name());
            if (!model1.equals(model2)) return false;
        }
        return true;
    }

    public void testEntityMatchingString() {
        this.loadModels();

        EOEntity found1 = ERXEOAccessUtilities.entityMatchingString(ec, "SomeCompanyPlace");
        Assert.assertSame(ajaxModel.entityNamed("Company"), found1);

        EOEntity found2 = ERXEOAccessUtilities.entityMatchingString(ec, "SomeEmployeeThing");
        Assert.assertSame(ajaxModel.entityNamed("Employee"), found2);

        EOEntity found3 = ERXEOAccessUtilities.entityMatchingString(ec, "CompanyThing");
        Assert.assertSame(ajaxModel.entityNamed("Company"), found3);

        EOEntity found4 = ERXEOAccessUtilities.entityMatchingString(ec, "ThatCompany");
        Assert.assertSame(ajaxModel.entityNamed("Company"), found4);

        EOEntity found5 = ERXEOAccessUtilities.entityMatchingString(ec, "Company");
        Assert.assertSame(ajaxModel.entityNamed("Company"), found5);

        EOEntity found6 = ERXEOAccessUtilities.entityMatchingString(ec, "SomeGarbage");
        Assert.assertNull(found6);

        EOEntity found7 = ERXEOAccessUtilities.entityMatchingString(ec, null);
        Assert.assertNull(found7);

        EOEntity found8 = ERXEOAccessUtilities.entityMatchingString(null, "SomeThing");
        Assert.assertNull(found8);

        EOEntity found9 = ERXEOAccessUtilities.entityMatchingString(null, null);
        Assert.assertNull(found9);
    }

    public void testEntityUsingTable() {
        this.loadModels();

        EOEntity found1 = ERXEOAccessUtilities.entityUsingTable(ec, "GarbageName");
        Assert.assertNull(found1);
    }

    public void testEntityWithNamedIsShared() {
        this.loadModels();

        Assert.assertFalse(ERXEOAccessUtilities.entityWithNamedIsShared(ec, "Company"));
        Assert.assertFalse(ERXEOAccessUtilities.entityWithNamedIsShared(ec, "Employee"));
        Assert.assertFalse(ERXEOAccessUtilities.entityWithNamedIsShared(null, "Company"));

        try {
            boolean check = ERXEOAccessUtilities.entityWithNamedIsShared(ec, null);
            Assert.fail();
        } catch (java.lang.IllegalStateException ise) { /* Good! */ }
    }

    public void testEntityNamed() {
        this.loadModels();

        // It would be more obvious to use EOEntity equals here, but that method does not seem to work,
        // sees differences. -rrk
        //
        EOEntity found1 = ERXEOAccessUtilities.entityNamed(ec, "Company");
        Assert.assertNotNull(found1);
        Assert.assertEquals(EOModelGroup.defaultGroup().entityNamed("Company").name(), found1.name());

        EOEntity found2 = ERXEOAccessUtilities.entityNamed(ec, "Employee");
        Assert.assertNotNull(found2);
        Assert.assertEquals(EOModelGroup.defaultGroup().entityNamed("Employee").name(), found2.name());

        EOEntity found3 = ERXEOAccessUtilities.entityNamed(null, "Company");
        Assert.assertNotNull(found3);
        Assert.assertEquals(EOModelGroup.defaultGroup().entityNamed("Company").name(), found3.name());

        Assert.assertNull(ERXEOAccessUtilities.entityNamed(ec, null));
    }

    public void testModelGroup() {
        this.loadModels();

        /*
        // Does not seem to be very reliable...

        EOModelGroup referenceGroup = EOModelGroup.defaultGroup();

        EOModelGroup group1 = ERXEOAccessUtilities.modelGroup(ec);
        Assert.assertTrue(eoModelGroupsEqual(referenceGroup, group1));

        // Apparently, this does not work all the time...
        //
        EOModelGroup group2 = ERXEOAccessUtilities.modelGroup(null);
        Assert.assertTrue(eoModelGroupsEqual(referenceGroup, group2));
        */
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

    public static EOEntity destinationEntityForKeyPath(com.webobjects.eoaccess.EOEntity, String);

    public static EOEntity entityForEo(EOEnterpriseObject);

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

