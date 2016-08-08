package er.rest;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSMutableDictionary;

public class ERXRestContext {
	private EOEditingContext _editingContext;
	private NSMutableDictionary<String, Object> _userInfo;

	public ERXRestContext() {
		this(null);
	}

	public ERXRestContext(EOEditingContext editingContext) {
		_editingContext = editingContext;
	}

	public EOEditingContext editingContext() {
		return _editingContext;
	}
	
	public void setUserInfoForKey(Object value, String key) {
		if (_userInfo == null) {
			_userInfo = new NSMutableDictionary<>();
		}
		_userInfo.setObjectForKey(value, key);
	}
	
	public Object userInfoForKey(String key) {
		Object value = null;
		if (_userInfo != null) {
			value = _userInfo.objectForKey(key);
		}
		return value;
	}
}
