package er.pdf.builder;

import java.io.OutputStream;
import java.io.StringReader;
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
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableDictionary;

public class Fop2PdfImpl implements FOPBuilder {

	public static final String GENERATOR_NAME_KEY = "GENERATOR_NAME";
	public static final String GENERATOR_NAME = "ERPDFGeneration";
	public static final String AUTHOR_KEY = "DEFAULT_AUTHOR";
	public static final String DEFAULT_AUTHOR = "ERPDFGeneration";
	public static final String TARGET_RESOLUTION_KEY = "TARGET_RESOLUTION";
	public static final int DEFAULT_RESOLUTION = 300;
	private String _fopxslLocation;
	private String _xmlToTransform;
	private String _outputType = MimeConstants.MIME_PDF;
	private NSMutableDictionary<String, Object> _config;

	private static final Logger log = LoggerFactory.getLogger(Fop2PdfImpl.class);

	protected static FopFactory fopFactory;

	public Fop2PdfImpl() {

	}

	public void setXSL(String fopxslLocation) {
		_fopxslLocation = fopxslLocation;
	}

	public void setXML(String xmlToTransform) {
		_xmlToTransform = xmlToTransform;

	}

	/**
	 * some basic defaults for configuring the fop agent. This should be
	 * property driven, but I'll do that later (yeah... right)
	 * 
	 * @return a configuration dictionary
	 */
	public NSDictionary<String, Object> agentDefaults() {
		NSMutableDictionary<String, Object> d = new NSMutableDictionary<String, Object>();
		d.setObjectForKey(DEFAULT_RESOLUTION, TARGET_RESOLUTION_KEY);
		d.setObjectForKey(DEFAULT_AUTHOR, AUTHOR_KEY);
		d.setObjectForKey(GENERATOR_NAME, GENERATOR_NAME_KEY);
		return d.immutableClone();
	}

	public void createDocument(OutputStream os) throws Throwable {
		createDocument(os, agentDefaults());
	}

	public void createDocument(OutputStream os, NSDictionary<String, Object> agentAttributes) throws Throwable {
		log.debug("createDocument(OutputStream os={}, NSDictionary<String,Object> agentAttributes={}) - start", os, agentAttributes);

		fopFactory = FopFactory.newInstance();

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

	public void setConfiguration(NSMutableDictionary<String, Object> config) {
		_config = config;
	}

}
