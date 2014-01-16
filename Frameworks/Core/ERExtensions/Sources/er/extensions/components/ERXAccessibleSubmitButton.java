package er.extensions.components;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.components._private.ERXSubmitButton;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Extends ERXSubmitButton with self-configuring accessibility hot key for the
 * button. The default hot key is the first character. If that has been used by
 * a button that appeared earlier on the page, the next choice is a capital
 * character. If no non-conflicting capital characters are available, it selects
 * the first character that does not conflict with an earlier button. If you
 * want a different hot key, specify it in the accesskey binding. Note that if
 * this conflicts with the self configured choice for a button that appeared
 * earlier on the page that this conflict will not be resolved. You will have to
 * define an accesskey for the conflicting button as well. If you don't want a
 * hot key, bind accesskey to the empty string.
 * 
 * <p>
 * The default is to use the u (underline) element to identify the hot key in
 * the button text. Use the accesskeyElement binding to specify a different
 * element. The element can be decorated with other attributes, e.g.
 * 
 * <pre>
 * accesskeyElement = &quot;span style='text-decoration: underline;'&quot;;
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * You can have this class replace WOSubmitButton via
 * ERXPatcher.setClassForName(ERXAccessibleSubmitButton.class,
 * "WOSubmitButton"); or use it explicitly by name in your WOD. It works best
 * (does the most work for you) if you use it with the value attribute. No self
 * configuring is done if there is any content between the open and close tags.
 * </p>
 * 
 * @binding accesskey optional key for hot key, "" to disable hot key
 * @binding accesskeyElement optional element name and decoration to wrap hot
 *          key character with in the button text
 * @binding value the button text
 * 
 * @see ERXSubmitButton
 * 
 * @author chill
 */
public class ERXAccessibleSubmitButton  extends ERXSubmitButton {
	
    protected WOAssociation _accesskey;
    protected WOAssociation _accesskeyElement;
    
   
	public ERXAccessibleSubmitButton(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
		super(name, associations, template);

        _accesskey = _associations.removeObjectForKey("accesskey");
        _accesskeyElement = _associations.removeObjectForKey("accesskeyElement");
        if(_accesskeyElement == null) {
        	_accesskeyElement = new WOConstantValueAssociation("u");
        }
	}

	/**
	 * @param component WOComponent to evaluate the associations in
	 * @return the character to use for the button's accesskey attribute, or null if there will not be an accesskey
	 */
	protected String accesskey(WOComponent component) {
		String accessKey = null;
		
		// If accesskey is unbound or bound to null, determine it from the value binding
		if (_accesskey == null || _accesskey.valueInComponent(component) == null) {
			String value = (String)_value.valueInComponent(component);
			if ( ! ERXStringUtilities.stringIsNullOrEmpty(value)) {
				
				// Preference is for the first character in the button text
				// This CAN conflict with explicitly set hot keys
				accessKey = value.substring(0, 1);
				
				// If that character has been used, then try any capitalized characters in order
				if (hasUsedHotKey(component, accessKey)) {
					accessKey = null;
					for (int i = 1; accessKey == null && i < value.length(); i++) {
						if (Character.isUpperCase(value.charAt(i)) && ! hasUsedHotKey(component, value.substring(i, i+1))) {
							accessKey = value.substring(i, i+1);
						}
					}
					
					// If all the capitals have been used (or there aren't any more), then pick the first unused character
					for (int i = 1; accessKey == null && i < value.length(); i++) {
						if (! hasUsedHotKey(component, value.substring(i, i+1))) {
							accessKey = value.substring(i, i+1);
						}
					}
				}
			}
		} else {
			// Binding accesskey to "" indicates that there should not be a hot key
			accessKey = (String)_accesskey.valueInComponent(component);
			accessKey = "".equals(accessKey) ? null : accessKey;
		}
		
		return accessKey;
	}
	
	/**
	 * @param component WOComponent to evaluate the associations in
	 * @return element name and decoration to wrap hot key character with in the button text
	 */
	protected String accesskeyElement(WOComponent component) {
		return (String) _accesskeyElement.valueInComponent(component);
	}
	
	/**
	 * @param component WOComponent to evaluate the associations in
	 * @return <code>true</code> if accesskey should not be generated
	 */
	protected boolean isDisabled(WOComponent component) {
		return accesskey(component) == null;
	}
	
