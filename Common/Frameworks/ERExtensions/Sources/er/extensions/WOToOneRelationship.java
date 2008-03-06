package er.extensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

/**
 * Back port from WO 5 WOExtensions. This component is binding compatible, but not source compatible.<br />
 * @author ak 
 */

public class WOToOneRelationship extends ERXArrayChooser {
    protected Object _selection;

    public WOToOneRelationship(WOContext aContext)  {
        super(aContext);
    }
    
    public void reset() {
        super.reset();
        _selection = null;
    }

    public void awake() {
        super.awake();
       _selection = null;
    }

    public void updateSourceObject(Object value) {
        String realRelationshipKey = realRelationshipKey();
        Object realSourceObject = realSourceObject();
        
        Object currentValue = NSKeyValueCoding.Utility.valueForKey(realSourceObject, realRelationshipKey);
        if(!ERXExtensions.safeEquals(value, currentValue)) {
            if(realSourceObject instanceof EOEnterpriseObject) {
                EOEnterpriseObject eo = (EOEnterpriseObject)realSourceObject;
                if(value instanceof EOEnterpriseObject) {
                    eo.addObjectToBothSidesOfRelationshipWithKey((EOEnterpriseObject)value, realRelationshipKey);
                } else {
                    Object oldValue = eo.valueForKey(realRelationshipKey);
                    if(oldValue instanceof EOEnterpriseObject) {
                        eo.removeObjectFromBothSidesOfRelationshipWithKey((EOEnterpriseObject)oldValue, realRelationshipKey);
                    } else {
                        //  handle attributes
                        eo.takeValueForKey(value, realRelationshipKey);
                    }
                }
            } else { 
                // handle every other type of object, 
                // we rely on NSMutableDictionary.takeValueForKey(null, someKey) will actually remove the object
                NSKeyValueCoding.Utility.takeValueForKey(realSourceObject, value, realRelationshipKey);
            }
        }
    }
    
    public void setSelection(Object value) {
        if ((value!=null) && (value instanceof NSArray)) {
            log.warn("We were passed an array but expected an EO. Compensating by choosing first element");
            NSArray array = (NSArray)value;
            if (array.count() == 0) {
                value = null;
            } else {
                value = array.objectAtIndex(0);
            }
        }
        
        _selection = value;
        
        if (value==NO_SELECTION_STRING) {
            value = null;
        }
        
        updateSourceObject(value);
        if (hasBinding("selection") && !(sourceObject() instanceof EOEnterpriseObject)) {
        	setValueForBinding(value, "selection");
        }
    }

    public NSArray currentValues() {
    	Object current = selection();
    	return current == null ? NSArray.EmptyArray : new NSArray(current);
    }
    
    public Object selection() {
        if (_selection == null) {
            Object object = realSourceObject();
            String key = realRelationshipKey();
            Object selection = NSKeyValueCoding.Utility.valueForKey(object,key);
            if (selection != null && selection instanceof EOEnterpriseObject) {
              EOEnterpriseObject eo = (EOEnterpriseObject)selection;
              if (eo.editingContext() != editingContext()) {
                selection = ERXEOControlUtilities.localInstanceOfObject(editingContext(), eo);
              }
            }

            setSelection(selection);
        }
        // deal with isMandatory
        if ((_selection==null) && !isMandatory()) {
            setSelection(NO_SELECTION_STRING);
        }
        return _selection;
    }
    
    public NSArray theList() {
        if (_list==null) {
            _list = super.theList();
            if (!isMandatory()) {
                NSMutableArray array = _list.mutableClone();
                array.insertObjectAtIndex(NO_SELECTION_STRING, 0);
                _list = array;
            }
        }
        return _list;
    }

    protected boolean isSingleSelection() {
        return true;
    }
}
