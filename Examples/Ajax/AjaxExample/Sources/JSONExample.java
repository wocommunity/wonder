import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSTimestamp;

import er.ajax.example.Company;
import er.ajax.example.ComplexPerson;
import er.ajax.example.ExampleDataFactory;
import er.ajax.example.SimplePerson;
import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXEC;

public class JSONExample extends com.webobjects.appserver.WOComponent {
	private JSONProxy _proxy;
	private NSArray<ComplexPerson> _people;

	private static class Constant extends ERXConstant.StringConstant {

		public Constant(String value, String name) {
			super(value, name);
		}
	}
	
	private static Constant  TEST1 = new Constant("test1", "Test 1");
	private static Constant  TEST2 = new Constant("test2", "Test 2");
	
	public JSONExample(WOContext context) {
		super(context);
		_proxy = new JSONProxy();
		_people = ExampleDataFactory.family();
	}

	public JSONProxy proxy() {
		return _proxy;
	}

	public class JSONProxy {
		public void printSomething() {
			System.out.println("JSONExample.printSomething: SOMETHING!");
		}

		public ComplexPerson onePerson() {
			return _people.objectAtIndex(0);
		}
		
		private NSArray<SimplePerson> simpleData;
		
		public NSArray simpleData() {
			if (simpleData == null) {
				simpleData = new NSMutableArray<SimplePerson>();
				SimplePerson p = new SimplePerson("Mike", 29);
				simpleData.add(p);
				simpleData.add(p);
			}
			return simpleData;
		}
		
		public boolean verifySimpleData(NSArray data) {
			return simpleData.equals(data); 
		}
		
		public NSArray eoData() {
			EOEditingContext editingContext = ERXEC.newEditingContext();
			NSArray<Company> originalCompanies = ExampleDataFactory.companies(editingContext);
			NSMutableArray<Company> companiesWithDupes = new NSMutableArray<Company>();
			companiesWithDupes.addObjectsFromArray(originalCompanies);
			companiesWithDupes.addObjectsFromArray(originalCompanies);
			return companiesWithDupes;
		}
		
		public boolean verifyEOData(NSArray data) {
			// MS: not the best check in the world here, but good enough for what we're testing for
			boolean verified = false;
			if (data != null && data.count() == 2 * Company.fetchAllCompanies(ERXEC.newEditingContext()).count()) {
				verified = true;
				for (Object o : data) {
					if (o == null) {
						System.out.println("JSONProxy.verifyEOData: o");
						verified = false;
					}
				}
			}
			return verified; 
		}

		private NSArray<Object> complexData = new NSArray<Object>(
				// AK: new json doesn't handle this
				new Object[] { TEST1, TEST2, TEST1, Integer.valueOf(0), "test", new NSTimestamp(), NSPropertyListSerialization.propertyListFromString("<0000c0a8004a0000d2f5480400000113c81c0584c55806fa>"
				) });

		public NSArray complexData() {
			return complexData;
		}

		public boolean verifyComplexData(NSArray value) {
			System.out.println("JSONExample.verifyData: \nactual:   " + value + "\nexpected: " + complexData);
			return value.equals(complexData);
		}

		public NSArray people() {
			return _people;
		}
	}
}
