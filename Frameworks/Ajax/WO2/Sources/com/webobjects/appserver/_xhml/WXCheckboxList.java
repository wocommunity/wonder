package com.webobjects.appserver._xhml;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * XHTML equivalents of WOCheckboxList
 * 
 * @see WOCheckboxList
 * @author mendis
 *
 */
public class WXCheckboxList extends WOComponent {
	public int index;
	private String _id;
	
    public WXCheckboxList(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }
    
    @Override
    public boolean isStateless() {
    	return true;
    }
    
    @Override
    public void reset() {
    	super.reset();
    	_id = null;
    }
    
	/*
	 *  api of component
	 */
	public static interface Bindings {
		public static final String selections = "selections";
		public static final String item = "item";
		public static final String id = "id";
		public static final String name = "name";
		public static final String index = "index";
		public static final String prefix = "prefix";	// TODO
		public static final String suffix = "suffix";	// TODO
	}
    
    // accesors
    @SuppressWarnings("unchecked")
	private NSArray<Object> selections() {
    	return (NSArray<Object>) valueForBinding(Bindings.selections);
    }
    
    private Object item() {
    	return valueForBinding(Bindings.item);
    }
    
    public Object selection() {
    	if (selections() == null) return null;
    	return selections().contains(item()) ? item() : null;
    }
    
    public void setSelection(Object value) {
     	NSMutableArray<Object> selections = (selections() != null) ? selections().mutableClone() : new NSMutableArray<Object>();
    	if (value != null) {
    		selections.addObject(item());
    	} else {
    		selections.removeObject(item());
    	}
    	setValueForBinding(selections, Bindings.selections);
    }
    
    public String elementName() {
    	return hasBinding(Bindings.name) ? (String) valueForBinding(Bindings.name) : _id();		
    }
    
    public String id() {
    	return _id() + "_" + index;
    }
    
    private String _id() {
    	if (_id == null) _id = hasBinding(Bindings.id) ? (String)  valueForBinding(Bindings.id) : context().elementID();	// RM: FIXME: convert to javascriptElementID() in WO 5.4
    	return _id;
    }
    
    public void setIndex(int i) {
    	index = i;
    	setValueForBinding(index, Bindings.index);
    }
}