package er.modern.movies.test;

import er.extensions.appserver.ERXSession;

public class Session extends ERXSession {
	private static final long serialVersionUID = 1L;

	private MoviesNavigationController _navController;
	
	public Session() {
	}
	
	public MoviesNavigationController navController() {
		if (_navController == null) {
			_navController = new MoviesNavigationController(this);
		}
		return _navController;
	}
}
