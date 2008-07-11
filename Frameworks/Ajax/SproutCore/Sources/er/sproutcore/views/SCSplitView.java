package er.sproutcore.views;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCSplitView extends SCView {

    public SCSplitView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
        moveProperty("direction", "layoutDirection");
    }

    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.doAppendToResponse(response, context);
    }
}
