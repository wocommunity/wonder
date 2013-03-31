package er.pdf.components;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXResponse;
import er.extensions.appserver.ERXWOContext;
import er.pdf.ERPDFMerge;
import er.pdf.ERPDFUtilities;
import er.pdf.ERPDFWrapper;

/**
 * modeled on ERPDFWrapper in the ERPDF Framework of Project Wonder. I'm using
 * the classes in that framework as a model to try to fit this FOP transform
 * renderer into the ERPDF framework. Right now, there is a ujac and iText
 * renderer and I have a bunch of FOP to work with so this may be useful to
 * someone else eventually.
 * 
 * 
 * @author lmg42
 * 
 */
public class ERFOPWrapper extends WODynamicGroup implements WOActionResults {

	private static final Logger logger = Logger.getLogger(ERFOPWrapper.class);

	protected NSMutableDictionary<String, WOAssociation> _associations;
	protected WOAssociation _secure;
	protected WOAssociation _enabled;
	protected WOAssociation _filename;
	protected WOAssociation _additionalPDFs;
	protected WOAssociation _xml2fopxsl;
	protected WOElement _component;

	public ERFOPWrapper(String name, NSDictionary<String, WOAssociation> someAssociations, WOElement component) {
		super(name, someAssociations, component);
		_associations = someAssociations.mutableClone();
		_secure = _associations.removeObjectForKey("secure");
		_enabled = _associations.removeObjectForKey("enabled");
		_filename = _associations.removeObjectForKey("filename");
		_xml2fopxsl = _associations.removeObjectForKey("xml2fopXsl");
		_additionalPDFs = _associations.removeObjectForKey("additionalPDFs");
		if (logger.isDebugEnabled()) {
			logger.debug("XML2FOPWrapper(String, NSDictionary<String,WOAssociation>, WOElement) - WOAssociation _xml2fopxsl=" + _xml2fopxsl); //$NON-NLS-1$
			logger.debug("xsl class: " + _xml2fopxsl.getClass().getName());
		}

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

	public void responseAsPdf(WOResponse response, WOContext context) {

		NSMutableDictionary<String, Object> config = new NSMutableDictionary<String, Object>();
		String xml2fopxsl = null;
		NSData data = null;

		try {
			if (_xml2fopxsl != null) {
				xml2fopxsl = (String) _xml2fopxsl.valueInComponent(context.component());
			} else {
				throw new Exception(
						"I don't have a transform file to look for on the classpath. (I don't know if it can be found if I don't know where it is.)");
			}

			data = ERPDFUtilities.xml2Fop2Pdf(response.contentString(), xml2fopxsl, config);

			if (_additionalPDFs != null && _additionalPDFs.valueInComponent(context.component()) != null) {
				NSArray<String> additionalPDFs = (NSArray<String>) _additionalPDFs.valueInComponent(context.component());
				try {
					List<InputStream> pdfs = new ArrayList<InputStream>();
					pdfs.add(data.stream());
					Enumeration<String> e = additionalPDFs.objectEnumerator();
					while (e.hasMoreElements())
						pdfs.add(new FileInputStream(e.nextElement()));
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					ERPDFMerge.concatPDFs(pdfs, output, false);
					data = new NSData(output.toByteArray());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Throwable e1) {

			e1.printStackTrace();
		}
		String filename = _filename != null ? (String) _filename.valueInComponent(context.component()) : "result.pdf";
		response.setHeader("inline; filename=\"" + filename + "\"", "content-disposition");
		response.setHeader("application/pdf", "Content-Type");
		response.setHeader(String.valueOf(data.length()), "Content-Length");
		response.setContent(data);
	}

	/**
	 * same as ERPDFWrapper. appendToResponse doesn't work if I subclass
	 * ERPDFWrapper so we're both just subclasses of WODynamicGroup and this is
	 * the same as in ERPDFWrapper.
	 */
	public WOResponse generateResponse() {
		WOResponse response;
		if (_component instanceof WOActionResults) {
			response = ((WOActionResults) _component).generateResponse();
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