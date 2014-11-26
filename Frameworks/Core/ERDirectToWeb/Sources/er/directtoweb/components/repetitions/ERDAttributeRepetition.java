package er.directtoweb.components.repetitions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.ERD2WContainer;
import er.directtoweb.ERDirectToWeb;
import er.directtoweb.components.ERDCustomComponent;
import er.directtoweb.pages.ERD2WPage;
import er.extensions.appserver.ERXWOContext;

/**
 * Class for DirectToWeb Component ERDAttributeRepetition.
 *
 * @author ak on Mon Sep 01 2003
 * 
 * @d2wKey sectionKey
 * @d2wKey displayNameForPageConfiguration
 * @d2wKey pageConfiguration
 * @d2wKey propertyKey
 * @d2wKey alternateKeyInfo
 * @d2wKey sectionsContents
 */
public class ERDAttributeRepetition extends ERDCustomComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
    @Override
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

    /**
     * Gets the <code>displayVariant</code> for the current property key.  The intention is that the display variant
     * allows variation in the display method of property keys without needing different, slightly varying,
     * <code>displayPropertyKeys</code> or <code>tabSectionsContents</code> rules.  Template support has been added for
     * the <code>omit</code> and <code>blank</code> variants.  One could imagine others, such as <code>collapsed</code>,
     * <code>ajax</code>, etc.
     * @return the display variant, if specified
     */
    public String displayVariant() {
        return (String)d2wContext().valueForKey(ERD2WPage.Keys.displayVariant);
    }

    /**
     * Determines if display of the current property key should be <code>omitted</code>.
     * @return true if key should be omitted
     */
    public boolean isKeyOmitted() {
        return "omit".equals(displayVariant());
    }

    public NSArray<String> displayPropertyKeys() {
        return (NSArray<String>)valueForBinding("displayPropertyKeys");
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
        keys = keys == null ? (NSArray)currentSection().keys : keys;
        if (log.isDebugEnabled())
            log.debug("Setting sectionKey and keys: " + _currentSection.name + keys);
        return keys;
    }

    public NSArray sectionsContents() {
        //if (_sectionsContents == null || true) {
            NSArray sectionsContentsFromRule=(NSArray)d2wContext().valueForKey("sectionsContents");
            if (sectionsContentsFromRule==null) {
                sectionsContentsFromRule=displayPropertyKeys();
            }
            if (sectionsContentsFromRule == null)
                throw new RuntimeException("Couldn't find sectionsContents or displayPropertyKeys in d2wContext: " + d2wContext().valueForKey("pageConfiguration"));
            if(sectionsContentsFromRule.count() > 0 && !(sectionsContentsFromRule.objectAtIndex(0) instanceof ERD2WContainer))
                _sectionsContents = ERDirectToWeb.convertedPropertyKeyArray(sectionsContentsFromRule, '(', ')');
            else
                _sectionsContents = sectionsContentsFromRule.mutableClone();

        //}
        return _sectionsContents;
    }
    
    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
        //HACK ak we should clean this on every step of the phase or not cache at all...
        _sectionsContents=null;
        super.appendToResponse(r,c);
    }
    
    @Override
    public void awake() {
        //HACK ak we should clean this on every step of the phase or not cache at all...
        _sectionsContents=null;
        super.awake();
    }
}
