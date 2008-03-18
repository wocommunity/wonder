
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public class JSONExample extends com.webobjects.appserver.WOComponent {
	private JSONProxy _proxy;
	private NSMutableArray<Person> _people;

	public JSONExample(WOContext context) {
		super(context);
		_proxy = new JSONProxy();
		_people = new NSMutableArray<Person>();
		_people.add(new Person("Mike", 29));
		_people.add(new Person("Andrew", 2));
		_people.add(new Person("Kirsten", 29));
		_people.add(new Person("Jonathan", 36));
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

		public NSArray people() {
			return _people;
		}
	}
}