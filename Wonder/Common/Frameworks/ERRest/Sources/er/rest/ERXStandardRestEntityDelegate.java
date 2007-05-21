package er.rest;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.ERXProperties;

/**
 * <p>
 * ERXStandardRestEntityDelegate provides additional implementations of methods above and beyond
 * ERXAbstractRestEntityDelegate.
 * </p>
 * 
 * <p>
 * In particular it adds support for loading certain common configuration out of your application Properties file,
 * including:
 * </p>
 * <ul>
 * <li>entity aliases</li>
 * <li>property aliases</li>
 * <li>view properties</li>
 * <li>insert properties</li>
 * <li>update properties</li>
 * </ul>
 * 
 * <h2>Entity Aliases</h2>
 * <p>
 * Entity aliases allow you to remap the name of entity from its actual internal name to some other name that is shown
 * to consumers of your service:
 * 
 * <pre>
 * ERXRest.[EntityName].alias=SomeOtherName
 * </pre>
 * 
 * For example:
 * 
 * <pre>
 * ERXRest.Person.alias=Employee
 * </pre>
 * 
 * In the above example, our entity is named Person, but we want users of our service to see it named "Employee."
 * </p>
 * 
 * <h2>Property Aliases</h2>
 * <p>
 * Just like entity aliases, it is often desirable to be able to rename properties of your entity for use externally.
 * Property aliases provide a mechanism to do this, and ERXStandardRestEntityDelegate allows you to define these
 * mappings in your Application properties file.
 * 
 * <pre>
 * ERXRest.[EntityName].[propertyName].alias=someOtherName
 * </pre>
 * 
 * For example:
 * 
 * <pre>
 * ERXRest.Person.displayName.alias=name
 * </pre>
 * 
 * In the above example, the we are saying that the "displayName" property of the "Person" entity should be called
 * "name" to users of the service. The entity name in this property should be the actual entity name, not the entity
 * alias if one exists.
 * </p>
 * 
 * <h2>View/Update/Insert Properties</h2>
 * <p>
 * There are three types of properties you can control: view, update, and insert. View properties are the set of
 * properties (keys, attributes, relationships, etc) that a particular caller is allowed to view on an object. Insert
 * properties are the properties that a caller is allowed to specify at creation time for a new object. Update
 * properties are the properties that a caller is allowed to update during an update request. Insert and update
 * properties are implicitly view properties, and update properties are implicitly insert properties.
 * </p>
 * 
 * <p>
 * Note that all properties that appear in a properties definition should be "actual" property names, not property
 * aliases. Similarly, all entity names should be the actual entity names and not entity aliases.
 * </p>
 * 
 * <p>
 * If you have already ERXXmlRestResponseWriter (if not, you should), you know that it can define a set of properties
 * that CAN be displayed to your users for any particular entity or keypath (the "detailsProperties"). The properties
 * defined for the renderer only define what the user can possibly see -- it does not deal with security, which is the
 * entity delegate's responsibility. So it is possible to declare that the renderer should show
 * "firstName,lastName,email" for a Person in details, but your view properties may only allow you to see "firstName".
 * </p>
 * 
 * <p>
 * This implementation provides a simple way to define view, insert, and update properties for an entity. These
 * definitions, by default, are fixed for any use of the entity by any user. If you want to dynamically adjust what
 * keypaths users can view, insert, and update based on some more complicated permissions structure, then you can either
 * override the corresponding methods, or extend ERXAbstractRestEntityDelegate instead of this implementation.
 * </p>
 * 
 * <p>
 * Because the view properties are not dynamic by default, it is a common desire to use the detailsProperties
 * declaration from ERXXmlRestResponseWriter as the view properties also. To do this, you can use the the
 * ERXStandardRestEntityDelegate(String entityName) variant of the constructor, or you can use the
 * ERXStandardRestEntityDelegate() constructor and manually call
 * loadDetailsPropertiesAsViewPropertiesForEntityNamed(String entityName) and it will automatically make all details
 * properties view properties.
 * </p>
 * 
 * <p>
 * To define view, insert, and update properties in your Application's Properties file, you can declare:
 * 
 * <pre>
 * ERXRest.[EntityName].viewProperties=property1,property2,property3,...
 * ERXRest.[EntityName].insertProperties=property1,property2,property3,...
 * ERXRest.[EntityName].updateProperties=property1,property2,property3,...
 * </pre>
 * 
 * For example: 
 * <pre>
 * ERXRest.BlogPost.viewProperties=author,title,submissionDate,contents
 * ERXRest.BlogPost.insertProperties=author,title,contents
 * ERXRest.BlogPost.updateProperties=title,contents
 * </pre>
 * 
 * In the above example, we allow users to see the author, title, submissionDate and contents properties of a BlogPost.
 * However, when they insert, we want to automatically set "submissionDate", so we do not allow that property.
 * Additionally we don't want to allow users to change the original author of a post (note this example is a bit
 * contrived), so we don't include "author" key in the updateProperties.
 * </p>
 * 
 * @author mschrag
 */
