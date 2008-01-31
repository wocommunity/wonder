package er.wolips.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.extensions.ERXComponentUtilities;

/**
 * WOLToolBar renders a collapsing toolbar at the bottom of your 
 * page containing useful debugging tools that integrate with 
 * WOLips.
 * 
 * @author mschrag
 * @binding expanded if true, the toolbar is expanded by default; defaults to false 
 */
public class WOLToolBar extends WOComponent {
  public WOLToolBar(WOContext context) {
    super(context);
  }
  
  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }
  
  public String style() {
    String style = null;
    if (!ERXComponentUtilities.booleanValueForBinding(this, "expanded", false)) {
      style = "display: none";
    }
    return style;
  }
}