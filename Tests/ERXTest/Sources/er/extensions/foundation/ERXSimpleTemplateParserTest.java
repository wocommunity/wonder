package er.extensions.foundation;

import java.util.Properties;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableDictionary;

import er.erxtest.ERXTestCase;

public class ERXSimpleTemplateParserTest extends ERXTestCase {
	/**
	 * Uses keys that have keyPath separator in them which will invoke NSDictionary flattened key feature in valueForKeyPath.
	 */
	public void testParseTemplatedStringWithNSDictionaryObject() {
		String template = "The @@animal.description@@ brown @@animal.type@@ jumped over the fence";
		
		NSMutableDictionary<String, String> variables = new NSMutableDictionary<String, String>();
		variables.setObjectForKey("LAZY", "animal.description");
		variables.setObjectForKey("FOX", "animal.type");
		
		String result = ERXSimpleTemplateParser.parseTemplatedStringWithObject(template, variables);
		
		assertEquals("The LAZY brown FOX jumped over the fence", result);
	}
	
	/**
	 * Uses keys that have keyPath separator in them and a Properties object
	 */
	public void testParseTemplatedStringWithPropertiesObject() {
		String template = "The @@animal.description@@ brown @@animal.type@@ jumped over the fence";
		
		Properties variables = new Properties();
		variables.setProperty("animal.description", "LAZY");
		variables.setProperty("animal.type", "FOX");
		
		String result = ERXSimpleTemplateParser.parseTemplatedStringWithObject(template, variables);
		
		assertEquals("The LAZY brown FOX jumped over the fence", result);
	}
	
	
	public void testParseTemplatedStringWithNSKeyValueCodingAdditionsObject() {
		String template = "The @@animal.description@@ brown @@animal.type@@ jumped over the fence";
		
		// Anonymous classes
		NSKeyValueCodingAdditions variables = new AnimalHolder();
		
		String result = ERXSimpleTemplateParser.parseTemplatedStringWithObject(template, variables);
		
		assertEquals("The LAZY brown FOX jumped over the fence", result);
	}
	
	public static class Animal implements NSKeyValueCodingAdditions {
		
		public Animal() { }

		public String description() {
			return "LAZY";
		}
		
		public String type() {
			return "FOX";
		}

		@Override
		public Object valueForKey(String paramString) {
			return NSKeyValueCoding.DefaultImplementation.valueForKey(this, paramString);
		}

		@Override
		public void takeValueForKey(Object paramObject, String paramString) {}

		@Override
		public Object valueForKeyPath(String paramString) {
			return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, paramString);
		}

		@Override
		public void takeValueForKeyPath(Object paramObject, String paramString) { }
	}
	
	public static class AnimalHolder implements NSKeyValueCodingAdditions {
		
		public AnimalHolder() { }
		
		@Override
		public Object valueForKey(String paramString) {
			return NSKeyValueCoding.DefaultImplementation.valueForKey(this, paramString);
		}

		@Override
		public void takeValueForKey(Object paramObject, String paramString) { }

		@Override
		public Object valueForKeyPath(String paramString) {
			return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, paramString);
		}

		@Override
		public void takeValueForKeyPath(Object paramObject, String paramString) { }
		
		private Animal _animal;
		
		/** @return KVCA animal object */
		public Animal animal() {
			if ( _animal == null ) {
				_animal = new Animal();
			}
			return _animal;
		}
	}
}
