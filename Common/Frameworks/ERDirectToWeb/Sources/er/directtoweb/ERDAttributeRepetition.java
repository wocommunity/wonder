package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

/**
 * Class for DirectToWeb Component ERDAttributeRepetition.
 *
 * @binding sample sample binding explanation
 * @d2wKey sample sample d2w key
 *
 * @created ak on Mon Sep 01 2003
 * @project ERDirectToWeb
 */

public class ERDAttributeRepetition extends ERDCustomComponent {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERDAttributeRepetition.class,"components");

    /**
     * Public constructor
     * @param context the context
     */
    public ERDAttributeRepetition(WOContext context) {
        super(context);
    }
    
    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }

    public String sectionTitle() {
        String result = (String)d2wContext().valueForKey("sectionKey");
        if(result == null || result.length() == 0) result = (String)d2wContext().valueForKey("displayNameForPageConfiguration");
        if(result == null || result.length() == 0) result = (String)d2wContext().valueForKey("pageConfiguration");
        if(result == null || result.length() == 0) result = "noSectionTitle";
        return result;
    }

    public boolean hasPropertyName() {
        String displayNameForProperty=(String)d2wContext().valueForKey("displayNameForProperty");
        return displayNameForProperty!=null && displayNameForProperty.length()>0;
    }

    public NSArray displayPropertyKeys() {
        return (NSArray)valueForBinding("displayPropertyKeys");
    }

    public boolean hasSections() {
        return (sectionsContents().count() > 1 && (sectionsContents().objectAtIndex(0) instanceof ERD2WContainer));
    }

    private NSMutableArray _sectionsContents;

    private ERD2WContainer _currentSection;
    public ERD2WContainer currentSection() { return _currentSection; }
    public void setCurrentSection(ERD2WContainer value) {
        _currentSection = value;
        if (value != null) {
            d2wContext().takeValueForKey(value.name, "sectionKey");
            if (log.isDebugEnabled())
                log.debug("Setting sectionKey: " + value.name);
        }
    }

    public NSArray currentSectionKeys() {
        if (log.isDebugEnabled())
            log.debug("currentSectionKeys()");
        NSArray keys = (NSArray)d2wContext().valueForKey("alternateKeyInfo");
        if (log.isDebugEnabled())
            log.debug("currentSectionKeys (from alternateKeyInfo):" +
                      keys);
        keys = keys == null ? (NSArray)this.currentSection().keys : keys;
        if (log.isDebugEnabled())
            log.debug("Setting sectionKey and keys: " + _currentSection.name + keys);
        return keys;
    }

    public NSArray sectionsContents() {
        if (_sectionsContents ==null) {
            NSArray sectionsContentsFromRule=(NSArray)d2wContext().valueForKey("sectionsContents");
            if (sectionsContentsFromRule==null) {
                sectionsContentsFromRule=(NSArray)displayPropertyKeys();
            }
            if (sectionsContentsFromRule == null)
                throw new RuntimeException("Couldn't find sectionsContents or displayPropertyKeys in d2wContext!");
            if(sectionsContentsFromRule.count() > 0 && !(sectionsContentsFromRule.objectAtIndex(0) instanceof ERD2WContainer))
                _sectionsContents = ERDirectToWeb.convertedPropertyKeyArray(sectionsContentsFromRule, '(', ')');
            else
                _sectionsContents = sectionsContentsFromRule.mutableClone();

        }
        return _sectionsContents;
    }
}