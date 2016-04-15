package er.neo4jadaptor.test;

import junit.framework.TestCase;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXQ;
import er.neo4jadaptor.test.eo.FirstEntity;
import er.neo4jadaptor.test.eo.Join;
import er.neo4jadaptor.test.eo.SecondEntity;
import er.neo4jadaptor.test.eo.ThirdEntity;
import er.neo4jadaptor.test.tools.Tools;

public class Neo4JAdaptorTest extends TestCase {
	private EOEditingContext ec;
	
	@Override
	public void setUp() {
		Tools.ensureInitialized();
		Tools.cleanup();
		
		ec = ERXEC.newEditingContext();
	}

	@SuppressWarnings("unchecked")
	private <T> NSArray<T> fetch(String entityName, EOQualifier qualifier) {
		EOFetchSpecification fs = new EOFetchSpecification(entityName, qualifier, null);
		
		return ec.objectsWithFetchSpecification(fs);
	}

	@SuppressWarnings("unchecked")
	private <T> NSArray<T> fetch(String entityName, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrdering) {
		EOFetchSpecification fs = new EOFetchSpecification(entityName, qualifier, sortOrdering);
		
		return ec.objectsWithFetchSpecification(fs);
	}
	
	private <T> NSArray<T> fetchAll(String entityName, NSArray<EOSortOrdering> sortOrdering) {
		return fetch(entityName, null, sortOrdering);
	}

	private <T> NSArray<T> fetchAll(String entityName) {
		return fetch(entityName, null, null);
	}
	
	private String path(String ... keys) {
		StringBuilder b = new StringBuilder();
		
		for (int i=0; i<keys.length; i++) {
			if (i > 0) {
				b.append('.');
			}
			b.append(keys[i]);
		}
		return b.toString();
	}
	
	public void test01_insert() {
		FirstEntity row = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		FirstEntity fetchedRow;
		NSTimestamp ts = new NSTimestamp();
		
		row.setText("aaa");
		row.setBool(true);
		row.setNumber(123);
		row.setTimestamp(ts);
		
		ec.saveChanges();
		
		NSArray<FirstEntity> results = fetchAll(FirstEntity.ENTITY_NAME);
		
		assertEquals("Expecting only one row", 1, results.count());
		fetchedRow = results.get(0);
		
		assertEquals(row.text(), fetchedRow.text());
		assertEquals(row.bool(), fetchedRow.bool());
		assertEquals(row.number(), fetchedRow.number());
		assertEquals(row.timestamp(), fetchedRow.timestamp());
		
	}
	
	public void test02_update() {
		FirstEntity row = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		NSArray<FirstEntity> results;
		
		row.setText("aaa");
		
		ec.saveChanges();
		
		row.setText("bbb");
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		results = fetchAll(FirstEntity.ENTITY_NAME);
		assertEquals(1, results.count());
		
		assertEquals("bbb", results.get(0).text());
	}

