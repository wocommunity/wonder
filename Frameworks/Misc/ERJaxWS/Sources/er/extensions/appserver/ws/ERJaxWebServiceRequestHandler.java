package er.extensions.appserver.ws;

import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODirectActionRequestHandler;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * The WebObject request handler that maps a WORequest to a JaxWS request.
 * This class uses the existing JaxWS mechanism for handling Servlet requests.
 *  
 *
 *  to register your own WebServices, you have to instanciate this class in your
 *  Application and bind this to a request handler key:
 *  
 *  <pre>
 *  ERJaxWebServiceRequestHandler wsHandler = new ERJaxWebServiceRequestHandler()
 *  registerRequestHandler(wsHandler, this.webServiceRequestHandlerKey());
 *  </pre>
 *  
 *  To this ERJaxWebServiceRequestHandler you can bind single WebServices, for example:
 *  
 *  <pre>
 *  wsHandler.registerWebService("TestWS", new ERJaxWebService<TestWSImpl>(TestWSImpl.class));
 *  </pre>
 *  
 *  This binds the implementation "TestWSImpl" to the given service name.
 *  
 *  This example can be called using this URL:
 *  
 *  <pre>
 *  http://<em>hostname</em>/<em>adapterpath</em>/<em>appname</em>/ws/TestWS
 *  </pre>
 *  
 *  Appending the parameter "?wsdl" will return the full WSDL for this WebService.
 *  
 */
public class ERJaxWebServiceRequestHandler
    extends WODirectActionRequestHandler
{
    /** a dictionary where all WebService registrations are kept */
    protected NSMutableDictionary<String, ERJaxWebService<? extends Object>> registeredWebServices =
        new NSMutableDictionary<String, ERJaxWebService<? extends Object>>();

    /**
     * Register a WebService implementation under a given service name
     * 
     * @param serviceName the given servicename, a existing service for this name will be replaced
     * @param webService the ERXJaxWebService object, holding the implementation for the referred service
     */
    public void registerWebService(String serviceName, ERJaxWebService<? extends Object> webService)
    {
        registeredWebServices.put(serviceName, webService);
    }

    /* (non-Javadoc)
     * @see com.webobjects.appserver._private.WOActionRequestHandler#handleRequest(com.webobjects.appserver.WORequest)
     */
    @Override
    public WOResponse handleRequest(WORequest aRequest)
    {
        NSArray<String> requestHandlerPath = aRequest.requestHandlerPathArray();
        String serviceName = requestHandlerPath.objectAtIndex(0);

        ERJaxWebService<? extends Object> ws = registeredWebServices.get(serviceName);
        if(ws != null)
        {
            WOResponse resp = ws.handleRequest(aRequest);
            if(resp != null)
                return resp;

            return nullResponse();
        }

        return nullResponse();
    }
}
