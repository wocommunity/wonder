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
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.lowagie.text.DocumentException;
import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;

public class FlyingSaucerImpl implements PDFBuilder {
  private static final DocumentBuilderFactory _builderFactory;

  private ITextRenderer renderer = new ITextRenderer();
  private Document doc;
  private ErrorHandler errorHandler;
  private SAXParseException exception;

  static {
    _builderFactory = DocumentBuilderFactory.newInstance();
    _builderFactory.setValidating(false);
    _builderFactory.setNamespaceAware(false);
    _builderFactory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
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
      builder.setErrorHandler(errorHandler);
    } catch (ParserConfigurationException e) {
      throw NSForwardException._runtimeExceptionForThrowable(e);
    }
    InputStream is = new ByteArrayInputStream(document.getBytes(encoding));
    ITextFontResolver resolver = renderer.getFontResolver();
    for (String font : fontsFromConfiguration(configuration)) {
      try {
        resolver.addFont(font, true);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    try {
      doc = builder.parse(is);
      if (exception != null)
        throw exception;
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
    NSMutableArray<String> result = new NSMutableArray<String>();
    String framework = (String) configuration.objectForKey("framework");
    if (fonts != null) {
      for (String font : fonts) {
        URL path = WOApplication.application().resourceManager().pathURLForResourceNamed(font, framework, (NSArray<String>)null);
        if (path != null)
          result.addObject(path.getFile());
      }
    }
    return result;
  }
}
