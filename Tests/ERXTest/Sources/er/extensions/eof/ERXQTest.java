package er.extensions.eof;

import junit.framework.TestCase;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOGenericRecord;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;


public class ERXQTest extends TestCase {

	public void testExtractKeyValueQualifiers() {
		EOQualifier qualifier = null;
		NSArray<EOKeyValueQualifier> found = ERXQ.extractKeyValueQualifiers(qualifier);
		assertNotNull(found);
		assertTrue(found.isEmpty());
		
		qualifier = ERXQ.is("anyKey", "a");
		found = ERXQ.extractKeyValueQualifiers(qualifier);
		assertNotNull(found);
		assertEquals(1, found.count());
		assertEquals(qualifier, found.get(0));
		
		EOQualifier notQualifier = ERXQ.not(qualifier);
		found = ERXQ.extractKeyValueQualifiers(notQualifier);
		assertNotNull(found);
		assertEquals(1, found.count());
		assertEquals(qualifier, found.get(0));
		
		EOQualifier andQualifier = ERXQ.and(qualifier, notQualifier);
		found = ERXQ.extractKeyValueQualifiers(andQualifier);
		assertNotNull(found);
		assertEquals(2, found.count());
		assertTrue(found.contains(qualifier));
		
		EOQualifier orQualifier = ERXQ.or(qualifier, notQualifier, andQualifier);
		found = ERXQ.extractKeyValueQualifiers(orQualifier);
		assertNotNull(found);
		assertEquals(4, found.count());
		assertTrue(found.contains(qualifier));
	}
	
	public void testReplaceQualifierWithQualifier() {
		EOQualifier oldQualifier = ERXQ.is("age", "99");
		EOQualifier qualifier = ERXQ.and(ERXQ.contains("name", "o"), ERXQ.or(oldQualifier, ERXQ.is("haircolor", "black")));
		EOQualifier newQualifier = ERXQ.is("age", "100");
		NSArray data = NSPropertyListSerialization.arrayForString("({\"age\"=\"99\"; \"name\"=\"John\"; \"haircolor\"=\"brown\";},"
				+ "{\"age\"=\"100\"; \"name\"=\"Robert\"; \"haircolor\"=\"brown\";})");
		
		NSArray filtered = ERXQ.filtered(data, qualifier);
		assertEquals(1, filtered.count());
		assertEquals("99", ((NSDictionary)filtered.get(0)).valueForKey("age"));
		
		EOQualifier replacedQualifier = ERXQ.replaceQualifierWithQualifier(qualifier, oldQualifier, newQualifier);
		assertNotSame(qualifier, replacedQualifier);
		
		NSArray replacedFiltered = ERXQ.filtered(data, replacedQualifier);
		assertEquals(1, replacedFiltered.count());
		assertEquals("100", ((NSDictionary)replacedFiltered.get(0)).valueForKey("age"));
	}

	public void testMatchingValues() {

		EOQualifier qualifier = ERXQ.matchingValues("name", "Bob");
		assertNotNull(qualifier);
		assertTrue(qualifier instanceof EOKeyValueQualifier);

                qualifier = ERXQ.matchingValues("firstName", "Bob", "lastName", "Smith");
                assertNotNull(qualifier);
                assertTrue(qualifier instanceof EOAndQualifier && ((EOAndQualifier)qualifier).qualifiers().size() == 2);

		qualifier = ERXQ.matchingValues(qualifier, "firstName", "Bob");
                assertNotNull(qualifier);
                assertTrue(qualifier instanceof EOAndQualifier && ((EOAndQualifier)qualifier).qualifiers().size() == 2);

		qualifier = ERXQ.matchingValues(new ERXKey<String>("firstName"), "Bob");
                assertNotNull(qualifier);
                assertTrue(qualifier instanceof EOKeyValueQualifier);

		NSMutableDictionary values = new NSMutableDictionary();
		values.setObjectForKey("firstName", "Bob");
		values.setObjectForKey("lastName", "Smith");

		qualifier = ERXQ.matchingValues("friendOf", "John", values);
                assertNotNull(qualifier);
                assertTrue(qualifier instanceof EOAndQualifier && ((EOAndQualifier)qualifier).qualifiers().size() == 3);

                qualifier = ERXQ.matchingValues(values, "friendOf", "John");
                assertNotNull(qualifier);
                assertTrue(qualifier instanceof EOAndQualifier && ((EOAndQualifier)qualifier).qualifiers().size() == 3);
	}

	public void testOne() {
		NSArray<EOGenericRecord> persons = personArray();
		EOGenericRecord firstElement = persons.get(0);
		
		EOGenericRecord result = ERXQ.one(persons, ERXQ.is("firstName", "Bill"));
		assertNotNull(result);
		assertEquals(firstElement, result);

		result = ERXQ.one(persons, ERXQ.is("firstName", "Billl"));
		assertNull(result);

		result = ERXQ.one(null, ERXQ.is("firstName", "Bill"));
		assertNull(result);
		
		result = ERXQ.one(NSArray.<EOGenericRecord>emptyArray(), ERXQ.is("firstName", "Bill"));
		assertNull(result);

		try {
			result = ERXQ.one(persons, null);
			fail("missing IllegalStateException");
		} catch (IllegalStateException e) {
			// got expected exception
		}

		try {
			result = ERXQ.one(persons, ERXQ.is("lastName", "Smith"));
			fail("missing IllegalStateException");
		} catch (IllegalStateException e) {
			// got expected exception
		}
	}

	public void testFirst() {
		NSArray<EOGenericRecord> persons = personArray();
		EOGenericRecord firstElement = persons.get(0);
		
		EOGenericRecord result = ERXQ.first(persons, ERXQ.is("firstName", "Bill"));
		assertNotNull(result);
		assertEquals(firstElement, result);

		result = ERXQ.first(persons, ERXQ.is("firstName", "Billl"));
		assertNull(result);

		result = ERXQ.first(null, ERXQ.is("firstName", "Bill"));
		assertNull(result);
		
		result = ERXQ.first(NSArray.<EOGenericRecord>emptyArray(), ERXQ.is("firstName", "Bill"));
		assertNull(result);

		result = ERXQ.first(persons, null);
		assertNotNull(result);
		assertEquals(firstElement, result);
		
		result = ERXQ.first(persons, ERXQ.is("lastName", "Smith"));
		assertNotNull(result);
		assertEquals(firstElement, result);
	}

	private NSArray<EOGenericRecord> personArray() {
		NSMutableArray<EOGenericRecord> persons = new NSMutableArray<>();
		persons.add(createPerson("Bill", "Smith"));
		persons.add(createPerson("Bob", "Anything"));
		persons.add(createPerson("John", "Smith"));
		persons.add(createPerson("Peter", "Pan"));
		return persons.immutableClone();
	}

	private EOGenericRecord createPerson(String firstName, String lastName) {
		EOClassDescription classDescription = EOClassDescription.classDescriptionForEntityName("Person");
		EOGenericRecord person = new EOGenericRecord(classDescription);
		person.takeValueForKey(firstName, "firstName");
		person.takeValueForKey(lastName, "lastName");
		return person;
	}
}
