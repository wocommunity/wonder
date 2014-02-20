package er.ajax;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXWOContext;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;

// PROTOTYPE FUNCTIONS (WRAPPER)
/**
 * Autocompleting combo-box similar to Google suggest.<br/>
 * 
 * This is a component that look like a text field, where when you start
 * entering value, it start giving you a menu of options related to what you
 * type. Think about the auto-completion feature of many IDE (XCode / Eclipse)
 * inside a textField.<br/> <br/> 
 * The scriptaculous library has 2 version of the autocompleter combo-box : 
 * a local version and an ajax version.
 * 
 * <h3>Local</h3>
 * The local version hold the list of values all in memory (client-side), there
 * is no interaction. If the number of elements is big enough to be in a
 * WOPopUP, then this variant is well suited for you. If the list of element to
 * show is too big, then you might prefer the 'ajax' version.<br/> You have to
 * tell the component that it is local (by default it is 'ajax' type) using the
 * <code>isLocal</code> binding. Then the <code>list</code> binding will
 * need to provide all the objects needed to be found. Filtering of the list as
 * you type will be done client-side, all javascript.
 * 
 * <h3>Ajax</h3>
 * Autocomplete field similar to what google has. You bind a value and a method
 * that returns a list and it hits the server on each keystroke and displays the
 * results.
 * 
 * @binding list bound to a method that should return the whole list of object
 *          to be displayed. When used in an Ajax context, the component will
 *          push first to the <cite>value</cite> binding, giving you the chance
 *          to narrow the list of elements displayed. When used in a Local
 *          context, the list should contain all possible objects. the list will
 *          be filtered by the scriptaculous engine.
 * @binding value string that will hold the text entered in the field. It is
 *          continuously updated.
 * @binding item pushed and pulled the current element of the list. This can be
 *          used to customized the string representation (in conjunction with the
 *          <cite>displayString</cite> binding) of the object.
 * @binding displayString optional custom string representation of the current
 *          element.
 * @binding isLocal boolean indicating if you want the list to be completely
 *          client-side. Binding a true value, would mean that the list will
 *          filtered on the client.
 * @binding isLocalSharedList boolean indicating if the list needs to be shared.
 * @binding localSharedVarName the name of the javascript variable to use to 
 *          store the list in.  The list is stored in the userInfo dictionary
 *          on the server side to allow for shared use by multiple auto complete 
 *          components.
 * @binding token
 * @binding frequency Look at the scriptaculous documentation.
 * @binding minChars Look at the scriptaculous documentation.
 * @binding indicator Look at the scriptaculous documentation.
 * @binding updateElement Look at the scriptaculous documentation.
 * @binding afterUpdateElement Look at the scriptaculous documentation.
 * @binding select Look at the scriptaculous documentation.
 * @binding onShow Look at the scriptaculous documentation.
 * @binding fullSearch Look at the scriptaculous documentation.
 * @binding partialSearch Look at the scriptaculous documentation.
 * @binding choices Look at the scriptaculous documentation (Local only)
 * @binding partialChars Look at the scriptaculous documentation (Local only)
 * @binding ignoreCase Look at the scriptaculous documentation (Local only)
 * @binding accesskey hot key that should activate the text field (optional)
 * @binding tabindex tab index of the text field (optional)
 * @binding default hint for the text field, when used together with {@link AjaxTextHinter}. 
 * @binding selection if set, if the text field's string matches the displayString of one of the objects in the provided list, that object will be bound back as the selection. currently this only supports displayString renderers and not child templates
 * @binding class class attribute of the text field
 * @binding style class attribute of the text field
 * @binding onblur onblur attribute of the text field
 * @binding onfocus onfocus attribute of the text field
 * @binding onchange onchange attribute of the text field
 * @binding activateOnFocus activate when text field gets focus
 * @binding containerId tag id for the container of the popup div (default is body)
 * @author ak
 */
