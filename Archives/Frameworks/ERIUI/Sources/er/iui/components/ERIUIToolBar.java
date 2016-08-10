package er.iui.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

/**
 *
 * @binding backAction
 * @binding title
 */
public class ERIUIToolBar extends ERXComponent {
  public ERIUIToolBar(WOContext context) {
    super(context);
  }
  
  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }
}
