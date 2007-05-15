package er.extensions.rest;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.extensions.ERXStringUtilities;

public class ERXRestResult {
	private ERXRestResult _previousResult;
	private EOEntity _entity;
	private Object _value;
	private String _nextKey;
	private String _nextPath;
	private boolean _nextKeyIsAll;
	private ERXRestResult _cachedNextResult;

	public ERXRestResult(String keypath) {
		this(null, null, null, keypath);
	}

	public ERXRestResult(ERXRestResult previousResult, EOEntity entity, Object value, String keypath) {
		_previousResult = previousResult;
		_entity = entity;
		_value = value;
		initializePaths(keypath);
		if (_nextKey != null && _entity == null) {
			_entity = EOModelGroup.defaultGroup().entityNamed(_nextKey);
			initializePaths(_nextPath);
			if (_nextKey == null) {
				_nextKeyIsAll = true;
			}
		}
	}

	public ERXRestResult cloneResult() {
		return _cloneResult(null);
	}

	protected ERXRestResult _cloneResult(String keypath) {
		ERXRestResult previousCloneResult = null;
		if (_previousResult != null) {
			previousCloneResult = _previousResult._cloneResult(keypath);
		}
		ERXRestResult cloneResult = new ERXRestResult(previousCloneResult, _entity, _value, null);
		cloneResult._nextKey = _nextKey;
		cloneResult._nextPath = _nextPath;
		cloneResult._nextKeyIsAll = _nextKeyIsAll;
		if (previousCloneResult != null) {
			previousCloneResult._cachedNextResult = cloneResult;
		}
		if (keypath != null) {
			if (cloneResult._nextPath == null) {
				if (cloneResult._nextKey == null) {
					cloneResult._nextKey = keypath;
				}
				else {
					cloneResult._nextPath = keypath;
				}
			}
			else {
				cloneResult._nextPath += "/" + keypath;
			}
			cloneResult._nextKeyIsAll = false;
		}
		return cloneResult;
	}

	public ERXRestResult extendResult(String keypath) {
		ERXRestResult previousResult = null;
		if (_previousResult != null) {
			previousResult = _previousResult._cloneResult(keypath);
		}
		ERXRestResult extendedResult = new ERXRestResult(previousResult, _entity, _value, keypath);
		return extendedResult;
	}

	public ERXRestResult previousResult() {
		return _previousResult;
	}

	public boolean isNextKeyIsAll() {
		return _nextKeyIsAll;
	}

	protected void initializePaths(String keypath) {
		if (keypath != null && keypath.length() > 0) {
			int slashIndex = keypath.indexOf('/');
			if (slashIndex == -1) {
				_nextKey = keypath;
				_nextPath = null;
			}
			else {
				_nextKey = keypath.substring(0, slashIndex);
				_nextPath = keypath.substring(slashIndex + 1);
				if (_nextPath.length() == 0) {
					_nextPath = null;
				}
			}
		}
		else {
			_nextKey = null;
			_nextPath = null;
		}
	}

	public String keypath(boolean skipGID, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		StringBuffer sb = new StringBuffer();
		sb.append(_entity.name());
		appendKey(sb, skipGID, context);
		return sb.toString();
	}

	protected void appendKey(StringBuffer sb, boolean skipGID, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		if (_nextKey != null) {
			boolean showKey = !skipGID || !isKeyGID();
			if (showKey) {
				sb.append("/");
				sb.append(_nextKey);
			}
			nextResult(context, _nextPath != null).appendKey(sb, skipGID, context);
		}
	}

	public ERXRestResult firstResult() {
		ERXRestResult firstResult;
		if (_previousResult == null) {
			firstResult = this;
		}
		else {
			firstResult = _previousResult.firstResult();
		}
		return firstResult;
	}

	public EOEntity entity() {
		return _entity;
	}

	public Object value() {
		// MS: Fault arrays ..
		if (_value instanceof NSArray) {
			((NSArray) _value).count();
		}
		return _value;
	}

	public String nextKey() {
		return _nextKey;
	}

	public String nextPath() {
		return _nextPath;
	}

	public boolean isKeyGID() {
		return _nextKey != null && ERXStringUtilities.isDigitsOnly(_nextKey);
	}

	public boolean isLastResult() {
		return _nextKey == null && !_nextKeyIsAll;
	}

	public boolean isNextToLastResult() {
		return _nextKeyIsAll || (_nextKey != null && _nextPath == null);
	}

	public ERXRestResult nextToLastResult(ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		ERXRestResult nextToLastResult;
		if (isLastResult() || isNextToLastResult()) {
			nextToLastResult = this;
		}
		else {
			nextToLastResult = nextResult(context).nextToLastResult(context);
		}
		return nextToLastResult;
	}

	public ERXRestResult lastResult(ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		ERXRestResult lastResult;
		if (isLastResult()) {
			lastResult = this;
		}
		else {
			lastResult = nextResult(context).lastResult(context);
		}
		return lastResult;
	}

	public Object lastValue(ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		ERXRestResult lastResult = lastResult(context);
		return lastResult.value();
	}

	public void _setCachedNextResult(ERXRestResult cachedNextResult) {
		_cachedNextResult = cachedNextResult;
	}

	public ERXRestResult nextResult(ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		return nextResult(context, true);
	}

	public ERXRestResult nextResult(ERXRestContext context, boolean includeContent) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		ERXRestResult nextResult;
		if (_cachedNextResult != null) {
			nextResult = _cachedNextResult;
		}
		else if (_nextKeyIsAll) {
			NSArray objs = NSArray.EmptyArray;
			if (includeContent) {
				objs = context.delegate().objectsForEntity(_entity, context);
			}
			nextResult = new ERXRestResult(this, _entity, objs, _nextPath);
		}
		else if (isKeyGID()) {
			EOEnterpriseObject nextEO = null;
			if (includeContent) {
				if (_value == null) {
					nextEO = context.delegate().objectWithKey(_entity, _nextKey, context);
				}
				else if (_value instanceof NSArray) {
					NSArray objs = (NSArray) _value;
					nextEO = context.delegate().objectWithKey(_entity, _nextKey, objs, context);
				}
				else {
					throw new ERXRestException("Unable to evaluate the id '" + _nextKey + "' on an object.");
				}
			}
			nextResult = new ERXRestResult(this, _entity, nextEO, _nextPath);
		}
		else if (_value == null) {
			throw new ERXRestException("Unable to evalute the key '" + _nextKey + "' on a " + _entity.name() + " without an object.");
		}
		else {
			if (_nextKey != null) {
				nextResult = context.delegate().nextResult(this, includeContent, context);
				if (nextResult == null) {
					throw new ERXRestNotFoundException("You are not allowed to view the relationship '" + _nextKey + "' on '" + _entity.name() + "' for this path.");
				}
			}
			else {
				throw new ERXRestException("Missing next key for entity '" + _entity.name() + "'.");
			}
		}

		if (includeContent && _cachedNextResult == null) {
			_cachedNextResult = nextResult;
		}

		return nextResult;
	}
}