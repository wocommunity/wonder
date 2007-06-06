package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.ERXArrayUtilities;

/**
 * Can be used as a repetition in list pages.
 *
 * @binding sample sample binding explanation
 * @d2wKey sample sample d2w key
 *
 * @created ak on Mon Sep 01 2003
 * @project ERDirectToWeb
 */

public class ERDListPageRepetition extends ERDAttributeRepetition {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDListPageRepetition.class);

    protected static final NSDictionary NO_ACTIONS = NSDictionary.EmptyDictionary;
    
    public int rowIndex;
    
    /**
     * Public constructor
     * @param context the context
     */
    public ERDListPageRepetition(WOContext context) {
        super(context);
    }

    /** Calculate the colspan for the divider (number of keys in current section + one before and one after the line) */
    public int currentSectionKeysColspan() {
        return currentSectionKeys().count();
    }

    /** Should return an array of arrays denoting actions */
    public NSDictionary actions() {
        NSDictionary actions = (NSDictionary)valueForBinding("actions");
        return actions == null ? NO_ACTIONS : actions;
    }
    
    public NSArray sectionsContents() {
    	NSArray result = super.sectionsContents();
    	if(result.count() == 1) {
    		return result;
    	}
    	ERD2WContainer pair = (ERD2WContainer) result.objectAtIndex(0);
    	return (NSArray) new NSArray(pair);
    }
    
    public NSArray itemSectionsContents() {
    	NSArray result = super.sectionsContents();
    	if(result.count() == 1) {
    		return NSArray.EmptyArray;
    	}
    	return ERXArrayUtilities.arrayByRemovingFirstObject(result);
    }

    public NSArray leftActions() {
        return (NSArray)actions().objectForKey("left");
    }

    public NSArray centerActions() {
        return (NSArray)actions().objectForKey("center");
    }

    public NSArray rightActions() {
        return (NSArray)actions().objectForKey("right");
    }

    public WODisplayGroup displayGroup() {
        return (WODisplayGroup)valueForBinding("displayGroup");
    }

    public boolean isListEmpty() {
        return displayGroup().allObjects().count() == 0;
    }
    
    public String rowClass() {
    	return "AttributeRow" + (rowIndex % 2);
    }
    
    public int displayPropertyKeyCount() {
     	return ((ERD2WContainer)sectionsContents().objectAtIndex(0)).keys.count();
    }
}
