package er.examples.erjaxws.ws;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import er.examples.erjaxws.Session;
import er.extensions.appserver.ERXWOContext;
import er.extensions.appserver.ws.ERWSWOHTTPConnection;

@WebService (
		endpointInterface = "er.examples.erjaxws.ws.StatefulAction")

public class StatefulActionImpl implements StatefulAction 
{
	// this WebServiceContext object is injected by JaxWS during the RR cycle
    @Resource
    private WebServiceContext wsContext;

	@Override
	@WebMethod
	public void authenticate(String login, String password)
			throws StatefulActionException {
		
		if(login.length() == 0 || password.length() == 0)
		{
			StatefulActionFaultInfo statefulActionFaultInfo = new StatefulActionFaultInfo();
			statefulActionFaultInfo.setMessage("INVALID_LOGIN");
			throw new StatefulActionException("INVALID_LOGIN", statefulActionFaultInfo);
		};
			
        MessageContext mc = wsContext.getMessageContext();
        ERXWOContext context = (ERXWOContext) mc.get(ERWSWOHTTPConnection.ERJAXWS_ERXWOCONTEXT);
        
        ((Session)context.session()).authenticated = true;		
	}

	@Override
	@WebMethod
	public String testAction(String name) throws StatefulActionException {
        MessageContext mc = wsContext.getMessageContext();
        ERXWOContext context = (ERXWOContext) mc.get(ERWSWOHTTPConnection.ERJAXWS_ERXWOCONTEXT);
        
        if(!((Session)context.session()).authenticated)
		{
			StatefulActionFaultInfo statefulActionFaultInfo = new StatefulActionFaultInfo();
			statefulActionFaultInfo.setMessage("NOT_AUTHENTICATED");
			throw new StatefulActionException("NOT_AUTHENTICATED", statefulActionFaultInfo);
		}
        
        return "Hello " + name;
	}
}
