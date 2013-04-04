package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXWOContext;

public class AjaxAccordion extends AjaxComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  private String _accordionID;
  
  public AjaxAccordion(WOContext context) {
    super(context);
  }

  @Override
  public boolean isStateless() {
    return true;
  }

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
    _accordionID = (String) valueForBinding("id", ERXWOContext.safeIdentifierName(context, true) + "Accordion");
    super.appendToResponse(response, context);
  }

  public String accordionID() {
    return _accordionID;
  }

  public NSDictionary createAjaxOptions() {
    NSMutableArray ajaxOptionsArray = new NSMutableArray();
    ajaxOptionsArray.addObject(new AjaxOption("expandedBg", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("hoverBg", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("collapsedBg", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("expandedTextColor", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("expandedFontWeight", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("hoverTextColor", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("collapsedTextColor", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("collapsedFontWeight", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("hoverTextColor", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("borderColor", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("panelHeight", AjaxOption.NUMBER));
    ajaxOptionsArray.addObject(new AjaxOption("onHideTab", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("onShowTab", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("onLoadShowTab", AjaxOption.SCRIPT));
    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);
    return options;
  }

  @Override
  protected void addRequiredWebResources(WOResponse response) {
    addScriptResourceInHead(response, "prototype.js");
    addScriptResourceInHead(response, "rico.js");
  }

  @Override
  public WOActionResults handleRequest(WORequest request, WOContext context) {
    return null;
  }
}
