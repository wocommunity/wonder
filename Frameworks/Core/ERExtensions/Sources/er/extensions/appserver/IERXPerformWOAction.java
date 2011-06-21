package er.extensions.appserver;

import com.webobjects.appserver.WOActionResults;

/**
 * Simple interface for a delegate or controller class that has logic to perform a component action or a direct action.
 * 
 * This interface is useful for component action delegates where we want to
 * reuse the same WOComponent view (html/wod) but use different logic in specific actions.
 *
 * @author kieran
 */
public interface IERXPerformWOAction {

	public WOActionResults performAction();
}
