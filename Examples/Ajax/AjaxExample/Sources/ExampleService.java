
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

import er.ajax.example.ComplexPerson;

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
		NSMutableArray<ComplexPerson> people = new NSMutableArray<>();
		people.add(new ComplexPerson("Mike", 29));
		people.add(new ComplexPerson("Andrew", 2));
		people.add(new ComplexPerson("Kirsten", 29));
		people.add(new ComplexPerson("Jonathan", 36));
		return people;
	}
	
	public void printPerson(ComplexPerson person) {
		System.out.println("ExampleService.printPerson: " + person.getName());
	}
}
