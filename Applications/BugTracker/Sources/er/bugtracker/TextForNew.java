/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

import java.util.Date;

public class TextForNew extends WOComponent {

    public TextForNew(WOContext aContext) {
        super(aContext);
    }

    public EOEnterpriseObject object;
    public String key;
    public String newText;
    public Integer index;
    public Object extraBindings;

    public void setNewText(String newValue) {
        newText=newValue;
        EOEnterpriseObject user=((Session)session()).getUser();
        if (newValue!=null && !newValue.equals("")) {
            Date myDate = new Date();
            String authorInfo = "On "+java.text.DateFormat.getDateTimeInstance().format(myDate)+" "+user.valueForKey("name")+" wrote:";
            String existingText=object.valueForKey(key)!=null ? (object.valueForKey(key)+"\n\n") : "";
            String newText=existingText+authorInfo+"\n"+newValue;
            object.takeValueForKey(newText, key);  // not textDescription!
        }
    }
      
    public Object value() {
        return object.valueForKey(key);
    }	
}
