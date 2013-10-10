package er.wolips.components;

import java.lang.reflect.Method;
import java.net.MalformedURLException;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.foundation.ERXMutableURL;
import er.extensions.foundation.ERXProperties;
import er.wolips.WOLipsUtilities;

/**
 * WOLClickToOpen provide support for opening components on your page
 * based on metadata that is added by ERXComponent (or from your component
 * base class).
 * 
 * @property er.component.clickToOpen
 *
 * @author mschrag
 */
public class WOLClickToOpen extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  public WOLClickToOpen(WOContext context) {
    super(context);
  }

  @Override
  public boolean isStateless() {
    return true;
  }

  public boolean canClickToOpen() {
    return ERXProperties.booleanForKeyWithDefault("er.component.clickToOpen", false);
  }

  public String clickToOpenUrl() {
    String app = (String) valueForBinding("app");
    if (app == null) {
      app = NSBundle.mainBundle().name();
    }
    NSMutableDictionary params = new NSMutableDictionary();
    params.setObjectForKey(app, "app");
    params.setObjectForKey("REPLACEME", "component");
    return WOLipsUtilities.wolipsUrl("openComponent", params);
  }

  public String clickToDebugUrl() {
    try {
      ERXMutableURL url = new ERXMutableURL(context().componentActionURL());
      url.addQueryParameter("component", "REPLACEME");
      return url.toExternalForm();
    }
    catch (MalformedURLException e) {
      System.err.println("WOLClickToOpen.clickToDebugUrl failed to generate a URL: " + e.getMessage());
      return "http://broken";
    }
  }
  
  @Override
  public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
    if (wocontext.senderID() != null && wocontext.senderID().startsWith(wocontext.elementID())) {
      try {
        String componentName = worequest.stringFormValueForKey("component");
 
        WOApplication application = WOApplication.application();
        
        Method debugEnabledMethod = application.getClass().getMethod("debugEnabledForComponent", String.class);
        Boolean debugEnabled = (Boolean) debugEnabledMethod.invoke(application, componentName);

        Method setDebugEnabledMethod = application.getClass().getMethod("setDebugEnabledForComponent", boolean.class, String.class);
        setDebugEnabledMethod.invoke(application, Boolean.valueOf(!debugEnabled.booleanValue()), componentName);
      }
      catch (Throwable e) {
        e.printStackTrace();
      }
      
      return wocontext.page();
    }
    return super.invokeAction(worequest, wocontext);
  }
  
  @Override
  public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
    WOLipsUtilities.includePrototype(woresponse, wocontext);
    ERXResponseRewriter.addScriptResourceInHead(woresponse, wocontext, "WOLips", "wolips.js");
    super.appendToResponse(woresponse, wocontext);
  }
}
