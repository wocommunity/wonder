package er.rest.format;

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
	 */
	public void appendToResponse(ERXRestRequestNode node, IERXRestResponse response);
}
