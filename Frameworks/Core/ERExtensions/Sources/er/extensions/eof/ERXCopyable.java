package er.extensions.eof;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOProperty;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.foundation.ERXArrayUtilities;

/**
 * This class specifies an interface for flexible copying of
 * {@code EOEnterpriseObject}s, a default implementation for doing the actual
 * copying, and a Utility class that provides convenience methods to make
 * implementing the interface easier.
 * <p>
 * There are several ways to implement this interface:
 * <ol>
 * <li>Manually implement the interface in each {@code Entity.java} class that
 * you want to be able to copy</br>This is the quickest way to get started.
 * Simply add specify that your EO implements ERXCopyable&lt;MyEntity&gt; in the
 * class declaration, then implement the required methods. Here is what you'll
 * need to add to get started:
 * 
 * <pre>
 * &#064;Override
 * public MyEntity copy() {
 * 	MyEntity copy = copy(new NSMutableDictionary&lt;EOGlobalID, ERXCopyable&lt;?&gt;&gt;());
 * 	return copy;
 * }
 * 
 * &#064;Override
 * public MyEntity copy(NSMutableDictionary&lt;EOGlobalID, ERXCopyable&lt;?&gt;&gt; copiedObjects) {
 * 	MyEntity copy = ERXCopyable.DefaultImplementation.copy(copiedObjects, (MyEntity) this);
 * 	return copy;
 * }
 * 
 * &#064;Override
 * public MyEntity duplicate(NSMutableDictionary&lt;EOGlobalID, ERXCopyable&lt;?&gt;&gt; copiedObjects) {
 * 	MyEntity duplicate = ERXCopyable.Utility.deepCopy(copiedObjects, (MyEntity) this);
 * 	return duplicate;
 * }
 * </pre>
 * 
 * </li>
 * <li>Manually implement the interface in a {@link ERXGenericRecord} subclass
 * </br>This is almost as easy as option #1 but has the added advantage of
 * allowing you to implement the interface just once, and then override the
 * default behavior as needed for individual EOs. Simply specify that your
 * {@link ERXGenericRecord} subclass implements
 * ERXCopyable&lt;MyGenericRecord&gt; in the class declaration and then
 * implement the required methods. The defaults are very similar to option #1:
 * 
 * <pre>
 * &#064;Override
 * public MyGenericRecord copy() {
 * 	MyGenericRecord copy = copy(new NSMutableDictionary&lt;EOGlobalID, ERXCopyable&lt;?&gt;&gt;());
 * 	return copy;
 * }
 * 
 * &#064;Override
 * public MyGenericRecord copy(NSMutableDictionary&lt;EOGlobalID, ERXCopyable&lt;?&gt;&gt; copiedObjects) {
 * 	MyGenericRecord copy = ERXCopyable.DefaultImplementation.copy(copiedObjects, this);
 * 	return copy;
 * }
 * 
 * &#064;Override
 * public MyGenericRecord duplicate(NSMutableDictionary&lt;EOGlobalID, ERXCopyable&lt;?&gt;&gt; copiedObjects) {
 * 	MyGenericRecord duplicate = ERXCopyable.Utility.deepCopy(copiedObjects, this);
 * 	return duplicate;
 * }
 * </pre>
 * 
 * </li>
 * <li>Add {@code UserInfo} dictionary entries into your EOModel and make some
 * additions to your EOGenerator templates that will automatically implement
 * this interface based on your model settings.</br>This option is the most
 * powerful and flexible, and requires the least amount of ongoing programming,
 * but takes more work to get setup. It's well worth it if you are going to be
 * implementing ERXCopyable on more than just a few EOs. Here's what you'll need
 * to do:</li>
 * <ol>
 * <li>Create or modify your own EOGenerator template with the following
 * additions:</li>
 * <ul>
 * <li>At the very end of the class declaration line (just prior to the opening
 * '{') insert the following:
 * 
 * <pre>
 * #if(${entity.userInfo.ERXCopyable}) implements ERXCopyable<${entity.classNameWithOptionalPackage}>#end
 * </pre>
 * 
 * </li>
 * <li>Somewhere in the body of the class (immediately before the closing '}' at
 * the end of the template is easiest) insert the following:
 * 
 * <pre>
 *  #if(${entity.userInfo.ERXCopyable} == 'Model' || ${entity.userInfo.ERXCopyable} == 'Default')
 * 
 * 
 *     public ${entity.classNameWithOptionalPackage} copy() {
 *         $entity.classNameWithOptionalPackage copy = copy(new NSMutableDictionary<EOGlobalID, ERXCopyable<?>>());
 *         return copy;
 *     }
 * 
 *     public ${entity.classNameWithOptionalPackage} copy(NSMutableDictionary<EOGlobalID, ERXCopyable<?>> copiedObjects) {
 *         ${entity.classNameWithOptionalPackage} copy = ERXCopyable.DefaultImplementation.copy(copiedObjects, (${entity.classNameWithOptionalPackage}) this);
 *         return copy;
 *     }
 * 
 *     public ${entity.classNameWithOptionalPackage} duplicate(NSMutableDictionary<EOGlobalID, ERXCopyable<?>> copiedObjects) {
 * #if(${entity.userInfo.ERXCopyable} == 'Model')
 *         ${entity.classNameWithOptionalPackage} duplicate = ERXCopyable.Utility.modelCopy(copiedObjects, (${entity.classNameWithOptionalPackage}) this);
 * #elseif(${entity.userInfo.ERXCopyable} == 'Default')
 *         ${entity.classNameWithOptionalPackage} duplicate = ERXCopyable.Utility.deepCopy(copiedObjects, (${entity.classNameWithOptionalPackage}) this);
 * #end
 *         return duplicate;
 *     }
 * #end
 * 
 * </pre>
 * 
 * </li>
 * </ul>
 * </ol>
 * </ol>
 * 
 * @param <T>
 *            the specific subclass of {@code ERXCopyable} that is being copied
 * 
 * @author Chuck Hill
 * @author Sacha Mallais
 * @author David Avendasora
 */
public interface ERXCopyable<T extends ERXCopyable> extends ERXEnterpriseObject {

	/**
	 * <p>
	 * {@link Enum} that specifies the valid ways in which an {@link EOModel}'s
	 * {@link EOProperty}s can be copied.
	 * </p>
	 * <p>
	 * The {@link CopyType} is specified for a given {@link EOProperty} using a
	 * String entry in the property's UserInfo dictionary with a key of
	 * {@code ERXCopyable.CopyType} and a value matching (case-insensitively)
	 * one of the elements of this enum.
	 * </p>
	 * 
	 * @author David Avendasora
	 */
	public enum CopyType {

		/**
		 * <em>For attributes and relationships.</em>Stored as "
		 * {@code ERXCopyable.CopyType = Nullify;}" in the property's UserInfo
		 * dictionary. This setting <b>does not</b> copy the {@code original}'s
		 * value. It sets the {@code copy}'s value to <code>null</code>.
		 */
		NULLIFY("Nullify"),

		/**
		 * <em>For attributes and relationships.</em> Stored as "
		 * {@code ERXCopyable.CopyType = Reference;}" in the {@link EOAttribute}
		 * 's and {@link EORelationship}'s UserInfo dictionary. For attributes
		 * this simply sets the same value on the destination as the source. For
		 * relationships, this sets the {@code copy}'s relationship to point to
		 * the <b>same</b> {@link ERXCopyable} object as the {@code original}'s
		 * relationship does. <b>WARNING:</b> if you use this on a
		 * non-flattening, to-many relationship, the destination objects will be
		 * <b>moved<b> from the {@code original} to the {@code copy}.
		 */
		REFERENCE("Reference"),

