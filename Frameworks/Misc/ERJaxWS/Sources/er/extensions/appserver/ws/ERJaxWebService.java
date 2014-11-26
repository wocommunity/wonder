package er.extensions.appserver.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.message.ExceptionHasMessage;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.server.DocumentAddressResolver;
import com.sun.xml.ws.api.server.PortAddressResolver;
import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.api.server.ServiceDefinition;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WSEndpoint.PipeHead;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.server.UnsupportedMediaException;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WODynamicURL;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXRequest;
import er.extensions.appserver.ERXResourceManager;
import er.extensions.appserver.ERXResponse;

/**
 * @author mstoll
 *
 * @param <T>
 */
public class ERJaxWebService<T>
{
    /**
     * 
     */
    public static final Logger webServiceLog =
        Logger.getLogger("er.extensions.appserver.ws.ERJaxWebServiceRequestHandler.Logging");

    /**
     * 
     */
    protected WSEndpoint<T> wsEndpoint;
    /**
     * 
     */
    protected PipeHead pipeHead;
    /**
     * 
     */
    protected Codec codec;

    /**
     * 
     */
    protected Map<String, SDDocument> wsdls;
    /**
     * 
     */
    private Map<SDDocument, String> revWsdls;
    
    public ERJaxWebService(Class<T> implementationClass)
    {
    	wsEndpoint = WSEndpoint.create(implementationClass, false, null, null, null, null,
        		BindingImpl.create(BindingID.parse(implementationClass)), null, null, null, true);
        pipeHead = wsEndpoint.createPipeHead();
        codec = wsEndpoint.createCodec();
        initWSDLMap(wsEndpoint.getServiceDefinition());
    }

    /**
     * @param woRequest
     * @return
     */
    public WOResponse handleRequest(WORequest woRequest)
    {
        if(isMetadataQuery(woRequest.queryString()))
        {
            SDDocument doc = wsdls.get(woRequest.queryString());
            if(doc == null)
            {
                ERXResponse resp = new ERXResponse();
                resp.setStatus(WOMessage.HTTP_STATUS_NOT_FOUND);
                return resp;
            }

            ERXResponse resp = new ERXResponse();

            resp.setStatus(HttpURLConnection.HTTP_OK);
            resp.setHeader("text/xml;charset=utf-8", "Content-Type");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            WODynamicURL du = woRequest._uriDecomposed();
            String baseUri = String.format("%s/%s.woa/%s/%s",
                    du.prefix(),
                    du.applicationName(),
                    du.requestHandlerKey(),
                    du.requestHandlerPath());

            boolean isSecure;
            
            if(woRequest instanceof ERXRequest)
            {
                isSecure = ((ERXRequest)woRequest).isSecure();
            } else
            {
                isSecure = ERXRequest.isRequestSecure(woRequest);
            }
                
            String soapAddress = ERXResourceManager._completeURLForResource(
                    baseUri, 
                    isSecure,
                    WOApplication.application().createContextForRequest(woRequest)
                    );

            try
            {
                doc.writeTo(new ERPortAddressResolver(soapAddress),
                            new ERDocumentAddressResolver(soapAddress),
                            baos);
                baos.flush();
            }
            catch(IOException e)
            {
            }

            resp.setContent(baos.toByteArray());

            return resp;
        }

        ERWSWOHTTPConnection con = new ERWSWOHTTPConnection(woRequest);
        try
        {
            Packet packet;
            boolean invoke = false;
            try
            {
                packet = decodePacket(con, codec);
                invoke = true;
            }
            catch(Exception e)
            {
                packet = new Packet();
                if(e instanceof ExceptionHasMessage)
                {
                    webServiceLog.error(e.getMessage(), e);
                    packet.setMessage(((ExceptionHasMessage)e).getFaultMessage());
                }
                else if(e instanceof UnsupportedMediaException)
                {
                    webServiceLog.error(e.getMessage(), e);
                    con.setStatus(415);
                }
                else
                {
                    webServiceLog.error(e.getMessage(), e);
                    con.setStatus(500);
                }
            }
            if(invoke)
            {
                try
                {
                    packet = pipeHead.process(packet, con.getWebServiceContextDelegate(),
                                              packet.transportBackChannel);
                }
                catch(Exception e)
                {
                    webServiceLog.error(e.getMessage(), e);
                    return null;
                }
            }
            try
            {
                encodePacket(packet, con, codec);
            }
            catch(IOException e)
            {
                webServiceLog.error(e.getMessage(), e);
            }
        }
        finally
        {
            if(!con.isClosed())
            {
                con.close();
            }
        }

        return con.generateResponse();

    }

    /**
     * @param con
     * @param codec
     * @return
     * @throws IOException
     */
    private Packet decodePacket(WSHTTPConnection con, Codec codec)
        throws IOException
    {
        String ct = con.getRequestHeader("Content-Type");
        InputStream in = con.getInput();
        Packet packet = new Packet();
        packet.soapAction = fixQuotesAroundSoapAction(con.getRequestHeader("SOAPAction"));
        packet.wasTransportSecure = con.isSecure();
        packet.acceptableMimeTypes = con.getRequestHeader("Accept");
        packet.addSatellite(con);
        addSatellites(packet);
        packet.webServiceContextDelegate = con.getWebServiceContextDelegate();
        codec.decode(in, ct, packet);
        return packet;
    }

