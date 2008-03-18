
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

public class ExampleService {
	public void printThisString(String string) {
		System.out.println("ExampleService.printThisString: " + string);
	}

	public String returnMySessionID(WOSession session) {
		return session.sessionID();
	}

	public String returnThisApplicationName() {
		return WOApplication.application().name();
	}

	public NSTimestamp currentTime() {
		return new NSTimestamp();
	}

	public NSArray somePeople() {
		NSMutableArray<Person> people = new NSMutableArray<Person>();
		people.add(new Person("Mike", 29));
		people.add(new Person("Andrew", 2));
		people.add(new Person("Kirsten", 29));
		people.add(new Person("Jonathan", 36));
		return people;
	}
	
	public void printPerson(Person person) {
		System.out.println("ExampleService.printPerson: " + person.getName());
	}
}
