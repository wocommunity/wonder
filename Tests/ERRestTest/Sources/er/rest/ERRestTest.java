package er.rest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXKeyFilter;
import er.memoryadaptor.EREntityStoreFactory;
import er.rest.format.ERXRestFormat;
import er.rest.format.ERXRestFormatDelegate;
import er.rest.format.ERXXmlRestParser;
import er.rest.format.ERXXmlRestWriter;
import er.rest.model.Car;
import er.rest.model.Company;
import er.rest.model.Manufacturer;
import er.rest.model.Person;
import er.rest.utils.ERXEONoIdRestDelegate;

public class ERRestTest extends TestCase {
    private EOObjectStoreCoordinator _osc;

    @Override
    public void setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        NSTimeZone.setDefaultTimeZone(NSTimeZone.timeZoneWithName("UTC", true));
        System.setProperty("NSProjectBundleEnabled", "true");
        System.setProperty("NSPropertiesInitializationWarning", "false");
        NSBundle.mainBundle();

        // register special that excludes the id attribute for EOs
        IERXRestDelegate.Factory.setDefaultDelegate(new ERXEONoIdRestDelegate());

        EOObjectStoreCoordinator osc = new EOObjectStoreCoordinator();
        // EOEditingContext editingContext = ERXEC.newEditingContext(osc);
        // editingContext.lock();
        // try {
        // for (int i = 0; i < 3; i++) {
        // Company c = Company.createCompany(editingContext, "Company " + i);
        // for (int j = 0; j < 3; j++) {
        // Person p = Person.createPerson(editingContext, "Person " + i + "/" + j);
        // p.setCompanyRelationship(c);
        // p.setAge(Integer.valueOf(10 * i + j));
        // p.setSalary(Double.valueOf(10 * i + j * 1000));
        // }
        // }
        //
        // Person p = Person.createPerson(editingContext, "Standalone Person");
        // p.setAge(Integer.valueOf(20));
        // p.setSalary(Double.valueOf(100000));
        //
        // Person.createPerson(editingContext, "Null All Person");
        // editingContext.saveChanges();
        // }
        // finally {
        // editingContext.unlock();
        // editingContext.dispose();
        // }
        _osc = osc;
    }

    @Override
    protected void tearDown() throws Exception {
        NSNotificationCenter.defaultCenter().postNotification(EREntityStoreFactory.RESET_ALL_ENTITIES, this);
        _osc.dispose();
        super.tearDown();
    }

    public void testFormats() {
        assertEquals(ERXRestFormat.JSON_KEY, ERXRestFormat.json().name());
        assertEquals(ERXRestFormat.PLIST_KEY, ERXRestFormat.plist().name());
        assertEquals(ERXRestFormat.XML_KEY, ERXRestFormat.xml().name());
    }

    public void testSimpleRestWriterFormat() {
        String output = ERXRestFormat.html().toString("Test");
        assertEquals("[String=Test]", output);
    }

    public void testDictionaryToJSON() {
        NSMutableDictionary<String, Object> dict = new NSMutableDictionary<>();
        dict.setObjectForKey("Mike", "Name");
        String output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"Name\":\"Mike\"}\n", output);

        Object parsedDict = ERXRestFormat.json().parse(output).createObjectWithFilter(null, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext());
        assertEquals(dict, parsedDict);
    }

    public void testDictionaryToPlist() {
        NSMutableDictionary<String, Object> dict = new NSMutableDictionary<>();
        dict.setObjectForKey("Mike", "Name");
        String output = ERXRestFormat.plist().toString(dict);
        assertEquals("{\n\t\"Name\" = \"Mike\";\n}\n", output);

        Object parsedDict = ERXRestFormat.plist().parse(output).createObjectWithFilter(null, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext());
        assertEquals(dict, parsedDict);
    }

    public void testDictionaryToXML() {
        NSMutableDictionary<String, Object> dict = new NSMutableDictionary<>();
        dict.setObjectForKey("Mike", "Name");
        String output = ERXRestFormat.xml().toString(dict);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<NSDictionary type=\"NSDictionary\">\n  <Name>Mike</Name>\n</NSDictionary>\n", output);
    }

    public void testXMLToDictionary() {
        NSDictionary<String, Object> dict = new NSDictionary<>("Mike", "Name");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><NSDictionary type=\"NSDictionary\"><Name>Mike</Name></NSDictionary>";
        Object result = ERXRestFormat.xml().parse(xml).createObjectWithFilter(null, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext());
        assertEquals(dict, result);
    }

    public void testNullToJSON() {
        String output = ERXRestFormat.json().toString(null);
        assertEquals("undefined\n", output);

        ERXRestRequestNode parsedNode = ERXRestFormat.json().parse(null);
        assertTrue(parsedNode.isNull());
        assertNull(parsedNode.type());
        Object parsedObject = parsedNode.createObjectWithFilter(null, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext());
        assertNull(parsedObject);
    }

    public void testPrimitiveArrayToJSON() {
        String output = ERXRestFormat.json().toString(null, new NSArray<>(new String[] { "a", "b", "c" }), ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext());
        assertEquals("[\"a\",\"b\",\"c\"]\n", output);
    }

    public void testJSONToPrimitiveArray() {
        NSArray<String> array = new NSArray<>(new String[] { "a", "b", "c" });
        String json = "[\"a\",\"b\",\"c\"]";
        Object result = ERXRestFormat.json().parse(json).createObjectWithFilter(null, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext());
        assertEquals(array, result);
    }

    public void testPrimitiveToJSON() {
        String output = ERXRestFormat.json().toString(Integer.valueOf(100));
        assertEquals("100\n", output);

        // MS: apparently you can't parse a single primitive value
        // ERXRestRequestNode parsedNode = ERXRestFormat.json().parse(output);
        ERXRestRequestNode parsedNode = new ERXRestRequestNode();
        parsedNode.setValue(Integer.valueOf(100));
        assertEquals(Integer.valueOf(100), parsedNode.value());
        Object parsedObject = parsedNode.createObjectWithFilter(null, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext());
        assertEquals(Integer.valueOf(100), parsedObject);
    }

    public void testPrimitivesToJSON() {
        NSDictionary<String, Object> dict = new NSDictionary<>("Mike", "String");
        String output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"String\":\"Mike\"}\n", output);

        dict = new NSDictionary<>(Integer.valueOf(32), "int");
        output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"int\":32}\n", output);

        dict = new NSDictionary<>(Boolean.TRUE, "boolean");
        output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"boolean\":true}\n", output);

        dict = new NSDictionary<>(Long.valueOf(100000000000L), "long");
        output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"long\":100000000000}\n", output);

        dict = new NSDictionary<>(Short.valueOf((short) 100), "short");
        output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"short\":100}\n", output);

        dict = new NSDictionary<>(Float.valueOf(100.5f), "float");
        output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"float\":100.5}\n", output);

        dict = new NSDictionary<>(Double.valueOf(100.5), "double");
        output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"double\":100.5}\n", output);

        dict = new NSDictionary<>(new NSTimestamp(1301584117085L), "timestamp");
        output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"timestamp\":\"2011-03-31T15:08:37Z\"}\n", output);

        dict = new NSDictionary<>(new Date(1301584117085L), "date");
        output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"date\":\"2011-03-31T15:08:37Z\"}\n", output);

        dict = new NSDictionary<>(NSKeyValueCoding.NullValue, "nullValue");
        output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"nullValue\":null}\n", output);

        dict = new NSDictionary<>(LocalDate.of(2016, Month.AUGUST, 1), "localDate");
        output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"localDate\":\"2016-08-01\"}\n", output);

        dict = new NSDictionary<>(LocalDateTime.of(2016, Month.AUGUST, 1, 12, 5, 8), "localDateTime");
        output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"localDateTime\":\"2016-08-01T12:05:08\"}\n", output);

        dict = new NSDictionary<>(LocalTime.of(12, 5, 8), "localTime");
        output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"localTime\":\"12:05:08\"}\n", output);

        dict = new NSDictionary<>(OffsetDateTime.of(2016, 8, 1, 12, 5, 8, 0, ZoneOffset.UTC), "offsetDateTime");
        output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"offsetDateTime\":\"2016-08-01T12:05:08Z\"}\n", output);
    }

    public void testJSONToPrimitives() {
        ERXKeyFilter filter = ERXKeyFilter.filterWithAllRecursive();
        ERXRestContext context = new ERXRestContext();
        
        NSDictionary<String, Object> dict = new NSDictionary<>("Mike", "String");
        String json = "{\"String\":\"Mike\"}";
        Object result = ERXRestFormat.json().parse(json).createObjectWithFilter(null, filter, context);
        assertEquals(dict, result);

        dict = new NSDictionary<>(Integer.valueOf(32), "int");
        json = "{\"int\":32}";
        result = ERXRestFormat.json().parse(json).createObjectWithFilter(null, filter, context);
        assertEquals(dict, result);

        dict = new NSDictionary<>(Boolean.TRUE, "boolean");
        json = "{\"boolean\":true}";
        result = ERXRestFormat.json().parse(json).createObjectWithFilter(null, filter, context);
        assertEquals(dict, result);

        dict = new NSDictionary<>(Long.valueOf(100000000000L), "long");
        json = "{\"long\":100000000000}";
        result = ERXRestFormat.json().parse(json).createObjectWithFilter(null, filter, context);
        assertEquals(dict, result);

        dict = new NSDictionary<>(Short.valueOf((short) 100), "short");
        json = "{\"short\":100}";
        result = ERXRestFormat.json().parse(json).createObjectWithFilter(null, filter, context);
        //assertEquals(dict, result); // value is interpreted as Integer instead of Short
        assertEquals(new NSDictionary<>(Integer.valueOf(100), "short"), result);

        dict = new NSDictionary<>(Float.valueOf(100.5f), "float");
        json = "{\"float\":100.5}";
        result = ERXRestFormat.json().parse(json).createObjectWithFilter(null, filter, context);
        //assertEquals(dict, result); // value is interpreted as Double instead of Float
        assertEquals(new NSDictionary<>(Double.valueOf(100.5), "float"), result);

        dict = new NSDictionary<>(Double.valueOf(100.5), "double");
        json = "{\"double\":100.5}";
        result = ERXRestFormat.json().parse(json).createObjectWithFilter(null, filter, context);
        assertEquals(dict, result);

        dict = new NSDictionary<>(new NSTimestamp(1301584117085L), "timestamp");
        json = "{\"timestamp\":\"2011-03-31T15:08:37Z\"}";
        result = ERXRestFormat.json().parse(json).createObjectWithFilter(null, filter, context);
        //assertEquals(dict, result); // String value is not interpreted as date

        dict = new NSDictionary<>(new Date(1301584117085L), "date");
        json = "{\"date\":\"2011-03-31T15:08:37Z\"}";
        result = ERXRestFormat.json().parse(json).createObjectWithFilter(null, filter, context);
        //assertEquals(dict, result); // String value is not interpreted as date

        dict = new NSDictionary<>(NSKeyValueCoding.NullValue, "nullValue");
        json = "{\"nullValue\":null}";
        result = ERXRestFormat.json().parse(json).createObjectWithFilter(null, filter, context);
        assertEquals(new NSDictionary<>(), result); // null values are dropped

        dict = new NSDictionary<>(LocalDate.of(2016, Month.AUGUST, 1), "localDate");
        json = "{\"localDate\":\"2016-08-01\"}";
        result = ERXRestFormat.json().parse(json).createObjectWithFilter(null, filter, context);
        //assertEquals(dict, result); // String value is not interpreted as date

        dict = new NSDictionary<>(LocalDateTime.of(2016, Month.AUGUST, 1, 12, 5, 8), "localDateTime");
        json = "{\"localDateTime\":\"2016-08-01T12:05:08\"}";
        result = ERXRestFormat.json().parse(json).createObjectWithFilter(null, filter, context);
        //assertEquals(dict, result); // String value is not interpreted as date

        dict = new NSDictionary<>(LocalTime.of(12, 5, 8), "localTime");
        json = "{\"localTime\":\"12:05:08\"}";
        result = ERXRestFormat.json().parse(json).createObjectWithFilter(null, filter, context);
        //assertEquals(dict, result); // String value is not interpreted as date

        dict = new NSDictionary<>(OffsetDateTime.of(2016, 8, 1, 12, 5, 8, 0, ZoneOffset.UTC), "offsetDateTime");
        json = "{\"offsetDateTime\":\"2016-08-01T12:05:08Z\"}";
        result = ERXRestFormat.json().parse(json).createObjectWithFilter(null, filter, context);
        //assertEquals(dict, result); // String value is not interpreted as date
    }

    public void testArrayOfStringsToJSON() {
        NSMutableDictionary<String, Object> dict = new NSMutableDictionary<>();
        dict.setObjectForKey(new NSArray<String>(new String[] { "a", "b", "c" }), "array");
        String output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"array\":[\"a\",\"b\",\"c\"]}\n", output);

        Object parsedDict = ERXRestFormat.json().parse(output).createObjectWithFilter(null, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext());
        assertEquals(dict, parsedDict);
    }

    public void testArrayOfIntegersToJSON() {
        NSMutableDictionary<String, Object> dict = new NSMutableDictionary<>();
        dict.setObjectForKey(new NSArray<Integer>(new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) }), "array");
        String output = ERXRestFormat.json().toString(dict);
        assertEquals("{\"array\":[1,2,3]}\n", output);

        Object parsedDict = ERXRestFormat.json().parse(output).createObjectWithFilter(null, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext());
        assertEquals(dict, parsedDict);
    }

    // MS: plist in WO quotes everything, even numbers ... it's pretty messed up
    public void testPrimitivesToPlist() {
        NSDictionary<String, Object> dict = new NSDictionary<>("Mike", "String");
        String output = ERXRestFormat.plist().toString(dict);
        assertEquals("{\n\t\"String\" = \"Mike\";\n}\n", output);

        dict = new NSDictionary<>(Integer.valueOf(32), "int");
        output = ERXRestFormat.plist().toString(dict);
        assertEquals("{\n\t\"int\" = \"32\";\n}\n", output);

        dict = new NSDictionary<>(Boolean.TRUE, "boolean");
        output = ERXRestFormat.plist().toString(dict);
        assertEquals("{\n\t\"boolean\" = \"true\";\n}\n", output);

        dict = new NSDictionary<>(Long.valueOf(100000000000L), "long");
        output = ERXRestFormat.plist().toString(dict);
        assertEquals("{\n\t\"long\" = \"100000000000\";\n}\n", output);

        dict = new NSDictionary<>(Short.valueOf((short) 100), "short");
        output = ERXRestFormat.plist().toString(dict);
        assertEquals("{\n\t\"short\" = \"100\";\n}\n", output);

        dict = new NSDictionary<>(Float.valueOf(100.5f), "float");
        output = ERXRestFormat.plist().toString(dict);
        assertEquals("{\n\t\"float\" = \"100.5\";\n}\n", output);

        dict = new NSDictionary<>(Double.valueOf(100.5), "double");
        output = ERXRestFormat.plist().toString(dict);
        assertEquals("{\n\t\"double\" = \"100.5\";\n}\n", output);

        dict = new NSDictionary<>(new NSTimestamp(1301584117085L), "timestamp");
        output = ERXRestFormat.plist().toString(dict);
        assertEquals("{\n\t\"timestamp\" = \"2011-03-31 15:08:37 Etc/GMT\";\n}\n", output);

        dict = new NSDictionary<>(new Date(1301584117085L), "date");
        output = ERXRestFormat.plist().toString(dict);
        assertEquals("{\n\t\"date\" = \"Thu Mar 31 15:08:37 UTC 2011\";\n}\n", output);

        dict = new NSDictionary<>(NSKeyValueCoding.NullValue, "nullValue");
        output = ERXRestFormat.plist().toString(dict);
        assertEquals("{\n\t\"nullValue\" = \"<com.webobjects.foundation.NSKeyValueCoding$Null>\";\n}\n", output);

        dict = new NSDictionary<>(LocalDate.of(2016, Month.AUGUST, 1), "localDate");
        output = ERXRestFormat.plist().toString(dict);
        assertEquals("{\n\t\"localDate\" = \"2016-08-01\";\n}\n", output);

        dict = new NSDictionary<>(LocalDateTime.of(2016, Month.AUGUST, 1, 12, 5, 8), "localDateTime");
        output = ERXRestFormat.plist().toString(dict);
        assertEquals("{\n\t\"localDateTime\" = \"2016-08-01T12:05:08\";\n}\n", output);

        dict = new NSDictionary<>(LocalTime.of(12, 5, 8), "localTime");
        output = ERXRestFormat.plist().toString(dict);
        assertEquals("{\n\t\"localTime\" = \"12:05:08\";\n}\n", output);

        dict = new NSDictionary<>(OffsetDateTime.of(2016, 8, 1, 12, 5, 8, 0, ZoneOffset.UTC), "offsetDateTime");
        output = ERXRestFormat.plist().toString(dict);
        assertEquals("{\n\t\"offsetDateTime\" = \"2016-08-01T12:05:08Z\";\n}\n", output);
    }

    public void testPrimitivesToXML() {
        NSDictionary<String, Object> dict = new NSDictionary<>("Mike", "String");
        String output = ERXRestFormat.xml().toString(dict);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<NSDictionary type=\"NSDictionary\">\n  <String>Mike</String>\n</NSDictionary>\n", output);

        dict = new NSDictionary<>(Integer.valueOf(32), "int");
        output = ERXRestFormat.xml().toString(dict);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<NSDictionary type=\"NSDictionary\">\n  <int type = \"integer\">32</int>\n</NSDictionary>\n", output);

        dict = new NSDictionary<>(Boolean.TRUE, "boolean");
        output = ERXRestFormat.xml().toString(dict);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<NSDictionary type=\"NSDictionary\">\n  <boolean type = \"boolean\">true</boolean>\n</NSDictionary>\n", output);

        dict = new NSDictionary<>(Long.valueOf(100000000000L), "long");
        output = ERXRestFormat.xml().toString(dict);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<NSDictionary type=\"NSDictionary\">\n  <long type = \"long\">100000000000</long>\n</NSDictionary>\n", output);

        dict = new NSDictionary<>(Short.valueOf((short) 100), "short");
        output = ERXRestFormat.xml().toString(dict);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<NSDictionary type=\"NSDictionary\">\n  <short type = \"short\">100</short>\n</NSDictionary>\n", output);

        dict = new NSDictionary<>(Float.valueOf(100.5f), "float");
        output = ERXRestFormat.xml().toString(dict);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<NSDictionary type=\"NSDictionary\">\n  <float type = \"float\">100.5</float>\n</NSDictionary>\n", output);

        dict = new NSDictionary<>(Double.valueOf(100.5), "double");
        output = ERXRestFormat.xml().toString(dict);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<NSDictionary type=\"NSDictionary\">\n  <double type = \"double\">100.5</double>\n</NSDictionary>\n", output);

        dict = new NSDictionary<>(new NSTimestamp(1301584117000L), "timestamp"); // MS: originally was 1301584117085L, but timestamps lose their millis
        output = ERXRestFormat.xml().toString(dict);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<NSDictionary type=\"NSDictionary\">\n  <timestamp type = \"datetime\">2011-03-31T15:08:37Z</timestamp>\n</NSDictionary>\n", output);

        dict = new NSDictionary<>(new Date(1301584117000L), "date");
        output = ERXRestFormat.xml().toString(dict);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<NSDictionary type=\"NSDictionary\">\n  <date type = \"datetime\">2011-03-31T15:08:37Z</date>\n</NSDictionary>\n", output);

        dict = new NSDictionary<>(NSKeyValueCoding.NullValue, "nullValue");
        output = ERXRestFormat.xml().toString(dict);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<NSDictionary type=\"NSDictionary\">\n  <nullValue nil=\"true\"/>\n</NSDictionary>\n", output);
    }

    public void testXMLToPrimitives() {
        ERXKeyFilter filter = ERXKeyFilter.filterWithAllRecursive();
        ERXRestContext context = new ERXRestContext();

        NSDictionary<String, Object> dict = new NSDictionary<>("Mike", "String");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><NSDictionary type=\"NSDictionary\"><String>Mike</String></NSDictionary>";
        Object result = ERXRestFormat.xml().parse(xml).createObjectWithFilter(null, filter, context);
        assertEquals(dict, result);

        dict = new NSDictionary<>(Integer.valueOf(32), "int");
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><NSDictionary type=\"NSDictionary\"><int type = \"integer\">32</int></NSDictionary>";
        result = ERXRestFormat.xml().parse(xml).createObjectWithFilter(null, filter, context);
        assertEquals(dict, result);

        dict = new NSDictionary<>(Boolean.TRUE, "boolean");
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><NSDictionary type=\"NSDictionary\"><boolean type = \"boolean\">true</boolean></NSDictionary>";
        result = ERXRestFormat.xml().parse(xml).createObjectWithFilter(null, filter, context);
        assertEquals(dict, result);

        dict = new NSDictionary<>(Long.valueOf(100000000000L), "long");
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><NSDictionary type=\"NSDictionary\"><long type = \"long\">100000000000</long></NSDictionary>";
        result = ERXRestFormat.xml().parse(xml).createObjectWithFilter(null, filter, context);
        assertEquals(dict, result);

        dict = new NSDictionary<>(Short.valueOf((short) 100), "short");
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><NSDictionary type=\"NSDictionary\"><short type = \"short\">100</short></NSDictionary>";
        result = ERXRestFormat.xml().parse(xml).createObjectWithFilter(null, filter, context);
        assertEquals(dict, result);

        dict = new NSDictionary<>(Float.valueOf(100.5f), "float");
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><NSDictionary type=\"NSDictionary\"><float type = \"float\">100.5</float></NSDictionary>";
        result = ERXRestFormat.xml().parse(xml).createObjectWithFilter(null, filter, context);
        assertEquals(dict, result);

        dict = new NSDictionary<>(Double.valueOf(100.5), "double");
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><NSDictionary type=\"NSDictionary\"><double type = \"double\">100.5</double></NSDictionary>";
        result = ERXRestFormat.xml().parse(xml).createObjectWithFilter(null, filter, context);
        assertEquals(dict, result);

        dict = new NSDictionary<>(new NSTimestamp(1301584117000L), "timestamp"); // MS: originally was 1301584117085L, but timestamps lose their millis
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><NSDictionary type=\"NSDictionary\"><timestamp type = \"datetime\">2011-03-31T15:08:37Z</timestamp></NSDictionary>";
        result = ERXRestFormat.xml().parse(xml).createObjectWithFilter(null, filter, context);
        assertEquals(dict, result);

        dict = new NSDictionary<>(new Date(1301584117000L), "date");
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><NSDictionary type=\"NSDictionary\"><date type = \"datetime\">2011-03-31T15:08:37Z</date></NSDictionary>";
        result = ERXRestFormat.xml().parse(xml).createObjectWithFilter(null, filter, context);
        assertEquals(dict, result);

        dict = new NSDictionary<>(NSKeyValueCoding.NullValue, "nullValue");
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><NSDictionary type=\"NSDictionary\"><nullValue nil=\"true\"/></NSDictionary>";
        result = ERXRestFormat.xml().parse(xml).createObjectWithFilter(null, filter, context);
        assertEquals(new NSDictionary<>(), result); // MS: we don't get an NSNull back, because dictionary.takeValueForKey(.. null) removes the key ...
    }

    public void testEOWithAttributesFilterToJSON() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            Person p = Person.createPerson(editingContext, "Mike");
            assertEquals("{\"type\":\"Person\",\"age\":null,\"name\":\"Mike\",\"salary\":null}\n", ERXRestFormat.json().toString(p, ERXKeyFilter.filterWithAttributes()));
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testPluralNamesFormat() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            NSArray<Person> ps = new NSArray<>(Person.createPerson(editingContext, "Mike"));
            ERXRestFormat format = new ERXRestFormat("xml", new ERXXmlRestParser(), new ERXXmlRestWriter(), new ERXRestFormatDelegate("id", "type", "nil", true, true, true, true, true));
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<people type=\"array\">\n" + "  <person type=\"person\">\n" + "    <age nil=\"true\"/>\n" + "    <name>Mike</name>\n" + "    <salary nil=\"true\"/>\n" + "  </person>\n" + "</people>\n", format.toString(EOClassDescription.classDescriptionForEntityName(Person.ENTITY_NAME), ps, ERXKeyFilter.filterWithAttributes(), new ERXRestContext(editingContext)));

            Person p = Person.createPerson(editingContext, "Mike");
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<person type=\"person\">\n" + "  <age nil=\"true\"/>\n" + "  <name>Mike</name>\n" + "  <salary nil=\"true\"/>\n" + "</person>\n", format.toString(p, ERXKeyFilter.filterWithAttributes(), new ERXRestContext(editingContext)));
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testCustomIdKeyFormat() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            Car c = Car.cars().objectAtIndex(0);
            ERXRestFormat format = new ERXRestFormat("json", new ERXXmlRestParser(), new ERXXmlRestWriter(), new ERXRestFormatDelegate("CUSTOMID", "type", "nil", true, true, true, true, true));
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<car CUSTOMID=\"Cooper S\" type=\"car\">\n" + "  <name>Cooper S</name>\n" + "</car>\n", format.toString(c, ERXKeyFilter.filterWithAttributes(), new ERXRestContext(editingContext)));
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testCustomNilAndTypeKeysFormat() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            ERXRestFormat format = new ERXRestFormat("json", new ERXXmlRestParser(), new ERXXmlRestWriter(), new ERXRestFormatDelegate("id", "CUSTOM_TYPE", "CUSTOM_NIL", true, true, true, true, true));
            Person p = Person.createPerson(editingContext, "Mike");
            String output = format.toString(p, ERXKeyFilter.filterWithAttributes(), new ERXRestContext(editingContext));
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<person CUSTOM_TYPE=\"person\">\n" + "  <age CUSTOM_NIL=\"true\"/>\n" + "  <name>Mike</name>\n" + "  <salary CUSTOM_NIL=\"true\"/>\n" + "</person>\n", output);

            Person parsedPerson = (Person) format.parse(output).createObjectWithFilter(null, ERXKeyFilter.filterWithAttributes(), new ERXRestContext(editingContext));
            assertNotNull(parsedPerson);
            assertEquals("Mike", parsedPerson.name());
            assertNull(parsedPerson.age());
            assertNull(parsedPerson.salary());
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testUnderscoreNamesFormat() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            NSArray<Company> cs = new NSArray<>(Company.createCompany(editingContext, "Mike"));
            ERXRestFormat format = new ERXRestFormat("json", new ERXXmlRestParser(), new ERXXmlRestWriter(), new ERXRestFormatDelegate("id", "type", "nil", true, true, true, true, true));
            ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
            filter.include("nonModelAttribute");
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<companies type=\"array\">\n" + "  <company type=\"company\">\n" + "    <name>Mike</name>\n" + "    <revenue nil=\"true\"/>\n" + "    <non_model_attribute>NonModelAttribute</non_model_attribute>\n" + "  </company>\n" + "</companies>\n", format.toString(EOClassDescription.classDescriptionForEntityName(Company.ENTITY_NAME), cs, filter, new ERXRestContext(editingContext)));
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testNoNilKeysFormat() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            NSArray<Company> cs = new NSArray<>(Company.createCompany(editingContext, "Mike"));
            ERXRestFormat format = new ERXRestFormat("json", new ERXXmlRestParser(), new ERXXmlRestWriter(), new ERXRestFormatDelegate("id", "type", "nil", false, true, true, true, true));
            ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
            filter.include("nonModelAttribute");
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<companies type=\"array\">\n" + "  <company type=\"company\">\n" + "    <name>Mike</name>\n" + "    <revenue/>\n" + "    <non_model_attribute>NonModelAttribute</non_model_attribute>\n" + "  </company>\n" + "</companies>\n", format.toString(EOClassDescription.classDescriptionForEntityName(Company.ENTITY_NAME), cs, filter, new ERXRestContext(editingContext)));
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testNoArrayTypeFormat() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            NSArray<Company> cs = new NSArray<>(Company.createCompany(editingContext, "Mike"));
            ERXRestFormat format = new ERXRestFormat("json", new ERXXmlRestParser(), new ERXXmlRestWriter(), new ERXRestFormatDelegate("id", "type", "nil", false, true, true, false, true));
            ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
            filter.include("nonModelAttribute");
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<companies type=\"company\">\n" + "  <company type=\"company\">\n" + "    <name>Mike</name>\n" + "    <revenue/>\n" + "    <non_model_attribute>NonModelAttribute</non_model_attribute>\n" + "  </company>\n" + "</companies>\n", format.toString(EOClassDescription.classDescriptionForEntityName(Company.ENTITY_NAME), cs, filter, new ERXRestContext(editingContext)));
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testNoTypeKeysFormat() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            NSArray<Company> cs = new NSArray<>(Company.createCompany(editingContext, "Mike"));
            ERXRestFormat format = new ERXRestFormat("json", new ERXXmlRestParser(), new ERXXmlRestWriter(), new ERXRestFormatDelegate("id", "type", "nil", false, true, true, false, false));
            ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
            filter.include("nonModelAttribute");
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<companies>\n" + "  <company>\n" + "    <name>Mike</name>\n" + "    <revenue/>\n" + "    <non_model_attribute>NonModelAttribute</non_model_attribute>\n" + "  </company>\n" + "</companies>\n", format.toString(EOClassDescription.classDescriptionForEntityName(Company.ENTITY_NAME), cs, filter, new ERXRestContext(editingContext)));
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testUnsavedEOs() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            Person p = Person.createPerson(editingContext, "Mike");
            assertEquals("{\"type\":\"Person\",\"age\":null,\"name\":\"Mike\",\"salary\":null,\"company\":null}\n", ERXRestFormat.json().toString(p));
            p.setAge(Integer.valueOf(25));
            assertEquals("{\"type\":\"Person\",\"age\":25,\"name\":\"Mike\",\"salary\":null,\"company\":null}\n", ERXRestFormat.json().toString(p));
            Company c = Company.createCompany(editingContext, "Company");
            assertEquals("{\"type\":\"Company\",\"name\":\"Company\",\"revenue\":null,\"employees\":[]}\n", ERXRestFormat.json().toString(c));
            p.setCompanyRelationship(c);
            assertEquals("{\"type\":\"Person\",\"age\":25,\"name\":\"Mike\",\"salary\":null,\"company\":{\"type\":\"Company\",\"name\":\"Company\",\"revenue\":null,\"employees\":[{\"type\":\"Person\"}]}}\n", ERXRestFormat.json().toString(p));
            assertEquals("{\"type\":\"Company\",\"name\":\"Company\",\"revenue\":null,\"employees\":[{\"type\":\"Person\",\"age\":25,\"name\":\"Mike\",\"salary\":null,\"company\":{\"type\":\"Company\"}}]}\n", ERXRestFormat.json().toString(c));
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testSimpleEOAndJSON() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            Company c = Company.createCompany(editingContext, "Company");
            assertEquals("{\"type\":\"Company\",\"name\":\"Company\",\"revenue\":null,\"employees\":[]}\n", ERXRestFormat.json().toString(c));
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testSimpleEOAndXML() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            Company c = Company.createCompany(editingContext, "Company");
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<Company type=\"Company\">\n" + "  <name>Company</name>\n" + "  <revenue nil=\"true\"/>\n" + "  <employees type=\"Person\">\n" + "  </employees>\n" + "</Company>\n", ERXRestFormat.xml().toString(c));
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testNonEntity() {
        Manufacturer m = Manufacturer.PORSCHE;
        assertEquals("{\"id\":\"Porsche\",\"type\":\"Manufacturer\",\"name\":\"Porsche\"}\n", ERXRestFormat.json().toString(m));
    }

    public void testNonEntityWithRelationship() {
        Car c = Car.cars().lastObject();
        assertEquals("{\"id\":\"Celica\",\"type\":\"Car\",\"name\":\"Celica\",\"manufacturer\":{\"id\":\"Toyota\",\"type\":\"Manufacturer\",\"name\":\"Toyota\"}}\n", ERXRestFormat.json().toString(c));
    }

    public void testEOAndNonModelAttribute() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            Company c = Company.createCompany(editingContext, "Company");
            ERXKeyFilter filter = ERXKeyFilter.filterWithAll();
            filter.include("nonModelAttribute");
            assertEquals("{\"type\":\"Company\",\"name\":\"Company\",\"revenue\":null,\"employees\":[],\"nonModelAttribute\":\"NonModelAttribute\"}\n", ERXRestFormat.json().toString(c, filter));
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testEOAndNonModelRelationship() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            Company c = Company.createCompany(editingContext, "Company");
            ERXKeyFilter filter = ERXKeyFilter.filterWithAll();
            filter.include("nonModelAttribute");
            filter.include("manufacturers");
            assertEquals("{\"type\":\"Company\",\"name\":\"Company\",\"revenue\":null,\"employees\":[],\"nonModelAttribute\":\"NonModelAttribute\",\"manufacturers\":[{\"id\":\"Mini\",\"type\":\"Manufacturer\"},{\"id\":\"Porsche\",\"type\":\"Manufacturer\"},{\"id\":\"Toyota\",\"type\":\"Manufacturer\"}]}\n", ERXRestFormat.json().toString(c, filter));
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testCreateEO() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            Company c = (Company) ERXRestFormat.json().parse("{\"type\":\"Company\",\"name\":\"Company\",\"revenue\":100}").createObjectWithFilter(null, ERXKeyFilter.filterWithAll(), new ERXRestContext(editingContext));
            assertTrue(editingContext.globalIDForObject(c).isTemporary());
            assertEquals("Company", c.name());
            assertEquals(100.0, c.revenue().doubleValue());
            assertEquals(0, c.employees().count());
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testCreateEOAndRelatedEO() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            Company c = (Company) ERXRestFormat.json().parse("{\"type\":\"Company\",\"name\":\"Company\",\"revenue\":100,\"employees\":[{\"type\":\"Person\",\"age\":10,\"name\":\"Mike\",\"salary\":null}]}").createObjectWithFilter(null, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext(editingContext));
            assertTrue(editingContext.globalIDForObject(c).isTemporary());
            assertEquals("Company", c.name());
            assertEquals(100.0, c.revenue().doubleValue());
            NSArray<Person> employees = c.employees();
            assertEquals(1, employees.count());
            Person p = employees.objectAtIndex(0);
            assertTrue(editingContext.globalIDForObject(p).isTemporary());
            assertEquals("Mike", p.name());
            assertEquals(10, p.age().intValue());
            assertEquals(null, p.salary());
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testUpdateEO() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            Company c = Company.createCompany(editingContext, "Company");
            ERXRestFormat.json().parse("{\"name\":\"Company Updated\",\"revenue\":100}]}").updateObjectWithFilter(c, ERXKeyFilter.filterWithAll(), new ERXRestContext(editingContext));
            assertEquals("Company Updated", c.name());
            assertEquals(100.0, c.revenue().doubleValue());
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testUpdateAndCreateRelatedEO() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            Person p = Person.createPerson(editingContext, "Mike");
            Company c = Company.createCompany(editingContext, "Company");
            p.setCompanyRelationship(c);
            editingContext.saveChanges();

            ERXRestFormat.json().parse("{\"name\":\"Mike Updated\",\"company\":{\"name\":\"Company Updated\"}}").updateObjectWithFilter(p, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext(editingContext));
            assertEquals("Mike Updated", p.name());
            assertNotSame(c, p.company());
            assertNotNull(p.company());
            assertFalse(editingContext.deletedObjects().containsObject(c));
            assertTrue(editingContext.globalIDForObject(p.company()).isTemporary());
            assertEquals("Company Updated", p.company().name());
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testUpdateRelatedEO() {
        EOEditingContext editingContext = ERXEC.newEditingContext(_osc);
        editingContext.lock();
        try {
            Person p = Person.createPerson(editingContext, "Mike");
            Company c = Company.createCompany(editingContext, "Company");
            p.setCompanyRelationship(c);

            ERXKeyFilter filter = ERXKeyFilter.filterWithAllRecursive();
            filter.setAnonymousUpdateEnabled(true);

            ERXRestFormat.json().parse("{\"name\":\"Mike Updated\",\"company\":{\"name\":\"Company Updated\"}}").updateObjectWithFilter(p, filter, new ERXRestContext(editingContext));
            assertEquals("Mike Updated", p.name());
            assertSame(c, p.company());
            assertEquals("Company Updated", p.company().name());
        }
        finally {
            editingContext.unlock();
            editingContext.dispose();
        }
    }

    public void testSimpleParse() {
        Object obj = ERXRestFormat.json().parse("{ 'firstName':'Mike' }").createObject();
        assertSame(NSMutableDictionary.class, obj.getClass());
    }

    public void testMap() {
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        LinkedHashMap<String, Object> message = new LinkedHashMap<>();
        message.put("subject", "this is a subject");
        message.put("message", "this is a message");
        message.put("priority", Integer.valueOf(10));
        response.put("message", message);

        ERXKeyFilter messageFilter = new ERXKeyFilter(ERXKeyFilter.Base.Attributes);
        messageFilter.setUnknownKeyIgnored(true);
        messageFilter.include("message.subject");
        messageFilter.include("message.message");
        messageFilter.include("message.priority");
        assertEquals("{\"message\":{\"subject\":\"this is a subject\",\"message\":\"this is a message\",\"priority\":10}}\n", ERXRestFormat.json().toString(response, messageFilter));
        
        response.remove("message");
        assertEquals("{\"nil\":true}\n", ERXRestFormat.json().toString(response, messageFilter));
    }
    
    public void testArrayKeyPaths() {
    	ERXRestRequestNode node = new ERXRestRequestNode();
    	node.takeValueForKey("Mike", "firstName");
        node.takeValueForKeyPath("Apple", "companies[1].name");
        node.takeValueForKeyPath("mDT", "companies[0].name");
        node.takeValueForKeyPath(Integer.valueOf(5), "ages[0]");
        node.takeValueForKeyPath(Integer.valueOf(10), "ages[1]");
        String json = node.toString(ERXRestFormat.json(), new ERXRestContext());
        assertEquals("{\"firstName\":\"Mike\",\"companies\":[{\"name\":\"mDT\"},{\"name\":\"Apple\"}],\"ages\":[5,10]}\n", json);
        
        assertEquals(Integer.valueOf(5), node.valueForKeyPath("ages[0]"));
        assertEquals("Apple", node.valueForKeyPath("companies[1].name"));
    }

    public void testRelationshipKeys() {
        Manufacturer m = Manufacturer.PORSCHE;
        ERXKeyFilter f = ERXKeyFilter.filterWithNone();
        f.include("cars.name");
        assertEquals("{\"id\":\"Porsche\",\"type\":\"Manufacturer\",\"cars\":[{\"id\":\"911\",\"type\":\"Car\",\"name\":\"911\"},{\"id\":\"Cayenne\",\"type\":\"Car\",\"name\":\"Cayenne\"}]}\n", ERXRestFormat.json().toString(m, f));
        assertEquals("{\n" + 
        		"\t\"type\" = \"Manufacturer\";\n" + 
        		"\t\"id\" = \"Porsche\";\n" + 
        		"\t\"cars\" = (\n" + 
        		"\t\t{\n" + 
        		"\t\t\t\"name\" = \"911\";\n" + 
        		"\t\t\t\"type\" = \"Car\";\n" + 
        		"\t\t\t\"id\" = \"911\";\n" + 
        		"\t\t},\n" + 
        		"\t\t{\n" + 
        		"\t\t\t\"name\" = \"Cayenne\";\n" + 
        		"\t\t\t\"type\" = \"Car\";\n" + 
        		"\t\t\t\"id\" = \"Cayenne\";\n" + 
        		"\t\t}\n" + 
        		"\t);\n" + 
        		"}\n", ERXRestFormat.plist().toString(m, f));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        		"<Manufacturer id=\"Porsche\" type=\"Manufacturer\">\n" + 
        		"  <cars type=\"Car\">\n" + 
        		"    <Car id=\"911\" type=\"Car\">\n" + 
        		"      <name>911</name>\n" + 
        		"    </Car>\n" + 
        		"    <Car id=\"Cayenne\" type=\"Car\">\n" + 
        		"      <name>Cayenne</name>\n" + 
        		"    </Car>\n" + 
        		"  </cars>\n" + 
        		"</Manufacturer>\n", ERXRestFormat.xml().toString(m, f));
    }
}