    /**
     * @param soapAction
     * @return
     */
    public static String fixQuotesAroundSoapAction(String soapAction)
    {
        if(soapAction != null && (!soapAction.startsWith("\"") || !soapAction.endsWith("\"")))
        {
            webServiceLog.info("Received WS-I BP non-conformant Unquoted SoapAction HTTP header: " + soapAction);
            String fixedSoapAction = soapAction;
            if(!soapAction.startsWith("\""))
                fixedSoapAction = (new StringBuilder()).append("\"").append(fixedSoapAction).toString();
            if(!soapAction.endsWith("\""))
                fixedSoapAction = (new StringBuilder()).append(fixedSoapAction).append("\"").toString();
            return fixedSoapAction;
        }
        else
        {
            return soapAction;
        }
    }

    /**
     * @param packet1
     */
    protected void addSatellites(Packet packet1)
    {
    }

    /**
     * @param connStatus
     * @return
     */
    private boolean isClientErrorStatus(int connStatus)
    {
        return (connStatus == HttpURLConnection.HTTP_FORBIDDEN); // add more for future.
    }


    /**
     * @param packet
     * @param con
     * @param codec
     * @throws IOException
     */
    private void encodePacket(@NotNull Packet packet, @NotNull WSHTTPConnection con, @NotNull Codec codec)
        throws IOException
    {
        Message responseMessage = packet.getMessage();

        if(responseMessage == null)
        {
            if(con.getStatus() == 0)
            {
                con.setStatus(WSHTTPConnection.ONEWAY);
            }
        }
        else
        {
            if(con.getStatus() == 0)
            {
                // if the application didn't set the status code,
                // set the default one.
                con.setStatus(responseMessage.isFault()
                                                       ? HttpURLConnection.HTTP_INTERNAL_ERROR
                                                       : HttpURLConnection.HTTP_OK);
            }

            if(isClientErrorStatus(con.getStatus()))
                return;

            ContentType contentType = codec.getStaticContentType(packet);
            if(contentType != null)
            {
                con.setContentTypeResponseHeader(contentType.getContentType());
                OutputStream os = con.getOutput();
                codec.encode(packet, os);
            }
            else
            {

                ByteArrayBuffer buf = new ByteArrayBuffer();
                contentType = codec.encode(packet, buf);
                con.setContentTypeResponseHeader(contentType.getContentType());
                OutputStream os = con.getOutput();
                buf.writeTo(os);
            }
        }
    }

    /**
     * @param sdef
     */
    public final void initWSDLMap(ServiceDefinition sdef)
    {
        if(sdef == null)
        {
            wsdls = Collections.emptyMap();
            revWsdls = Collections.emptyMap();
        }
        else
        {
            wsdls = new HashMap<String, SDDocument>();
            // wsdl=1 --> Doc
            // Sort WSDL, Schema documents based on SystemId so that the same
            // document gets wsdl=x mapping
            Map<String, SDDocument> systemIds = new TreeMap<String, SDDocument>();
            for(SDDocument sdd : sdef)
            {
                if(sdd == sdef.getPrimary())
                {
                    // No sorting for Primary WSDL
                    wsdls.put("wsdl", sdd);
                    wsdls.put("WSDL", sdd);
                }
                else
                {
                    systemIds.put(sdd.getURL().toString(), sdd);
                }
            }

            int wsdlnum = 1;
            int xsdnum = 1;
            for(Map.Entry<String, SDDocument> e : systemIds.entrySet())
            {
                SDDocument sdd = e.getValue();
                if(sdd.isWSDL())
                {
                    wsdls.put("wsdl=" + (wsdlnum++), sdd);
                }
                if(sdd.isSchema())
                {
                    wsdls.put("xsd=" + (xsdnum++), sdd);
                }
            }

            revWsdls = new HashMap<SDDocument, String>();    // Doc --> wsdl=1
            for(Entry<String, SDDocument> e : wsdls.entrySet())
            {
                if(!e.getKey().equals("WSDL"))
                {           // map Doc --> wsdl, not WSDL
                    revWsdls.put(e.getValue(), e.getKey());
                }
            }
        }
    }

    /**
     * @param query
     * @return
     */
    private boolean isMetadataQuery(String query)
    {
        // we intentionally return true even if documents don't exist,
        // so that they get 404.
        return query != null && (query.equals("WSDL") || query.startsWith("wsdl") || query.startsWith("xsd="));
    }

    /**
     * @author mstoll
     *
     */
    final class ERPortAddressResolver
        extends PortAddressResolver
    {
        /**
         * 
         */
        String base;

        /**
         * @param base
         */
        public ERPortAddressResolver(String base)
        {
            super();
            this.base = base;
        }

        /* (non-Javadoc)
         * @see com.sun.xml.ws.api.server.PortAddressResolver#getAddressFor(javax.xml.namespace.QName, java.lang.String)
         */
        @Override
        public String getAddressFor(QName qname, String s)
        {
            return base;
        }
    };

    /**
     * @author mstoll
     *
     */
    final class ERDocumentAddressResolver
        implements
            DocumentAddressResolver
    {
        /**
         * 
         */
        String base;

        /**
         * @param base
         */
        public ERDocumentAddressResolver(String base)
        {
            super();
            this.base = base;
        }

        /* (non-Javadoc)
         * @see com.sun.xml.ws.api.server.DocumentAddressResolver#getRelativeAddressFor(com.sun.xml.ws.api.server.SDDocument, com.sun.xml.ws.api.server.SDDocument)
         */
        @Override
        public String getRelativeAddressFor(SDDocument sddocument, SDDocument referenced)
        {
            assert (revWsdls.containsKey(referenced));
            return base + '?' + ((String)revWsdls.get(referenced));
        }
    };

}
