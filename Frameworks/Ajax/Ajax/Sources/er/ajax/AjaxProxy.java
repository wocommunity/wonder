package er.ajax;

import java.util.NoSuchElementException;

import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCResult;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.json.JSONBridge;
import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.appserver.ERXWOContext;

/**
 * A RPC mechanism to call methods on the Java server side from a JavaScript client.
 * It Handles javascript-java communication (client-server) between the javascript world running in a web browser and the
 * java world, running in a WebObject application. This remote-procedure-call communication is done using json protocol,
 * as implemented by the JSON-RPC library.
 * <p>
 * This component generates javascript code that will initialize a variable that will be the starting point for rpc
 * communication. The name of this variable is given in the <code>name</code> binding. There will be an object, server
 * side that will be the proxy and will handle the request. You can define the proxy on the server side (it has to be a
 * JSONRPCBridge). The proxy will be your window to the java world, from there you can access your java objects from the
 * javascript side. The name for this java variable is <code>proxyName</code>. By default, it will configure the
 * parent component as one java proxy object and name it <code>wopage</code> from the javascript side.
 * (WARNING: DON'T DO THIS. YOU SHOULD ALWAYS PROVIDE A SEPARATE PROXY OBJECT. See below.) The JSONRPCBridge
 * object is created if not given as a binding. If the binding is there but the value is null, it will create the bridge
 * then push it to the binding. That way, you can configure a single bridge for multiple proxy objects. It is of good
 * practice to provide a value in a binding (at least a binding) so that the object is not created on every ajax
 * request.
 * </p>
 * <p>
 * The <em>proxy</em> object will be the one visible for RPC from the javascript world. For example, the following
 * binding:<br>
 * <code>
 * PageProxy : AjaxProxy {<br>
 *      proxyName = "wopage";<br>
 *      name = "jsonrpc";<br>
 * }</code> <br>
 * will be used as follow : <table border="1">
 * <tr>
 * <th style="width:50%">JavaScript (client)</th>
 * <th style="width:50%">Java (server)</th>
 * </tr>
 * <tr>
 * <td><pre><code>
 * <em>// index of the selection on the client</em>
 * var idx = 3; // fixed value for the demo
 * <em>// using rpc, ask our page to get the name for that client for that index.</em>
 * var nom = jsonrpc.wopage.clientNameAtIndex(idx);
 * </code></pre></td>
 * <td><pre><code>
 * <em>// Java-WebObject side, we receive the index and simple return what they asked for, as for any java call.</em>
 * public String clientNameAtIndex(int i) {
 *     return <em>"something!"+i</em>;
 * }
 * </code></pre></td>
 * </tr>
 * </table>
 * <p>
 * <b>Every public instance and static method of the proxy object is published on the javascript side.</b>
 * Remember that if no proxy object is given, it will use the parent component, which is the component in which this component is embedded.
 * This is probably a very bad idea, as it possible to call something like 'valueForKeyPath("application.terminate")' from the client side.
 * And you usually don't control the client.</p>
 * <p>
 * You currently cannot publish more than one proxy object under a given bridge "name" (e.g. "jsonrpc"). The initialization code will only be
 * called for the first object.</p>
 * <p>
 * Note: The component used to have a binding "JSONRPCBridge". This seems to been renamed to "AjaxBridge". Both bindings are supported. In
 * future versions the JSONRPCBridge may be removed.</p>   
 * <h2>Todo</h2>
 * <ul>
 * <li> Remove support to automatically proxy the parent component. Maybe even warn about proxying WOComponents per se.</li> 
 * <li> Complete the JSON-RPC integration to be able to leverage all possibilities of that library (foreign references,
 * etc.).</li>
 * <li> Allow the use of interfaces to reduce the number of published methods ({@link JSONRPCBridge#registerObject(Object, Object, Class)}</li>
 * <li> Allow multiple proxy objects under a given bridge name.</li> 
 * <li> Update org.jabsorb to latest version (1.3.2) </li>
 * <li> Extend JSONRPCBridge to only publish methods marked with an annotation.</li>  
 * </ul>
 * 
 * @binding proxy Server side object (Java) that will be visible for rpc communication (Javascript).  
 * If no object is bound, the parent() object is assigned by default. You should ALWYAS provide an object.
 * @binding proxyName Client side name (Javascript) used to identify the proxy (Java) from the bridge object.
 * @binding name Client side name (Javascript) of the bridge object.
 * @binding AjaxBridge Server side object (Java) used to handle the request.  If no value is bound, a new 
 * object is created for every ajax request.  If a binding is there but has a null value, a new 
 * object will be created and pushed to the binding so that this new object can be shared 
 * for multiple proxies.
 * @binding lazy (default false) if true, the proxy is only initialized on-demand, rather than on-load. The initialization requests 
 * the available methods from the proxy object.
 * 
 * @author Jean-François Veillette &lt;jfveillette@os.ca&gt;
 * @version $Revision $, $Date $ <br>
 *          &copy; 2005-2006 OS communications informatiques, inc. http://www.os.ca
 *          Tous droits réservés.
 */
