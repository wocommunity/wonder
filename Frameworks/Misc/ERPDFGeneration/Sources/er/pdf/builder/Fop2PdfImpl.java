package er.pdf.builder;

import java.io.File;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.Date;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXApplication;
import er.extensions.foundation.ERXProperties;

/**
 * 
 *  @property er.pdf.builder.fop.resolution Resolution of the pdf. Defaults to 300.
 *  @property er.pdf.builder.fop.author Name of the author. Defaults to 'ERPDFGeneration'.
 *  @property er.pdf.builder.fop.generator Name of the generator. Defaults to 'ERPDFGeneration'.
 *  
 *  @property er.pdf.builder.fop.config.filename Set to the name of the fop configuration file. Defaults to 'fop.xconf'.
 *  @property er.pdf.builder.fop.config.framework Name of the framework the config file exists within. Defaults to 'ERPDFGeneration'.
 */
public class Fop2PdfImpl implements FOPBuilder {

	private static final Logger log = LoggerFactory.getLogger(Fop2PdfImpl.class);

	public static final String GENERATOR_NAME_KEY = "GENERATOR_NAME";
	public static final String GENERATOR_NAME = "ERPDFGeneration";
	public static final String AUTHOR_KEY = "DEFAULT_AUTHOR";
	public static final String DEFAULT_AUTHOR = "ERPDFGeneration";
	public static final String TARGET_RESOLUTION_KEY = "TARGET_RESOLUTION";
	public static final int DEFAULT_RESOLUTION = 300;
	
	public static final String FOP_CONF_FILENAME_KEY = "FOP_CONF_FILENAME";
	public static final String FOP_CONF_FILENAME = "fop.xconf";
	public static final String FOP_CONF_FRAMEWORK_KEY = "FOP_CONF_FRAMEWORK";
	public static final String FOP_CONF_FRAMEWORK = "ERPDFGeneration";
	
	private String _fopxslLocation;
	private String _xmlToTransform;
	private NSMutableDictionary<String, Object> _config;

	public Fop2PdfImpl() {}

	public void setXSL(String fopxslLocation) {
		_fopxslLocation = fopxslLocation;
	}

	public void setXML(String xmlToTransform) {
		_xmlToTransform = xmlToTransform;
	}

	/**
	 * @return a configuration dictionary for the user agent
	 */
	public NSDictionary<String, Object> agentDefaults() {
		NSMutableDictionary<String, Object> d = new NSMutableDictionary<>();
		d.setObjectForKey(ERXProperties.intForKeyWithDefault("er.pdf.builder.fop.resolution", DEFAULT_RESOLUTION), TARGET_RESOLUTION_KEY);
		d.setObjectForKey(ERXProperties.stringForKeyWithDefault("er.pdf.builder.fop.author", DEFAULT_AUTHOR), AUTHOR_KEY);
		d.setObjectForKey(ERXProperties.stringForKeyWithDefault("er.pdf.builder.fop.generator", GENERATOR_NAME), GENERATOR_NAME_KEY);
		return d.immutableClone();
	}

	public void createDocument(OutputStream os) throws Throwable {
		createDocument(os, agentDefaults());
	}