public abstract class ERXStandardRestEntityDelegate extends ERXAbstractRestEntityDelegate {
	private NSMutableSet _viewPropertyNames;
	private NSMutableSet _updatePropertyNames;
	private NSMutableSet _insertPropertyNames;
	private NSMutableDictionary _propertyAliasForPropertyName;
	private NSMutableDictionary _propertyNameForPropertyAlias;

	/**
	 * Constructs an ERXStandardRestEntityDelegate without an explicit entity reference.
	 * loadDetailsPropertiesAsViewPropertiesForEntityNamed, loadViewPropertiesForEntityNamed,
	 * loadUpdatePropertiesForEntityNamed, and loadInsertPropertiesForEntityNamed will NOT be called.
	 */
	public ERXStandardRestEntityDelegate() {
		_viewPropertyNames = new NSMutableSet();
		_updatePropertyNames = new NSMutableSet();
		_insertPropertyNames = new NSMutableSet();
		_propertyAliasForPropertyName = new NSMutableDictionary();
		_propertyNameForPropertyAlias = new NSMutableDictionary();
	}

	/**
	 * Constructs an ERXStandardRestEntityDelegate with an explicit entity name, optionally loading details properties
	 * as view properties.  Additionally, loadViewPropertiesForEntityNamed, loadUpdatePropertiesForEntityNamed,
	 * and loadInsertPropertiesForEntityNamed WILL be called.
	 * 
	 * @param entityName
	 *            the name of the entity that this delegate represents
	 * @param detailsPropertiesAreViewProperties
	 *            if true, .detailsProperties will be loaded as .viewProperties
	 */
	public ERXStandardRestEntityDelegate(String entityName, boolean detailsPropertiesAreViewProperties) {
		this();
		if (detailsPropertiesAreViewProperties) {
			loadDetailsPropertiesAsViewPropertiesForEntityNamed(entityName);
		}
		loadViewPropertiesForEntityNamed(entityName);
		loadUpdatePropertiesForEntityNamed(entityName);
		loadInsertPropertiesForEntityNamed(entityName);
	}

	/**
	 * Constructs an ERXStandardRestEntityDelegate with an explicit entity name, and WILL load details properties
	 * as view properties.  Additionally, loadViewPropertiesForEntityNamed, loadUpdatePropertiesForEntityNamed,
	 * and loadInsertPropertiesForEntityNamed WILL be called.
	 * 
	 * @param entityName
	 *            the name of the entity that this delegate represents
	 */
	public ERXStandardRestEntityDelegate(String entityName) {
		this(entityName, true);
	}

	/**
	 * Returns the entity alias for the given entity name.  This looks in the
	 * ERXRest.[EntityName].alias property.  If not found, entityName is returned.
	 * 
	 * @param entityName the name of the entity
	 * @return the entity alias
	 */
	public String entityAliasForEntityNamed(String entityName) {
		String entityAlias = ERXProperties.stringForKey(ERXXmlRestResponseWriter.REST_PREFIX + entityName + ".alias");
		if (entityAlias == null) {
			entityAlias = super.entityAliasForEntityNamed(entityName);
		}
		return entityAlias;
	}

