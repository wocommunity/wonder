package er.yui;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxUtils;

/**
 * Generates a YUI class = "bd" div.
 * 
 * @binding id the id of the div
 * @binding style the style of the div
 * 
 * @author mschrag
 */
public class YUIBody extends YUIDivContainer {
  public YUIBody(String aName, NSDictionary associations, WOElement template) {
    super(aName, associations, template);
  }

  protected String divID(WOContext context) {
    return AjaxUtils.stringValueForBinding("id", associations(), context.component());
  }

  protected String divClass(WOContext context) {
    return "bd";
  }

  protected String divStyle(WOContext context) {
    return AjaxUtils.stringValueForBinding("style", associations(), context.component());
  }
}
