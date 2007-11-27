/**
 * 
 */
package er.extensions.partials;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EORelationshipManipulation;
import com.webobjects.foundation.NSValidation;
import com.webobjects.foundation.NSValidation.ValidationException;

import er.extensions.ERXGenericRecord;

/**
 * <p>
 * For overview information on partials, read the package.html in er.extensions.partials.
 * </p>
 * 
 * <p>
 * ERXPartial is the superclass of all partial entity implementations.  ERXPartial is not
 * itself an EO, but is acts as a partial typesafe wrapper around an existing base EO (which must
 * extend ERXPartialGenericRecord).  For instance, the base entity might be Person, but the partial 
 * may be CalendarPerson which might have expose like calendarPerson.scheduledEvents().
 * </p>
 * 
 * <p>
 * To obtain a partial, you request an instance from a base EO.  Take the Person example from above.  You
 * can access the interface of the CalendarPerson partial in two ways:
 * </p>
 * 
 * <code>
 * Person person = ...;
 * CalendarPerson calendarPerson = person.partialForClass(CalendarPerson.class);
 * </code>
 * 
 * or
 * 
 * <code>
 * Person person = ...;
 * CalendarPerson calendarPerson = person.valueForKey("@CalendarPerson");
 * </code>
 * 
 * which allows easy use of the partial entities in component bindings, like "person.@CalendarPerson.scheduledEvents".
 * 
 * @author mschrag
 *
 * @param <T> the EO class that this is a partial of
 */
public class ERXPartial<T extends ERXGenericRecord> {
	private T _primaryEO;
	
	public void setPrimaryEO(T primaryEO) {
		_primaryEO = primaryEO;
	}
	
	public T primaryEO() {
		return _primaryEO;
	}
	
	/**
	 * Returns primaryEO.editingContext.
	 * 
	 * @return primaryEO.editingContext
	 */
	public EOEditingContext editingContext() {
		return _primaryEO.editingContext();
	}
	
	/**
	 * Returns primaryEO.storedValueForKey.
	 * 
	 * @return primaryEO.storedValueForKey
	 */
    public Object storedValueForKey(String key) {
    	return _primaryEO.storedValueForKey(key);
    }

	/**
	 * Calls primaryEO.takeStoredValueForKey.
	 */
    public void takeStoredValueForKey(Object value, String key) {
    	_primaryEO.takeStoredValueForKey(value, key);
    }

	/**
	 * Calls primaryEO.addObjectToBothSidesOfRelationshipWithKey.
	 */
    public void addObjectToBothSidesOfRelationshipWithKey(EORelationshipManipulation eo, String key) {
    	_primaryEO.addObjectToBothSidesOfRelationshipWithKey(eo, key);
    }

	/**
	 * Calls primaryEO.removeObjectFromBothSidesOfRelationshipWithKey.
	 */
    public void removeObjectFromBothSidesOfRelationshipWithKey(EORelationshipManipulation eo, String key) {
    	_primaryEO.removeObjectFromBothSidesOfRelationshipWithKey(eo, key);
    }
    
    /**
     * Delegated from the base entity.
     */
    public void awakeFromInsertion(EOEditingContext editingContext) {
    	// DO NOTHING
    }
    
    /**
     * Delegated from the base entity.
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
     */
    public void willDelete() throws NSValidation.ValidationException {
    	// DO NOTHING
    }
    
    /**
     * Delegated from the base entity.
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
     */
    public void didRevert(EOEditingContext ec) {
    	// DO NOTHING
    }

    /**
     * Delegated from the base entity.
     */
    public void validateForSave() throws NSValidation.ValidationException {
    	// DO NOTHING
    }

    /**
     * Delegated from the base entity.
     */
    public void validateForInsert() throws NSValidation.ValidationException {
    	// DO NOTHING
    }
    
    /**
     * Delegated from the base entity.
     */
    public void validateForUpdate() throws NSValidation.ValidationException {
    	// DO NOTHING
    }

    /**
     * Delegated from the base entity.
     */
    public Object validateTakeValueForKeyPath(Object value, String keyPath) throws ValidationException {
    	return com.webobjects.foundation.NSValidation.DefaultImplementation.validateTakeValueForKeyPath(this, value, keyPath);
    }
    
    /**
     * Delegated from the base entity.
     */
    public Object validateValueForKey(Object value, String key) throws NSValidation.ValidationException {
    	try {
    		return com.webobjects.foundation.NSValidation.DefaultImplementation._validateValueForKey(this, value, key, EOEnterpriseObject._CLASS);
    	}
    	catch (com.webobjects.foundation.NSValidation.ValidationException exception) {
    		throw exception.exceptionWithObjectAndKey(this, key);
    	}
    }
}