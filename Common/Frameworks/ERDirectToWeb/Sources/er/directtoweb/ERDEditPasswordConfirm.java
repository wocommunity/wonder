/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.EOEnterpriseObject;
import er.extensions.*;
import org.apache.log4j.*;

public class ERDEditPasswordConfirm extends ERDCustomEditComponent {
    public static final Category cat = Category.getInstance(ERDEditPasswordConfirm.class);
    
    public ERDEditPasswordConfirm(WOContext context) { super(context); }

    public String password;
    public String passwordConfirm;

    public void fail(String errorCode) {
        if(cat.isDebugEnabled())
            cat.debug("fail:<object:" + object() + "; key:" + key() + ";  password: " + password + "; code:" + errorCode + ";>");
        validationFailedWithException(ERXValidationFactory.defaultFactory().createException(object(), key(), password, errorCode), password, key());
    }
    
    public boolean passwordExists() { return objectKeyPathValue() != null ? true : false; }

    public void setObject(EOEnterpriseObject newObject) {
        if (newObject!=object()) {
            password=passwordConfirm=null;
        }
        super.setObject(newObject);
    }

    public void takeValuesFromRequest(WORequest r, WOContext c){
        super.takeValuesFromRequest(r,c);
        if (password==null || password.equals("") ||
            passwordConfirm==null || passwordConfirm.equals("")) {
            // one or both is null or empty
            if (objectKeyPathValue()==null)
                fail ("PasswordsFillBothFieldsException");
            else {
                // if we already have a value, then they need to both be null||empty
                if (!(password==null || password.equals("")) && (passwordConfirm==null || passwordConfirm.equals("")))
                    fail("PasswordsFillBothFieldsException");
            }
        } else {
            // they are both non-null
            if(!password.equals(passwordConfirm))
                fail("PasswordsDontMatchException");
            else {
                if (object() instanceof NSValidation) {
                    try {
                        object().validateTakeValueForKeyPath(password, key());
                    } catch (NSValidation.ValidationException e) {
                        validationFailedWithException(e, password, key());
                    }
                } else
                    object().validateTakeValueForKeyPath(password, key());
            }
        }
    }
}
