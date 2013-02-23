package er.extensions.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

/**
 * <p>
 * ERXEmbeddedPage allows you to wrap a section of your page and treat return
 * values from invokeAction as a replacement only for the this element and not
 * for the entire page. This allows you to write components that operate like a
 * sequence of top level elements, yet actually they live within a larger page.
 * </p>
 * <p>
 * As an example, you might have a multi-page form that you want to embed in 
 * a larger page.  Rather than write a top level component for your form that
 * has to deal with a big conditional, you can just wrap the form in an 
 * ERXEmbeddedPage and have your "next page" action methods just return the
 * next subcomponent in the sequence.
 * </p>
 * <p>
 * If you need to pass bindings in and out of later components in your sequence,
 * you can instead choose to use ERXSwitchEmbeddedPage, which will has similar
 * bindings rules to WOSwitchComponent (all components in the sequence must
 * support the set of bindings).
 * </p> 
 * 
 * @author mschrag
 */
public class ERXEmbeddedPage extends WODynamicGroup {
	public ERXEmbeddedPage(String s, NSDictionary nsdictionary, WOElement woelement) {
		super(s, nsdictionary, woelement);
	}

	public ERXEmbeddedPage(String s, NSDictionary nsdictionary, NSMutableArray nsmutablearray) {
		super(s, nsdictionary, nsmutablearray);
	}

	@Override
	public WOActionResults invokeChildrenAction(WORequest worequest, WOContext wocontext) {
		WOActionResults woactionresults = super.invokeChildrenAction(worequest, wocontext);

		if (woactionresults != null) {
			WOElement nextComponent;
			if (woactionresults instanceof WOElement) {
				nextComponent = (WOElement) woactionresults;
			}
			else {
				ERXResponseComponent responseComponent = new ERXResponseComponent(wocontext);
				responseComponent.setActionResults(woactionresults);
				nextComponent = responseComponent;
			}
			if (_children == null) {
				_children = new NSMutableArray(nextComponent);
			}
			else {
				_children.removeAllObjects();
				_children.addObject(nextComponent);
			}
			woactionresults = wocontext.page();
		}

		return woactionresults;
	}
}