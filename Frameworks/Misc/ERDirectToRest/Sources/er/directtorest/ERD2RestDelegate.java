package er.directtorest;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOEntity;

import er.extensions.foundation.ERXThreadStorage;
import er.rest.ERXRestException;
import er.rest.entityDelegates.ERXDefaultRestDelegate;
import er.rest.entityDelegates.ERXDenyRestEntityDelegate;
import er.rest.entityDelegates.ERXRestContext;
import er.rest.entityDelegates.ERXRestKey;
import er.rest.entityDelegates.ERXRestNotFoundException;
import er.rest.entityDelegates.ERXRestRequest;
import er.rest.entityDelegates.ERXRestSecurityException;
import er.rest.entityDelegates.IERXRestEntityDelegate;

public class ERD2RestDelegate extends ERXDefaultRestDelegate {
    
    private IERXRestEntityDelegate _defaultDelegate;
    
    /**
     * Constructs an ERXDefaultRestDelegate with an ERXDenyRestEntityDelegate as the default entity delegate.
     */
    public ERD2RestDelegate() {
        super(new ERXDenyRestEntityDelegate(), true);
        _defaultDelegate = new ERXDenyRestEntityDelegate();
    }
    
    @Override
    public String entityNameForAlias(String entityAlias) {
        d2wContext().takeValueForKey(entityAlias, "restEntityAlias"); 
        String entityName = (String) d2wContext().valueForKeyPath("restEntityName");
        if (entityName == null) {
            entityName = entityAlias;
        }
        return entityName;
    }

    public IERXRestEntityDelegate entityDelegate(EOEntity entity) {
        String entityName = entity.name();
        IERXRestEntityDelegate entityDelegate = (IERXRestEntityDelegate) ERXThreadStorage.valueForKey("restEntityDelegate." + entityName);
        if (entityDelegate == null) {
            entityDelegate = (IERXRestEntityDelegate) d2wContext().valueForKey("restEntityDelegate");
            if (entityDelegate == null) {
                entityDelegate = _defaultDelegate;
            }
            ERXThreadStorage.takeValueForKey(entityDelegate, "restEntityDelegate." + entityName);
        }
        d2wContext().setEntity(entity);
        entityDelegate.initializeEntityNamed(entityName);
        return entityDelegate;
    }

    /**
     * Call this method to register an entity-specific delegate for a particular entity name.
     * 
     * @param entityDelegate
     *            the entity delegate
     * @param entityName
     *            the entity name to associate the delegate with
     */
    @Override
    public void addDelegateForEntityNamed(IERXRestEntityDelegate entityDelegate, String entityName) {
        throw new IllegalStateException("Can't addDelegateForEntityNamed, is handled by d2wContext.");
    }

    /**
     * Removes the delegate for the given entity name.
     * 
     * @param entityName
     *            the name of the entity
     */
    @Override
    public void removeDelegateForEntityNamed(String entityName) {
        throw new IllegalStateException("Can't removeDelegateForEntityNamed, is handled by d2wContext.");
    }
    

    protected void updateDynamicPage(String task, ERXRestRequest restRequest, ERXRestContext restContext) {
        d2wContext().setDynamicPage("Rest" + task + restRequest.key().entity().name());
    }

    private D2WContext d2wContext() {
        return ERDirectToRest.d2wContext();
    }

    @Override
    public ERXRestKey view(ERXRestRequest restRequest, ERXRestContext restContext) {
        updateDynamicPage("View", restRequest, restContext);
        return super.view(restRequest, restContext);
    }

    @Override
    public ERXRestKey insert(ERXRestRequest restRequest, ERXRestContext restContext) {
        updateDynamicPage("Insert", restRequest, restContext);
        return super.view(restRequest, restContext);
    }

    @Override
    public void update(ERXRestRequest restRequest, ERXRestContext restContext) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
        updateDynamicPage("Update", restRequest, restContext);
        super.update(restRequest, restContext);
    }

    @Override
    public void delete(ERXRestRequest restRequest, ERXRestContext restContext) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
        updateDynamicPage("Delete", restRequest, restContext);
        super.delete(restRequest, restContext);
    }
}
