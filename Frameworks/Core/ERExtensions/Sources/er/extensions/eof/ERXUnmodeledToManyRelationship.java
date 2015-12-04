package er.extensions.eof;

import java.util.concurrent.ConcurrentHashMap;

import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * A class than is composited into an EO to provide common toMany functionality
 * for the case where the toMany cannot be modeled in EOF due to the unusually
 * large size possibilities of the toMany relationship.
 * <p>
 * This class is for simple to many relationships, has not been tested on 
 * flattened toMany relationships having a join table, aka "many-to-many" relationships.
 * <p>
 * Usage: Lazily create a private instance of this inside an EO passing in the
 * appropriate constructor parameters and then implement cover methods similar
 * to those that would have been available thru the normal Entity templates
 * calling the corresponding methods of this class.
 * <p>
 * For example, you might do this in an entity named CTMediaTemplate that formerly had a 'messages'
 * relationship to CTMessage but due to the huge size of the toMany impacting performance, the toMany side of the
 * relationship had to be deleted from the EOModel:
 * 
 * <pre><code>
 	private ERXUnmodeledToManyRelationship&lt;CTMediaTemplate, CTMessage&gt; _messagesRelationship;

	// Lazily initialize the helper class
	public ERXUnmodeledToManyRelationship&lt;CTMediaTemplate, CTMessage&gt; messagesRelationship() {
		if (_messagesRelationship == null) {
			_messagesRelationship = new ERXUnmodeledToManyRelationship&lt;CTMediaTemplate, CTMessage&gt;(this,
							CTMessage.ENTITY_NAME, CTMessage.XKEY_MEDIA_TEMPLATE);
		}
		return _messagesRelationship;
	}
	
	public Integer countMessages() {
		return messagesRelationship().countObjects();
	}

	public EOQualifier qualifierForMessages() {
		return messagesRelationship().qualifierForObjects();
	}

	public NSArray&lt;CTMessage&gt; messages() {
		return messagesRelationship().objects();
	}

	public ERXFetchSpecification&lt;CTMessage&gt; fetchSpecificationForMessages() {
		return messagesRelationship().fetchSpecificationForObjects();
	}

	public NSArray&lt;CTMessage&gt; messages(EOQualifier qualifier) {
		return messagesRelationship().objects(qualifier);
	}

	public NSArray&lt;CTMessage&gt; messages(EOQualifier qualifier, boolean fetch) {
		return messagesRelationship().objects(qualifier, null, fetch);
	}

	public NSArray&lt;CTMessage&gt; messages(EOQualifier qualifier, NSArray&lt;EOSortOrdering&gt; sortOrderings, boolean fetch) {
		return messagesRelationship().objects(qualifier, sortOrderings, fetch);
	}

	public void addToMessagesRelationship(CTMessage object) {
		messagesRelationship().addToObjectsRelationship(object);
	}

	public void removeFromMessagesRelationship(CTMessage object) {
		messagesRelationship().removeFromObjectsRelationship(object);
	}

	public void deleteMessagesRelationship(CTMessage object) {
		messagesRelationship().deleteObjectRelationship(object);
	}

	public void deleteAllMessagesRelationships() {
		messagesRelationship().deleteAllObjectsRelationships();
	}
	</code></pre>
 * @author kieran
 * 
 */
public class ERXUnmodeledToManyRelationship<S extends ERXEnterpriseObject, D extends ERXEnterpriseObject> {

	private final S sourceObject;
	private final String destinationEntityName;
	private final ERXKey<S> reverseRelationshipKey;
	private final boolean isDeep;

	// Cache the entityHierarchies rather than fiddle with EOEntities for every instance of this class (once per EO)
	private static ConcurrentHashMap<String, NSArray<String>> _entityHierarchies = new ConcurrentHashMap<String, NSArray<String>>();


	/**
	 * Standard constructor where the destination entity is a single type. Excludes entities that might inherit from the destination entity
	 * 
	 * @param sourceObject
	 * @param destinationEntityName
	 * @param reverseRelationshipKey
	 */
	public ERXUnmodeledToManyRelationship(S sourceObject, String destinationEntityName, ERXKey<S> reverseRelationshipKey) {
		this(sourceObject, destinationEntityName, reverseRelationshipKey, false);
	}
	
	/**
	 * A constructor that allows isDeep to be set to true to handle destination entity that is the super class of an inheritance hierarchy.
	 * The inherited entities are included in the methods that return arrays of destination EOEnterpriseObjects.
	 * 
	 * @param sourceObject
	 * @param destinationEntityName
	 * @param reverseRelationshipKey
	 * @param isDeep
	 */
	public ERXUnmodeledToManyRelationship(S sourceObject, String destinationEntityName, ERXKey<S> reverseRelationshipKey, boolean isDeep) {
		this.sourceObject = sourceObject;
		this.destinationEntityName = destinationEntityName;
		this.reverseRelationshipKey = reverseRelationshipKey;
		this.isDeep = isDeep;
	}

	/**
	 * @return the total count of objects in the relationship
	 */
	public Integer countObjects() {
		int count = 0;

		// Can only do SQL count if not a new unsaved object
		if (!sourceObject.isNewObject()) {
			count += ERXEOControlUtilities.objectCountWithQualifier(sourceObject.editingContext(),
							destinationEntityName, qualifierForObjects()).intValue();
		}

		// Add count of unsaved related objects
		count += insertedObjects().count();

		return Integer.valueOf(count);
	}

