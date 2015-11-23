package er.extensions.partials;

import java.io.Serializable;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EORelationshipManipulation;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSValidation;
import com.webobjects.foundation.NSValidation.ValidationException;

import er.extensions.eof.ERXGenericRecord;

/**
 * For overview information on partials, read the {@code package.html} in
 * {@code er.extensions.partials}.
 * <p>
 * {@code ERXPartial} is the superclass of all partial entity implementations.
 * {@code ERXPartial} is not itself an EO, but is acts as a partial typesafe
 * wrapper around an existing base EO (which must extend
 * {@link ERXPartialGenericRecord}). For instance, the base entity might be
 * {@code Person}, but the partial may be {@code CalendarPerson} which might
 * expose methods like {@code calendarPerson.scheduledEvents()}.
 * <p>
 * To obtain a partial, you request an instance from a base EO. Take the
 * {@code Person} example from above. You can access the interface of the
 * {@code CalendarPerson} partial in two ways:
 * <pre>
 * <code>Person person = ...;
 * CalendarPerson calendarPerson = person.partialForClass(CalendarPerson.class);</code>
 * </pre>
 * 
 * or
 * 
 * <pre>
 * <code>Person person = ...;
 * CalendarPerson calendarPerson = person.valueForKey("@CalendarPerson");</code>
 * </pre>
 * 
 * which allows easy use of the partial entities in component bindings, like
 * {@code person.@CalendarPerson.scheduledEvents}.
 * 
 * @author mschrag
 * @param <T>
 *            the EO class that this is a partial of
 */
public class ERXPartial<T extends ERXGenericRecord> implements Serializable {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This partial's primary EO
	 */
	private T _primaryEO;

	/**
	 * Sets this partial's primary EO.
	 *
	 * @param primaryEO the primary EO
	 */
	public void setPrimaryEO(T primaryEO) {
		_primaryEO = primaryEO;
	}

	/**
	 * Returns this partial's primary EO.
	 *
	 * @return primary EO
	 */
	public T primaryEO() {
		return _primaryEO;
	}
	
	/**
	 * When partial entities are initialized the {@link EOEntity} is removed from the
	 * model, making it impossible to query attributes and relationships later.
	 * 
	 * @return array of String keys for the partial entity attributes
	 */
	public static NSArray<String> partialAttributes() {
		return NSArray.EmptyArray;
	}

	/**
	 * When partial entities are initialized the {@link EOEntity} is removed from the
	 * model making it impossible to query attributes and relationships later.
	 * 
	 * @return array of String keys for the partial entity relationships
	 */
	public static NSArray<String> partialRelationships() {
		return NSArray.EmptyArray;
	}

	/**
	 * When partial entities are initialized the {@link EOEntity} is removed
	 * from the model making it impossible to query attributes and relationships
	 * later.
	 * 
	 * @return array of String keys for the partial entity attributes and
	 *         relationships
	 */
	public static NSArray<String> partialProperties() {
		return partialAttributes().arrayByAddingObjectsFromArray(partialRelationships());
	}

