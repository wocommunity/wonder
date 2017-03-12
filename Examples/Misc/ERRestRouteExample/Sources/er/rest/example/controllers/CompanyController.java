package er.rest.example.controllers;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXRestFetchSpecification;
import er.rest.example.model.Company;
import er.rest.example.model.Person;
import er.rest.routes.ERXRouteController;
import er.rest.routes.jsr311.GET;
import er.rest.routes.jsr311.Path;
import er.rest.routes.jsr311.PathParam;

/**
 * CompanyController is implemented with JSR-311 annotations for all parameters, and adds
 * a custom "employees" route at the bottom that shows JSR-311 annotation for registration.
 *  
 * @author mschrag
 *
 */
public class CompanyController extends ERXRouteController {
	public CompanyController(WORequest request) {
		super(request);
	}

	public static ERXKeyFilter queryFilter() {
		ERXKeyFilter filter = ERXKeyFilter.filterWithAllRecursive();
		return filter;
	}

	public static ERXKeyFilter showFilter() {
		ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
		return filter;
	}

	public static ERXKeyFilter updateFilter() {
		ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
		return filter;
	}

	public WOActionResults createAction() {
		Company company = create(updateFilter());
		editingContext().saveChanges();
		return response(company, showFilter());
	}

	public WOActionResults updateAction(@PathParam("company") Company company) {
		update(company, updateFilter());
		editingContext().saveChanges();
		return response(company, showFilter());
	}

	public WOActionResults destroyAction(@PathParam("company") Company company) throws Throwable {
		company.delete();
		editingContext().saveChanges();
		return response(company, showFilter());
	}

	public WOActionResults showAction(@PathParam("company") Company company) {
		return response(company, showFilter());
	}

	public WOActionResults newAction() {
		Company company = Company.createCompany(editingContext(), "New Company");
		return response(company, showFilter());
	}

	/**
	 * JSR-311 registration of an employees route. This also creates a custom filter that says
	 * that when we show employees through a company, use the showFilter from PersonController,
	 * but remove the company relationship (since you specified a company) and include
	 * the "pets" relationship, but don't include any attributes from pets.
	 * @param company
	 * @return
	 */
	@GET
	@Path("/company/{company:Company}/employees")
	public WOActionResults employeesAction(@PathParam("company") Company company) {
		ERXKeyFilter personFilter = PersonController.showFilter();
		personFilter.exclude(Person.COMPANY);
		personFilter.include(Person.PETS).includeNone();
		return response(company.employees(), personFilter);
	}

	public WOActionResults indexAction() {
		if (isSchemaRequest()) {
			return schemaResponse(showFilter());
		}
		ERXRestFetchSpecification<Company> fetchSpec = new ERXRestFetchSpecification<>(Company.ENTITY_NAME, null, null, queryFilter(), Company.NAME.ascs(), 25);
		return response(fetchSpec, showFilter());
	}
}
