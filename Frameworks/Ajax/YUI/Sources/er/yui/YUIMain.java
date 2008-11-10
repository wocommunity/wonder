package er.yui;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

import er.yui.YUIDivContainer;

@SuppressWarnings("unused")
public class YUIMain extends YUIDivContainer {
  public YUIMain(String aName, NSDictionary<String, Object> associations, WOElement template) {
    super(aName, associations, template);
  }

  protected String divID(WOContext context) {
    return "yui-main";
  }

  protected String divClass(WOContext context) {
    return null;
  }

  protected String divStyle(WOContext context) {
    return null;
  }
}