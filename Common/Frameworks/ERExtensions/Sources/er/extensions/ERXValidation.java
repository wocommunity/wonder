/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERValidation.java created by patrice on Mon 27-Mar-2000 */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import java.lang.*;
import java.lang.reflect.*;
import java.util.*;
import org.apache.log4j.Category;

public class ERXValidation {

    /////////////////////////////////////////////  log4j category  /////////////////////////////////////////
    public static final Category cat = Category.getInstance("er.validation.ERValidation");

    public final static boolean PUSH_INCORRECT_VALUE_ON_EO=true;
    public final static boolean DO_NOT_PUSH_INCORRECT_VALUE_ON_EO=false;

    private static boolean pushChangesDefault = DO_NOT_PUSH_INCORRECT_VALUE_ON_EO;
    public static void setPushChangesDefault(boolean val) {
        pushChangesDefault = val;
    }
    
    private static D2WContext propertyNameContext = new D2WContext();

    public static void validationFailedWithException(Throwable e,
                                                     Object value,
                                                     String keyPath,
                                                     NSMutableDictionary errorMessages,
                                                     String displayPropertyKeyPath,
                                                     ERXLocalizer localizer
                                                     ) {
        validationFailedWithException(e,value,keyPath,errorMessages,displayPropertyKeyPath,localizer,null);
    }

    public static void validationFailedWithException(Throwable e,
                                                     Object value,
                                                     String keyPath,
                                                     NSMutableDictionary errorMessages,
                                                     String displayPropertyKeyPath,
                                                     ERXLocalizer localizer,
                                                     EOEntity entity
                                                     ) {
        validationFailedWithException(e,value,keyPath,errorMessages, displayPropertyKeyPath, localizer, entity, pushChangesDefault);
    }

    public static void validationFailedWithException(Throwable e,
                                                     Object value,
                                                     String keyPath,
                                                     NSMutableDictionary errorMessages,
                                                     String displayPropertyKeyPath,
                                                     ERXLocalizer localizer,
                                                     EOEntity entity,
                                                      boolean pushChanges
                                                     ) {
        if (cat.isDebugEnabled())
            cat.debug("ValidationFailedWithException: " + e.getClass().getName() + " message: " + e.getMessage());
        boolean addKeyToErrorMessage=false;
        String key = null;
        String newErrorMessage=e.getMessage();
        // Need to reset the context for each validation exception.
        propertyNameContext.setEntity(null);
        
        if (e instanceof NSValidation.ValidationException && ((NSValidation.ValidationException)e).key() != null
            && ((NSValidation.ValidationException)e).object() != null) {
            NSValidation.ValidationException nve = (NSValidation.ValidationException)e;
            key = nve.key();
            Object eo=nve.object();
            // this because exceptions raised by formatters have the failing VALUE in this key..
            // strip the exception name
            newErrorMessage=newErrorMessage.substring(newErrorMessage.indexOf(":")+1);
            newErrorMessage=newErrorMessage.substring(newErrorMessage.indexOf(":")+1);
            if (eo instanceof EOEnterpriseObject) {
                // the exception is coming from EREnterpriseObject
                // WE PUSH THE WRONG VALUE INTO THE EO ANYWAY!
                if (pushChanges)  {
                    ((EOEnterpriseObject)eo).takeValueForKeyPath(value, key);
                }
                // Setting the entity on the context
                propertyNameContext.setEntity(EOUtilities.entityForObject(((EOEnterpriseObject)eo).editingContext(),
                                                                          (EOEnterpriseObject)eo));
            } else {
                //the exception is coming from a formatter
                key=(String)NSArray.componentsSeparatedByString(displayPropertyKeyPath,".").lastObject();
                newErrorMessage="<b>"+key+"</b>:"+newErrorMessage;
                if (entity!=null)
                    propertyNameContext.setEntity(entity);
            }
        } else {
            key = keyPath;
            if (entity!=null)
                propertyNameContext.setEntity(entity);
        }
        // Leveraging the power of D2WContext to generate great looking error messages.
        if (propertyNameContext.entity() != null && key != null) {
            propertyNameContext.setPropertyKey(key);
            //FIXME: (ak) this is just until I can rethink the whole message processing
            NSMutableDictionary fakeSession = new NSMutableDictionary(localizer, "localizer");
            propertyNameContext.takeValueForKey( fakeSession, "session");
            if(newErrorMessage != null)
                errorMessages.setObjectForKey(newErrorMessage, propertyNameContext.displayNameForProperty());
        } else {
            errorMessages.setObjectForKey(newErrorMessage, key);
        }
    }

    public static String displayNameForPropertyWithEO(String propertyKey, EOEnterpriseObject eo){
        EOEntity entity = EOUtilities.entityForObject(eo.editingContext(),
                                                      eo);
        return displayNameForPropertyWithEntity(propertyKey, entity);
    }

    public static String displayNameForPropertyWithEntity(String propertyKey, EOEntity entity){
        String result = null;
        if (entity != null && propertyKey != null) {
            propertyNameContext.setPropertyKey(propertyKey);
            propertyNameContext.setEntity(entity);
            result = propertyNameContext.displayNameForProperty();
        } else {
            result = propertyKey;
        }
        return result;
    }
}
