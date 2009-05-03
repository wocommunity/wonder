package er.directtorest;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;

import er.directtorest.security.ERD2RestAllowSecurityDelegate;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXFetchSpecification;
import er.extensions.foundation.ERXValueUtilities;
import er.rest.ERXRestException;
import er.rest.entityDelegates.ERXAbstractRestEntityDelegate;
import er.rest.entityDelegates.ERXRestContext;
import er.rest.entityDelegates.ERXRestKey;
import er.rest.entityDelegates.ERXRestNotFoundException;
import er.rest.entityDelegates.ERXRestSecurityException;
import er.rest.entityDelegates.IERXRestSecurityDelegate;

public class ERD2RestDefaultEntityDelegate extends ERXAbstractRestEntityDelegate {
    
    private IERXRestSecurityDelegate _securityHandler = new ERD2RestAllowSecurityDelegate();

    @Override
    public void inserted(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
        // nothing
    }

    @Override
    public void updated(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
        // nothing
    }

    @Override
    public String[] displayProperties(ERXRestKey key, boolean allProperties, boolean allToMany, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
      @SuppressWarnings("unchecked") NSArray<String> props = (NSArray<String>) d2wContext().valueForKey("displayPropertyKeys");
        if(props != null) {
            return props.toArray(new String[0]);
        }
        return super.displayProperties(key, allProperties, allToMany, context);
    }


    @Override
    public boolean displayDetails(ERXRestKey key, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
        EOEntity entity = d2wContext().entity();
        return key.previousKey() == null;
    }

    
    /**
     * Returns entityName;
     * 
     * @return entityName
     */
    @Override
    public String entityAliasForEntityNamed(String entityName) {
        EOEntity old = d2wContext().entity();
        d2wContext().setEntity(ERXEOAccessUtilities.entityNamed(null, entityName));
        String result = (String) d2wContext().valueForKey("restEntityAliasName");
        d2wContext().setEntity(old);
        if(result == null) {
            result = entityName;
        }
        return result;
    }

    /**
     * Returns propertyAlias.
     * 
     * @return propertyAlias
     */
    @Override
    public String propertyNameForPropertyAlias(EOEntity entity, String propertyAlias) {
//        if(entity.classPropertyNames().containsObject(propertyAlias)) {
//            return propertyAlias;
//        }
        return propertyAlias;
    }

    /**
     * Returns propertyName.
     * 
     * @return propertyName
     */
    @Override
    public String propertyAliasForPropertyNamed(EOEntity entity, String propertyName) {
        return propertyName;
    }

    public EOEntity nextEntity(EOEntity entity, String key) {
        return null;
    }
    
    protected int intValue(String key, int defaultValue) {
        return ERXValueUtilities.intValueWithDefault(d2wContext().valueForKeyPath(key), defaultValue);
    }

    private D2WContext d2wContext() {
        return ERDirectToRest.d2wContext();
    }

    public NSArray objectsForEntity(EOEntity entity, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
        EOFetchSpecification fs = new ERXFetchSpecification<EOEnterpriseObject>(entity.name(), null, null);
        fs.setFetchLimit(intValue("fetchLimit", 0));
        NSArray objects = context.editingContext().objectsWithFetchSpecification(fs);
        return objects;
    }

    public NSArray visibleObjects(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, NSArray objects, ERXRestContext context) throws ERXRestException,
            ERXRestSecurityException, ERXRestNotFoundException {
        return objects;
    }
    
    public IERXRestSecurityDelegate securityHandler() {
        return _securityHandler;
    }

    public boolean canDeleteObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
        return securityHandler().canDeleteObject(entity, eo, context);
    }

    public final boolean canInsertObject(EOEntity entity, ERXRestContext context) {
        return securityHandler().canInsertObject(entity, context);
    }

    public final boolean canInsertObject(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, ERXRestContext context) {
        return securityHandler().canInsertObject(parentEntity, parentObject, parentKey, entity, context);
    }

    public final boolean canInsertProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
        return securityHandler().canInsertProperty(entity, eo, propertyName, context);
    }

    public final boolean canUpdateObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
        return securityHandler().canUpdateObject(entity, eo, context);
    }

    public final boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
        return securityHandler().canUpdateProperty(entity, eo, propertyName, context);
    }

    public boolean canViewObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
        return securityHandler().canViewObject(entity, eo, context);
    }

    public boolean canViewProperty(EOEntity entity, Object obj, String propertyName, ERXRestContext context) {
        return securityHandler().canViewProperty(entity, obj, propertyName, context);
    }

}
