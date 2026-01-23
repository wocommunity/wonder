//
// ERD2WList.java: Class file for WO Component 'ERD2WList'
// Project ERDirectToWeb
//
// Created by max on Wed Nov 20 2002
//
package er.directtoweb.components.relationships;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.directtoweb.components.ERDCustomEditComponent;

public class ERD2WList extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /* logging support */
    public static final Logger log = LoggerFactory.getLogger(ERD2WList.class);

    /** caches the list */
    protected NSArray list;

    /**
     * Public constructor
     * @param context current context
     */
    public ERD2WList(WOContext context) {
        super(context);
    }

    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }

    public NSArray list() {
        if (list == null) {
            try {
                if (hasBinding("list")) {
                    list = (NSArray)valueForBinding("list");
                } else {
                    list = (NSArray)objectKeyPathValue();
                }
            } catch(java.lang.ClassCastException ex) {
                // (ak) This happens quite often when you haven't set up all display keys...
                // the statement makes this more easy to debug
                log.error(ex + " while getting " + key() + " of " + object());
            }
            if (list == null)
                list = NSArray.EmptyArray;
        }
        return list;
    }

    // This is fine because we only use the D2WList if we have at least one element in the list.

    // FIXME: This sucks.
    public boolean isTargetXML(){
        String listConfigurationName = (String)valueForBinding("listConfigurationName");
        return listConfigurationName != null && listConfigurationName.indexOf("XML") > -1;
    }

    public boolean erD2WListOmitCenterTag() {
        return hasBinding("erD2WListOmitCenterTag") ? booleanValueForBinding("erD2WListOmitCenterTag") : false;
    }
    
}
