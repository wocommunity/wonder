package er.directtoweb.components.repetitions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.ERD2WContainer;
import er.extensions.foundation.ERXArrayUtilities;

/**
 * Can be used as a repetition in list pages.
 *
 * @author ak on Mon Sep 01 2003
 * 
 * @d2wKey componentName
 * @d2wKey object
 * @d2wKey extraListComponentName
 * @d2wKey justification
 * @d2wKey displayNameForProperty
 * @d2wKey sortKeyForList
 * @d2wKey sortCaseInsensitive
 * @d2wKey propertyIsSortable 
 */
public class ERDListPageRepetition extends ERDAttributeRepetition {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = LoggerFactory.getLogger(ERDListPageRepetition.class);

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
    
    @Override
    public NSArray sectionsContents() {
    	NSArray result = super.sectionsContents();
    	if(result.count() == 1) {
    		return result;
    	}
        if(result.count() == 0) {
            return NSArray.EmptyArray;
        }
        ERD2WContainer pair = (ERD2WContainer) result.objectAtIndex(0);
    	return new NSArray(pair);
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
