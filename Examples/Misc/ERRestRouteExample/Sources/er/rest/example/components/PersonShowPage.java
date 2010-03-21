package er.rest.example.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;
import er.rest.example.model.Person;
import er.rest.routes.ERXRouteParameter;
import er.rest.routes.IERXRouteComponent;

/**
 * Because PersonController has automatic HTML routing enabled, we didn't have to do anything special to
 * have a /Person/1.html URL route to this page. It has to implement IERXRouteComponent as a security
 * precaution.
 * 
 * @author mschrag
 */
public class PersonShowPage extends ERXComponent implements IERXRouteComponent {
	private Person _person;

	public PersonShowPage(WOContext context) {
		super(context);
	}

	/**
	 * By specifying this method is an ERXRouteParameter, the route controller will automatically
	 * bind the value of the "person" router parameter to this method.
	 *  
	 * @param person
	 */
	@ERXRouteParameter
	public void setPerson(Person person) {
		_person = person;
	}

	public Person person() {
		return _person;
	}
}