	/**
	 * Returns the property alias for the given property name for the specified entity.  
	 * This looks in the ERXRest.[EntityName].[propertyName].alias property.  If not found, 
	 * entityName is returned.
	 * 
	 * @param entity the entity
	 * @param propertyName the name of the property
	 * @return the property alias
	 */
	public String propertyAliasForPropertyNamed(EOEntity entity, String propertyName) {
		String propertyAlias = (String) _propertyAliasForPropertyName.objectForKey(entity.name() + "." + propertyName);
		if (propertyAlias == null) {
			propertyAlias = super.propertyAliasForPropertyNamed(entity, propertyName);
		}
		return propertyAlias;
	}

	/**
	 * Returns the property name for the given property alias for the specified entity.  
	 * This looks in the ERXRest.[EntityName].[propertyName].alias property.  If not found, 
	 * entityName is returned.
	 * 
	 * @param entity the entity
	 * @param propertyAlias the name of the property alias
	 * @return the property name
	 */
	public String propertyNameForPropertyAlias(EOEntity entity, String propertyAlias) {
		String propertyName = (String) _propertyNameForPropertyAlias.objectForKey(entity.name() + "." + propertyAlias);
		if (propertyName == null) {
			propertyName = super.propertyNameForPropertyAlias(entity, propertyAlias);
		}
		return propertyName;
	}

	/**
	 * Loads the view properties for the specified entity from the 
	 * property ERXRest.[EntityName].viewProperties and calls
	 * addViewPropertyName for each.
	 * 
	 * @param entityName the name of the entity to load properties for
	 */
	protected void loadViewPropertiesForEntityNamed(String entityName) {
		String propertiesKey = ERXXmlRestResponseWriter.REST_PREFIX + entityName + ".viewProperties";
		String propertyNamesStr = ERXProperties.stringForKey(propertiesKey);
		if (propertyNamesStr != null) {
			String[] propertyNames = propertyNamesStr.split(",");
			for (int propertyNum = 0; propertyNum < propertyNames.length; propertyNum++) {
				String propertyName = propertyNames[propertyNum];
				addViewPropertyName(entityName, propertyName);
			}
		}
	}

	/**
	 * Loads the update properties for the specified entity from the 
	 * property ERXRest.[EntityName].updateProperties and calls
	 * addUpdatePropertyName for each.
	 * 
	 * @param entityName the name of the entity to load properties for
	 */
	protected void loadUpdatePropertiesForEntityNamed(String entityName) {
		String propertiesKey = ERXXmlRestResponseWriter.REST_PREFIX + entityName + ".updateProperties";
		String propertyNamesStr = ERXProperties.stringForKey(propertiesKey);
		if (propertyNamesStr != null) {
			String[] propertyNames = propertyNamesStr.split(",");
			for (int propertyNum = 0; propertyNum < propertyNames.length; propertyNum++) {
				String propertyName = propertyNames[propertyNum];
				addUpdatePropertyName(entityName, propertyName);
			}
		}
	}

	/**
	 * Loads the insert properties for the specified entity from the 
	 * property ERXRest.[EntityName].insertProperties and calls
	 * addInsertPropertyName for each.
	 * 
	 * @param entityName the name of the entity to load properties for
	 */
	protected void loadInsertPropertiesForEntityNamed(String entityName) {
		String propertiesKey = ERXXmlRestResponseWriter.REST_PREFIX + entityName + ".insertProperties";
		String propertyNamesStr = ERXProperties.stringForKey(propertiesKey);
		if (propertyNamesStr != null) {
			String[] propertyNames = propertyNamesStr.split(",");
			for (int propertyNum = 0; propertyNum < propertyNames.length; propertyNum++) {
				String propertyName = propertyNames[propertyNum];
				addInsertPropertyName(entityName, propertyName);
			}
		}
	}

	/**
	 * Loads the details properties for the specified entity from the
	 * configuration for ERXXmlRestResponseWriter and calls 
	 * addViewPropertyName for each.
	 * 
	 * @param entityName the name of the entity to load properties for
	 */
	protected void loadDetailsPropertiesAsViewPropertiesForEntityNamed(String entityName) {
		String propertiesKey = ERXXmlRestResponseWriter.REST_PREFIX + entityName + ERXXmlRestResponseWriter.DETAILS_PROPERTIES_PREFIX;
		String propertyNamesStr = ERXProperties.stringForKey(propertiesKey);
		if (propertyNamesStr != null) {
			String[] propertyNames = propertyNamesStr.split(",");
			for (int propertyNum = 0; propertyNum < propertyNames.length; propertyNum++) {
				String propertyName = propertyNames[propertyNum];
				addViewPropertyName(entityName, propertyName);
			}
		}
	}

