package er.yui;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxOption;
import er.ajax.AjaxOptions;
import er.ajax.AjaxUtils;
import er.extensions.components.ERXComponentUtilities;

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
public class YUIPanel extends YUIDivContainer {
  public YUIPanel(String name, NSDictionary associations, WOElement template) {
    super(name, associations, template);
  }

  protected String divID(WOContext context) {
    return YUIUtils.id("id", associations(), context);
  }

  protected String divClass(WOContext context) {
    return null;
  }

  protected String divStyle(WOContext context) {
    String style = ERXComponentUtilities.stringValueForBinding("style", associations(), context.component());
    StringBuffer styleBuffer = new StringBuffer();
    styleBuffer.append("visibility: hidden; ");
    if (style != null) {
      styleBuffer.append(style);
    }
    return styleBuffer.toString();
  }

  protected NSDictionary createYUIOptions(WOContext context) {
    WOComponent component = context.component();
    NSMutableArray ajaxOptionsArray = new NSMutableArray();
    ajaxOptionsArray.addObject(new AjaxOption("close", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("constraintoviewport", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("context", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("draggable", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("effect", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("fixedcenter", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("height", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("iframe", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("keylisteners", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("modal", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("monitorresize", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("underlay", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("visible", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("width", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("x", AjaxOption.NUMBER));
    ajaxOptionsArray.addObject(new AjaxOption("xy", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("y", AjaxOption.NUMBER));
    ajaxOptionsArray.addObject(new AjaxOption("zIndex", AjaxOption.NUMBER));
    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
    return options;
  }

  public void appendToResponse(WOResponse response, WOContext context) {
    YUIUtils.addScriptResourceInHead(context, response, "yahoo/yahoo.js");
    YUIUtils.addScriptResourceInHead(context, response, "dom/dom.js");
    YUIUtils.addScriptResourceInHead(context, response, "event/event.js");
    YUIUtils.addScriptResourceInHead(context, response, "animation/animation.js");
    YUIUtils.addScriptResourceInHead(context, response, "dragdrop/dragdrop.js");
    YUIUtils.addScriptResourceInHead(context, response, "container/container.js");
    YUIUtils.addStylesheetResourceInHead(context, response, "container/assets/container.css");

    super.appendToResponse(response, context);

    AjaxUtils.appendScriptHeader(response);
    String id = divID(context);
    String varName = YUIUtils.varName(id, associations(), context);
    response.appendContentString("var " + varName + " = new YAHOO.widget.Panel(\"" + id + "\",");
    AjaxOptions.appendToResponse(createYUIOptions(context), response, context);
    response.appendContentString(");");
    response.appendContentString("\n");
    // response.appendContentString(varName + ".render();");
    response.appendContentString("\n");
    AjaxUtils.appendScriptFooter(response);
  }
}