	/**
	 * @return the {@link EOQualifier} that qualifies the toMany destination
	 *         objects
	 */
	public EOQualifier qualifierForObjects() {
		return reverseRelationshipKey.eq(sourceObject);
	}

	/**
	 * @return the related destination objects
	 */
	public NSArray<D> objects() {
		// Grab the unsaved ones
		NSMutableArray<D> objects = insertedObjects();
		// Add the persisted ones form the database
		objects.addObjectsFromArray(persistedObjects());
		// return them all
		return objects.immutableClone();
	}

	/**
	 * @return the {@link ERXFetchSpecification} that fetches the destination
	 *         toMany objects that have been persisted to the database
	 */
	public ERXFetchSpecification<D> fetchSpecificationForObjects() {
		return new ERXFetchSpecification<D>(destinationEntityName, qualifierForObjects(), null, false, isDeep, null);
	}

	public NSArray<D> objects(EOQualifier qualifier) {
		return objects(qualifier, null, false);
	}

	public NSArray<D> objects(EOQualifier qualifier, boolean fetch) {
		return objects(qualifier, null, fetch);
	}

	public NSArray<D> objects(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
		NSArray<D> results;
		if (fetch) {
			EOQualifier fullQualifier;
			EOQualifier inverseQualifier = qualifierForObjects();

			if (qualifier == null) {
				fullQualifier = inverseQualifier;
			} else {
				NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
				qualifiers.addObject(qualifier);
				qualifiers.addObject(inverseQualifier);
				fullQualifier = new EOAndQualifier(qualifiers);
			}
			ERXFetchSpecification<D> fs = fetchSpecificationForObjects();
			fs.setSortOrderings(sortOrderings);
			fs.setQualifier(fullQualifier);
			fs.setRefreshesRefetchedObjects(fetch);
			results = fs.fetchObjects(sourceObject.editingContext());
		} else {
			results = objects();
			if (qualifier != null) {
				results = ERXQ.filtered(results, qualifier);
			}
			if (sortOrderings != null) {
				results = ERXS.sorted(results, sortOrderings);
			}
		}
		return results;
	}

	/**
	 * @return the fetched pre-existing objects from the database
	 */
	private NSArray<D> persistedObjects() {
		ERXFetchSpecification<D> fs = fetchSpecificationForObjects();
		return fs.fetchObjects(sourceObject.editingContext());
	}

	/**
	 * @return the array of related instances that
	 *         are inserted into this editing context, but not yet saved.
	 */
	private NSMutableArray<D> insertedObjects() {
		return ERXEOControlUtilities.insertedObjects(sourceObject.editingContext(), destinationEntityNames(),
						qualifierForObjects());
	}

	public void addToObjectsRelationship(D object) {
		object.addObjectToBothSidesOfRelationshipWithKey(sourceObject, reverseRelationshipKey.toString());
	}

	public void addToObjectsRelationship(NSArray<D> objects) {
		for (D object : objects) {
			object.addObjectToBothSidesOfRelationshipWithKey(sourceObject, reverseRelationshipKey.toString());
		}
	}

	public void removeFromObjectsRelationship(D object) {
		object.removeObjectFromBothSidesOfRelationshipWithKey(sourceObject, reverseRelationshipKey.toString());
	}

	public void removeFromObjectsRelationship(NSArray<D> objects) {
		for (D object : objects) {
			object.removeObjectFromBothSidesOfRelationshipWithKey(sourceObject, reverseRelationshipKey.toString());
		}
	}
	
	public void removeAllFromObjectsRelationship() {
		NSArray<D> objects = objects().immutableClone();
		for (D object : objects) {
			removeFromObjectsRelationship(object);
		}
	}

	public void deleteObjectRelationship(D object) {
		removeFromObjectsRelationship(object);
		sourceObject.editingContext().deleteObject(object);
	}

	public void deleteAllObjectsRelationships() {
		NSArray<D> objects = objects().immutableClone();
		for (D object : objects) {
			deleteObjectRelationship(object);
		}
	}
	
	private NSArray<String> _destinationEntityNames;
	
	/** @return the destination entity names. If this is an inheritance hierarchy, the subclass entities of the specified entity are included */
	private NSArray<String> destinationEntityNames() {
		if ( _destinationEntityNames == null ) {
			if (isDeep) {
				_destinationEntityNames = entityHierarchyNamesForEntityNamed(sourceObject.editingContext(), destinationEntityName);
				
			} else {
				_destinationEntityNames = new NSArray<String>(destinationEntityName);
			}
		}
		return _destinationEntityNames;
	}
	
	/**
	 * @param rootEntityName
	 * @return a list of all concrete entity names that inherit from rootEntityName, including rootEntityName itself if it is concrete.
	 */
	private static NSArray<String> entityHierarchyNamesForEntityNamed(EOEditingContext ec, String rootEntityName) {
		NSArray<String> cachedResult = _entityHierarchies.get(rootEntityName);
		if (cachedResult == null) {
			cachedResult = ERXEOAccessUtilities.entityHierarchyNamesForEntityNamed(ec, rootEntityName);
			_entityHierarchies.put(rootEntityName, cachedResult);
		}
		return cachedResult;
	}
	

}