		/**
		 * <em>For relationships only.</em> Stored as "
		 * {@code ERXCopyable.CopyType = Shallow;}" in the
		 * {@link EORelationship}'s UserInfo dictionary. New instances of the
		 * destination {@link ERXCopyable} objects will be made and all of the
		 * original's attributes and relationships will be reference copied.
		 */
		SHALLOW("Shallow"),

		/**
		 * <em>For relationships only.</em> Stored as "
		 * {@code ERXCopyable.CopyType = Deep;}" in the {@link EORelationship}'s
		 * UserInfo dictionary. Duplicates each of the destination
		 * {@link ERXCopyable} objects using their implementation of the
		 * {@link #duplicate(NSMutableDictionary)} method.
		 */
		DEEP("Deep"),

		/**
		 * <em>For attributes only.</em> Stored as "
		 * {@code ERXCopyable.CopyType = CurrentTimestamp;}" in the
		 * {@link EOAttribute}'s UserInfo dictionary. This setting <b>does
		 * not</b> copy the {@code original}'s value. It sets the {@code copy}'s
		 * value to the <em>current</em> date and time using
		 * {@code new NSTimestamp()}
		 */
		CURRENT_TIMESTAMP("CurrentTimestamp");

		private final String _type;

		CopyType(String type) {
			_type = type;
		}

		public String type() {
			return _type;
		}

		/**
		 * @param typeAsString
		 *            a String to match (case-insensitive) to a {@link CopyType}
		 * 
		 * @return the {@link CopyType} equivalent to the {@code typeAsString}
		 * 
		 * @author David Avendasora
		 */
		public static CopyType get(String typeAsString) {
			CopyType copyType = null;
			if (typeAsString != null) {
				for (CopyType ct : CopyType.values()) {
					if (typeAsString.equalsIgnoreCase(ct.type())) {
						copyType = ct;
						break;
					}
				}
			}
			return copyType;
		}
	}

	/**
	 * <p>
	 * This class provides a default implementation of ERXCopyable that handles
	 * the most common situations encountered copying {@link EOEnterpriseObject}
	 * s.
	 * </p>
	 * <p>
	 * <b>Notes</b>
	 * <ul>
	 * <li>Debugging information can be turned on with the DEBUG level of the
	 * log4j logger
	 * 
	 * <pre>
	 * er.extensions.eof.ERXCopyable}.
	 * </pre>
	 * 
	 * </li>
	 * <li>If you implement your own deep copy of relationships you should
	 * register the new object before copying its relationships to so that
	 * circular relationships will be copied correctly. For example:
	 * 
	 * <pre>
	 * EOGlobalID globalID = editingContext().globalIDForObject(this);
	 * copiedObjects.setObjectForKey(copy, globalID);
	 * </pre>
	 * 
	 * </li>
	 * </ul>
	 * </p>
	 */
	public static class DefaultImplementation {

		/**
		 * Returns a copy of this object. The actual copy mechanism,
		 * {@link CopyType#REFERENCE Reference}, {@link CopyType#DEEP Deep},
		 * etc. is up to the object being copied. If a copy already exists in
		 * the <code>copiedObjects</code> dictionary, then that existing copy is
		 * returned instead of making a new copy. This allows complex graphs of
		 * objects, including those with cycles, to be copied without producing
		 * duplicate objects.
		 * 
		 * @param <T>
		 *            the Type of the {@code source}
		 * 
		 * @param copiedObjects
		 *            the copied objects keyed on the EOGlobalID of the object
		 *            the copy was made from.
		 * @param source
		 *            the {@code ERXCopyable} to copy
		 * @return a copy of this object
		 */
		@SuppressWarnings("unchecked")
		public static <T extends ERXCopyable> T copy(NSMutableDictionary<EOGlobalID, ERXCopyable<?>> copiedObjects, T source) {
			EOGlobalID globalID = source.editingContext().globalIDForObject(source);
			ERXCopyable.copyLogger.debug("Copying object " + source.userPresentableDescription());
			T copy = (T) copiedObjects.objectForKey(globalID);
			if (copy == null) {
				ERXCopyable.copyLogger.debug("Creating duplicate.");
				copy = (T) source.duplicate(copiedObjects);
				copiedObjects.setObjectForKey(copy, globalID);
			}
			else {
				ERXCopyable.copyLogger.debug("A duplicate was already made. Using the existing duplicate instead of creating a new one.");
			}
			return copy;

		}

		/**
		 * Returns a deep copy of this object.
		 * 
		 * @param <T>
		 *            the Type of the {@code source}
		 * 
		 * @param copiedObjects
		 *            the copied objects keyed on the EOGlobalID of the object
		 *            the copy was made from.
		 * @param source
		 * @return a deep copy of this object
		 */
		public static <T extends ERXCopyable> T duplicate(NSMutableDictionary<EOGlobalID, ERXCopyable<?>> copiedObjects, T source) {
			T duplicate = Utility.deepCopy(copiedObjects, source);
			return duplicate;
		}

	}

	/**
	 * This class provides utility methods for use implementing ERXCopyable.
	 * They handle the most common situations encountered copying EO objects.
	 * The {@link DefaultImplementation} uses them internally. The
	 * implementations of
	 * <ul>
	 * <li>{@link Utility#modelCopy(NSMutableDictionary, ERXCopyable)}</li>
	 * <li>{@link Utility#referenceCopy(ERXCopyable)}</li>
	 * <li>{@link Utility#shallowCopy(ERXCopyable)}</li>
	 * <li>{@link Utility#deepCopy(NSMutableDictionary, ERXCopyable)}</li>
	 * </ul>
	 * should be suitable for most {@link EOEnterpriseObject} instances to use
	 * for their {@link ERXCopyable#duplicate(NSMutableDictionary)} method.
	 * However there are some situations that can not be handled with this
	 * generic code:<br>
	 * <ol>
	 * <li>An attribute or relationship must not be copied (e.g. order numbers).
	 * </li>
	 * <li>An attribute or relationship needs special handling (e.g.
	 * dateModified should reflect when the copy was made, not when the original
	 * object was created).</li>
	 * <li>An EO object should not be copied the same way in all situations
	 * (e.g. the relationship from one object should be copied deeply, but from
	 * another object should be a reference copy).</li>
	 * <li>The relationships must be copied in a certain order (e.g. due to side
	 * effects in the methods setting the relationships).</li>
	 * </ol>
	 * In these situations you will need to write a custom implementation of the
	 * duplicate(NSMutableDictionary) method. This can be as simple as invoking
	 * the default implementation and then cleaning up the result to as complex
	 * as doing it all by hand. {@link Utility} also provides lower-level
	 * methods that you can use to copy any or all attributes or relationships
	 * when creating a custom duplicate(NSMutableDictionary) method.
	 * 
	 * <p>
	 * Debugging information can be turned on with the DEBUG level of the log4j
	 * logger <code>er.extensions.eof.ERXCopyable</code>.
	 * </p>
	 */
	public static class Utility {

		protected static volatile NSMutableDictionary<String, NSArray<EOAttribute>> _exposedPKAndFKAttributeDictionary = null;
		protected static volatile NSMutableDictionary<String, NSArray<EOAttribute>> _classAttributesDictionary = null;
		protected static volatile NSMutableDictionary<String, NSArray<EORelationship>> _classRelationshipsDictionary = null;

