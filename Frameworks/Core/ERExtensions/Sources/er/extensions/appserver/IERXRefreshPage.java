package er.extensions.appserver;

/**
 * A useful interface that can be used in WOComponent pages to indicate that the page is able to refresh
 * its data.
 * 
 * An example of usage might be where an action on a page that implements this interface dispatches a
 * background task. The background task in turn modifies the EOEnterpriseObject that is displayed on the
 * original page. However the EOEnterpriseObject is modified in a different EOObjectStore. The long response
 * logic might check for this interface and call it before returning to the original page to ensure that changes to the
 * EOEnterpriseObject are refreshed to reflect the changes that were made in the background task.
 * 
 * @author kieran
 *
 */
public interface IERXRefreshPage {
	public void refresh();
}
