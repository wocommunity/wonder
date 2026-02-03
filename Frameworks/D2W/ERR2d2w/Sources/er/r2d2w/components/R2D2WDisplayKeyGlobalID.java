package er.r2d2w.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXEOGlobalIDUtilities;
import er.extensions.eof.ERXKeyGlobalID;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;

public class R2D2WDisplayKeyGlobalID extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    static final Logger log = LoggerFactory.getLogger(R2D2WDisplayKeyGlobalID.class);
    
    private static final String displayNameForEntityKey = "displayNameForEntityOfGlobalID";
    private static final String primaryKeyStringForGlobalIDKey = "primaryKeyStringForGlobalID";
    private static final String labelLocKey = "R2D2W.inspectGlobalID.buttonLabel";
    private static final String titleLocKey = "R2D2W.inspectGlobalID.helpString";

    public R2D2WDisplayKeyGlobalID(WOContext context) {
        super(context);
    }

    public EOEnterpriseObject objectForGlobalID() {
    	return ERXEOGlobalIDUtilities.fetchObjectWithGlobalID(object().editingContext(), globalIDFromObjectPropertyValue());
    }
    
    public ERXKeyGlobalID globalIDFromObjectPropertyValue() {
    	return (ERXKeyGlobalID)objectPropertyValue();
    }
    
    public String inspectEntityLabel() {
    	NSMutableDictionary<String, String> locDict = new NSMutableDictionary<String, String>();
    	ERXKeyGlobalID gid = globalIDFromObjectPropertyValue();
    	locDict.setObjectForKey(ERXEOControlUtilities.primaryKeyStringForGlobalID(gid), primaryKeyStringForGlobalIDKey);
    	locDict.addEntriesFromDictionary(displayNameDictionary());
    	ERXLocalizer loc = ERXLocalizer.currentLocalizer();
    	return loc.localizedTemplateStringForKeyWithObject(labelLocKey, locDict);
    }
    
    public String inspectEntityTitle() {
    	ERXLocalizer loc = ERXLocalizer.currentLocalizer();
    	return loc.localizedTemplateStringForKeyWithObject(titleLocKey, displayNameDictionary());
    }
    
    protected NSDictionary<String, String> displayNameDictionary() {
    	String entityName = globalIDFromObjectPropertyValue().entityName();
    	String localizerKey = "Entity.name." + entityName;
    	String result = ERXLocalizer.currentLocalizer().localizedStringForKey(localizerKey);
        if(result == null) {
            result = ERXStringUtilities.displayNameForKey(entityName);
            result = localizedValueForKey(result, localizerKey);
        }
        return new NSDictionary<String, String>(result, displayNameForEntityKey);
    }
    
	private String localizedValueForKey(String result, String localizerKey) {
		String formerResult = ERXLocalizer.currentLocalizer().localizedStringForKey(result);
		if(formerResult != null) {
			result = formerResult;
			log.info("Found an old-style entry: " + localizerKey +"->" + formerResult);
		}
		ERXLocalizer.currentLocalizer().takeValueForKey(result, localizerKey);
		return result;
	}

	public WOActionResults inspectEntity() {
		InspectPageInterface ipi = D2W.factory().inspectPageForEntityNamed(globalIDFromObjectPropertyValue().entityName(), session());
		ipi.setObject(objectForGlobalID());
		ipi.setNextPage(context().page());
		return (WOComponent)ipi;
	}

}