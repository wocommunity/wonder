package er.movies.rest;

import webobjectsexamples.businesslogic.movies.common.Movie;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXRestFetchSpecification;
import er.rest.format.ERXRestFormat;
import er.rest.routes.ERXDefaultRouteController;

public class MoviesController extends ERXDefaultRouteController {
	
	public MoviesController(WORequest request) {
		super(request);
	}

	@Override
	public WOActionResults createAction() throws Throwable {
		Movie movie = create(showFilter());
		editingContext().saveChanges();
		return response(movie, showFilter());
	}

	@Override
	public WOActionResults destroyAction() throws Throwable {
		Movie movie = routeObjectForKey("movie");
		editingContext().deleteObject(movie);
		editingContext().saveChanges();
		return response(movie, showFilter());
	}

	@Override
	public WOActionResults indexAction() throws Throwable {
		if (isSchemaRequest()) {
			return schemaResponse(showFilter());
		}
		
		ERXRestFetchSpecification<Movie> fetchSpec = new ERXRestFetchSpecification<>(entityName(), null, null, showFilter(), null, 20);
		return response(fetchSpec, showFilter());
	}

	@Override
	public WOActionResults newAction() throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WOActionResults showAction() throws Throwable {
		return response(routeObjectForKey("movie"), showFilter());
	}
	
	@Override
	public WOActionResults updateAction() throws Throwable {
		Movie movie = routeObjectForKey("movie");
		update(movie, showFilter());
		editingContext().saveChanges();
		return response(movie, showFilter());
	}

	protected ERXKeyFilter showFilter() {
		ERXKeyFilter filter = ERXKeyFilter.filterWithNone();
		filter.include(Movie.TITLE);
		filter.include(Movie.RATED);
		filter.include(Movie.POSTER_NAME);
		filter.include(Movie.DATE_RELEASED);
		filter.include(Movie.CATEGORY);

		return filter;
	}

	@Override
	protected ERXRestFormat defaultFormat() {
		return ERXRestFormat.json();
	}
}
