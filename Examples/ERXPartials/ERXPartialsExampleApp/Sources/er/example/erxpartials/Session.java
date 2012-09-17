package er.example.erxpartials;

import er.extensions.appserver.ERXSession;

public class Session extends ERXSession {
	private static final long serialVersionUID = 1L;

	private MainNavigationController _navController;

	public Session() {
	}
	
	public MainNavigationController navController()
	{
		if (_navController == null)
		{
			_navController = new MainNavigationController(this);
		}
		return _navController;
	}
}
