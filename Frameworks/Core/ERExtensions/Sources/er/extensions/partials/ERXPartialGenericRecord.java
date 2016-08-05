package er.extensions.partials;

import java.util.Collection;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSValidation;
import com.webobjects.foundation._NSUtilities;

import er.extensions.eof.ERXEntityClassDescription;
import er.extensions.eof.ERXGenericRecord;

/**
 * <p>
 * For overview information on partials, read the {@code package.html} in
 * {@code er.extensions.partials}.
 * </p>
 * 
 * <p>
 * {@code ERXPartialGenericRecord} is the base class of any entity that allows
 * itself to be extended with partials.
 * </p>
 * 
 * @author mschrag
 */
public class ERXPartialGenericRecord extends ERXGenericRecord {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private NSDictionary<Class, ERXPartial> _partials;

	@SuppressWarnings("unchecked")
	public NSDictionary<Class, ERXPartial> _partialsDictionary() {
		if (_partials == null) {
			ERXEntityClassDescription cd = (ERXEntityClassDescription) classDescription();
			NSArray<Class<ERXPartial>> partialEntityClasses = cd.partialClasses();
			if (partialEntityClasses == null || partialEntityClasses.count() == 0) {
				_partials = NSMutableDictionary.EmptyDictionary;
			}
			else {
				NSMutableDictionary<Class, ERXPartial> partials = new NSMutableDictionary<Class, ERXPartial>();
				for (Class<ERXPartial> partialEntityClass : partialEntityClasses) {
					try {
						ERXPartial partial = partialEntityClass.newInstance();
						partial.setPrimaryEO(this);
						partials.setObjectForKey(partial, partialEntityClass);
					}
					catch (Exception e) {
						throw new RuntimeException("Failed to create the partial '" + partialEntityClass.getSimpleName() + "' for the EO '" + this + "'.", e);
					}
				}
				_partials = partials;
			}
		}
		return _partials;
	}

	public Collection<ERXPartial> _partials() {
		return _partialsDictionary().values();
	}

	/**
	 * Returns the ERXPartial partial implementation for the given partial type.
	 * 
	 * @param <U>
	 *            the partial type
	 * @param partialClass
	 *            the partial type
	 * @return an instance of the given partial associated with this EO
	 */
	@SuppressWarnings("unchecked")
	public <U extends ERXPartial> U partialForClass(Class<U> partialClass) {
		U partial = (U) _partialsDictionary().objectForKey(partialClass);
		if (partial == null) {
			throw new IllegalArgumentException("There is no partial '" + partialClass.getSimpleName() + "' for the EO '" + this + "'.");
		}
		return partial;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object valueForKey(String key) {
		if (key != null && key.charAt(0) == '@') {
			return partialForClass(_NSUtilities.classWithName(key.substring(1)));
		}
		return super.valueForKey(key);
	}

	@Override
	public void delete() {
		for (ERXPartial partial : _partials()) {
			partial.delete();
		}
		super.delete();
	}

	@Override
	public void mightDelete() {
		super.mightDelete();
		for (ERXPartial partial : _partials()) {
			partial.mightDelete();
		}
	}

	@Override
	public void willDelete() throws NSValidation.ValidationException {
		super.willDelete();
		for (ERXPartial partial : _partials()) {
			partial.willDelete();
		}
	}

	@Override
	public void willInsert() {
		super.willInsert();
		for (ERXPartial partial : _partials()) {
			partial.willInsert();
		}
	}

	@Override
	public void willUpdate() {
		super.willUpdate();
		for (ERXPartial partial : _partials()) {
			partial.willUpdate();
		}
	}

	@Override
	public void didDelete(EOEditingContext ec) {
		super.didDelete(ec);
		for (ERXPartial partial : _partials()) {
			partial.didDelete(ec);
		}
	}

	@Override
	public void didUpdate() {
		super.didUpdate();
		for (ERXPartial partial : _partials()) {
			partial.didUpdate();
		}
	}

	@Override
	public void didInsert() {
		super.didInsert();
		for (ERXPartial partial : _partials()) {
			partial.didInsert();
		}
	}

	@Override
	public void willRevert() {
		super.willRevert();
		for (ERXPartial partial : _partials()) {
			partial.willRevert();
		}
	}

	@Override
	public void didRevert(EOEditingContext ec) {
		super.didRevert(ec);
		for (ERXPartial partial : _partials()) {
			partial.didRevert(ec);
		}
	}

	@Override
	public void awakeFromInsertion(EOEditingContext editingContext) {
		super.awakeFromInsertion(editingContext);
		for (ERXPartial partial : _partials()) {
			partial.awakeFromInsertion(editingContext);
		}
	}

	@Override
	protected void init(EOEditingContext editingContext) {
		super.init(editingContext);
		// Call init() on all partial entities
		for (ERXPartial partial : _partials()) {
			partial.init(editingContext);
		}
	}

	@Override
	public void awakeFromFetch(EOEditingContext editingContext) {
		super.awakeFromFetch(editingContext);
		for (ERXPartial partial : _partials()) {
			partial.awakeFromFetch(editingContext);
		}
	}

	@Override
	protected Object _validateValueForKey(Object value, String key) throws ValidationException {
		Object result = value;
		for (ERXPartial partial : _partials()) {
			result = partial.validateValueForKey(result, key);
		}
		return result;
	}

	@Override
	public Object validateTakeValueForKeyPath(Object value, String keyPath) throws ValidationException {
		Object result = super.validateTakeValueForKeyPath(value, keyPath);
		for (ERXPartial partial : _partials()) {
			if (partial.isPartialKeypath(keyPath)) {
				result = partial.validateTakeValueForKeyPath(result, keyPath);
			}
		}
		return result;
	}

	@Override
	public void validateForSave() throws NSValidation.ValidationException {
		super.validateForSave();
		for (ERXPartial partial : _partials()) {
			partial.validateForSave();
		}
	}

	@Override
	public void validateForInsert() throws NSValidation.ValidationException {
		super.validateForInsert();
		for (ERXPartial partial : _partials()) {
			partial.validateForInsert();
		}
	}

	@Override
	public void validateForUpdate() throws NSValidation.ValidationException {
		super.validateForUpdate();
		for (ERXPartial partial : _partials()) {
			partial.validateForUpdate();
		}
	}
}
