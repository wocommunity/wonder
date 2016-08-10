package com.webobjects.eocontrol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Enumeration;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSValidation;
import com.webobjects.foundation._NSReflectionUtilities;
import com.webobjects.foundation._NSStringUtilities;
import com.webobjects.foundation._NSUtilities;

public abstract class EOCustomObject implements EOEnterpriseObject, EODeferredFaulting,
		EOKeyValueCoding._KeyBindingCreation, NSKeyValueCoding._ReflectionKeyBindingCreation.Callback,
		EOKeyValueCoding._BestBindingCreation._ForwardingBindingCheck, _EOPrivateMemento {
	
	public static final Class<?> _CLASS = _NSUtilities._classWithFullySpecifiedName("com.webobjects.eocontrol.EOCustomObject");
	static final long serialVersionUID = 1L;
	private transient EOFaultHandler __faultHandler;
	private transient Object __unarchivedResultingEnterpriseObject;
	private transient int __hashCodeCache;
	transient EOClassDescription __classDescription;
	transient EOEditingContext __editingContext;
	transient EOGlobalID __gid;
	transient NSDictionary __originalSnapshot;
	transient NSDictionary __lastSnapshot;
	transient int __retainCount;
	protected transient Boolean __readOnly;
	transient int __flags;

	//Never used?
	private static final int kInitializedFlag = 0;
	private static final int kSharedFlag = 1;
	private static final int kAuxObserverFlag = 2;
	private static final int kPendingUpdatesFlag = 3;
	private static final int kPendingInsertionFlag = 4;
	private static final int kPendingDeletionFlag = 5;

	private static final int kInitializedMask = 1;
	private static final int kSharedMask = 2;
	private static final int kAuxObserverMask = 4;
	private static final int kPendingUpdatesMask = 8;
	private static final int kPendingInsertionMask = 16;
	private static final int kPendingDeletionMask = 32;
	private static final int kPendingChangesMask = 56;

	/*
	 * Updated serial version
	 */
	private static final long SerializationVersion = 5001L;
	private static final String SerializationVersionFieldKey = "version";
	private static final String SerializationEditingContextFieldKey = "editingContext";
	private static final String SerializationGlobalIDFieldKey = "globalID";
	private static final String SerializationPropertiesFieldKey = "properties";

	/*
	 * Added classDescription just after version
	 */
	private static final String SerializationClassDescriptionFieldKey = "classDescription";
	private static final ObjectStreamField[] serialPersistentFields = {
			new ObjectStreamField(SerializationVersionFieldKey, Long.TYPE),
			new ObjectStreamField(SerializationClassDescriptionFieldKey, EOClassDescription._CLASS),
			new ObjectStreamField(SerializationEditingContextFieldKey, EOEditingContext._CLASS),
			new ObjectStreamField(SerializationGlobalIDFieldKey, EOGlobalID._CLASS),
			new ObjectStreamField(SerializationPropertiesFieldKey, _NSUtilities._ObjectClass), };

	public final EOEditingContext __editingContext() {
		return __editingContext;
	}

	public final void __setEditingContext(EOEditingContext ec) {
		__editingContext = ec;
	}

	public final EOClassDescription __classDescription() {
		if (__classDescription == null) {
			__classDescription = EOClassDescription.classDescriptionForClass(getClass());
			__readOnly = null;
		}
		return __classDescription;
	}

	/*
	 * Added from EOGenericRecord to support deserialization of classDescription
	 */
	private void __setClassDescription() {
		EOClassDescription cd = EOClassDescription.classDescriptionForClass(getClass());
		if (cd == null) {
			throw new IllegalStateException("Unabled to find an EOClassDescription for objects of " + getClass());
		}
		__setClassDescription(cd);
	}

	public void __setClassDescription(EOClassDescription cd) {
		__classDescription = cd;
		__readOnly = null;
	}

	public final EOGlobalID __globalID() {
		return __gid;
	}

	public final void __setGlobalID(EOGlobalID gid) {
		__gid = gid;
	}

	public final NSDictionary __originalSnapshot() {
		return __originalSnapshot;
	}

	public final void __setOriginalSnapshot(NSDictionary os) {
		__originalSnapshot = os;
	}

	public final NSDictionary __lastSnapshot() {
		return __lastSnapshot;
	}

	public final void __setLastSnapshot(NSDictionary ls) {
		__lastSnapshot = ls;
	}

	public final int __retainCount() {
		return __retainCount;
	}

	public final void __setRetainCount(int rc) {
		__retainCount = rc;
	}

	public final boolean __isInitialized() {
		return (__flags & kInitializedMask) != 0;
	}

	public final void __setInitialized(boolean inited) {
		if (inited)
			__flags |= kInitializedMask;
		else
			__flags &= ~kInitializedMask;
	}

	public final boolean __isShared() {
		return (__flags & kSharedMask) != 0;
	}

	public final void __setShared(boolean shared) {
		if (shared)
			__flags |= kSharedMask;
		else
			__flags &= ~kSharedMask;
	}

	public final boolean __hasAuxillaryObservers() {
		return (__flags & kAuxObserverMask) != 0;
	}

	public final void __setAuxillaryObservers(boolean hao) {
		if (hao)
			__flags |= kAuxObserverMask;
		else
			__flags &= ~kAuxObserverMask;
	}

	public boolean __hasPendingChanges() {
		return (__flags & kPendingChangesMask) != 0;
	}

	public void __clearPendingChanges() {
		__flags &= ~kPendingChangesMask;
	}

	public boolean __hasPendingUpdate() {
		return (__flags & kPendingUpdatesMask) != 0;
	}

	public void __setPendingUpdate(boolean changed) {
		if (changed)
			__flags |= kPendingUpdatesMask;
		else
			__flags &= ~kPendingUpdatesMask;
	}

	public final boolean __isPendingInsertion() {
		return (__flags & kPendingInsertionMask) != 0;
	}

	public final void __setPendingInsertion(boolean inserted) {
		if (inserted)
			__flags |= kPendingInsertionMask;
		else
			__flags &= ~kPendingInsertionMask;
	}

	public final boolean __isPendingDeletion() {
		return (__flags & kPendingDeletionMask) != 0;
	}

	public final void __setPendingDeletion(boolean deleted) {
		if (deleted)
			__flags |= kPendingDeletionMask;
		else
			__flags &= ~kPendingDeletionMask;
	}

	@Deprecated
	public EOCustomObject(EOEditingContext editingContext, EOClassDescription classDescription, EOGlobalID gid) {
		this();
	}

	public EOCustomObject() {
		__readOnly = null;
	}

	public final Object opaqueState() {
		return this;
	}

	@Override
	public final boolean equals(Object other) {
		return this == other;
	}

	@Override
	public int hashCode() {
		if (__hashCodeCache == 0) {
			__hashCodeCache = super.hashCode();
		}
		return __hashCodeCache;
	}

	@Override
	public String toString() {
		return eoDescription();
	}

	public EOEditingContext editingContext() {
		return __editingContext();
	}

	public void willChange() {
		willRead();
		EOObserverCenter.notifyObserversObjectWillChange(this);
	}

	public EOClassDescription classDescription() {
		return __classDescription();
	}

	public String entityName() {
		EOClassDescription cd = classDescription();
		return cd != null ? cd.entityName() : null;
	}

	public NSArray<String> attributeKeys() {
		EOClassDescription cd = classDescription();
		if (cd != null) {
			return cd.attributeKeys();
		}
		return NSArray.emptyArray();
	}

	public NSArray<String> toOneRelationshipKeys() {
		EOClassDescription cd = classDescription();
		if (cd != null) {
			return cd.toOneRelationshipKeys();
		}
		return NSArray.emptyArray();
	}

	public NSArray<String> toManyRelationshipKeys() {
		EOClassDescription cd = classDescription();
		if (cd != null) {
			return cd.toManyRelationshipKeys();
		}
		return NSArray.emptyArray();
	}

	public String inverseForRelationshipKey(String relationshipKey) {
		EOClassDescription cd = classDescription();
		return cd != null ? cd.inverseForRelationshipKey(relationshipKey) : null;
	}

	public int deleteRuleForRelationshipKey(String relationshipKey) {
		EOClassDescription cd = classDescription();
		return cd != null ? cd.deleteRuleForRelationshipKey(relationshipKey) : 0;
	}

	public boolean ownsDestinationObjectsForRelationshipKey(String relationshipKey) {
		EOClassDescription cd = classDescription();
		return cd != null ? cd.ownsDestinationObjectsForRelationshipKey(relationshipKey) : false;
	}

	public EOClassDescription classDescriptionForDestinationKey(String detailKey) {
		EOClassDescription cd = classDescription();
		return cd != null ? cd.classDescriptionForDestinationKey(detailKey) : null;
	}

	public void awakeFromInsertion(EOEditingContext ec) {
		EOClassDescription cd = classDescription();
		if (cd != null) {
			cd.awakeObjectFromInsertion(this, ec);
		}
		__readOnly = null;
	}

	public void awakeFromFetch(EOEditingContext ec) {
		EOClassDescription cd = classDescription();
		if (cd != null) {
			cd.awakeObjectFromFetch(this, ec);
		}
		__readOnly = null;
	}

	public NSDictionary<String, Object> snapshot() {
		NSArray<String> attributeKeys = attributeKeys();
		NSArray<String> toOneRelationshipKeys = toOneRelationshipKeys();
		NSArray<String> toManyRelationshipKeys = toManyRelationshipKeys();
		int akCount = attributeKeys.count();
		int tokCount = toOneRelationshipKeys.count();
		int tmkCount = toManyRelationshipKeys.count();
		NSMutableDictionary<String, Object> snapshot = new NSMutableDictionary<String, Object>(akCount + tokCount + tmkCount);

		while (akCount-- > 0) {
			String key = attributeKeys.objectAtIndex(akCount);
			Object value = storedValueForKey(key);
			if (value != null) {
				snapshot.setObjectForKey(value, key);
				continue;
			}
			snapshot.setObjectForKey(NSKeyValueCoding.NullValue, key);
		}

		while (tokCount-- > 0) {
			String key = toOneRelationshipKeys.objectAtIndex(tokCount);
			Object value = storedValueForKey(key);
			if (value != null) {
				snapshot.setObjectForKey(value, key);
				continue;
			}
			snapshot.setObjectForKey(NSKeyValueCoding.NullValue, key);
		}

		while (tmkCount-- > 0) {
			String key = toManyRelationshipKeys.objectAtIndex(tmkCount);
			NSArray toManyValue = (NSArray) storedValueForKey(key);
			if (toManyValue != null) {
				NSArray snap = toManyValue.immutableClone();
				snapshot.setObjectForKey(snap, key);
			} else {
				snapshot.setObjectForKey(NSKeyValueCoding.NullValue, key);
			}
		}

		return snapshot;
	}

	public void updateFromSnapshot(NSDictionary<String, Object> snapshot) {
		Enumeration<String> state = snapshot.keyEnumerator();
		while (state.hasMoreElements()) {
			String key = state.nextElement();
			Object value = snapshot.objectForKey(key);
			if (value == NSKeyValueCoding.NullValue) {
				value = null;
			} else if ((value instanceof NSArray)) {
				NSArray arrayValue = (NSArray) value;
				if (EOFaultHandler.isFault(arrayValue)) {
					EOFaultHandler handler = ((EOFaulting) arrayValue).faultHandler();
					value = handler._mutableCloneForArray(arrayValue);
				} else {
					value = new _EOCheapCopyMutableArray(arrayValue);
				}

				if (!EOFaultHandler.isFault(value)) {
					NSArray storedValue = (NSArray) storedValueForKey(key);
					if ((storedValue != null) && (EOFaultHandler.isFault(storedValue))) {
						((EOFaulting) storedValue).willRead();
					}
				}
			}
			takeStoredValueForKey(value, key);
		}
	}

	public NSDictionary changesFromSnapshot(NSDictionary snapshot) {
		NSArray uncommittedChanges = editingContext()._newUncommittedChangesForObject(this, snapshot);

		if (uncommittedChanges == null) {
			return NSDictionary.EmptyDictionary;
		}
		int max = uncommittedChanges.count();
		NSMutableDictionary result = new NSMutableDictionary(max / 2);
		for (int i = 0; i < max; i += 2) {
			result.setObjectForKey(uncommittedChanges.objectAtIndex(i + 1), uncommittedChanges.objectAtIndex(i));
		}
		return result;
	}

	public void reapplyChangesFromDictionary(NSDictionary changes) {
		Enumeration enumerator = changes.keyEnumerator();

		while (enumerator.hasMoreElements()) {
			String key = (String) enumerator.nextElement();
			EOEditingContext._mergeValueForKey(this, changes.objectForKey(key), key);
		}
	}

	public boolean isToManyKey(String key) {
		return toManyRelationshipKeys().containsObject(key);
	}

	public NSArray<String> allPropertyKeys() {
		NSArray<String> attributeKeys = attributeKeys();
		NSArray<String> toOneRelationshipKeys = toOneRelationshipKeys();
		NSArray<String> toManyRelationshipKeys = toManyRelationshipKeys();
		int attCount = attributeKeys.count();
		int toOneCount = toOneRelationshipKeys.count();
		int toManyCount = toManyRelationshipKeys.count();

		NSMutableArray<String> result = new NSMutableArray<String>(attCount + toOneCount + toManyCount);

		if (attCount > 0) {
			result.addObjectsFromArray(attributeKeys);
		}
		if (toOneCount > 0) {
			result.addObjectsFromArray(toOneRelationshipKeys);
		}
		if (toManyCount > 0) {
			result.addObjectsFromArray(toManyRelationshipKeys);
		}
		return result;
	}

	public void clearProperties() {
		NSArray<String> props = toOneRelationshipKeys();
		for (int i = 0, c = props.count(); i < c; i++) {
			takeStoredValueForKey(null, props.objectAtIndex(i));
		}
		props = toManyRelationshipKeys();
		for (int i = 0, c = props.count(); i < c; i++) {
			takeStoredValueForKey(null, props.objectAtIndex(i));
		}
		__clearPendingChanges();
	}

	public void propagateDeleteWithEditingContext(EOEditingContext ec) {
		EOClassDescription cd = classDescription();
		if (cd != null)
			cd.propagateDeleteForObject(this, ec);
	}

	public String userPresentableDescription() {
		EOClassDescription cd = classDescription();
		if (cd != null) {
			return cd.userPresentableDescriptionForObject(this);
		}
		return null;
	}

	public String eoShallowDescription() {
		String globalIDString = "[Gid Not Found]";
		EOEditingContext editingContext = editingContext();
		if (editingContext != null) {
			EOGlobalID gid = editingContext.globalIDForObject(this);
			if (gid != null)
				globalIDString = gid.toString();
		}
		String className = getClass().getName();
		String identifyString = Integer.toHexString(System.identityHashCode(this));
		if (isFault()) {
			String handlerClassName = faultHandler().getClass().getName();
			return "<" + className + " " + identifyString + " (" + handlerClassName + " " + globalIDString + ")>";
		}
		return "<" + className + " " + identifyString + " " + globalIDString + ">";
	}

	public String eoDescription() {
		if (isFault()) {
			return faultHandler().descriptionForObject(this);
		}

		NSMutableDictionary tmpDict = new NSMutableDictionary();
		NSMutableDictionary valueDict = new NSMutableDictionary();
		tmpDict.setObjectForKey(valueDict, "values");

		tmpDict.setObjectForKey(eoShallowDescription(), "this");

		NSDictionary values = valuesForKeys(attributeKeys());
		if (values.count() != 0) {
			valueDict.addEntriesFromDictionary(values);
		}

		NSArray<String> toOnes = toOneRelationshipKeys();

		int c = toOnes.count();
		if (c != 0) {
			for (int i = 0; i < c; i++) {
				String key = toOnes.objectAtIndex(i);
				EOEnterpriseObject value = (EOEnterpriseObject) valueForKey(key);

				if (value == null)
					valueDict.setObjectForKey("null", key);
				else {
					valueDict.setObjectForKey(value.eoShallowDescription(), key);
				}

			}

		}

		NSArray<String> toMany = toManyRelationshipKeys();

		if (toMany.count() != 0) {
			for (int i = 0; i < c; i++) {
				String key = toMany.objectAtIndex(i);
				NSArray array = (NSArray) valueForKey(key);

				if (array == null) {
					valueDict.setObjectForKey("null", key);
				} else if (EOFaultHandler.isFault(array)) {
					valueDict.setObjectForKey(EOFaultHandler.eoShallowDescription(array), key);
				} else {
					int count = array.count();
					NSMutableArray descriptions = new NSMutableArray(count);

					for (int j = 0; j < count; j++) {
						descriptions.addObject(((EOEnterpriseObject) array.objectAtIndex(j)).eoShallowDescription());
					}

					valueDict.setObjectForKey(descriptions, key);
				}
			}

		}

		String string = tmpDict.toString();
		return string;
	}

	public Object invokeRemoteMethod(String methodName, Class[] argumentTypes, Object[] arguments) {
		EOEditingContext context = editingContext();
		return context.invokeRemoteMethod(context, context.globalIDForObject(this), methodName, argumentTypes,
				arguments);
	}

	public void prepareValuesForClient() {
	}

	public void awakeFromClientUpdate(EOEditingContext ec) {
	}

	private Method _valueManipulationMethod(Class cls, String methodName, Object value) {
		Class[] parameterTypes = { value != null ? value.getClass() : _NSUtilities._ObjectClass };
		Method result = null;

		while ((result == null) && (parameterTypes[0] != null)) {
			result = _NSReflectionUtilities._methodForClass(cls, methodName, parameterTypes, true);
			if (result == null) {
				parameterTypes[0] = parameterTypes[0].getSuperclass();
			}
		}

		if ((result == null) && ((value == null) || ((value instanceof _EOPrivateMemento)))) {
			parameterTypes[0] = EOEnterpriseObject._CLASS;
			result = _NSReflectionUtilities._methodForClass(cls, methodName, parameterTypes, true);
		}

		return result;
	}

	private Method _valueManipulationMethodWithPrefix(String prefix, String key, Object value) {
		String methodName = prefix + _NSStringUtilities.capitalizedString(key);
		Class cls = getClass();
		Method result = null;

		result = _NSReflectionUtilities._methodForClass(cls, methodName,
				new Class[] { _NSReflectionUtilities._inferredValueClassForKey(cls, key, false) }, true);
		if (result == null) {
			result = _valueManipulationMethod(cls, methodName, value);
		}
		return result;
	}

	protected void includeObjectIntoPropertyWithKey(Object eo, String key) {
		if (eo != null) {
			Object value = valueForKey(key);

			if ((value instanceof NSMutableArray)) {
				NSMutableArray mutableValue = (NSMutableArray) value;

				if (!mutableValue.containsObject(eo)) {
					willChange();
					mutableValue.addObject(eo);
				}
			} else if ((value instanceof NSArray)) {
				NSArray immutableValue = (NSArray) value;
				if (!immutableValue.containsObject(eo)) {
					NSArray newArray = immutableValue.arrayByAddingObject(eo);
					takeValueForKey(newArray, key);
				}
			} else if (value == null) {
				NSMutableArray newArray = new NSMutableArray(eo);
				takeValueForKey(newArray, key);
			} else {
				throw new IllegalArgumentException("addObjectToPropertyWithKey: the key " + key
						+ " is not null, an NSArray or one of its subclasses - unable to add the value.");
			}
		}
	}

	public void addObjectToPropertyWithKey(Object eo, String key) {
		if (eo != null) {
			Method method = _valueManipulationMethodWithPrefix("addTo", key, eo);

			if (method != null)
				NSSelector._safeInvokeMethod(method, this, new Object[] { eo });
			else
				includeObjectIntoPropertyWithKey(eo, key);
		}
	}

	protected void excludeObjectFromPropertyWithKey(Object eo, String key) {
		if (eo != null) {
			Object value = valueForKey(key);

			if ((value instanceof NSMutableArray)) {
				NSMutableArray mutableValue = (NSMutableArray) value;

				if (mutableValue.containsObject(eo)) {
					willChange();
					mutableValue.removeObject(eo);
				}
			} else if ((value instanceof NSArray)) {
				NSArray immutableValue = (NSArray) value;
				if (immutableValue.containsObject(eo)) {
					NSMutableArray newArray = new NSMutableArray(immutableValue);

					newArray.removeObject(eo);
					takeValueForKey(newArray, key);
				}
			} else if (value != null) {
				throw new IllegalArgumentException("removeObjectToPropertyWithKey: the key " + key
						+ " is not a Array or one of its subclasses - unable to remove the value.");
			}
		}
	}

	public void removeObjectFromPropertyWithKey(Object eo, String key) {
		if (eo != null) {
			Method method = _valueManipulationMethodWithPrefix("removeFrom", key, eo);

			if (method != null)
				NSSelector._safeInvokeMethod(method, this, new Object[] { eo });
			else
				excludeObjectFromPropertyWithKey(eo, key);
		}
	}

	public void addObjectToBothSidesOfRelationshipWithKey(EORelationshipManipulation object, String key) {
		if (object != null) {
			String reciprocal = inverseForRelationshipKey(key);
			EOEnterpriseObject eo = (EOEnterpriseObject) object;

			if (isToManyKey(key)) {
				addObjectToPropertyWithKey(eo, key);

				if (reciprocal != null)
					if (eo.isToManyKey(reciprocal)) {
						eo.addObjectToPropertyWithKey(this, reciprocal);
					} else {
						EOEnterpriseObject other = (EOEnterpriseObject) eo.valueForKey(reciprocal);

						if (other != this) {
							if (other != null) {
								other.removeObjectFromPropertyWithKey(eo, key);
							}
							eo.takeValueForKey(this, reciprocal);
						}
					}
			} else {
				if (reciprocal != null) {
					EOEnterpriseObject other = (EOEnterpriseObject) valueForKey(key);

					if (eo.isToManyKey(reciprocal)) {
						if (other != null) {
							other.removeObjectFromPropertyWithKey(this, reciprocal);
						}

						eo.addObjectToPropertyWithKey(this, reciprocal);
					} else {
						if (other != null) {
							other.takeValueForKey(null, reciprocal);
						}

						eo.takeValueForKey(this, reciprocal);
					}
				}
				takeValueForKey(eo, key);
			}
		}
	}

	public void removeObjectFromBothSidesOfRelationshipWithKey(EORelationshipManipulation object, String key) {
		if (object != null) {
			String reciprocal = inverseForRelationshipKey(key);
			EOEnterpriseObject eo = (EOEnterpriseObject) object;

			if (isToManyKey(key)) {
				removeObjectFromPropertyWithKey(eo, key);

				if (reciprocal != null) {
					if (eo.isToManyKey(reciprocal))
						eo.removeObjectFromPropertyWithKey(this, reciprocal);
					else
						eo.takeValueForKey(null, reciprocal);
				}
			} else {
				EOEnterpriseObject other = (EOEnterpriseObject) valueForKey(key);

				if ((other == eo) && (reciprocal != null)) {
					if (eo.isToManyKey(reciprocal))
						other.removeObjectFromPropertyWithKey(this, reciprocal);
					else {
						other.takeValueForKey(null, reciprocal);
					}
				}
				takeValueForKey(null, key);
			}
		}
	}

	public static boolean usesDeferredFaultCreation() {
		return false;
	}

	public void willRead() {
		if (!__isShared()) {
			if (__faultHandler == null)
				return;
		} else {
			synchronized (this) {
				if (__faultHandler == null) {
					return;
				}
			}
		}

		EOEditingContext ec = editingContext();
		if (ec == null) {
			if ((__faultHandler instanceof EOEditingContext._EOInvalidFaultHandler)) {
				__faultHandler.completeInitializationOfObject(this);
			}
			throw new IllegalStateException(
					"Attempt to access an EO that has either not been inserted into any EOEditingContext or its EOEditingContext has already been disposed");
		}

		ec.lockObjectStore();
		try {
			synchronized (this) {
				if (__faultHandler == null) {
					ec.unlockObjectStore();
					return;
				}
			}
			__faultHandler.completeInitializationOfObject(this);
		} finally {
			ec.unlockObjectStore();
		}
	}

	public boolean isFault() {
		if (!__isShared()) {
			return __faultHandler != null;
		}
		synchronized (this) {
			return __faultHandler != null;
		}
	}

	public void clearFault() {
		if (!__isShared())
			__faultHandler = null;
		else
			synchronized (this) {
				__faultHandler = null;
			}
	}

	public void turnIntoFault(EOFaultHandler handler) {
		if (!__isShared())
			__faultHandler = handler;
		else {
			synchronized (this) {
				__faultHandler = handler;
			}
		}
		__readOnly = null;
	}

	public EOFaultHandler faultHandler() {
		if (!__isShared()) {
			return __faultHandler;
		}
		synchronized (this) {
			return __faultHandler;
		}
	}

	public Object willReadRelationship(Object object) {
		if (!__isShared()) {
			if (!EOFaultHandler.isFault(object))
				return object;
		} else {
			synchronized (this) {
				if (!EOFaultHandler.isFault(object)) {
					return object;
				}
			}
		}

		EOEditingContext ec = editingContext();

		ec.lockObjectStore();
		Object value;
		try {
			EOFaultHandler handler;
			synchronized (this) {
				if (!EOFaultHandler.isFault(object)) {
					Object localObject2 = object;

					ec.unlockObjectStore();
					return localObject2;
				}
				handler = ((EOFaulting) object).faultHandler();
			}

			value = handler.createFaultForDeferredFault(object, this);
		} finally {
			ec.unlockObjectStore();
		}

		return value;
	}

	public Object validateValueForKey(Object value, String key) throws NSValidation.ValidationException {
		EOClassDescription cd = classDescription();
		try {
			Object validatedValue = cd != null ? cd.validateValueForKey(value, key) : value;
			return NSValidation.DefaultImplementation._validateValueForKey(this, validatedValue, key,
					EOEnterpriseObject._CLASS);
		} catch (NSValidation.ValidationException exception) {
			throw exception.exceptionWithObjectAndKey(this, key);
		}
	}

	public Object validateTakeValueForKeyPath(Object value, String keyPath) throws NSValidation.ValidationException {
		return NSValidation.DefaultImplementation.validateTakeValueForKeyPath(this, value, keyPath);
	}

	public void validateForSave() throws NSValidation.ValidationException {
		NSValidation.ValidationException firstException = null;
		NSMutableArray<ValidationException> additionalExceptions = null;

		EOClassDescription cd = classDescription();
		if (cd != null) {
			try {
				cd.validateObjectForSave(this);
			} catch (NSValidation.ValidationException e) {
				firstException = e;
			}
		}

		for (int type = 0; type < 3; type++) {
			NSArray<String> keyEnumerator;
			switch (type) {
			case 0:
				keyEnumerator = attributeKeys();
				break;
			case 1:
				keyEnumerator = toOneRelationshipKeys();
				break;
			default:
				keyEnumerator = toManyRelationshipKeys();
			}

			int i = 0;
			for (int c = keyEnumerator.count(); i < c; i++) {
				String key = keyEnumerator.objectAtIndex(i);
				Object currentValue = valueForKey(key);
				try {
					Object newValue = validateValueForKey(currentValue, key);
					if (newValue != currentValue)
						takeStoredValueForKey(newValue, key);
				} catch (NSValidation.ValidationException exception) {
					if (firstException != null) {
						if (additionalExceptions == null) {
							additionalExceptions = new NSMutableArray<ValidationException>(firstException);
						}
						additionalExceptions.addObject(exception);
					} else {
						firstException = exception;
					}
				}
			}
		}

		if (additionalExceptions != null)
			throw NSValidation.ValidationException.aggregateExceptionWithExceptions(additionalExceptions);
		if (firstException != null)
			throw firstException;
	}

	public void validateForDelete() throws NSValidation.ValidationException {
		EOClassDescription cd = classDescription();
		if (cd != null)
			cd.validateObjectForDelete(this);
	}

	public void validateForInsert() throws NSValidation.ValidationException {
		validateForSave();
	}

	public void validateForUpdate() throws NSValidation.ValidationException {
		validateForSave();
	}

	public void validateClientUpdate() throws NSValidation.ValidationException {
	}

	public static boolean canAccessFieldsDirectly() {
		return true;
	}

	public Object valueForKey(String key) {
		if (key == null) {
			return null;
		}
		NSKeyValueCoding._KeyBinding binding = _keyGetBindingForKey(key);
		return binding.valueInObject(this);
	}

	public void takeValueForKey(Object value, String key) {
		if (key == null) {
			throw new IllegalArgumentException("Key cannot be null");
		}
		NSKeyValueCoding._KeyBinding binding = _keySetBindingForKey(key);
		binding.setValueInObject(value, this);
	}

	public Object handleQueryWithUnboundKey(String key) {
		return NSKeyValueCoding.DefaultImplementation.handleQueryWithUnboundKey(this, key);
	}

	public void handleTakeValueForUnboundKey(Object value, String key) {
		NSKeyValueCoding.DefaultImplementation.handleTakeValueForUnboundKey(this, value, key);
	}

	public void unableToSetNullForKey(String key) {
		NSKeyValueCoding.DefaultImplementation.unableToSetNullForKey(this, key);
	}

	public Object valueForKeyPath(String keyPath) {
		return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, keyPath);
	}

	public void takeValueForKeyPath(Object value, String keyPath) {
		NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(this, value, keyPath);
	}

	public static boolean shouldUseStoredAccessors() {
		return true;
	}

	public Object storedValueForKey(String key) {
		if (key == null) {
			return null;
		}
		NSKeyValueCoding._KeyBinding binding = _storedKeyGetBindingForKey(key);
		return binding.valueInObject(this);
	}

	public void takeStoredValueForKey(Object value, String key) {
		if (key == null) {
			throw new IllegalArgumentException("Key cannot be null");
		}
		NSKeyValueCoding._KeyBinding binding = _storedKeySetBindingForKey(key);
		binding.setValueInObject(value, this);
	}

	public NSDictionary valuesForKeys(NSArray keys) {
		return EOKeyValueCodingAdditions.DefaultImplementation.valuesForKeys(this, keys);
	}

	public NSDictionary valuesForKeysWithMapping(NSDictionary mapping) {
		return EOKeyValueCodingAdditions.DefaultImplementation.valuesForKeysWithMapping(this, mapping);
	}

	public void takeValuesFromDictionary(NSDictionary dictionary) {
		EOKeyValueCodingAdditions.DefaultImplementation.takeValuesFromDictionary(this, dictionary);
	}

	public void takeValuesFromDictionaryWithMapping(NSDictionary dictionary, NSDictionary mapping) {
		EOKeyValueCodingAdditions.DefaultImplementation.takeValuesFromDictionaryWithMapping(this, dictionary, mapping);
	}

	public NSKeyValueCoding._KeyBinding _createKeyGetBindingForKey(String key) {
		return NSKeyValueCoding.DefaultImplementation._createKeyGetBindingForKey(this, key);
	}

	public NSKeyValueCoding._KeyBinding _createKeySetBindingForKey(String key) {
		return NSKeyValueCoding.DefaultImplementation._createKeySetBindingForKey(this, key);
	}

	public NSKeyValueCoding._KeyBinding _keyGetBindingForKey(String key) {
		return NSKeyValueCoding.DefaultImplementation._keyGetBindingForKey(this, key);
	}

	public NSKeyValueCoding._KeyBinding _keySetBindingForKey(String key) {
		return NSKeyValueCoding.DefaultImplementation._keySetBindingForKey(this, key);
	}

	public NSKeyValueCoding._KeyBinding _createStoredKeyGetBindingForKey(String key) {
		return EOKeyValueCoding.DefaultImplementation._createStoredKeyGetBindingForKey(this, key);
	}

	public NSKeyValueCoding._KeyBinding _createStoredKeySetBindingForKey(String key) {
		return EOKeyValueCoding.DefaultImplementation._createStoredKeySetBindingForKey(this, key);
	}

	public NSKeyValueCoding._KeyBinding _storedKeyGetBindingForKey(String key) {
		return EOKeyValueCoding.DefaultImplementation._storedKeyGetBindingForKey(this, key);
	}

	public NSKeyValueCoding._KeyBinding _storedKeySetBindingForKey(String key) {
		return EOKeyValueCoding.DefaultImplementation._storedKeySetBindingForKey(this, key);
	}

	boolean _usesDeferredFaultCreationForClass(Class objectClass) {
		return _NSReflectionUtilities._staticBooleanMethodValue("usesDeferredFaultCreation", null, null, objectClass,
				EODeferredFaulting._CLASS, false);
	}

	public NSKeyValueCoding._KeyBinding _fieldKeyBinding(String key, String fieldName) {
		Class<?> objectClass = getClass();
		NSKeyValueCoding.ValueAccessor valueAccessor = NSKeyValueCoding.ValueAccessor
				._valueAccessorForClass(objectClass);
		boolean publicFieldOnly = valueAccessor == null;

		Field field = _NSReflectionUtilities._fieldForClass(objectClass, fieldName, publicFieldOnly);
		if (field != null) {
			if ((!field.getType().isPrimitive()) && (_usesDeferredFaultCreationForClass(getClass()))) {
				EOClassDescription classDescription = classDescription();
				if ((classDescription != null) && (!classDescription.attributeKeys().containsObject(key))) {
					return new _LazyFieldBinding(objectClass, key, field, valueAccessor);
				}
			}

			Class<?> valueClass = _NSUtilities.classObjectForClass(field.getType());
			if (Number.class.isAssignableFrom(valueClass))
				return new _NumberFieldBinding(objectClass, key, field, valueClass, valueAccessor);
			if (Boolean.class.isAssignableFrom(valueClass)) {
				return new _BooleanFieldBinding(objectClass, key, field, valueAccessor);
			}
			return new _FieldBinding(objectClass, key, field, valueAccessor);
		}
		return null;
	}

	public NSKeyValueCoding._KeyBinding _methodKeyGetBinding(String key, String methodName) {
		return NSKeyValueCoding._ReflectionKeyBindingCreation._methodKeyGetBinding(this, key, methodName);
	}

	public NSKeyValueCoding._KeyBinding _methodKeySetBinding(String key, String methodName) {
		return NSKeyValueCoding._ReflectionKeyBindingCreation._methodKeySetBinding(this, key, methodName);
	}

	public NSKeyValueCoding._KeyBinding _otherStorageBinding(String key) {
		return null;
	}

	public boolean _forwardingBindingNeededForClass(Class objectClass, String methodName, Class[] methodArgumentTypes) {
		return EOKeyValueCoding._BestBindingCreation._isMethodOverriddenInSubclass(_CLASS, objectClass, methodName,
				methodArgumentTypes);
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		EOEditingContext editingContext = null;
		EOGlobalID globalID = null;
		ObjectOutputStream.PutField fields = out.putFields();

		fields.put(SerializationVersionFieldKey, SerializationVersion);
		fields.put(SerializationClassDescriptionFieldKey, classDescription());

		editingContext = EOEditingContext.usesContextRelativeEncoding() ? editingContext() : null;
		if (editingContext != null) {
			editingContext.lock();
			try {
				if (editingContext._willObjectBeForgottenNextPRC(this))
					editingContext = null;
			} finally {
				if (editingContext != null) {
					editingContext.unlock();
				}
			}
		}
		if (editingContext != null) {
			fields.put(SerializationEditingContextFieldKey, editingContext);
			globalID = editingContext.globalIDForObject(this);
			fields.put(SerializationGlobalIDFieldKey, globalID);
		} else if (isFault()) {
			globalID = __globalID();
			fields.put(SerializationGlobalIDFieldKey, globalID);
		} else {
			fields.put(SerializationPropertiesFieldKey, EOEditingContext._valuesForObject(this));
		}

		out.writeFields();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		ObjectInputStream.GetField fields = in.readFields();

		EOEditingContext editingContext = null;
		EOGlobalID gid = null;
		fields.get(SerializationVersionFieldKey, 0L);

		EOClassDescription cd = (EOClassDescription) fields.get(SerializationClassDescriptionFieldKey, null);
		if (cd == null) {
			__setClassDescription();
		} else {
			__setClassDescription(cd);
		}

		editingContext = (EOEditingContext) fields.get(SerializationEditingContextFieldKey, null);

		__unarchivedResultingEnterpriseObject = this;
		if (editingContext == null) {
			NSArray values = (NSArray) fields.get(SerializationPropertiesFieldKey, null);
			if (values != null) {
				EOEditingContext._applyValuesToObject(values, this);
			} else {
				gid = (EOGlobalID) fields.get(SerializationGlobalIDFieldKey, null);
				__setGlobalID(gid);
				turnIntoFault(EOEditingContext._InvalidEOHandler);
			}
		} else {
			gid = (EOGlobalID) fields.get(SerializationGlobalIDFieldKey, null);
			if (gid == null) {
				throw new StreamCorruptedException("While unarchiving a \"" + getClass().getName()
						+ "\" a valid EOGlobalID was missing from the stream.");
			}
			Object other = editingContext.objectForGlobalID(gid);
			if (other != null) {
				__unarchivedResultingEnterpriseObject = other;
			} else if (gid.isTemporary())
				editingContext.recordObject(this, gid);
			else
				__unarchivedResultingEnterpriseObject = editingContext.faultForGlobalID(gid, editingContext);
		}
	}

	protected Object readResolve() throws ObjectStreamException {
		Object temp = __unarchivedResultingEnterpriseObject;
		__unarchivedResultingEnterpriseObject = null;
		return temp;
	}

	public boolean isReadOnly() {
		if (__readOnly == null) {
			EOClassDescription aDescription = classDescription();
			if (aDescription != null)
				__readOnly = Boolean.valueOf(aDescription.isEntityReadOnly());
			else {
				__readOnly = Boolean.FALSE;
			}
		}
		return __readOnly != null ? __readOnly.booleanValue() : false;
	}

	public static class _LazyFieldBinding extends EOCustomObject._FieldBinding {
		public _LazyFieldBinding(Class targetClass, String key, Field field,
				NSKeyValueCoding.ValueAccessor valueAccessor) {
			super(targetClass, key, field, valueAccessor);
		}

		@Override
		public Object valueInObject(Object object) {
			Object value = super.valueInObject(object);
			return value != null ? ((EOEnterpriseObject) object).willReadRelationship(value) : null;
		}
	}

	public static class _BooleanFieldBinding extends NSKeyValueCoding._BooleanFieldBinding {
		public _BooleanFieldBinding(Class targetClass, String key, Field field,
				NSKeyValueCoding.ValueAccessor valueAccessor) {
			super(targetClass, key, field, valueAccessor);
		}

		@Override
		public Object valueInObject(Object object) {
			((EOEnterpriseObject) object).willRead();
			return super.valueInObject(object);
		}

		@Override
		protected void _setValidatedValueInObject(Object value, Object object) throws IllegalAccessException {
			((EOEnterpriseObject) object).willChange();
			super._setValidatedValueInObject(value, object);
		}
	}

	public static class _NumberFieldBinding extends NSKeyValueCoding._NumberFieldBinding {
		public _NumberFieldBinding(Class targetClass, String key, Field field, Class valueClass,
				NSKeyValueCoding.ValueAccessor valueAccessor) {
			super(targetClass, key, field, valueClass, valueAccessor);
		}

		@Override
		public Object valueInObject(Object object) {
			((EOEnterpriseObject) object).willRead();
			return super.valueInObject(object);
		}

		@Override
		protected void _setValidatedValueInObject(Object value, Object object) throws IllegalAccessException {
			((EOEnterpriseObject) object).willChange();
			super._setValidatedValueInObject(value, object);
		}
	}

	public static class _FieldBinding extends NSKeyValueCoding._FieldBinding {
		public _FieldBinding(Class targetClass, String key, Field field, NSKeyValueCoding.ValueAccessor valueAccessor) {
			super(targetClass, key, field, valueAccessor);
		}

		@Override
		public Object valueInObject(Object object) {
			((EOEnterpriseObject) object).willRead();
			return super.valueInObject(object);
		}

		@Override
		protected void _setValidatedValueInObject(Object value, Object object) throws IllegalAccessException {
			((EOEnterpriseObject) object).willChange();
			super._setValidatedValueInObject(value, object);
		}
	}
}
