package er.rest.example.controllers;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXRestFetchSpecification;
import er.rest.example.model.Company;
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
		Person person = routeObjectForKey("person");
		return person;
	}

	public static ERXKeyFilter queryFilter() {
		ERXKeyFilter filter = ERXKeyFilter.filterWithAllRecursive();
		return filter;
	}

	/**
	 * This shows adding a derived attribute into the filter results.
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
		filter.include(Person.COMPANY).includeAttributes(); // let you update a company inside of a person
		return filter;
	}
	
	@Override
	public WOActionResults createAction() {
		Person person = create(updateFilter());
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

	/**
	 * lockedUpdate is just like update except that the "company" relationships is locked, which means
	 * that you can update the object on the other side of the relationship, but you can't change the
	 * related object itself.
	 */
	public WOActionResults lockedUpdateAction() {
		Person person = person();
		ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
		filter.include(Person.COMPANY).includeAttributes(); // let you update a company inside of a person
		filter.lockRelationship(Person.COMPANY); // don't let you change the company relationship
		update(person, filter);
		editingContext().saveChanges();
		return response(person, showFilter());
	}

	/**
	 * securityUpdate is just like a regular update except that it will not let you change the person's
	 * company name to Microsoft when updating the Person using the ERXKeyFilter.Delegate API
	 */
	public WOActionResults securityUpdateAction() {
		Person person = person();
		ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
		filter.include(Person.COMPANY).includeAttributes(); // let you update a company inside of a person
		filter.setDelegate(new ERXKeyFilter.Delegate() {
			public void willTakeValueForKey(Object target, Object value, String key) throws SecurityException {
				if (target instanceof Company && "name".equals(key) && value != null && ((String)value).contains("Microsoft")) {
					throw new SecurityException("You can't change a Person's company name to Microsoft.");
				}
			}
			
			public void didTakeValueForKey(Object target, Object value, String key) throws SecurityException {
			}
			
			public void didSkipValueForKey(Object target, Object value, String key) throws SecurityException {
			}
		});
		update(person, filter);
		editingContext().saveChanges();
		return response(person, showFilter());
	}
	
	@Override
	public WOActionResults destroyAction() throws Throwable {
		Person person = person();
		person.delete();
		editingContext().saveChanges();
		return response(person, showFilter());
	}

	@Override
	public WOActionResults newAction() throws Throwable {
		Person person = Person.createPerson(editingContext(), "New Person");
		return response(person, showFilter());
	}

	@Override
	public WOActionResults showAction() {
		return response(person(), showFilter());
	}

	@Override
	public WOActionResults indexAction() {
		if (isSchemaRequest()) {
			return schemaResponse(showFilter());
		}
		ERXRestFetchSpecification<Person> fetchSpec = new ERXRestFetchSpecification<>(Person.ENTITY_NAME, null, null, queryFilter(), Person.NAME.ascs(), 25);
		return response(fetchSpec, showFilter());
	}
}
