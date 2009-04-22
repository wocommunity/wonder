package er.directtorest.security;

import com.webobjects.eocontrol.EOEnterpriseObject;

import er.rest.entityDelegates.ERXRestContext;
import er.rest.entityDelegates.IERXRestSecurityDelegate;
import er.rest.routes.model.IERXEntity;

public class ERD2RestAllowSecurityDelegate implements IERXRestSecurityDelegate {

    public boolean canDeleteObject(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
        return true;
    }

    public boolean canInsertObject(IERXEntity entity, ERXRestContext context) {
        return true;
    }

    public boolean canInsertObject(IERXEntity parentEntity, Object parentObject, String parentKey, IERXEntity entity, ERXRestContext context) {
        return true;
    }

    public boolean canInsertProperty(IERXEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
        return true;
    }

    public boolean canUpdateObject(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
        return true;
    }

    public boolean canUpdateProperty(IERXEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
        return true;
    }

    public boolean canViewObject(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
        return true;
    }

    public boolean canViewProperty(IERXEntity entity, Object obj, String propertyName, ERXRestContext context) {
        return true;
    }

}
