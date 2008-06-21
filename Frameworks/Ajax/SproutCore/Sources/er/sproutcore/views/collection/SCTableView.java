package er.sproutcore.views.collection;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

public class SCTableView extends SCCollectionView {

    public SCTableView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.doAppendToResponse(response, context);
    }
}
