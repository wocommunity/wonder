import org.apache.log4j.Logger;

import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSLog;

import er.ajax.json.JSONRequestHandler;
import er.extensions.appserver.ERXApplication;

public class Application extends ERXApplication {
	static Logger log = Logger.getLogger(Application.class);

	public static void main(String argv[]) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		super();
		log.info("Welcome to " + name() + " !");
		/* ** put your initialization code in here ** */
		setAllowsConcurrentRequestHandling(true);
		setDefaultRequestHandler(requestHandlerForKey(directActionRequestHandlerKey()));
		JSONRequestHandler requestHandler = JSONRequestHandler.register();
		requestHandler.registerService("exampleService", new ExampleService());
	}
	
	@Override
	public WOResponse dispatchRequest(WORequest request) {
		boolean isActionRequest = request.uri().indexOf("/wo/") > -1 || request.uri().indexOf("/wa/") > -1 || request.uri().indexOf("/ajax/") > -1;
		isActionRequest = false;  // Comment this out to enable debug logging
		if (isActionRequest) {
			NSLog.out.appendln("---- start of RR loop ----\n");
			if (request.uri().indexOf("/wo/") > -1) log.info("Received component action request " + request.uri());
			else if (request.uri().indexOf("/wa/") > -1) log.info("Received direct action request " + request.uri());
			else if (request.uri().indexOf("/ajax/") > -1) log.info("Received ajax action request " + request.uri());
			NSLog.out.appendln("form values " + request.formValues());
			
			WOResponse response =  super.dispatchRequest(request);
			
	    	//NSLog.out.appendln("returned response " + response.contentString());
	    	NSLog.out.appendln("returned response headers " + response.headers());
	    	NSLog.out.appendln("\n");
	    	return response;
		}

		return super.dispatchRequest(request);
	}
}
