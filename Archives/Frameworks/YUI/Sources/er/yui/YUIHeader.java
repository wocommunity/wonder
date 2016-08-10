package er.yui;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

import er.extensions.components.ERXComponentUtilities;

/**
 * Generates a YUI class = "hd" div.
 * 
 * @binding id the id of the div
 * @binding style the style of the div
 * 
 * @author mschrag
 */
public class YUIHeader extends YUIDivContainer {
  public YUIHeader(String aName, NSDictionary associations, WOElement template) {
    super(aName, associations, template);
  }

  protected String divID(WOContext context) {
    return ERXComponentUtilities.stringValueForBinding("id", associations(), context.component());
  }

  protected String divClass(WOContext context) {
    return "hd";
  }

  protected String divStyle(WOContext context) {
    return ERXComponentUtilities.stringValueForBinding("style", associations(), context.component());
  }
}
