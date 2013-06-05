package er.ajax.mootools;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxOption;
import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXStatelessComponent;


/**
 * @binding id - the identifier for the select button and the JavaScript variable.
 * @binding useDefaultCss - If TRUE (default) it will load the default stylesheet.
 * @binding showText - If TRUE (default) keeps text in the dropdown menu.
 * @binding Unsupported operation showImages - If TRUE (default) show images in the dropdown menu.
 * TODO figure out how to use the data images drop down.	
 * @binding className: A class name for CSS styling, default 'fancy-select'. 
 * @binding autoHide: If TRUE auto-hide the dropdown menu when user clicks outside.
 * @binding autoScrollWindow: If TRUE auto-scroll browser window when FancySelect is out of viewport.
 * @binding animateFade: If TRUE (default) animate the dropdown menu appearance.
 * @binding fx: An object for additional Fx options (default {'duration': 'short'}).
 * @binding onShow: The dropdown menu appears.
 * @binding onHide: The dropdown menu disappears.
 * @binding onAttach: FancySelect just replaced the {@code <select>} DOM element.
 * @binding onDetach: The {@code <select>} DOM element is back.
 * @binding list: Array of objects from which the WOPopUpButton derives its values.
 * @binding item: Identifier for the elements of the list. For example, aCollege could represent an object in a colleges array.
 * @binding displayString: Value to display in the selection list; for example, aCollege.name for each college object in the list.
 * @binding value: For each OPTION tag within the selection, this is the "value" attribute (that is, {@code <OPTION value="someValue">}). You can use this binding to specify additional identifiers of each item in the menu.
 * @binding selection: Object that the user chose from the selection list. For the college example, selection would be a college object.
 * @binding selectedValue: Value that is used with direct actions to specify which option in the list is selected.
 * @binding name: Name that uniquely identifies this element within the form. You can specify a name or let WebObjects automatically assign one at runtime.
 * @binding disabled: If disabled evaluates to true, this element appears in the page but is not active. That is, selection does not contain the user's selection when the page is submitted. 
 * @binding escapeHTML: If escapeHTML evaluates to true, the string rendered by displayString is converted so that characters which would be interpreted as HTML control characters become their escaped equivalent (this is the default). Thus, if your displayString is "{@code a <b>bold</b> idea}", the string passed to the client browser would be "{@code a &lt;B&gt;bold&lt;/B&gt; idea}", but it would display in the browser as "{@code a <b>bold</b> idea}". If escapeHTML evaluates to false , WebObjects simply passes your data to the client browser "as is". In this case, the above example would display in the client browser as "a <b>bold</b> idea". If you are certain that your strings have no characters in them which might be interpreted as HTML control characters, you get better performance if you set escapeHTML to false.
 * @binding noSelectionString: Enables the first item to be "empty". Bind this attribute to a string (such as an empty string) that, if chosen, represents an empty selection. When this item is selected, the selection attribute is set to null.
 */
public class MTStyledPopUpButton extends ERXStatelessComponent {

	private static final long serialVersionUID = 1L;

	private String _id;
	private NSArray<Object> _list;
	
	public MTStyledPopUpButton(WOContext context) {
        super(context);
    }

    @Override
    public void reset() {
    	super.reset();
    	_id = null;
    }
	
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
        if(selectionIsDefined() && selectedValueIsDefined()) {
        	throw new IllegalArgumentException("You must specify either selection or selectedValue.");
        }
		MTAjaxUtils.addScriptResourceInHead(context, response, "MooTools", MTAjaxUtils.MOOTOOLS_CORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context, response, "MooTools", "scripts/plugins/fancyselect/FancySelect.js");
		if(useDefaultCss()) {
			AjaxUtils.addStylesheetResourceInHead(context, response, "MooTools", "scripts/plugins/fancyselect/FancySelect.css");
		}
		super.appendToResponse(response, context);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public NSDictionary createAjaxOptions() {
		
		NSMutableArray ajaxOptionsArray = new NSMutableArray();
		ajaxOptionsArray.addObject(new AjaxOption("showText", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("showImages", Boolean.FALSE, AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("className", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("autoHide", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("autoScrollWindow", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("animateFade", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("fx", AjaxOption.DICTIONARY));
		ajaxOptionsArray.addObject(new AjaxOption("onShow", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onHide", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("attach", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("detach", AjaxOption.SCRIPT));
		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);
		return options;

	}		
	
	public boolean useDefaultCss() {
		
		Boolean useDefaultCss = (Boolean)valueForBinding("useDefaultCss");
	
		if(useDefaultCss == null) {
			useDefaultCss = Boolean.TRUE;
		}
		
		return useDefaultCss.booleanValue();

	}
	
	public boolean selectionIsDefined() {
		return hasBinding("selection");
	}

	public boolean selectedValueIsDefined() {
		return hasBinding("selectedValue");
	}
	
	/**
	 * @return the id
	 */
	public String id() {

		if(_id == null) {
			_id = (String) valueForBinding("id");
		}
		
		if (_id == null) {
			_id = ERXWOContext.safeIdentifierName(context(), false);
		}
		
		return _id;
	
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		_id = id;
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	@SuppressWarnings("unchecked")
	public NSArray<Object> list() {
		if(_list == null) {
			_list = (NSArray<Object>)valueForBinding("list");
		}
		return _list;
	}
	
}