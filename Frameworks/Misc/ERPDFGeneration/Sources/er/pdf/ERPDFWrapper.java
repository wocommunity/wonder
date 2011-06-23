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
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXRequest;
import er.extensions.appserver.ERXResourceManager;
import er.extensions.appserver.ERXResponse;
import er.extensions.appserver.ERXWOContext;

/**
 * ERPDFWrapper will render the containing component content as a PDF document.
 * The contained content must be valid XHTML markup suitable for processing by
 * the chosen rendering engine. ERPDFWrapper is intended to be the outer most
 * element on the page and should not have any trailing content or whitespace after
 * the closing tag.
 * 
 * @binding secure
 * @binding enabled
 * @binding filename
 * @binding fonts
 * @binding framework
 * @binding additionalPDFs
 * 
 * @author sharpy
 * @author q
 */
public class ERPDFWrapper extends WODynamicGroup implements WOActionResults {

  protected NSMutableDictionary<String, WOAssociation> _associations;
  protected WOAssociation _secure;
  protected WOAssociation _enabled;
  protected WOAssociation _filename;
  protected WOAssociation _additionalPDFs;
  protected WOElement _component;

  public ERPDFWrapper(String name, NSDictionary<String, WOAssociation> someAssociations, WOElement component) {
    super(name, someAssociations, component);
    _associations = someAssociations.mutableClone();
    _secure = _associations.removeObjectForKey("secure");
    _enabled = _associations.removeObjectForKey("enabled");
    _filename = _associations.removeObjectForKey("filename");
    _additionalPDFs = _associations.removeObjectForKey("additionalPDFs");
    _component = component;
  }

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
  	ERXResponse.setXHTML(response, true);
    boolean enabled = _enabled != null ? _enabled.booleanValueInComponent(context.component()) : true;

    super.appendToResponse(response, context);
    
    if (enabled) {
      responseAsPdf(response, context);
    }
  }

  protected void responseAsPdf(WOResponse response, WOContext context) {
    boolean secure = _secure != null ? _secure.booleanValueInComponent(context.component()) : ERXRequest.isRequestSecure(context.request());
    String resourceUrlPrefix = ERXResourceManager._completeURLForResource("", secure, context);
    
    NSMutableDictionary<String, Object> config = new NSMutableDictionary<String, Object>();
    for (Map.Entry<String, WOAssociation> entry : _associations.entrySet()) {
      Object value = entry.getValue().valueInComponent(context.component());
      if (value != null)
        config.setObjectForKey(value, entry.getKey());
    }
    
    NSData data = ERPDFUtilities.htmlAsPdf(response.contentString(), response.contentEncoding(), resourceUrlPrefix, config);
    if (_additionalPDFs != null && _additionalPDFs.valueInComponent(context.component()) != null) {
      NSArray<String> additionalPDFs = (NSArray<String>)_additionalPDFs.valueInComponent(context.component());
      try {
        List<InputStream> pdfs = new ArrayList<InputStream>();
        pdfs.add(data.stream());
        Enumeration<String> e = additionalPDFs.objectEnumerator();
        while (e.hasMoreElements()) pdfs.add(new FileInputStream(e.nextElement()));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ERPDFMerge.concatPDFs(pdfs, output, false);
        data = new NSData(output.toByteArray());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    String filename = _filename != null ? (String)_filename.valueInComponent(context.component()) : "result.pdf";

    response.setHeader("inline; filename=\"" + filename + "\"", "content-disposition");
    response.setHeader("application/pdf", "Content-Type");
    response.setHeader(String.valueOf(data.length()), "Content-Length");
    response.setContent(data);
  }

  public WOResponse generateResponse() {
    WOResponse response;
    if (_component instanceof WOActionResults) {
      response = ((WOActionResults)_component).generateResponse();
      responseAsPdf(response, ERXWOContext.currentContext());
    } else {
      WOContext context = ERXWOContext.currentContext();
      response = WOApplication.application().createResponseInContext(context);

      WOElement currentElement = context._pageElement();
      context._setPageElement(_component);
      appendToResponse(response, context);
      context._setPageElement(currentElement);
    }
    return response;
  }
}