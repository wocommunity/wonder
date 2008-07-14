package er.sproutcore.views;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

public class SCSegmentedView extends SCComponent {
    
    public SCSegmentedView(WOContext arg0) {
        super(arg0);
    }

    @Override
    public boolean isStateless() {
        return true;
    }
    
    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.doAppendToResponse(response, context);
    }
    
    public Object item() {
        return valueForBinding("item");
    }
    
    public NSArray list() {
        return (NSArray) valueForBinding("segments");
    }
    
    public String itemIdentifier() {
        return stringValueForBinding("itemIdentifier", null);
    }

    public String itemClass() {
        int index = list().indexOfObject(item());
        
        String extra = "";
        if(list().count() > 1) {
            extra = " segment " + (index == 0 ? "segment-left" : index == list().count() - 1 ? "segment-right" : "segment-inner");
        }
            
        return stringValueForBinding("itemClass", "") + extra;
    }

    public String itemLabel() {
        return stringValueForBinding("itemLabel", item().toString());
    }
}
