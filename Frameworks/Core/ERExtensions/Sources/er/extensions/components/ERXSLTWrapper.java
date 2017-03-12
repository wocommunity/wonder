package er.extensions.components;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSForwardException;

import er.extensions.appserver.ERXResponse;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXSimpleTemplateParser;

/**
 * Wrapper that translates its content via XSLT. The content must be valid XML for this to work. 
 * This is pretty usefull in conjunction with DynaReporter when you want to use one of the 
 * zillion PDF libs. You can generate the content via DynaReporter and then transform the content
 * to a form that the PDF lib understands. Most likely this will be much easier than trying to re-generate
 * the report with XML.
 * <p>
 * Other uses include a simple transformation of the generated front end code to privide for "skinning".
 * As there is only so much you can do with CSS, you might need to structurally change the generated HTML prior
 * to handing it to the client.
 * <p>
 * Note that XSLT engines vary <em>greatly</em> in speed. The default case of using Xalan which is included by WO
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
 *  
 * @author ak on 07.04.05
 */
public class ERXSLTWrapper extends ERXNonSynchronizingComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ERXSLTWrapper.class);

	private long start, current;
	/**
	 * Public constructor
	 * @param context the context
	 */
	public ERXSLTWrapper(WOContext context) {
		super(context);
	}

	private boolean isEnabled() {
		return booleanValueForBinding("enabled", true);
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
			if(false) {
				// FIXME AK: we need  real handling for the normal case (HTML->FOP XML)
				EntityResolver resolver = new EntityResolver() {
					public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {
						log.info("{}::{}", arg0, arg1);
						InputSource source = new InputSource((new URL("file:///Volumes/Home/Desktop/dtd/xhtml1-transitional.dtd")).openStream());
						source.setSystemId(arg1);
						return source;
					}
				};
				xmlReader.setEntityResolver(resolver);
			}
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

	/** 
	 * Overridden to get use apply the XLST transformation on the content.
	 * @throws TransformerException 
	 */
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		start = System.currentTimeMillis(); current = start;
		if (isEnabled()) {
			ERXResponse newResponse = new ERXResponse();
			newResponse.setContentEncoding(response.contentEncoding());

			super.appendToResponse(newResponse, context);

			if (log.isDebugEnabled()) {
				String contentString = newResponse.contentString();
				log.debug("Converting content string:\n{}", contentString);
			}

			try {
				NSData data = transform(transformer(), newResponse.content());
				if(hasBinding("data") && canSetValueForBinding("data")) {
					setValueForBinding(data, "data");
				}
				if(hasBinding("stream") && canSetValueForBinding("stream")) {
					setValueForBinding(data.stream(), "stream");
				}
				response.appendContentData(data);
			} catch (TransformerException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		} else {
			super.appendToResponse(response, context);
		}
		log.debug("Total: {}", System.currentTimeMillis() - start);  start = System.currentTimeMillis();
	}

	private static TemplatePool pool = new TemplatePool();

	public static Transformer getTransformer(String framework, String filename) {
		return pool.getTransformer(framework, filename);
	}

	public static String transform(Transformer transformer, String xml) throws TransformerException {
		StringReader stringreader = new StringReader(xml);
		InputSource inputsource = new InputSource(stringreader);
		SAXSource s = new SAXSource(inputsource);
		
		StringWriter writer = new StringWriter();
		StreamResult r = new StreamResult(writer);

		transformer.transform(s, r);
		String result = writer.toString();
		return result;
	}

	public static NSData transform(Transformer transformer, NSData data) throws TransformerException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data.bytes());
		SAXSource saxSource = new SAXSource();
		saxSource.setXMLReader(xmlReader);
		saxSource.setInputSource(new InputSource(bis));
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Result r = new StreamResult(os);
		transformer.transform(saxSource, r);
		NSData result = new NSData(os.toByteArray());
		return result;
	}

	public static class TemplatePool {

		//private static final WeakHashMap templates = new WeakHashMap();
		private final Map   templates = new HashMap();
		private static final Logger log = LoggerFactory.getLogger(TemplatePool.class);
		private ERXSimpleTemplateParser templateParser = new ERXSimpleTemplateParser();

		protected TemplatePool() {}

		public Map getTemplates() {
			return templates;
		}

		public synchronized Transformer getTransformer(String framework, String filename) {
			if (filename == null || filename.length() == 0) { 
				throw new IllegalArgumentException("filename cannot be null or empty"); 
			}
			String key = framework + "-"  +filename;

			Templates t = (Templates) pool.getTemplates().get(key);
			String s = null;

			if (t == null) {
				try {
					WOApplication app = WOApplication.application();
					WOResourceManager rm = app.resourceManager();

					TransformerFactory fac = TransformerFactory.newInstance();

					log.debug("creating template for file {} in framework {}", filename, framework);
					InputStream is = rm.inputStreamForResourceNamed(filename, framework, null);
					if (is == null) {
						log.debug("trying with framework = null");
						is = rm.inputStreamForResourceNamed(filename, null, null);
						if (is == null) { 
							throw new IllegalArgumentException("inputStream is null"); 
						}
					}
					if (is.available() == 0) { 
						throw new IllegalArgumentException("InputStream has 0 bytes available, cannot read xsl file!"); 
					}
					s = ERXFileUtilities.stringFromInputStream(is);
					s = templateParser.parseTemplateWithObject(s, "@@", app);
					t = fac.newTemplates(new StreamSource(new ByteArrayInputStream(s.getBytes())));

					if (app.isCachingEnabled()) {
						templates.put(key, t);
					}
				} catch (IOException e1) {
					throw NSForwardException._runtimeExceptionForThrowable(e1);
				} catch (TransformerConfigurationException tce) {
					log.error("could not create template {}", tce.getLocationAsString(), tce);
					log.error("  cause", tce.getCause());
					if (tce.getCause() != null && tce.getCause() instanceof org.xml.sax.SAXParseException) {
						org.xml.sax.SAXParseException e = (org.xml.sax.SAXParseException) tce.getCause();
						log.error("SAXParseException: line {}, column {}", e.getLineNumber(), e.getColumnNumber());
					}
					log.error("this is the incorrect xsl:>>>{}<<<", s);
					return null;
				}
			}

			try {
				return t.newTransformer();
			} catch (TransformerConfigurationException tce) {
				log.error("could not create template {}", tce.getLocationAsString(), tce);
				log.error("  cause", tce.getCause());
				return null;
			}
		}
	}
}
