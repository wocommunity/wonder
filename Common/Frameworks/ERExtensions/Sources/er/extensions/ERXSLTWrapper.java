package er.extensions;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/**
 * Wrapper that translates its content via XSLT. The content must be valid XML for this to work. 
 * This is pretty usefull in conjunction with DynaReporter when you want to use one of the 
 * zillion PDF libs. You can generate the content via DynaReporter and then transform the content
 * to a form that the PDF lib understands. Most likely this will be much easier than trying to re-generate
 * the report with XML. <br />
 * Other uses include a simple transformation of the generated front end code to privide for "skinning".
 * As there is only so much you can do with CSS, you might need to structurally change the generated HTML prior
 * to handing it to the client.<br />
 * Note that XSLT engines vary <emp>greatly</emp> in speed. The default case of using Xalan which is included by WO
 * is probably not the best choice for a site with a little bit of traffic. 
 * Therefore there is an option where you can set the transformer factory name to use, you also need to include the 
 * corresponding jar into the classpath.
 * 
 * @binding enabled flag that decides if the transformation is applied. If not set, then only the content will be shown.
 * @binding stylesheet name of the XLST stylesheet (mandatory)
 * @binding transformerFactory name of the class for the XSLT transformer factory (optional, defaults to Xalan)
 * @binding framework name of the XLST stylesheet's framework (optional)
 * @binding data will be set to the transformed data (optional)
 * @binding stream will be set to the transformed data (optional)
 * @binding nocache flag that if set creates a new transformer instead of using the one in the cache. Useful when deleloping the stylesheet. 
 * @created ak on 07.04.05
 * @project ERExtensions
 */

public class ERXSLTWrapper extends ERXNonSynchronizingComponent {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERXSLTWrapper.class);
    
    private long start, current;
    /**
     * Public constructor
     * @param context the context
     */
    public ERXSLTWrapper(WOContext context) {
        super(context);
    }

    private boolean isEnabled() {
        return booleanValueForBinding("enabled");
    }

    private static Map cache = new HashMap();
    
    private Transformer transformer() {
        Transformer transformer;
        try {
            synchronized (cache) {
                String stylesheet = (String)valueForBinding("stylesheet");
                String framework = (String)valueForBinding("framework");
                NSArray languages = session().languages();
                String key = stylesheet + "-" + framework;
                transformer = (Transformer) cache.get(key);
                if(transformer == null || booleanValueForBinding("nocache")) {
                    byte bytes[] = application().resourceManager().bytesForResourceNamed(stylesheet, framework, languages);
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    documentBuilderFactory.setValidating(false);
                    documentBuilderFactory.setNamespaceAware(true);
                    DocumentBuilder documentBuilder;
                    documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                    Document document = documentBuilder.parse(bis);
                    Source xslt = new DOMSource(document);           
                    xslt.setSystemId(key);
                    String transformerFactoryName = (String)valueForBinding("transformerFactory");
                    String oldTransformerFactoryName = System.getProperty("javax.xml.transform.TransformerFactory");
                    if(transformerFactoryName != null) {
                        System.setProperty("javax.xml.transform.TransformerFactory", transformerFactoryName);
                    } else {
                        System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");
                    }
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    if(oldTransformerFactoryName != null) {
                        System.setProperty("javax.xml.transform.TransformerFactory", oldTransformerFactoryName);
                    }
                    transformer = transformerFactory.newTransformer(xslt);
                    // transformer.setOutputProperty("indent", "no");
                    // transformer.setOutputProperty("method", "xml");
                    
                    cache.put(key, transformer);
                }
            }
            return transformer;
        } catch(Exception ex) {
            throw NSForwardException._runtimeExceptionForThrowable(ex);
        }
    }
    
    private static XMLReader xmlReader;
    
    static {
        try {
            xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setFeature("http://xml.org/sax/features/validation", false);
            // FIXME AK: we need  real handling for the normal case (HTML->FOP XML)
            EntityResolver resolver = new EntityResolver() {
                public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {
                    log.info(arg0 + "::" + arg1);
                    InputSource source = new InputSource((new URL("file:///Volumes/Home/Desktop/dtd/xhtml1-transitional.dtd")).openStream());
                    source.setSystemId(arg1);
                    return source;
                }
            };
            // xmlReader.setEntityResolver(resolver);
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
    
    private NSData transform(WOResponse response) {
        try {
            Source xml;
            SAXSource saxSource = new SAXSource();
            saxSource.setXMLReader(xmlReader);
            ByteArrayInputStream bis = new ByteArrayInputStream(response.content().bytes());
            saxSource.setInputSource(new InputSource(bis));
            xml = saxSource;
            log.debug("DOM: " + (System.currentTimeMillis() - current));  current = System.currentTimeMillis();
            
            Transformer transformer = transformer();
            log.debug("Stylesheet: " + (System.currentTimeMillis() - current));  current = System.currentTimeMillis();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Result result = new StreamResult(os);
            
            transformer.transform(xml, result);
            log.debug("Transform: " + (System.currentTimeMillis() - current));  current = System.currentTimeMillis();
            
            NSData data = new NSData(os.toByteArray());
            return data;
        } catch(Exception ex) {
            throw NSForwardException._runtimeExceptionForThrowable(ex);
        }
    }

    /** 
     * Overridden to get use apply the XLST transformation on the content.
     */
    public void appendToResponse(WOResponse response, WOContext context) {
        start = System.currentTimeMillis(); current = start;
        if (isEnabled()) {
            WOResponse newResponse = new WOResponse();
            newResponse.setContentEncoding(response.contentEncoding());
            
            super.appendToResponse(newResponse, context);

            if (log.isDebugEnabled()) {
                String contentString = newResponse.contentString();
                log.debug("Converting content string:\n" + contentString);
            }

            NSData data = transform(newResponse);
            if(hasBinding("data") && canSetValueForBinding("data")) {
                setValueForBinding(data, "data");
            }
            if(hasBinding("stream") && canSetValueForBinding("stream")) {
                setValueForBinding(data.stream(), "stream");
            }
            response.appendContentData(data);
         } else {
            super.appendToResponse(response, context);
        }
        log.debug("Total: " + (System.currentTimeMillis() - start));  start = System.currentTimeMillis();
   }
}
