package er.yui;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxUtils;

/**
 * Generates a YUI class = "yui-b" div.
 * 
 * @binding id the id of the div
 * @binding style the style of the div
 * 
 * @author Mitchell Smith
 */
public class YUIBlock extends YUIDivContainer {
  public YUIBlock(String aName, NSDictionary<String, Object> associations, WOElement template) {
    super(aName, associations, template);
  }

  @Override
  protected String divID(WOContext context) {
    return AjaxUtils.stringValueForBinding("id", associations(), context.component());
  }

  @Override
  protected String divClass(WOContext context) {
    return "yui-b";
  }

  @Override
  protected String divStyle(WOContext context) {
    return AjaxUtils.stringValueForBinding("style", associations(), context.component());
  }

}
