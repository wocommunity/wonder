//
// ERExcelListPage.java: Class file for WO Component 'ERExcelListPage'
// Project ERExcelLook
//
// Created by max on Mon Apr 26 2004
//
package er.directtoweb.excel;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import er.directtoweb.*;
import er.extensions.*;
import er.extensions.foundation.ERXFileUtilities;

public class ERExcelListPage extends ERD2WListPage {

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
