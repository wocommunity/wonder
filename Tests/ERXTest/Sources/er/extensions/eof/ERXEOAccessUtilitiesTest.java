package er.extensions.eof;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSDictionary;

import er.erxtest.model.Company;
import er.erxtest.model.Employee;
import er.erxtest.tests.ERXTestCase;
import er.extensions.ERExtensionsTest;

/**
 * Test the static methods in the ERXEOAccessUtilities class. This class has an inner
 * class that actually contains the test methods. We can take the available models
 * (or the ones compatible with these tests) and run the tests multiple times. The
 * tests can be parameterized to use different models or different configurations.
 */
public class ERXEOAccessUtilitiesTest extends ERXTestCase {
  static boolean modelDataLoaded = false;

  public static Test suite() {
    TestSuite suite = new TestSuite();
    // See note in er.extensions.eof.ERXEOAccessUtilities.testAll() -rrk

    java.util.Enumeration<String> adaptors = ERExtensionsTest.availableAdaptorNames().objectEnumerator();
    while (adaptors.hasMoreElements()) {
      String adaptorName = adaptors.nextElement();
      if (ERExtensionsTest.dbExistsForAdaptor(adaptorName)) {
        suite.addTest(new ERXEOAccessUtilitiesTest(adaptorName));
      }
    }

//      NSArray<String> methods = ERExtensionsTest.testMethodsForClassName(Tests.class.getName());
//      for (int idx = 0; idx < methods.count(); idx++) {
//          String testName = methods.get(idx);
//
//          java.util.Enumeration<String> adaptors = ERExtensionsTest.availableAdaptorNames().objectEnumerator();
//          while (adaptors.hasMoreElements()) {
//              String adaptorName = adaptors.nextElement();
//              if (ERExtensionsTest.dbExistsForAdaptor(adaptorName))
//                  suite.addTest(new Tests(testName, adaptorName));
//          }
//      }

    return suite;
  }

  private EOEditingContext _ec;
  private String _adaptorName;
  private String _modelName;
  private EOModel _model;
  private NSDictionary _origConnectionDictionary;

  public ERXEOAccessUtilitiesTest(String adaptorName) {
    super(adaptorName);
    this._adaptorName = adaptorName;
  }

  String config() {
    return "adaptor: \"" + _adaptorName + "\"";
  }

  public void setUp() throws Exception {
    super.setUp();

    // System.out.println("ERXEOAccessUtilitiesTest.setUp: setup");

    if (_ec != null)
      _ec.dispose();
    if (_model != null)
      _model.dispose();

    _modelName = "ERXTest";
    _model = EOModelGroup.defaultGroup().modelNamed(_modelName);
    _origConnectionDictionary = _model.connectionDictionary();
    _model.setConnectionDictionary(ERExtensionsTest.connectionDict(_adaptorName));

    _ec = ERXEC.newEditingContext();
    if (_model.adaptorName().equals("JDBC") && !modelDataLoaded) {
      modelDataLoaded = true;
      ERExtensionsTest.loadData(_ec, _model, this, "AjaxExampleData.plist");
    }
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    _model.setConnectionDictionary(_origConnectionDictionary);
  }

  public void testEntityMatchingString() {
    EOEntity found1 = ERXEOAccessUtilities.entityMatchingString(_ec, "SomeCompanyPlace");
    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(_model.entityNamed(Company.ENTITY_NAME), found1));

