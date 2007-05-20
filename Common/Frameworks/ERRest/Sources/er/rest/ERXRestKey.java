package er.rest;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.extensions.ERXStringUtilities;

public class ERXRestKey {
	private ERXRestContext _context;
	private ERXRestKey _previousKey;
	private EOEntity _entity;
	private String _key;
	private Object _value;
	private boolean _valueFetched;

	private EOEntity _nextEntity;
	private ERXRestKey _nextKey;

	protected ERXRestKey() {
	}

	public ERXRestKey(ERXRestContext context, EOEntity entity, String key) throws ERXRestException {
		if (entity == null) {
			throw new ERXRestException("Unable to evaluate the key '" + _key + "' without an entity.");
		}
		_context = context;
		_entity = entity;
		_key = key;
	}

	public ERXRestKey(ERXRestContext context, EOEntity entity, String key, Object value) throws ERXRestException {
		this(context, entity, key);
		_value = value;
		_valueFetched = true;
	}

	public ERXRestKey trimPrevious() throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		// Make sure the value is fetched, but we might accidentally trip a security violation doing this. So
		// we want to ignore the error for now long enough to get the primary key of the object on the other
		// side of the relationship, then queue that up for later.
		Object value = value(false);
		ERXRestKey trimmedKey = cloneKey(false);
		trimmedKey._entity = nextEntity();
		if (value instanceof EOEnterpriseObject) {
			trimmedKey._key = ERXRestUtils.idForEO((EOEnterpriseObject) value);
		}
		else {
			trimmedKey._key = null;
		}
		return trimmedKey;
	}

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

	public ERXRestKey extend(String key, Object value, boolean clone) throws ERXRestException {
		ERXRestKey nextKey = new ERXRestKey(_context, nextEntity(), key, value);
		_extend(nextKey, clone);
		return nextKey;
	}

	public ERXRestKey extend(String key, boolean clone) throws ERXRestException {
		ERXRestKey nextKey = new ERXRestKey(_context, nextEntity(), key);
		_extend(nextKey, clone);
		return nextKey;
	}

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

	public EOEntity entity() {
		return _entity;
	}

	public String key() {
		return _key;
	}

	public ERXRestKey previousKey() {
		return _previousKey;
	}

	public ERXRestKey firstKey() {
		ERXRestKey firstKey = this;
		if (_previousKey != null) {
			firstKey = _previousKey.firstKey();
		}
		return firstKey;
	}

	public ERXRestKey lastKey() {
		ERXRestKey lastKey = this;
		if (_nextKey != null) {
			lastKey = _nextKey.lastKey();
		}
		return lastKey;
	}

	public String path(boolean skipGID) {
		StringBuffer pathBuffer = new StringBuffer();
		pathBuffer.append(_entity.name());
		appendKey(pathBuffer, skipGID);
		return pathBuffer.toString();
	}

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

	public ERXRestKey nextKey() {
		return _nextKey;
	}

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

	public Object value() throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		return value(true);
	}

	public Object value(boolean _checkToOnePermissions) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
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
							if (_checkToOnePermissions) {
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

	public boolean isKeyAll() {
		return _key == null;
	}

	public boolean isKeyGID() {
		return ERXRestKey.isKeyGID(_key);
	}

	protected static boolean isKeyGID(String key) {
		return key != null && ERXStringUtilities.isDigitsOnly(key);
	}

	public static ERXRestKey parse(ERXRestContext context, String path) throws ERXRestException, ERXRestNotFoundException {
		ERXRestKey key = null;
		String[] paths = path.split("/");
		if (paths.length > 0) {
			EOEntity entity = EOModelGroup.defaultGroup().entityNamed(context.delegate().entityNameForAlias(paths[0]));
			if (entity == null) {
				throw new ERXRestNotFoundException("There is no entity named '" + paths[0] + "'.");
			}
			if (paths.length > 1) {
				key = new ERXRestKey(context, entity, paths[1]);
				for (int pathNum = 2; pathNum < paths.length; pathNum++) {
					key = key.extend(paths[pathNum], false);
				}
			}
			else {
				key = new ERXRestKey(context, entity, null);
			}
		}
		return key;
	}
}
