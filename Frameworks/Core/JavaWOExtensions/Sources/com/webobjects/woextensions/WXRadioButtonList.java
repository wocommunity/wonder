package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/**
 * XHTML version of WORadioButtonList
 * 
 * @see WORadioButtonList
 * @author mendis
 *
 */
public class WXRadioButtonList extends WOComponent {
    public WXRadioButtonList(WOContext context) {
        super(context);
    }
    
	public int index;
	private String _id;
    
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
		public static final String selection = "selection";
		public static final String item = "item";
		public static final String id = "id";
		public static final String name = "name";
		public static final String index = "index";
		public static final String prefix = "prefix";	// TODO
		public static final String suffix = "suffix";	// TODO
	}
    
    // accesors
    public Object selection() {
    	return valueForBinding(Bindings.selection);
    }
    
    public void setSelection(Object selection) {
    	setValueForBinding(selection, Bindings.selection);
    }
    
    private Object item() {
    	return valueForBinding(Bindings.item);
    }
    
    public boolean checked() {
    	return (selection() != null) ? selection().equals(item()) : false;
    }
    
    public void setChecked(boolean checked) {
    	if (checked) setSelection(item());
    	else setSelection(null);
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
