//
// ERD2WEditRelationshipPage.java
// Project ERDirectToWeb
//
// Created by bposokho on Wed Jul 24 2002
//

package er.directtoweb;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WEditRelationshipPage;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.Services;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EODetailDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.ERXEC;

public class ERD2WEditRelationshipPage extends D2WEditRelationshipPage {

    private String _relationshipKey;

    /**
     * Public constructor
     * @param c current context
     */
    public ERD2WEditRelationshipPage(WOContext c) {
        super(c);
    }

    /**
     * Overridden because the action bound should not return null.
     */
    public WOComponent queryAction() {
        WOComponent nextPage = super.queryAction();
        if(nextPage == null) {
            nextPage = context().page();
        }
        return nextPage;
    }
    
    /**
     * Overridden because the action bound should not return null.
     */
    public WOComponent selectAction() {
        WOComponent nextPage = super.selectAction();
        if(nextPage == null) {
            nextPage = context().page();
        }
        return nextPage;
    }
    
    public WOComponent editObjectInRelationship(){
        WOComponent result = null;
        if(browserSelections != null && browserSelections.count() == 1) {
            String editConfigurationName = (String)d2wContext().valueForKey("editConfigurationName");
            EditPageInterface epi;
            if(editConfigurationName != null && editConfigurationName.length() > 0) {
                epi = (EditPageInterface)D2W.factory().pageForConfigurationNamed(editConfigurationName,session());
            } else {
                epi = D2W.factory().editPageForEntityNamed(object().entityName(),session());
            }            
            EOEnterpriseObject eo = (EOEnterpriseObject)browserSelections.objectAtIndex(0);
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
