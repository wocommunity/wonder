/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

public class ERD2WEditToManyRelationship extends D2WEditToManyRelationship {

    public ERD2WEditToManyRelationship(WOContext context) { super(context); }
    
    // Validation Support
    public void validationFailedWithException (Throwable e, Object value, String keyPath) {
        parent().validationFailedWithException(e,value,keyPath);
    }
    
    public Object restrictedChoiceList() {
        String restrictedChoiceKey=(String)d2wContext().valueForKey("restrictedChoiceKey");
        return restrictedChoiceKey!=null &&  restrictedChoiceKey.length()>0 ? valueForKeyPath(restrictedChoiceKey) : null;
    }

    private Object _selectedChoiceList = null;
    public Object selectedChoiceList() {
        if (_selectedChoiceList == null) {
            String selectedChoiceKey=(String)d2wContext().valueForKey("selectedChoiceKey");
            _selectedChoiceList = selectedChoiceKey!=null && selectedChoiceKey.length()>0 ? valueForKeyPath(selectedChoiceKey) : null;
        }
        return _selectedChoiceList;
    }
    public void setSelectedChoiceList(Object selectedChoiceList) {
        _selectedChoiceList = selectedChoiceList;
    }

    public boolean shouldShowSelectAllButtons() {
        boolean result = false;
        if (canGetValueForBinding("shouldShowSelectAllButtons")) {
            result = ((Integer)valueForBinding("shouldShowSelectAllButtons")).intValue() != 0;
        } else {
            Integer tmp = (Integer)d2wContext().valueForKey("shouldShowSelectAllButtons");
            if (tmp != null)
                result = tmp.intValue() != 0;
        }
        return result;
    }
}
