package er.wolips.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

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
    String style;
    Object expandedObj = valueForBinding("expanded");
    boolean expanded = ((expandedObj instanceof Boolean && ((Boolean)expandedObj).booleanValue()) || (expandedObj instanceof String && Boolean.valueOf((String)expandedObj).booleanValue()));
    if (expanded) {
      style = null;
    }
    else {
      style = "display: none";
    }
    return style;
  }
}