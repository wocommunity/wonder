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

	protected String _debugDescription() {
		return _parentBindingName;
	}

	@Override
	public void setValue(Object aValue, WOComponent aComponent) {
		if (WOHelperFunctionParser._debugSupport) {
			WOHelperFunctionDebugUtilities.setDebugEnabled(this, aComponent);
		}

/*  70*/ com.webobjects.appserver.WOAssociation.Event anEvent = null;
/*  72*/ if(_debugEnabled)
/*  73*/      _logPushValue(aValue, aComponent);
/*  76*/ if(_keyPath != null) {
/*  77*/     com.webobjects.foundation.NSValidation.ValidationException aValidationException = null;
/*  78*/     Object aCoercedValue = null;
/*  79*/     Object aTarget = aComponent.valueForBinding(_parentBindingName);
/*  81*/     anEvent = _markStartOfEventIfNeeded("takeValueForKeyPath", _keyPath, aComponent);
/*  83*/     if(aTarget != null)
/*  87*/         try {
/*  87*/             aCoercedValue = com.webobjects.foundation.NSValidation.Utility.validateTakeValueForKeyPath(aTarget, aValue, _keyPath);
                 }
/*  88*/         catch(com.webobjects.foundation.NSValidation.ValidationException exception) {
/*  89*/             NSLog._conditionallyLogPrivateException(exception);
/*  90*/             aValidationException = exception;
                 }
/*  94*/         if(anEvent != null)
/*  95*/             EOEventCenter.markEndOfEvent(anEvent);
/*  98*/         if(_debugEnabled)
/*  99*/             if(aTarget instanceof WOComponent)
/* 100*/                 _logPushValue(aCoercedValue, (WOComponent)aTarget);
/* 102*/             else
/* 102*/                 _logPushValue(aCoercedValue, null);
/* 105*/         if(aValidationException != null) {
			         if (aTarget instanceof WOComponent) {
					     /* 107*/ ((WOComponent)aTarget).validationFailedWithException(aValidationException, aValue, _keyPath);
					 }
			         // Bug Fix as of WO 5.4.3: validation exceptions are swallowed by WOBindingNameAssociation 
			         // when key paths are used that don't start with a WOComponent, e.g. ^eo.attribute
			         else {
					     aComponent.validationFailedWithException(aValidationException, aValue, _parentBindingName + "." + _keyPath);
					 }
        		} 
		}
		else {
            /* 111*/  anEvent = _markStartOfEventIfNeeded("takeValueForKeyPath", _keyPath, aComponent);
            /* 113*/  aComponent.setValueForBinding(aValue, _parentBindingName);
            /* 115*/  if(anEvent != null)
            /* 116*/   EOEventCenter.markEndOfEvent(anEvent);
        }
    }
	
}
