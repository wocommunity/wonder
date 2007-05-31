package er.rest;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.extensions.ERXLocalizer;
import er.extensions.ERXStringUtilities;

/**
 * ERXRestKey represents the keypath that was embodied in the request URL. This differs from a normal keypath in that it
 * can contain object IDs. An ERXRestKey is basically a doubly-linked list of keypath entries.
 * 
 * @author mschrag
 */
public class ERXRestKey {
	private ERXRestContext _context;
	private ERXRestKey _previousKey;
	private EOEntity _entity;
	private String _keyAlias;
	private String _key;
	private Object _value;
	private boolean _valueFetched;

	private EOEntity _nextEntity;
	private ERXRestKey _nextKey;

	protected ERXRestKey() {
	}

	/**
	 * Constructs an ERXRestKey.
	 * 
	 * @param context
	 *            the context
	 * @param entity
	 *            the entity that this key is in
	 * @param keyAlias
	 *            the alias of the key
	 * @throws ERXRestException
	 */
	public ERXRestKey(ERXRestContext context, EOEntity entity, String keyAlias) throws ERXRestException {
		if (entity == null) {
			throw new ERXRestException("Unable to evaluate the key '" + _key + "' without an entity.");
		}
		_context = context;
		_entity = entity;
		_keyAlias = keyAlias;
		_key = context.delegate().entityDelegate(entity).propertyNameForPropertyAlias(entity, keyAlias);
	}

	/**
	 * Constructs an ERXRestKey.
	 * 
	 * @param context
	 *            the context
	 * @param entity
	 *            the entity that this key is in
	 * @param keyAlias
	 *            the alias of the key
	 * @param value
	 *            the cached value of the key at this point in the path
	 * @throws ERXRestException
	 */
	public ERXRestKey(ERXRestContext context, EOEntity entity, String keyAlias, Object value) throws ERXRestException {
		this(context, entity, keyAlias);
		_value = value;
		_valueFetched = true;
	}

