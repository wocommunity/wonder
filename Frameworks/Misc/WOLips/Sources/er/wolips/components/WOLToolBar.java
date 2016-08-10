package er.wolips.components;

import java.lang.reflect.Method;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSLog;

import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.ERXResponseRewriter;
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
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  private boolean _debugEnabled;

  public WOLToolBar(WOContext context) {
    super(context);
  }

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
	  super.appendToResponse(response, context);
	  if(ERXApplication.erxApplication().isDevelopmentMode()) {
		  ERXResponseRewriter.addStylesheetResourceInHead(response, context, "WOLips", "wolips.css");
	  }
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
  
  public String pageClassName() {
	  return context().page().getClass().getName();
  }
  
  public String pageSimpleName() {
	  return context().page().getClass().getSimpleName();
  }
  
  public WOActionResults toggleDebugEnabled() {
    try {
      WOApplication application = WOApplication.application();
      Method setDebugMethod = application.getClass().getMethod("setDebugEnabledForComponent", boolean.class, WOComponent.class);
      _debugEnabled = !_debugEnabled;
      setDebugMethod.invoke(application, Boolean.valueOf(_debugEnabled), context().page());
    }
    catch (Throwable t) {
      NSLog.debug.appendln("Your application does not have a setDebugEnabledForComopnent(boolean, WOComponent) method.");
    }
    return context().page();
  }
}