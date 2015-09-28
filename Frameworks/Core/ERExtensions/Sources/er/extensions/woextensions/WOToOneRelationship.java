package er.extensions.woextensions;

import org.apache.commons.lang3.ObjectUtils;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.components.ERXArrayChooser;
import er.extensions.eof.ERXEOControlUtilities;

/**
 * Back port from WO 5 WOExtensions. This component is binding compatible, but not source compatible.
 * 
 * @author ak 
 */
public class WOToOneRelationship extends ERXArrayChooser {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    protected Object _selection;

    public WOToOneRelationship(WOContext aContext)  {
        super(aContext);
    }

    @Override
    public void reset() {
        super.reset();
        _selection = null;
    }

    @Override
    public void awake() {
        super.awake();
       _selection = null;
    }

    public void updateSourceObject(Object value) {
        String realRelationshipKey = realRelationshipKey();
        Object realSourceObject = realSourceObject();
        
        Object currentValue = NSKeyValueCoding.Utility.valueForKey(realSourceObject, realRelationshipKey);
        if (ObjectUtils.notEqual(value, currentValue)) {
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

    @Override
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
        //If using a browser, we have to return an array. Be sure we don't stick null into an array.
        Object selection;
        if (isBrowser()) {
        	if (_selection == null) {
        		selection = NSArray.EmptyArray;
        	}
        	else {
        		selection = new NSArray(_selection);
        	}
        }
        else {
        	selection = _selection;
        }
        return selection;
    }

    @Override
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

    @Override
    protected boolean isSingleSelection() {
        return true;
    }
}
