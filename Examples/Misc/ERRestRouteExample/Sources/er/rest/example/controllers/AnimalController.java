package er.rest.example.controllers;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXRestFetchSpecification;
import er.rest.example.model.Animal;
import er.rest.routes.ERXDefaultRouteController;

/**
 * AnimalController uses the non-annotation-based approach to routing and accessing objects.
 *  
 * @author mschrag
 */
public class AnimalController extends ERXDefaultRouteController {
	public AnimalController(WORequest request) {
		super(request);
	}

	/**
	 * Returns the value of the "pet" variable from the route.
	 * @return
	 */
	public Animal pet() {
		Animal pet = (Animal) routeObjectForKey("pet");
		return pet;
	}

	/**
	 * The query filter is used in indexAction to control what attributes and relationships
	 * you expose to qualifiers through query parameters
	 * 
	 * @return
	 */
	public static ERXKeyFilter queryFilter() {
		ERXKeyFilter filter = ERXKeyFilter.filterWithAllRecursive();
		return filter;
	}

	/**
	 * This showFilter is used by indexAction and showAction and says to return all attributes
	 * of an Animal as well as the Owner relationship, and for the owner, include all of its attributes. 
	 */
	public static ERXKeyFilter showFilter() {
		ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
		filter.include(Animal.OWNER).includeAttributes();
		return filter;
	}

	/**
	 * The updateFilter us used by updateAction and createAction and says to allow updating any attributes of an
	 * Animal as well as the Owner relationship.
	 * @return
	 */
	public static ERXKeyFilter updateFilter() {
		ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
		filter.include(Animal.OWNER);
		return filter;
	}

	@Override
	public WOActionResults createAction() {
		Animal pet = (Animal) create(Animal.ENTITY_NAME, updateFilter());
		editingContext().saveChanges();
		return response(pet, showFilter());
	}

	@Override
	public WOActionResults updateAction() {
		Animal pet = pet();
		update(pet, updateFilter());
		editingContext().saveChanges();
		return response(pet, showFilter());
	}

	@Override
	public WOActionResults destroyAction() throws Throwable {
		Animal pet = pet();
		pet.delete();
		editingContext().saveChanges();
		return response(pet.primaryKey(), null);
	}

	@Override
	public WOActionResults newAction() throws Throwable {
		return null;
	}

	@Override
	public WOActionResults showAction() {
		return response(pet(), showFilter());
	}

	/**
	 * indexAction uses an ERXRestFetchSpecification, which optionally allows you to expose sorting, qualifying, and batching in query parameters 
	 */
	@Override
	public WOActionResults indexAction() {
		ERXRestFetchSpecification<Animal> fetchSpec = new ERXRestFetchSpecification<Animal>(Animal.ENTITY_NAME, null, null, queryFilter(), Animal.NAME.ascs(), 25);
		return response(editingContext(), Animal.ENTITY_NAME, fetchSpec.objects(editingContext(), options()), showFilter());
	}
}
