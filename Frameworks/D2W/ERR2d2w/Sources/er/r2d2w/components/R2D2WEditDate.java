package er.r2d2w.components;

import java.text.Format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.validation.ERXValidationException;
import er.extensions.validation.ERXValidationFactory;

public class R2D2WEditDate extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(R2D2WEditDate.class);
    private static final String _COMPONENT_CLASS = "date";

    private String labelID;
	private String dateString;

	public R2D2WEditDate(WOContext context) {
		super(context);
	}

    public void reset() {
    	super.reset();
    	dateString = null;
    	labelID = null;
    }

	public void appendToResponse(WOResponse r, WOContext c) {
		Object date = objectPropertyValue();
		if (date != null) {
			setDateString(dateFormatter().format(date));
		} else {
			setDateString(null);
		}
		super.appendToResponse(r, c);
	}

	public void takeValuesFromRequest(WORequest request, WOContext context) {
		super.takeValuesFromRequest(request, context);
		Object obj = null;
		try {
			if (dateString() != null) {
				obj = dateFormatter().parseObject(dateString());
			}
		} catch (Exception e) {
			log.debug("Exception:" + e);
			ERXValidationException v = ERXValidationFactory.defaultFactory().createException(object(), propertyKey(), dateString(), "InvalidDateFormatException");
			parent().validationFailedWithException(v, obj, propertyKey());
		}
		
		try {
			if (object() != null) {
				object().validateTakeValueForKeyPath(obj, propertyKey());
			}
		} catch (ValidationException e) {
			log.debug("NSValidation.ValidationException:" + e);
			parent().validationFailedWithException(e, obj, propertyKey());
		}
	}

	public Format dateFormatter() {
		Format result = (Format)d2wContext().valueForKey("formatObject");
		return result;
	}

	/**
	 * @return the dateString
	 */
	public String dateString() {
		return dateString;
	}

	/**
	 * @param dateString
	 *            the dateString to set
	 */
	public void setDateString(String dateString) {
		this.dateString = dateString;
	}
	
    public String componentClasses() {
    	return _COMPONENT_CLASS;
    }
    
	public String labelID() {
		if(labelID == null) {
			labelID = ERXStringUtilities.safeIdentifierName(context().elementID(), "id", '_');
		}
		return labelID;
	}

}