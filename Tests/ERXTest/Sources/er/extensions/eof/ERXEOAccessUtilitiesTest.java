package er.extensions.eof;

import junit.framework.Assert;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEditingContext;

import er.erxtest.ERXTestCase;
import er.erxtest.model.Company;
import er.erxtest.model.Employee;

/**
 * Test the static methods in the ERXEOAccessUtilities class. This class has an inner
 * class that actually contains the test methods. We can take the available models
 * (or the ones compatible with these tests) and run the tests multiple times. The
 * tests can be parameterized to use different models or different configurations.
 */
public class ERXEOAccessUtilitiesTest extends ERXTestCase {
  static boolean modelDataLoaded = false;

  private EOEditingContext ec;
  
  private EOModel model;

  private EOEntity companyEntity;
  private EOEntity employeeEntity;
  
  public ERXEOAccessUtilitiesTest(String adaptorName) {
    super(adaptorName);
  }

  public void setUp() throws Exception {
    super.setUp();

    // System.out.println("ERXEOAccessUtilitiesTest.setUp: setup");

    ec = ERXEC.newEditingContext();

    model = EOModelGroup.defaultGroup().modelNamed("ERXTest");

    companyEntity = model.entityNamed(Company.ENTITY_NAME);
    employeeEntity = model.entityNamed(Employee.ENTITY_NAME);
  }

  public void testEntityMatchingString() {
	  Assert.assertEquals(companyEntity, ERXEOAccessUtilities.entityMatchingString(ec, "SomeCompanyPlace"));
	  Assert.assertEquals(employeeEntity, ERXEOAccessUtilities.entityMatchingString(ec, "SomeEmployeeThing"));
	  Assert.assertEquals(companyEntity, ERXEOAccessUtilities.entityMatchingString(ec, "CompanyThing"));
	  Assert.assertEquals(companyEntity, ERXEOAccessUtilities.entityMatchingString(ec, "ThatCompany"));
	  Assert.assertEquals(companyEntity, ERXEOAccessUtilities.entityMatchingString(ec, "Company"));
	  Assert.assertNull(ERXEOAccessUtilities.entityMatchingString(ec, "SomeGarbage"));
	  Assert.assertNull(ERXEOAccessUtilities.entityMatchingString(ec, "null"));
	  Assert.assertNull(ERXEOAccessUtilities.entityMatchingString(null, "SomeThing"));
	  Assert.assertNull(ERXEOAccessUtilities.entityMatchingString(null, null));
  }

  public void testEntityUsingTable() {
	  Assert.assertEquals(companyEntity, ERXEOAccessUtilities.entityUsingTable(ec, "Company"));
	  Assert.assertNull(ERXEOAccessUtilities.entityUsingTable(ec, "GarbageName"));
  }

  public void testEntityWithNamedIsShared() {
    Assert.assertFalse(ERXEOAccessUtilities.entityWithNamedIsShared(ec, Company.ENTITY_NAME));
    Assert.assertFalse(ERXEOAccessUtilities.entityWithNamedIsShared(ec, Employee.ENTITY_NAME));
    Assert.assertFalse(ERXEOAccessUtilities.entityWithNamedIsShared(null, Company.ENTITY_NAME));

    try {
      @SuppressWarnings("unused")
      boolean check = ERXEOAccessUtilities.entityWithNamedIsShared(ec, null);
      Assert.fail();
    }
    catch (java.lang.IllegalStateException ise) { /* Good! */
    }
  }

  public void testEntityNamed() {
	  Assert.assertEquals(companyEntity, ERXEOAccessUtilities.entityNamed(ec, Company.ENTITY_NAME));
	  Assert.assertEquals(employeeEntity, ERXEOAccessUtilities.entityNamed(ec, Employee.ENTITY_NAME));
	  Assert.assertEquals(companyEntity, ERXEOAccessUtilities.entityNamed(null, Company.ENTITY_NAME));
	  Assert.assertNull(ERXEOAccessUtilities.entityNamed(ec, null));
  }

  public void testModelGroup() {
	    Assert.assertEquals(EOModelGroup.defaultGroup(), ERXEOAccessUtilities.modelGroup(ec));
	    Assert.assertEquals(EOModelGroup.defaultGroup(), ERXEOAccessUtilities.modelGroup(null));
  }

  public void _testDestinationEntityForKeyPath() {
//    // public static EOEntity destinationEntityForKeyPath(com.webobjects.eoaccess.EOEntity, String);
//
//    EOEntity companyEntity = EOModelGroup.defaultGroup().entityNamed(Company.ENTITY_NAME);
//    EOEntity employeeEntity = EOModelGroup.defaultGroup().entityNamed(Employee.ENTITY_NAME);
//
//    EOEntity entity1 = ERXEOAccessUtilities.destinationEntityForKeyPath(companyEntity, "employees");
//    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(employeeEntity, entity1));
//
//    EOEntity entity2 = ERXEOAccessUtilities.destinationEntityForKeyPath(employeeEntity, "company");
//    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(companyEntity, entity2));
  }

  public void _testEntityForEo() {
    // public static EOEntity entityForEo(EOEnterpriseObject);
    // MS: hmmm .. this test randomly fails for me. it appears to be some sort of race condition with
    // class description loading. i SUSPECT if this was going through the full wonder startup process,
    // this wouldn't happen as you'd have the full class description set loaded before any EOF
    // API was touched.

//    EOEntity companyEntity = EOModelGroup.defaultGroup().entityNamed(Company.ENTITY_NAME);
//    Assert.assertNotNull(this.config(), companyEntity);
//
//    EOEntity employeeEntity = EOModelGroup.defaultGroup().entityNamed(Employee.ENTITY_NAME);
//    Assert.assertNotNull(this.config(), employeeEntity);
//
//    EOEnterpriseObject eo1 = EOUtilities.createAndInsertInstance(_ec, Company.ENTITY_NAME);
//    Assert.assertNotNull(this.config(), eo1);
//
//    EOEntity entity1 = ERXEOAccessUtilities.entityForEo(eo1);
//    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(companyEntity, entity1));
//
//    EOEnterpriseObject eo2 = EOUtilities.createAndInsertInstance(_ec, Employee.ENTITY_NAME);
//    Assert.assertNotNull(this.config(), eo2);
//
//    EOEntity entity2 = ERXEOAccessUtilities.entityForEo(eo2);
//    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(employeeEntity, entity2));
  }

  public void _testRowCountForFetchSpecification() {
    // public static int rowCountForFetchSpecification(EOEditingContext, com.webobjects.eocontrol.EOFetchSpecification);

//    if (!_model.adaptorName().equals("JDBC"))
//      return;
//
//    // first check getting all objects for entity...
//
//    @SuppressWarnings("unused")
//	int count = ERXEOAccessUtilities.rowCountForFetchSpecification(_ec, new EOFetchSpecification(Employee.ENTITY_NAME, null, null));
    //int count = ERXEOAccessUtilities.rowCountForFetchSpecification(ec, new EOFetchSpecification("Expn", null, null));

    //System.out.println("count: "+count);
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
