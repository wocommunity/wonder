package er.sproutcore.views.collection;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCListView extends SCCollectionView {

    public SCListView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public String cssName(WOContext context) {
    	return "sc-collection-view sc-list-view";
    }
    
    @Override
    public String css(WOContext context) {
    	return super.css(context) + " focus";
    }
    
    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.doAppendToResponse(response, context);
    }
}