	public void test03_textSearchOnRowInsertion() {
		FirstEntity row = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		NSArray<FirstEntity> results;
		
		row.setText("aaa");
		
		ec.saveChanges();
		
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.equals(FirstEntity.TEXT_KEY, "aaa"));
		// if it fails then probably Lucene index hasn't been updated
		assertEquals(1, results.count());
		assertEquals(row, results.get(0));
	}
	
	public void test04_textSearchOnRowUpdate() {
		FirstEntity row = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		NSArray<FirstEntity> results;
		
		row.setText("aaa");
		
		ec.saveChanges();
		
		row.setText("bbb");
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.equals(FirstEntity.TEXT_KEY, "aaa"));
		// if it fails then probably Lucene index hasn't been updated
		assertEquals("Succeeded to search for 'aaa', but shouldn't", 0, results.count());
		
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.equals(FirstEntity.TEXT_KEY, "bbb"));
		// if it fails then probably Lucene index hasn't been updated
		assertEquals(1, results.count());
		assertEquals("bbb", results.get(0).text());
	}
	
	public void test05_queryingNulls() {
		FirstEntity row1 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		FirstEntity row2 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		NSArray<FirstEntity> results;
		
		row1.setText(null);
		row2.setText("abc");

		ec.saveChanges();
		ec.invalidateAllObjects();

		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.isNull(FirstEntity.TEXT_KEY));
		assertEquals(1, results.count());
		assertEquals(row1, results.get(0));
		assertNull(results.get(0).text());
		
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.isNotNull(FirstEntity.TEXT_KEY));
		assertEquals(1, results.count());
		assertEquals(row2, results.get(0));
		
		// test if clearing value will work correctly
		row2.setText(null);
		ec.saveChanges();
		
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.isNull(FirstEntity.TEXT_KEY));
		assertEquals(2, results.count());
	}
	
	public void test06_queryingTextValues() {
		FirstEntity row1 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		FirstEntity row2 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		NSArray<FirstEntity> results;
		
		row1.setText("aaa");
		row2.setText("abc");

		ec.saveChanges();
		ec.invalidateAllObjects();

		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.equals(FirstEntity.TEXT_KEY, "aaa"));
		assertEquals(1, results.count());
		assertEquals(row1, results.get(0));

		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.greaterThan(FirstEntity.TEXT_KEY, "aaa"));
		assertEquals(1, results.count());
		assertEquals(row2, results.get(0));
		
		// now verify handling quote characters
		row1.setText("a\"b");
		ec.saveChanges();
		
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.equals(FirstEntity.TEXT_KEY, "a\"b"));
		assertEquals(1, results.count());
		assertEquals(row1, results.get(0));
	}
	
	public void test07_queryingTextValuesLikeOperator() {
		FirstEntity row1 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		FirstEntity row2 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		NSArray<FirstEntity> results;
		
		row1.setText("aaa");
		row2.setText("Aaa");

		ec.saveChanges();
		ec.invalidateAllObjects();

		// case sensitive
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.like(FirstEntity.TEXT_KEY, "aa*"));
		assertEquals(1, results.count());
		assertEquals(row1, results.get(0));
		
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.like(FirstEntity.TEXT_KEY, "Aa*"));
		assertEquals(1, results.count());
		assertEquals(row2, results.get(0));
		
		// case insensitive
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.likeInsensitive(FirstEntity.TEXT_KEY, "AA*"));
		assertEquals(2, results.count());
	}

	public void test08_queryingTextValuesAsciiLikeOperator() {
		FirstEntity row1 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		FirstEntity row2 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		NSArray<FirstEntity> results;
		
		row1.setText("aóa");
		row2.setText("Aóa");

		ec.saveChanges();
		ec.invalidateAllObjects();

		// case sensitive
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.matches(FirstEntity.TEXT_KEY, "a.+a"));
		assertEquals(1, results.count());
		assertEquals(row1, results.get(0));
		
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.matches(FirstEntity.TEXT_KEY, "A[oó]a"));
		assertEquals(1, results.count());
		assertEquals(row2, results.get(0));
	}
	
	public void test09_queryingBooleanValues() {
		FirstEntity row1 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		FirstEntity row2 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		NSArray<FirstEntity> results;
		
		row1.setBool(true);
		row2.setBool(false);

		ec.saveChanges();
		ec.invalidateAllObjects();

		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.isTrue(FirstEntity.BOOL_KEY));
		assertEquals(1, results.count());
		assertEquals(row1, results.get(0));
		
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.isFalse(FirstEntity.BOOL_KEY));
		assertEquals(1, results.count());
		assertEquals(row2, results.get(0));
		
	}
	
	public void test10_queryingIntegerValues() {
		FirstEntity row1 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		FirstEntity row2 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		NSArray<FirstEntity> results;
		
		row1.setNumber(19);
		row2.setNumber(2);
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.greaterThan(FirstEntity.NUMBER_KEY, 10));
		assertEquals(1, results.count());
		assertEquals(row1, results.get(0));
		
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.greaterThan(FirstEntity.NUMBER_KEY, 19));
		assertEquals(0, results.count());
		
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.greaterThanOrEqualTo(FirstEntity.NUMBER_KEY, 19));
		assertEquals(1, results.count());
		assertEquals(row1, results.get(0));
		
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.greaterThanOrEqualTo(FirstEntity.NUMBER_KEY, 20));
		assertEquals(0, results.count());
	}
	
	public void test11_queryingTimestampValues() throws InterruptedException {
		FirstEntity row1 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		FirstEntity row2 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		NSTimestamp ts1;
		NSTimestamp ts2;
		NSArray<FirstEntity> results;
		
		ts1  = new NSTimestamp();
		Thread.sleep(3);
		ts2 = new NSTimestamp();

		row1.setTimestamp(ts1);
		row2.setTimestamp(ts2);
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.equals(FirstEntity.TIMESTAMP_KEY, ts1));
		assertEquals(1, results.count());
		assertEquals(row1, results.get(0));

		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.greaterThan(FirstEntity.TIMESTAMP_KEY, ts1));
		assertEquals(1, results.count());
		assertEquals(row2, results.get(0));
	}
	
	public void test12_storingRelationships() {
		FirstEntity row1 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		SecondEntity row2 = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		NSArray<FirstEntity> results;
		NSArray<SecondEntity> results2;
		
		row1.setSecondEntityRelationship(row2);
		
		row1.setText("r1");
		row2.setNumber(17);
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		results = fetchAll(FirstEntity.ENTITY_NAME);
		assertEquals(1, results.count());
		assertEquals(row2, results.get(0).secondEntity());
		
		assertEquals(1, row2.firstEntities().count());
		
		results2 = fetchAll(SecondEntity.ENTITY_NAME);
		assertEquals(1, results.count());
		assertEquals(1, results2.get(0).firstEntities().count());
		assertEquals(row1, results2.get(0).firstEntities().get(0));
	}

	public void test13_updatingRelationships() {
		FirstEntity row1 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		SecondEntity row2a = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		SecondEntity row2b = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		NSArray<FirstEntity> results;

		row1.setSecondEntityRelationship(row2a);
		
		row1.setText("r1");
		row2a.setNumber(17);
		row2b.setNumber(20);
		
		ec.saveChanges();
		
		assertEquals(1, row2a.firstEntities().count());
		
		
		// perform updates
		
		row1.setSecondEntityRelationship(row2b);
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		assertEquals(0, row2a.firstEntities().count());
		
		// start n=node:types("type:FirstEntity"), n_secondEntityId=node(4) match n-[?]->x, n-[:secondEntityId]->n_secondEntityId where 1=1 return n.id
		assertEquals(1, row2b.firstEntities().count());
		
		results = fetchAll(FirstEntity.ENTITY_NAME);
		assertEquals(1, results.count());
		assertEquals(row2b, results.get(0).secondEntity());
	}
	
	public void test14_queryingSimpleAttributesByToOneRelationships() {
		FirstEntity row1 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		SecondEntity row2 = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		NSArray<FirstEntity> results;
		
		row1.setSecondEntityRelationship(row2);
		
		row2.setNumber(17);
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.equals(path(FirstEntity.SECOND_ENTITY_KEY, SecondEntity.NUMBER_KEY), 17));
		assertEquals(1, results.count());
		assertEquals(row1, results.get(0));
	}

	public void test15_queryingEOObjectsByToOneRelationships() {
		FirstEntity row1a = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		@SuppressWarnings("unused")
		FirstEntity row1b = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		SecondEntity row2 = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		ThirdEntity row3 = ERXEOControlUtilities.createAndInsertObject(ec, ThirdEntity.class);
//		NSArray<FirstEntity> results;
		NSArray<ThirdEntity> results3;
		
		row1a.setSecondEntityRelationship(row2);
		row2.setThirdEntityRelationship(row3);
		row3.setFirstEntityRelationship(row1a);
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		results3 = fetch(ThirdEntity.ENTITY_NAME, ERXQ.equals(path(ThirdEntity.FIRST_ENTITY_KEY, FirstEntity.SECOND_ENTITY_KEY), row2));
		assertEquals(1, results3.count());
		assertEquals(row3, results3.get(0));
	}
	
	public void test16_queryingSimpleAttributesByOneToManyRelationships() {
		FirstEntity row1 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		SecondEntity row2 = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		NSArray<SecondEntity> results;
		
		row1.setText("aaa");
		row1.setSecondEntityRelationship(row2);
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		results = fetch(SecondEntity.ENTITY_NAME, ERXQ.equals(path(SecondEntity.FIRST_ENTITIES_KEY, FirstEntity.TEXT_KEY), "aaa"));
		assertEquals(1, results.count());
		assertEquals(row2, results.get(0));
	}

	public void test17_queryingEOObjectsByOneToManyRelationships() {
		FirstEntity row1a = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		@SuppressWarnings("unused")
		FirstEntity row1b = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		SecondEntity row2 = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		NSArray<FirstEntity> results;
		
		row1a.setSecondEntityRelationship(row2);
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		results = fetch(FirstEntity.ENTITY_NAME, 
				ERXQ.equals(path(FirstEntity.SECOND_ENTITY_KEY, SecondEntity.FIRST_ENTITIES_KEY), row1a));
		assertEquals(1, results.count());

		results = fetch(FirstEntity.ENTITY_NAME, 
				ERXQ.equals(path(FirstEntity.SECOND_ENTITY_KEY, SecondEntity.FIRST_ENTITIES_KEY, FirstEntity.SECOND_ENTITY_KEY, SecondEntity.FIRST_ENTITIES_KEY), row1a));
		assertEquals(1, results.count());
	}
	
	public void test18_deleting() {
		FirstEntity row1 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		SecondEntity row2 = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		NSArray<SecondEntity> results;
		
		row1.setSecondEntityRelationship(row2);
		
		ec.saveChanges();
		
		ec.deleteObject(row1);
		ec.saveChanges();
		
		ec.invalidateAllObjects();

		assertEquals(0, row2.firstEntities().size());
		
		results = fetchAll(FirstEntity.ENTITY_NAME);
		assertEquals(0, results.count());
	}

	public void test20_queryNullRelationshipValue() {
		FirstEntity row1a = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		FirstEntity row1b = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		SecondEntity row2 = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		NSArray<FirstEntity> results;
		
		row1b.setSecondEntityRelationship(row2);
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.equals(FirstEntity.SECOND_ENTITY_KEY, null));
		assertEquals(1, results.count());
		assertEquals(row1a, results.get(0));
	}
	
	public void test21_insertJoinEntity_explicit() {
		FirstEntity first = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		SecondEntity second = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		
		// explicit create Join row
		Join join = ERXEOControlUtilities.createAndInsertObject(ec, Join.class);
		
		join.setFirstEntityRelationship(first);
		join.setSecondEntityRelationship(second);
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		NSArray<FirstEntity> results = fetchAll(FirstEntity.ENTITY_NAME);
		assertEquals("Expecting only one row", 1, results.count());
		first = results.get(0);
		assertEquals(1, first.joins().count());
		assertEquals(second, first.joins().get(0).secondEntity());

		NSArray<SecondEntity> results2 = fetchAll(SecondEntity.ENTITY_NAME);
		assertEquals("Expecting only one row", 1, results2.count());
		second = results2.get(0);
		assertEquals(1, second.joins().count());
		assertEquals(first, second.joins().get(0).firstEntity());
		
	}

	public void test22_insertJoinEntity_implicit() {
		FirstEntity first = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		SecondEntity second = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		
		// implicit create Join row
		first.addToJoins_secondEntityRelationship(second);
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		NSArray<FirstEntity> results = fetchAll(FirstEntity.ENTITY_NAME);
		assertEquals("Expecting only one row", 1, results.count());
		first = results.get(0);
		assertEquals(1, first.joins().count());
		assertEquals(second, first.joins().get(0).secondEntity());

		NSArray<SecondEntity> results2 = fetchAll(SecondEntity.ENTITY_NAME);
		assertEquals("Expecting only one row", 1, results2.count());
		second = results2.get(0);
		assertEquals(1, second.joins().count());
		assertEquals(first, second.joins().get(0).firstEntity());
		
	}
	
	public void test23_insertFlattenedManyToManyRelationship() {
		FirstEntity first = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		SecondEntity second = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		
		first.addToJoins_secondEntityRelationship(second);
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		NSArray<SecondEntity> results2 = fetchAll(SecondEntity.ENTITY_NAME);
		assertEquals("Expecting only one row", 1, results2.count());
		second = results2.get(0);
		assertEquals(1, second.joins_firstEntity().count());
		assertEquals(first, second.joins_firstEntity().get(0));
		
		NSArray<FirstEntity> results = fetchAll(FirstEntity.ENTITY_NAME);
		assertEquals("Expecting only one row", 1, results.count());
		first = results.get(0);
		assertEquals(1, first.joins_secondEntity().count());
		assertEquals(second, first.joins_secondEntity().get(0));
	}
	
	public void test24_flattenedKeyPathAccess() {
		FirstEntity first = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		SecondEntity second = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		ThirdEntity third = ERXEOControlUtilities.createAndInsertObject(ec, ThirdEntity.class);
		Join join = ERXEOControlUtilities.createAndInsertObject(ec, Join.class);
		NSArray<?> results;
		
		join.setFirstEntityRelationship(first);
		join.setSecondEntityRelationship(second);
	
		first.setSecondEntityRelationship(second);
		second.setThirdEntityRelationship(third);
		third.setFirstEntityRelationship(first);
		
		// add some fake objects
		ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		ERXEOControlUtilities.createAndInsertObject(ec, ThirdEntity.class);
		
		ec.saveChanges();
		
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.isNotNull(path(FirstEntity.JOINS_SECOND_ENTITY_KEY, SecondEntity.THIRD_ENTITY_KEY)));
		assertEquals(1, results.count());
		assertEquals(first, results.get(0));
		
		results = fetch(ThirdEntity.ENTITY_NAME, ERXQ.isNotNull(path(ThirdEntity.FIRST_ENTITY_KEY, FirstEntity.JOINS_SECOND_ENTITY_KEY)));
		assertEquals(1, results.count());
		assertEquals(third, results.get(0));
	}
	
	public void test25_sortOrdering() {
		for (int i=0; i<20; i++) {
			FirstEntity first = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);

			first.setNumber((int) (Math.random() * 100));
		}
		// add one with null number value
		ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		
		NSArray<FirstEntity> results;
		ec.saveChanges();
		
		// fetch in descending order
		results = fetchAll(
				FirstEntity.ENTITY_NAME,
				new NSArray<EOSortOrdering>(EOSortOrdering.sortOrderingWithKey(FirstEntity.NUMBER_KEY, EOSortOrdering.CompareDescending))
				);
	
		Integer previous = Integer.MAX_VALUE;
		
		for (FirstEntity fe : results) {
			if (previous == null) {
				assertNull(fe.number());
			} else {
				assertTrue(fe.number() == null || previous >= fe.number());
				previous = fe.number();
			}
		}
		
		// fetch in ascending order
		results = fetchAll(
				FirstEntity.ENTITY_NAME,
				new NSArray<EOSortOrdering>(EOSortOrdering.sortOrderingWithKey(FirstEntity.NUMBER_KEY, EOSortOrdering.CompareAscending))
				);
	
		previous = Integer.MIN_VALUE;
		
		for (FirstEntity fe : results) {
			if (previous == null) {
				assertNull(fe.number());
			} else {
				assertTrue(fe.number() == null || previous <= fe.number());
				previous = fe.number();
			}
		}
	}
	
	public void test26_lockingOnUpdateUponRelationshipUpdate() {
		FirstEntity first = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);

		ec.saveChanges();
		
		SecondEntity second = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		
		first.setSecondEntityRelationship(second);
		
		ec.saveChanges();
		
		first.setText("asdasd");
		
		// when FirstEntity.secondEntityId is used for locking then it sometimes fails here
		ec.saveChanges();
	}
	
	public void test27_updatingToManyRelationships() {
		FirstEntity first = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);		// Event
		SecondEntity second = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);	// Programme
		
		first.setSecondEntityRelationship(second);	// event.setProgramme(programme)
		
		ec.saveChanges();
		ec.forgetObject(second);
		second = (SecondEntity) fetchAll(SecondEntity.ENTITY_NAME).get(0);
		
		assertFalse(second.firstEntities().isEmpty());	// programme.events().isEmpty()
	}
	

	public void test28_deletingJoinRelationship() {
		FirstEntity first = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		SecondEntity second = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		
		// explicit create Join row
		Join join = ERXEOControlUtilities.createAndInsertObject(ec, Join.class);
		
		join.setFirstEntityRelationship(first);
		join.setSecondEntityRelationship(second);
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		NSArray<Join> results = fetchAll(Join.ENTITY_NAME);
		assertEquals("Expecting only one row", 1, results.count());
		
		join = results.get(0);
		
		ec.deleteObject(join);
		
		ec.saveChanges();
		
		// ensure there's no relationship between First and Second anymore
		NSArray<FirstEntity> results2 = fetchAll(FirstEntity.ENTITY_NAME);
		first = results2.get(0);
		assertEquals(0, first.joins().count());
		assertTrue(first.joins_secondEntity().isEmpty());
	}
	
	// tests discovered bug
	public void test29_flattenedRelationship() {
		FirstEntity f0 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		SecondEntity s1 = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		
		s1.addToFirstEntitiesRelationship(f0);
		
		Join j2 = ERXEOControlUtilities.createAndInsertObject(ec, Join.class);
		
		j2.setSecondEntityRelationship(s1);
		
		FirstEntity f3 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		
		j2.setFirstEntityRelationship(f3);
		
		ThirdEntity t4 = ERXEOControlUtilities.createAndInsertObject(ec, ThirdEntity.class);
		
		t4.setFirstEntityRelationship(f3);
		
		ec.saveChanges();
		ec.invalidateAllObjects();
		
		// basic stuff, not using any flattened relationship
		assertEquals(1, t4.firstEntity().joins().get(0).secondEntity().firstEntities().size());
		
		assertEquals(1, t4.firstEntity().joins_secondEntity().size());
		assertEquals(1, t4.firstEntity().joins_secondEntity_firstEntities().size());
		
		// problematic parts
		assertEquals(1, t4.firstEntity_joins().size());
		assertEquals(1, t4.firstEntity_joins_secondEntity().size());
		assertEquals(1, t4.firstEntity_joins_secondEntity_firstEntities().size());
		
	}
	

	public void test30_queryingFloatValues() {
		FirstEntity row1 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		FirstEntity row2 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		NSArray<FirstEntity> results;
		
		row1.setFloatNumber(19f);
		row2.setFloatNumber(2f);
		
		ec.saveChanges();
		ec.invalidateAllObjects();

		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.greaterThan(FirstEntity.FLOAT_NUMBER_KEY, 10f));
		assertEquals(1, results.count());
		assertEquals(row1, results.get(0));
		
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.greaterThan(FirstEntity.FLOAT_NUMBER_KEY, 19f));
		assertEquals(0, results.count());
		
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.greaterThanOrEqualTo(FirstEntity.FLOAT_NUMBER_KEY, 19f));
		assertEquals(1, results.count());
		assertEquals(row1, results.get(0));
		
		results = fetch(FirstEntity.ENTITY_NAME, ERXQ.greaterThanOrEqualTo(FirstEntity.FLOAT_NUMBER_KEY, 20f));
		assertEquals(0, results.count());
	}
	
	/**
	 * Test for bug discovered when fetching using AND qualifier with foreign key equality check and less than comparison 
	 */
	public void test31_byPrimaryKeyFilterTest() {
		FirstEntity f1 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		FirstEntity f2 = ERXEOControlUtilities.createAndInsertObject(ec, FirstEntity.class);
		SecondEntity s = ERXEOControlUtilities.createAndInsertObject(ec, SecondEntity.class);
		NSArray<FirstEntity> results;
		
		f1.setNumber(1);
		// 2 is not used
		f2.setNumber(3);
		
		f1.setSecondEntityRelationship(s);
		f2.setSecondEntityRelationship(s);
		
		ec.saveChanges();
		
		results = fetch(
				FirstEntity.ENTITY_NAME,
				ERXQ.and(
						ERXQ.equals(FirstEntity.SECOND_ENTITY_KEY, s),
						ERXQ.greaterThan(FirstEntity.NUMBER_KEY, 1)
				)
		);
		
		assertEquals(1, results.size());
	}
}
