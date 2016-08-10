package er.pdfexamples.xsl.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.xmlgraphics.util.MimeConstants;
import org.xml.sax.InputSource;

/**
 * This is just a stub class that can be called directly (outside the context of
 * any WO or WOnder app) to test the fop xsl translation and rendering. It's
 * easier to find problems with the stylesheet without having to deal with the
 * wrappers etc.
 * 
 * 
 * @author Larry Mills-Gahl &lt;lmg@webfarm.com&gt;
 */
public class TestFOP {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(TestFOP.class);

	//private String _fopxslLocation = "er/pdfexamples/xsl/xhtml2xslfo.xsl";
	private String _fopxslLocation = "er/pdfexamples/xsl/testxml2fo.xsl";
	private String _xmlToTransform = "er/pdfexamples/xsl/test/xml4test.xml";
	public static final String GENERATOR_NAME = "FOP Test Renderator";
	protected static FopFactory fopFactory;

	public TestFOP() {
		fopFactory = FopFactory.newInstance();
	}

	public void generatePdf(OutputStream os) {

		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
		foUserAgent.setCreator(GENERATOR_NAME);
		foUserAgent.setAuthor("Testy McTesterton");
		foUserAgent.setCreationDate(new Date());
		foUserAgent.setTargetResolution(300);

		foUserAgent.setTitle("FOP Referral Package Test");
		foUserAgent.setKeywords("Referral Package Vasc-Alert");
		try {
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, os);

			logger.debug("xml to transform: " + _xmlToTransform);
			InputStream xmlStream = TestFOP.class.getClassLoader().getResourceAsStream(_xmlToTransform);
			// logger.debug("xml as stream: " + xmlStream);
			// logger.debug(new Scanner(xmlStream).useDelimiter("\\A").next());
			logger.debug("xsl doing the transforming: " + _fopxslLocation);
			InputStream xslStream = TestFOP.class.getClassLoader().getResourceAsStream(_fopxslLocation);
			// logger.debug("xsl as stream " + xslStream);
			// logger.debug(new Scanner(xslStream).useDelimiter("\\A").next());

			TransformerFactory txfac = TransformerFactory.newInstance();
			Transformer tx;

			tx = txfac.newTransformer(new SAXSource(new InputSource(xslStream)));

			Source src = new StreamSource(xmlStream);
			logger.debug("source systemid: " + src.getSystemId());
			Result res = new SAXResult(fop.getDefaultHandler());
			logger.debug("result systemid: " + res.getSystemId());

			tx.transform(src, res);

		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FOPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * just call it and check the output of the pdf
	 * 
	 * @throws Throwable
	 * @throws FileNotFoundException
	 **/
	public static void main(String[] args) throws FileNotFoundException, Throwable {

		BasicConfigurator.configure();
		logger.setLevel(Level.DEBUG);
		if (logger.isDebugEnabled()) {
			logger.debug("main(String[] args=" + args + ") - start"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		TestFOP tfop = new TestFOP();
		FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/Desktop/" + TestFOP.class.getSimpleName() + "_OUTPUT.pdf");
		tfop.generatePdf(fos);
		fos.close();

		if (logger.isDebugEnabled()) {
			logger.debug("main(String[]) - end"); //$NON-NLS-1$
		}
	}

}
