package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.appserver._private.*;
import com.webobjects.foundation.*;

/**
 * Allows for multiple contents in a component. For every of one of these, when 
 * you have ERXWOComponentContent in your wrapper, then they will append in their stead.
 * Note that if you also have a plain WOComponentContent in your wrapper, it will get appended
 * a second time. See {@link ERXWOComponentContent} for a full explaination.
 * @author ak (Java port)
 * @author Charles Lloyd
 */
public class ERXWOTemplate extends WODynamicElement {

    private String _templateName;
    private WOElement _template;
    
    public ERXWOTemplate(String s, NSDictionary associations, WOElement woelement) {
        super(s, associations, woelement);
        WOAssociation assoc = (WOAssociation) associations.objectForKey("templateName");
        if(!(assoc instanceof WOConstantValueAssociation)) {
            throw new IllegalStateException("You must bind 'templateName' to a constant string");
        }
        _templateName = (String) assoc.valueInComponent(null);
        _template = woelement;
        if(_template == null) {
        	_template = new WONoContentElement();
        }
    }

    public String templateName() {
    	return _templateName;
    }
    
    public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
    	_template.takeValuesFromRequest(worequest, wocontext);
    }
    
    public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
    	WOActionResults results = _template.invokeAction(worequest, wocontext);
    	return results;
    }
    
    public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
    	_template.appendToResponse(woresponse, wocontext);
    }
    
    public String toString() {
    	return "<" + getClass().getName() + "@" + System.identityHashCode(this) + " : " + _templateName  + ">";
    }
}
