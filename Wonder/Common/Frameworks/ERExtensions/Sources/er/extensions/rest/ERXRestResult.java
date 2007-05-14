package er.extensions.rest;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.extensions.ERXStringUtilities;

public class ERXRestResult {
	private EOEntity _entity;
	private Object _value;
	private String _nextKey;
	private String _nextPath;
	private boolean _nextKeyIsAll;

	public ERXRestResult(String keypath) {
		this(null, null, keypath);
	}

	public ERXRestResult(EOEntity entity, Object value, String keypath) {
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
		ERXRestResult eRXRestResult;
		if (isLastResult() || isNextToLastResult()) {
			eRXRestResult = this;
		}
		else {
			eRXRestResult = nextResult(context).nextToLastResult(context);
		}
		return eRXRestResult;
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

	public ERXRestResult nextResult(ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		return nextResult(context, true);
	}
	
	public ERXRestResult nextResult(ERXRestContext context, boolean includeContent) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		ERXRestResult nextResult;
		if (_nextKeyIsAll) {
			NSArray objs = NSArray.EmptyArray;
			if (includeContent) {
				objs = context.delegate().objectsForEntity(_entity, context);
			}
			nextResult = new ERXRestResult(_entity, objs, _nextPath);
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
			nextResult = new ERXRestResult(_entity, nextEO, _nextPath);
		}
		else if (_value == null) {
			throw new ERXRestException("Unable to evalute the key '" + _nextKey + "' without an object.");
		}
		else {
			if (_nextKey != null) {
				nextResult = context.delegate().nextResult(_entity, _value, _nextKey, _nextPath, includeContent, context);
				if (nextResult == null) {
					throw new ERXRestNotFoundException("You are not allowed to view the relationship '" + _nextKey + "' on '" + _entity.name() + "' for this path.");
				}
			}
			else {
				throw new ERXRestException("Missing next key for entity '" + _entity.name() + "'.");
			}
		}

		return nextResult;
	}
}