public class AjaxProxy extends AjaxComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(AjaxProxy.class);

	public AjaxProxy(WOContext context) {
		super(context);
	}

	/**
	 * Overridden because the component is stateless
	 */
	@Override
	public boolean isStateless() {
		return true;
	}
	
	/**
	 * The binding is now called "AjaxBridge", but we also used "JSONRPCBridge" in the past. 
	 * @return the JSONRPCBridge to use
	 */
	private JSONRPCBridge getBridgeBinding() {
		Object bridge = valueForBinding("AjaxBridge");
		if (bridge == null) {
			bridge = valueForBinding("JSONRPCBridge");
		}
		return (JSONRPCBridge)bridge;
	}
	
	/**
	 * Push the bridge object to "AjaxBridge" and "JSONRPCBridge" if available. 
	 * @param bridge the bridge object to set in the binding
	 */
	private void setBridgeBinding(JSONRPCBridge bridge)	{
		if (canSetValueForBinding("AjaxBridge")) {
			setValueForBinding(bridge, "AjaxBridge");
		}
		if (canSetValueForBinding("JSONRPCBridge")) {
			setValueForBinding(bridge, "JSONRPCBridge");
		}
	}

	/**
	 * Adds the jsonrpc.js script to the head in the response if not already present and also adds a javascript proxy
	 * for the supplied bridge under the name "JSONRPC_&lt;variableName&gt;".
	 * 
	 * @param res the response to write into
	 */
	@Override
	protected void addRequiredWebResources(WOResponse res) {
		addScriptResourceInHead(res, "jsonrpc.js");

		NSMutableDictionary userInfo = ERXWOContext.contextDictionary();
		String name = (String) valueForBinding("name");
		String key = "JSONRPC_" + name;
		Object oldValue = userInfo.objectForKey(key);
		Object bridge = getBridgeBinding();
		if (bridge == null) {
			bridge = NSKeyValueCoding.NullValue;
		}
		if (oldValue == null) {
			// add the javascript variable 'name' only if not already in the
			// response
			userInfo.setObjectForKey(bridge, key);
			String jsonRpcJavascript;
			if (booleanValueForBinding("lazy", false)) {
			    String varName = "_" + name;
			    jsonRpcJavascript = "function " + name + "(callback) { if (typeof " + varName + " == 'undefined') { " + varName + "=new JSONRpcClient(callback, '" + AjaxUtils.ajaxComponentActionUrl(context()) + "'); } else { callback(); } }";
			}
			else {
			    jsonRpcJavascript = name + "=new JSONRpcClient('" + AjaxUtils.ajaxComponentActionUrl(context()) + "');";
			}
			ERXResponseRewriter.addScriptCodeInHead(res, context(), jsonRpcJavascript, key);
		}
		else {
			// ok, the javascript variable 'name' is already in the response,
			// was it referencing the same JSONRPCBridge object ?
			if (bridge != oldValue) {
				// well, it wasn't ... there is high chance of unexpected
				// problem. just warn the user (programmer), that it might cause
				// problem.
				log.warn("JSONRPCProxy detected a conflict. You defined the javascript variable '{}' multiple times, and linked to differents proxy objects: <{}> and <{}>", name, bridge, oldValue);
			}
		}
	}

	/** Ask the an JSONRPCBridge object to handle the json request. */
	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOResponse response = AjaxUtils.createResponse(request, context);
		response.setHeader("application/json", "content-type");

		String inputString = request.contentString();
		log.debug("AjaxProxy.handleRequest: input = {}", inputString);

		// Process the request
		JSONObject input = null;
		Object output = null;
		try {
			input = new JSONObject(inputString);

			Object proxy;
			if (canGetValueForBinding("proxy")) {
				proxy = valueForBinding("proxy");
			}
			else {
				proxy = parent();
				log.warn("No proxy binding given, so using parent component. This is probably a very bad idea.");
			}
			String proxyName = (String) valueForBinding("proxyName");

			JSONRPCBridge bridge = getBridgeBinding();
			if (bridge == null) {
				bridge = JSONBridge.createBridge();
				setBridgeBinding(bridge);
			}
			bridge.registerObject(proxyName, proxy);
			output = bridge.call(new Object[] { request, context, response, proxy }, input);
		}
		catch (NoSuchElementException e) {
			log.error("No method in request");
			output = JSONRPCResult.MSG_ERR_NOMETHOD;
		}
		catch (Exception e) {
			log.error("Exception", e);
			output = JSONRPCResult.MSG_ERR_NOMETHOD;
		}

		// Write the response
		log.debug("AjaxProxy.handleRequest: output = {}", output);
		response.appendContentString(output.toString());
		return response;
	}
}
