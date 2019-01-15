package er.pdfexamples.xsl.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.webobjects.foundation.NSMutableDictionary;

import er.pdf.builder.FOPBuilder;
import er.pdf.builder.FOPBuilderFactory;
import er.pdf.builder.Fop2PdfImpl;

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

	public TestFOP() {
	}

	public void generatePdf(OutputStream os) 
	{
		FOPBuilder fopBuilder = FOPBuilderFactory.newBuilder();
		
		NSMutableDictionary<String, Object> agentAttributes = new NSMutableDictionary<>();
		agentAttributes.setObjectForKey("Testy McTesterton", Fop2PdfImpl.AUTHOR_KEY);
		agentAttributes.setObjectForKey("FOP Test Renderator", Fop2PdfImpl.GENERATOR_NAME_KEY);
		agentAttributes.setObjectForKey(300, Fop2PdfImpl.TARGET_RESOLUTION_KEY);
		
		try {
			fopBuilder.setXSL(_fopxslLocation);

			InputStream xmlStream = TestFOP.class.getClassLoader().getResourceAsStream(_xmlToTransform);
			fopBuilder.setXML(IOUtils.toString(xmlStream, StandardCharsets.UTF_8));
			
			fopBuilder.createDocument(os, agentAttributes);
		} 
		catch (Throwable e) {
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