	/**
	 * Adds a view property for the specified entity.
	 * 
	 * @param entityName the name of the entity
	 * @param visiblePropertyName the name of the view property
	 */
	public void addViewPropertyName(String entityName, String visiblePropertyName) {
		_viewPropertyNames.addObject(entityName + "." + visiblePropertyName);

		String propertyAliasKey = ERXXmlRestResponseWriter.REST_PREFIX + entityName + "." + visiblePropertyName + ".alias";
		String propertyAlias = ERXProperties.stringForKey(propertyAliasKey);
		if (propertyAlias != null) {
			_propertyAliasForPropertyName.setObjectForKey(propertyAlias, entityName + "." + visiblePropertyName);
			_propertyNameForPropertyAlias.setObjectForKey(visiblePropertyName, entityName + "." + propertyAlias);
		}
	}

	/**
	 * Adds an update property for the specified entity.
	 * 
	 * @param entityName the name of the entity
	 * @param updatePropertyName the name of the update property
	 */
	public void addUpdatePropertyName(String entityName, String updatePropertyName) {
		addViewPropertyName(entityName, updatePropertyName);
		addInsertPropertyName(entityName, updatePropertyName);
		_updatePropertyNames.addObject(entityName + "." + updatePropertyName);
	}

	/**
	 * Adds an insert property for the specified entity.
	 * 
	 * @param entityName the name of the entity
	 * @param insertPropertyName the name of the insert property
	 */
	public void addInsertPropertyName(String entityName, String insertPropertyName) {
		addViewPropertyName(entityName, insertPropertyName);
		_insertPropertyNames.addObject(entityName + "." + insertPropertyName);
	}

	/**
	 * Returns true if propertyName is declared as an insert property.
	 * 
	 * @param entity the entity
	 * @param eo the object to check
	 * @param propertyName the property name to check
	 * @param context the rest context
	 * @return true if propertyName is declared as an insert property
	 */
	public boolean canInsertProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return _insertPropertyNames.containsObject(entity.name() + "." + propertyName);
	}

	/**
	 * Returns true if propertyName is declared as an update property.
	 * 
	 * @param entity the entity
	 * @param eo the object to check
	 * @param propertyName the property name to check
	 * @param context the rest context
	 * @return true if propertyName is declared as an update property
	 */
	public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return _updatePropertyNames.containsObject(entity.name() + "." + propertyName);
	}

	/**
	 * Returns true if propertyName is declared as a view property.
	 * 
	 * @param entity the entity
	 * @param obj the object to check
	 * @param propertyName the property name to check
	 * @param context the rest context
	 * @return true if propertyName is declared as a view property
	 */
	public boolean canViewProperty(EOEntity entity, Object obj, String propertyName, ERXRestContext context) {
		return _viewPropertyNames.containsObject(entity.name() + "." + propertyName);
	}

	/**
	 * Default empty implementation.  Extend to do custom insertion processing for the object.
	 * 
	 * @param entity the entity of the object
	 * @param eo the inserted object
	 * @param context the rest context
	 * @throws ERXRestException if a general error occurs
	 * @throws ERXRestSecurityException if a security exception occurs
	 */
	public void inserted(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		// DO NOTHING
	}

	/**
	 * Default empty implementation.  Extend to do custom update processing for the object.
	 * 
	 * @param entity the entity of the object
	 * @param eo the updated object
	 * @param context the rest context
	 * @throws ERXRestException if a general error occurs
	 * @throws ERXRestSecurityException if a security exception occurs
	 */
	public void updated(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		// DO NOTHING
	}

	/**
	 * Default empty implementation.  Extend to provide support for custom non-model properties.
	 * 
	 * @param entity the entity of the object
	 * @param key the key of the entity to return an entity definition for
	 */
	public EOEntity nextEntity(EOEntity entity, String key) {
		return null;
	}
}
