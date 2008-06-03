package er.yui;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxUtils;

/**
 * YUIShowPanelLink generates either an hyperlink or an input button that 
 * displays a particular panel.
 *
 * @binding panelID (required) the id of the panel to display
 * @binding type "button" or "link", defaults to link
 * @binding id the id of this link or button
 * @binding class the css class of this link or button
 * 
 * @author mschrag
 */
public class YUIShowPanelLink extends WODynamicGroup {
  private NSDictionary _associations;
  
  public YUIShowPanelLink(String name, NSDictionary associations, WOElement template) {
    super(name, associations, template);
    _associations = associations;
  }

  public void appendToResponse(WOResponse response, WOContext context) {
    String id = AjaxUtils.stringValueForBinding("panelID", _associations, context.component());
    String varName = YUIUtils.varName(id, _associations, context);
    String type = AjaxUtils.stringValueForBinding("type", _associations, context.component());
    String showScript = varName + ".render();" + varName + ".show()";
    if ("button".equals(type)) {
      response.appendContentString("<input");
      YUIUtils.appendAttributeValue(response, context, "type", "button");
      YUIUtils.appendAttributeValue(response, context, "onClick", showScript);
      YUIUtils.appendAttributeValue(response, context, _associations, "id");
      YUIUtils.appendAttributeValue(response, context, _associations, "class");
      response.appendContentString(">");
      super.appendToResponse(response, context);
      response.appendContentString("</input>");
    }
    else {
      response.appendContentString("<a");
      YUIUtils.appendAttributeValue(response, context, "href", "#");
      YUIUtils.appendAttributeValue(response, context, "onClick", showScript);
      YUIUtils.appendAttributeValue(response, context, _associations, "id");
      YUIUtils.appendAttributeValue(response, context, _associations, "class");
      response.appendContentString(">");
      super.appendToResponse(response, context);
      response.appendContentString("</a>");
    }
  }

}