		/**
		 * Returns the entity for the current object. Defers to
		 * {@link ERXEOAccessUtilities#entityNamed(EOEditingContext, String)
		 * ERXEOAccessUtilities.entityNamed()} for the actual work.
		 * 
		 * @param <T>
		 *            the Type of the {@code enterpriseObject}
		 * @param enterpriseObject
		 * 
		 * @return {@link EOEntity} of the {@code enterpriseObject}
		 */
		public static <T extends ERXEnterpriseObject> EOEntity entity(T enterpriseObject) {
			EOEditingContext editingContext = enterpriseObject.editingContext();
			String entityName = enterpriseObject.entityName();
			EOEntity entity = ERXEOAccessUtilities.entityNamed(editingContext, entityName);
			return entity;
		}

		/**
		 * When an EO object is created it can already have some relationships
		 * set. This can come from to one relationships that are marked as 'owns
		 * destination' and also from the effects of awakeFromInsertion() and
		 * need some special handling prior to making a copy.
		 * <ol>
		 * <li>All objects are disconnected from the relationship.</li>
		 * <li>If a disconnected object has a temporary EOGlobalID it is
		 * deleted.</li>
		 * </ol>
		 * 
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param source
		 *            the {@code ERXCopyable} that copy was created from
		 * @param destination
		 *            the newly instantiated copy of source that needs to have
		 *            its relationships cleaned
		 */
		public static <T extends ERXCopyable> void cleanRelationships(T source, T destination) {
			ERXCopyable.copyLogger.debug("Cleaning related objects in copy of " + source);
			EOEditingContext editingContext = source.editingContext();
			EOEntity entity = Utility.entity(source);

			// To-Many relationships
			for (String relationshipName : destination.toManyRelationshipKeys()) {
				@SuppressWarnings("unchecked")
				NSArray<ERXCopyable> relatedObjects = (NSArray<ERXCopyable>) destination.valueForKey(relationshipName);

				if (relatedObjects.count() > 0) {
					entity.relationshipNamed(relationshipName);
					ERXCopyable.copyLogger.debug("Removing objects in to-many relationship " + relationshipName);
					for (ERXCopyable relatedObject : relatedObjects) {

						destination.removeObjectFromBothSidesOfRelationshipWithKey(relatedObject, relationshipName);
						if (relatedObject.isNewObject()) {
							editingContext.deleteObject(relatedObject);
						}
					}
				}
			}

			// To-one relationships
			for (String relationshipName : destination.toOneRelationshipKeys()) {
				ERXCopyable relatedObject = (ERXCopyable) destination.valueForKey(relationshipName);
				if (relatedObject != null) {
					ERXCopyable.copyLogger.debug("Removing object in to-one relationship " + relationshipName);
					destination.removeObjectFromBothSidesOfRelationshipWithKey(relatedObject, relationshipName);

					if (Utility.globalIDForObject(relatedObject).isTemporary()) {
						source.editingContext().deleteObject(relatedObject);
					}
				}
			}
		}

		/**
		 * This copies only the class attributes from the source
		 * {@code ERXCopyable} to the destination. However if an attribute is a
		 * class property and also used in a relationship it is assumed to be an
		 * exposed primary- or foreign-key and <em>are not</em> copied. Such
		 * attributes are set to null. See
		 * {@link #exposedPKAndFKAttributes(ERXCopyable)} for details on how
		 * this is determined. It can be used when creating custom
		 * implementations of the
		 * {@link ERXCopyable#duplicate(NSMutableDictionary)} method.
		 * 
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param source
		 *            object to copy attribute values from
		 * @param destination
		 *            object to copy attribute values to
		 */
		public static <T extends ERXCopyable> void copyClassAttributes(T source, T destination) {
			EOEntity entity = Utility.entity(source);
			ERXCopyable.copyLogger.debug("Copying all attributes for  " + source.userPresentableDescription());
			NSArray<EOAttribute> attributes = Utility.classAttributes(entity);
			for (EOAttribute attribute : attributes) {
				Utility.copyAttribute(source, destination, attribute);
			}
		}

		/**
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param source
		 *            object to copy the attribute value from
		 * @param destination
		 *            object to copy the attribute value to
		 * @param attribute
		 *            the {@link EOAttribute} that should have its value copied
		 * @since Feb 10, 2013
		 */
		public static <T extends ERXCopyable> void copyAttribute(T source, T destination, EOAttribute attribute) {
			String attributeName = attribute.name();
			Object sourceValue = source.storedValueForKey(attributeName);
			NSArray<EOAttribute> exposedPKAndFKAttributes = Utility.exposedPKAndFKAttributes(source);
			if (exposedPKAndFKAttributes.containsObject(attribute)) {
				ERXCopyable.copyLogger.debug("Nulling exposed key " + attributeName);
				destination.takeStoredValueForKey(null, attributeName);
			}
			else {
				ERXCopyable.copyLogger.debug("Copying attribute " + attributeName + ", value " + sourceValue);
				destination.takeStoredValueForKey(sourceValue, attributeName);
			}
		}

		/**
		 * Returns a deep copy of this object, the attribute values are
		 * reference copied and the relationships are copied by calling
		 * {@link ERXCopyable#copy(NSMutableDictionary)} on them. Thus each
		 * related object will be copied based on its own implementation of the
		 * {@link ERXCopyable#duplicate(NSMutableDictionary)} method. The copy
		 * is registered with {@code copiedObjects} dictionary as soon as it is
		 * created so that circular relationships can be safely copied without
		 * triggering infinite loops. This method of copying is suitable for
		 * duplicating complex graphs of objects.
		 * 
		 * @param <T>
		 *            the Type of the {@code source} object
		 * @param copiedObjects
		 *            the dictionary of objects that have already been copied,
		 *            keyed on the {@link EOGlobalID}s of the {@code source}
		 *            object that the copy was made from.
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy
		 * @return a copy of this object
		 */
		public static <T extends ERXCopyable> T deepCopy(NSMutableDictionary<EOGlobalID, ERXCopyable<?>> copiedObjects, T source) {

			ERXCopyable.copyLogger.debug("Making deep copy of " + source.userPresentableDescription());

			T copy = Utility.newInstance(source);

			// Register this object right away to handle circular relationships
			copiedObjects.setObjectForKey(copy, Utility.globalIDForObject(source));

			Utility.copyClassAttributes(source, copy);
			Utility.deepCopyClassRelationships(copiedObjects, source, copy);

			return copy;

		}

		/**
		 * This copies related objects from the source {@code ERXCopyable} to
		 * the destination by calling deepCopyRelationship on them. It can be
		 * used when creating custom implementations of the duplicate() method
		 * in ERXCopyable. Only relationships which are class properties are
		 * copied.
		 * 
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * 
		 * @param copiedObjects
		 *            the copied objects keyed on the EOGlobalID of the object
		 *            the copy was made from
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy attribute
		 *            values from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy attribute
		 *            values to
		 */
		public static <T extends ERXCopyable> void deepCopyClassRelationships(NSMutableDictionary<EOGlobalID, ERXCopyable<?>> copiedObjects, T source, T destination) {
			ERXCopyable.copyLogger.debug("Deep copying relationships for  " + source.userPresentableDescription());
			EOEntity entity = Utility.entity(source);
			NSArray<EORelationship> relationships = Utility.classRelationships(entity);
			for (EORelationship relationship : relationships) {
				Utility.deepCopyRelationship(copiedObjects, source, destination, relationship);
			}
		}

