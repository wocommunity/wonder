package er.modern.directtoweb.components.buttons;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.directtoweb.SelectPageInterface;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.ERDirectToWeb;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;

/**
 * Inspect button for repetitions
 * 
 * @binding object
 * 
 * @d2wKey inspectConfigurationName
 * @d2wKey classForInspectObjButton
 * @d2wKey inspectButtonLabel
 * 
 * @author davidleber
 */
public class ERMDInspectButton extends ERMDActionButton {
	
	public interface Keys extends ERMDActionButton.Keys {
		public static final String inspectButtonLabel = "inspectButtonLabel";
		public static final String classForInspectObjButton = "classForInspectObjButton";
		public static final String inspectConfigurationName = "inspectConfigurationName";
	}
	
    public ERMDInspectButton(WOContext context) {
        super(context);
    }

    /**
     * Label for the inspect button
     * <p>
     * Defaults to "Inspect"
     */
	public String buttonLabel() {
		if (_buttonLabel == null) {
			_buttonLabel = stringValueForBinding(Keys.inspectButtonLabel, "Inspect");
		}
		return _buttonLabel;
	}
    
	/**
	 * CSS class for inspect button
	 * <p>
	 * Defaults to "Button ObjButton InspectObjButton"
	 */
	public String buttonClass() {
		if (_buttonClass == null) {
			_buttonClass = stringValueForBinding(Keys.classForInspectObjButton, "Button ObjButton InspectObjButton");
		}
		return _buttonClass;
	}
	
	/**
	 * Action for inspect button
	 */
	public WOActionResults inspectObjectAction() {
		WOActionResults result = null;
		if (shouldAllowInlineEditing()) {
			EOEditingContext ec = ERXEC.newEditingContext(object().editingContext());
			EOEnterpriseObject localObj = ERXEOControlUtilities.localInstanceOfObject(ec, object());
			SelectPageInterface parent = parentSelectPage();
	        if(parent != null) {
	        	d2wContext().takeValueForKey("inspect", Keys.inlineTask);
	            parent.setSelectedObject(localObj);
	        } else {
	        	throw new IllegalStateException("This page is not an instance of SelectPageInterface. I can't select here.");
	        }
		} else {
			result = inspectObjectInPageAction();
		}
		return result;
	}
	
	/**
	 * Action for inspect button if in-line editing is disabled
	 */
	@SuppressWarnings("unchecked")
    public WOActionResults inspectObjectInPageAction() {
    	String currentPageConfiguration = stringValueForBinding(Keys.pageConfiguration);
		
		NSDictionary extraValues = currentPageConfiguration != null ? new NSDictionary(currentPageConfiguration, Keys.pageConfiguration) : null;
        String configuration = (String)ERDirectToWeb.d2wContextValueForKey(Keys.inspectConfigurationName, object().entityName(), extraValues);
        
        InspectPageInterface epi = (InspectPageInterface) D2W.factory().pageForConfigurationNamed(configuration, session());
        epi.setNextPage(context().page());
        epi.setObject(object());
        return (WOActionResults)epi;
    }
}
