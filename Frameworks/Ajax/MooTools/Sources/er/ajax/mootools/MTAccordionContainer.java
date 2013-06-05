package er.ajax.mootools;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxComponent;
import er.ajax.AjaxOption;
import er.extensions.appserver.ERXWOContext;

/**
 * 
 *	A DHTML accordion based on the Accordion Component found in MooTools: http://mootools.net/docs/more/Fx/Fx.Accordion
 *  The class name of the container that contains the label is always "toggler" and the class name of the container that holds the
 *  content is "elements".  If anyone has a suggestion on how to set this programatically please email me at jlmiller@kahalawai.com
 *  
 *  @binding display - (integer: defaults to 0) The index of the element to show at start (with a transition). To force all elements to be closed by default, pass in -1.
 *  @binding show - (integer: defaults to 0) The index of the element to be shown initially.
 *  @binding trigger - (string: defaults to 'click') The event that triggers a change in element display.
 *  @binding height - (boolean: defaults to true) If set to true, a height transition effect will take place when switching between displayed elements.
 *  @binding width - (boolean: defaults to false) If set to true, it will add a width transition to the accordion when switching between displayed elements. Warning: CSS mastery is required to make this work!
 *  @binding opacity - (boolean: defaults to true) If set to true, an opacity transition effect will take place when switching between displayed elements.
 *  @binding fixedHeight - (boolean: defaults to false) If set to true, displayed elements will have a fixed height.
 *  @binding fixedWidth - (boolean: defaults to false) If set to true, displayed elements will have a fixed width.
 *  @binding alwaysHide - (boolean: defaults to false) If set to true, it will be possible to close all displayable elements. Otherwise, one will remain open at all time.
 *  @binding initialDisplayFx - (boolean; defaults to true) If set to false, the initial item displayed will not display with an effect but will just be shown immediately.
 *  @binding resetHeight - (boolean; defaults to true) If set to false, the height of an opened accordion section will be set to an absolute pixel size.
 *	@binding onActive - (function: signature = function(toggler, element) {}) a method that is called on the container that is opened.
 *	@binding onBackground - (function: signature = function(toggler, element) {}) a method that is called on the container that is closed.
 *
 */
public class MTAccordionContainer extends AjaxComponent {

	private static final long serialVersionUID = 1L;

	private String _accordionID;
	
	public MTAccordionContainer(WOContext context) {
        super(context);
    }

	public String accordionID() {

		if(_accordionID == null) {
			_accordionID = (String) valueForBinding("id", ERXWOContext.safeIdentifierName(context(), true) + "Accordion");
		}
		
		return _accordionID;
	
	}

	@Override
	public boolean isStateless() {
		return true;
	}

	public String elementClassName() {
		return valueForStringBinding("elementClassName", "elements");
	}
	
	public String togglerClassName() {
		return valueForStringBinding("togglerClassName", "toggler");
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	public NSDictionary createAjaxOptions() {
		NSMutableArray ajaxOptionsArray = new NSMutableArray();
		ajaxOptionsArray.addObject(new AjaxOption("display", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("show", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("trigger", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("height", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("width", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("opacity", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("fixedHeight", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("fixedWidth", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("alwaysHide", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("initialDisplayFx", AjaxOption.BOOLEAN));	
		ajaxOptionsArray.addObject(new AjaxOption("resetHeight", AjaxOption.BOOLEAN));	
		ajaxOptionsArray.addObject(new AjaxOption("onActive", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onBackground", AjaxOption.SCRIPT));
		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);
		return options;
	}
	
	@Override
	protected void addRequiredWebResources(WOResponse res) {
		MTAjaxUtils.addScriptResourceInHead(context(), res, "MooTools", MTAjaxUtils.MOOTOOLS_CORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context(), res, "MooTools", MTAjaxUtils.MOOTOOLS_MORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context(), res, "MooTools", MTAjaxUtils.MOOTOOLS_WONDER_JS);
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}
}
