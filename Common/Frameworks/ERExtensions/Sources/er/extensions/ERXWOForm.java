//
// ERXWOForm.java
// Project armehaut
//
// Created by ak on Mon Apr 01 2002
//

package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

/** Transparent replacement for WOForm.
 *  It adds the Forms name to the ERXContext's mutableUserInfo as as "formName" key,
 *  which makes writing JavaScript elements a bit easier.
 *  Also, it warns you when you have one Form embedded inside another.
 *  Should be fixed to skip writing out the Form tag, which would make the
 *  ERXOptionalForm obsolete.
 *  Additionally, it pushes the <code>enctype</code> into the userInfo, so that {@link ERXWOFileUpload}
 *  can check if it is set correctly.
 *  This subclass is installed when the frameworks loads. 
 */  
public class ERXWOForm extends com.webobjects.appserver._private.WOForm {
    static final ERXLogger log = ERXLogger.getERXLogger(ERXWOForm.class);
    WOAssociation _formName;
    WOAssociation _enctype;
    
    public ERXWOForm(String name, NSDictionary associations,
                     WOElement template) {
        super(name, associations, template);
        _formName = (WOAssociation) _associations.removeObjectForKey("name");
        _enctype = (WOAssociation) _associations.removeObjectForKey("enctype");
    }

    public void appendAttributesToResponse(WOResponse response, WOContext context) {
        if(context != null && context instanceof ERXMutableUserInfoHolderInterface && _formName != null) {
            NSMutableDictionary ui = ((ERXMutableUserInfoHolderInterface)context).mutableUserInfo();
            String formName = (String)_formName.valueInComponent(context.component());
            if(formName != null) {
                ui.setObjectForKey(formName, "formName");
                response._appendTagAttributeAndValue("name", formName, false);
            }
            String enctype = (String)_enctype.valueInComponent(context.component());
            if(enctype != null) {
                ui.setObjectForKey(enctype.toLowerCase(), "enctype");
                response._appendTagAttributeAndValue("enctype", enctype, false);
            }
        }
        super.appendAttributesToResponse(response, context);
    }

    public void appendToResponse(WOResponse response, WOContext context) {
        if(context.isInForm())
            log.warn("This Form is embedded in another Form.");
        super.appendToResponse(response, context);
        if(context instanceof ERXMutableUserInfoHolderInterface) {
            NSMutableDictionary ui = ((ERXMutableUserInfoHolderInterface)context).mutableUserInfo();
            ui.removeObjectForKey("formName");
            ui.removeObjectForKey("enctype");
        }
    }
}