	/**
	 * Tests the given keypath to see if it is a valid attribute or relationship
	 * for this partial.
	 *
	 * @param keypath a keypath
	 * @return {@code true} if {@code keypath} matches an attribute or
	 *         relationship
	 */
	public boolean isPartialKeypath(String keypath) {
		for (String key : partialProperties()) {
			if (key.equals(keypath)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the primary EO's {@link EOEditingContext}.
	 * 
	 * @return primary EO's {@link EOEditingContext}
	 */
	public EOEditingContext editingContext() {
		return _primaryEO.editingContext();
	}
	
	/**
	 * Returns {@code _primaryEO.storedValueForKey(key)}.
	 * 
	 * @param key
	 *            a key
	 * @return {@code _primaryEO.storedValueForKey(key)}
	 */
    public Object storedValueForKey(String key) {
    	return _primaryEO.storedValueForKey(key);
    }

	/**
	 * Calls {@code _primaryEO.takeStoredValueForKey(value, key)}.
	 * 
	 * @param value
	 *            a value
	 * @param key
	 *            a key
	 */
    public void takeStoredValueForKey(Object value, String key) {
    	_primaryEO.takeStoredValueForKey(value, key);
    }

	/**
	 * Calls {@code _primaryEO.includeObjectIntoPropertyWithKey(value, key)}.
	 * 
	 * @param value
	 *            a value
	 * @param key
	 *            a key
	 */
	public void includeObjectIntoPropertyWithKey(Object value, String key) {
		_primaryEO.includeObjectIntoPropertyWithKey(value, key);
	}
    
	/**
	 * Calls {@code _primaryEO.excludeObjectFromPropertyWithKey(value, key)}.
	 * 
	 * @param value
	 *            a value
	 * @param key
	 *            a key
	 */
	public void excludeObjectFromPropertyWithKey(Object value, String key) {
		_primaryEO.excludeObjectFromPropertyWithKey(value, key);
	}
    
	/**
	 * Calls
	 * {@code _primaryEO.addObjectToBothSidesOfRelationshipWithKey(eo, key)}.
	 * 
	 * @param eo
	 *            an {@link EORelationshipManipulation}
	 * @param key
	 *            a key
	 */
	public void addObjectToBothSidesOfRelationshipWithKey(EORelationshipManipulation eo, String key) {
		_primaryEO.addObjectToBothSidesOfRelationshipWithKey(eo, key);
	}

	/**
	 * Calls
	 * {@code _primaryEO.removeObjectFromBothSidesOfRelationshipWithKey(eo, key)}.
	 * 
	 * @param eo
	 *            an {@link EORelationshipManipulation}
	 * @param key
	 *            a key
	 */
	public void removeObjectFromBothSidesOfRelationshipWithKey(EORelationshipManipulation eo, String key) {
		_primaryEO.removeObjectFromBothSidesOfRelationshipWithKey(eo, key);
	}
    
	/**
	 * Delegated from the base entity.
	 * 
	 * @param editingContext
	 *            this object's {@link EOEditingContext}
	 */
    public void awakeFromFetch(EOEditingContext editingContext) {
    	// DO NOTHING
    }
    
    /**
     * Delegated from the base entity.
     */
    public void delete() {
    	// DO NOTHING
    }

    /**
     * Delegated from the base entity.
     */
    public void mightDelete() {
    	// DO NOTHING
    }

	/**
	 * Delegated from the base entity.
	 * 
	 * @throws NSValidation.ValidationException
	 *             in the case of a validation failure
	 */
    public void willDelete() throws NSValidation.ValidationException {
    	// DO NOTHING
    }
    
	/**
	 * Delegated from the base entity.
	 * 
	 * @param ec
	 *            this object's {@link EOEditingContext}
	 */
    public void didDelete(EOEditingContext ec) {
    	// DO NOTHING
    }
    
    /**
     * Delegated from the base entity.
     */
    public void willInsert() {
    	// DO NOTHING
    }

    /**
     * Delegated from the base entity.
     */
    public void didInsert() {
    	// DO NOTHING
    }

    /**
     * Delegated from the base entity.
     */
    public void willUpdate() {
    	// DO NOTHING
    }

    /**
     * Delegated from the base entity.
     */
    public void didUpdate() {
    	// DO NOTHING
    }
    
    /**
     * Delegated from the base entity.
     */
    public void willRevert() {
    	// DO NOTHING
    }

    /**
     * Delegated from the base entity.
	 * 
	 * @param ec
	 *            this object's {@link EOEditingContext}
     */
    public void didRevert(EOEditingContext ec) {
    	// DO NOTHING
    }

    /**
     * Delegated from the base entity.
	 * 
	 * @throws NSValidation.ValidationException
	 *             in the case of a validation failure
     */
	public void validateForSave() throws NSValidation.ValidationException {
		// DO NOTHING
	}

    /**
     * Delegated from the base entity.
	 * 
	 * @throws NSValidation.ValidationException
	 *             in the case of a validation failure
     */
    public void validateForInsert() throws NSValidation.ValidationException {
    	// DO NOTHING
    }
    
    /**
     * Delegated from the base entity.
	 * 
	 * @throws NSValidation.ValidationException
	 *             in the case of a validation failure
     */
    public void validateForUpdate() throws NSValidation.ValidationException {
    	// DO NOTHING
    }

	/**
	 * Delegated from the base entity.
	 * 
	 * @param value
	 *            a value
	 * @param keyPath
	 *            a keypath
	 * @return the destination object
	 * @throws ValidationException
	 *             in the case of a validation failure
	 */
	public Object validateTakeValueForKeyPath(Object value, String keyPath) throws ValidationException {
		return com.webobjects.foundation.NSValidation.DefaultImplementation.validateTakeValueForKeyPath(this, value, keyPath);
	}
    
	/**
	 * Delegated from the base entity.
	 * 
	 * @param value
	 *            a value
	 * @param key
	 *            a key
	 * @return the destination object
	 * @throws NSValidation.ValidationException
	 *             in the case of a validation failure
	 */
	public Object validateValueForKey(Object value, String key) throws NSValidation.ValidationException {
		try {
			return com.webobjects.foundation.NSValidation.DefaultImplementation._validateValueForKey(this, value, key, EOEnterpriseObject._CLASS);
		}
		catch (com.webobjects.foundation.NSValidation.ValidationException exception) {
			throw exception.exceptionWithObjectAndKey(this, key);
		}
	}

	/**
	 * Delegated from the base entity. A partial entity can override this method
	 * to perform object initialisation. It will be called when the base
	 * entity's {@code init()} method is called.
	 * 
	 * @param editingContext
	 *            this object's {@link EOEditingContext}
	 */
	protected void init(EOEditingContext editingContext) {
	}
}
