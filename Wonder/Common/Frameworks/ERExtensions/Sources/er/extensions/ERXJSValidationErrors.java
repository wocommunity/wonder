package er.extensions;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

/**
 * Server side part of the JavaScript validation
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Fri May 02 2003
 * @project ERExtensions
 */

public class ERXJSValidationErrors extends ERXStatelessComponent {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERXJSValidationErrors.class,"components");

    public String _errors;
    public String _callback;

    /**
     * Public constructor
     * @param context the context
     */
    public ERXJSValidationErrors(WOContext context) {
        super(context);
    }

    public String callback() { return "parent." + _callback; }
    public void awake() {
        String key = context().request().stringFormValueForKey("_vkey");
        String value = context().request().stringFormValueForKey("_vvalue");
        String entity = context().request().stringFormValueForKey("_ventityName");

        _callback = context().request().stringFormValueForKey("callback");

        Object newValue = value;

        log.info("validateKeyAndValueInEntityAction: key="+key+", value="+value+", entity="+entity + ", callback=" + _callback);

        try {
            EOClassDescription cd = EOClassDescription.classDescriptionForEntityName(entity);
            if(cd != null)
                newValue = cd.validateValueForKey(value, key);
        } catch (NSValidation.ValidationException ex) {
            _errors = ex.getMessage();
        }
    }
    
    public void reset() { _errors = null; _callback = null;}
}
