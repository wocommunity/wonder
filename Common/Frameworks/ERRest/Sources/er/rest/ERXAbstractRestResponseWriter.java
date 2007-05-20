package er.rest;

import java.text.ParseException;
import java.util.Enumeration;

import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.ERXLocalizer;

public abstract class ERXAbstractRestResponseWriter implements IERXRestResponseWriter {
	public void appendToResponse(ERXRestContext context, WOResponse response, ERXRestKey result) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		appendToResponse(context, response, result.trimPrevious(), 0, new NSMutableSet());
	}

	protected void appendArrayToResponse(ERXRestContext context, WOResponse response, ERXRestKey result, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		String arrayName;
		EOEntity entity = result.nextEntity();
		IERXRestEntityDelegate entityDelegate = context.delegate().entityDelegate(entity);
		String entityAlias = entityDelegate.entityAliasForEntityNamed(entity.name());
		if (result.key() == null) {
			arrayName = entityAlias;
		}
		else {
			arrayName = result.key();
		}
		NSArray values = (NSArray) result.value();
		if (arrayName.equals(entityAlias)) {
			arrayName = ERXLocalizer.currentLocalizer().plurifiedString(arrayName, 2);
		}
		entityDelegate.preprocess(entity, values, context);

		NSMutableArray valueKeys = new NSMutableArray();
		Enumeration valuesEnum = values.objectEnumerator();
		while (valuesEnum.hasMoreElements()) {
			EOEnterpriseObject eo = (EOEnterpriseObject) valuesEnum.nextElement();
			ERXRestKey eoKey = result.extend(ERXRestUtils.idForEO(eo), eo, true);
			valueKeys.addObject(eoKey);
		}
		
		appendArrayToResponse(context, response, result, arrayName, entityAlias, valueKeys, indent, visitedObjects);
	}
	
	protected void appendToResponse(ERXRestContext context, WOResponse response, ERXRestKey result, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		Object value = result.value();
		if (value == null) {
			// DO NOTHING
		}
		else if (value instanceof NSArray) {
			appendArrayToResponse(context, response, result, indent, visitedObjects);
		}
		else {
			EOEntity entity = result.nextEntity();
			IERXRestEntityDelegate entityDelegate = context.delegate().entityDelegate(entity);
			String entityAlias = entityDelegate.entityAliasForEntityNamed(entity.name());
			
			String objectName;
			if (result.previousKey() == null || result.isKeyGID()) {
				objectName = entityAlias;
			}
			else {
				objectName = result.key();
			}

			EOEnterpriseObject eo = (EOEnterpriseObject) value;
			String id = ERXRestUtils.idForEO(eo);

			boolean alreadyVisited = visitedObjects.containsObject(eo);
			if (alreadyVisited) {
				appendVisitedToResponse(context, response, entity, eo, objectName, entityAlias, id, indent);
			}	
			else {
				visitedObjects.addObject(eo);
				boolean displayDetails = displayDetails(context, result);
				if (!displayDetails) {
					appendNoDetailsToResponse(context, response, entity, eo, objectName, entityAlias, id, indent);
				}
				else {
					NSMutableArray displayKeys = new NSMutableArray();
					String[] displayPropertyNames = displayProperties(context, result);
					if (displayPropertyNames != null && displayPropertyNames.length > 0) {
						for (int displayPropertyNum = 0; displayPropertyNum < displayPropertyNames.length; displayPropertyNum++) {
							String propertyName = displayPropertyNames[displayPropertyNum];
							if (context.delegate().entityDelegate(entity).canViewProperty(entity, eo, propertyName, context)) {
								ERXRestKey nextKey = result.extend(propertyName, true);
								displayKeys.addObject(nextKey);
								// EORelationship relationship = entity.relationshipNamed(propertyName);
								// if (relationship != null && !relationship.isToMany()) {
								//								
								// }
							}
						}
					}
					if (displayKeys.count() == 0) {
						appendNoDetailsToResponse(context, response, entity, eo, objectName, entityAlias, id, indent);
					}
					else {
						appendDetailsToResponse(context, response, entity, eo, objectName, entityAlias, id, displayKeys, indent, visitedObjects);
					}
				}
			}
		}
	}
	
	protected abstract void appendArrayToResponse(ERXRestContext context, WOResponse response, ERXRestKey result, String arrayName, String entityName, NSArray valueKeys, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException;

	protected abstract boolean displayDetails(ERXRestContext context, ERXRestKey result) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException;

	protected abstract String[] displayProperties(ERXRestContext context, ERXRestKey result) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException;
	
	protected abstract void appendVisitedToResponse(ERXRestContext context, WOResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, String id, int indent);
	
	protected abstract void appendNoDetailsToResponse(ERXRestContext context, WOResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, String id, int indent);

	protected abstract void appendDetailsToResponse(ERXRestContext context, WOResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, String id, NSArray displayKeys, int indent, NSMutableSet visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException;
}