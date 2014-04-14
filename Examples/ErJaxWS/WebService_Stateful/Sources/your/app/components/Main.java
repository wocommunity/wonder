package your.app.components;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.ws.BindingProvider;

import your.app.ws.StatefulAction;
import your.app.ws.StatefulActionException;
import your.app.ws.StatefulActionImplService;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

@SuppressWarnings("serial")
public class Main extends BaseComponent {
	public Main(WOContext context) {
		super(context);
	}

	public WOActionResults doit() throws MalformedURLException, StatefulActionException {
		URL url = new URL("http://127.0.0.1:3335/cgi-bin/WebObjects/WebService_Stateful.woa/ws/StatefulAction?wsdl"); 

		StatefulActionImplService service = new StatefulActionImplService(url);

		StatefulAction sAction = service.getPort(StatefulAction.class);

		((BindingProvider) sAction).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

		sAction.authenticate("name", "password");
		serverOutput = sAction.testAction("test");
		
		return null;
	}
	
	public String serverOutput = null;
}
