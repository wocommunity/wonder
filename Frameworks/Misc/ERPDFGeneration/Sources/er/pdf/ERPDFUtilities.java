package er.pdf;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.apache.commons.lang3.CharEncoding;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.pdf.builder.FOPBuilder;
import er.pdf.builder.FOPBuilderFactory;
import er.pdf.builder.PDFBuilder;
import er.pdf.builder.PDFBuilderFactory;

public class ERPDFUtilities {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(ERPDFUtilities.class);

  private ERPDFUtilities() {
    // Utility class. Don't instantiate
  }

  /**
   * Creates and returns a component that will render the output of the passed
   * wocomponent as a PDF when response generation is invoked. Note that the
   * output of the passed component needs to be valid XHTML.
   * 
   * @param element
   *          the component you want to render as a PDF
   * @return an element that wraps the passed component and renders the output
   *         as a PDF
   */
  public static WOActionResults pageAsPdf(WOElement element) {
    return pageAsPdf(element, null);
  }

  /**
   * Creates and returns a page component that will render the output of the
   * passed component as a PDF when appendToResponse() is called. Note that the
   * output of the passed component needs to be valid XHTML.
   * 
   * @param element
   *          the component you want to render as a PDF
   * @param config
   *          a dictionary of binding values to use to configure the PDFWrapper
   *          component and subsequent rending engine. See PDFWrapper for
   *          details on available bindings.
   * @return a component that wraps the passed element and renders the output as
   *         a PDF
   */
  public static WOActionResults pageAsPdf(WOElement element, NSDictionary<String, Object> config) {
    NSMutableDictionary<String, WOAssociation> associations = new NSMutableDictionary<String, WOAssociation>();
    if (config != null) {
      for (Map.Entry<String, Object> entry : config.entrySet()) {
        associations.setObjectForKey(WOAssociation.associationWithValue(entry.getValue()), entry.getKey());
      }
    }
    ERPDFWrapper wrapper = new ERPDFWrapper("PDFWrapper", associations, element);
    return wrapper;
  }

  /**
   * Turns a valid XHTML document string and renders it as a PDF document.
   * 
   * @param content
   *          a string containing valid XHTML markup
   * @return an NSData object containing raw PDF data.
   */
  public static NSData htmlAsPdf(String content) {
    return htmlAsPdf(content, CharEncoding.UTF_8, null, null);
  }

  /**
   * 
   * Turns a valid XHTML document string and renders it as a PDF document.
   * 
   * @param content
   *          a string containing valid XHTML markup
   * @param encoding
   *          the encoding type used by the html string
   * @return an NSData object containing raw PDF data.
   */
  public static NSData htmlAsPdf(String content, String encoding) {
    return htmlAsPdf(content, encoding, null, null);
  }

  /**
   * 
   * Turns a valid XHTML document string and renders it as a PDF document.
   * 
   * @param content
   *          a string containing valid XHTML markup
   * @param encoding
   *          the encoding type used by the html string
   * @param urlPrefix
   *          the URL prefix to prepend to unqualified URLs in the html
   * @return an NSData object containing raw PDF data.
   */
  public static NSData htmlAsPdf(String content, String encoding, String urlPrefix) {
    return htmlAsPdf(content, encoding, urlPrefix, null);
  }

  /**
   * 
   * Turns a valid XHTML document string and renders it as a PDF document.
   * 
   * @param html
   *          a string containing valid XHTML markup
   * @param encoding
   *          the encoding type used by the html string
   * @param urlPrefix
   *          the URL prefix to prepend to unqualified URLs in the html
   * @param config
   *          a dictionary of binding values used to configure the rending
   *          engine specified in the "engine" key. See the chosen rendering
   *          engine's documentation for available config options.
   * @return an NSData object containing raw PDF data.
   */
  public static NSData htmlAsPdf(String html, String encoding, String urlPrefix, NSDictionary<String, Object> config) {
    NSMutableDictionary<String, Object> _config = config == null ? new NSMutableDictionary<String, Object>() : config.mutableClone();
    PDFBuilder builder = PDFBuilderFactory.newBuilder((String) _config.removeObjectForKey("engine"));
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      builder.setSource(html, encoding, urlPrefix, _config);
      builder.createDocument(os);
      os.close();
      return new NSData(os.toByteArray());
    } catch (Exception e) {
      throw com.webobjects.foundation.NSForwardException._runtimeExceptionForThrowable(e);
    }
  }
  
  
  
	/**
	 * takes xml as a string and a transform document as a url and transforms it
	 * to pdf like freakin' magic.
	 * 
	 * @param xml a string of xml to be passed into the transformation process 
	 * @param fopxsl the location of the xml-&gt;fo transform sheet (should be in the classpath)
	 * @param config dictionary of additional configuration elements for the fop engine 
	 * @return NSData raw pdf file contents
	 * @throws Throwable java.io.IOException 
	 */
	public static NSData xml2Fop2Pdf(String xml, String fopxsl, NSDictionary<String, Object> config) throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("xml2Fop2Pdf(String xml (length)=" + xml.length() + ", String fopxsl=" + fopxsl + ", NSDictionary<String,Object> config=" + config + ") - start"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}


		NSMutableDictionary<String, Object> _config = config == null ? new NSMutableDictionary<String, Object>() : config.mutableClone();
		FOPBuilder fopb = FOPBuilderFactory.newBuilder();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			fopb.setConfiguration(_config);
			fopb.setXML(xml);
			fopb.setXSL(fopxsl);
			fopb.createDocument(os);
			os.close();
			NSData returnNSData = new NSData(os.toByteArray());
			if (logger.isDebugEnabled()) {
				logger.debug("xml2Fop2Pdf(String, String, NSDictionary<String,Object>) - end - return value=" + returnNSData); //$NON-NLS-1$
			}
			return returnNSData;
		} catch (java.io.IOException e) {
			logger.error("xml2Fop2Pdf(String, String, NSDictionary<String,Object>)", e); //$NON-NLS-1$

			throw com.webobjects.foundation.NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

}
