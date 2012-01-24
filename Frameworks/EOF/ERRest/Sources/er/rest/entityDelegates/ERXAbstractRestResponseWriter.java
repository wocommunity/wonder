package er.rest.entityDelegates;

import java.text.ParseException;
import java.util.Enumeration;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXKeyFilter;
import er.extensions.localization.ERXLocalizer;
import er.rest.ERXRestException;
import er.rest.format.IERXRestResponse;

/**
 * <p>
 * ERXAbstractRestResponseWriter provides the output-method-agnostic methods for processing a rest response and provides
 * support for specifying rendering configuration in your application properties.
 * </p>
 * 
 * <p>
 * There are multiple levels of rendering controls that you can adjust. These adjustments come in two primary forms --
 * details and details properties.
 * </p>
 * 
 * <p>
 * The details setting allows you to specify that for a given keypath, whether or not a particular relationship should
 * display only the id of the related object or the id as well as its properties.
 * </p>
 * 
 * <p>
 * The details properties setting defines the "upper bound" of properties to display to a user for a particular keypath.
 * That is to say that for a particular key path, the user will never be shown any key that is outside of the specified
 * list. However, permissions on a particular entity may restrict access such that the user is not able to see all the
 * keys specified. This control allows you to specify at a rendering level what the user can and cannot see on an
 * object.
 * </p>
 * 
 * <p>
 * These properties take the form:
 * 
 * <pre>
 * ERXRest.[EntityName].details=true/false
 * ERXRest.[EntityName].detailsProperties=property_1,property_2,property_3,...,property_n
 * ERXRest.[EntityName].property_a.property_a_b.details=true/false
 * ERXRest.[EntityName].property_a.property_a_b.detailsProperties=property_1,property_2,property_3,...,property_n
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * For example:
 * 
 * <pre>
 * ERXRest.Organization.details=true
 * ERXRest.Organization.detailsProperties=name,purchasedPlans
 * ERXRest.Organization.purchasedPlans.details=false
 * 
 * ERXRest.Site.details=true
 * ERXRest.Site.detailsProperties=title,organization,disabledAt,memberships,sheetSets,blogEntries
 * ERXRest.Site.blogEntries.details=true
 * ERXRest.Site.blogEntries.detailsProperties=author,submissionDate,title,contents
 * ERXRest.Site.sheetSets.details=false
 * ERXRest.Site.memberships.details=false
 * 
 * ERXRest.BlogEntry.details=true
 * ERXRest.BlogEntry.detailsProperties=site,author,submissionDate,title,contents
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * Note that all properties that appear in a details properties definition should be "actual" property names, not
 * property aliases. Similarly, all entity references should be the actual entity name, not an entity alias.
 * </p>
 * 
 * <p>
 * In the example above, if someone requests an Organization as the top level entity, the details will be displayed. The
 * properties that will be displayed in those details includes "name" and "purchasedPlans", which is a to-many
 * relationship. In the example, we have explicitly declared that Organization.purchasedPlans will not show any details,
 * though this was technically unnecessary because the default is "false".
 * </p>
 * 
 * <p>
 * For a request for http://yoursite/yourapp.woa/rest/Organization/100.xml, the output will look like:
 * 
 * <pre>
 * &lt;Organization id = &quot;100&quot;&gt;
 *   &lt;name&gt;Organization Name&lt;/name&gt;
 *   &lt;purchasedPlans type = &quot;PurchasedPlan&quot;&gt;
 *     &lt;PurchasedPlan id = &quot;200&quot;/&gt;
 *     &lt;PurchasedPlan id = &quot;201&quot;/&gt;
 *     &lt;PurchasedPlan id = &quot;202&quot;/&gt;
 *   &lt;/purchasedPlans&gt;
 * &lt;/Organization&gt;
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * In the second and third blocks of the example, you can see two different specifications for properties to display for
 * a BlogEntry. If you request Site/100/blogEntries.xml, you will see a set of properties that does not include the site
 * relationship of the blog entry (notice that ERXRest.Site.blogEntries.detailsProperties does not specify "site").
 * However, if you request BlogEntry/301.xml you will see the site relationship (notice that
 * ERXRest.BlogEntry.detailsProperties DOES contain "site"). Also note that primary keys should not be used in the
 * definition of renderer configurations. Configuration is assumed to apply to any instance of an object that
 * structurally matches the keypaths you define.
 * </p>
 * 
 * <p>
 * The renderer looks for the longest matching keypath specification in the Properties file to determine whether or not
 * to display properties and which properties to display, so you can construct arbitrarily deep specifications for which
 * properties to display for any given keypath.
 * </p>
 * 
 * @author mschrag
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
public abstract class ERXAbstractRestResponseWriter implements IERXRestResponseWriter {
	private ERXKeyFilter _filter;
	private boolean _displayAllProperties;
	private boolean _displayAllToMany;

	/**
	 * Constructs an ERXAbstractRestResponseWriter with displayAllProperties = false.
	 */
	public ERXAbstractRestResponseWriter() {
		this(false, false);
	}

	/**
	 * Constructs an ERXAbstractRestResponseWriter.
	 * 
	 * @param displayAllProperties
	 *            if true, by default all properties are eligible to be displayed (probably should only be true in
	 *            development, but it won't really hurt anything). Note that entity delegates will still control
	 *            permissions on the properties, it just defaults to checking all of them.
	 * @param displayAllToMany
	 *            if true, all to-many relationships will be displayed
	 */
	public ERXAbstractRestResponseWriter(boolean displayAllProperties, boolean displayAllToMany) {
		_displayAllProperties = displayAllProperties;
		_displayAllToMany = displayAllToMany;
	}

	/**
	 * Constructs an ERXAbstractRestResponseWriter.
	 * 
	 * @param filter
	 *            the filter to apply to the written results
	 */
	public ERXAbstractRestResponseWriter(ERXKeyFilter filter) {
		_filter = filter;
	}

	/**
	 * Returns whether or not the details (i.e. the keys of an EO) should displayed for the given key.
	 * 
	 * @param context
	 *            the rest context
	 * @param key
	 *            the current key
	 * @return whether or not the details (i.e. the keys of an EO) should displayed for the given key
	 * @throws ERXRestException
	 *             if a general error occurs
	 * @throws ERXRestSecurityException
	 *             if a security error occurs
	 * @throws ERXRestNotFoundException
	 *             if an object is not found
	 */
	protected boolean displayDetails(ERXRestContext context, ERXRestKey key) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		boolean displayDetails;
		if (_filter == null) {
			String entityName = key.entity().name();
			EOEntity entity = ERXRestEntityDelegateUtils.requiredEntityNamed(context, entityName);
			displayDetails = context.delegate().entityDelegate(entity).displayDetails(key, context);
		}
		else {
			ERXKeyFilter filter = _filter;
			for (ERXRestKey pathKey = key.firstKey(); pathKey != null && pathKey != key; pathKey = pathKey.nextKey()) {
				if (!pathKey.isKeyGID()) {
					filter = filter._filterForKey(new ERXKey(pathKey.key()));
				}
			}
			displayDetails = (filter.base() != ERXKeyFilter.Base.None || filter.includes().size() > 0);
		}
		return displayDetails;
	}

	/**
	 * Returns the set of properties that can be displayed for the given key.
	 * 
	 * @param context
	 *            the rest context
	 * @param key
	 *            the current key
	 * @return the set of properties that can be displayed for the given key
	 * @throws ERXRestException
	 *             if a general error occurs
	 * @throws ERXRestSecurityException
	 *             if a security error occurs
	 * @throws ERXRestNotFoundException
	 *             if an object is not found
	 */
	protected String[] displayProperties(ERXRestContext context, ERXRestKey key) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		String[] displayProperties;
		if (_filter == null) {
			String entityName = key.entity().name();
			EOEntity entity = ERXRestEntityDelegateUtils.requiredEntityNamed(context, entityName);
			displayProperties = context.delegate().entityDelegate(entity).displayProperties(key, _displayAllProperties, _displayAllToMany, context);
		}
		else {
			ERXKeyFilter filter = _filter;
			for (ERXRestKey pathKey = key.firstKey(); pathKey != null && pathKey != key; pathKey = pathKey.nextKey()) {
				if (!pathKey.isKeyGID()) {
					filter = filter._filterForKey(new ERXKey(pathKey.key()));
				}
			}

			NSMutableSet<String> displayPropertySet = new NSMutableSet<String>();
			EOEntity entity = key.nextEntity();
			Enumeration attributesEnum = entity.attributes().objectEnumerator();
			while (attributesEnum.hasMoreElements()) {
				EOAttribute attribute = (EOAttribute) attributesEnum.nextElement();
				if (entity.classProperties().containsObject(attribute) && filter.matches(new ERXKey(attribute.name()), ERXKey.Type.Attribute)) {
					displayPropertySet.addObject(attribute.name());
				}
			}

			Enumeration relationshipsEnum = entity.relationships().objectEnumerator();
			while (relationshipsEnum.hasMoreElements()) {
				EORelationship relationship = (EORelationship) relationshipsEnum.nextElement();
				if (entity.classProperties().containsObject(relationship) && filter.matches(new ERXKey(relationship.name()), relationship.isToMany() ? ERXKey.Type.ToManyRelationship : ERXKey.Type.ToOneRelationship)) {
					displayPropertySet.addObject(relationship.name());
				}
			}

			for (ERXKey includeKey : filter.includes().keySet()) {
				displayPropertySet.addObject(includeKey.key());
			}

			displayProperties = displayPropertySet.toArray(new String[displayPropertySet.count()]);
		}
		return displayProperties;
	}

	public void appendToResponse(ERXRestContext context, IERXRestResponse response, ERXRestKey result) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		appendToResponse(context, response, result.trimPrevious(), 0, new NSMutableSet<Object>());
	}
	
	
	protected void appendDictionaryToResponse(ERXRestContext context, IERXRestResponse response, ERXRestKey result, int indent, NSMutableSet<Object> visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		EOEntity entity = result.nextEntity();
		IERXRestEntityDelegate entityDelegate = context.delegate().entityDelegate(entity);
		if (!(entityDelegate instanceof ERXDenyRestEntityDelegate)) {
			String arrayName;
			String entityAlias = entityDelegate.entityAliasForEntityNamed(entity.name());
			if (result.keyAlias() == null) {
				arrayName = entityAlias;
			}
			else {
				arrayName = result.keyAlias();
			}
			if (arrayName.equals(entityAlias)) {
				arrayName = ERXLocalizer.currentLocalizer().plurifiedString(arrayName, 2);
			}
			NSDictionary values = (NSDictionary) result.value();

			NSMutableArray<ERXRestKey> valueKeys = new NSMutableArray<ERXRestKey>();
			Enumeration valuesEnum = values.objectEnumerator();
			while (valuesEnum.hasMoreElements()) {
				EOEnterpriseObject eo = (EOEnterpriseObject) valuesEnum.nextElement();
				ERXRestKey eoKey = result.extend(entityDelegate.stringIDForEO(entity, eo), eo);
				valueKeys.addObject(eoKey);
			}

			appendArrayToResponse(context, response, result, arrayName, entityAlias, valueKeys, indent, visitedObjects);
		}
	}
	
	protected void appendArrayToResponse(ERXRestContext context, IERXRestResponse response, ERXRestKey result, int indent, NSMutableSet<Object> visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		EOEntity entity = result.nextEntity();
		IERXRestEntityDelegate entityDelegate = context.delegate().entityDelegate(entity);
		if (!(entityDelegate instanceof ERXDenyRestEntityDelegate)) {
			String arrayName;
			String entityAlias = entityDelegate.entityAliasForEntityNamed(entity.name());
			if (result.keyAlias() == null) {
				arrayName = entityAlias;
			}
			else {
				arrayName = result.keyAlias();
			}
			NSArray values = (NSArray) result.value();
			if (arrayName.equals(entityAlias)) {
				arrayName = ERXLocalizer.currentLocalizer().plurifiedString(arrayName, 2);
			}
			entityDelegate.preprocess(entity, values, context);

			NSMutableArray<ERXRestKey> valueKeys = new NSMutableArray<ERXRestKey>();
			Enumeration valuesEnum = values.objectEnumerator();
			while (valuesEnum.hasMoreElements()) {
				Object value = valuesEnum.nextElement();
				if (value instanceof EOEnterpriseObject) {
					EOEnterpriseObject eo = (EOEnterpriseObject) value;
					ERXRestKey eoKey = result.extend(entityDelegate.stringIDForEO(entity, eo), eo);
					valueKeys.addObject(eoKey);
				} else {
					ERXRestKey eoKey = result.extend(value.getClass().getSimpleName(), value);
					valueKeys.addObject(eoKey);
				}
			}

			appendArrayToResponse(context, response, result, arrayName, entityAlias, valueKeys, indent, visitedObjects);
		}
	}

	protected void appendToResponse(ERXRestContext context, IERXRestResponse response, ERXRestKey result, int indent, NSMutableSet<Object> visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		Object value = result.value();
		if (value == null) {
			// DO NOTHING
		}
		else if (value instanceof NSArray) {
			appendArrayToResponse(context, response, result, indent, visitedObjects);
		}
		else if (value instanceof EOEnterpriseObject) {
			String entityName = ((EOEnterpriseObject) value).entityName();
			EOEntity entity = ERXRestEntityDelegateUtils.requiredEntityNamed(context, entityName);
			// EOEntity entity = result.nextEntity();
			IERXRestEntityDelegate entityDelegate = context.delegate().entityDelegate(entity);
			String entityAlias = entityDelegate.entityAliasForEntityNamed(entity.name());

			String objectName;
			if (result.previousKey() == null || result.isKeyGID()) {
				objectName = entityAlias;
			}
			else {
				objectName = result.keyAlias();
			}

			EOEnterpriseObject eo = (EOEnterpriseObject) value;
			Object id = entityDelegate.idForEO(entity, eo);

			boolean alreadyVisited = visitedObjects.containsObject(eo);
			if (alreadyVisited) {
				appendVisitedToResponse(context, response, entity, eo, objectName, entityAlias, id, indent);
			}
			else {
				if (!result.isKeyGID()) {
					result = result.extend(entityDelegate.stringIDForEO(entity, eo), eo);
				}

				visitedObjects.addObject(eo);
				boolean displayDetails = displayDetails(context, result);
				if (!displayDetails) {
					appendNoDetailsToResponse(context, response, entity, eo, objectName, entityAlias, id, indent);
				}
				else {
					NSMutableArray<ERXRestKey> displayKeys = new NSMutableArray<ERXRestKey>();
					String[] displayPropertyNames = displayProperties(context, result);
					if (displayPropertyNames != null && displayPropertyNames.length > 0) {
						for (int displayPropertyNum = 0; displayPropertyNum < displayPropertyNames.length; displayPropertyNum++) {
							String propertyName = displayPropertyNames[displayPropertyNum];
							if (entityDelegate.canViewProperty(entity, eo, propertyName, context)) {
								ERXRestKey nextKey = result.extend(entityDelegate.propertyAliasForPropertyNamed(entity, propertyName));
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
				visitedObjects.removeObject(eo);
			}
		}
		else {
			appendPrimitiveToResponse(context, response, result, indent, value);
		}
	}

	protected void indent(IERXRestResponse response, int indent) {
		for (int i = 0; i < indent; i++) {
			response.appendContentString("  ");
		}
	}

	/**
	 * Writes the given array of objects to the response. Permissions have already been checked by the time this method
	 * is called.
	 * 
	 * @param context
	 *            the rest context
	 * @param response
	 *            the response
	 * @param key
	 *            the current key
	 * @param arrayName
	 *            the name of the array in the context of its parent
	 * @param entityName
	 *            the entity name of the contents of the array
	 * @param valueKeys
	 *            an array of ERXRestKeys that represent the entries in the array
	 * @param indent
	 *            the indent level
	 * @param visitedObjects
	 *            the list of objects that have been visited already in this request (to prevent infinite loops)
	 * @throws ERXRestException
	 *             if a general error occurs
	 * @throws ERXRestSecurityException
	 *             if a security error occurs
	 * @throws ERXRestNotFoundException
	 *             if an object is not found
	 * @throws ParseException
	 *             if a parse error occurs
	 */
	protected abstract void appendArrayToResponse(ERXRestContext context, IERXRestResponse response, ERXRestKey key, String arrayName, String entityName, NSArray valueKeys, int indent, NSMutableSet<Object> visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException;

	/**
	 * Write an object to the response that has already been visited. Typically this would just write out the type and
	 * id of the object, to prevent entering an infinite loop in the renderer. Permissions have already been checked by
	 * the time this method is called.
	 * 
	 * @param context
	 *            the rest context
	 * @param response
	 *            the response
	 * @param entity
	 *            the entity of the object
	 * @param eo
	 *            the current object
	 * @param objectName
	 *            the name of the object (relative to its parent)
	 * @param entityName
	 *            the entity name of the object
	 * @param id
	 *            the id of the object
	 * @param indent
	 *            the indent level
	 */
	protected abstract void appendVisitedToResponse(ERXRestContext context, IERXRestResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, Object id, int indent);

	/**
	 * Write an object to the response without showing its details. This is typically similar to
	 * appendVisitedToResponse, but is provided as a separate call because it is semantically a different scenario.
	 * Permissions have already been checked by the time this method is called.
	 * 
	 * @param context
	 *            the rest context
	 * @param response
	 *            the response
	 * @param entity
	 *            the entity of the object
	 * @param eo
	 *            the current object
	 * @param objectName
	 *            the name of the object (relative to its parent)
	 * @param entityName
	 *            the entity name of the object
	 * @param id
	 *            the id of the object
	 * @param indent
	 *            the indent level
	 */
	protected abstract void appendNoDetailsToResponse(ERXRestContext context, IERXRestResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, Object id, int indent);

	/**
	 * Writes the visible details of an object to the response. Permissions have already been checked by the time this
	 * method is called.
	 * 
	 * @param context
	 *            the rest context
	 * @param response
	 *            the response
	 * @param entity
	 *            the entity of the object
	 * @param eo
	 *            the current object
	 * @param objectName
	 *            the name of the object (relative to its parent)
	 * @param entityName
	 *            the entity name of the object
	 * @param id
	 *            the id of the object
	 * @param displayKeys
	 *            the list of ERXRestKeys to display
	 * @param indent
	 *            the indent level
	 * @param visitedObjects
	 *            the list of objects that have been visited already in this request (to prevent infinite loops)
	 * @throws ERXRestException
	 *             if a general error occurs
	 * @throws ERXRestSecurityException
	 *             if a security error occurs
	 * @throws ERXRestNotFoundException
	 *             if an object is not found
	 * @throws ParseException
	 *             if a parse error occurs
	 */
	protected abstract void appendDetailsToResponse(ERXRestContext context, IERXRestResponse response, EOEntity entity, EOEnterpriseObject eo, String objectName, String entityName, Object id, NSArray displayKeys, int indent, NSMutableSet<Object> visitedObjects) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException;

	/**
	 * Writes the bare primitive out to the response. Permissions have already been checked by the time this method is
	 * called.
	 * 
	 * @param context
	 *            the rest context
	 * @param response
	 *            the response
	 * @param result
	 *            the current key
	 * @param indent
	 *            the indent level
	 * @param value
	 *            the value to append
	 * @throws ERXRestException
	 *             if a general failure occurs
	 */
	protected abstract void appendPrimitiveToResponse(ERXRestContext context, IERXRestResponse response, ERXRestKey result, int indent, Object value) throws ERXRestException;

	/**
	 * Returns a String form of the given object using the unsafe delegate.
	 * 
	 * @param value the value to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public String toString(Object value) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestEntityDelegateUtils.toString(new ERXRestContext(new ERXUnsafeRestEntityDelegate(true)), this, value);
	}

	/**
	 * Returns a String form of the given objects using the unsafe delegate.
	 * 
	 * @param values the values to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public String toString(EOEntity entity, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestEntityDelegateUtils.toString(new ERXRestContext(new ERXUnsafeRestEntityDelegate(true)), this, entity, values);
	}

	/**
	 * Returns a String form of the given objects using the unsafe delegate.
	 * 
	 * @param values the values to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public String toString(EOEditingContext editingContext, String entityName, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestEntityDelegateUtils.toString(new ERXRestContext(new ERXUnsafeRestEntityDelegate(true)), this, ERXRestEntityDelegateUtils.requiredEntityNamed(editingContext, entityName), values);
	}
}