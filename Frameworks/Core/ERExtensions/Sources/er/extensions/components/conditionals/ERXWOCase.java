package er.extensions.components.conditionals;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

/**
 * Contains one case of a {@link ERXWOSwitch}.
 *
 * @author ak (Java port)
 * @author Charles Lloyd
 * @binding case
 */
public class ERXWOCase extends WODynamicElement {

    protected Object _value;
    protected WOElement _children;
    
    public ERXWOCase(String name, NSDictionary<String, WOAssociation> associations, WOElement woelement) {
        super(name, associations, woelement);
        WOAssociation assoc = associations.objectForKey("case");
        if(!assoc.isValueConstant()) {
            throw new IllegalStateException("You must bind 'case' to a constant value");
        }
        _value = assoc.valueInComponent(null);
        _children = woelement;
    }
    
    public Object caseValue() {
        return _value;
    }
    
    @Override
    public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
        _children.appendToResponse(woresponse, wocontext);
    }
    
    @Override
    public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
        return _children.invokeAction(worequest, wocontext);
    }
    
    @Override
    public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
        _children.takeValuesFromRequest(worequest, wocontext);
    }
}
