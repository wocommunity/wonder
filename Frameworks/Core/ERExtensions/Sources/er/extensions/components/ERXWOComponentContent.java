package er.extensions.components;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.appserver._private.WOHTMLBareString;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.components.conditionals.ERXWOTemplate;

/**
 * Allows for multiple Component Contents.
 * 
 * Currently, WOComponentContent can only access a single subtemplate. We need a
 * way to pass several named contents. <pre><code>
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
    templateName = "firstTemplate";
}

Template2: ERXWOTemplate {
    templateName = "secondTemplate";
}

Template3: ERXWOTemplate {
    templateName = "thirdTemplate";
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
</code></pre>
 * 
 * So, the way this could work is to add functionality to WOComponentContent
 * which allows it to iterate through its elements and locate the named
 * templates. It also needs to be extended so that it takes the contents of its
 * reference as a default if no named template is provided/found.
 * 
 * <pre><code>
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
    templateName = "true";
}

Template2: ERXWOTemplate {
    templateName = "false";
}
</code></pre>
 * 
 * @binding templateName The templateName of the ERXWOTemplate which should be rendered
 *          in place of this element. If not set, this element will behave like
 *          a regular WOComponentContent, but filter out all ERXWOTemplates.
 *          
 * @author ak (Java port)
 * @author Charles Lloyd
 */
public class ERXWOComponentContent extends WODynamicElement {
    
	public static Logger log = Logger.getLogger(ERXWOComponentContent.class);
	
    public static String WOHTMLTemplateNameAttribute = "templateName";

    private WOAssociation _templateName;
    protected WOElement _defaultTemplate;
    
    public ERXWOComponentContent(String name, NSDictionary associations, WOElement woelement) {
        super(name, associations, woelement);
        _templateName = (WOAssociation) associations.objectForKey("templateName");
        _defaultTemplate = woelement == null ? new WOHTMLBareString("") : woelement;
    }

    private WOElement template(WOComponent component) {
    	WOElement content =  component._childTemplate();
    	WOElement result = null;
    	String templateName = (_templateName == null) ? null : (String) _templateName.valueInComponent(component);
    	if (content instanceof WODynamicGroup) {
			WODynamicGroup group = (WODynamicGroup) content;
			if (templateName == null) {
				// MS: If you don't set a template name, then let's construct all the children of 
				// this element that are NOT ERXWOTemplate's, so we don't double-display.  This lets
				// you use an ERXWOComponentContent and have it just act like a "default" template
				// that skips all the children that are explicitly wrapped in an ERXWOTemplate.
				NSMutableArray<WOElement> originalChildrenElements = group.childrenElements();
				if (originalChildrenElements != null && originalChildrenElements.count() > 0) {
					NSMutableArray<WOElement> nonTemplateChildrenElements = new NSMutableArray<WOElement>();
					for (WOElement originalChild : originalChildrenElements) {
						if (!(originalChild instanceof ERXWOTemplate)) {
							nonTemplateChildrenElements.addObject(originalChild);
						}
					}
					result = new WODynamicGroup(null, null, nonTemplateChildrenElements);
				}
			}
			else {
		        for(Enumeration e = group.childrenElements().objectEnumerator(); e.hasMoreElements() && result == null ; ) {
		        	WOElement current = (WOElement) e.nextElement();
		        	if(current instanceof ERXWOTemplate) {
		        		ERXWOTemplate template = (ERXWOTemplate)current;
		        		String name = template.templateName(component);
		        		if(name.equals(templateName)) {
		        			result = template;
		        		}
		        	}
		        }
			}
		} else if (content instanceof ERXWOTemplate) {
			ERXWOTemplate template = (ERXWOTemplate) content;
    		String name = template.templateName(component);
    		if(name.equals(templateName)) {
    			result = template;
    		}
		} else if (content instanceof WOHTMLBareString && templateName == null) {
			result=content;
		}
    	return result;
    }

    @Override
    public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
    	WOComponent component = wocontext.component();
    	WOElement template = template(component);
    	if(template != null) {
    		wocontext._setCurrentComponent(component.parent());
   			template.takeValuesFromRequest(worequest, wocontext);
    		wocontext._setCurrentComponent(component);
    	} else {
    		_defaultTemplate.takeValuesFromRequest(worequest, wocontext);
    	}
    }

    @Override
    public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
    	WOComponent component = wocontext.component();
    	WOElement template = template(component);
    	WOActionResults result;
    	if(template != null) {
    		wocontext._setCurrentComponent(component.parent());
    		result = template.invokeAction(worequest, wocontext);
    		wocontext._setCurrentComponent(component);
    	} else {
    		result = _defaultTemplate.invokeAction(worequest, wocontext);
    	}
    	return result;
    }

    @Override
    public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
        WOComponent component = wocontext.component();
        WOElement template = template(component);
        if(template != null) {
        	wocontext._setCurrentComponent(component.parent());
        	template.appendToResponse(woresponse, wocontext);
        	wocontext._setCurrentComponent(component);
        } else {
        	_defaultTemplate.appendToResponse(woresponse, wocontext);
        }
    }

    @Override
    public String toString() {
        return "<" + getClass().getName() + "@" + System.identityHashCode(this) + " : " + _templateName  + ">";
    }
}