public class AjaxAutoComplete extends AjaxComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public String divName;
    public String fieldName;
    public String indicatorName;

    public AjaxAutoComplete(WOContext context) {
        super(context);
    }
    
    /**
     * Overridden to set the IDs for the field and the div tag.
     */
    @Override
    public void awake() {
        super.awake();
        divName = safeElementID() + "_div";
        fieldName = safeElementID() + "_field";
        indicatorName = safeElementID() + "_indicator";
    }
    
    @Override
    public void sleep() {
    	divName = null;
    	fieldName = null;
    	indicatorName = null;
    	super.sleep();
    }

    /**
     * Overridden because the component is stateless
     */
    @Override
    public boolean isStateless() {
        return true;
    }

    public String indicator() {
    	String indicator = (String)valueForBinding("indicator");
    	if (indicator == null && valueForBinding("indicatorFilename") != null) {
    		indicator = "'" + indicatorName + "'";
    	}
    	return indicator;
    }
    
    protected NSDictionary createAjaxOptions() {
      NSMutableArray ajaxOptionsArray = new NSMutableArray();
      ajaxOptionsArray.addObject(new AjaxOption("tokens", AjaxOption.STRING_ARRAY));
      ajaxOptionsArray.addObject(new AjaxOption("frequency", AjaxOption.NUMBER));
      ajaxOptionsArray.addObject(new AjaxOption("minChars", AjaxOption.NUMBER));
      ajaxOptionsArray.addObject(new AjaxOption("indicator", indicator(), AjaxOption.SCRIPT));
      ajaxOptionsArray.addObject(new AjaxOption("updateElement", AjaxOption.SCRIPT));
      ajaxOptionsArray.addObject(new AjaxOption("afterUpdateElement", AjaxOption.SCRIPT));
      ajaxOptionsArray.addObject(new AjaxOption("onShow", AjaxOption.SCRIPT));
      ajaxOptionsArray.addObject(new AjaxOption("fullSearch", AjaxOption.BOOLEAN));
      ajaxOptionsArray.addObject(new AjaxOption("partialSearch", AjaxOption.BOOLEAN));
      ajaxOptionsArray.addObject(new AjaxOption("defaultValue", AjaxOption.STRING));
      ajaxOptionsArray.addObject(new AjaxOption("select", AjaxOption.STRING));
      ajaxOptionsArray.addObject(new AjaxOption("autoSelect", AjaxOption.BOOLEAN));
      ajaxOptionsArray.addObject(new AjaxOption("choices", AjaxOption.NUMBER));
      ajaxOptionsArray.addObject(new AjaxOption("partialChars", AjaxOption.NUMBER));
      ajaxOptionsArray.addObject(new AjaxOption("ignoreCase", AjaxOption.BOOLEAN));
      ajaxOptionsArray.addObject(new AjaxOption("activateOnFocus", AjaxOption.BOOLEAN));
      NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);
      return options;
    }
   
    /**
     * Overridden to add the initialization javascript for the auto completer.
     */
    @Override
    public void appendToResponse(WOResponse res, WOContext ctx) {
        super.appendToResponse(res, ctx);
		boolean isDisabled = hasBinding("disabled") && ((Boolean) valueForBinding("disabled")).booleanValue();
		if ( !isDisabled ) {
			boolean isLocal = hasBinding("isLocal") && ((Boolean) valueForBinding("isLocal")).booleanValue();
			if (isLocal) {
				StringBuffer str = new StringBuffer();
				boolean isLocalSharedList = hasBinding("isLocalSharedList") && ((Boolean) valueForBinding("isLocalSharedList")).booleanValue();
				String listJS = null;
				if (isLocalSharedList) {
					String varName = (String) valueForBinding("localSharedVarName");
					NSMutableDictionary userInfo = ERXWOContext.contextDictionary();
					if (userInfo.objectForKey(varName) == null) {
						String ljs = listeJS();
						AjaxUtils.addScriptCodeInHead(res, ctx, "var " + varName + " = " + ljs + ";");
						userInfo.setObjectForKey(ljs, varName);
					}
					listJS = varName;
				} else {
					listJS = listeJS();
				}
				str.append("<script type=\"text/javascript\">\n// <![CDATA[\n");
				str.append("new Autocompleter.Local('");
				str.append(fieldName);
				str.append("','");
				str.append(divName);
				str.append("',");
				str.append(listJS);
				str.append(',');
				AjaxOptions.appendToBuffer(createAjaxOptions(), str, ctx);
				str.append(");\n// ]]>\n</script>\n");
				res.appendContentString(String.valueOf(str));
			} else {
				String actionUrl = AjaxUtils.ajaxComponentActionUrl(ctx);
				AjaxUtils.appendScriptHeader(res);
				res.appendContentString("new Ajax.Autocompleter('"+fieldName+"', '"+divName+"', '"+actionUrl+"', ");
				AjaxOptions.appendToResponse(createAjaxOptions(), res, ctx);
				res.appendContentString(");");
				AjaxUtils.appendScriptFooter(res);
			}
		}
    }

	String listeJS() {
		StringBuilder str = new StringBuilder();
		str.append("new Array(");
		NSArray list = (NSArray) valueForBinding("list");
		int max = list.count();
		String cnt = "";
		boolean hasItem = hasBinding("item");
		for (int i = 0; i < max; i++) {
			Object ds = list.objectAtIndex(i);
			if (i > 0) {
				str.append(',');
			}
			str.append("\n\"");
			if (hasItem) {
				setValueForBinding(ds, "item");
			}
			Object displayValue = valueForBinding("displayString", valueForBinding("item", ds));
			str.append(displayValue.toString());
			// TODO: We should escape the javascript string delimiter (") to keep the javascript interpreter happy.
			//str.append(displayValue.toString().replaceAll("\"", "\\\\\\\\\"")); // doesn't work
			str.append(cnt);
			str.append("\"");
		}
		str.append(')');
		return str.toString();
	}		

    /**
     * Adds all required resources.
     */
    @Override
    protected void addRequiredWebResources(WOResponse res) {
		boolean isDisabled = hasBinding("disabled") && ((Boolean) valueForBinding("disabled")).booleanValue();
		if ( !isDisabled ) {
			addScriptResourceInHead(res, "prototype.js");
			addScriptResourceInHead(res, "effects.js");
			addScriptResourceInHead(res, "controls.js");
			addScriptResourceInHead(res, "wonder.js");
		}
    }
    
	public String stringValue() {
		String strValue = null;
		if (hasBinding("selection")) {
			Object selection = valueForBinding("selection");
			if (selection != null) {
				if (hasBinding("displayString")) {
					setValueForBinding(selection, "item");
					strValue = displayStringForValue(valueForBinding("value"));
				}
				else {
					strValue = String.valueOf(selection);
				}
			}
			else
				strValue = (String) valueForBinding("value");
		}
		else if (hasBinding("value")) {
			strValue = (String) valueForBinding("value");
		}
		return strValue;
	}
    
    protected String displayStringForValue(Object value) {
    	Object displayValue = valueForBinding("displayString", valueForBinding("item", value));
    	String displayString = displayValue == null ? null : displayValue.toString();
    	return displayString;
    }
    
    protected int maxItems() {
        int maxItems = ERXValueUtilities.intValueWithDefault(valueForBinding("maxItems"), 50);
        return maxItems;
    }
    
    public void setStringValue(String strValue) {
    	if (hasBinding("selection")) {
            Object selection = null;
            if (strValue != null) {
	    		NSArray values = (NSArray) valueForBinding("list");
		        int maxItems = maxItems();
		        int itemsCount = 0;
		        for(Enumeration e = values.objectEnumerator(); e.hasMoreElements() && itemsCount++ < maxItems;) {
		            Object value = e.nextElement();
	                setValueForBinding(value, "item");
	            	String displayString = displayStringForValue(value);
	            	if (ERXStringUtilities.stringEqualsString(displayString, strValue)) {
	            		selection = value;
	            		break;
	            	}
		        }
            }
        	setValueForBinding(selection, "selection");
    	}
    	setValueForBinding(strValue, "value");
    }
    
    protected void appendItemToResponse(Object value, WOElement child, boolean hasItem, WOResponse response, WOContext context) {
        response.appendContentString("<li>");
        if(hasItem && child != null) {
            setValueForBinding(value, "item");
            context._setCurrentComponent(parent());
            child.appendToResponse(response, context);
            context._setCurrentComponent(this);
        } else {
        	if(hasItem) {
                setValueForBinding(value, "item");
         	}
            response.appendContentString(displayStringForValue(value));
        }
        response.appendContentString("</li>");
    }
    
    /**
     * Handles the Ajax request. Checks for the form value in the edit field,
     * pushes it up to the parent and pulls the "list" binding. The parent is
     * responsible for returning a list with some items that match the current value.
     */
    @Override
     public WOActionResults handleRequest(WORequest request, WOContext context) {
        // String inputString = request.contentString();
        String fieldValue = context.request().stringFormValueForKey(fieldName);
        setValueForBinding(fieldValue, "value");
        
        WOResponse response = AjaxUtils.createResponse(request, context);
        response.appendContentString("<ul>");
        
        int maxItems = maxItems();
        int itemsCount = 0;
        Object values = valueForBinding("list");
        WOElement child = _childTemplate();
        boolean hasItem = hasBinding("item");
        if (values instanceof NSArray) {
	        for(Enumeration valueEnum = ((NSArray)values).objectEnumerator(); valueEnum.hasMoreElements() && itemsCount++ < maxItems;) {
	        	appendItemToResponse(valueEnum.nextElement(), child, hasItem, response, context);
	        }
        }
        else if (values instanceof List) {
	        for(Iterator iter = ((List)values).iterator(); iter.hasNext() && itemsCount++ < maxItems;) {
	        	appendItemToResponse(iter.next(), child, hasItem, response, context);
	        }
        }
        response.appendContentString("</ul>");
        return response;
     }

	public String zcontainerName() {
		return "ZContainer" + divName;
	}
}