		/**
		 * @param entity
		 *            the {@link EOEntity} for the object being copied
		 * @return an array of {@link EOAttribute}s that are designated as class
		 *         attributes in the {@code entity}
		 */
		public static synchronized NSArray<EORelationship> classRelationships(EOEntity entity) {
			String entityName = entity.name();
			if (Utility._classRelationshipsDictionary == null) {
				Utility._classRelationshipsDictionary = new NSMutableDictionary<String, NSArray<EORelationship>>();
			}
			NSArray<EORelationship> classRelationships = Utility._classRelationshipsDictionary.objectForKey(entityName);
			if (classRelationships == null) {
				NSArray<EORelationship> allRelationships = entity.relationships();
				NSMutableArray<EORelationship> relationships = new NSMutableArray<EORelationship>();
				for (EORelationship relationship : allRelationships) {
					if (entity.classProperties().containsObject(relationship)) {
						relationships.addObject(relationship);
					}
				}
				classRelationships = relationships.immutableClone();
				Utility._classRelationshipsDictionary.setObjectForKey(classRelationships, entityName);
			}
			return classRelationships;
		}

		/**
		 * This copies the object(s) for the named relationship from the source
		 * {@code ERXCopyable} to the destination by calling
		 * {@link ERXCopyable#copy(NSMutableDictionary)} on them. Thus each
		 * related object will be copied by its own reference, shallow, deep, or
		 * custom {@link ERXCopyable#duplicate(NSMutableDictionary)} method. It
		 * can be used when creating custom implementations of the
		 * {@link ERXCopyable#duplicate(NSMutableDictionary)} method.
		 * 
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param copiedObjects
		 *            the copied objects keyed on the {@code EOGlobalID} of the
		 *            object the copy was made from
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy attribute
		 *            values from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy attribute
		 *            values to
		 * @param relationship
		 *            the {@link EORelationship} to copy
		 */
		public static <T extends ERXCopyable> void deepCopyRelationship(NSMutableDictionary<EOGlobalID, ERXCopyable<?>> copiedObjects, T source, T destination, EORelationship relationship) {
			if (relationship.isToMany()) {
				Utility.deepCopyToManyRelationship(copiedObjects, source, destination, relationship);
			}
			else {
				Utility.deepCopyToOneRelationship(copiedObjects, source, destination, relationship);
			}
		}

		/**
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param copiedObjects
		 *            the copied objects keyed on the {@code EOGlobalID} of the
		 *            object the copy was made from
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy attribute
		 *            values from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy attribute
		 *            values to
		 * @param relationship
		 *            the to-one {@link EORelationship} to copy
		 */
		public static <T extends ERXCopyable> void deepCopyToOneRelationship(NSMutableDictionary<EOGlobalID, ERXCopyable<?>> copiedObjects, T source, T destination, EORelationship relationship) {
			String relationshipName = relationship.name();
			EOEntity sourceEntity = relationship.entity();
			String sourceEntityName = sourceEntity.name();
			ERXCopyable original = (ERXCopyable) source.valueForKey(relationshipName);
			if (original != null) {
				ERXCopyable.copyLogger.debug("Copying to-one relationship " + sourceEntityName + "." + relationshipName);
				ERXCopyable.copyLogger.debug("                       from " + source);
				ERXCopyable.copyLogger.debug("Copying " + original.userPresentableDescription());
				ERXCopyable copy = original.copy(copiedObjects);
				destination.addObjectToBothSidesOfRelationshipWithKey(copy, relationshipName);
			}
		}

		/**
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param copiedObjects
		 *            the copied objects keyed on the {@code EOGlobalID} of the
		 *            object the copy was made from
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy attribute
		 *            values from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy attribute
		 *            values to
		 * @param relationship
		 *            the to-many {@link EORelationship} to copy
		 */
		public static <T extends ERXCopyable> void deepCopyToManyRelationship(NSMutableDictionary<EOGlobalID, ERXCopyable<?>> copiedObjects, T source, T destination, EORelationship relationship) {
			String relationshipName = relationship.name();
			String inverseRelationshipName = null;
			if (relationship.inverseRelationship() != null) {
				inverseRelationshipName = relationship.inverseRelationship().name();
			}
			ERXCopyable.copyLogger.debug("Copying to-many relationship " + relationshipName);
			ERXCopyable.copyLogger.debug("                        from " + source);

			@SuppressWarnings("unchecked")
			NSArray<T> originals = ((NSArray<T>) source.valueForKey(relationshipName)).immutableClone();
			@SuppressWarnings("unchecked")
			NSArray<ERXCopyable> destinationInitialRelatedObjects = ((NSArray<ERXCopyable>) destination.valueForKey(relationshipName)).immutableClone();
			ERXCopyable.copyLogger.debug("Copying " + originals.count() + " object(s) for relationship " + relationshipName);

			try {
				for (T original : originals) {
					T copy = (T) original.copy(copiedObjects);

					/*
					 * This is a tricky part. Making the copy in the previous line
					 * may have already added objects to the relationship that we
					 * are about to set. We need to check for this so that we do not
					 * create duplicated relationships.
					 */
					if (!destinationInitialRelatedObjects.containsObject(copy)) {
						ERXCopyable.copyLogger.debug("Adding " + copy.userPresentableDescription() + " to " + relationshipName + " of " + destination.userPresentableDescription());
						if (inverseRelationshipName == null) {
							destination.addObjectToBothSidesOfRelationshipWithKey(copy, relationshipName);
						}
						else {
							copy.addObjectToBothSidesOfRelationshipWithKey(destination, inverseRelationshipName);
						}
					}
				}
			}
			catch (ClassCastException e) {
				String message = source.entityName() 
						+ " does not impliment " 
						+ ERXCopyable.class.getCanonicalName() 
						+ ". If you are using the Standard mode, you must manually add the implements clause to the class delcaration in " 
						+ source.getClass().getSimpleName() 
						+ ".java. If you are using the \"Model\" mode you must have an 'ERXCopyable = Model' entry in " 
						+ source.entityName() 
						+ "'s UserInfo dictionary in the EOModel.";
				throw new ClassCastException(message);
			}
		}

		/**
		 * Returns an array of attribute names from the {@link EOEntity} of
		 * {@code source} that are used in the primary key, or in forming
		 * relationships. These can be presumed to be exposed primary or foreign
		 * keys and must be handled differently when copying an object.
		 * 
		 * @param <T>
		 *            the Type of the {@code source} object
		 * @param source
		 *            the {@code ERXCopyable} to copy attribute values from
		 * @return an array of {@link EOAttribute#name()} values from the
		 *         {@code source}'s {@link EOEntity} that are used in forming
		 *         {@link EORelationship}s.
		 **/
		public static <T extends ERXCopyable> NSArray<String> exposedPKandFKAttributeNames(T source) {
			@SuppressWarnings("unchecked")
			NSArray<String> attributeNames = (NSArray<String>) exposedPKAndFKAttributes(source).valueForKey("name");
			return attributeNames;
		}

