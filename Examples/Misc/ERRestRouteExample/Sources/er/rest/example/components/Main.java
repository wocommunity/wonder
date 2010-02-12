package er.rest.example.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;
import er.rest.routes.ERXRouteRequestHandler;

public class Main extends ERXComponent {
	public Main(WOContext context) {
		super(context);
	}
	
	protected String url(String path) { 
		context()._generateCompleteURLs();
		String url = context().urlWithRequestHandlerKey(ERXRouteRequestHandler.Key, path, null);
		context()._generateRelativeURLs();
		return url;
	}
	
	public String personXmlURL() {
		return url("Person/1.xml");
	}
	
	public String personPlistURL() {
		return url("Person/1.plist");
	}
	
	public String personJsonURL() {
		return url("Person/1.json");
	}
	
	public String companyURL() {
		return url("Company/2.plist");
	}
}
