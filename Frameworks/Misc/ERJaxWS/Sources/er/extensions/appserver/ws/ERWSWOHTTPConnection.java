package er.extensions.appserver.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.PropertySet;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;

import er.extensions.appserver.ERXRequest;
import er.extensions.appserver.ERXResponse;
import er.extensions.appserver.ERXWOContext;


/**
 * @author mstoll
 *
 * This class maps ERXRequest/ERXResponse to a WSHTTPConnection
 * This enables to jump into the request handling mechanism of JaxWS
 * for handling WebServices from a Servlet.
 *  
 */
public class ERWSWOHTTPConnection
    extends WSHTTPConnection
{
    public static final String ERJAXWS_WOCONTEXT = "com.webobjects.appserver.WOContext";
    public static final String ERJAXWS_ERXWOCONTEXT = "er.extensions.appserver.ERXWOContext";
	
    /** the current ERXRequest */
    ERXRequest woRequest;
    
    /** the output stream JaxWS writes into */
    ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();

    /** the HTTP result status, being set from JaxWS
     *  to be handed into ERXResponse
     */
    int responseStatus = 0;

    /** the current ERXResponse */
    ERXResponse woResponse;
    
    /** the associated session **/
    WOSession session = null;
    
	private static final PropertySet.PropertyMap model = parse(ERWSWOHTTPConnection.class);

    /**
     * The constructor 
     * @param req the current ERXRequest for this RR cycle
     */
    public ERWSWOHTTPConnection(WORequest req)
    {
        woRequest = (ERXRequest)req;
        woResponse = new ERXResponse();
    }
    
    /* (non-Javadoc)
     * @see com.sun.xml.ws.transport.http.WSHTTPConnection#getInput()
     */
    @Override
    public InputStream getInput() throws IOException
    {
        return woRequest.content().stream();
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.transport.http.WSHTTPConnection#getOutput()
     */
    @Override
    public OutputStream getOutput() throws IOException
    {
        return responseOutputStream;
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.transport.http.WSHTTPConnection#getPathInfo()
     */
    @Override
    public String getPathInfo()
    {
        return "";
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.transport.http.WSHTTPConnection#getQueryString()
     */
    @Override
    public String getQueryString()
    {
        return woRequest.queryString();
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.transport.http.WSHTTPConnection#getRequestHeader(java.lang.String)
     */
    @Override
    public String getRequestHeader(String s)
    {
        return woRequest.headerForKey(s);
    }

// JAX 2.2 methods
//    @Override
//    public Set<String> getRequestHeaderNames()
//    {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public List<String> getRequestHeaderValues(String s)
//    {
//        // TODO Auto-generated method stub
//        return null;
//    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.transport.http.WSHTTPConnection#getRequestHeaders()
     */
    @SuppressWarnings({
        "rawtypes", "unchecked"
    })
	@PropertySet.Property({ "javax.xml.ws.http.request.headers",
	"com.sun.xml.ws.api.message.packet.inbound.transport.headers" })
    @NotNull
    @Override
    public Map<String, List<String>> getRequestHeaders()
    {
        return (Map)woRequest.headers();
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.transport.http.WSHTTPConnection#getRequestMethod()
     */
    @Override
    public String getRequestMethod()
    {
        return woRequest.method();
    }

// JAX 2.2 methods
//    @Override
//    public String getRequestScheme()
//    {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public String getRequestURI()
//    {
//        // TODO Auto-generated method stub
//        return null;
//    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.transport.http.WSHTTPConnection#getResponseHeaders()
     */
	@PropertySet.Property({ "javax.xml.ws.http.response.headers",
	"com.sun.xml.ws.api.message.packet.outbound.transport.headers" })
    @Override
    public Map<String, List<String>> getResponseHeaders()
    {
        return null;
    }

//    @Override
//    public String getServerName()
//    {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public int getServerPort()
//    {
//        // TODO Auto-generated method stub
//        return 0;
//    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.transport.http.WSHTTPConnection#getStatus()
     */
    @Override
    public int getStatus()
    {
        return responseStatus;
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.transport.http.WSHTTPConnection#getWebServiceContextDelegate()
     */
    @Override
    public WebServiceContextDelegate getWebServiceContextDelegate()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.transport.http.WSHTTPConnection#isSecure()
     */
    @Override
    public boolean isSecure()
    {
        return woRequest.isSecure();
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.transport.http.WSHTTPConnection#setContentTypeResponseHeader(java.lang.String)
     */
    @Override
    public void setContentTypeResponseHeader(String s)
    {
        woResponse.setHeader(s, "Content-Type");
    }

//    @Override
//    public void setResponseHeader(String s, List<String> list)
//    {
//        // TODO Auto-generated method stub
//
//    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.transport.http.WSHTTPConnection#setResponseHeaders(java.util.Map)
     */
    @Override
    public void setResponseHeaders(Map<String, List<String>> map)
    {
        woResponse.setHeaders(map);
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.transport.http.WSHTTPConnection#setStatus(int)
     */
    @Override
    public void setStatus(int i)
    {
        responseStatus = i;
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.api.PropertySet#getPropertyMap()
     */
    @Override
	protected PropertySet.PropertyMap getPropertyMap() 
    {
		return model;
	}

    /**
     * Generate the response after the ERXRequest has been treaten by JaxWS
     * 
     * @return the generated ERXResponse
     */
    public WOResponse generateResponse()
    {
        try
        {
            responseOutputStream.flush();
            woResponse.setStatus(responseStatus);
            woResponse.setContent(responseOutputStream.toByteArray());
            
            if(context != null && context._session() != null)
            {
            	context._session().setStoresIDsInCookies(true);
            	context._session()._appendCookieToResponse(woResponse);
            	WOApplication.application().saveSessionForContext(context);

            	woResponse._finalizeInContext(context);
            }
        }
        catch(IOException e)
        {
            Logger.getLogger("er.extensions.appserver.ws.ERJaxWebServiceRequestHandler.Logging").error("Exception on writing response", e);
            return null;
        }
        
        return woResponse;
    }

	private WOContext context = null;

	@PropertySet.Property({ "com.webobjects.appserver.WOContext" })
	public synchronized WOContext WOContext()
	{
		if(context == null)
		{
			synchronized (this) 
			{
				if(context == null)
				{
				    context = WOApplication.application().createContextForRequest(woRequest);
					
					String sessionID = getSessionIDFromCookie();
					if(sessionID != null)
						context._setRequestSessionID(sessionID);
				}
			}
		}
		
		return context;
	}

    @PropertySet.Property({ "er.extensions.appserver.ERXWOContext" })
    public synchronized ERXWOContext ERXWOContext()
    {
        WOContext c = WOContext();
        
        if(c instanceof ERXWOContext)
        {
            return (ERXWOContext)c;
        }
        
        throw new IllegalArgumentException("WOContext is no sublass of ERXWOContext");
    }
    
	private String getSessionIDFromCookie()
	{
		return woRequest.cookieValueForKey(WOApplication.application().sessionIdKey());
	}
	
}
