/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERBranchDelegate.java created by max on Wed 04-Oct-2000 */
package er.directtoweb;

import com.webobjects.directtoweb.*;
import com.webobjects.appserver.*;
import java.lang.reflect.*;
import org.apache.log4j.Category;
import er.extensions.ERXUtilities;

public class ERDBranchDelegate implements NextPageDelegate {

    ///////////////////////////////////////////  log4j category  //////////////////////////////////////////
    public final static Category cat = Category.getInstance("er.directtoweb.delegates.ERDBranchDelegate");

    public final static Class[] WOComponentClassArray = new Class[] { WOComponent.class };
    
    public WOComponent nextPage(WOComponent sender) {
        WOComponent nextPage = null;
        if (sender instanceof ERDBranchInterface) {
            String branchName = ((ERDBranchInterface)sender).branchName();
            try {
                Method m = getClass().getMethod(branchName, WOComponentClassArray);
                nextPage = (WOComponent)m.invoke(this, new Object[] { sender });
            } catch (InvocationTargetException ite) {
                cat.error("Invocation exception occurred in ERBranchDelegate: " + ite.getTargetException() + " backtrace: " +
                          ERXUtilities.stackTrace(ite.getTargetException()) + " for branch name: " + branchName);
            } catch (Exception e) {
                cat.error("Exception occurred in ERBranchDelegate: " + e.toString() + " for branch name: " + branchName);
            }
        } else {
            cat.warn("Branch delegate being used with a component that does not implement the ERBranchInterface");
        }
        return nextPage;
    }
}