		/**
		 * Returns an array of class {@link EOAttribute}s from the
		 * {@link EOEntity} of {@code source} that are used in the primary key,
		 * or in forming {@link EORelationship}s. These are presumed to be
		 * exposed primary- or foreign-keys and must be handled differently when
		 * copying an object.
		 * 
		 * @param <T>
		 *            the Type of the {@code source} object
		 * @param source
		 *            the subclass of {@code ERXCopyable} that will be copied
		 * @return an array of attribute names from the {@code EOEntity} of
		 *         source that are used in forming relationships.
		 **/
		public static synchronized <T extends ERXCopyable> NSArray<EOAttribute> exposedPKAndFKAttributes(T source) {
			EOEntity entity = Utility.entity(source);
			String entityName = entity.name();
			if (Utility._exposedPKAndFKAttributeDictionary == null) {
				Utility._exposedPKAndFKAttributeDictionary = new NSMutableDictionary<String, NSArray<EOAttribute>>();
			}
			NSArray<EOAttribute> exposedPKAndFKAttributes = Utility._exposedPKAndFKAttributeDictionary.objectForKey(entityName);
			if (exposedPKAndFKAttributes == null) {
				ERXCopyable.copyLogger.debug("Checking " + entityName + " for Primary and/or Foreign Key attributes that are marked as class properties in the EOModel...");
				NSArray<EOAttribute> classAttributes = classAttributes(entity);
				NSArray<EOAttribute> primaryAndForeignKeyAttributes = primaryAndForeignKeyAttributes(source);
				exposedPKAndFKAttributes = ERXArrayUtilities.intersectingElements(classAttributes, primaryAndForeignKeyAttributes);
				if (exposedPKAndFKAttributes.isEmpty()) {
					ERXCopyable.copyLogger.debug("--> NO Primary or Foreign Key attributes are marked as class properties in the EOModel. Excellent! Good work designer.");
				}
				else {
					ERXCopyable.copyLogger.debug("--> The following Primary and/or Foreign Key attributes are marked as class properties in the EOModel." + exposedPKAndFKAttributes.componentsJoinedByString(",") + ". There better be a good reason.");
				}
				Utility._exposedPKAndFKAttributeDictionary.setObjectForKey(exposedPKAndFKAttributes, entityName);
			}
			return exposedPKAndFKAttributes;
		}

		/**
		 * @param entity
		 *            the {@link EOEntity} for the object being copied
		 * @return an array of {@link EOAttribute}s that are designated as class
		 *         attributes in the {@code entity}
		 */
		public static synchronized NSArray<EOAttribute> classAttributes(EOEntity entity) {
			String entityName = entity.name();
			if (Utility._classAttributesDictionary == null) {
				Utility._classAttributesDictionary = new NSMutableDictionary<String, NSArray<EOAttribute>>();
			}
			NSArray<EOAttribute> classAttributes = Utility._classAttributesDictionary.objectForKey(entityName);
			if (classAttributes == null) {
				NSArray<EOAttribute> allAttributes = entity.attributes();
				NSMutableArray<EOAttribute> attributes = new NSMutableArray<EOAttribute>();
				for (EOAttribute attribute : allAttributes) {
					if (entity.classProperties().containsObject(attribute)) {
						attributes.addObject(attribute);
					}
				}
				classAttributes = attributes.immutableClone();
				Utility._classAttributesDictionary.setObjectForKey(classAttributes, entityName);
			}
			return classAttributes;
		}

		/**
		 * Convenience method to get {@link EOGlobalID} for an
		 * {@link EOEnterpriseObject} from its own {@link EOEditingContext}.
		 * 
		 * @param enterpriseObject
		 *            the {@code EOEnterpriseObject} to return the EOGlobalID
		 *            for
		 * @return the {@code EOGlobalID} of the {@code enterpriseObject}
		 *         parameter
		 */
		public static EOGlobalID globalIDForObject(ERXCopyable enterpriseObject) {
			EOGlobalID globalID = enterpriseObject.editingContext().globalIDForObject(enterpriseObject);
			return globalID;
		}

		/**
		 * This creates and returns a new instance of the same Entity as source.
		 * When an EO object is created it can already have some relationships
		 * and attributes set. These can come from to one relationships that are
		 * marked as 'owns destination' and also from the effects of
		 * awakeFromInsertion(). Preset attributes should be overwritten when
		 * all attributes are copied, but the relationships need some special
		 * handling. See the method
		 * {@link Utility#cleanRelationships(ERXCopyable, ERXCopyable)} for
		 * details on what is done. This method can be used when creating custom
		 * implementations of the
		 * {@link ERXCopyable#duplicate(NSMutableDictionary)} method.
		 * 
		 * @param <T>
		 *            the Type of the {@code source}
		 * 
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy
		 * @return a new instance of the same Entity as source
		 */
		public static <T extends ERXCopyable> T newInstance(T source) {
			// ** require [valid_source] source != null; **/
			ERXCopyable.copyLogger.debug("Making new instance of " + source.userPresentableDescription());
			@SuppressWarnings("unchecked")
			T destination = (T) EOUtilities.createAndInsertInstance(source.editingContext(), source.entityName());
			Utility.cleanRelationships(source, destination);
			return destination;
		}

		/**
		 * Returns a copy of this object by reference. This is equivalent to
		 * <code>return this;</code> on an {@code ERXCopyable}. This method of
		 * copying is suitable for lookup list items and other objects which
		 * should never be duplicated.
		 * 
		 * @param <T>
		 *            the Type of the {@code source}
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy
		 * @return a copy of this object
		 */
		public static <T extends ERXEnterpriseObject> T referenceCopy(T source) {
			ERXCopyable.copyLogger.debug("Reference copying " + source);
			return source;
		}

		/**
		 * Returns a shallow copy of this object, the attribute and
		 * relationships are copied by reference. This method of copying is
		 * suitable for things like an order item where duplication of the
		 * product is not wanted and where the order will not be changed (the
		 * copied order item will be on the original order, not a copy of it).
		 * 
		 * @param <T>
		 *            the Type of the {@code source}
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy
		 * @return a copy of this object
		 */
		public static <T extends ERXCopyable> T shallowCopy(T source) {
			ERXCopyable.copyLogger.debug("Making shallow copy of " + source);
			T copy = Utility.newInstance(source);
			Utility.copyClassAttributes(source, copy);
			Utility.referenceCopyClassRelationships(source, copy);
			return copy;
		}

		/**
		 * This copies related objects from the source {@code ERXCopyable} to
		 * the destination by reference. Only relationships which are class
		 * properties are copied. It can be used when creating custom
		 * implementations of the duplicate() method in ERXCopyable.
		 * 
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy attribute
		 *            values from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy attribute
		 *            values to
		 */
		public static <T extends ERXEnterpriseObject> void referenceCopyClassRelationships(T source, T destination) {
			ERXCopyable.copyLogger.debug("Reference copying relationships for  " + source);
			EOEntity entity = EOUtilities.entityForObject(source.editingContext(), source);
			for (EORelationship relationship : classRelationships(entity)) {
				if (entity.classProperties().containsObject(relationship)) {
					Utility.referenceCopyRelationship(source, destination, relationship);
				}
			}
		}

		/**
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy attribute
		 *            values from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy attribute
		 *            values to
		 * @param relationship
		 *            the {@link EORelationship} to copy from the {@code source}
		 *            to the {@code destination}
		 */
		public static <T extends ERXEnterpriseObject> void referenceCopyRelationship(T source, T destination, EORelationship relationship) {
			if (relationship.isToMany()) {
				Utility.referenceCopyToManyRelationship(source, destination, relationship);
			}
			else {
				Utility.referenceCopyToOneRelationship(source, destination, relationship);
			}

		}

