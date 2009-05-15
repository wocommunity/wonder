package er.directtorest.security;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.directtorest.ERDirectToRest;
import er.extensions.foundation.ERXValueUtilities;
import er.rest.entityDelegates.ERXRestContext;
import er.rest.entityDelegates.IERXRestSecurityDelegate;

public class ERD2RestDefaultSecurityDelegate implements IERXRestSecurityDelegate {

    protected boolean booleanValueForKey(String key, String propertyKey, EOEnterpriseObject eo) {
        D2WContext context = ERDirectToRest.d2wContext();
        context.setPropertyKey(propertyKey);
        context.takeValueForKey(eo, "object");
        return ERXValueUtilities.booleanValue(context.valueForKey(key));
    }
    
    public boolean canDeleteObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
        return booleanValueForKey("restCanDelete", null, eo);
    }

    public boolean canInsertObject(EOEntity entity, ERXRestContext context) {
        return booleanValueForKey("restCanInsert", null, null);
    }

    public boolean canInsertObject(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, ERXRestContext context) {
        // AK CHECKME : does this make sense?
        return booleanValueForKey("restCanInsert", parentKey, null) && booleanValueForKey("restCanInsert", null, null);
    }

    public boolean canInsertProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
        return booleanValueForKey("restCanInsert", propertyName, eo);
    }

    public boolean canUpdateObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
        return booleanValueForKey("restCanUpdate", null, eo);
    }

    public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
        return booleanValueForKey("restCanUpdate", propertyName, eo);
    }

    public boolean canViewObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
        return booleanValueForKey("restCanView", null, eo);
    }

    public boolean canViewProperty(EOEntity entity, Object obj, String propertyName, ERXRestContext context) {
        return booleanValueForKey("restCanView", propertyName, null);
    }

}
