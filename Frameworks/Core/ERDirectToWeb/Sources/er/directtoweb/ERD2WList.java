//
// ERD2WList.java: Class file for WO Component 'ERD2WList'
// Project ERDirectToWeb
//
// Created by max on Wed Nov 20 2002
//
package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

public class ERD2WList extends ERDCustomEditComponent {

    /* logging support */
    public static final Logger log = Logger.getLogger(ERD2WList.class);

    /** caches the list */
    protected NSArray list;

    /**
     * Public constructor
     * @param context current context
     */
    public ERD2WList(WOContext context) {
        super(context);
    }

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
