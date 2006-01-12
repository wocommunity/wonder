package er.extensions;

import java.util.*;

import com.webobjects.appserver.*;
import com.webobjects.appserver._private.*;
import com.webobjects.foundation.*;

/**
 * Allows for multiple Component Contents.

Currently, WOComponentContent can only access a single subtemplate.  We need a way to pass several 
named contents.
<code><pre>
==============================
Parent component:
==============================

&lt;webobject name=SomeComponent&gt;
    This text will be ignored (unless you use WOComponentContent without templateName
    in which case the templates will get appended twice!)
    &lt;webobject name=Template1&gt;
        This is the first template
    &lt;/webobject&gt;

    &lt;webobject name=Template2&gt;
        This is the second template
    &lt;/webobject&gt;

    &lt;webobject name=Template3&gt;
        This is the third template
    &lt;/webobject&gt;
&lt;/webobject&gt;

===========
Parent wod:
===========

SomeComponent: SomeComponent {
    someIvar = someValue;
}

Template1: ERXWOTemplate {
    name = "firstTemplate";
}

Template2: ERXWOTemplate {
    name = "secondTemplate";
}

Template3: ERXWOTemplate {
    name = "thirdTemplate";
}

==============================
Child Component (SomeComponent)
==============================
Some static html
&lt;webobject name=ComponentContent1&gt;
    This is the default content if "firstTemplate" is not defined by parent
&lt;/webobject&gt;

&lt;webobject name=Repetition&gt;
    &lt;webobject name=ComponentContent3&gt;
        This is the default content if "thirdTemplate" is not defined by parent
    &lt;/webobject&gt;
&lt;/webobject&gt;

&lt;webobject name=ComponentContent2&gt;
    This is the default content if "secondTemplate" is not defined by parent
&lt;/webobject&gt;
some more static html

===========
Child wod:
===========
ComponentContent1: ERXWOComponentContent {
    templateName = "firstTemplate";
}

ComponentContent2: ERXWOComponentContent {
    templateName = "secondTemplate";
}

ComponentContent3: ERXWOComponentContent {
    templateName = "thirdTemplate";
}
</pre></code>

So, the way this could work is to add functionality to WOComponentContent which allows 
it to iterate through its elements and locate the named templates.  It also needs to be extended 
so that it takes the contents of its refernce as a default if no named template is provided/found.

<code><pre>
&lt;webobject name=IfThenElse&gt;
    &lt;webobject name=TrueBlock&gt;
        This is true block
    &lt;/webobject&gt;
    &lt;webobject name=FalseBlock&gt;
        This is false block
    &lt;/webobject&gt;
&lt;/webobject&gt;


IfThenElse: IfThenElseComponent {
    condition = someCondition;
}

Template1: ERXWOTemplate {
    name = "true";
}

Template2: ERXWOTemplate {
    name = "false";
}
</pre></code>
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
        if(!assoc.isValueConstant()) {
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
        WOActionResults result = template.invokeAction(worequest, wocontext);
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