    EOEntity found2 = ERXEOAccessUtilities.entityMatchingString(_ec, "SomeEmployeeThing");
    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(_model.entityNamed(Employee.ENTITY_NAME), found2));

    EOEntity found3 = ERXEOAccessUtilities.entityMatchingString(_ec, "CompanyThing");
    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(_model.entityNamed(Company.ENTITY_NAME), found3));

    EOEntity found4 = ERXEOAccessUtilities.entityMatchingString(_ec, "ThatCompany");
    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(_model.entityNamed(Company.ENTITY_NAME), found4));

    EOEntity found5 = ERXEOAccessUtilities.entityMatchingString(_ec, "Company");
    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(_model.entityNamed(Company.ENTITY_NAME), found5));

    EOEntity found6 = ERXEOAccessUtilities.entityMatchingString(_ec, "SomeGarbage");
    Assert.assertNull(this.config(), found6);

    EOEntity found7 = ERXEOAccessUtilities.entityMatchingString(_ec, null);
    Assert.assertNull(this.config(), found7);

    EOEntity found8 = ERXEOAccessUtilities.entityMatchingString(null, "SomeThing");
    Assert.assertNull(this.config(), found8);

    EOEntity found9 = ERXEOAccessUtilities.entityMatchingString(null, null);
    Assert.assertNull(this.config(), found9);
  }

  public void testEntityUsingTable() {
    EOEntity found1 = ERXEOAccessUtilities.entityUsingTable(_ec, "GarbageName");
    Assert.assertNull(this.config(), found1);
  }

  public void testEntityWithNamedIsShared() {
    Assert.assertFalse(ERXEOAccessUtilities.entityWithNamedIsShared(_ec, Company.ENTITY_NAME));
    Assert.assertFalse(ERXEOAccessUtilities.entityWithNamedIsShared(_ec, Employee.ENTITY_NAME));
    Assert.assertFalse(ERXEOAccessUtilities.entityWithNamedIsShared(null, Company.ENTITY_NAME));

    try {
      @SuppressWarnings("unused")
      boolean check = ERXEOAccessUtilities.entityWithNamedIsShared(_ec, null);
      Assert.fail();
    }
    catch (java.lang.IllegalStateException ise) { /* Good! */
    }
  }

  public void testEntityNamed() {
    EOEntity found1 = ERXEOAccessUtilities.entityNamed(_ec, Company.ENTITY_NAME);
    Assert.assertNotNull(this.config(), found1);
    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(EOModelGroup.defaultGroup().entityNamed(Company.ENTITY_NAME).name(), found1.name()));

    EOEntity found2 = ERXEOAccessUtilities.entityNamed(_ec, Employee.ENTITY_NAME);
    Assert.assertNotNull(this.config(), found2);
    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(EOModelGroup.defaultGroup().entityNamed(Employee.ENTITY_NAME).name(), found2.name()));

    EOEntity found3 = ERXEOAccessUtilities.entityNamed(null, Company.ENTITY_NAME);
    Assert.assertNotNull(this.config(), found3);
    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(EOModelGroup.defaultGroup().entityNamed(Company.ENTITY_NAME).name(), found3.name()));

    Assert.assertNull(this.config(), ERXEOAccessUtilities.entityNamed(_ec, null));
  }

  public void testModelGroup() {
    EOModelGroup referenceGroup = EOModelGroup.defaultGroup();

    EOModelGroup group1 = ERXEOAccessUtilities.modelGroup(_ec);
    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(referenceGroup, group1));

    EOModelGroup group2 = ERXEOAccessUtilities.modelGroup(null);
    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(referenceGroup, group2));
  }

  public void testDestinationEntityForKeyPath() {
    // public static EOEntity destinationEntityForKeyPath(com.webobjects.eoaccess.EOEntity, String);

    EOEntity companyEntity = EOModelGroup.defaultGroup().entityNamed(Company.ENTITY_NAME);
    EOEntity employeeEntity = EOModelGroup.defaultGroup().entityNamed(Employee.ENTITY_NAME);

    EOEntity entity1 = ERXEOAccessUtilities.destinationEntityForKeyPath(companyEntity, "employees");
    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(employeeEntity, entity1));

    EOEntity entity2 = ERXEOAccessUtilities.destinationEntityForKeyPath(employeeEntity, "company");
    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(companyEntity, entity2));
  }

  public void testEntityForEo() {
    // public static EOEntity entityForEo(EOEnterpriseObject);
    // MS: hmmm .. this test randomly fails for me. it appears to be some sort of race condition with
    // class description loading. i SUSPECT if this was going through the full wonder startup process,
    // this wouldn't happen as you'd have the full class description set loaded before any EOF
    // API was touched.

    EOEntity companyEntity = EOModelGroup.defaultGroup().entityNamed(Company.ENTITY_NAME);
    Assert.assertNotNull(this.config(), companyEntity);

    EOEntity employeeEntity = EOModelGroup.defaultGroup().entityNamed(Employee.ENTITY_NAME);
    Assert.assertNotNull(this.config(), employeeEntity);

    EOEnterpriseObject eo1 = EOUtilities.createAndInsertInstance(_ec, Company.ENTITY_NAME);
    Assert.assertNotNull(this.config(), eo1);

    EOEntity entity1 = ERXEOAccessUtilities.entityForEo(eo1);
    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(companyEntity, entity1));

    EOEnterpriseObject eo2 = EOUtilities.createAndInsertInstance(_ec, Employee.ENTITY_NAME);
    Assert.assertNotNull(this.config(), eo2);

    EOEntity entity2 = ERXEOAccessUtilities.entityForEo(eo2);
    Assert.assertTrue(this.config(), ERExtensionsTest.equalsForEOAccessObjects(employeeEntity, entity2));
  }

  public void testRowCountForFetchSpecification() {
    // public static int rowCountForFetchSpecification(EOEditingContext, com.webobjects.eocontrol.EOFetchSpecification);

    if (!_model.adaptorName().equals("JDBC"))
      return;

    // first check getting all objects for entity...

    @SuppressWarnings("unused")
	int count = ERXEOAccessUtilities.rowCountForFetchSpecification(_ec, new EOFetchSpecification(Employee.ENTITY_NAME, null, null));
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
