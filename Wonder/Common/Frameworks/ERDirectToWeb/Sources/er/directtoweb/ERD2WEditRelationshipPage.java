//
// ERD2WEditRelationshipPage.java
// Project ERDirectToWeb
//
// Created by bposokho on Wed Jul 24 2002
//

package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;

import er.extensions.*;

public class ERD2WEditRelationshipPage extends D2WEditRelationshipPage {

    private String _relationshipKey;

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
    
    public void setMasterObjectAndRelationshipKey(EOEnterpriseObject eo, String relationshipKey) {
        EOEditingContext ec = ERXEC.newEditingContext(eo.editingContext(), false); // no validation;
        setEditingContext(ec);
        EOEnterpriseObject localEO = EOUtilities.localInstanceOfObject(ec, eo);
        setObject(localEO);
        _relationshipKey = relationshipKey; 
        if (object().isToManyKey(relationshipKey))
            isRelationshipToMany = true;
        else
            relationshipDisplayGroup.setSelectsFirstObjectAfterFetch(true);
        EODetailDataSource ds = new EODetailDataSource(object().classDescription(), _relationshipKey);
        ds.qualifyWithRelationshipKey(_relationshipKey, localEO);
        setDataSource(ds);
        relationshipDisplayGroup.setDataSource(ds);
        relationshipDisplayGroup.fetch();
        setPropertyKey(displayKey());
    }

    public String displayNameForRelationshipKey() {
        return Services.capitalize(_relationshipKey);
    }

    public WOComponent removeFromToOneRelationshipAction() {
        dataSource().deleteObject((EOEnterpriseObject) object().valueForKeyPath(_relationshipKey));
        relationshipDisplayGroup.fetch();
        return null;
    }
    
}
