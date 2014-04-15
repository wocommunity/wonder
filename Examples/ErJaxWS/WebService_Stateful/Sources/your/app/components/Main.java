package your.app.components;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.ws.BindingProvider;

import your.app.ws.StatefulAction;
import your.app.ws.StatefulActionException;
import your.app.ws.StatefulActionImplService;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;

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
		
		WORequest request = context().request();
		URL url = new URL(
					String.format("http://localhost:%d/%s/%s.woa/ws/StatefulAction?wsdl", 
							WOApplication.application().port(),
							request.adaptorPrefix(),
							request.applicationName())
							);

		StatefulActionImplService service = new StatefulActionImplService(url);

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
