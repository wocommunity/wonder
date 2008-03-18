import java.util.LinkedList;
import java.util.List;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.ERXConstant;

public class JSONExample extends com.webobjects.appserver.WOComponent {
	private JSONProxy _proxy;
	private NSMutableArray<Person> _people;

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
		_people = new NSMutableArray<Person>();
		Person mike = new Person("Mike", 29);
		Person kirsten = new Person("Kirsten", 29);
		Person andrew = new Person("Andrew", 2);
		mike.setSpouse(kirsten);
		kirsten.setSpouse(mike);
		mike.setChildren(new NSArray<Person>(andrew));
		kirsten.setChildren(new NSArray<Person>(andrew));
		
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

		public Person onePerson() {
			return _people.objectAtIndex(0);
		}
		
		private List<SimplePerson> simpleData;
		
		public List simpleData() {
			if (simpleData == null) {
				simpleData = new LinkedList<SimplePerson>();
				SimplePerson p = new SimplePerson("Mike", 29);
				simpleData.add(p);
				simpleData.add(p);
			}
			return simpleData;
		}
		
		public boolean verifySimpleData(List data) {
			return simpleData.equals(data); 
		}

		private NSArray<Object> someData = new NSArray<Object>(
				// AK: new json doesn't handle this
				new Object[] { TEST1, TEST2, TEST1, new Integer(0), "test", new NSTimestamp(), NSPropertyListSerialization.propertyListFromString("<0000c0a8004a0000d2f5480400000113c81c0584c55806fa>"
				) });

		public NSArray someData() {
			return someData;
		}

		public boolean verifyData(NSArray value) {
			System.out.println("JSONExample.verifyData: \nactual:   " + value + "\nexpected: " + someData);
			return value.equals(someData);
		}

		public NSArray people() {
			return _people;
		}
	}
}