package er.cayenne;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;

import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXApplication;

/**
 * Adds Cayenne support to WOApplication, which can be used instead of EOF.
 * 
 * @author john
 *
 */
public abstract class CayenneApplication extends ERXApplication {
	
	private ServerRuntime runtime;
	
	public CayenneApplication() {
		super();
		runtime = createRuntime();
		if (runtime == null) {
			throw new IllegalStateException("Cayenne runtime was not created.");
		}
		ServerRuntime.bindThreadInjector(runtime.getInjector());
	}

	/**
	 * Subclasses need to override this and create and return the runtime by doing something like:
	 * 
	 * return new ServerRuntime("cayenne-MyDomain.xml");
	 * 
	 * @return a org.apache.cayenne.configuration.server.ServerRuntime object
	 */
	protected abstract ServerRuntime createRuntime();

	@Override
	public WOResponse dispatchRequest(WORequest request) {
    	ServerRuntime.bindThreadInjector(runtime.getInjector());
		return super.dispatchRequest(request);
	}
	
	@Override
	public void terminate() {
		runtime.shutdown();
		super.terminate();
	}
	
	public ServerRuntime getRuntime() {
		return runtime;
	}
	
	/**
	 * New ObjectContext instances can be obtained by calling this method from anywhere in the application.
	 * 
	 * @return a org.apache.cayenne.ObjectContext object
	 */
	public ObjectContext newObjectContext() {
		return runtime.getContext();
	}
	
}
