package wowodc.background.appserver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wowodc.background.components.Main;
import wowodc.background.rest.controllers.TaskInfoController;
import wowodc.eof.TaskInfo;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSLog;

import er.extensions.appserver.ERXApplication;
import er.rest.routes.ERXRoute;
import er.rest.routes.ERXRouteRequestHandler;

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
	  ERXRouteRequestHandler restHandler = new ERXRouteRequestHandler();
	  restHandler.addRoute(new ERXRoute(TaskInfo.ENTITY_NAME, "/taskInfos", ERXRoute.Method.Post, TaskInfoController.class, "create"));
	  restHandler.addRoute(new ERXRoute(TaskInfo.ENTITY_NAME, "/taskInfos/{taskInfo:TaskInfo}", ERXRoute.Method.Get, TaskInfoController.class, "show"));
	  restHandler.addRoute(new ERXRoute(TaskInfo.ENTITY_NAME, "/taskInfos/{taskInfo:TaskInfo}/results", ERXRoute.Method.Get, TaskInfoController.class, "results"));
	  registerRequestHandler(restHandler, ERXRouteRequestHandler.Key);	
	  NSLog.out.appendln(restHandler.routes());
	}
	
	/**
	 * @return shared standard ExecutorService provided by {@link Executors#newCachedThreadPool()}
	 */
	public static ExecutorService plainExecutorService() {
		return PLAIN_EXECUTOR_SERVICE.LAZY_OBJECT;
	}
	
	@Override
	public WOResponse handleSessionRestorationErrorInContext(WOContext aContext) {
		return pageWithName(Main.class, aContext).generateResponse();
	}
}
