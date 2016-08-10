//
// ERExcelListPage.java: Class file for WO Component 'ERExcelListPage'
// Project ERExcelLook
//
// Created by max on Mon Apr 26 2004
//
package er.directtoweb.excel;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.pages.ERD2WListPage;
import er.extensions.foundation.ERXFileUtilities;

public class ERExcelListPage extends ERD2WListPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERExcelListPage(WOContext context) {
        super(context);
    }
    
    public NSDictionary styles() { 
    	NSDictionary styles = null;
    	String excelStyleFileName = (String)d2wContext().valueForKey("excelStyleFileName"); 
    	String excelStyleFrameworkName = (String)d2wContext().valueForKey("excelStyleFrameworkName"); 
    	if(excelStyleFileName != null) {
    		if(excelStyleFrameworkName == null) {
    			excelStyleFrameworkName = "app";
    		}
    		styles = (NSDictionary)ERXFileUtilities.readPropertyListFromFileInFramework(excelStyleFileName, excelStyleFrameworkName);
    	}  
    	if(styles == null) {
    		styles = ERExcelLook.styles();
    	}
    	return styles; 
    }
    
    public NSArray objectsForSheet() {
        NSArray objectsForSheet = displayGroup().allObjects();
        NSArray sortOrderings = displayGroup().sortOrderings();
        if (sortOrderings != null && sortOrderings.count() > 0) {
            objectsForSheet = EOSortOrdering.sortedArrayUsingKeyOrderArray(objectsForSheet, sortOrderings);
        }
        return objectsForSheet;
    }
}