		/**
		 * This copies related objects from the source {@code ERXCopyable} to
		 * the destination by reference. Only relationships which are class
		 * properties are copied. It can be used to streamline creating custom
		 * implementations of the
		 * {@link ERXCopyable#duplicate(NSMutableDictionary)} method.
		 * 
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy to-one
		 *            relationships values from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy to-one
		 *            relationships values to
		 */
		public static <T extends ERXCopyable> void referenceCopyToOneClassRelationships(T source, T destination) {
			ERXCopyable.copyLogger.debug("Reference copying all to-one relationships for  " + source.userPresentableDescription());
			EOEntity entity = Utility.entity(source);
			for (EORelationship relationship : classRelationships(entity)) {
				boolean isClassProperty = entity.classProperties().containsObject(relationship);
				if (!relationship.isToMany() && isClassProperty) {
					Utility.referenceCopyToOneRelationship(source, destination, relationship);
				}
			}
		}

		/**
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy the
		 *            relationship's value from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy the
		 *            relationship's value to
		 * @param relationship
		 *            the {@link EORelationship} to copy from the {@code source}
		 *            to the {@code destination}
		 */
		public static <T extends ERXEnterpriseObject> void referenceCopyToOneRelationship(T source, T destination, EORelationship relationship) {
			String relationshipName = relationship.name();
			ERXEnterpriseObject sourceRelatedEO = (ERXEnterpriseObject) source.valueForKey(relationshipName);
			if (sourceRelatedEO != null) {
				ERXCopyable.copyLogger.debug("Copying " + sourceRelatedEO.userPresentableDescription() + " object for relationship " + relationshipName);
				ERXEnterpriseObject destinationRelatedEO = Utility.referenceCopy(sourceRelatedEO);
				destination.addObjectToBothSidesOfRelationshipWithKey(destinationRelatedEO, relationshipName);
			}
		}

		/**
		 * This copies to-one related objects from the source
		 * {@code ERXCopyable} to the destination by reference. Only
		 * relationships which are class properties are copied. It can be used
		 * when creating custom implementations of the duplicate() method in
		 * ERXCopyable.
		 * 
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy to-many
		 *            {@code relationship}'s values from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy to-many
		 *            {@code relationship}'s values to
		 */
		public static <T extends ERXEnterpriseObject> void referenceCopyToManyClassRelationships(T source, T destination) {
			ERXCopyable.copyLogger.debug("Reference copying all to-many relationships for  " + source.userPresentableDescription());
			EOEntity entity = Utility.entity(source);
			for (EORelationship relationship : classRelationships(entity)) {
				boolean isClassProperty = entity.classProperties().containsObject(relationship);
				if (relationship.isToMany() && isClassProperty) {
					Utility.referenceCopyToManyRelationship(source, destination, relationship);
				}
			}
		}

		/**
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy the
		 *            {@code relationship}'s value(s) from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy the
		 *            {@code relationship}'s value(s) to
		 * @param relationship
		 *            the {@link EORelationship} to copy values for from the
		 *            {@code source} to the {@code destination}
		 * @since Feb 10, 2013
		 */
		public static <T extends ERXEnterpriseObject> void referenceCopyToManyRelationship(T source, T destination, EORelationship relationship) {
			String relationshipName = relationship.name();
			@SuppressWarnings("unchecked")
			NSArray<ERXCopyable> sourceRelatedEOs = (NSArray<ERXCopyable>) source.valueForKey(relationshipName);
			ERXCopyable.copyLogger.debug("Copying " + sourceRelatedEOs.count() + " for relationship " + relationshipName);
			for (ERXCopyable sourceRelatedEO : sourceRelatedEOs) {
				ERXCopyable.copyLogger.debug("Copying " + sourceRelatedEO.userPresentableDescription() + " for relationship " + relationshipName);
				ERXCopyable destinationRelatedEO = Utility.referenceCopy(sourceRelatedEO);
				destination.addObjectToBothSidesOfRelationshipWithKey(destinationRelatedEO, relationshipName);
			}
		}

		/**
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy attribute
		 *            values from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy attribute
		 *            values to
		 * @param relationship
		 *            the {@link EORelationship} to copy from the {@code source}
		 *            to the {@code destination}
		 */
		public static <T extends ERXCopyable> void shallowCopyRelationship(T source, T destination, EORelationship relationship) {
			if (relationship.isToMany()) {
				Utility.shallowCopyToManyRelationship(source, destination, relationship);
			}
			else {
				Utility.shallowCopyToOneRelationship(source, destination, relationship);
			}

		}

		/**
		 * Creates a new instance for each of the of the source's related
		 * objects' Entity and reference copies the attributes and relationships
		 * to it
		 * 
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy the
		 *            {@code relationship}'s value(s) from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy the
		 *            {@code relationship}'s value(s) to
		 * @param relationship
		 *            the {@link EORelationship} to copy values for from the
		 *            {@code source} to the {@code destination}
		 * @since Feb 10, 2013
		 */
		public static <T extends ERXCopyable> void shallowCopyToManyRelationship(T source, T destination, EORelationship relationship) {
			String relationshipName = relationship.name();
			@SuppressWarnings("unchecked")
			NSArray<ERXCopyable> sourceRelatedEOs = (NSArray<ERXCopyable>) source.valueForKey(relationshipName);
			ERXCopyable.copyLogger.debug("Copying " + sourceRelatedEOs.count() + " for relationship " + relationshipName);
			for (ERXCopyable sourceRelatedEO : sourceRelatedEOs) {
				ERXCopyable.copyLogger.debug("Copying " + sourceRelatedEO.userPresentableDescription() + " for relationship " + relationshipName);
				ERXCopyable destinationRelatedEO = Utility.shallowCopy(sourceRelatedEO);
				destination.addObjectToBothSidesOfRelationshipWithKey(destinationRelatedEO, relationshipName);
			}
		}

		/**
		 * Creates a new instance of the source's related object's Entity and
		 * reference copies the attributes and relationships to it
		 * 
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy the
		 *            relationship's value from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy the
		 *            relationship's value to
		 * @param relationship
		 *            the {@link EORelationship} to copy from the {@code source}
		 *            to the {@code destination}
		 */
		public static <T extends ERXEnterpriseObject> void shallowCopyToOneRelationship(T source, T destination, EORelationship relationship) {
			String relationshipName = relationship.name();
			ERXCopyable sourceRelatedEO = (ERXCopyable) source.valueForKey(relationshipName);
			if (sourceRelatedEO != null) {
				ERXCopyable.copyLogger.debug("Copying " + sourceRelatedEO.userPresentableDescription() + " object for relationship " + relationshipName);
				ERXCopyable destinationRelatedEO = Utility.shallowCopy(sourceRelatedEO);
				destination.addObjectToBothSidesOfRelationshipWithKey(destinationRelatedEO, relationshipName);
			}
		}

