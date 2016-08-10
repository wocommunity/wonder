package er.extensions.components.conditionals;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WONoContentElement;
import com.webobjects.foundation.NSDictionary;

import er.extensions.components.ERXWOComponentContent;

/**
 * Allows for multiple contents in a component. For every of one of these, when 
 * you have ERXWOComponentContent in your wrapper, then they will append in their stead.
 * Note that if you also have a plain WOComponentContent in your wrapper, it will get appended
 * a second time. See {@link ERXWOComponentContent} for a full explaination.
 * @author ak (Java port)
 * @author Charles Lloyd
 * @binding templateName
 */
public class ERXWOTemplate extends WODynamicElement {

    private WOAssociation _templateName;
    private WOElement _template;
    
    public ERXWOTemplate(String s, NSDictionary associations, WOElement woelement) {
        super(s, associations, woelement);
        _templateName = (WOAssociation) associations.objectForKey("templateName");
        if(_templateName == null || !_templateName.isValueConstant()) {
            //throw new IllegalStateException("You must bind 'templateName' to a constant string: " + associations);
        }
        _template = woelement;
        if(_template == null) {
        	_template = new WONoContentElement();
        }
    }

    public String templateName(WOComponent component) {
       return (String) _templateName.valueInComponent(component);
    }
    
    @Override
    public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
    	_template.takeValuesFromRequest(worequest, wocontext);
    }
    
    @Override
    public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
    	WOActionResults results = _template.invokeAction(worequest, wocontext);
    	return results;
    }
    
    @Override
    public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
    	_template.appendToResponse(woresponse, wocontext);
    }
    
    @Override
    public String toString() {
    	return "<" + getClass().getName() + "@" + System.identityHashCode(this) + " : " + _templateName  + ">";
    }
}
