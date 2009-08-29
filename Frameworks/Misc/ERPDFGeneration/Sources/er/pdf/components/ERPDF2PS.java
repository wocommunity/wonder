package er.pdf.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXProperties;

/**
 * ERPDF2PS will render the containing PDF content as a Postscript document suitable for printing.
 * You must have the pdftops binary from xPDF / Poppler installed. The path to the binary is 
 * configured using the er.pdf.pdftops system property and defaults to /usr/local/bin/pdftops if
 * not set.
 * This is intended to be used as a wrapper around ERPDFWrapper
 * 
 * @binding enabled
 * @binding duplex
 * 
 * @author q
 */
public class ERPDF2PS extends WOComponent {
  public static final String PDF_TO_PS_KEY = "er.pdf.pdftops";
  private String pdftops = ERXProperties.stringForKeyWithDefault(PDF_TO_PS_KEY, "/usr/local/bin/pdftops");
  private boolean _duplex = false;
  private boolean _enabled = true;

  public ERPDF2PS(WOContext context) {
    super(context);
  }

  public boolean duplex() {
    return _duplex;
  }
  
  public void setDuplex(boolean duplex) {
    _duplex = duplex;
  }

  public boolean enabled() {
    return _enabled;
  }
  
  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }
  
  @Override
  public boolean isStateless() {
    return true;
  }
  
  @Override
  public void reset() {
    _duplex = false;
    _enabled = true;
  }
  
  @Override
  public void appendToResponse(WOResponse response, WOContext aContext) {
    super.appendToResponse(response, aContext);
    if (_enabled) {
      File tempFile = null;
      File psFile = null;
      try {
        NSData content = response.content();
        tempFile = File.createTempFile("pdftops", "pdf");
        tempFile.deleteOnExit();
        psFile = File.createTempFile("pdftops", "ps");
        psFile.deleteOnExit();
        NSMutableArray<String> array = new NSMutableArray<String>(pdftops, tempFile.getPath(), psFile.getPath());
        if (_duplex) {
          array.add(1, "-duplex");
        }
        content.writeToStream(new FileOutputStream(tempFile));
        Process process = Runtime.getRuntime().exec(array.toArray(new String[array.size()]));
        process.waitFor();
        NSData data = new NSData(new FileInputStream(psFile), 4096);
        String header = response.headerForKey("content-disposition");
        response.setHeader(header.replace(".pdf", ".ps"), "content-disposition");
        response.setHeader("application/postscript", "Content-Type");
        response.setHeader(String.valueOf(data.length()), "Content-Length");
        response.setContent(data);
      } catch (FileNotFoundException e) {
        throw NSForwardException._runtimeExceptionForThrowable(e);
      } catch (IOException e) {
        throw NSForwardException._runtimeExceptionForThrowable(e);
      } catch (InterruptedException e) {
        throw NSForwardException._runtimeExceptionForThrowable(e);
      } finally {
        if (tempFile != null)
          tempFile.delete();
        if (psFile != null)
          psFile.delete();
      }
    }
  }
}