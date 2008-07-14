package er.sproutcore.views;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXStringUtilities;
import er.sproutcore.SCItem;

public class SCSegmentedView extends SCComponent {
    
    public SCSegmentedView(WOContext context) {
        super(context);
        removeProperty("item");
        removeProperty("segments");
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
    
    public String itemOutlet() {
    	return item() + "Button";
    }

    public String containerID() {
      SCItem item = SCItem.currentItem();
      return (item.isRoot()) ? item.id() : null;
    }
    
    public String containerClass() {
      StringBuffer css = new StringBuffer();
      if (!booleanValueForBinding("enabled", true)) {
        css.append("disabled segments disabled_segment");
      }
      else {
        css.append("segments");
        css.append(" ");
        css.append(SCItem.currentItem().id());
      }
      return css.toString();
    }
    
    public String itemClass() {
      // disabled segments disabled_segment
        int index = list().indexOfObject(item());
        
        String extra = "";
        if(list().count() > 1) {
            extra = " segment " + (index == 0 ? "segment-left" : index == list().count() - 1 ? "segment-right" : "segment-inner");
        }
            
        return stringValueForBinding("itemClass", "") + extra;
    }

    public String itemLabel() {
    	String defaultLabel = item().toString();
    	defaultLabel = ERXStringUtilities.displayNameForKey(defaultLabel);
        return stringValueForBinding("itemLabel", defaultLabel);
    }
}
