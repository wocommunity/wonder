//
// MarkReadFly.java: Class file for WO Component 'MarkReadFly'
// Project BugTracker
//
// Created by ak on Wed May 08 2002
//

package er.bugtracker;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

public class MarkReadFly extends WOComponent {

    public MarkReadFly(WOContext context) {
        super(context);
    }
    
    public void setObject(EOEnterpriseObject bug) {
        ((Bug)bug).markReadBy((People)((Session)session()).getUser());
    }
}
