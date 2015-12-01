package er.modern.look.pages;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.ERD2WUtilities;
import com.webobjects.directtoweb.ListPageInterface;

import er.directtoweb.pages.templates.ERD2WListPageTemplate;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Modernized list page.
 * 
 * @d2wKey printerButtonComponentName
 * @d2wKey emptyListComponentName
 * @d2wKey headerComponentName
 * @d2wKey entity
 * @d2wKey allowsFiltering
 * @d2wKey repetitionComponentName
 * @d2wKey batchNavigationBarComponentName
 * @d2wKey displayNameForEntity
 * @d2wKey backgroundColorForTable
 * @d2wKey pageWrapperName
 * @d2wKey returnButtonLabel
 * 
 * @author davidleber
 */
public class ERMODListPage extends ERD2WListPageTemplate implements ListPageInterface {
  /**
   * Do I need to update serialVersionUID?
   * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
   * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
   */
  private static final long serialVersionUID = 1L;

	public interface Keys extends ERD2WListPageTemplate.Keys {
		public static final String useAjaxControlsWhenEmbedded = "useAjaxControlsWhenEmbedded";
		public static final String parentPageConfiguration = "parentPageConfiguration";
		public static final String idForParentMainContainer = "idForParentMainContainer";
		public static final String idForMainContainer = "idForMainContainer";
		public static final String allowInlineEditing = "allowInlineEditing";
		public static final String shouldShowCancelButton = "shouldShowCancelButton";
	}
	
	public ERMODListPage(WOContext wocontext) {
		super(wocontext);
	}
	
	/**
	 * Show the cancel button. From parent: Should we show the cancel button? It's only visible 
	 * when we have a nextPage set up. Overridden to allow us to show the cancel button if query 
	 * page is embedded and shouldShowCancelButton is true.
	 */
	@Override
	public boolean showCancel() {
		boolean showCancelButton = ERXValueUtilities.booleanValue(d2wContext().valueForKeyPath(Keys.shouldShowCancelButton));
		Object parentConfig = d2wContext().valueForKeyPath(Keys.parentPageConfiguration);
		return super.showCancel() || (parentConfig != null && showCancelButton);
	}
	
	/**
	 * Perform the back action. Overridden to handle in-line and ajax behaviour.
	 * If in-line then we will usually just return null the current page, and if we're using ajax
	 * we will need to reset the current inlineTask.
	 */
	@Override
	public WOComponent backAction() {
		WOComponent result = nextPageFromDelegate();
		boolean useAjaxWhenEmbedded = ERXValueUtilities.booleanValue(d2wContext().valueForKey(Keys.useAjaxControlsWhenEmbedded));
		boolean allowInline = ERXValueUtilities.booleanValue(d2wContext().valueForKey(Keys.allowInlineEditing));
		if (result == null) {
			result = nextPage();
			if (result == null && !allowInline) {
				result = (WOComponent) D2W.factory().queryPageForEntityNamed(entity().name(), session());
			}
		}
		if (useAjaxWhenEmbedded) {
			if (parent() != null) {
				D2WPage parent = (D2WPage)ERD2WUtilities.enclosingPageOfClass(this, D2WPage.class);
				if (parent != null) 
					parent.takeValueForKeyPath(null, "d2wContext.inlineTask");
			}
		}
		return result;
	}

}
