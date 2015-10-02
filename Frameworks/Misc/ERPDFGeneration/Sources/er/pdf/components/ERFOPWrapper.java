package er.pdf.components;

import java.util.Map;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.pdf.ERPDFUtilities;
import er.pdf.ERPDFWrapper;

/**
 * ERFOPWrapper will render the containing component content as a PDF document using
 * Apache FOP.
 * 
 * @binding xml2fopXsl the location of the xml-&gt;fo transform sheet (should be in the classpath)
 * @author lmg42
 */
public class ERFOPWrapper extends ERPDFWrapper {
	public ERFOPWrapper(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
		super(name, associations, template);
	}

	@Override
	public NSData responseAsPdf(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		
		NSMutableDictionary<String, Object> config = new NSMutableDictionary<String, Object>();
	    for (Map.Entry<String, WOAssociation> entry : associations().entrySet()) {
	      Object value = entry.getValue().valueInComponent(component);
	      if (value != null) {
	        config.setObjectForKey(value, entry.getKey());
	      }
	    }
		String xml2fopxsl = stringValueForBinding("xml2fopXsl", component);
		NSData data = null;

		try {
			data = ERPDFUtilities.xml2Fop2Pdf(response.contentString(), xml2fopxsl, config);
			data = appendPDFs(data, context);
		} catch (Throwable e) {
			log.error(e, e);
		}
		
		return data;
	}
}