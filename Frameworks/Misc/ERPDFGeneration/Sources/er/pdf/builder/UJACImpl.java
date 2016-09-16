package er.pdf.builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.ujac.print.DocumentPrinter;
import org.ujac.util.io.HttpResourceLoader;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;

public class UJACImpl implements PDFBuilder {
  private DocumentPrinter documentPrinter;
  private Map<String, String> documentProperties = new HashMap<>();
  
  public void createDocument(OutputStream os) {
    try {
      documentPrinter.printDocument(os);
    } catch (Exception e) {
      NSForwardException._runtimeExceptionForThrowable(e);
    }
  }

  public void setSource(String document, String encoding, String urlPrefix, NSDictionary<String, Object> configuration) throws UnsupportedEncodingException {
    InputStream is = new ByteArrayInputStream(document.getBytes(encoding));
    documentPrinter = new DocumentPrinter(is, documentProperties);
    documentPrinter.setResourceLoader(new HttpResourceLoader(urlPrefix));    
  }

  
}
