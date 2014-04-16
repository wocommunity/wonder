package er.examples.erjaxws;

import javax.xml.ws.Endpoint;

import er.examples.erjaxws.ws.Calculator;
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

        // do it the WONDER way
        ERJaxWebServiceRequestHandler wsHandler = new ERJaxWebServiceRequestHandler();
        wsHandler.registerWebService("Calculator", new ERJaxWebService<Calculator>(Calculator.class));
        this.registerRequestHandler(wsHandler, this.webServiceRequestHandlerKey());

        // create a standalone endpoint using Jax WS mechanisms
        Endpoint.publish("http://localhost:9999/ws/Calculator", new Calculator());
	}
	
	// modify URL to auto open in Browser to show the wsdl 
	@Override
	public String directConnectURL() {
		return super.directConnectURL() + "/ws/Calculator?wsdl";
	}
}
