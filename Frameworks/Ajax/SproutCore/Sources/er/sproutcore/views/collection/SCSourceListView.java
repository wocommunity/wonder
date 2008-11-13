package er.sproutcore.views.collection;

import java.util.Set;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

public class SCSourceListView extends SCCollectionView {

    public SCSourceListView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }

	@Override
	public Set<String> cssNames(WOContext context) {
		Set<String> cssNames = super.cssNames(context);
		cssNames.add("sc-source-list-view");
		return cssNames;
	}
}
