package er.examples.erjaxws;

import er.examples.erjaxws.ws.impl.CalculatorImpl;
import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.ws.ERJaxWebService;
import er.extensions.appserver.ws.ERJaxWebServiceRequestHandler;

public class Application extends ERXApplication {
	public static void main(String[] argv) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		ERXApplication.log.info("Welcome to " + name() + " !");
		/* ** put your initialization code in here ** */
		setAllowsConcurrentRequestHandling(true);		

        ERJaxWebServiceRequestHandler wsHandler = new ERJaxWebServiceRequestHandler();
        wsHandler.registerWebService("Calculator", new ERJaxWebService<CalculatorImpl>(CalculatorImpl.class));
        this.registerRequestHandler(wsHandler, this.webServiceRequestHandlerKey());

	}
	
	// modify URL to auto open in Browser to show the wsdl 
	@Override
	public String directConnectURL() {
		return super.directConnectURL() + "/ws/Calculator?wsdl";
	}
}
