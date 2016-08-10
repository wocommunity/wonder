import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class InPlaceExample extends WOComponent {
	public String _manualValue;
	public String _value;
	public String _firstName;
	public String _lastName;
	public String _phoneNumber;

	public InPlaceExample(WOContext context) {
		super(context);
		_value = "ExampleInPlace Value";
		_manualValue = "ExampleInPlaceManual Value";
		_firstName = "Johnny";
		_lastName = "Ajax";
		_phoneNumber = "800-555-1212";
	}

	public WOActionResults valueSaved() {
		System.out.println("InPlaceExample.valueSaved: saved!");
		return null;
	}

	public WOActionResults valueCancelled() {
		System.out.println("InPlaceExample.valueCancelled: cancelled!");
		return null;
	}
}
