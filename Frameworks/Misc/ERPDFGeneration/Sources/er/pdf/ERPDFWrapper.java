package er.pdf;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXRequest;
import er.extensions.appserver.ERXResourceManager;
import er.extensions.appserver.ERXResponse;
import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXDynamicElement;

/**
 * ERPDFWrapper will render the containing component content as a PDF document.
 * The contained content must be valid XHTML markup suitable for processing by
 * the chosen rendering engine. ERPDFWrapper is intended to be the outer most
 * element on the page and should not have any trailing content or whitespace after
 * the closing tag.
 * 
 * @binding secure <code>true</code> if HTTPS should be used for unqualified URLs
 *          in the HTML, defaults to the request type
 * @binding enabled <code>true</code> if a PDF should be created instead of HTML
 *          during appendToResponse phase defaults to <code>true</code>
 * @binding filename the filename on the client, defaults to <i>result.pdf</i>
 * @binding fonts (optional) array of font filenames to include for PDF generation
 * @binding framework (optional) framework name where font files are
 * @binding additionalPDFs (optional) array of PDF filenames to append to the
 *          generated PDF
 * 
 * @author sharpy
 * @author q
 */
public class ERPDFWrapper extends ERXDynamicElement implements WOActionResults {
  protected WOElement _component;

  public ERPDFWrapper(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
    super(name, associations, template);
    _component = template;
  }

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
    WOComponent component = context.component();
  	ERXResponse.setXHTML(response, true);
    boolean enabled = booleanValueForBinding("enabled", true, component);

    super.appendToResponse(response, context);
    
    if (enabled) {
      NSData data = responseAsPdf(response, context);
      
      String filename = stringValueForBinding("filename", "result.pdf", component);
      response.setHeader("inline; filename=\"" + filename + "\"", "content-disposition");
      response.setHeader("application/pdf", "Content-Type");
      response.setHeader(String.valueOf(data.length()), "Content-Length");
      response.setContent(data);
    }
  }

  protected NSData responseAsPdf(WOResponse response, WOContext context) {
    WOComponent component = context.component();
    boolean secure = booleanValueForBinding("secure", ERXRequest.isRequestSecure(context.request()), component);
    String resourceUrlPrefix = ERXResourceManager._completeURLForResource("", secure, context);
    
    NSMutableDictionary<String, Object> config = new NSMutableDictionary<>();
    for (Map.Entry<String, WOAssociation> entry : associations().entrySet()) {
      Object value = entry.getValue().valueInComponent(component);
      if (value != null) {
        config.setObjectForKey(value, entry.getKey());
      }
    }
    
    NSData data = ERPDFUtilities.htmlAsPdf(response.contentString(), response.contentEncoding(), resourceUrlPrefix, config);
    return appendPDFs(data, context);
  }
  
  /**
   * Appends PDFs that have been denoted by the <i>additionalPDFs</i> binding
   * to the current PDF data object.
   * 
   * @param data the current PDF data object
   * @param context context of the transaction
   * @return PDF data with appended PDFs or the unaltered data object if no
   *         files were given to append
   */
  protected NSData appendPDFs(NSData data, WOContext context) {
	  WOComponent component = context.component();
	  
	  NSArray<String> additionalPDFs = arrayValueForBinding("additionalPDFs", component);
	  if (additionalPDFs != null) {
	    try {
	      List<InputStream> pdfs = new ArrayList<>();
	      pdfs.add(data.stream());
	      Enumeration<String> e = additionalPDFs.objectEnumerator();
	      while (e.hasMoreElements()) {
	        pdfs.add(new FileInputStream(e.nextElement()));
	      }
	      ByteArrayOutputStream output = new ByteArrayOutputStream();
	      ERPDFMerge.concatPDFs(pdfs, output, false);
	      data = new NSData(output.toByteArray());
	    } catch (Exception e) {
	      log.error(e.getMessage(), e);
	    }
	  }
	  return data;
  }

  public WOResponse generateResponse() {
    WOResponse response;
    WOContext context = ERXWOContext.currentContext();
    if (_component instanceof WOActionResults) {
      response = ((WOActionResults)_component).generateResponse();
      responseAsPdf(response, context);
    } else {
      response = WOApplication.application().createResponseInContext(context);

      WOElement currentElement = context._pageElement();
      context._setPageElement(_component);
      appendToResponse(response, context);
      context._setPageElement(currentElement);
    }
    return response;
  }
}