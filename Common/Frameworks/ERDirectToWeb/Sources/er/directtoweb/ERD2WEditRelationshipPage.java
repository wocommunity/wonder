//
// ERD2WEditRelationshipPage.java
// Project ERDirectToWeb
//
// Created by bposokho on Wed Jul 24 2002
//

package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;

public class ERD2WEditRelationshipPage extends D2WEditRelationshipPage {

    /**
    * Public constructor
     * @param c current context
     */
    public ERD2WEditRelationshipPage(WOContext c) {
        super(c);
    }

    public WOComponent editObjectInRelationship(){
        WOComponent result = null;
        System.out.println("browserSelections = "+browserSelections);
        if(browserSelections != null && browserSelections.count() == 1)
        {
            EOEnterpriseObject eo = (EOEnterpriseObject)browserSelections.objectAtIndex(0);
            EditPageInterface epi = D2W.factory().editPageForEntityNamed(eo.entityName(), session());
            epi.setObject(eo);
            epi.setNextPage(context().page());
            result = (WOComponent)epi;
        }
        return result;
    }
    
}
