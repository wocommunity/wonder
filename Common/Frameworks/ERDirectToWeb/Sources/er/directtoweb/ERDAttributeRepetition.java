package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXWOContext;

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
    private static final Logger log = Logger.getLogger(ERDAttributeRepetition.class);

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

    public String propertyKey() { return (String)d2wContext().valueForKey("propertyKey"); }
    public void setPropertyKey(String propertyKey) {
    	d2wContext().takeValueForKey(propertyKey, "propertyKey");
    	if(propertyKey != null) {
    		ERXWOContext.contextDictionary().setObjectForKey(propertyKey, "componentIdentifier");
    	} else {
    		ERXWOContext.contextDictionary().removeObjectForKey("componentIdentifier");
    	}
    }
    
    public boolean hasPropertyName() {
        return !booleanValueForBinding("hidePropertyName");
    }

    public NSArray displayPropertyKeys() {
        return (NSArray)valueForBinding("displayPropertyKeys");
    }

    public boolean hasSections() {
        return (sectionsContents().count() > 1 && (sectionsContents().objectAtIndex(0) instanceof ERD2WContainer));
    }

    protected NSMutableArray _sectionsContents;

    protected ERD2WContainer _currentSection;
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
        if (_sectionsContents == null || true) {
            NSArray sectionsContentsFromRule=(NSArray)d2wContext().valueForKey("sectionsContents");
            if (sectionsContentsFromRule==null) {
                sectionsContentsFromRule=(NSArray)displayPropertyKeys();
            }
            if (sectionsContentsFromRule == null)
                throw new RuntimeException("Couldn't find sectionsContents or displayPropertyKeys in d2wContext: " + d2wContext().valueForKey("pageConfiguration"));
            if(sectionsContentsFromRule.count() > 0 && !(sectionsContentsFromRule.objectAtIndex(0) instanceof ERD2WContainer))
                _sectionsContents = ERDirectToWeb.convertedPropertyKeyArray(sectionsContentsFromRule, '(', ')');
            else
                _sectionsContents = sectionsContentsFromRule.mutableClone();

        }
        return _sectionsContents;
    }
    public void appendToResponse(WOResponse r, WOContext c) {
        //HACK ak we should clean this on every step of the phase or not cache at all...
        _sectionsContents=null;
        super.appendToResponse(r,c);
    }
    public void awake() {
        //HACK ak we should clean this on every step of the phase or not cache at all...
        _sectionsContents=null;
        super.awake();
    }
}