package er.pdf.builder;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.webobjects.foundation.NSDictionary;

public interface PDFBuilder {
  public void setSource(String document, String encoding, String urlPrefix, NSDictionary<String, Object> configuration) throws UnsupportedEncodingException;
  public void createDocument(OutputStream os);
}
