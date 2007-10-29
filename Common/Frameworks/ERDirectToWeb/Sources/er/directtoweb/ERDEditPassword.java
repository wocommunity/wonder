package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSValidation;

import er.extensions.ERXValidationFactory;
import er.extensions.ERXWOContext;

/**
 * Provides a edit "password" service. Should be
 * used in conjunction with {@link ERDEditPasswordConfirmation}, as it puts the
 * the  password value into the <code>context.mutableUserInfo</code>.
 * From where it can be confirmed against a second edit field.
 * In case you don't need a second field, then the routine is:
 * if no password is set or it is set and there is a value given, then
 * the object is asked to validate this value.
 * This behaviour prevents having to enter the password multiple times.
 *
 * @binding object the object to edit
 * @binding propertyKey the key of the object to edit
 * @binding length the length of the text field
 * @binding passwordConfirmationValidates if true, then the property key validation is left to the confirmation component. Otherwise the validation occurs here.
 * @d2wKey length the length of the text field
 *
 * @created ak on Sun Aug 17 2003
 * @project ERDirectToWeb
 */

public class ERDEditPassword extends ERDCustomEditComponent {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDEditPassword.class);

    public static final String passwordPropertyKey = "ERDEditPassword.propertyKey";
    
    /**
     * Public constructor
     * @param context the context
     */
    public ERDEditPassword(WOContext context) {
        super(context);
    }

    public boolean synchronizesVariablesWithBindings() { return false; }
    
    protected Boolean _passwordConfirmationValidates;
    public boolean passwordConfirmationValidates() {
        if(_passwordConfirmationValidates == null) {
            _passwordConfirmationValidates = booleanValueForBinding("passwordConfirmationValidates") ? Boolean.TRUE : Boolean.FALSE;
        }
        return _passwordConfirmationValidates.booleanValue();
    }

    protected String _password;
    public String password() {
        return _password;
    }
    public void setPassword(String value) {
        _password = value;
    }

    public void setObject(EOEnterpriseObject newObject) {
        if (newObject!=object()) {
            setPassword(null);
        }
        super.setObject(newObject);
    }

    public void fail(String errorCode) {
        if(log.isDebugEnabled())
            log.debug("fail:<object:" + object() + "; key:" + key() + ";  password: " + password() + "; code:" + errorCode + ";>");
        validationFailedWithException(ERXValidationFactory.defaultFactory().createException(object(), key(), password(), errorCode), password(), key());
    }

    public boolean passwordExists() { return objectKeyPathValue() != null ? true : false; }

    protected void updateContextValues() {
        NSMutableDictionary userInfo = new NSMutableDictionary();
        userInfo.setObjectForKey(key(), passwordPropertyKey);
        if (_password!=null) {
			userInfo.setObjectForKey(_password, "ERDEditPassword." + key() + ".value");
		} else {
			userInfo.removeObjectForKey("ERDEditPassword." + key() + ".value");
		}
        ERXWOContext.contextDictionary().setObjectForKey(userInfo, "ERDEditPassword");
    }

    public void appendToResponse(WOResponse r, WOContext c) {
        super.appendToResponse(r,c);
        if(passwordConfirmationValidates()) {
            updateContextValues();
        }
    }
    
    public void takeValuesFromRequest(WORequest r, WOContext c) {
        super.takeValuesFromRequest(r,c);
        if (c._wasFormSubmitted()) {
        	if(passwordConfirmationValidates()) {
        		updateContextValues();
        	} else {
        		String password = password();
        		if(!passwordExists() || (passwordExists() && password != null)) {
        			try {
        				object().validateTakeValueForKeyPath(password, key());
        			} catch(NSValidation.ValidationException ex) {
        				validationFailedWithException(ex, password, key());
        			}
        		}
        	}
		}
    }
}
