/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

public class ERD2WCustomDisplayBoolean extends D2WDisplayBoolean {

   public ERD2WCustomDisplayBoolean(WOContext context) {
        super(context);
    }
    
    protected NSArray _choicesNames;
    
    public NSArray choicesNames() {
         if(_choicesNames == null) {
             _choicesNames = (NSArray)d2wContext().valueForKey("choicesNames");
         }
         return _choicesNames;
     }

     public String yesName(){
         return (String)choicesNames().objectAtIndex(0);
     }

     public String noName(){
         return (String)choicesNames().objectAtIndex(1);
     }
}
