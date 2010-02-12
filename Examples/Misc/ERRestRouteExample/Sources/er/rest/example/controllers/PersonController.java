package er.rest.example.controllers;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXRestFetchSpecification;
import er.rest.example.model.Person;
import er.rest.routes.ERXDefaultRouteController;

/**
 * PersonController shows the non-annotation style of accessing parameters
 * (i.e. using the routeObjectForKey methods)
 * 
 * @author mschrag
 */
public class PersonController extends ERXDefaultRouteController {
	public PersonController(WORequest request) {
		super(request);
	}
	
	/**
	 * Automatic HTML routing means that if someone requests Entity/X/action.html, the
	 * router controller will automatically look for an EntityActionPage component. For 
	 * example, if you go to /Person/1.html, it will look for PersonShowPage. You can
	 * manually handle these as well but checking the current request format and 
	 * choosing the appropriate component to return.
	 */
	@Override
	protected boolean isAutomaticHtmlRoutingEnabled() {
		return true;
	}

	protected Person person() {
		Person person = (Person) routeObjectForKey("person");
		return person;
	}

	public static ERXKeyFilter queryFilter() {
		ERXKeyFilter filter = ERXKeyFilter.filterWithAllRecursive();
		return filter;
	}

	/**
	 * This shows adding a derived attribute into the fitler results.
	 * 
	 * @return
	 */
	public static ERXKeyFilter showFilter() {
		ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
		filter.include(Person.COMPANY).includeAttributes();
		filter.include(Person.PETS).includeAttributes();
		filter.include(new ERXKey<NSTimestamp>("derivedCurrentTime")); // derivedCurrentTime is a non-model method on Person
		return filter;
	}

	public static ERXKeyFilter updateFilter() {
		ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
		filter.include(Person.COMPANY);
		return filter;
	}

	@Override
	public WOActionResults createAction() {
		Person person = (Person) create(Person.ENTITY_NAME, updateFilter());
		editingContext().saveChanges();
		return response(person, showFilter());
	}

	@Override
	public WOActionResults updateAction() {
		Person person = person();
		update(person, updateFilter());
		editingContext().saveChanges();
		return response(person, showFilter());
	}

	@Override
	public WOActionResults destroyAction() throws Throwable {
		Person person = person();
		person.delete();
		editingContext().saveChanges();
		return response(person.primaryKey(), null);
	}

	@Override
	public WOActionResults newAction() throws Throwable {
		return null;
	}

	@Override
	public WOActionResults showAction() {
		return response(person(), showFilter());
	}

	@Override
	public WOActionResults indexAction() {
		ERXRestFetchSpecification<Person> fetchSpec = new ERXRestFetchSpecification<Person>(Person.ENTITY_NAME, null, null, queryFilter(), Person.NAME.ascs(), 25);
		return response(editingContext(), Person.ENTITY_NAME, fetchSpec.objects(editingContext(), options()), showFilter());
	}
}
