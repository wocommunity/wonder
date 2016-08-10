package er.distribution.example.client;

import java.io.IOException;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;

import er.distribution.client.ERClientApplication;
import er.distribution.example.client.eo.Movie;
import er.extensions.eof.ERXEC;

public class Application extends ERClientApplication {

	public static void main(String[] args) {
		Application app = new Application();
		app.connectToServer();
		try {
			
			EOGlobalID userGlobalID = app.distributedObjectStore().login("john", "password");
			if (userGlobalID == null) {
				System.out.println("Authentication failed.");
				System.exit(1);
			}
			
			EOEditingContext ec = ERXEC.newEditingContext();
			NSArray<Movie> movies = Movie.fetchAllMovies(ec);
			for (Movie movie : movies) {
				System.out.println(movie.title());
			}
			
		} finally {
			app.distributedObjectStore().terminateSessionOnServer();
		}
	}
	
	public Application() {
		// do custom initialization here
	}

	@Override
	protected String modelPackageName() {
		return Movie.class.getPackage().getName();
	}

	@Override
	protected void handleNoInstanceAvailable(IOException e) {
		System.out.println(e.getMessage());
		System.exit(1);
	}

	@Override
	protected void handleMissingSession(IOException e) {
		System.out.println(e.getMessage());
		System.exit(1);
	}

}
