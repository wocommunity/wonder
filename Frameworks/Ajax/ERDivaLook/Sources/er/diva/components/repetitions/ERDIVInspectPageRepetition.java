package er.diva.components.repetitions;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.repetitions.ERDInspectPageRepetition;
import er.extensions.eof.ERXEOControlUtilities;

/**
 * Inspect page repetition for Diva Look
 * 
 * @author mendis
 *
 */
public class ERDIVInspectPageRepetition extends ERDInspectPageRepetition {
    public ERDIVInspectPageRepetition(WOContext context) {
        super(context);
    }
    
    // accessors
    //FIXME RM: move into rules
	public String contentClassString() {
		return "content " + d2wContext().componentName();
	}
	
	public boolean disabled() {
		return !hasSections();
	}
	
	public String attributeClassString() {
		return (String) d2wContext().valueForKey("classForAttribute"); 
	}
	
	public String accordionID() {
		return d2wContext().valueForKey("idForPageConfiguration") + primaryKey() + "_Accordion";
	}
	
	public String sectionID() {
		return d2wContext().valueForKey("idForSection") + primaryKey();
	}
	
	private String primaryKey() {
		return ERXEOControlUtilities.primaryKeyStringForObject(object());
	}
	
	public String contentContainerID() {
		return d2wContext().valueForKey("id") + "_container";
	}
}