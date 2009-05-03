package er.directtorest.security;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.rest.entityDelegates.ERXRestContext;
import er.rest.entityDelegates.IERXRestSecurityDelegate;

public class ERD2RestAllowSecurityDelegate implements IERXRestSecurityDelegate {

    public boolean canDeleteObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
        return true;
    }

    public boolean canInsertObject(EOEntity entity, ERXRestContext context) {
        return true;
    }

    public boolean canInsertObject(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, ERXRestContext context) {
        return true;
    }

    public boolean canInsertProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
        return true;
    }

    public boolean canUpdateObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
        return true;
    }

    public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
        return true;
    }

    public boolean canViewObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
        return true;
    }

    public boolean canViewProperty(EOEntity entity, Object obj, String propertyName, ERXRestContext context) {
        return true;
    }

}
