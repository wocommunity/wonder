/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERXSessionAdditions.java created by max on Fri 30-Mar-2001 */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import org.apache.log4j.Category;
import java.util.*;

public abstract class ERXSessionAddition implements ERXSessionAdditionInterface {

    //////////////////////////////////////////  log4j category  ///////////////////////////
    public final static Category cat = Category.getInstance(ERXSessionAddition.class);

    public ERXApplication erxApplication() { return (ERXApplication)WOApplication.application(); }
    
    private ERXSession _session;
    public ERXSession erxSession() { return _session; }
    public void setSession(ERXSession session) {
        _session = session;
        if (_session == null)
            flushSessionCache();
    }

    public void flushSessionCache() {}
}
