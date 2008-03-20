import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSTimestamp;

import er.ajax.example.Company;
import er.extensions.ERXConstant;
import er.extensions.ERXEC;

public class JSONExample extends com.webobjects.appserver.WOComponent {
	private JSONProxy _proxy;
	private NSMutableArray<ComplexPerson> _people;

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
		_people = new NSMutableArray<ComplexPerson>();
		ComplexPerson mike = new ComplexPerson("Mike", 29);
		ComplexPerson kirsten = new ComplexPerson("Kirsten", 29);
		ComplexPerson andrew = new ComplexPerson("Andrew", 2);
		mike.setSpouse(kirsten);
		kirsten.setSpouse(mike);
		mike.setChildren(new NSArray<ComplexPerson>(andrew));
		kirsten.setChildren(new NSArray<ComplexPerson>(andrew));
		
		_people.add(mike);
		_people.add(kirsten);
		_people.add(andrew);
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
			if (Company.fetchAllCompanies(editingContext).count() == 0) {
				Company.createCompany(editingContext, "Company 1");
				Company.createCompany(editingContext, "Company 2");
				Company.createCompany(editingContext, "Company 3");
				Company.createCompany(editingContext, "Company 4");
				editingContext.saveChanges();
			}
			NSMutableArray companiesWithDupes = new NSMutableArray();
			NSArray originalCompanies = Company.fetchAllCompanies(editingContext);
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
				new Object[] { TEST1, TEST2, TEST1, new Integer(0), "test", new NSTimestamp(), NSPropertyListSerialization.propertyListFromString("<0000c0a8004a0000d2f5480400000113c81c0584c55806fa>"
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