	/**
	 * @param component WOComponent to evaluate the associations in
	 * @return String from the value binding with the accesskey wrapped in accesskeyElement
	 */
	protected String styledValue(WOComponent component) {
		String value = (String)_value.valueInComponent(component);
		String accessKey = accesskey(component);
		if (accessKey == null) {
			return value;
		}
		
		String accesskeyElement = accesskeyElement(component);
		int index = value.indexOf(accessKey);
		if (index != -1) {
			StringBuilder sb = new StringBuilder();
			sb.append(value.substring(0, index));
			sb.append('<');
			sb.append(accesskeyElement);
			sb.append('>');
			sb.append(accessKey);
			sb.append("</");
			int coIndex = accesskeyElement.indexOf(' ');
			sb.append(coIndex == -1 ? accesskeyElement : accesskeyElement.substring(0, coIndex));
			sb.append('>');
			sb.append(value.substring(index + 1));
			value = sb.toString();
		}
		return value;
	}
	
	/**
	 * Records the accesskey for this button so other buttons on the page won't use it.
	 *
	 * @see er.extensions.components._private.ERXSubmitButton#appendToResponse(com.webobjects.appserver.WOResponse, com.webobjects.appserver.WOContext)
	 */
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		if (accesskey(context.component()) != null) {
			recordUsedHotKey(context.component(), accesskey(context.component()));
		}
	}
	
	/**
	 * Adds the accesskey binding as we took it out of the associations dictionary
	 *
	 * @see er.extensions.components._private.ERXSubmitButton#appendAttributesToResponse(com.webobjects.appserver.WOResponse, com.webobjects.appserver.WOContext)
	 */
    @Override
    public void appendAttributesToResponse(WOResponse response, WOContext context) {
    	super.appendAttributesToResponse(response, context);
    	response._appendTagAttributeAndValue("accesskey", accesskey(context.component()), false);
    }
	
    /**
     * Adds styledValue between the open and close tags
     *
     * @see er.extensions.components._private.ERXSubmitButton#appendChildrenToResponse(com.webobjects.appserver.WOResponse, com.webobjects.appserver.WOContext)
     */
    @Override
    public void appendChildrenToResponse(WOResponse response, WOContext context) {
        if(hasChildrenElements()) {
            super.appendChildrenToResponse(response, context);
        } else {
            response.appendContentString(styledValue(context.component()));
        }
    }
    
    /**
	 * @param component WOComponent to evaluate the associations in
     * @param hotKey the character to check
     * @return <code>true</code> if hotKey has been used by another ERXAccessibleSubmitButton on this page
     */
    protected boolean hasUsedHotKey(WOComponent component, String hotKey) {
    	
    	return usedHotKeys(component).containsObject(hotKey);
    	
    }

    /**
     * Records that hotKey is being used by a ERXAccessibleSubmitButton on this page.
	 * @param component WOComponent to evaluate the associations in
     * @param hotKey the character to record
     */
    protected void recordUsedHotKey(WOComponent component, String hotKey) {
    	usedHotKeys(component).addObject(hotKey);
    }
    
    /**
     * @param component WOComponent to evaluate the associations in
     * @return NSMutableArray containing the hotKeys being used by ERXAccessibleSubmitButtons on this page 
     */
    protected NSMutableArray usedHotKeys(WOComponent component) {
    	WOResponse response = component.context().response();
    	NSDictionary userInfo = response.userInfo();
    	if (userInfo == null) {
    		userInfo = new NSMutableDictionary(new NSMutableArray(), ERXAccessibleSubmitButton.class.getName());
    		response.setUserInfo(userInfo);
    	}
    	
    	NSMutableArray usedHotKeys = (NSMutableArray)userInfo.objectForKey(ERXAccessibleSubmitButton.class.getName());
    	if (usedHotKeys == null) {
    		userInfo = userInfo.mutableClone();
    		usedHotKeys = new NSMutableArray();
    		((NSMutableDictionary)userInfo).setObjectForKey(usedHotKeys, ERXAccessibleSubmitButton.class.getName());
    		response.setUserInfo(userInfo);
    	}
    	return (NSMutableArray)userInfo.objectForKey(ERXAccessibleSubmitButton.class.getName());
    }
    
    
}
