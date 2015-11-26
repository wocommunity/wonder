package er.directtoweb.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOSession;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.foundation.NSKeyValueCoding;

import er.directtoweb.ERD2WFactory;

/**
 * Simple class that makes creating flows of pages a bit easier. Instead of the
 * normal (sender instanceof ...) in nextPage(), you can implement methods
 * <b>nextPageFrom</b>SomePageConfiguration() or
 * <b>nextPageFrom</b>PageNameOfCurrentPage(). For example:<pre><code>
 * public class CreateAssetWithSelectionDelegate extends ERDFlowDelegate {
 * 	  
 *    public WOComponent nextPageFromSelectAssetGroups() {
 *        ERD2WListPage page = parent(ERD2WListPage.class)
 *        if(page.selectedObjects().count() &gt; 0)
 *           ...
 *           return D2W.factory.pageForConfigurationNamed("CreateAsset");
 *        return page.pageWithName("MaybeNextTimePage");
 *    }
 *    
 *    public WOComponent nextPageFromCreateAsset() {
 *        ERD2WInspectPage page = parent(ERD2WInspectPage.class)
 *        if(page.wasObjectSaved())
 *           return page.pageWithName("ThankYouPage");
 *        return page.pageWithName("MaybeNextTimePage"); 
 *    }
 * }
 * 
 * ...
 *   D2WPage page = D2W.factory.pageForConfigurationNamed("SelectAssetGroups");
 *   page.setNextPageDelegate(new CreateAssetWithSelectionDelegate())
 * ...
 * 
 * </code></pre>
 * 
 * @author ak
 * 
 */
public class ERDFlowDelegate implements NextPageDelegate {

	private WOComponent _current;

	/**
	 * Set the current component.
	 * @param current
	 */
	protected void setCurrentComponent(WOComponent current) {
		_current = current;
	}

	/**
	 * Returns the current component.
	 */
	public WOComponent currentComponent() {
		return _current;
	}

	/**
	 * Returns the current page.
	 */
	public WOComponent page() {
		return currentComponent().context().page();
	}

	/**
	 * Returns the session.
	 */
	public WOSession session() {
		return currentComponent().session();
	}

	/**
	 * Returns the innermost enclosing component that extends the supplied
	 * clazz.
	 * 
	 * @param <T>
	 * @param clazz
	 */
	protected <T> T parent(Class<? extends T> clazz) {
		WOComponent curr = currentComponent();
		while (curr != null) {
			if (clazz.isAssignableFrom(curr.getClass())) {
				T t = (T) curr;
				return t;
			}
			curr = curr.parent();
		}
		return null;
	}

	/**
	 * Returns the page cast as the supplied clazz.
	 * 
	 * @param <T>
	 * @param clazz
	 */
	protected <T> T page(Class<? extends T> clazz) {
		return (T) currentComponent().context().page();
	}

	/**
	 * Returns either the pageConfiguration of the topmost page or the name of
	 * the topmost page.
	 */
	protected String pageName() {
		String pageName = ERD2WFactory.pageConfigurationFromPage(page());
		if (pageName == null) {
			pageName = page().name();
		}
		return pageName;
	}

	/**
	 * Calls up nextPageFrom + pageName()
	 */
	public final WOComponent nextPage(WOComponent sender) {
		setCurrentComponent(sender);
		return (WOComponent) NSKeyValueCoding.Utility.valueForKey(this, "nextPageFrom" + pageName());
	}

}
