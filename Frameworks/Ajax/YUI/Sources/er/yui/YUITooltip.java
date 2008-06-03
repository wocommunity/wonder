package er.yui;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxOption;
import er.ajax.AjaxOptions;
import er.ajax.AjaxUtils;

/**
 * Generates a YUI panel (@see http://developer.yahoo.com/yui/container/panel/).
 *
 * @binding id the id of the panel
 * @binding class the css class of the panel
 * @binding style the style of the panel
 * 
 * @binding close refer to YUI docs
 * @binding constraintoviewport refer to YUI docs
 * @binding context refer to YUI docs
 * @binding draggable refer to YUI docs
 * @binding context refer to YUI docs
 * @binding effect refer to YUI docs
 * @binding fixedcenter refer to YUI docs
 * @binding height refer to YUI docs
 * @binding iframe refer to YUI docs
 * @binding keylisteners refer to YUI docs
 * @binding modal refer to YUI docs
 * @binding monitorresize refer to YUI docs
 * @binding underlay refer to YUI docs
 * @binding visible refer to YUI docs
 * @binding width refer to YUI docs
 * @binding x refer to YUI docs
 * @binding xy refer to YUI docs
 * @binding y refer to YUI docs
 * @binding zIndex refer to YUI docs
 * 
 * @author mschrag
 */
public class YUITooltip extends WODynamicGroup {
  private NSDictionary _associations;

  public YUITooltip(String name, NSDictionary associations, WOElement template) {
    super(name, associations, template);
    _associations = associations;
  }

  protected NSDictionary createYUIOptions(WOContext context) {
    WOComponent component = context.component();

    NSMutableArray ajaxOptionsArray = new NSMutableArray();
    ajaxOptionsArray.addObject(new AjaxOption("text", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("context", AjaxOption.STRING_OR_ARRAY));
    ajaxOptionsArray.addObject(new AjaxOption("container", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("preventoverlap", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("showdelay", AjaxOption.NUMBER));
    ajaxOptionsArray.addObject(new AjaxOption("hidedelay", AjaxOption.NUMBER));
    ajaxOptionsArray.addObject(new AjaxOption("autodismissdelay", AjaxOption.NUMBER));

    ajaxOptionsArray.addObject(new AjaxOption("constraintoviewport", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("effect", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("fixedcenter", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("height", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("iframe", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("monitorresize", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("visible", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("width", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("x", AjaxOption.NUMBER));
    ajaxOptionsArray.addObject(new AjaxOption("xy", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("y", AjaxOption.NUMBER));
    ajaxOptionsArray.addObject(new AjaxOption("zIndex", AjaxOption.NUMBER));

    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, _associations);
    if (options.objectForKey("text") == null && hasChildrenElements()) {
      WOResponse childrenResponse = WOApplication.application().createResponseInContext(context);
      super.appendToResponse(childrenResponse, context);
      String text = childrenResponse.contentString();
      text = text.replaceAll("\"", "&quot;");
      options.setObjectForKey("\"" + text + "\"", "text");
    }
    
    return options;
  }

  public void appendToResponse(WOResponse response, WOContext context) {
    YUIUtils.addScriptResourceInHead(context, response, "yahoo/yahoo.js");
    YUIUtils.addScriptResourceInHead(context, response, "dom/dom.js");
    YUIUtils.addScriptResourceInHead(context, response, "event/event.js");
    YUIUtils.addScriptResourceInHead(context, response, "container/container.js");
    YUIUtils.addStylesheetResourceInHead(context, response, "container/assets/container.css");

    AjaxUtils.appendScriptHeader(response);
    String id = YUIUtils.id("id", _associations, context);
    String varName = YUIUtils.varName(id, _associations, context);
    response.appendContentString(varName + " = new YAHOO.widget.Tooltip(\"" + id + "\",");
    AjaxOptions.appendToResponse(createYUIOptions(context), response, context);
    response.appendContentString(");");
    response.appendContentString("\n");
    AjaxUtils.appendScriptFooter(response);
  }
}
