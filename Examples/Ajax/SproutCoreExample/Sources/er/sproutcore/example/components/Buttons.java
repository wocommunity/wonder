package er.sproutcore.example.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.components.ERXComponent;
import er.sproutcore.views.SCComponent;

public class Buttons extends ERXComponent {
    
    public Buttons(WOContext context) {
        super(context);
    }
    
    public NSArray values() {
        return new NSArray(new Object[]{ });
    }
}