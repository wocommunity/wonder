package er.rest.format;

import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;

/**
 * IERXRestWriter provides the interface for generating the output of a restful request.
 * 
 * @author mschrag
 */
public interface IERXRestWriter {
	/**
	 * Called at the end of a request to produce the output to the user.
	 * 
	 * @param node
	 *            the node to render
	 * @param response
	 *            the response to write into
	 * @param context
	 *            the REST context
	 */
	public void appendHeadersToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestContext context);
	
	/**
	 * Called at the end of a request to produce the output to the user.
	 * 
	 * @param node
	 *            the node to render
	 * @param response
	 *            the response to write into
	 * @param delegate
	 *            the REST delegate
	 * @param context
	 *            the REST context
	 */
	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response, ERXRestFormat.Delegate delegate, ERXRestContext context);
}