		/**
		 * <p>
		 * Creates a new instance of the {@code source}'s Entity, then steps
		 * through all attributes and relationships, copying them as defined in
		 * each property's UserInfo dictionary in the EOModel.
		 * </p>
		 * 
		 * <p>
		 * To make use of this method of copying an EO, simply override the
		 * {@link ERXCopyable#duplicate(NSMutableDictionary)
		 * duplicate(NSMutableDictionary)} method in your EO with the following:
		 * 
		 * <pre>
		 * public MyEO duplicate(NSMutableDictionary&lt;EOGlobalID, ERXCopyable&lt;?&gt;&gt; copiedObjects) {
		 * 	MyEO duplicate = ERXCopyable.Utility.modelCopy(copiedObjects, (MyEO) this);
		 * 	return duplicate;
		 * }
		 * </pre>
		 * 
		 * </p>
		 * 
		 * @param <T>
		 *            the Type of the {@code source} and returned objects
		 * @param copiedObjects
		 *            the copied objects keyed on the {@link EOGlobalID} of the
		 *            object the copy was made from.
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy
		 * @return a copy of the {@code source} object in the same
		 *         {@link EOEditingContext}
		 * 
		 * @author David Avendasora
		 */
		public static <T extends ERXCopyable> T modelCopy(NSMutableDictionary<EOGlobalID, ERXCopyable<?>> copiedObjects, T source) {
			EOEntity entity = Utility.entity(source);
			EOModel model = entity.model();
			NSDictionary<String, Object> entityUserInfo = entity.userInfo();
			String entityName = entity.name();
			String modelName = model.name();

			if (!entityUserInfo.containsKey(ERXCopyable.ERXCOPYABLE_KEY)) {
				String message = "In order to use modelCopy the \"" + ERXCopyable.ERXCOPYABLE_KEY + "\" key must be set in the UserInfo dictionary of the \"" + entityName + "\" Entity in the " + modelName + " EOModel.";
				throw new IllegalStateException(message);
			}
			ERXCopyable.copyLogger.debug("Making copy of " + source + " based on UserInfo settings in the " + modelName + " EOModel");
			T copy = Utility.newInstance(source);

			// Register this object right away to handle circular relationships
			copiedObjects.setObjectForKey(copy, Utility.globalIDForObject(source));

			for (EOProperty property : entity.classProperties()) {
				if (property instanceof EOAttribute) {
					EOAttribute attribute = (EOAttribute) property;
					if (exposedPKAndFKAttributes(source).containsObject(attribute)) {
						copy.takeStoredValueForKey(null, attribute.name());
					}
					else {
						Utility.modelCopyAttribute(source, copy, attribute);
					}
				}
				else {
					EORelationship relationship = (EORelationship) property;
					Utility.modelCopyRelationship(copiedObjects, source, copy, relationship);
				}
			}
			return copy;
		}

		/**
		 * Reads the values set in the EOModel's attribute and relationship
		 * UserInfo dictionaries and then uses them to control how the
		 * {@code source} object is copied.
		 * 
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 * @param copiedObjects
		 *            the copied objects keyed on the {@link EOGlobalID} of the
		 *            object the copy was made from.
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy all (to-one
		 *            and to-many) class relationships values from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy all (to-one
		 *            and to-many) class relationships values to
		 */
		public static <T extends ERXCopyable> void modelCopyClassRelationships(NSMutableDictionary<EOGlobalID, ERXCopyable<?>> copiedObjects, T source, T destination) {
			ERXCopyable.copyLogger.debug("Model-copying class relationships for  " + source.userPresentableDescription());
			for (EORelationship relationship : Utility.classRelationships(Utility.entity(source))) {
				Utility.modelCopyRelationship(copiedObjects, source, destination, relationship);
			}
		}

		/**
		 * Reads the values set in the EOModel's attribute and relationship
		 * UserInfo dictionaries and then uses them to control how the
		 * {@code source} object is copied.
		 * 
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 *            objects
		 * @param copiedObjects
		 *            the copied objects keyed on the {@link EOGlobalID} of the
		 *            object the copy was made from. the Type of the
		 *            {@code source} and {@code destination}
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy the
		 *            {@code relationship}'s value(s) from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy the
		 *            {@code relationship}'s value(s) to
		 * @param relationship
		 *            the {@link EORelationship} to copy values for from the
		 *            {@code source} to the {@code destination}
		 */
		public static <T extends ERXCopyable> void modelCopyRelationship(NSMutableDictionary<EOGlobalID, ERXCopyable<?>> copiedObjects, T source, T destination, EORelationship relationship) {
			String relationshipName = relationship.name();
			CopyType copyType = Utility.copyType(relationship);
			ERXCopyable.copyLogger.debug("CopyType \"" + copyType.type() + "\" specified for " + relationshipName);
			switch (copyType) {
			case REFERENCE:
				Utility.referenceCopyRelationship(source, destination, relationship);
				break;
			case SHALLOW:
				Utility.shallowCopyRelationship(source, destination, relationship);
				break;
			case DEEP:
				Utility.deepCopyRelationship(copiedObjects, source, destination, relationship);
				break;
			case NULLIFY:
				destination.takeStoredValueForKey(null, relationshipName);
				break;
			default:
				handleMissingOrInvalidCopyType(relationship, copyType);
			}
		}

		/**
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 *            objects
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy the class
		 *            attribute values from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy the class
		 *            attribute values to
		 */
		public static <T extends ERXCopyable> void modelCopyClassAttributes(T source, T destination) {
			ERXCopyable.copyLogger.debug("Model-copying class attributes for  " + source.userPresentableDescription());
			NSArray<EOAttribute> attributesToCopy = Utility.classAttributes(Utility.entity(source));
			for (EOAttribute attribute : attributesToCopy) {
				modelCopyAttribute(source, destination, attribute);
			}
		}

		/**
		 * @param <T>
		 *            the Type of the {@code source} and {@code destination}
		 *            objects
		 * @param source
		 *            the subclass of {@code ERXCopyable} to copy the
		 *            {@code attribute}'s value from
		 * @param destination
		 *            the subclass of {@code ERXCopyable} to copy the
		 *            {@code attribute}'s value to
		 * @param attribute
		 *            the {@link EOAttribute} that should have its value copied
		 *            from the {@code source} to the {@code destination}
		 */
		public static <T extends ERXCopyable> void modelCopyAttribute(T source, T destination, EOAttribute attribute) {
			String attributeName = attribute.name();
			CopyType copyType = Utility.copyType(attribute);
			switch (copyType) {
			case REFERENCE:
				Utility.copyAttribute(source, destination, attribute);
				break;
			case CURRENT_TIMESTAMP:
				destination.takeStoredValueForKey(new NSTimestamp(), attributeName);
				break;
			case NULLIFY:
				destination.takeStoredValueForKey(null, attributeName);
				break;
			case DEEP:
				handleMissingOrInvalidCopyType(attribute, copyType);
				break;
			default:
				handleMissingOrInvalidCopyType(attribute, copyType);
			}
		}

		/**
		 * @param property
		 *            the attribute or relationship being copied
		 * @return the {@link CopyType} value specified for the
		 *         {@code ERXCopyable.CopyType} key in the {@code property}'s
		 *         UserInfo dictionary in the EOModel
		 */
		public static CopyType copyType(EOProperty property) {
			CopyType copyType;
			if (property instanceof EOAttribute) {
				EOAttribute attribute = (EOAttribute) property;
				@SuppressWarnings("unchecked")
				NSDictionary<String, Object> userInfo = attribute.userInfo();
				copyType = Utility.copyType(attribute, userInfo);
			}
			else {
				EORelationship relationship = (EORelationship) property;
				NSDictionary<String, Object> userInfo = relationship.userInfo();
				copyType = Utility.copyType(relationship, userInfo);
			}
			return copyType;
		}