	/**
	 * Returns a new ERXRestKey with the previous key path removed, so it appears that this key is the start of the
	 * path. This method will have to resolve the value of this key.
	 * 
	 * @return a key path starting at this key
	 * @throws ERXRestException
	 *             if a general exception occurs
	 * @throws ERXRestSecurityException
	 *             if a security error occurs
	 * @throws ERXRestNotFoundException
	 *             if an object is not found in the kaypath
	 */
	public ERXRestKey trimPrevious() throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		// Make sure the value is fetched, but we might accidentally trip a security violation doing this. So
		// we want to ignore the error for now long enough to get the primary key of the object on the other
		// side of the relationship, then queue that up for later.
		Object value = _value(false);
		ERXRestKey trimmedKey = cloneKey(false);
		trimmedKey._entity = nextEntity();
		if (value instanceof EOEnterpriseObject) {
			trimmedKey._key = ERXRestUtils.stringIDForEO((EOEnterpriseObject) value);
		}
		else {
			trimmedKey._key = null;
		}
		return trimmedKey;
	}

	/**
	 * Returns a clone of this key, optionally also cloning back up the keypath. If you choose not to clone the previous
	 * key, it will not have a previous key.
	 * 
	 * @param clonePrevious
	 *            if true, the previous key is also cloned
	 * @return the cloned key
	 * @throws ERXRestException
	 *             if a general exception occurs
	 */
	protected ERXRestKey cloneKey(boolean clonePrevious) throws ERXRestException {
		ERXRestKey cloneKey = new ERXRestKey();
		cloneKey._context = _context;
		cloneKey._entity = _entity;
		cloneKey._key = _key;
		cloneKey._value = _value;
		cloneKey._valueFetched = _valueFetched;
		if (_previousKey != null && clonePrevious) {
			cloneKey._previousKey = _previousKey.cloneKey(true);
			cloneKey._previousKey._nextKey = cloneKey;
		}
		return cloneKey;
	}

	/**
	 * Clones and extends this key to the next key in a path with the given cached value.
	 * 
	 * @param key
	 *            the next key
	 * @param value
	 *            the current cached value
	 * @return a cloned and extended key
	 * @throws ERXRestException
	 *             if a general failure occurs
	 */
	public ERXRestKey extend(String key, Object value) throws ERXRestException {
		ERXRestKey nextKey = new ERXRestKey(_context, nextEntity(), key, value);
		_extend(nextKey, true);
		return nextKey;
	}

	/**
	 * Clones and extends this key to the next key in a path.
	 * 
	 * @param key
	 *            the next key
	 * @return a cloned and extended key
	 * @throws ERXRestException
	 *             if a general failure occurs
	 */
	public ERXRestKey extend(String key) throws ERXRestException {
		ERXRestKey nextKey = new ERXRestKey(_context, nextEntity(), key);
		_extend(nextKey, true);
		return nextKey;
	}

	/**
	 * Extends this key "in-place" to the next key in the path.
	 * 
	 * @param key
	 *            the next key
	 * @return an extended key
	 * @throws ERXRestException
	 *             if a general failure occurs
	 */
	protected ERXRestKey _extendWithoutClone(String key) throws ERXRestException {
		ERXRestKey nextKey = new ERXRestKey(_context, nextEntity(), key);
		_extend(nextKey, false);
		return nextKey;
	}

	/**
	 * Extends this key to the next key (optionally cloning).
	 * 
	 * @param nextKey
	 *            the next key
	 * @param clone
	 *            if true, clone ourselves
	 * @throws ERXRestException
	 *             if a general failure occurs
	 */
	protected void _extend(ERXRestKey nextKey, boolean clone) throws ERXRestException {
		if (isKeyAll()) {
			if (!nextKey.isKeyGID()) {
				throw new ERXRestException("You can only extend the 'all " + _entity.name() + "' with an id, not '" + nextKey.key() + "'.");
			}
		}
		else if (clone) {
			ERXRestKey cloneKey = cloneKey(true);
			cloneKey._nextKey = nextKey;
			nextKey._previousKey = cloneKey;
		}
		else {
			if (_nextKey != null) {
				throw new ERXRestException("You attempted to extend '" + nextKey._key + "' which has already been extended.");
			}
			_nextKey = nextKey;
			nextKey._previousKey = this;
		}
	}

	/**
	 * Returns the entity that contains this key. Note that this is the containing entity NOT the destination entity of
	 * the key.
	 * 
	 * @return the entity
	 */
	public EOEntity entity() {
		return _entity;
	}

	/**
	 * Returns the key alias property name for this key.
	 * 
	 * @return the key alias property name for this key
	 */
	public String keyAlias() {
		return _keyAlias;
	}

	/**
	 * Returns the actual key property name for this key.
	 * 
	 * @return the actual key property name for this key
	 */
	public String key() {
		return _key;
	}

	/**
	 * Returns the previous key in the path (or null if this is the first key).
	 * 
	 * @return the previous key in the path (or null if this is the first key)
	 */
	public ERXRestKey previousKey() {
		return _previousKey;
	}

	/**
	 * Returns the first key in this path (can be this).
	 * 
	 * @return the first key in this path (can be this)
	 */
	public ERXRestKey firstKey() {
		ERXRestKey firstKey = this;
		if (_previousKey != null) {
			firstKey = _previousKey.firstKey();
		}
		return firstKey;
	}

	/**
	 * Returns the last key in this path (can be this).
	 * 
	 * @return the last key in this path (can be this)
	 */
	public ERXRestKey lastKey() {
		ERXRestKey lastKey = this;
		if (_nextKey != null) {
			lastKey = _nextKey.lastKey();
		}
		return lastKey;
	}

	/**
	 * Returns the key path from this key to the end of the path.
	 * 
	 * @param skipGID
	 *            if true, global ids will be skipped in the generated path
	 * @return the key path from this key to the end of the path
	 */
	public String path(boolean skipGID) {
		StringBuffer pathBuffer = new StringBuffer();
		pathBuffer.append(_entity.name());
		appendKey(pathBuffer, skipGID);
		return pathBuffer.toString();
	}

	/**
	 * Appends this key to the given keypath buffer.
	 * 
	 * @param pathBuffer
	 *            the current key path buffer
	 * @param skipGID
	 *            if true, global ids will be skipped in the generated path
	 */
	protected void appendKey(StringBuffer pathBuffer, boolean skipGID) {
		boolean showKey = !skipGID || !isKeyGID();
		if (showKey) {
			pathBuffer.append("/");
			pathBuffer.append(_key);
		}
		if (_nextKey != null) {
			_nextKey.appendKey(pathBuffer, skipGID);
		}
	}

	/**
	 * Returns the next key in this path (can be null).
	 * 
	 * @return the next key in this path (can be null)
	 */
	public ERXRestKey nextKey() {
		return _nextKey;
	}

	/**
	 * Returns the next entity in this key path (i.e. the destination entity). This can be null.
	 * 
	 * @return the next entity in this key path (i.e. the destination entity). This can be null.
	 * @throws ERXRestException
	 *             if a general error occurs
	 */
	protected EOEntity nextEntity() throws ERXRestException {
		EOEntity nextEntity = _nextEntity;
		if (_nextEntity == null) {
			if (isKeyAll()) {
				nextEntity = _entity;
			}
			else if (isKeyGID()) {
				nextEntity = _entity;
			}
			else {
				EORelationship relationship = _entity.relationshipNamed(_key);
				if (relationship != null) {
					nextEntity = relationship.destinationEntity();
				}
				else {
					nextEntity = _context.delegate().entityDelegate(_entity).nextEntity(_entity, _key);
					if (nextEntity == null) {
						nextEntity = _entity;
					}
				}
			}
			_nextEntity = nextEntity;
		}
		return nextEntity;
	}

	/**
	 * Returns (possibly fetching) the destination value of this key. This can be an expensive call, but it will cache
	 * its results.
	 * 
	 * @return the destination value of this key
	 * @throws ERXRestException
	 *             if a general error occurs
	 * @throws ERXRestSecurityException
	 *             if fetching the value triggers a security violation
	 * @throws ERXRestNotFoundException
	 *             if the referenced value does not exist
	 */
	public Object value() throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		return _value(true);
	}

	/**
	 * Returns (possibly fetching) the destination value of this key. This can be an expensive call, but it will cache
	 * its results.
	 * 
	 * @param checkToOnePermissions
	 *            if false, this will not check permissions on a to-one call (this is necessary under certain
	 *            circumstances to provide a more appropriate error messaga).
	 * @return the destination value of this key
	 * @throws ERXRestException
	 *             if a general error occurs
	 * @throws ERXRestSecurityException
	 *             if fetching the value triggers a security violation
	 * @throws ERXRestNotFoundException
	 *             if the referenced value does not exist
	 */
	protected Object _value(boolean checkToOnePermissions) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		Object value = _value;
		if (!_valueFetched) {
			boolean cacheValue = true;
			IERXRestEntityDelegate entityDelegate = _context.delegate().entityDelegate(_entity);
			if (isKeyAll()) {
				value = entityDelegate.objectsForEntity(_entity, _context);
			}
			else {
				Object previousValue = null;
				if (_previousKey != null) {
					previousValue = _previousKey.value();
				}
				if (isKeyGID()) {
					if (previousValue == null) {
						value = entityDelegate.objectWithKey(_entity, _key, _context);
					}
					else if (previousValue instanceof NSArray) {
						NSArray previousObjects = (NSArray) previousValue;
						value = entityDelegate.objectWithKey(_entity, _key, previousObjects, _context);
					}
					else {
						throw new ERXRestException("Unable to evaluate the id '" + _key + "' on an object of type '" + previousValue.getClass().getName() + "'.");
					}
				}
				else if (previousValue == null) {
					throw new ERXRestNotFoundException("Unable to evaluate the key '" + _key + "' on a missing object.");
				}
				else if (previousValue instanceof NSArray) {
					throw new ERXRestException("Unable to evalute the key '" + _key + "' on an array.");
				}
				else {
					if (!entityDelegate.canViewProperty(_entity, previousValue, _key, _context)) {
						throw new ERXRestSecurityException("You are not allowed to view the key '" + _key + "' on the entity '" + _entity.name() + "'.");
					}
					value = entityDelegate.valueForKey(_entity, previousValue, _key, _context);
					EOEntity nextEntity = nextEntity();
					if (value instanceof NSArray) {
						value = _context.delegate().entityDelegate(nextEntity).visibleObjects(_entity, previousValue, _key, nextEntity, (NSArray) value, _context);
					}
					else if (value instanceof EOEnterpriseObject) {
						if (!_context.delegate().entityDelegate(nextEntity).canViewObject(nextEntity, (EOEnterpriseObject) value, _context)) {
							if (checkToOnePermissions) {
								if (_previousKey == null) {
									throw new ERXRestSecurityException("You are not allowed to view the " + nextEntity.name() + " with the id '" + _key + "'.");
								}
								else {
									value = null;
								}
							}
							cacheValue = false;
						}
					}
				}
			}

			if (value instanceof NSArray) {
				((NSArray) value).count();
			}

			if (cacheValue) {
				_value = value;
				_valueFetched = true;
			}
		}
		return value;
	}

	/**
	 * Returns whether or not this key requests a query against all the values of an Entity (/Site.xml = all Sites).
	 * 
	 * @return whether or not this key requests a query against all the values of an Entity
	 */
	public boolean isKeyAll() {
		return _key == null;
	}

	/**
	 * Returns whether or not this key requests a primary key.  Right now, only integer primary keys are supported.
	 *  
	 * @return whether or not this key requests a primary key
	 */
	public boolean isKeyGID() {
		return ERXRestUtils.isEOID(this);
	}

	/**
	 * Parse the given URL path and return an ERXRestKey that represents it.
	 * 
	 * @param context
	 *            the rest context
	 * @param path
	 *            the path to parse
	 * @return the corresponding ERXRestKey
	 * @throws ERXRestException
	 *             if a general exception occurs
	 * @throws ERXRestNotFoundException
	 *             if an object can not be found in the path
	 */
	public static ERXRestKey parse(ERXRestContext context, String path) throws ERXRestException, ERXRestNotFoundException {
		ERXRestKey key = null;
		String[] paths = path.split("/");
		if (paths.length > 0) {
			String entityName = context.delegate().entityNameForAlias(paths[0]);
			EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
			if (entity == null) {
				String railsyEntityName = ERXStringUtilities.capitalize(ERXLocalizer.currentLocalizer().singularifiedString(entityName));
				if (!railsyEntityName.equals(entityName)) {
					entity = EOModelGroup.defaultGroup().entityNamed(railsyEntityName);
				}
				if (entity == null) {
					throw new ERXRestNotFoundException("There is no entity named '" + entityName + "' or '" + railsyEntityName + "'.");
				}
			}
			if (paths.length > 1) {
				key = new ERXRestKey(context, entity, paths[1]);
				for (int pathNum = 2; pathNum < paths.length; pathNum++) {
					key = key._extendWithoutClone(paths[pathNum]);
				}
			}
			else {
				key = new ERXRestKey(context, entity, null);
			}
		}
		return key;
	}
}
