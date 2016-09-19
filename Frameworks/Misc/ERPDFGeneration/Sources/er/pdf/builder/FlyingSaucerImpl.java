package er.pdf.builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.FSEntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXProperties;

/**
 *
 * @property er.pdf.validation
 */
public class FlyingSaucerImpl implements PDFBuilder {
  private static final DocumentBuilderFactory _builderFactory;

  private ITextRenderer renderer = new ITextRenderer();
  private Document doc;
  private ErrorHandler errorHandler;
  private SAXParseException exception;

  static {
    _builderFactory = DocumentBuilderFactory.newInstance();
    _builderFactory.setValidating(ERXProperties.booleanForKeyWithDefault("er.pdf.validation", false));
    _builderFactory.setNamespaceAware(true);
    _builderFactory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
  }

  public FlyingSaucerImpl() {
    errorHandler = new ErrorHandler() {

      public void error(SAXParseException e) throws SAXException {
        exception = e;
      }

      public void fatalError(SAXParseException e) throws SAXException {
        exception = e;
      }

      public void warning(SAXParseException e) throws SAXException {
        exception = e;
      }
    };
    
    ReplacedElementFactory ref = new ERPDFReplacedElementFactory(renderer.getOutputDevice());
    renderer.getSharedContext().setReplacedElementFactory(ref);
  }
  
  public void createDocument(OutputStream os) {
    try {
      renderer.createPDF(os);
    } catch (DocumentException e) {
      throw NSForwardException._runtimeExceptionForThrowable(e);
    }
  }

  public void setSource(String document, String encoding, String urlPrefix, NSDictionary<String, Object> configuration) throws UnsupportedEncodingException {
    DocumentBuilder builder;
    try {
      exception = null;
      builder = _builderFactory.newDocumentBuilder();
      builder.setEntityResolver(FSEntityResolver.instance());
      builder.setErrorHandler(errorHandler);
    } catch (ParserConfigurationException e) {
      throw NSForwardException._runtimeExceptionForThrowable(e);
    }
    InputStream is = new ByteArrayInputStream(document.getBytes(encoding));
    ITextFontResolver resolver = renderer.getFontResolver();
    for (String font : fontsFromConfiguration(configuration)) {
      try {
        resolver.addFont(font, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    try {
      doc = builder.parse(is);
      if (exception != null)
        throw exception;
      
	  // Hook up a metadata listener to push keys and values to PDF
      FlyingSaucerMetadataCreationListener mcl = new FlyingSaucerMetadataCreationListener();
      mcl.parseMetaTags(doc);
      renderer.setListener(mcl);
      
      renderer.setDocument(doc, urlPrefix);
      renderer.layout();
    } catch (Exception e) {
      throw NSForwardException._runtimeExceptionForThrowable(e);
    }
  }

  @SuppressWarnings("unchecked")
  private NSArray<String> fontsFromConfiguration(NSDictionary<String, Object> configuration) {
    if (configuration == null)
      return NSArray.emptyArray();
    NSArray<String> fonts = (NSArray<String>) configuration.objectForKey("fonts");
    NSMutableArray<String> result = new NSMutableArray<>();
    String framework = (String) configuration.objectForKey("framework");
    if (fonts != null) {
      for (String font : fonts) {
        URL path = WOApplication.application().resourceManager().pathURLForResourceNamed(font, framework, null);
        if (path != null)
          result.addObject(path.getFile());
      }
    }
    return result;
  }
}
