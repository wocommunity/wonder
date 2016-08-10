package er.examples.erjaxws.components;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;

import er.examples.erjaxws.ws.StatefulAction;
import er.examples.erjaxws.ws.StatefulActionException;
import er.examples.erjaxws.ws.StatefulActionImplService;

@SuppressWarnings("serial")
public class Main extends BaseComponent {
	public Main(WOContext context) {
		super(context);
	}

	public WOActionResults doit() throws MalformedURLException, StatefulActionException {
		/* To simulate the client server call within this single application we
		 * construct dynamically an URL pointing to the WebService in this 
		 * running application
		 * */ 
		
		URL url = new URL(WOApplication.application().directConnectURL() + 
				"/ws/StatefulAction?wsdl"); 

		StatefulActionImplService service = new StatefulActionImplService(url, 
				new QName("http://ws.erjaxws.examples.er/", "StatefulActionImplService"));
		StatefulAction sAction = service.getPort(StatefulAction.class);

		/* setting this property is essential for enabling stateful mode in our client proxy
		 * after setting this, session cookies will be passed to the server on subsequent
		 * requests */
		((BindingProvider) sAction).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

		sAction.authenticate("name", "password");
		serverOutput = sAction.testAction("test");
		
		return null;
	}
	
	public String serverOutput = null;
}
