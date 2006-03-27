package er.ajax;

import org.apache.log4j.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/**
 * This abstract (by design) superclass component isolate general utility methods.
 * 
 * @author Jean-Francois Veillette <jean.francois.veillette@gmail.com>
 * @version $Revision $, $Date $ <br>
 *          &copy; 2006 OS communications informatiques, inc. http://www.os.ca
 *          Tous droits réservés.
 */

public abstract class AjaxComponent extends WOComponent {

    /**
     * Key that flags the session to not save the page in the cache.
     */
    public static final String AJAX_REQUEST_KEY = "AJAX_REQUEST_KEY";

    /** logging */
    protected Logger log = Logger.getLogger(getClass());

    public AjaxComponent(WOContext context) {
        super(context);
    }

    /**
     * Utility to get the value of a binding or a default value if none is
     * supplied.
     * @param name
     * @param defaultValue
     * @return
     */
    public Object valueForBinding(String name, Object defaultValue) {
        Object value = defaultValue;
        if(hasBinding(name)) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Creates a response for the given context (which can be null), sets
     * the charset to UTF-8, the connection to keep-alive and flags it as
     * a Ajax request by adding an AJAX_REQUEST_KEY header. You can check this
     * header in the session to decide if you want to save the request or not.
     * @param context
     * @return
     */
    protected WOResponse createResponse(WOContext context) {
        WOApplication app = WOApplication.application();
        WOResponse response = app.createResponseInContext(context);

        // Encode using UTF-8, although We are actually ASCII clean as all
        // unicode data is JSON escaped using backslash u. This is less data
        // efficient for foreign character sets but it is needed to support
        // naughty browsers such as Konqueror and Safari which do not honour the
        // charset set in the response
        response.setHeader("text/plain; charset=utf-8", "content-type");
        response.setHeader("Connection", "keep-alive");
        response.setHeader(AJAX_REQUEST_KEY, AJAX_REQUEST_KEY);
        return response;
    }

    private String htmlCloseHead() {
        String head = System.getProperty("er.ajax.AJComponent.htmlCloseHead");
        return (head == null ? "</head>" : head);
    }
    
    /**
     * Returns the userInfo dictionary if the supplied message and replaces it with a mutable
     * version if it isn't already one.
     * @param message
     * @return
     */
    protected NSMutableDictionary mutableUserInfo(WOMessage message) {
        NSDictionary dict = message.userInfo();
        NSMutableDictionary result = null;
        if(dict == null) {
            result = new NSMutableDictionary();
            context().response().setUserInfo(result);
        } else {
            if(dict instanceof NSMutableDictionary) {
                result = (NSMutableDictionary)dict;
            } else {
                result = dict.mutableClone();
                message.setUserInfo(result);
            }
        }
        return result;
    }

    /**
     * Utility to add the given text before the given tag. Used to add stuff in the HEAD.
     * @param res
     * @param content
     * @param tag
     */
    private void insertInResponseBeforeTag(WOResponse res, String content, String tag) {
        String stream = res.contentString();
        int idx = stream.indexOf(tag);
        if(idx >= 0) {
            String pre = stream.substring(0,idx);
            String post = stream.substring(idx, stream.length());
            res.setContent(pre+content+post);
        }
    }

    /**
     * Adds a script tag with a correct resource url in the html head tag if it isn't already present in 
     * the response.
     * @param res
     * @param fileName
     */
    protected void addScriptResourceInHead(WOResponse res, String fileName) {
        NSMutableDictionary userInfo = mutableUserInfo(context().response());
        if(userInfo.objectForKey(fileName) == null) {
            userInfo.setObjectForKey(fileName, fileName);
            WOResourceManager rm = application().resourceManager();
            String url = rm.urlForResourceNamed(fileName, "Ajax", session().languages(), context().request());
            String js = "<script type=\"text/javascript\" src=\""+ url +"\"></script>";
            insertInResponseBeforeTag(res, js, htmlCloseHead());
        }
    }


    /**
     * Adds javascript code in a script tag in the html head tag. 
     * @param res
     * @param script
     */
    protected void addScriptCodeInHead(WOResponse res, String script) {
        String js = "<script type=\"text/javascript\"><!--\n" + script +"\n//--></script>";
        insertInResponseBeforeTag(res, js, htmlCloseHead());
    }

    /**
     * Execute the request, if it's comming from our action, then invoke the
     * ajax handler and put the key <code>AJAX_REQUEST_KEY</code> in the
     * request userInfo dictionary (<code>request.userInfo()</code>).
     */
    public WOActionResults invokeAction(WORequest request, WOContext context) {
        Object result = null;
        String elementID = context.elementID();
        String senderID = context.senderID();
        WOComponent wocomponent = context.component();
        if (elementID != null && elementID.equals(senderID)) {
            result = handleRequest(request, context);
            NSMutableDictionary dict = mutableUserInfo(context().request());
            dict.takeValueForKey(AJAX_REQUEST_KEY, AJAX_REQUEST_KEY);
        } else {
            result = super.invokeAction(request, context);
        }
        return (WOActionResults) result;
    }

    /**
     * Provides a unique name for this component, based on the element id.
     * @return
     */
    public String scriptBaseName() {
        return "wo_" + context().elementID().replaceAll("\\.", "_");
    }

    /**
     * Overridden to call {@see #addRequiredWebResources(WOResponse)}.
     */
    public void appendToResponse(WOResponse res, WOContext ctx) {
        super.appendToResponse(res, ctx);
        addRequiredWebResources(res);
    }

    /*
    protected void superAppendToResponse(WOResponse res, WOContext ctx) {
        super.appendToResponse(res, ctx);
    }
    
    protected WOActionResults superInvokeAction(WORequest request, WOContext context) {
        return super.invokeAction(request, context);
    }
    */
    
    /**
     * Override this method to append the needed scripts for this component.
     * @param res
     */
    protected abstract void addRequiredWebResources(WOResponse res);
    
    /**
     * Override this method to return the response for an Ajax request.
     * @param request
     * @param context
     * @return
     */
    protected abstract WOActionResults handleRequest(WORequest request, WOContext context);

}
