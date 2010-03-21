package er.divalite.components.repetitions;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;

import er.directtoweb.components.repetitions.ERDInspectPageRepetition;
import er.extensions.eof.ERXEOControlUtilities;

/**
 * Inspect page repetition for Diva lite Look
 * 
 * @author mendis
 *
 */
public class ERLITInspectPageRepetition extends ERDInspectPageRepetition {
    public ERLITInspectPageRepetition(WOContext context) {
        super(context);
    }
    
    // accessors
    //FIXME RM: move into rules
	public String contentClassString() {
		return "content " + subContext().componentName();
	}
	
	public boolean disabled() {
		return !hasSections();
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
		return subContext().valueForKey("id") + "_container";
	}
	
	protected D2WContext _subContext;
	
	public D2WContext subContext() {
		return _subContext;
	}
	
	public void setSubContext(D2WContext aContext) {
		_subContext = aContext;
	}
	
	public String toggleClassString() {
		return "accordion-content " + d2wContext().valueForKey("displayNameForSectionKey");
	}
	
	/**
	 * Gives each property its own d2wContext rather than sharing one
	 * Necessary for ajax or dyanmic D2W
	 */
	@Override
	public void setPropertyKey(String propertyKey) {
		_subContext = new D2WContext(d2wContext());
		_subContext.takeValueForKey(propertyKey, "propertyKey");
	}
}
