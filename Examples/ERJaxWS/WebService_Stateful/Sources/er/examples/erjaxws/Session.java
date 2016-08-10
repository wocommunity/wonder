package er.examples.erjaxws;


import er.extensions.appserver.ERXSession;

public class Session extends ERXSession {
	private static final long serialVersionUID = 1L;

	public Session() {
	}
	
	@Override
	public Application application() {
		return (Application)super.application();
	}
	
	
	public boolean authenticated = false;
}
