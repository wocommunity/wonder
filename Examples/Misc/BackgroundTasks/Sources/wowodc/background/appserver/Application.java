package wowodc.background.appserver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wowodc.background.components.Main;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXApplication;

public class Application extends ERXApplication {
	
	// Lazy static initialization of shared plain ExecutorService
	// You could just do this for an application specific standard thread pool
	// where you do not want to extend Project Wonder
	private static class PLAIN_EXECUTOR_SERVICE {
		static ExecutorService LAZY_OBJECT = Executors.newCachedThreadPool();
	}
	
	public static void main(String[] argv) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		ERXApplication.log.info("Welcome to " + name() + " !");
		/* ** put your initialization code in here ** */
	}
	
	/**
	 * @return shared standard ExecutorService provided by {@link Executors#newCachedThreadPool()}
	 */
	public static ExecutorService plainExecutorService() {
		return PLAIN_EXECUTOR_SERVICE.LAZY_OBJECT;
	}
	
	@Override
	public WOResponse handleSessionRestorationErrorInContext(WOContext aContext) {
		return pageWithName(Main.class.getName(), aContext).generateResponse();
	}
	
	
}
