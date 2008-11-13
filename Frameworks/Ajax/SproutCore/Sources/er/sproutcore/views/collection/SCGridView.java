package er.sproutcore.views.collection;

import java.util.Set;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxOption;

public class SCGridView extends SCCollectionView {
	public SCGridView(String name, NSDictionary associations, WOElement element) {
		super(name, associations, element);
	}

	@Override
	protected void addProperties() {
		super.addProperties();
		addProperty("row_height");
		addProperty("column_width");
		addProperty("example_view", AjaxOption.SCRIPT);
	}

	@Override
	public Set<String> cssNames(WOContext context) {
		Set<String> cssNames = super.cssNames(context);
		cssNames.add("sc-grid-view");
		return cssNames;
	}
}
