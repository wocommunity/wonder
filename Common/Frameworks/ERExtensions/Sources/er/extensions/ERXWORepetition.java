package er.extensions;

import java.util.List;

import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/**
 * Replacement for WORepetition. Adds support for {@see java.util.List} and {@see java.lang.Array},
 * in addition to {@see com.webobjects.foundation.NSArray} and {@see java.util.Vector} 
 * (which is a {@see java.util.List} in 1.4). This is listed as Radar #3325342 since June 2003.
 * Should be installed via ERXPatcher.setClassForName(ERXWORepetition.class, "WORepetition")
 * into the runtime system.
 * Note that this implementation adds a small amount of overhead due to the creation of the Context for each
 * RR phase, but this is preferable to having to give so many parameters.
 * @author ak
 */

public class ERXWORepetition extends WODynamicGroup {
    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERXWORepetition.class,"elements");
    
    protected WOAssociation _list;
    protected WOAssociation _item;
    protected WOAssociation _count;
    protected WOAssociation _index;
    
    /** 
     * WOElements must be reentrant, so we need a context object or will have to add the 
     * parameters to every method. Note that it's OK to have no object at all.
     */
    protected class Context {
        protected NSArray nsarray;
        protected List list;
        protected Object[] array;
        
        public Context(Object object) {
            	if (object != null) {
            		if (object instanceof NSArray)
            			nsarray = (NSArray) object;
            		else if (object instanceof List)
            			list = (List) object;
            		else if (object instanceof Object [])
            			array = (Object []) object;
            		else
            			throw new IllegalArgumentException
						("Evaluating 'list' binding returned a "
								+ object.getClass().getName()
								+ " when it should return either a NSArray, an Object[] array or a java.util.List .");
            	}
        }
        
        /** 
         * Gets the number of elements from any object. 
         */
        protected int count() { 
        	    if (nsarray != null) {
        	    	    return nsarray.count();
        	    } else if (list != null) {
        	    	    return list.size();
        	    } else if (array != null) {
        	    	    return array.length;
        	    }
        	    return 0;
        }
        /** 
         * Gets the object at the given index from any object.
         */
        protected Object objectAtIndex(int i) {
            	if (nsarray != null) {
            		return nsarray.objectAtIndex(i);
            	} else if(list != null) {
            		return list.get(i);
            	} else if(array != null) {
            		return array[i];
            	} 
            return null;
        }
    }
    
    /** Designated Constructor. Gets called by the template parser. Checks if the bindings are valid. */
    public ERXWORepetition(String string, NSDictionary associations, WOElement woelement) {
        super(null, null, woelement);
        
        _list  = (WOAssociation) associations.objectForKey("list");
        _item  = (WOAssociation) associations.objectForKey("item");
        _count = (WOAssociation) associations.objectForKey("count");
        _index = (WOAssociation) associations.objectForKey("index");
        
        if (_list == null && _count == null)
            _failCreation("Missing 'list' or 'count' attribute.");
        if (_list != null && _item == null)
            _failCreation("Missing 'item' attribute with 'list' attribute.");
        if (_list != null && _count != null)
            _failCreation("Illegal use of 'count' attribute with 'list' attribute.");
        if (_count != null && (_list != null || _item != null))
            _failCreation("Illegal use of 'list', or 'item'attributes with 'count' attribute.");
        if (_item != null && !_item.isValueSettable())
            _failCreation("Illegal read-only 'item' attribute.");
        if (_index != null && !_index.isValueSettable())
            _failCreation("Illegal read-only 'index' attribute.");
    }
    
    /** Utility to throw an exception if the bindings are incomplete. */
    protected void _failCreation(String message) {
        throw new WODynamicElementCreationException("<" + this.getClass().getName() + "> " + message);
    }
    
    /** Human readable description. */
    public String toString() {
        return ("<" + this.getClass().getName()
                + " list: "  + (_list != null ? _list.toString() : "null")
                + " item: "  + (_item != null ? _item.toString() : "null")
                + " count: " + (_count != null ? _count.toString() : "null")
                + " index: " + (_index != null ? _index.toString() : "null")
                + ">");
    }
    
    
    /** Prepares the WOContext for the loop iteration. */
    protected void _prepareForIterationWithIndex(Context context, int index, WOContext wocontext, WOComponent wocomponent) {
        if (_item != null) {
            Object object = context.objectAtIndex(index);
            _item._setValueNoValidation(object, wocomponent);
        }
        if (_index != null) {
            Integer integer = ERXConstant.integerForInt(index);
            _index._setValueNoValidation(integer, wocomponent);
        }
        if (index != 0)
            wocontext.incrementLastElementIDComponent();
        else
            wocontext.appendZeroElementIDComponent();
    }
    
    /** Cleans the WOContext after the loop iteration. */
    protected void _cleanupAfterIteration(int i, WOContext wocontext, WOComponent wocomponent) {
        if (_item != null)
            _item._setValueNoValidation(null, wocomponent);
        if (_index != null) {
            Integer integer = ERXConstant.integerForInt(i);
            _index._setValueNoValidation(integer, wocomponent);
        }
        wocontext.deleteLastElementIDComponent();
    }
    
     
    /** Fills the context with the object given in the "list" binding. */
    protected String _indexStringForSenderAndElement(String senderID, String elementID) {
        int dotOffset = elementID.length() + 1;
        int nextDotOffset = senderID.indexOf('.', dotOffset);
        String indexString;
        if (nextDotOffset < 0)
            indexString = senderID.substring(dotOffset);
        else
            indexString = senderID.substring(dotOffset, nextDotOffset);
        return indexString;
    }
    
    protected String _indexOfChosenItem(WORequest worequest, WOContext wocontext) {
        String indexString = null;
        String senderID = wocontext.senderID();
        String elementID = wocontext.elementID();
        if (senderID.startsWith(elementID)) {
            int i = elementID.length();
            if (senderID.length() > i && senderID.charAt(i) == '.')
                indexString = _indexStringForSenderAndElement(senderID, elementID);
        }
        return indexString;
    }
    
    protected int _count(Context context, WOComponent wocomponent) {
        int count;
        if (_list != null) {
            count = context.count();
        } else {
            Object object = _count.valueInComponent(wocomponent);
            if (object != null) {
                count = ERXValueUtilities.intValue(object);
            } else {
                log.error(toString() + " 'count' evaluated to null in component "
                        + wocomponent.toString()
                        + ".\nRepetition  count reset to 0.");
                count = 0;
            }
        }
        return count;
    }
    
    protected Context createContext(WOComponent wocomponent) {
    	    return new Context(_list != null ? _list.valueInComponent(wocomponent) : null);  
    }
    
    public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
        WOComponent wocomponent = wocontext.component();
        Context context = createContext(wocomponent);
        
        int count = _count(context, wocomponent);
        
        for (int index = 0; index < count; index++) {
            _prepareForIterationWithIndex(context, index, wocontext, wocomponent);
            super.takeValuesFromRequest(worequest, wocontext);
        }
        if (count > 0) {
            _cleanupAfterIteration(count, wocontext, wocomponent);
        }
    }
    
    public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
        WOComponent wocomponent = wocontext.component();
        Context context = createContext(wocomponent);
        
        int count = _count(context, wocomponent);
        
        String indexString = _indexOfChosenItem(worequest, wocontext);
        int index = indexString == null ? 0 : Integer.parseInt(indexString);
        int start = indexString == null ? 0 : index;
        int end   = indexString == null ? count : (index + 1);
        
        WOActionResults woactionresults = null;
        
        for (int i = start; i < end && woactionresults == null; i++) {
            _prepareForIterationWithIndex(context, i, wocontext, wocomponent);
            woactionresults = super.invokeAction(worequest, wocontext);
        }
        
        if (count > 0) {
            _cleanupAfterIteration(count, wocontext, wocomponent);
        }
        return woactionresults;
    }
    
    public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
        WOComponent wocomponent = wocontext.component();
        Context context = createContext(wocomponent);
        
        int count = _count(context,wocomponent);
        
        for (int index = 0; index < count; index++) {
            _prepareForIterationWithIndex(context, index, wocontext, wocomponent);
            super.appendChildrenToResponse(woresponse, wocontext);
        }
        if (count > 0) {
        	    _cleanupAfterIteration(count, wocontext, wocomponent);
        }
    }
}

