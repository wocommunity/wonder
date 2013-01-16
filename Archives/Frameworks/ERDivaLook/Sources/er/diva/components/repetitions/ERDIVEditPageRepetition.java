package er.diva.components.repetitions;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;

import er.directtoweb.components.repetitions.ERDInspectPageRepetition;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Edit page repetition for Diva look
 * 
 * @author mendis
 *
 */
public class ERDIVEditPageRepetition extends ERDInspectPageRepetition {
    public ERDIVEditPageRepetition(WOContext context) {
        super(context);
    }
    
    // accessors	
    //FIXME RM: move into rules
	public String propertyClassString() {
		return isMandatory() ? "propertyKey mandatory" : "propertyKey";
	}
	
	public boolean isMandatory() {
		return ERXValueUtilities.booleanValue(subContext().valueForKey("isMandatory"));
	}
	
    //FIXME RM: move into rules
	public String contentClassString() {
		return "content " + subContext().componentName();
	}
	
	public boolean disabled() {
		return !hasSections();
	}
	
	public String attributeClassString() {
		String attributeClassString = (String) subContext().valueForKey("classForAttribute");
		return hasTitle() ? attributeClassString + " tooltip" : attributeClassString;
	}
	
	private String title() {
		return (String) subContext().valueForKey("title");
	}
	
	private boolean hasTitle() {
		return (title() != null && !title().equals(""));
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
	
	/**
	 * Gives each property its own d2wContext rather than sharing one
	 * Necessary for ajax or dyanmic D2W
	 */
	@Override
	public void setPropertyKey(String propertyKey) {
		_subContext = new D2WContext(d2wContext());
		_subContext.takeValueForKey(propertyKey, "propertyKey");
	}
	
	public String accordionID() {
		return d2wContext().valueForKey("idForPageConfiguration") + "_accordion";
	}
}
