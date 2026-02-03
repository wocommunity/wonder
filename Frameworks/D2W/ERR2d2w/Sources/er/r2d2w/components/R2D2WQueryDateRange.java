package er.r2d2w.components;

import java.text.Format;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2WQueryDateRange;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSValidation;

import er.extensions.foundation.ERXStringUtilities;
import er.extensions.validation.ERXValidationException;
import er.extensions.validation.ERXValidationFactory;

public class R2D2WQueryDateRange extends D2WQueryDateRange {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(R2D2WQueryDateRange.class);
	
    private String maxID;
	private String minID;
	private String maxDateString;
	private String minDateString;

	public R2D2WQueryDateRange(WOContext context) {
        super(context);
    }

	public void reset() {
		maxID = null;
		minID = null;
		maxDateString = null;
		minDateString = null;
		super.reset();
	}

	public void takeValuesFromRequest(WORequest request, WOContext context) {
		super.takeValuesFromRequest(request, context);
		Object obj = null;
		try {
			if (minDateString() != null) {
				obj = dateFormatter().parseObject(minDateString());
				setMinValue(obj);
			}
		} catch (ParseException e) {
			log.debug("java.text.ParseException:" + e);
			ERXValidationException v = ERXValidationFactory.defaultFactory().createException(object(), propertyKey(), minDateString(), "InvalidDateFormatException");
			parent().validationFailedWithException(v, obj, propertyKey());
		} catch (IllegalArgumentException e) {
			log.debug("IllegalArgumentException:" + e);
			ERXValidationException v = ERXValidationFactory.defaultFactory().createException(object(), propertyKey(), minDateString(), "InvalidDateFormatException");
			parent().validationFailedWithException(v, obj, propertyKey());
		} catch (NSValidation.ValidationException v) {
			log.debug("NSValidation.ValidationException:" + v);
			parent().validationFailedWithException(v, obj, propertyKey());
		} catch (Exception e) {
			//FIXME this does not seem to work
//			log.debug("Exception:" + e);
//			parent().validationFailedWithException(e, obj, propertyKey());
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
		
		try {
			if (maxDateString() != null) {
				obj = dateFormatter().parseObject(maxDateString());
				setMaxValue(obj);
			}
		} catch (ParseException e) {
			log.debug("java.text.ParseException:" + e);
			ERXValidationException v = ERXValidationFactory.defaultFactory().createException(object(), propertyKey(), maxDateString(), "InvalidDateFormatException");
			parent().validationFailedWithException(v, obj, propertyKey());
		} catch (IllegalArgumentException e) {
			log.debug("IllegalArgumentException:" + e);
			ERXValidationException v = ERXValidationFactory.defaultFactory().createException(object(), propertyKey(), maxDateString(), "InvalidDateFormatException");
			parent().validationFailedWithException(v, obj, propertyKey());
		} catch (NSValidation.ValidationException v) {
			log.debug("NSValidation.ValidationException:" + v);
			parent().validationFailedWithException(v, obj, propertyKey());
		} catch (Exception e) {
			//FIXME this does not seem to work
//			log.debug("Exception:" + e);
//			parent().validationFailedWithException(e, obj, propertyKey());
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	public Format dateFormatter() {
		Format result = (Format)d2wContext().valueForKey("formatObject");
		return result;
	}

	/**
	 * @return the maxID
	 */
	public String maxID() {
		if(maxID == null) {
			maxID = ERXStringUtilities.safeIdentifierName(context().elementID(),"id",'_');
		}
		return maxID;
	}

	/**
	 * @return the minID
	 */
	public String minID() {
		if(minID == null) {
			minID = ERXStringUtilities.safeIdentifierName(context().elementID(),"id",'_');
		}
		return minID;
	}

	/**
	 * @return the maxDateString
	 */
	public String maxDateString() {
		return maxDateString;
	}

	/**
	 * @param maxDateString the maxDateString to set
	 */
	public void setMaxDateString(String maxDateString) {
		this.maxDateString = maxDateString;
	}

	/**
	 * @return the minDateString
	 */
	public String minDateString() {
		return minDateString;
	}

	/**
	 * @param minDateString the minDateString to set
	 */
	public void setMinDateString(String minDateString) {
		this.minDateString = minDateString;
	}
}