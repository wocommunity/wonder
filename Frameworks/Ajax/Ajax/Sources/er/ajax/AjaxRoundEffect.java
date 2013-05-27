package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public class AjaxRoundEffect extends AjaxDynamicElement {
  private WOAssociation _elementNameAssociation;
  private WOAssociation _classAssociation;
  private WOAssociation _generateTagsAssociation;
  private WOAssociation _idAssociation;

  public AjaxRoundEffect(String name, NSDictionary associations, WOElement children) {
    super(name, associations, children);
    _elementNameAssociation = (WOAssociation) associations.objectForKey("elementName");
    if (_elementNameAssociation == null) {
      _elementNameAssociation = new WOConstantValueAssociation("div");
    }
    _classAssociation = (WOAssociation) associations.objectForKey("class");
    if (_classAssociation == null) {
      throw new WODynamicElementCreationException("'class' is a required binding.");
    }
    _generateTagsAssociation = (WOAssociation) associations.objectForKey("generateTags");
    if (_generateTagsAssociation == null) {
      _generateTagsAssociation = new WOConstantValueAssociation(Boolean.FALSE);
    }
    _idAssociation = (WOAssociation) associations.objectForKey("id");
  }

  @Override
  protected void addRequiredWebResources(WOResponse response, WOContext context) {
    addScriptResourceInHead(context, response, "prototype.js");
    addScriptResourceInHead(context, response, "rico.js");
  }

  @Override
  public WOActionResults handleRequest(WORequest request, WOContext context) {
    return null;
  }

  public NSDictionary createAjaxOptions(WOComponent component) {
    NSMutableArray ajaxOptionsArray = new NSMutableArray();
    ajaxOptionsArray.addObject(new AjaxOption("corners", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("color", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("bgColor", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("blend", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("border", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("compact", AjaxOption.BOOLEAN));
    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
    return options;
  }

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
    super.appendToResponse(response, context);
    WOComponent component = context.component();
    String className = (String) _classAssociation.valueInComponent(component);
    String elementName = (String) _elementNameAssociation.valueInComponent(component);
    boolean generateTags = ((Boolean) _generateTagsAssociation.valueInComponent(component)).booleanValue();
    if (generateTags) {
      elementName = "div";
      response.appendContentString("<");
      response.appendContentString(elementName);
      response.appendContentString(" class = \"");
      response.appendContentString(className);
      response.appendContentString("\"");
      if (_idAssociation != null) {
        response.appendContentString(" id = \"");
        String id = (String) _idAssociation.valueInComponent(component);
        response.appendContentString(id);
        response.appendContentString("\"");
      }
      response.appendContentString(">");
    }
    appendChildrenToResponse(response, context);
    if (generateTags) {
      response.appendContentString("\n</");
      response.appendContentString(elementName);
      response.appendContentString(">");
    }
    response.appendContentString("\n");
    AjaxUtils.appendScriptHeader(response);
    response.appendContentString("new Rico.Effect.Round('");
    response.appendContentString(elementName);
    response.appendContentString("', '");
    response.appendContentString(className);
    response.appendContentString("', ");
    NSDictionary options = createAjaxOptions(component);
    AjaxOptions.appendToResponse(options, response, context);
    response.appendContentString(");");
    AjaxUtils.appendScriptFooter(response);
  }
}