	public void createDocument(OutputStream os, NSDictionary<String, Object> agentAttributes) throws Throwable {
		log.debug("createDocument(OutputStream os={}, NSDictionary<String,Object> agentAttributes={}, NSDictionary<String,Object> configuration={}) - start", os, agentAttributes, configuration());
		
		FopFactoryBuilder fopBuilder = null;
		try {
			URL path = ERXApplication.erxApplication().resourceManager().pathURLForResourceNamed(
					(String) configuration().get(FOP_CONF_FILENAME_KEY), 
					(String) configuration().get(FOP_CONF_FRAMEWORK_KEY), 
					NSArray.emptyArray());
			if (path != null) {
				fopBuilder = new FopConfParser(new File(path.toURI())).getFopFactoryBuilder();
			}
		} 
		catch (NullPointerException ex) {
			log.warn("Can't find 'fop.xconf' in application resources", ex);
		}
		
		if (fopBuilder == null) {
			fopBuilder = new FopFactoryBuilder(URI.create("/"));
		}
		FopFactory fopFactory = fopBuilder.build();

		log.debug("createDocument(OutputStream) - initializing FOUserAgent");
		// get the defaults, but immediately override them with whatever was
		// passed in
		// from the program. This is meant to ensure that we have all the
		// attributes we need
		// before initializing the agent. If there is a better way, help
		// yourself.
		NSMutableDictionary<String, Object> _agentAtts = agentDefaults().mutableClone();
		_agentAtts.addEntriesFromDictionary(agentAttributes);

		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
		foUserAgent.setCreator((String) _agentAtts.get(GENERATOR_NAME_KEY));
		foUserAgent.setAuthor((String) _agentAtts.get(AUTHOR_KEY));
		foUserAgent.setCreationDate(new Date());
		foUserAgent.setTargetResolution((Integer) _agentAtts.get(TARGET_RESOLUTION_KEY));

		foUserAgent.setTitle("FOP Referral Package Test");
		foUserAgent.setKeywords("Referral Package Vasc-Alert");

		try {
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, os);

			if (log.isDebugEnabled()) {
				log.debug("createDocument(OutputStream) - Fop initialized with:  - _fopxslLocation={}, os={}, foUserAgent={}", _fopxslLocation, os, foUserAgent);
				log.debug("xsl resource: {}", Fop2PdfImpl.class.getClassLoader().getResourceAsStream(_fopxslLocation));
			}
			
			TransformerFactory txfac = TransformerFactory.newInstance();
			Transformer tx = txfac.newTransformer(new SAXSource(new InputSource(Fop2PdfImpl.class.getClassLoader().getResourceAsStream(_fopxslLocation))));
			tx.setParameter("versionParam", "2.0");

			Source src = new StreamSource(new StringReader(_xmlToTransform));
			Result res = new SAXResult(fop.getDefaultHandler());

			tx.transform(src, res);

		} catch (FOPException e) {
			log.error("createDocument(OutputStream, NSDictionary<String,Object>)", e);

			e.printStackTrace();
			throw NSForwardException._runtimeExceptionForThrowable(e);
		} catch (TransformerConfigurationException e) {
			log.error("createDocument(OutputStream, NSDictionary<String,Object>)", e);

			e.printStackTrace();
			throw NSForwardException._runtimeExceptionForThrowable(e);
		} catch (TransformerException e) {
			log.error("createDocument(OutputStream, NSDictionary<String,Object>)", e);

			e.printStackTrace();
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}

		if (log.isDebugEnabled()) {
			log.debug("createDocument(OutputStream, NSDictionary<String,Object>) - end");
		}
	}
	
	public NSDictionary<String, Object> configurationDefaults() {
		NSMutableDictionary<String, Object> c = new NSMutableDictionary<>();
		c.setObjectForKey(ERXProperties.stringForKeyWithDefault("er.pdf.builder.fop.config.filename", FOP_CONF_FILENAME), FOP_CONF_FILENAME_KEY);
		c.setObjectForKey(ERXProperties.stringForKeyWithDefault("er.pdf.builder.fop.config.framework", FOP_CONF_FRAMEWORK), FOP_CONF_FRAMEWORK_KEY);
		return c;
	}
	
	public NSDictionary<String, Object> configuration() {
		if (_config == null) {
			_config = new NSMutableDictionary<>();
		}
		NSMutableDictionary<String, Object> config = configurationDefaults().mutableClone();
		config.addEntriesFromDictionary(_config);
		return config;
	}

	public void setConfiguration(NSMutableDictionary<String, Object> config) {
		_config = config;
	}

}
