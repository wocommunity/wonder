package er.extensions.eof;

import junit.framework.Assert;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;

import er.erxtest.ERXTestCase;
import er.erxtest.ERXTestSuite;
import er.erxtest.ERXTestUtilities;
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

	private static final NSDictionary<String,NSArray<String>> skipTestsForAdaptor;

	static {
		NSMutableDictionary<String,NSArray<String>> skips = new NSMutableDictionary<String,NSArray<String>>();

		NSArray<String> memorySkips = new NSArray<String>(new String[] { "testRawRowsForSQLExpressionEOEditingContextStringEOSQLExpression" } );
		skips.setObjectForKey(memorySkips, "Memory");
                skips.setObjectForKey(NSArray.EmptyArray, "JDBC");

		skipTestsForAdaptor = skips.immutableClone();
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();

		// System.out.println("ERXEOAccessUtilitiesTest.setUp: setup");

		ec = ERXEC.newEditingContext();

		model = EOModelGroup.defaultGroup().modelNamed(ERXTestSuite.ERXTEST_MODEL);

		companyEntity = model.entityNamed(Company.ENTITY_NAME);
		employeeEntity = model.entityNamed(Employee.ENTITY_NAME);
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#entityMatchingString(com.webobjects.eocontrol.EOEditingContext, java.lang.String)}.
	 */
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

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#entityUsingTable(com.webobjects.eocontrol.EOEditingContext, java.lang.String)}.
	 */
	public void testEntityUsingTable() {
		Assert.assertEquals(companyEntity, ERXEOAccessUtilities.entityUsingTable(ec, "Company"));
		Assert.assertNull(ERXEOAccessUtilities.entityUsingTable(ec, "GarbageName"));
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#entityWithNamedIsShared(com.webobjects.eocontrol.EOEditingContext, java.lang.String)}.
	 */
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

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#entityNamed(com.webobjects.eocontrol.EOEditingContext, java.lang.String)}.
	 */
	public void testEntityNamed() {
		Assert.assertEquals(companyEntity, ERXEOAccessUtilities.entityNamed(ec, Company.ENTITY_NAME));
		Assert.assertEquals(employeeEntity, ERXEOAccessUtilities.entityNamed(ec, Employee.ENTITY_NAME));
		Assert.assertEquals(companyEntity, ERXEOAccessUtilities.entityNamed(null, Company.ENTITY_NAME));
		Assert.assertNull(ERXEOAccessUtilities.entityNamed(ec, null));
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#modelGroup(com.webobjects.eocontrol.EOEditingContext)}.
	 */
	public void testModelGroup() {
		Assert.assertEquals(EOModelGroup.defaultGroup(), ERXEOAccessUtilities.modelGroup(ec));
		Assert.assertEquals(EOModelGroup.defaultGroup(), ERXEOAccessUtilities.modelGroup(null));
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#destinationEntityForKeyPath(com.webobjects.eoaccess.EOEntity, java.lang.String)}.
	 */
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

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#entityForEo(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
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

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#rowCountForFetchSpecification(com.webobjects.eocontrol.EOEditingContext, com.webobjects.eocontrol.EOFetchSpecification)}.
	 */
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

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#getNextValFromSequenceNamed(com.webobjects.eocontrol.EOEditingContext, java.lang.String, java.lang.String)}.
	 */
	public void _testGetNextValFromSequenceNamed() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#evaluateSQLWithEntityNamed(com.webobjects.eocontrol.EOEditingContext, java.lang.String, java.lang.String)}.
	 */
	public void _testEvaluateSQLWithEntityNamed() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#evaluateSQLWithEntity(com.webobjects.eocontrol.EOEditingContext, com.webobjects.eoaccess.EOEntity, java.lang.String)}.
	 */
	public void _testEvaluateSQLWithEntity() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#sqlForFetchSpecification(com.webobjects.eocontrol.EOEditingContext, com.webobjects.eocontrol.EOFetchSpecification)}.
	 */
	public void _testSqlForFetchSpecification() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#rawRowsForSQLExpression(com.webobjects.eocontrol.EOEditingContext, java.lang.String, com.webobjects.eoaccess.EOSQLExpression)}.
	 * Not the best test, but it will work. Uses the select statement from the method being tested to fetch from the ERXTest model. Then, after inserting
	 * a few objects, makes sure that the results are greater than the last time.
	 */
	public void testRawRowsForSQLExpressionEOEditingContextStringEOSQLExpression() {

		EOAdaptor adaptor = EOAdaptor.adaptorWithName(EOModelGroup.defaultGroup().modelNamed(ERXTestSuite.ERXTEST_MODEL).adaptorName());
		if (skipTestsForAdaptor.objectForKey(adaptor.name()).contains("testRawRowsForSQLExpressionEOEditingContextStringEOSQLExpression")) return;
		EOSQLExpressionFactory factory = new EOSQLExpressionFactory(adaptor);
		NSArray<EOEntity> entities = EOModelGroup.defaultGroup().modelNamed(ERXTestSuite.ERXTEST_MODEL).entities();
		NSMutableDictionary<String,Number> counts = new NSMutableDictionary<String,Number>();

		for (EOEntity entity : entities) {
			EOSQLExpression sqlExp = factory.selectStatementForAttributes(entity.attributes(),
								false,
								new EOFetchSpecification(entity.name(), null, null),
								entity);
			NSArray rows = ERXEOAccessUtilities.rawRowsForSQLExpression(ec, ERXTestSuite.ERXTEST_MODEL, sqlExp);

			counts.setObjectForKey(rows.size(), entity.name());
		}

		ERXTestUtilities.createCompanyAnd3Employees();

		NSMutableSet<String> hasMore = new NSMutableSet<String>();

		for (EOEntity entity : entities) {
			EOSQLExpression sqlExp = factory.selectStatementForAttributes(entity.attributes(),
								false,
								new EOFetchSpecification(entity.name(), null, null),
								entity);
			NSArray rows = ERXEOAccessUtilities.rawRowsForSQLExpression(ec, ERXTestSuite.ERXTEST_MODEL, sqlExp);

			if ( ! counts.containsKey(entity.name()))
				fail();
			if (counts.objectForKey(entity.name()).intValue() > rows.size())
				fail();
			if (counts.objectForKey(entity.name()).intValue() < rows.size())
				hasMore.add(entity.name());
		}

		Assert.assertEquals(new NSSet<String>(new String[] { "Company", "Employee", "Paycheck" } ), hasMore);
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#rawRowsForSQLExpression(com.webobjects.eocontrol.EOEditingContext, com.webobjects.eoaccess.EOModel, com.webobjects.eoaccess.EOSQLExpression, com.webobjects.foundation.NSArray)}.
	 */
	public void _testRawRowsForSQLExpressionEOEditingContextEOModelEOSQLExpressionNSArrayOfEOAttribute() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#sqlExpressionForFetchSpecification(com.webobjects.eocontrol.EOEditingContext, com.webobjects.eocontrol.EOFetchSpecification, long, long)}.
	 */
	public void _testSqlExpressionForFetchSpecification() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#createAggregateAttribute(com.webobjects.eocontrol.EOEditingContext, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	public void _testCreateAggregateAttributeEOEditingContextStringStringString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#createAggregateAttribute(com.webobjects.eocontrol.EOEditingContext, java.lang.String, java.lang.String, java.lang.String, java.lang.Class, java.lang.String)}.
	 */
	public void _testCreateAggregateAttributeEOEditingContextStringStringStringClassString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#createAggregateAttribute(com.webobjects.eocontrol.EOEditingContext, java.lang.String, java.lang.String, java.lang.String, java.lang.Class, java.lang.String, java.lang.String)}.
	 */
	public void _testCreateAggregateAttributeEOEditingContextStringStringStringClassStringString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#createAggregateAttribute(com.webobjects.eocontrol.EOEditingContext, java.lang.String, java.lang.String, java.lang.String, java.lang.Class, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	public void _testCreateAggregateAttributeEOEditingContextStringStringStringClassStringStringString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#createSchemaSQLForEntitiesInModelWithNameAndOptionsForOracle9(com.webobjects.foundation.NSArray, java.lang.String, com.webobjects.foundation.NSDictionary)}.
	 */
	public void _testCreateSchemaSQLForEntitiesInModelWithNameAndOptionsForOracle9() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#createSchemaSQLForEntitiesInModelWithNameAndOptions(com.webobjects.foundation.NSArray, java.lang.String, com.webobjects.foundation.NSDictionary)}.
	 */
	public void _testCreateSchemaSQLForEntitiesInModelWithNameAndOptions() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#createSchemaSQLForEntitiesWithOptions(com.webobjects.foundation.NSArray, com.webobjects.eoaccess.EODatabaseContext, com.webobjects.foundation.NSDictionary)}.
	 */
	public void _testCreateSchemaSQLForEntitiesWithOptions() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#createSchemaSQLForEntitiesInModelWithName(com.webobjects.foundation.NSArray, java.lang.String)}.
	 */
	public void _testCreateSchemaSQLForEntitiesInModelWithName() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#createSchemaSQLForEntitiesInDatabaseContext(com.webobjects.foundation.NSArray, com.webobjects.eoaccess.EODatabaseContext, boolean, boolean)}.
	 */
	public void _testCreateSchemaSQLForEntitiesInDatabaseContext() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#createIndexSQLForEntitiesForOracle(com.webobjects.foundation.NSArray)}.
	 */
	public void _testCreateIndexSQLForEntitiesForOracle() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#createIndexSQLForEntities(com.webobjects.foundation.NSArray)}.
	 */
	public void _testCreateIndexSQLForEntitiesNSArray() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#createIndexSQLForEntities(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray)}.
	 */
	public void _testCreateIndexSQLForEntitiesNSArrayNSArray() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#entityUsesSeparateTable(com.webobjects.eoaccess.EOEntity)}.
	 */
	public void _testEntityUsesSeparateTable() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#attributeWithColumnNameFromEntity(java.lang.String, com.webobjects.eoaccess.EOEntity)}.
	 */
	public void _testAttributeWithColumnNameFromEntity() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#isOptimisticLockingFailure(com.webobjects.eoaccess.EOGeneralAdaptorException)}.
	 */
	public void _testIsOptimisticLockingFailure() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#snapshotsForObjectsFromRelationshipNamed(com.webobjects.foundation.NSArray, java.lang.String)}.
	 */
	public void _testSnapshotsForObjectsFromRelationshipNamed() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#primaryKeyDictionaryForEntity(com.webobjects.eocontrol.EOEditingContext, java.lang.String)}.
	 */
	public void _testPrimaryKeyDictionaryForEntity() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#primaryKeysForObjects(com.webobjects.foundation.NSArray)}.
	 */
	public void _testPrimaryKeysForObjects() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#lastRelationship(com.webobjects.eoaccess.EORelationship)}.
	 */
	public void _testLastRelationship() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#attributePathForKeyPath(com.webobjects.eoaccess.EOEntity, java.lang.String)}.
	 */
	public void _testAttributePathForKeyPath() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#sqlWhereClauseStringForKey(com.webobjects.eoaccess.EOSQLExpression, java.lang.String, com.webobjects.foundation.NSArray)}.
	 */
	public void _testSqlWhereClauseStringForKey() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#databaseContextForObject(com.webobjects.eocontrol.EOEnterpriseObject)}.
	 */
	public void _testDatabaseContextForObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#databaseContextForEntityNamed(com.webobjects.eocontrol.EOObjectStoreCoordinator, java.lang.String)}.
	 */
	public void _testDatabaseContextForEntityNamed() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#closeDatabaseConnections(com.webobjects.eocontrol.EOObjectStoreCoordinator)}.
	 */
	public void _testCloseDatabaseConnections() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#classPropertiesNotInParent(com.webobjects.eoaccess.EOEntity, boolean, boolean, boolean)}.
	 */
	public void _testClassPropertiesNotInParent() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#externalNamesForEntity(com.webobjects.eoaccess.EOEntity, boolean)}.
	 */
	public void _testExternalNamesForEntity() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#externalNamesForEntityNamed(java.lang.String, boolean)}.
	 */
	public void _testExternalNamesForEntityNamed() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#rootEntityForEntity(com.webobjects.eoaccess.EOEntity)}.
	 */
	public void _testRootEntityForEntity() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#rootEntityForEntityNamed(java.lang.String)}.
	 */
	public void _testRootEntityForEntityNamed() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#logExpression(com.webobjects.eoaccess.EOAdaptorChannel, com.webobjects.eoaccess.EOSQLExpression, long)}.
	 */
	public void _testLogExpression() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#createLogString(com.webobjects.eoaccess.EOAdaptorChannel, com.webobjects.eoaccess.EOSQLExpression, long)}.
	 */
	public void _testCreateLogString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#qualifierFromAttributes(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSDictionary)}.
	 */
	public void _testQualifierFromAttributes() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#relationshipsForAttribute(com.webobjects.eoaccess.EOEntity, com.webobjects.eoaccess.EOAttribute)}.
	 */
	public void _testRelationshipsForAttribute() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#sourceAttributeForRelationship(com.webobjects.eoaccess.EOEntity, java.lang.String)}.
	 */
	public void _testSourceAttributeForRelationshipEOEntityString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#sourceAttributeForRelationship(com.webobjects.eoaccess.EORelationship)}.
	 */
	public void _testSourceAttributeForRelationshipEORelationship() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#sourceColumnForRelationship(com.webobjects.eoaccess.EOEntity, java.lang.String)}.
	 */
	public void _testSourceColumnForRelationshipEOEntityString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#sourceColumnForRelationship(com.webobjects.eoaccess.EORelationship)}.
	 */
	public void _testSourceColumnForRelationshipEORelationship() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#refetchFailedObject(com.webobjects.eocontrol.EOEditingContext, com.webobjects.eoaccess.EOGeneralAdaptorException)}.
	 */
	public void _testRefetchFailedObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#reapplyChanges(com.webobjects.eocontrol.EOEnterpriseObject, com.webobjects.eoaccess.EOGeneralAdaptorException)}.
	 */
	public void _testReapplyChanges() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#deleteRowsDescribedByQualifier(com.webobjects.eocontrol.EOEditingContext, java.lang.String, com.webobjects.eocontrol.EOQualifier)}.
	 */
	public void _testDeleteRowsDescribedByQualifier() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#updateRowsDescribedByQualifier(com.webobjects.eocontrol.EOEditingContext, java.lang.String, com.webobjects.eocontrol.EOQualifier, com.webobjects.foundation.NSDictionary)}.
	 */
	public void _testUpdateRowsDescribedByQualifier() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#insertRow(com.webobjects.eocontrol.EOEditingContext, java.lang.String, com.webobjects.foundation.NSDictionary)}.
	 */
	public void _testInsertRow() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#insertRows(com.webobjects.eocontrol.EOEditingContext, java.lang.String, java.util.Collection)}.
	 */
	public void _testInsertRows() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#primaryKeysForNewRows(com.webobjects.eocontrol.EOEditingContext, java.lang.String, int)}.
	 */
	public void _testPrimaryKeysForNewRows() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#guessPluginName(com.webobjects.eoaccess.EOModel)}.
	 */
	public void _testGuessPluginName() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#guessPluginNameForConnectionDictionary(com.webobjects.foundation.NSDictionary)}.
	 */
	public void _testGuessPluginNameForConnectionDictionary() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#localizeFetchSpecification(com.webobjects.eocontrol.EOEditingContext, com.webobjects.eocontrol.EOFetchSpecification)}.
	 */
	public void _testLocalizeFetchSpecification() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#batchFetchRelationship(com.webobjects.eoaccess.EODatabaseContext, java.lang.String, java.lang.String, com.webobjects.foundation.NSArray, com.webobjects.eocontrol.EOEditingContext, boolean)}.
	 */
	public void _testBatchFetchRelationshipEODatabaseContextStringStringNSArrayEOEditingContextBoolean() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#batchFetchRelationship(com.webobjects.eoaccess.EODatabaseContext, com.webobjects.eoaccess.EORelationship, com.webobjects.foundation.NSArray, com.webobjects.eocontrol.EOEditingContext, boolean)}.
	 */
	public void _testBatchFetchRelationshipEODatabaseContextEORelationshipNSArrayEOEditingContextBoolean() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#verifyAllSnapshots()}.
	 */
	public void _testVerifyAllSnapshots() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#makeEditableSharedEntityNamed(java.lang.String)}.
	 */
	public void _testMakeEditableSharedEntityNamed() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#createRelationship(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, int, boolean, boolean, boolean)}.
	 */
	public void _testCreateRelationship() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#createFlattenedRelationship(java.lang.String, java.lang.String, java.lang.String, int, boolean, boolean)}.
	 */
	public void _testCreateFlattenedRelationship() {
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

	/**
	 * Test method for {@link java.lang.Object#finalize()}.
	 */
	public void _testFinalize() {
		fail("Not yet implemented");
	}
}
