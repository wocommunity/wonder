package er.extensions;

import java.util.Enumeration;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.appserver._private.WOHTMLBareString;
import com.webobjects.foundation.NSDictionary;

/**
 * Allows for multiple contents in a component. 
 * @author ak (Java port)
 * @author Charles Lloyd
 */
public class ERXWOComponentContent extends WODynamicElement {
    
    public static String WOHTMLTemplateNameAttribute = "templateName";

    protected String _templateName;
    protected WOElement _defaultTemplate;
    
    public ERXWOComponentContent(String name, NSDictionary associations, WOElement woelement) {
        super(name, associations, woelement);
        WOAssociation assoc = (WOAssociation) associations.objectForKey("templateName");
        if(!(assoc instanceof WOConstantValueAssociation)) {
            throw new IllegalStateException("You must bind 'templateName' to a constant string");
        }
        _templateName = (String) assoc.valueInComponent(null);
        _defaultTemplate = woelement == null ? new WOHTMLBareString("") : woelement;
    }
    
    private WOElement template(WOComponent component) {
        WODynamicGroup content = (WODynamicGroup) component._childTemplate();
        WOElement result;
        for(Enumeration e = content.childrenElements().objectEnumerator(); e.hasMoreElements(); ) {
            result = (WOElement) e.nextElement();
            if(result instanceof ERXWOTemplate) {
                if(((ERXWOTemplate)result).templateName().equals(_templateName)) {
                    return result;
                }
            }
        }
        
        return _defaultTemplate;
    }
    
    public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
        WOComponent component = wocontext.component();
        WOElement template = template(component);
        wocontext._setCurrentComponent(component.parent());
        template.appendToResponse(woresponse, wocontext);
        wocontext._setCurrentComponent(component);
    }
    
    public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
        WOComponent component = wocontext.component();
        WOElement template = template(component);
        wocontext._setCurrentComponent(component.parent());
        WOActionResults result;
        result = template.invokeAction(worequest, wocontext);
        wocontext._setCurrentComponent(component);
        return result;
    }
    
    public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
        WOComponent component = wocontext.component();
        WOElement template = template(component);
        wocontext._setCurrentComponent(component.parent());
        template.takeValuesFromRequest(worequest, wocontext);
        wocontext._setCurrentComponent(component);
    }

    public String toString() {
        return "<" + getClass().getName() + "@" + System.identityHashCode(this) + " : " + _templateName  + ">";
    }
}


