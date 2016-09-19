package er.rest.routes;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.eof.ERXKeyFilter;
import er.extensions.foundation.ERXStringUtilities;
import er.rest.ERXRestClassDescriptionFactory;
import er.rest.ERXRestFetchSpecification;
import er.rest.IERXRestDelegate;

/**
 * If you just want to quickly drop in a controller to test your entities, you can use or extend
 * ERXUnsafeReadOnlyRouteController. This provides a default read-only interface to the attributes and to-one
 * relationships of the specified entity.
 * 
 * <pre>
 * routeRequestHandler.addDefaultRoutes(YourEntity.ENTITY_NAME, ERXUnsafeReadOnlyRouteController.class);
 * </pre>
 *
 * @author mschrag
 * 
 * @param <T>
 *            the type of your entity
 */
public class ERXUnsafeReadOnlyRouteController<T extends EOEnterpriseObject> extends ERXDefaultRouteController {
	public ERXUnsafeReadOnlyRouteController(WORequest request) {
		super(request);
	}

	protected boolean allowUpdates() {
		return false;
	}

	@SuppressWarnings("unchecked")
	protected T object() {
		T obj = (T) routeObjectForKey(ERXStringUtilities.uncapitalize(entityName()));
		return obj;
	}

	protected ERXKeyFilter queryFilter() {
		ERXKeyFilter filter = ERXKeyFilter.filterWithAllRecursive();
		return filter;
	}

	protected ERXKeyFilter showFilter() {
		ERXKeyFilter filter = ERXKeyFilter.filterWithAttributesAndToOneRelationships();
		return filter;
	}

	protected ERXKeyFilter updateFilter() {
		ERXKeyFilter filter = ERXKeyFilter.filterWithAttributesAndToOneRelationships();
		return filter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public WOActionResults createAction() {
		T obj = (T) create(entityName(), updateFilter());
		editingContext().saveChanges();
		return response(obj, showFilter());
	}

	@Override
	public WOActionResults updateAction() {
		if (!allowUpdates()) {
			throw new SecurityException("You are not allowed to update this type of object.");
		}
		T obj = object();
		update(obj, updateFilter());
		editingContext().saveChanges();
		return response(obj, showFilter());
	}

	@Override
	public WOActionResults destroyAction() throws Throwable {
		if (!allowUpdates()) {
			throw new SecurityException("You are not allowed to delete this type of object.");
		}
		T obj = object();
        EOClassDescription classDescription = ERXRestClassDescriptionFactory.classDescriptionForObject(obj, false);
		Object primaryKey = IERXRestDelegate.Factory.delegateForClassDescription(classDescription).primaryKeyForObject(obj, restContext());
		editingContext().deleteObject(obj);
		editingContext().saveChanges();
		return response(primaryKey, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public WOActionResults newAction() throws Throwable {
		if (!allowUpdates()) {
			throw new SecurityException("You are not allowed to create this type of object.");
		}
		EOClassDescription classDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(entityName());
		T obj = (T) IERXRestDelegate.Factory.delegateForClassDescription(classDescription).createObjectOfEntityWithID(classDescription, null, restContext());
		return response(obj, showFilter());
	}

	@Override
	public WOActionResults showAction() {
		return response(object(), showFilter());
	}

	@Override
	public WOActionResults indexAction() {
		if (isSchemaRequest()) {
			return schemaResponse(showFilter());
		}
		
		ERXRestFetchSpecification<T> fetchSpec = new ERXRestFetchSpecification<>(entityName(), null, null, queryFilter(), null, 25);
		return response(fetchSpec, showFilter());
	}
}
