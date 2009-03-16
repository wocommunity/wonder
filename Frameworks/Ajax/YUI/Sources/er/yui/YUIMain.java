package er.yui;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

/**
 * Generates a YUI class = "yui-main" div.
 * 
 * @binding id the id of the div
 * @binding style the style of the div
 * 
 * @author Mitchell Smith
 */
public class YUIMain extends YUIDivContainer {
  public YUIMain(String aName, NSDictionary<String, Object> associations, WOElement template) {
    super(aName, associations, template);
  }

  @Override
  protected String divID(WOContext context) {
    return "yui-main";
  }

  @Override
  protected String divClass(WOContext context) {
    return null;
  }

  @Override
  protected String divStyle(WOContext context) {
    return null;
  }
}