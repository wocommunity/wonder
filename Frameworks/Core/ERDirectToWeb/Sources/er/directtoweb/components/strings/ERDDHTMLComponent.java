package er.directtoweb.components.strings;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Rich text edit component.<br />
 * @deprecated use {@link ERDEditHTML} instead
 */
@Deprecated
public class ERDDHTMLComponent extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    static final Logger log = Logger.getLogger(ERDDHTMLComponent.class);

    String varName = null;

    public ERDDHTMLComponent(WOContext context) {
        super(context);
    }

    @Override
    public boolean isStateless() {
	return false;
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    @Override
    public void reset() {
        super.reset();
        varName = null;
    }
    
    public String varName()  {
	if(varName == null) {
	    varName = StringUtils.replace("dhtml-" + context().elementID().hashCode() + "-" + key(), "-", "_");
	    varName = StringUtils.replace(varName, ".", "_");
	    log.debug(varName);
	}
	return varName;
    }

    @Override
    public void takeValuesFromRequest(WORequest q, WOContext c) throws NSValidation.ValidationException {
        super.takeValuesFromRequest(q,c);
        try {
            object().validateTakeValueForKeyPath(objectKeyPathValue(),key());
        } catch(Throwable e) {
            validationFailedWithException (e, objectKeyPathValue(), key());
        }
    }
}
