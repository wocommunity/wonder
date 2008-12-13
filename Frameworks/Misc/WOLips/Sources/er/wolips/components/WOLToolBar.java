package er.wolips.components;

import java.lang.reflect.Method;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSLog;

import er.extensions.components.ERXComponentUtilities;

/**
 * WOLToolBar renders a collapsing toolbar at the bottom of your 
 * page containing useful debugging tools that integrate with 
 * WOLips.
 * 
 * @author mschrag
 * @binding expanded if true, the toolbar is expanded by default; defaults to false 
 */
public class WOLToolBar extends WOComponent {
  private boolean _debugEnabled;

  public WOLToolBar(WOContext context) {
    super(context);
  }

  @Override
  public boolean isStateless() {
    return true;
  }

  public String style() {
    String style = null;
    if (!ERXComponentUtilities.booleanValueForBinding(this, "expanded", false)) {
      style = "display: none";
    }
    return style;
  }

  public boolean isDebugEnabled() {
    return _debugEnabled;
  }
  
  public WOActionResults toggleDebugEnabled() {
    try {
      WOApplication application = WOApplication.application();
      Method setDebugMethod = application.getClass().getMethod("setDebugEnabledForComponent", boolean.class, WOComponent.class);
      _debugEnabled = !_debugEnabled;
      setDebugMethod.invoke(application, _debugEnabled, context().page());
    }
    catch (Throwable t) {
      NSLog.debug.appendln("Your application does not have a setDebugEnabledForComopnent(boolean, WOComponent) method.");
    }
    return context().page();
  }
}