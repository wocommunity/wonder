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
   * @param _message
   * @return
   */
  public static NSMutableDictionary mutableUserInfo(WOMessage _message) {
    NSDictionary dict = _message.userInfo();
    NSMutableDictionary result = null;
    if (dict == null) {
      result = new NSMutableDictionary();
      _message.setUserInfo(result);
    }
    else {
      if (dict instanceof NSMutableDictionary) {
        result = (NSMutableDictionary) dict;
      } else {
        result = dict.mutableClone();
        _message.setUserInfo(result);
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
   * @param _response
   * @param _content
   * @param _tag
   */
  public static void insertInResponseBeforeTag(WOResponse _response, String _content, String _tag) {
    String stream = _response.contentString();
    int idx = stream.indexOf(_tag);
    if (idx < 0) {
      idx = stream.toLowerCase().indexOf(_tag.toLowerCase());
    }
    if (idx >= 0) {
      String pre = stream.substring(0, idx);
      String post = stream.substring(idx, stream.length());
      _response.setContent(pre + _content + post);
    }
  }

  /**
   * Adds a script tag with a correct resource url in the html head tag if it isn't already present in 
   * the response.
   * @param _response
   * @param _fileName
   */
  public static void addScriptResourceInHead(WOContext _context, WOResponse _response, String _fileName) {
    NSMutableDictionary userInfo = AjaxUtils.mutableUserInfo(_context.response());
    if (userInfo.objectForKey(_fileName) == null) {
      userInfo.setObjectForKey(_fileName, _fileName);
      WOResourceManager rm = WOApplication.application().resourceManager();
      String url = rm.urlForResourceNamed(_fileName, "Ajax", _context.session().languages(), _context.request());
      String js = "<script type=\"text/javascript\" src=\"" + url + "\"></script>";
      AjaxUtils.insertInResponseBeforeTag(_response, js, AjaxUtils.htmlCloseHead());
    }
  }

  /**
   * Adds javascript code in a script tag in the html head tag. 
   * @param _response
   * @param _script
   */
  public static void addScriptCodeInHead(WOResponse _response, String _script) {
    String js = "<script type=\"text/javascript\"><!--\n" + _script + "\n//--></script>";
    AjaxUtils.insertInResponseBeforeTag(_response, js, AjaxUtils.htmlCloseHead());
  }

  public static String toSafeElementID(String _elementID) {
    String elementID = _elementID;
    return "wo_" + elementID.replace('.', '_');
  }

  public static boolean shouldHandleRequest(WORequest _request, WOContext _context) {
    String elementID = _context.elementID();
    String senderID = _context.senderID();
    String invokeWOElementID = _request.stringFormValueForKey("invokeWOElementID");
    WOComponent wocomponent = _context.component();
    boolean shouldHandleRequest = elementID != null && (elementID.equals(senderID) || (invokeWOElementID != null && invokeWOElementID.equals(elementID)));
    return shouldHandleRequest;
  }

  public static void updateMutableUserInfoWithAjaxInfo(WOContext _context) {
    NSMutableDictionary dict = AjaxUtils.mutableUserInfo(_context.response());
    dict.takeValueForKey(AjaxUtils.DONT_STORE_PAGE, AjaxUtils.DONT_STORE_PAGE);
  }
}
