package ognl.helperfunction;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver._private.WOBindingNameAssociation;
import com.webobjects.eocontrol.EOEventCenter;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;

/**
 * WOHelperFunction version of WOBindingNameAssociation that adds support for dynamic debugSupport.
 * 
 * @author mschrag
 */
public class WOHelperFunctionBindingNameAssociation extends WOBindingNameAssociation {
	
	private String _keyPath;
	
	public WOHelperFunctionBindingNameAssociation(String keyPath) {
		super(keyPath);
		// Extract _keyPath for bug fix in setValue()
		NSMutableArray aKeyArray = NSArray._mutableComponentsSeparatedByString(keyPath, ".");
		if(aKeyArray.count() > 1) {
			aKeyArray.removeObjectAtIndex(0);
			_keyPath = aKeyArray.componentsJoinedByString(".");
		}
	}

	@Override
	protected String _debugDescription() {
		return _parentBindingName;
	}

	@Override
	public void setValue(Object aValue, WOComponent aComponent) {
		if (WOHelperFunctionParser._debugSupport) {
			WOHelperFunctionDebugUtilities.setDebugEnabled(this, aComponent);
		}

		com.webobjects.appserver.WOAssociation.Event anEvent = null;
		if(_debugEnabled)
			_logPushValue(aValue, aComponent);
		if(_keyPath != null) {
			com.webobjects.foundation.NSValidation.ValidationException aValidationException = null;
			Object aCoercedValue = null;
			Object aTarget = aComponent.valueForBinding(_parentBindingName);
			anEvent = _markStartOfEventIfNeeded("takeValueForKeyPath", _keyPath, aComponent);
			if(aTarget != null)
				try {
					aCoercedValue = com.webobjects.foundation.NSValidation.Utility.validateTakeValueForKeyPath(aTarget, aValue, _keyPath);
				}
			catch(com.webobjects.foundation.NSValidation.ValidationException exception) {
				NSLog._conditionallyLogPrivateException(exception);
				aValidationException = exception;
			}
			if(anEvent != null)
				EOEventCenter.markEndOfEvent(anEvent);
			if(_debugEnabled)
				if(aTarget instanceof WOComponent)
					_logPushValue(aCoercedValue, (WOComponent)aTarget);
				else
					_logPushValue(aCoercedValue, null);
			if(aValidationException != null) {
			         if (aTarget instanceof WOComponent) {
			        	 ((WOComponent)aTarget).validationFailedWithException(aValidationException, aValue, _keyPath);
					 }
			         // Bug Fix as of WO 5.4.3: validation exceptions are swallowed by WOBindingNameAssociation 
			         // when key paths are used that don't start with a WOComponent, e.g. ^eo.attribute
			         else {
					     aComponent.validationFailedWithException(aValidationException, aValue, _parentBindingName + "." + _keyPath);
					 }
        		} 
		}
		else {
			anEvent = _markStartOfEventIfNeeded("takeValueForKeyPath", _keyPath, aComponent);
			aComponent.setValueForBinding(aValue, _parentBindingName);
			if(anEvent != null)
				EOEventCenter.markEndOfEvent(anEvent);
        }
    }
	
}
