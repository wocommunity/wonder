package er.sproutcore.example.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.sproutcore.views.SCComponent;

public class Buttons extends SCComponent {
    
    public Buttons(WOContext context) {
        super(context);
    }
    
    public NSArray values() {
        return new NSArray(new Object[]{ });
    }
}