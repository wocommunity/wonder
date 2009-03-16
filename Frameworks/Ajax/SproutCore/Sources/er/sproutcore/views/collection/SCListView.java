package er.sproutcore.views.collection;

import java.util.Set;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

public class SCListView extends SCCollectionView {
    public SCListView(String name, NSDictionary associations, WOElement element) {
        super(name, associations, element);
    }
    
    @Override
    protected void addProperties() {
    	super.addProperties();
    	addProperty("row_height");
    }
    
    @Override
    public String css(WOContext context) {
    	return super.css(context) + " focus";
    }

	@Override
	public Set<String> cssNames(WOContext context) {
		Set<String> cssNames = super.cssNames(context);
		cssNames.add("sc-list-view");
		return cssNames;
	}
}