		/**
		 * Abstracted out of {@link #copyType(EOProperty)} to handle exceptions
		 * if the userInfo dictionary is null, there is no
		 * {@code ERXCopyable.CopyType} key, or the value for the key is null or
		 * invalid.
		 * 
		 * @param property
		 *            the attribute or relationship being copied
		 * @param userInfo
		 *            the {@code property}'s UserInfo dictionary that contains a
		 *            {@code ERXCopyable.CopyType} key
		 * @return the {@link CopyType} value specified for the
		 *         {@code ERXCopyable.CopyType} key in {@code userInfo}
		 *         dictionary for {@code property}'s UserInfo dictionary in the
		 *         EOModel
		 */
		public static CopyType copyType(EOProperty property, NSDictionary<String, Object> userInfo) {
			if (userInfo == null) {
				Utility.handleMissingOrInvalidCopyType(property, null);
			}
			String userInfoKey = ERXCopyable.COPY_TYPE_KEY;
			@SuppressWarnings("null")
			String copyTypeString = (String) userInfo.objectForKey(userInfoKey);
			CopyType copyType = (CopyType.get(copyTypeString));
			if (copyType == null) {
				Utility.handleMissingOrInvalidCopyType(property, copyType);
			}
			return copyType;
		}

		public static void handleMissingERXCopyableKey(Class<?> invalidClass) {
			String exceptionMessage = "To use ERXCopyable with " + invalidClass.getSimpleName() + " it must implement " + ERXCopyable.class.getSimpleName() + ".";
			throw new IllegalStateException(exceptionMessage);
		}

		/**
		 * Creates a meaningful error message to be displayed when
		 * {@code modelCopy} was specified for the {@code ERXCopyable} key in an
		 * Entity's UserInfo dictionary, but not all attributes and
		 * relationships have a valid {@code ERXCopyable.CopyType} specified.
		 * 
		 * @param property
		 *            the attribute or relationship being copied
		 * @param copyType
		 *            the invalid {@link CopyType} specified in the
		 *            {@code property}'s UserInfo dictionary in the EOModel
		 */
		public static void handleMissingOrInvalidCopyType(EOProperty property, CopyType copyType) {
			String propertyType = Utility.propertyType(property);
			@SuppressWarnings("unchecked")
			NSArray<String> copyTypes = (NSArray<String>) Utility.copyTypes(property).valueForKey("type");
			String validCopyTypes = copyTypes.componentsJoinedByString(", ");
			String propertyName = property.name();
			EOEntity entity = property.entity();
			String entityName = entity.name();
			EOModel model = entity.model();
			String modelName = model.name();
			String exceptionMessage = "To use ERXCopyable's modelCopy methods the \"" + ERXCopyable.COPY_TYPE_KEY + "\" key must be set in the UserInfo dictionary of the \"" + entityName + "." + propertyName + "\" " + propertyType + " in " + modelName + " AND it must be set to one of these values: {" + validCopyTypes + "}. " + copyType + " is not a valid value.";
			throw new IllegalStateException(exceptionMessage);
		}

		/**
		 * @param property
		 *            the attribute or relationship being copied
		 * @return "attribute" if the passed in property is an instance of
		 *         {@link EOAttribute}, "relationship" if it is an instance of
		 *         {@link EORelationship}
		 */
		public static String propertyType(EOProperty property) {
			String propertyType;
			if (property instanceof EOAttribute) {
				propertyType = "attribute";
			}
			else {
				propertyType = "relationship";
			}
			return propertyType;
		}

		/**
		 * @param property
		 *            the attribute or relationship being copied
		 * @return an array of the valid {@link CopyType}s for the passed-in
		 *         {@link EOProperty}
		 */
		public static NSArray<CopyType> copyTypes(EOProperty property) {
			NSArray<CopyType> validCopyTypes;
			if (property instanceof EOAttribute) {
				validCopyTypes = new NSArray<CopyType>(CopyType.NULLIFY, CopyType.CURRENT_TIMESTAMP, CopyType.REFERENCE);
			}
			else {
				validCopyTypes = new NSArray<CopyType>(CopyType.NULLIFY, CopyType.REFERENCE, CopyType.SHALLOW, CopyType.DEEP);
			}
			return validCopyTypes;
		}

		/**
		 * @param <T>
		 *            the Type of the {@code source} object
		 * @param source
		 *            the subclass of {@code ERXCopyable} that is being copied
		 * @return an array of {@link EOAttribute}s that are the Primary- and
		 *         Foreign-Key attributes for the {@code source} subclass of
		 *         {@link ERXCopyable}
		 */
		public static <T extends ERXCopyable> NSArray<EOAttribute> primaryAndForeignKeyAttributes(T source) {
			EOEntity entity = Utility.entity(source);
			NSArray<EOAttribute> primaryKeyAttributes = entity.primaryKeyAttributes();
			NSMutableSet<EOAttribute> keyAttributes = new NSMutableSet<EOAttribute>(primaryKeyAttributes);
			NSArray<EORelationship> classRelationships = Utility.classRelationships(entity);
			for (EORelationship relationship : classRelationships) {
				NSArray<EOAttribute> foreignKeyAttributes = relationship.sourceAttributes();
				keyAttributes.addObjectsFromArray(foreignKeyAttributes);
			}
			NSArray<EOAttribute> primaryAndForeignKeyAttributes = keyAttributes.allObjects();
			return primaryAndForeignKeyAttributes;
		}
	}

	public static Logger copyLogger = LoggerFactory.getLogger(ERXCopyable.class);

	/**
	 * "{@link ERXCopyable}" which is the exact String that must be used as the
	 * key in the Entity's EOModel UserInfo dictionary to designate it as
	 * implementing {@link ERXCopyable} interface.
	 */
	public static final String ERXCOPYABLE_KEY = ERXCopyable.class.getSimpleName();

	/**
	 * "{@code ERXCopyable.CopyType}" which is the exact String that must be
	 * used as the key in an Attribute's or Relationship's EOModel UserInfo
	 * dictionary to specify the way the property should be copied by
	 * {@link ERXCopyable}.
	 */
	public static final String COPY_TYPE_KEY = ERXCOPYABLE_KEY + "." + CopyType.class.getSimpleName();

	/**
	 * Convenience cover method for {@link #copy(NSMutableDictionary)} that
	 * creates the dictionary for you. You can use this to start the copying of
	 * a graph if you have no need to reference the dictionary.
	 * 
	 * @return a copy of this object
	 */
	public T copy();

	/**
	 * Returns a copy of this object, copying related objects as well. The
	 * actual copy mechanism (by reference, deep, or custom) for each object is
	 * up to the object being copied. If a copy already exists in
	 * {@code copiedObjects}, then that copy is returned instead of making a new
	 * copy. This allows complex graphs of objects, including those with cycles,
	 * to be copied without producing duplicate objects. The graph of copied
	 * objects will be the same regardless of where copy is started with two
	 * exceptions: if it is started on a reference copied object or if a
	 * reference copied object is the only path between two disconnected parts
	 * of the graph. In these cases the reference copied object prevents the
	 * copy from following the graph further.
	 * 
	 * @param copiedObjects
	 *            the copied objects keyed on the {@link EOGlobalID} of the
	 *            object the copy was made from.
	 * @return a copy of this object
	 */
	public T copy(NSMutableDictionary<EOGlobalID, ERXCopyable<?>> copiedObjects);

	/**
	 * Returns a copy of this object. Each {@code ERXCopyable} should implement
	 * this to produce the actual copy by an appropriate mechanism (reference,
	 * shallow, deep, or custom).
	 * 
	 * @param copiedObjects
	 *            the copied objects keyed on the {@link EOGlobalID} of the
	 *            object the copy was made from.
	 * @return a copy of this object
	 */
	public T duplicate(NSMutableDictionary<EOGlobalID, ERXCopyable<?>> copiedObjects);
}
