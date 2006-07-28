package er.ajax;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

public class AjaxUtils {
  private static String HTML_CLOSE_HEAD = System.getProperty("er.ajax.AJComponent.htmlCloseHead");

  /**
   * Key that tells the session not to store the current page. Checks both the 
   * response userInfo and the response headers if this key is present. The value doesn't matter,
   * but you need to update the corresponding value in ERXSession.  This is to keep the dependencies
   * between the two frameworks independent.
   */
  public static final String DONT_STORE_PAGE = "ERXSession.DontStorePage";

  /*
   * Key that is used to specify that a page should go in the replacement cache instead of
   * the backtrack cache.  This is used for Ajax components that actually generate component
   * actions in their output.  The value doesn't matter, but you need to update the 
   * corresponding value in ERXSession.  This is to keep the dependencies between the two
   * frameworks independent.
   */
  public static final String PAGE_REPLACEMENT_CACHE_LOOKUP_KEY = "pageCacheKey";

  /*
   * Key that is used during an Ajax form posting so that WOContext gets _wasFormSubmitted
   * set to true.  If this value is changed, you must also change ERXWOForm.
   */
  public static final String FORCE_FORM_SUBMITTED_KEY = "_forceFormSubmitted";

  public static void setPageReplacementCacheKey(WOContext _context, String _key) {
    _context.response().setHeader(_key, AjaxUtils.PAGE_REPLACEMENT_CACHE_LOOKUP_KEY);
  }
  
  /**
   * Checks if the message is an Ajax message by looking for the AJAX_REQUEST_KEY 
   * in the header and in the userInfo.
   * @param message
   * @return
   */
  public static boolean isAjaxMessage(WOMessage message) {
      return (message.headerForKey(AjaxUtils.DONT_STORE_PAGE) != null ||
              (message.userInfo() != null && message.userInfo().objectForKey(AjaxUtils.DONT_STORE_PAGE) != null));
  }

  /**
   * Creates a response for the given context (which can be null), sets
   * the charset to UTF-8, the connection to keep-alive and flags it as
   * a Ajax request by adding an AJAX_REQUEST_KEY header. You can check this
   * header in the session to decide if you want to save the request or not.
   * @param context
   * @return
   */
  public static WOResponse createResponse(WOContext context) {
    WOApplication app = WOApplication.application();
    WOResponse response = app.createResponseInContext(context);
    if(context != null) {
    	context._setResponse(response);
    }
    // Encode using UTF-8, although We are actually ASCII clean as all
    // unicode data is JSON escaped using backslash u. This is less data
    // efficient for foreign character sets but it is needed to support
    // naughty browsers such as Konqueror and Safari which do not honour the
    // charset set in the response
    response.setHeader("text/plain; charset=utf-8", "content-type");
    response.setHeader("Connection", "keep-alive");
    response.setHeader(AjaxUtils.DONT_STORE_PAGE, AjaxUtils.DONT_STORE_PAGE);
    return response;
  }

  /**
   * Returns the userInfo dictionary if the supplied message and replaces it with a mutable
   * version if it isn't already one.
   * @param message
   * @return
   */
  public static NSMutableDictionary mutableUserInfo(WOMessage message) {
    NSDictionary dict = message.userInfo();
    NSMutableDictionary result = null;
    if (dict == null) {
      result = new NSMutableDictionary();
      message.setUserInfo(result);
    }
    else {
      if (dict instanceof NSMutableDictionary) {
        result = (NSMutableDictionary) dict;
      } else {
        result = dict.mutableClone();
        message.setUserInfo(result);
      }
    }
    return result;
  }

  private static String htmlCloseHead() {
    String head = AjaxUtils.HTML_CLOSE_HEAD;
    return (head == null ? "</head>" : head);
  }

  /**
   * Utility to add the given text before the given tag. Used to add stuff in the HEAD.
   * @param response
   * @param content
   * @param tag
   */
  public static void insertInResponseBeforeTag(WOResponse response, String content, String tag) {
    String stream = response.contentString();
    int idx = stream.indexOf(tag);
    if (idx < 0) {
      idx = stream.toLowerCase().indexOf(tag.toLowerCase());
    }
    if (idx >= 0) {
      String pre = stream.substring(0, idx);
      String post = stream.substring(idx, stream.length());
      response.setContent(pre + content + post);
    }
  }

  /**
   * Adds a script tag with a correct resource url in the html head tag if it isn't already present in 
   * the response.
   * @param response
   * @param fileName
   */
  public static void addScriptResourceInHead(WOContext context, WOResponse response, String fileName) {
    NSMutableDictionary userInfo = AjaxUtils.mutableUserInfo(context.response());
    if (userInfo.objectForKey(fileName) == null) {
      userInfo.setObjectForKey(fileName, fileName);
      WOResourceManager rm = WOApplication.application().resourceManager();
      String url = rm.urlForResourceNamed(fileName, "Ajax", context.session().languages(), context.request());
      String js = "<script type=\"text/javascript\" src=\"" + url + "\"></script>";
      AjaxUtils.insertInResponseBeforeTag(response, js, AjaxUtils.htmlCloseHead());
    }
  }

  /**
   * Adds javascript code in a script tag in the html head tag. 
   * @param response
   * @param script
   */
  public static void addScriptCodeInHead(WOResponse response, String script) {
    String js = "<script type=\"text/javascript\"><!--\n" + script + "\n//--></script>";
    AjaxUtils.insertInResponseBeforeTag(response, js, AjaxUtils.htmlCloseHead());
  }

  public static String toSafeElementID(String elementID) {
    return "wo_" + elementID.replace('.', '_');
  }

  public static boolean shouldHandleRequest(WORequest request, WOContext context) {
    String elementID = context.elementID();
    String senderID = context.senderID();
    String invokeWOElementID = request.stringFormValueForKey("invokeWOElementID");
    // WOComponent wocomponent = context.component();
    // System.out.println(wocomponent.name() + " e:" + elementID + " - s: " + senderID + " - in " + invokeWOElementID);
    boolean shouldHandleRequest = elementID != null && (elementID.startsWith(senderID) || (invokeWOElementID != null && invokeWOElementID.equals(elementID)));
    return shouldHandleRequest;
  }

  public static void updateMutableUserInfoWithAjaxInfo(WOContext context) {
    NSMutableDictionary dict = AjaxUtils.mutableUserInfo(context.response());
    dict.takeValueForKey(AjaxUtils.DONT_STORE_PAGE, AjaxUtils.DONT_STORE_PAGE);
  }
}
