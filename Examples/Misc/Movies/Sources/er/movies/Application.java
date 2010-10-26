package er.movies;


import webobjectsexamples.businesslogic.movies.common.Movie;
import er.extensions.appserver.ERXApplication;
import er.movies.rest.MoviesController;
import er.rest.routes.ERXRoute;
import er.rest.routes.ERXRouteRequestHandler;
import er.taggable.ERTaggableEntity;

public class Application extends ERXApplication {
	public static void main(String[] argv) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		ERXApplication.log.info("Welcome to " + name() + " !");
		ERTaggableEntity.registerTaggable(Movie.ENTITY_NAME);
		
		ERXRouteRequestHandler restReqHandler = new ERXRouteRequestHandler();
		
		restReqHandler.addDefaultRoutes(Movie.ENTITY_NAME);
		restReqHandler.insertRoute(new ERXRoute(Movie.ENTITY_NAME,"/movies", ERXRoute.Method.Options,MoviesController.class, "options"));
		
		ERXRouteRequestHandler.register(restReqHandler);
	}
}
